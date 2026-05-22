package com.zfbml.aggregate.download

import com.zfbml.aggregate.source.MediaStream

data class DownloadTask(
    val id: String,
    val streamId: String,
    val title: String,
    val engine: DownloadEngine,
    val status: DownloadStatus,
    val progressPercent: Float = 0f,
    val outputPath: String? = null,
    val errorMessage: String? = null,
    val retryCount: Int = 0,
)

enum class DownloadEngine {
    Media3Cache,
    YtDlp,
    Aria2,
}

enum class DownloadStatus {
    Queued,
    Running,
    Paused,
    Completed,
    Failed,
    Blocked,
}

interface AdvancedDownloadProvider {
    val engine: DownloadEngine

    fun isAvailable(): Boolean

    suspend fun createTask(stream: MediaStream, title: String): DownloadTask
}

class YtDlpAdvancedDownloadProvider : AdvancedDownloadProvider {
    override val engine: DownloadEngine = DownloadEngine.YtDlp

    override fun isAvailable(): Boolean = false

    override suspend fun createTask(stream: MediaStream, title: String): DownloadTask {
        return DownloadTask(
            id = "ytdlp-${stream.id}",
            streamId = stream.id,
            title = title,
            engine = engine,
            status = DownloadStatus.Blocked,
            errorMessage = "yt-dlp runtime is not bundled yet.",
        )
    }
}
