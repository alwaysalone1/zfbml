package com.zfbml.aggregate.source

import com.zfbml.aggregate.torrent.TorrentTitleScorer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull

class MediaRouteResolver(
    private val providers: List<SourceProvider>,
    private val providerTimeoutMs: Long = 8_000L,
) {
    suspend fun resolve(request: MediaFetchRequest): List<RouteCandidate> = coroutineScope {
        val aliases = request.subjectNames.take(MAX_ALIAS_SEARCH_COUNT)
        if (aliases.isEmpty()) return@coroutineScope emptyList()

        val hits = providers
            .filter { provider ->
                SourceCapability.SEARCH in provider.manifest.capabilities &&
                    SourceCapability.STREAM in provider.manifest.capabilities
            }
            .map { provider ->
                async {
                    aliases.flatMap { alias ->
                        withTimeoutOrNull(providerTimeoutMs) {
                            runCatching { provider.search(alias) }.getOrDefault(emptyList())
                        }.orEmpty().map { result ->
                            SearchHit(
                                provider = provider,
                                result = result,
                                alias = alias,
                                score = scoreResult(result, request, alias),
                            )
                        }
                    }
                }
            }
            .awaitAll()
            .flatten()
            .distinctBy { "${it.result.providerId}|${it.result.url}" }
            .filter { hit -> isEpisodeCompatible(hit.result.title, request.episodeIndex) }
            .sortedByDescending { it.score }
            .take(MAX_SEARCH_HITS)

        val onlineHits = hits
            .filter { hit -> hit.result.raw["mediaKind"] == "online" }
            .take(MAX_ONLINE_HITS)
        val onlineRoutes = resolveHits(onlineHits, request)
        val fallbackRoutes = if (onlineRoutes.isEmpty()) {
            resolveHits(
                hits.filterNot { hit -> hit.result.raw["mediaKind"] == "online" }
                    .take(MAX_FALLBACK_HITS),
                request,
            )
        } else {
            emptyList()
        }

        (onlineRoutes + fallbackRoutes)
            .distinctBy { "${it.sourceId}|${it.stream.url}" }
            .sortedWith(routeComparator())
            .take(MAX_ROUTES)
    }

    private suspend fun resolveHits(
        hits: List<SearchHit>,
        request: MediaFetchRequest,
    ): List<RouteCandidate> {
        return hits.flatMap { hit ->
            val detail = runCatching { hit.provider.loadDetail(hit.result) }.getOrNull() ?: return@flatMap emptyList()
            detail.episodes
                .filter { episode ->
                    isEpisodeCompatible(episode.index ?: TorrentTitleScorer.extractEpisode(episode.title), request.episodeIndex)
                }
                .sortedByDescending { episode -> scoreEpisode(episode, request, hit.score) }
                .take(MAX_EPISODES_PER_HIT)
                .flatMap { candidateEpisode ->
                    val episodeScore = scoreEpisode(candidateEpisode, request, hit.score)
                    runCatching { hit.provider.resolveStreams(candidateEpisode) }
                        .getOrDefault(emptyList())
                        .map { stream ->
                            val score = (stream.sourceScore + hit.score / 2 + episodeScore / 4 + protocolScore(stream.protocol))
                                .coerceIn(0, 360)
                            val resolverMetadata = buildMap {
                                put("routeProviderId", hit.provider.manifest.id)
                                put("routeProviderName", hit.provider.manifest.name)
                                put("routeTitle", hit.result.title)
                                put("routeSubtitle", hit.result.subtitle.orEmpty())
                                put("routeUrl", hit.result.url)
                                put("routeAlias", hit.alias)
                                put("routeScore", score.toString())
                                put("routeEpisodeTitle", candidateEpisode.title)
                                candidateEpisode.index?.let { put("routeEpisodeIndex", it.toString()) }
                            }
                            val metadata = resolverMetadata + stream.metadata
                            val normalizedStream = stream.copy(
                                id = "${request.sourceEpisode.id}:${stream.id}",
                                downloadPolicy = if (stream.protocol == StreamProtocol.BITTORRENT) {
                                    DownloadPolicy.CacheOnly
                                } else {
                                    stream.downloadPolicy
                                },
                                sourceScore = score,
                                metadata = metadata,
                            )
                            normalizedStream.toRouteCandidate(
                                sourceId = hit.provider.manifest.id,
                                sourceName = hit.provider.manifest.name,
                                fallbackTitle = hit.result.title,
                            )
                        }
                }
            }
            .distinctBy { "${it.sourceId}|${it.stream.url}" }
            .sortedWith(routeComparator())
            .take(MAX_ROUTES)
    }

    private fun MediaStream.toRouteCandidate(
        sourceId: String,
        sourceName: String,
        fallbackTitle: String,
    ): RouteCandidate {
        return RouteCandidate(
            stream = this,
            sourceId = metadata["routeProviderId"] ?: sourceId,
            sourceName = metadata["routeProviderName"] ?: metadata["sourceName"] ?: sourceName,
            title = metadata["routeTitle"] ?: fallbackTitle,
            routeName = metadata["routeSubtitle"]?.takeIf { it.isNotBlank() } ?: quality,
            episodeTitle = metadata["routeEpisodeTitle"],
            episodeIndex = metadata["routeEpisodeIndex"]?.toIntOrNull() ?: metadata["episodeIndex"]?.toIntOrNull(),
            quality = quality ?: metadata["quality"],
            subgroup = metadata["subgroup"],
            protocol = protocol,
            score = metadata["routeScore"]?.toIntOrNull() ?: sourceScore,
            publishedAt = metadata["publishedAt"],
            sizeBytes = metadata["sizeBytes"]?.toLongOrNull(),
        )
    }

    private fun scoreResult(result: SearchResult, request: MediaFetchRequest, alias: String): Int {
        val title = result.title
        val lower = title.lowercase()
        var score = 60
        if (lower.contains(alias.lowercase())) score += 80
        if (request.subjectNames.any { lower.contains(it.lowercase()) }) score += 50
        if (result.raw["mediaKind"] == "online") score += 420
        result.raw["sourceTier"]?.toIntOrNull()?.let { score -= it * 8 }
        score += episodeScore(TorrentTitleScorer.extractEpisode(title), request.episodeIndex)
        score += qualityScore(lower)
        if ("batch" in lower || "\u5408\u96c6" in lower || Regex("""\b\d{1,3}\s*-\s*\d{1,3}\b""").containsMatchIn(lower)) {
            score -= 60
        }
        result.raw["seeders"]?.toIntOrNull()?.let { score += it.coerceAtMost(300) / 4 }
        result.raw["sizeBytes"]?.toLongOrNull()?.let { score += sizeScore(it) }
        return score
    }

    private fun scoreEpisode(episode: Episode, request: MediaFetchRequest, hitScore: Int): Int {
        val title = episode.title
        val lower = title.lowercase()
        var score = hitScore / 2
        score += episodeScore(episode.index ?: TorrentTitleScorer.extractEpisode(title), request.episodeIndex)
        if (request.subjectNames.any { lower.contains(it.lowercase()) }) score += 35
        score += qualityScore(lower)
        return score
    }

    private fun isEpisodeCompatible(title: String, expected: Int?): Boolean {
        return isEpisodeCompatible(TorrentTitleScorer.extractEpisode(title), expected)
    }

    private fun isEpisodeCompatible(candidate: Int?, expected: Int?): Boolean {
        return expected == null || candidate == null || candidate == expected
    }

    private fun episodeScore(candidate: Int?, expected: Int?): Int {
        if (expected == null) return 0
        return when (candidate) {
            expected -> 140
            null -> 8
            else -> -120
        }
    }

    private fun qualityScore(lower: String): Int {
        var score = 0
        if ("1080" in lower) score += 28
        if ("720" in lower) score += 16
        if ("2160" in lower || "4k" in lower) score += 12
        if ("mp4" in lower || "avc" in lower || "h264" in lower || "h.264" in lower) score += 24
        if ("hevc" in lower || "x265" in lower || "h265" in lower || "10bit" in lower || "av1" in lower) score -= 26
        return score
    }

    private fun protocolScore(protocol: StreamProtocol): Int {
        return when (protocol) {
            StreamProtocol.HLS -> 36
            StreamProtocol.DASH -> 28
            StreamProtocol.PROGRESSIVE -> 8
            StreamProtocol.SMOOTH_STREAMING -> 4
            else -> 0
        }
    }

    private fun routeComparator(): Comparator<RouteCandidate> {
        return compareByDescending<RouteCandidate> { it.score }
            .thenByDescending { protocolScore(it.protocol) }
            .thenBy { it.title }
    }

    private fun sizeScore(bytes: Long): Int {
        return when {
            bytes in 150L * 1024L * 1024L..2_500L * 1024L * 1024L -> 35
            bytes > 8L * 1024L * 1024L * 1024L -> -80
            bytes > 4L * 1024L * 1024L * 1024L -> -45
            else -> 0
        }
    }

    private data class SearchHit(
        val provider: SourceProvider,
        val result: SearchResult,
        val alias: String,
        val score: Int,
    )

    private companion object {
        const val MAX_ALIAS_SEARCH_COUNT = 2
        const val MAX_SEARCH_HITS = 24
        const val MAX_ONLINE_HITS = 5
        const val MAX_FALLBACK_HITS = 10
        const val MAX_EPISODES_PER_HIT = 2
        const val MAX_ROUTES = 24
    }
}
