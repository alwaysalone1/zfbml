package com.zfbml.aggregate.torrent

import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalTorrentFileServerTest {
    @Test
    fun servesRequestedByteRange() {
        val file = File.createTempFile("torrent-server", ".mp4")
        val bytes = ByteArray(1024) { index -> (index % 251).toByte() }
        file.writeBytes(bytes)

        LocalTorrentFileServer().use { server ->
            val url = server.serve(file, bytes.size.toLong())
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.setRequestProperty("Range", "bytes=100-199")

            assertEquals(206, connection.responseCode)
            assertEquals("bytes 100-199/1024", connection.getHeaderField("Content-Range"))
            assertEquals(100, connection.inputStream.readBytes().size)
        }
    }

    @Test
    fun reusesStableUrlForSameFile() {
        val file = File.createTempFile("torrent-server-stable", ".mkv")
        file.writeBytes(ByteArray(256) { 1 })

        LocalTorrentFileServer().use { server ->
            val first = server.serve(file, 256)
            val second = server.serve(file, 256)

            assertEquals(first, second)
            assertTrue(first.startsWith("http://127.0.0.1:"))
        }
    }
}
