package com.zfbml.aggregate.torrent

import java.io.BufferedInputStream
import java.io.Closeable
import java.io.File
import java.io.OutputStream
import java.io.RandomAccessFile
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LocalTorrentFileServer : Closeable {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val routes = ConcurrentHashMap<String, ServedFile>()
    private val routeByPath = ConcurrentHashMap<String, String>()

    @Volatile
    private var serverSocket: ServerSocket? = null

    @Volatile
    private var acceptJob: Job? = null

    fun serve(
        file: File,
        totalSizeBytes: Long,
        mimeType: String = mimeTypeFor(file.name),
        onRangeRequested: (Long) -> Unit = {},
    ): String {
        val server = ensureStarted()
        val pathKey = file.canonicalPath
        val token = routeByPath.getOrPut(pathKey) { UUID.randomUUID().toString() }
        routes[token] = ServedFile(
            file = file,
            totalSizeBytes = totalSizeBytes,
            mimeType = mimeType,
            onRangeRequested = onRangeRequested,
        )
        val encodedName = URLEncoder.encode(file.name, StandardCharsets.UTF_8.name())
        return "http://127.0.0.1:${server.localPort}/$token/$encodedName"
    }

    override fun close() {
        routes.clear()
        routeByPath.clear()
        runCatching { serverSocket?.close() }
        acceptJob?.cancel()
        serverSocket = null
        acceptJob = null
    }

    @Synchronized
    private fun ensureStarted(): ServerSocket {
        serverSocket?.let { return it }
        val socket = ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))
        serverSocket = socket
        acceptJob = scope.launch {
            while (isActive && !socket.isClosed) {
                val client = runCatching { socket.accept() }.getOrNull() ?: continue
                launch { handle(client) }
            }
        }
        return socket
    }

    private fun handle(socket: Socket) {
        socket.use { client ->
            client.soTimeout = SOCKET_TIMEOUT_MS
            val input = BufferedInputStream(client.getInputStream())
            val output = client.getOutputStream()
            val requestLine = readHttpLine(input) ?: return
            val requestParts = requestLine.split(" ")
            if (requestParts.size < 2) {
                output.writeStatus(400, "Bad Request")
                return
            }

            val method = requestParts[0]
            val target = requestParts[1]
            val headers = readHeaders(input)
            if (method != "GET" && method != "HEAD") {
                output.writeStatus(405, "Method Not Allowed")
                return
            }

            val servedFile = resolveRoute(target)
            if (servedFile == null) {
                output.writeStatus(404, "Not Found")
                return
            }

            serveFile(output, method == "HEAD", servedFile, headers["range"])
        }
    }

    private fun resolveRoute(target: String): ServedFile? {
        val cleanPath = target.substringBefore('?').trimStart('/')
        val token = cleanPath.substringBefore('/').takeIf { it.isNotBlank() } ?: return null
        return routes[URLDecoder.decode(token, StandardCharsets.UTF_8.name())]
    }

    private fun serveFile(output: OutputStream, headOnly: Boolean, servedFile: ServedFile, rangeHeader: String?) {
        if (!servedFile.file.exists()) {
            output.writeStatus(404, "Not Found")
            return
        }

        val range = parseRange(rangeHeader, servedFile.totalSizeBytes)
        if (range == null) {
            output.writeStatus(416, "Range Not Satisfiable")
            return
        }

        servedFile.onRangeRequested(range.start)

        val status = if (range.partial) "206 Partial Content" else "200 OK"
        val bodyLength = range.end - range.start + 1L
        output.writeAscii(
            buildString {
                append("HTTP/1.1 ").append(status).append("\r\n")
                append("Content-Type: ").append(servedFile.mimeType).append("\r\n")
                append("Accept-Ranges: bytes\r\n")
                append("Content-Length: ").append(bodyLength).append("\r\n")
                append("Content-Range: bytes ")
                    .append(range.start)
                    .append('-')
                    .append(range.end)
                    .append('/')
                    .append(servedFile.totalSizeBytes)
                    .append("\r\n")
                append("Cache-Control: no-store\r\n")
                append("Connection: close\r\n")
                append("\r\n")
            },
        )

        if (!headOnly) {
            streamGrowingFile(output, servedFile.file, range.start, bodyLength)
        }
    }

    private fun streamGrowingFile(output: OutputStream, file: File, start: Long, length: Long) {
        var position = start
        var remaining = length
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var waitedMs = 0L

        RandomAccessFile(file, "r").use { raf ->
            while (remaining > 0L) {
                val available = file.length() - position
                if (available <= 0L) {
                    if (waitedMs >= GROWING_FILE_WAIT_TIMEOUT_MS) {
                        throw SocketTimeoutException("Timed out waiting for torrent data at byte $position")
                    }
                    Thread.sleep(GROWING_FILE_WAIT_STEP_MS)
                    waitedMs += GROWING_FILE_WAIT_STEP_MS
                    continue
                }

                waitedMs = 0L
                val readSize = minOf(buffer.size.toLong(), available, remaining).toInt()
                raf.seek(position)
                val read = raf.read(buffer, 0, readSize)
                if (read <= 0) {
                    Thread.sleep(GROWING_FILE_WAIT_STEP_MS)
                    waitedMs += GROWING_FILE_WAIT_STEP_MS
                    continue
                }
                output.write(buffer, 0, read)
                output.flush()
                position += read
                remaining -= read
            }
        }
    }

    private fun parseRange(rangeHeader: String?, totalSizeBytes: Long): ByteRange? {
        if (totalSizeBytes <= 0L) {
            return null
        }
        if (rangeHeader.isNullOrBlank()) {
            return ByteRange(0L, totalSizeBytes - 1L, partial = false)
        }

        val value = rangeHeader.trim()
        if (!value.startsWith("bytes=", ignoreCase = true)) {
            return null
        }
        val rangeValue = value.substringAfter('=').substringBefore(',')
        val startText = rangeValue.substringBefore('-')
        val endText = rangeValue.substringAfter('-', missingDelimiterValue = "")
        val start = if (startText.isBlank()) {
            val suffixLength = endText.toLongOrNull() ?: return null
            (totalSizeBytes - suffixLength).coerceAtLeast(0L)
        } else {
            startText.toLongOrNull() ?: return null
        }
        val end = if (startText.isBlank()) {
            totalSizeBytes - 1L
        } else {
            endText.toLongOrNull()?.coerceAtMost(totalSizeBytes - 1L) ?: (totalSizeBytes - 1L)
        }

        if (start < 0L || start >= totalSizeBytes || end < start) {
            return null
        }
        return ByteRange(start, end, partial = true)
    }

    private fun readHeaders(input: BufferedInputStream): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        while (true) {
            val line = readHttpLine(input) ?: break
            if (line.isEmpty()) {
                break
            }
            val name = line.substringBefore(':', missingDelimiterValue = "").trim().lowercase()
            val value = line.substringAfter(':', missingDelimiterValue = "").trim()
            if (name.isNotEmpty()) {
                headers[name] = value
            }
        }
        return headers
    }

    private fun readHttpLine(input: BufferedInputStream): String? {
        val bytes = ArrayList<Byte>(128)
        while (bytes.size < MAX_HEADER_LINE_BYTES) {
            val next = input.read()
            if (next == -1) {
                return if (bytes.isEmpty()) null else String(bytes.toByteArray(), StandardCharsets.ISO_8859_1)
            }
            if (next == '\n'.code) {
                if (bytes.lastOrNull() == '\r'.code.toByte()) {
                    bytes.removeAt(bytes.lastIndex)
                }
                return String(bytes.toByteArray(), StandardCharsets.ISO_8859_1)
            }
            bytes.add(next.toByte())
        }
        return null
    }

    private fun OutputStream.writeStatus(code: Int, reason: String) {
        writeAscii(
            "HTTP/1.1 $code $reason\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n",
        )
    }

    private fun OutputStream.writeAscii(value: String) {
        write(value.toByteArray(StandardCharsets.ISO_8859_1))
        flush()
    }

    private data class ServedFile(
        val file: File,
        val totalSizeBytes: Long,
        val mimeType: String,
        val onRangeRequested: (Long) -> Unit,
    )

    private data class ByteRange(
        val start: Long,
        val end: Long,
        val partial: Boolean,
    )

    private companion object {
        const val SOCKET_TIMEOUT_MS = 60_000
        const val GROWING_FILE_WAIT_STEP_MS = 100L
        const val GROWING_FILE_WAIT_TIMEOUT_MS = 30_000L
        const val MAX_HEADER_LINE_BYTES = 8 * 1024

        fun mimeTypeFor(name: String): String {
            return when (name.substringAfterLast('.', missingDelimiterValue = "").lowercase()) {
                "m3u8" -> "application/vnd.apple.mpegurl"
                "m4v", "mp4" -> "video/mp4"
                "mkv" -> "video/x-matroska"
                "mov" -> "video/quicktime"
                "ts" -> "video/mp2t"
                "webm" -> "video/webm"
                else -> "application/octet-stream"
            }
        }
    }
}
