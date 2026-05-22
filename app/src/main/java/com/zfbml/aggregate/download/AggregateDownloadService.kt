package com.zfbml.aggregate.download

import android.app.Notification
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import com.zfbml.aggregate.R

@OptIn(UnstableApi::class)
class AggregateDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    CHANNEL_ID,
    R.string.download_channel_name,
    R.string.download_channel_description,
) {
    override fun getDownloadManager(): DownloadManager {
        return MediaDownloadModule.downloadManager(this)
    }

    override fun getScheduler(): Scheduler? = null

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int,
    ): Notification {
        return DownloadNotificationHelper(this, CHANNEL_ID).buildProgressNotification(
            this,
            R.drawable.ic_launcher,
            null,
            "Downloading",
            downloads,
            notMetRequirements,
        )
    }

    companion object {
        private const val CHANNEL_ID = "zfbml_downloads"
        private const val FOREGROUND_NOTIFICATION_ID = 42001
    }
}
