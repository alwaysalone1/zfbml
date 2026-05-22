package com.zfbml.aggregate.torrent

import com.zfbml.aggregate.source.MediaStream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotBundledTorrentEngine : TorrentEngine {
    private val _state = MutableStateFlow(TorrentEngineState())
    override val state: StateFlow<TorrentEngineState> = _state

    override suspend fun prepare(stream: MediaStream): TorrentPlaybackPlan {
        val plan = TorrentPlaybackPlan(stream = stream)
        _state.value = TorrentEngineState(
            stream = stream,
            plan = plan,
            isPreparing = false,
            isReady = false,
            errorMessage = "BitTorrent runtime is not bundled yet. Add a libtorrent/jlibtorrent implementation behind TorrentEngine.",
        )
        return plan
    }

    override fun release() {
        _state.value = TorrentEngineState()
    }
}
