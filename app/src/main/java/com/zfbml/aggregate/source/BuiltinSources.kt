package com.zfbml.aggregate.source

import kotlinx.coroutines.delay

class DemoBuiltinSourceProvider : SourceProvider {
    override val manifest = SourceManifest(
        id = "builtin-demo",
        name = "Built-in Demo",
        version = "1.0.0",
        author = "zfbml",
        capabilities = setOf(
            SourceCapability.SEARCH,
            SourceCapability.DETAIL,
            SourceCapability.EPISODES,
            SourceCapability.STREAM,
            SourceCapability.DOWNLOAD,
        ),
        domains = setOf("demo.local"),
        supportsDownload = true,
    )

    override suspend fun search(query: String): List<SearchResult> {
        delay(80)
        val normalized = query.trim().ifBlank { "demo" }
        return listOf(
            SearchResult(
                providerId = manifest.id,
                title = "Demo Movie: $normalized",
                url = "demo://movie/$normalized",
                subtitle = "Media3 progressive sample stream",
            ),
            SearchResult(
                providerId = manifest.id,
                title = "Demo Series: $normalized",
                url = "demo://series/$normalized",
                subtitle = "Multiple qualities and danmaku sample",
            ),
        )
    }

    override suspend fun loadDetail(result: SearchResult): MediaDetail {
        val episodes = listOf(
            Episode(
                providerId = manifest.id,
                id = "ep-1",
                title = "Episode 1",
                url = "${result.url}/ep1",
                index = 1,
            ),
            Episode(
                providerId = manifest.id,
                id = "ep-2",
                title = "Episode 2",
                url = "${result.url}/ep2",
                index = 2,
            ),
        )
        return MediaDetail(
            providerId = manifest.id,
            title = result.title,
            url = result.url,
            summary = "Local demo provider used to validate source, player, danmaku, and download flows.",
            episodes = episodes,
        )
    }

    override suspend fun resolveStreams(episode: Episode): List<MediaStream> {
        val headers = mapOf("User-Agent" to "ZfbmlAggregate/0.1")
        return listOf(
            MediaStream(
                id = "${episode.id}-hls",
                providerId = manifest.id,
                url = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                protocol = StreamProtocol.PROGRESSIVE,
                quality = "1080p",
                codec = "h264/aac",
                headers = headers,
                sourceScore = 90,
            ),
            MediaStream(
                id = "${episode.id}-backup",
                providerId = manifest.id,
                url = "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                protocol = StreamProtocol.PROGRESSIVE,
                quality = "720p",
                codec = "h264/aac",
                headers = headers,
                sourceScore = 70,
            ),
        )
    }
}

class DirectUrlSourceProvider : SourceProvider {
    override val manifest = SourceManifest(
        id = "direct-url",
        name = "Direct URL",
        version = "1.0.0",
        author = "zfbml",
        capabilities = setOf(SourceCapability.SEARCH, SourceCapability.DETAIL, SourceCapability.STREAM),
        supportsDownload = true,
    )

    override suspend fun search(query: String): List<SearchResult> {
        val url = query.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) return emptyList()
        return listOf(
            SearchResult(
                providerId = manifest.id,
                title = url.substringAfterLast('/').ifBlank { "Direct Stream" },
                url = url,
                subtitle = "User supplied stream URL",
            ),
        )
    }

    override suspend fun loadDetail(result: SearchResult): MediaDetail {
        return MediaDetail(
            providerId = manifest.id,
            title = result.title,
            url = result.url,
            summary = "Direct user supplied stream.",
            episodes = listOf(
                Episode(
                    providerId = manifest.id,
                    id = result.url.hashCode().toString(),
                    title = result.title,
                    url = result.url,
                    index = 1,
                ),
            ),
        )
    }

    override suspend fun resolveStreams(episode: Episode): List<MediaStream> {
        return listOf(
            MediaStream(
                id = episode.id,
                providerId = manifest.id,
                url = episode.url,
                protocol = inferProtocol(episode.url),
                quality = "auto",
                sourceScore = 50,
            ),
        )
    }

    private fun inferProtocol(url: String): StreamProtocol = when {
        url.contains(".m3u8", ignoreCase = true) -> StreamProtocol.HLS
        url.contains(".mpd", ignoreCase = true) -> StreamProtocol.DASH
        url.startsWith("rtsp://", ignoreCase = true) -> StreamProtocol.RTSP
        else -> StreamProtocol.PROGRESSIVE
    }
}
