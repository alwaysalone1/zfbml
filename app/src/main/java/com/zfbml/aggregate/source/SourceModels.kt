package com.zfbml.aggregate.source

import kotlinx.serialization.Serializable

@Serializable
data class SourceManifest(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val capabilities: Set<SourceCapability>,
    val domains: Set<String> = emptySet(),
    val requiresWebView: Boolean = false,
    val supportsDownload: Boolean = false,
)

@Serializable
enum class SourceCapability {
    SEARCH,
    DETAIL,
    EPISODES,
    STREAM,
    WEBVIEW_SNIFF,
    DOWNLOAD,
    BITTORRENT,
}

data class SearchResult(
    val providerId: String,
    val title: String,
    val url: String,
    val posterUrl: String? = null,
    val subtitle: String? = null,
    val raw: Map<String, String> = emptyMap(),
)

data class MediaDetail(
    val providerId: String,
    val title: String,
    val url: String,
    val summary: String? = null,
    val posterUrl: String? = null,
    val episodes: List<Episode> = emptyList(),
)

data class Episode(
    val providerId: String,
    val id: String,
    val title: String,
    val url: String,
    val season: Int? = null,
    val index: Int? = null,
    val raw: Map<String, String> = emptyMap(),
)

data class MediaStream(
    val id: String,
    val providerId: String,
    val url: String,
    val protocol: StreamProtocol,
    val quality: String? = null,
    val codec: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val subtitles: List<SubtitleTrack> = emptyList(),
    val drmInfo: DrmInfo? = null,
    val downloadPolicy: DownloadPolicy = DownloadPolicy.Allowed,
    val sourceScore: Int = 0,
    val metadata: Map<String, String> = emptyMap(),
)

enum class StreamProtocol {
    HLS,
    DASH,
    SMOOTH_STREAMING,
    RTSP,
    PROGRESSIVE,
    BITTORRENT,
    WEBVIEW_ONLY,
    UNKNOWN,
}

data class SubtitleTrack(
    val language: String,
    val label: String,
    val url: String,
    val mimeType: String? = null,
)

data class DrmInfo(
    val scheme: String,
    val licenseUrl: String? = null,
    val requestHeaders: Map<String, String> = emptyMap(),
)

enum class DownloadPolicy {
    Allowed,
    CacheOnly,
    BlockedDrm,
    BlockedWebViewOnly,
}
