package com.zfbml.aggregate.player

import com.zfbml.aggregate.source.MediaStream
import kotlinx.coroutines.flow.StateFlow

interface PlayerEngine {
    val state: StateFlow<PlayerEngineState>

    fun prepare(stream: MediaStream, playWhenReady: Boolean = true)

    fun currentPositionMs(): Long

    fun release()
}

data class PlayerEngineState(
    val stream: MediaStream? = null,
    val isReady: Boolean = false,
    val isPlaying: Boolean = false,
    val playbackStateLabel: String = "IDLE",
    val hasRenderedFirstFrame: Boolean = false,
    val videoSizeLabel: String? = null,
    val errorMessage: String? = null,
)
