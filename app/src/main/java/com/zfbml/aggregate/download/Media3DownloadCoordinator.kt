package com.zfbml.aggregate.download

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.zfbml.aggregate.source.DownloadPolicy
import com.zfbml.aggregate.source.MediaStream
import com.zfbml.aggregate.source.StreamProtocol

@OptIn(UnstableApi::class)
class Media3DownloadCoordinator(
    private val context: Context,
) {
    fun enqueue(stream: MediaStream, title: String): DownloadTask {
        if (stream.downloadPolicy != DownloadPolicy.Allowed && stream.downloadPolicy != DownloadPolicy.CacheOnly) {
            return DownloadTask(
                id = "media3-${stream.id}",
                streamId = stream.id,
                title = title,
                engine = DownloadEngine.Media3Cache,
                status = DownloadStatus.Blocked,
                errorMessage = "Stream download policy is ${stream.downloadPolicy}.",
            )
        }
        val requestBuilder = DownloadRequest.Builder(stream.id, Uri.parse(stream.url))
            .setCustomCacheKey(stream.id)
        mimeType(stream.protocol)?.let(requestBuilder::setMimeType)
        DownloadService.sendAddDownload(
            context,
            AggregateDownloadService::class.java,
            requestBuilder.build(),
            false,
        )
        return DownloadTask(
            id = "media3-${stream.id}",
            streamId = stream.id,
            title = title,
            engine = DownloadEngine.Media3Cache,
            status = DownloadStatus.Queued,
        )
    }

    private fun mimeType(protocol: StreamProtocol): String? = when (protocol) {
        StreamProtocol.HLS -> MimeTypes.APPLICATION_M3U8
        StreamProtocol.DASH -> MimeTypes.APPLICATION_MPD
        else -> null
    }
}
