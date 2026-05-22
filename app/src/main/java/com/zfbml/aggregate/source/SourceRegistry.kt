package com.zfbml.aggregate.source

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class SourceRegistry(
    providers: List<SourceProvider>,
) {
    private val providersById = providers.associateBy { it.manifest.id }

    val manifests: List<SourceManifest> = providers.map { it.manifest }

    fun provider(id: String): SourceProvider? = providersById[id]

    suspend fun searchAll(query: String): List<SearchResult> = coroutineScope {
        providersById.values
            .filter { SourceCapability.SEARCH in it.manifest.capabilities }
            .map { provider ->
                async {
                    runCatching { provider.search(query) }.getOrDefault(emptyList())
                }
            }
            .awaitAll()
            .flatten()
            .sortedWith(compareByDescending<SearchResult> { it.playabilityScore() }.thenBy { it.title })
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

    private fun SearchResult.playabilityScore(): Int {
        val lower = title.lowercase()
        var score = 0
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
}
