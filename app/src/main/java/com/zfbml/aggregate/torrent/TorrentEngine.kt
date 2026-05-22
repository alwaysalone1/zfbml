package com.zfbml.aggregate.torrent

import com.zfbml.aggregate.source.MediaStream
import kotlinx.coroutines.flow.StateFlow

interface TorrentEngine {
    val state: StateFlow<TorrentEngineState>

    suspend fun prepare(stream: MediaStream): TorrentPlaybackPlan

    fun release()
}
