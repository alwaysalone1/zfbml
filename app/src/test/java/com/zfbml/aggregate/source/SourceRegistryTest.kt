package com.zfbml.aggregate.source

import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SourceRegistryTest {
    @Test
    fun routeCandidateResolutionCachesEpisode() = runTest {
        val provider = CountingProvider()
        val registry = SourceRegistry(listOf(provider))
        val episode = episode("1")

        val first = registry.resolveRouteCandidates(episode)
        val second = registry.resolveRouteCandidates(episode)

        assertEquals(first, second)
        assertEquals(1, provider.resolveCount.get())
    }

    @Test
    fun routeCandidateResolutionCoalescesConcurrentCalls() = runTest {
        val provider = CountingProvider(delayMs = 50)
        val registry = SourceRegistry(listOf(provider))
        val episode = episode("2")

        val results = List(5) {
            async { registry.resolveRouteCandidates(episode) }
        }.awaitAll()

        assertEquals(1, results.distinct().size)
        assertEquals(1, provider.resolveCount.get())
    }

    @Test
    fun routeCandidateCacheSeparatesEpisodes() = runTest {
        val provider = CountingProvider()
        val registry = SourceRegistry(listOf(provider))

        registry.resolveRouteCandidates(episode("1"))
        registry.resolveRouteCandidates(episode("2"))

        assertEquals(2, provider.resolveCount.get())
    }

    @Test
    fun failedRouteCandidateResolutionIsNotCached() = runTest {
        val provider = FlakyProvider()
        val registry = SourceRegistry(listOf(provider))
        val episode = episode("3")

        val first = runCatching { registry.resolveRouteCandidates(episode) }
        val second = registry.resolveRouteCandidates(episode)

        assertEquals(true, first.isFailure)
        assertEquals(1, second.size)
        assertEquals(2, provider.resolveCount.get())
    }

    private fun episode(id: String): Episode {
        return Episode(
            providerId = "counting",
            id = "ep-$id",
            title = "第 $id 集",
            url = "counting://episode/$id",
            index = id.toInt(),
            raw = mapOf(
                "subjectId" to "subject-1",
                "episodeId" to id,
            ),
        )
    }

    private class CountingProvider(
        private val delayMs: Long = 0L,
    ) : SourceProvider {
        val resolveCount = AtomicInteger(0)

        override val manifest = SourceManifest(
            id = "counting",
            name = "Counting Provider",
            version = "1",
            author = "test",
            capabilities = setOf(SourceCapability.SEARCH, SourceCapability.DETAIL, SourceCapability.EPISODES, SourceCapability.STREAM),
        )

        override suspend fun search(query: String): List<SearchResult> = emptyList()

        override suspend fun loadDetail(result: SearchResult): MediaDetail {
            return MediaDetail(providerId = manifest.id, title = result.title, url = result.url)
        }

        override suspend fun resolveStreams(episode: Episode): List<MediaStream> {
            resolveCount.incrementAndGet()
            if (delayMs > 0) delay(delayMs)
            return listOf(
                MediaStream(
                    id = "${episode.id}-hls",
                    providerId = manifest.id,
                    url = "${episode.url}.m3u8",
                    protocol = StreamProtocol.HLS,
                    quality = "1080p",
                    sourceScore = 100,
                ),
            )
        }
    }

    private class FlakyProvider : SourceProvider {
        val resolveCount = AtomicInteger(0)

        override val manifest = SourceManifest(
            id = "counting",
            name = "Flaky Provider",
            version = "1",
            author = "test",
            capabilities = setOf(SourceCapability.SEARCH, SourceCapability.DETAIL, SourceCapability.EPISODES, SourceCapability.STREAM),
        )

        override suspend fun search(query: String): List<SearchResult> = emptyList()

        override suspend fun loadDetail(result: SearchResult): MediaDetail {
            return MediaDetail(providerId = manifest.id, title = result.title, url = result.url)
        }

        override suspend fun resolveStreams(episode: Episode): List<MediaStream> {
            if (resolveCount.incrementAndGet() == 1) {
                error("temporary source failure")
            }
            return listOf(
                MediaStream(
                    id = "${episode.id}-retry-hls",
                    providerId = manifest.id,
                    url = "${episode.url}.m3u8",
                    protocol = StreamProtocol.HLS,
                    quality = "1080p",
                    sourceScore = 100,
                ),
            )
        }
    }
}
