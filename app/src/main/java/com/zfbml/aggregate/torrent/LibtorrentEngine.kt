package com.zfbml.aggregate.torrent

import android.content.Context
import android.util.Log
import com.zfbml.aggregate.source.MediaStream
import com.zfbml.aggregate.source.StreamProtocol
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.libtorrent4j.AlertListener
import org.libtorrent4j.AnnounceEntry
import org.libtorrent4j.Priority
import org.libtorrent4j.SessionManager
import org.libtorrent4j.TorrentHandle
import org.libtorrent4j.TorrentInfo
import org.libtorrent4j.alerts.AddTorrentAlert
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.AlertType
import org.libtorrent4j.alerts.MetadataReceivedAlert
import org.libtorrent4j.alerts.TorrentAlert
import org.libtorrent4j.alerts.TorrentCheckedAlert
import org.libtorrent4j.alerts.TorrentErrorAlert
import org.libtorrent4j.alerts.TorrentFinishedAlert
import org.libtorrent4j.swig.torrent_flags_t

class LibtorrentEngine(
    context: Context,
) : TorrentEngine {
    private val appContext = context.applicationContext
    private val sessionManager = SessionManager(false)
    private val httpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val rootDir: File = appContext.getExternalFilesDir("torrents")
        ?: File(appContext.filesDir, "torrents")
    private val fileServer = LocalTorrentFileServer()

    private val _state = MutableStateFlow(TorrentEngineState())
    override val state: StateFlow<TorrentEngineState> = _state

    @Volatile
    private var currentStream: MediaStream? = null

    @Volatile
    private var currentHandle: TorrentHandle? = null

    @Volatile
    private var currentSaveDir: File? = null

    @Volatile
    private var prioritizedFileIndex: Int? = null

    @Volatile
    private var lastPlaybackPieceIndex: Int? = null

    @Volatile
    private var lastStatusLogAtMs: Long = 0L

    private var monitorJob: Job? = null
    private var listenerInstalled = false

    private val listener = object : AlertListener {
        override fun types(): IntArray = intArrayOf(
            AlertType.ADD_TORRENT.swig(),
            AlertType.METADATA_RECEIVED.swig(),
            AlertType.TORRENT_CHECKED.swig(),
            AlertType.TORRENT_ERROR.swig(),
            AlertType.TORRENT_FINISHED.swig(),
        )

        override fun alert(alert: Alert<*>) {
            when (alert) {
                is AddTorrentAlert -> handleAddTorrent(alert)
                is MetadataReceivedAlert,
                is TorrentCheckedAlert,
                is TorrentFinishedAlert -> refreshFromAlert(alert)
                is TorrentErrorAlert -> updateError(alert.error().getMessage().ifBlank { alert.message() })
            }
        }
    }

    override suspend fun prepare(stream: MediaStream): TorrentPlaybackPlan = withContext(Dispatchers.IO) {
        val initialPlan = TorrentPlaybackPlan(stream = stream)
        if (stream.protocol != StreamProtocol.BITTORRENT) {
            val message = "TorrentEngine received a non-BT stream: ${stream.protocol}"
            _state.value = TorrentEngineState(stream = stream, plan = initialPlan, errorMessage = message)
            return@withContext initialPlan
        }

        monitorJob?.cancel()
        prioritizedFileIndex = null
        currentStream = stream
        currentHandle = null
        currentSaveDir = streamSaveDir(stream).also { it.mkdirs() }
        _state.value = TorrentEngineState(
            stream = stream,
            plan = initialPlan,
            isPreparing = true,
            status = "starting",
        )
        Log.i(TAG, "prepare stream provider=${stream.providerId} url=${stream.url.take(160)}")

        runCatching {
            ensureSession()
            addTorrent(stream, currentSaveDir ?: rootDir)
            startMonitor()
        }.onFailure { throwable ->
            updateError(throwable.message ?: throwable::class.java.simpleName)
        }

        initialPlan
    }

    override fun release() {
        monitorJob?.cancel()
        monitorJob = null
        currentHandle?.let { handle ->
            runCatching {
                if (sessionManager.isRunning && handle.isValid) {
                    sessionManager.remove(handle)
                }
            }
        }
        currentStream = null
        currentHandle = null
        currentSaveDir = null
        prioritizedFileIndex = null
        lastPlaybackPieceIndex = null
        fileServer.close()
        _state.value = TorrentEngineState()
    }

    @Synchronized
    private fun ensureSession() {
        if (!listenerInstalled) {
            sessionManager.addListener(listener)
            listenerInstalled = true
        }
        if (!sessionManager.isRunning) {
            sessionManager.start()
        }
        if (sessionManager.isPaused) {
            sessionManager.resume()
        }
        if (!sessionManager.isDhtRunning) {
            sessionManager.startDht()
        }
    }

    private fun addTorrent(stream: MediaStream, saveDir: File) {
        val url = stream.url.trim()
        if (url.startsWith("magnet:", ignoreCase = true)) {
            Log.i(TAG, "add magnet torrent")
            sessionManager.download(url, saveDir, torrent_flags_t())
            return
        }

        if (url.startsWith("http", ignoreCase = true) && url.endsWith(".torrent", ignoreCase = true)) {
            val torrentFile = File(saveDir, "source.torrent")
            downloadTorrentFile(url, torrentFile)
            Log.i(TAG, "add torrent file bytes=${torrentFile.length()} url=${url.take(160)}")
            sessionManager.download(TorrentInfo(torrentFile), saveDir)
            return
        }

        throw IllegalArgumentException("Only magnet links and HTTP .torrent URLs are supported by the bundled BT engine.")
    }

    private fun downloadTorrentFile(url: String, outputFile: File) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/x-bittorrent, application/octet-stream, */*")
            .build()
        httpClient.newCall(request).execute().use { response ->
            require(response.isSuccessful) { "HTTP ${response.code} while downloading torrent file" }
            outputFile.outputStream().use { output ->
                response.body.byteStream().use { input -> input.copyTo(output) }
            }
        }
        require(outputFile.length() > 0L) { "Downloaded torrent file is empty" }
    }

    private fun handleAddTorrent(alert: AddTorrentAlert) {
        val error = alert.error()
        if (error.isError) {
            updateError(error.getMessage().ifBlank { alert.message() })
            return
        }
        currentHandle = alert.handle()
        seedTrackers(alert.handle())
        Log.i(TAG, "torrent added")
        refreshFromHandle(alert.handle())
    }

    private fun seedTrackers(handle: TorrentHandle) {
        DEFAULT_TRACKERS.forEach { tracker ->
            runCatching { handle.addTracker(AnnounceEntry(tracker)) }
        }
        runCatching { handle.forceDHTAnnounce() }
        runCatching { handle.forceReannounce() }
    }

    private fun refreshFromAlert(alert: Alert<*>) {
        val handle = (alert as? TorrentAlert<*>)?.handle() ?: currentHandle ?: return
        currentHandle = handle
        refreshFromHandle(handle)
    }

    private fun refreshFromHandle(handle: TorrentHandle) {
        scope.launch {
            updateFromHandle(handle)
        }
    }

    private fun startMonitor() {
        monitorJob = scope.launch {
            while (isActive) {
                currentHandle?.let { handle ->
                    updateFromHandle(handle)
                }
                delay(1_000)
            }
        }
    }

    private fun updateFromHandle(handle: TorrentHandle) {
        val stream = currentStream ?: return
        val saveDir = currentSaveDir ?: return
        if (!handle.isValid) {
            return
        }

        val status = runCatching { handle.status(true) }.getOrNull() ?: return
        val torrentInfo = runCatching { handle.torrentFile() }.getOrNull()
        val plan = if (torrentInfo != null && torrentInfo.isValid) {
            buildPlan(stream, handle, torrentInfo, saveDir)
        } else {
            _state.value.plan ?: TorrentPlaybackPlan(stream)
        }
        val error = status.errorCode().takeIf { it.isError }?.getMessage()
        logStatus(status.state().name.lowercase(), status.hasMetadata(), plan)

        _state.value = TorrentEngineState(
            stream = stream,
            plan = plan,
            isPreparing = !status.hasMetadata(),
            isReady = plan.localPlaybackUrl != null,
            hasMetadata = status.hasMetadata(),
            status = status.state().name.lowercase(),
            progressPercent = (status.progress().coerceIn(0f, 1f) * 100f),
            selectedFileProgressPercent = plan.selectedFileProgressPercent,
            downloadRateBytesPerSecond = status.downloadRate(),
            uploadRateBytesPerSecond = status.uploadRate(),
            connectedPeers = status.numPeers(),
            connectedSeeds = status.numSeeds(),
            errorMessage = error?.takeIf { it.isNotBlank() },
        )
    }

    private fun buildPlan(
        stream: MediaStream,
        handle: TorrentHandle,
        torrentInfo: TorrentInfo,
        saveDir: File,
    ): TorrentPlaybackPlan {
        val storage = torrentInfo.files()
        val files = (0 until storage.numFiles()).map { index ->
            TorrentFileCandidate(
                index = index,
                path = storage.filePath(index),
                name = storage.fileName(index),
                sizeBytes = storage.fileSize(index),
            )
        }
        val selected = TorrentFileSelector.choose(files)
            ?: return TorrentPlaybackPlan(stream = stream)

        if (prioritizedFileIndex != selected.index) {
            prioritizeSelectedFile(handle, storage.numFiles(), selected.index)
            prioritizeSequentialPieces(handle, storage.pieceIndexAtFile(selected.index), storage.lastPieceIndexAtFile(selected.index))
            prioritizedFileIndex = selected.index
            lastPlaybackPieceIndex = null
        }

        val firstPiece = storage.pieceIndexAtFile(selected.index)
        val lastPiece = storage.lastPieceIndexAtFile(selected.index)
        val pieceLength = storage.pieceLength().coerceAtLeast(1)
        val selectedProgress = runCatching {
            handle.fileProgress(TorrentHandle.PIECE_GRANULARITY).getOrNull(selected.index) ?: 0L
        }.getOrDefault(0L)
        val readyBytes = minOf(PLAYBACK_READY_BYTES, maxOf(MIN_PLAYBACK_READY_BYTES, selected.sizeBytes / 200L))
        val bufferingPercent = if (readyBytes <= 0L) {
            0f
        } else {
            ((selectedProgress.toFloat() / readyBytes.toFloat()).coerceIn(0f, 1f) * 100f)
        }
        val selectedFileProgressPercent = if (selected.sizeBytes <= 0L) {
            0f
        } else {
            ((selectedProgress.toFloat() / selected.sizeBytes.toFloat()).coerceIn(0f, 1f) * 100f)
        }
        val localFile = File(storage.filePath(selected.index, saveDir.absolutePath))
        val localUrl = fileServer.serve(localFile, selected.sizeBytes) { offset ->
            prioritizePlaybackOffset(handle, firstPiece, lastPiece, pieceLength, offset)
        }

        return TorrentPlaybackPlan(
            stream = stream,
            selectedFileName = selected.path,
            selectedFileIndex = selected.index,
            selectedFileSizeBytes = selected.sizeBytes,
            selectedFileProgressPercent = selectedFileProgressPercent,
            localPlaybackUrl = localUrl,
            bufferingPercent = bufferingPercent,
        )
    }

    private fun prioritizeSelectedFile(handle: TorrentHandle, fileCount: Int, selectedIndex: Int) {
        val priorities = Array(fileCount) { index ->
            if (index == selectedIndex) Priority.TOP_PRIORITY else Priority.IGNORE
        }
        runCatching { handle.prioritizeFiles(priorities) }
    }

    private fun prioritizeSequentialPieces(handle: TorrentHandle, firstPiece: Int, lastPiece: Int) {
        if (firstPiece < 0 || lastPiece < firstPiece) {
            return
        }
        runCatching { handle.setSequentialRange(firstPiece, lastPiece) }
        val warmupLastPiece = minOf(lastPiece, firstPiece + WARMUP_PIECE_COUNT)
        for (piece in firstPiece..warmupLastPiece) {
            runCatching { handle.setPieceDeadline(piece, (piece - firstPiece) * 250) }
        }
    }

    private fun prioritizePlaybackOffset(
        handle: TorrentHandle,
        firstPiece: Int,
        lastPiece: Int,
        pieceLength: Int,
        offset: Long,
    ) {
        if (firstPiece < 0 || lastPiece < firstPiece) {
            return
        }
        val piece = (firstPiece + (offset / pieceLength).toInt()).coerceIn(firstPiece, lastPiece)
        if (lastPlaybackPieceIndex == piece) {
            return
        }
        lastPlaybackPieceIndex = piece
        runCatching { handle.setSequentialRange(piece, lastPiece) }
        val warmupLastPiece = minOf(lastPiece, piece + WARMUP_PIECE_COUNT)
        for (currentPiece in piece..warmupLastPiece) {
            runCatching { handle.setPieceDeadline(currentPiece, (currentPiece - piece) * 150) }
        }
    }

    private fun streamSaveDir(stream: MediaStream): File {
        val id = stream.metadata["infoHash"] ?: stream.id.ifBlank { stream.url.hashCode().toString() }
        return File(rootDir, id.safePathSegment())
    }

    private fun updateError(message: String) {
        Log.e(TAG, message)
        val previous = _state.value
        _state.value = previous.copy(
            isPreparing = false,
            isReady = false,
            errorMessage = message,
        )
    }

    private fun String.safePathSegment(): String {
        return replace(Regex("[^A-Za-z0-9._-]"), "_").take(96).ifBlank { "torrent" }
    }

    private fun logStatus(status: String, hasMetadata: Boolean, plan: TorrentPlaybackPlan) {
        val now = System.currentTimeMillis()
        if (now - lastStatusLogAtMs < 5_000L) {
            return
        }
        lastStatusLogAtMs = now
        Log.i(
            TAG,
            "status=$status metadata=$hasMetadata file=${plan.selectedFileName.orEmpty()} " +
                "fileProgress=${"%.2f".format(plan.selectedFileProgressPercent)} " +
                "buffer=${"%.2f".format(plan.bufferingPercent)} urlReady=${plan.localPlaybackUrl != null}",
        )
    }

    private companion object {
        const val TAG = "ZfbmlTorrent"
        const val MIN_PLAYBACK_READY_BYTES = 2L * 1024L * 1024L
        const val PLAYBACK_READY_BYTES = 4L * 1024L * 1024L
        const val WARMUP_PIECE_COUNT = 48
        const val USER_AGENT =
            "Mozilla/5.0 (Android; ZFBML) AppleWebKit/537.36 (KHTML, like Gecko) ZfbmlAggregate/0.2"
        val DEFAULT_TRACKERS = listOf(
            "udp://tracker.opentrackr.org:1337/announce",
            "udp://open.stealth.si:80/announce",
            "udp://tracker.torrent.eu.org:451/announce",
            "udp://tracker.openbittorrent.com:6969/announce",
            "https://tr.bangumi.moe:9696/announce",
            "http://tr.bangumi.moe:6969/announce",
            "http://t.acg.rip:6699/announce",
        )
    }
}
