package com.zfbml.aggregate.torrent

import com.zfbml.aggregate.source.MediaStream

data class TorrentCandidate(
    val title: String,
    val magnetUri: String,
    val infoHash: String? = null,
    val sizeBytes: Long? = null,
    val seeders: Int? = null,
    val subgroup: String? = null,
    val quality: String? = null,
    val episodeIndex: Int? = null,
    val sourceName: String? = null,
) {
    fun metadata(): Map<String, String> = buildMap {
        infoHash?.let { put("infoHash", it) }
        sizeBytes?.let { put("sizeBytes", it.toString()) }
        seeders?.let { put("seeders", it.toString()) }
        subgroup?.let { put("subgroup", it) }
        quality?.let { put("quality", it) }
        episodeIndex?.let { put("episodeIndex", it.toString()) }
        sourceName?.let { put("sourceName", it) }
    }
}

data class TorrentPlaybackPlan(
    val stream: MediaStream,
    val selectedFileName: String? = null,
    val selectedFileIndex: Int? = null,
    val selectedFileSizeBytes: Long? = null,
    val selectedFileProgressPercent: Float = 0f,
    val localPlaybackUrl: String? = null,
    val bufferingPercent: Float = 0f,
)

data class TorrentEngineState(
    val stream: MediaStream? = null,
    val plan: TorrentPlaybackPlan? = null,
    val isPreparing: Boolean = false,
    val isReady: Boolean = false,
    val hasMetadata: Boolean = false,
    val status: String? = null,
    val progressPercent: Float = 0f,
    val selectedFileProgressPercent: Float = 0f,
    val downloadRateBytesPerSecond: Int = 0,
    val uploadRateBytesPerSecond: Int = 0,
    val connectedPeers: Int = 0,
    val connectedSeeds: Int = 0,
    val errorMessage: String? = null,
)

data class TorrentFileCandidate(
    val index: Int,
    val path: String,
    val name: String,
    val sizeBytes: Long,
)
