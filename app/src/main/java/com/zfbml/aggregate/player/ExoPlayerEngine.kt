package com.zfbml.aggregate.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
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
    private val _state = MutableStateFlow(PlayerEngineState())
    override val state: StateFlow<PlayerEngineState> = _state

    private var exoPlayer: ExoPlayer = buildPlayer(emptyMap())

    val player: Player
        get() = exoPlayer

    override fun prepare(stream: MediaStream, playWhenReady: Boolean) {
        exoPlayer.release()
        exoPlayer = buildPlayer(stream.headers)
        exoPlayer.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    _state.update {
                        it.copy(
                            isReady = playbackState == Player.STATE_READY,
                            isPlaying = exoPlayer.isPlaying,
                        )
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _state.update { it.copy(isPlaying = isPlaying) }
                }

                override fun onPlayerError(error: PlaybackException) {
                    _state.update { it.copy(errorMessage = error.message) }
                }
            },
        )
        val itemBuilder = MediaItem.Builder().setUri(stream.url)
        mimeType(stream.protocol)?.let(itemBuilder::setMimeType)
        exoPlayer.setMediaItem(itemBuilder.build())
        exoPlayer.playWhenReady = playWhenReady
        exoPlayer.prepare()
        _state.value = PlayerEngineState(stream = stream)
    }

    override fun currentPositionMs(): Long = exoPlayer.currentPosition

    override fun release() {
        exoPlayer.release()
    }

    private fun buildPlayer(headers: Map<String, String>): ExoPlayer {
        val httpFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(headers)
        return ExoPlayer.Builder(appContext)
            .setMediaSourceFactory(DefaultMediaSourceFactory(httpFactory))
            .build()
    }

    private fun mimeType(protocol: StreamProtocol): String? = when (protocol) {
        StreamProtocol.HLS -> MimeTypes.APPLICATION_M3U8
        StreamProtocol.DASH -> MimeTypes.APPLICATION_MPD
        else -> null
    }
}
