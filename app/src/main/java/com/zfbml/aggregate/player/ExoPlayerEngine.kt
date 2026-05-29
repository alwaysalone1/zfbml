package com.zfbml.aggregate.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.zfbml.aggregate.source.MediaStream
import com.zfbml.aggregate.source.StreamProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@OptIn(UnstableApi::class)
class ExoPlayerEngine(
    context: Context,
) : PlayerEngine {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val httpFactory = DefaultHttpDataSource.Factory()
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(HTTP_CONNECT_TIMEOUT_MS)
        .setReadTimeoutMs(HTTP_READ_TIMEOUT_MS)
    private val _state = MutableStateFlow(PlayerEngineState())
    override val state: StateFlow<PlayerEngineState> = _state

    private var exoPlayer: ExoPlayer = buildPlayer()
    private var progressLogger: Runnable? = null
    private var listenerPlayer: ExoPlayer? = null

    val player: Player
        get() = exoPlayer

    override fun prepare(stream: MediaStream, playWhenReady: Boolean) {
        stopProgressLogging()
        httpFactory.setDefaultRequestProperties(stream.headers)
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        ensureListener()
        val itemBuilder = MediaItem.Builder().setUri(stream.url)
        mimeType(stream.protocol)?.let(itemBuilder::setMimeType)
        exoPlayer.setMediaItem(itemBuilder.build())
        exoPlayer.playWhenReady = playWhenReady
        exoPlayer.prepare()
        _state.value = PlayerEngineState(
            stream = stream,
            playbackStateLabel = exoPlayer.playbackState.label(),
        )
        Log.i(TAG, "prepare protocol=${stream.protocol} quality=${stream.quality} url=${stream.url.take(160)}")
        startProgressLogging()
    }

    private fun ensureListener() {
        if (listenerPlayer === exoPlayer) {
            return
        }
        exoPlayer.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    _state.update {
                        it.copy(
                            isReady = playbackState == Player.STATE_READY,
                            isPlaying = exoPlayer.isPlaying,
                            playbackStateLabel = playbackState.label(),
                        )
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _state.update { it.copy(isPlaying = isPlaying) }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e(TAG, "Playback error: ${error.errorCodeName} ${error.message}", error)
                    _state.update { it.copy(errorMessage = error.message) }
                }

                override fun onRenderedFirstFrame() {
                    Log.i(TAG, "rendered first video frame")
                    _state.update { it.copy(hasRenderedFirstFrame = true) }
                }

                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    val label = if (videoSize.width > 0 && videoSize.height > 0) {
                        "${videoSize.width}x${videoSize.height}"
                    } else {
                        null
                    }
                    Log.i(TAG, "videoSize=$label")
                    _state.update { it.copy(videoSizeLabel = label) }
                }

                override fun onTracksChanged(tracks: Tracks) {
                    val groups = tracks.groups.joinToString { group ->
                        "${group.type}:${group.length}:${group.isSelected}"
                    }
                    Log.i(TAG, "tracks=$groups")
                }
            },
        )
        listenerPlayer = exoPlayer
    }

    override fun currentPositionMs(): Long = exoPlayer.currentPosition

    override fun release() {
        stopProgressLogging()
        exoPlayer.release()
    }

    private fun startProgressLogging() {
        val logger = object : Runnable {
            override fun run() {
                Log.i(
                    TAG,
                    "progress state=${exoPlayer.playbackState.label()} isPlaying=${exoPlayer.isPlaying} " +
                        "positionMs=${exoPlayer.currentPosition} bufferedMs=${exoPlayer.bufferedPosition} " +
                        "durationMs=${exoPlayer.duration} playWhenReady=${exoPlayer.playWhenReady} " +
                        "firstFrame=${_state.value.hasRenderedFirstFrame} videoSize=${_state.value.videoSizeLabel.orEmpty()}",
                )
                mainHandler.postDelayed(this, PROGRESS_LOG_INTERVAL_MS)
            }
        }
        progressLogger = logger
        mainHandler.postDelayed(logger, PROGRESS_LOG_INTERVAL_MS)
    }

    private fun stopProgressLogging() {
        progressLogger?.let(mainHandler::removeCallbacks)
        progressLogger = null
    }

    private fun buildPlayer(): ExoPlayer {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                MIN_BUFFER_MS,
                MAX_BUFFER_MS,
                BUFFER_FOR_PLAYBACK_MS,
                BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS,
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
        return ExoPlayer.Builder(appContext)
            .setMediaSourceFactory(DefaultMediaSourceFactory(httpFactory))
            .setLoadControl(loadControl)
            .build()
    }

    private fun mimeType(protocol: StreamProtocol): String? = when (protocol) {
        StreamProtocol.HLS -> MimeTypes.APPLICATION_M3U8
        StreamProtocol.DASH -> MimeTypes.APPLICATION_MPD
        else -> null
    }

    private fun Int.label(): String = when (this) {
        Player.STATE_IDLE -> "IDLE"
        Player.STATE_BUFFERING -> "BUFFERING"
        Player.STATE_READY -> "READY"
        Player.STATE_ENDED -> "ENDED"
        else -> "UNKNOWN($this)"
    }

    private companion object {
        const val TAG = "ZfbmlPlayer"
        const val PROGRESS_LOG_INTERVAL_MS = 5_000L
        const val HTTP_CONNECT_TIMEOUT_MS = 15_000
        const val HTTP_READ_TIMEOUT_MS = 180_000
        const val MIN_BUFFER_MS = 3_000
        const val MAX_BUFFER_MS = 60_000
        const val BUFFER_FOR_PLAYBACK_MS = 1_000
        const val BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 2_500
    }
}
