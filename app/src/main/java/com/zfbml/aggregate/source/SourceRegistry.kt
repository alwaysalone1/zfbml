package com.zfbml.aggregate.source

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout

class SourceRegistry(
    providers: List<SourceProvider>,
) {
    private val providersById = providers.associateBy { it.manifest.id }

    val manifests: List<SourceManifest> = providers.map { it.manifest }

    fun provider(id: String): SourceProvider? = providersById[id]

    suspend fun searchAll(query: String): List<SearchResult> = searchAllWithReport(query).results

    suspend fun searchAllWithReport(query: String): SourceSearchReport = coroutineScope {
        val outcomes = providersById.values
            .filter { SourceCapability.SEARCH in it.manifest.capabilities }
            .map { provider ->
                async {
                    runCatching { withTimeout(SEARCH_PROVIDER_TIMEOUT_MS) { provider.search(query) } }
                        .fold(
                            onSuccess = { results ->
                                SourceSearchOutcome(
                                    results = results,
                                    failure = null,
                                )
                            },
                            onFailure = { error ->
                                SourceSearchOutcome(
                                    results = emptyList(),
                                    failure = SourceSearchFailure(
                                        providerId = provider.manifest.id,
                                        providerName = provider.manifest.name,
                                        message = error.message ?: error::class.simpleName.orEmpty().ifBlank { "Unknown error" },
                                    ),
                                )
                            },
                        )
                }
            }
            .awaitAll()

        SourceSearchReport(
            results = outcomes
                .flatMap { it.results }
                .sortedWith(compareByDescending<SearchResult> { it.playabilityScore() }.thenBy { it.title }),
            failures = outcomes.mapNotNull { it.failure },
        )
    }

    suspend fun loadDetail(result: SearchResult): MediaDetail {
        val provider = requireNotNull(provider(result.providerId)) {
            "No provider registered for ${result.providerId}"
        }
        return provider.loadDetail(result)
    }

    suspend fun resolveStreams(episode: Episode): List<MediaStream> {
        val provider = requireNotNull(provider(episode.providerId)) {
            "No provider registered for ${episode.providerId}"
        }
        return provider.resolveStreams(episode)
            .sortedWith(compareByDescending<MediaStream> { it.sourceScore }.thenBy { it.quality.orEmpty() })
    }

    suspend fun resolveRouteCandidates(episode: Episode): List<RouteCandidate> {
        return resolveStreams(episode)
            .map { stream -> stream.toRouteCandidate(episode) }
            .sortedWith(compareByDescending<RouteCandidate> { it.score }.thenBy { it.title })
    }

    private fun SearchResult.playabilityScore(): Int {
        val lower = title.lowercase()
        var score = 0
        if (providerId == "bangumi-catalog") score += 260
        if (providerId == "direct-url") score += 200
        if (providerId == "bt") score += 200
        if (!posterUrl.isNullOrBlank()) score += 20
        if (raw["torrentUrl"]?.startsWith("http", ignoreCase = true) == true) score += 40
        if (raw["torrentUrl"]?.startsWith("magnet:", ignoreCase = true) == true) score += 20
        if ("mp4" in lower) score += 80
        if ("avc" in lower || "h264" in lower || "h.264" in lower) score += 40
        if ("1080" in lower) score += 24
        if ("720" in lower) score += 14
        if ("hevc" in lower || "x265" in lower || "10bit" in lower || "av1" in lower) score -= 30
        if ("batch" in lower || "\u5408\u96C6" in lower) score -= 70
        raw["seeders"]?.toIntOrNull()?.let { score += it.coerceAtMost(300) / 4 }
        raw["sizeBytes"]?.toLongOrNull()?.let { bytes ->
            score += when {
                bytes in 150L * 1024L * 1024L..2_500L * 1024L * 1024L -> 35
                bytes > 4L * 1024L * 1024L * 1024L -> -45
                else -> 0
            }
        }
        return score
    }

    private fun MediaStream.toRouteCandidate(episode: Episode): RouteCandidate {
        val sourceId = metadata["routeProviderId"] ?: providerId
        val sourceName = metadata["routeProviderName"]
            ?: metadata["sourceName"]
            ?: providersById[sourceId]?.manifest?.name
            ?: providerDisplayId(sourceId)
        return RouteCandidate(
            stream = this,
            sourceId = sourceId,
            sourceName = sourceName,
            title = metadata["routeTitle"] ?: metadata["catalogTitle"] ?: episode.title,
            routeName = metadata["routeSubtitle"]?.takeIf { it.isNotBlank() } ?: quality,
            episodeTitle = metadata["routeEpisodeTitle"] ?: metadata["episodeTitle"] ?: episode.title,
            episodeIndex = metadata["routeEpisodeIndex"]?.toIntOrNull()
                ?: metadata["episodeIndex"]?.toIntOrNull()
                ?: episode.index,
            quality = quality ?: metadata["quality"],
            subgroup = metadata["subgroup"],
            protocol = protocol,
            score = metadata["routeScore"]?.toIntOrNull() ?: sourceScore,
            publishedAt = metadata["publishedAt"],
            sizeBytes = metadata["sizeBytes"]?.toLongOrNull(),
        )
    }

    private fun providerDisplayId(sourceId: String): String {
        return sourceId.replace('-', ' ').replaceFirstChar { it.uppercase() }
    }

    private companion object {
        const val SEARCH_PROVIDER_TIMEOUT_MS = 15_000L
    }
}

data class SourceSearchReport(
    val results: List<SearchResult>,
    val failures: List<SourceSearchFailure>,
)

data class SourceSearchFailure(
    val providerId: String,
    val providerName: String,
    val message: String,
)

private data class SourceSearchOutcome(
    val results: List<SearchResult>,
    val failure: SourceSearchFailure?,
)
