package com.zfbml.aggregate.source

import kotlinx.coroutines.delay

class DemoBuiltinSourceProvider : SourceProvider {
    override val manifest = SourceManifest(
        id = "builtin-demo",
        name = "Online Demo",
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
                title = "\u661F\u6D77\u8FFD\u756A\u5385",
                url = "demo://movie/sintel?query=$normalized",
                subtitle = "\u9AD8\u6E05\u5728\u7EBF\u6F14\u793A\u3001\u81EA\u9002\u5E94\u6E05\u6670\u5EA6",
            ),
            SearchResult(
                providerId = manifest.id,
                title = "\u4ECA\u65E5\u65B0\u756A\u653E\u9001",
                url = "demo://series/mux-hls?query=$normalized",
                subtitle = "\u6D41\u7545\u64AD\u653E\u3001\u591A\u6E05\u6670\u5EA6\u81EA\u52A8\u5207\u6362",
            ),
        )
    }

    override suspend fun loadDetail(result: SearchResult): MediaDetail {
        val episodes = listOf(
            Episode(
                providerId = manifest.id,
                id = "ep-1",
                title = "\u7B2C 1 \u96C6",
                url = "${result.url}/ep1",
                index = 1,
            ),
            Episode(
                providerId = manifest.id,
                id = "ep-2",
                title = "\u7B2C 2 \u96C6",
                url = "${result.url}/ep2",
                index = 2,
            ),
        )
        return MediaDetail(
            providerId = manifest.id,
            title = result.title,
            url = result.url,
            summary = "\u5DF2\u4E3A\u4F60\u51C6\u5907\u9AD8\u6E05\u6F14\u793A\u7247\u6E90\uFF0C\u53EF\u76F4\u63A5\u8FDB\u5165\u64AD\u653E\u9875\u68C0\u67E5\u753B\u9762\u3001\u5F39\u5E55\u548C\u79BB\u7EBF\u4F53\u9A8C\u3002",
            episodes = episodes,
        )
    }

    override suspend fun resolveStreams(episode: Episode): List<MediaStream> {
        val headers = emptyMap<String, String>()
        return listOf(
            MediaStream(
                id = "${episode.id}-hls",
                providerId = manifest.id,
                url = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                protocol = StreamProtocol.HLS,
                quality = "auto",
                codec = "h264/aac",
                headers = headers,
                sourceScore = 100,
            ),
            MediaStream(
                id = "${episode.id}-mp4",
                providerId = manifest.id,
                url = "https://media.w3.org/2010/05/sintel/trailer.mp4",
                protocol = StreamProtocol.PROGRESSIVE,
                quality = "1080p",
                codec = "h264/aac",
                headers = headers,
                sourceScore = 90,
            ),
            MediaStream(
                id = "${episode.id}-backup",
                providerId = manifest.id,
                url = "https://filesamples.com/samples/video/mp4/sample_640x360.mp4",
                protocol = StreamProtocol.PROGRESSIVE,
                quality = "360p",
                codec = "h264/aac",
                headers = headers,
                sourceScore = 60,
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
