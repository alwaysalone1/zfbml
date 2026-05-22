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
}
