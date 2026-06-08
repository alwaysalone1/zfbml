package com.zfbml.aggregate.source

import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SourceRegistryTest {
    @Test
    fun detailLoadingCachesResult() = runTest {
        val provider = CountingProvider()
        val registry = SourceRegistry(listOf(provider))
        val result = result("1")

        val first = registry.loadDetail(result)
        val second = registry.loadDetail(result)

        assertEquals(first, second)
        assertEquals(1, provider.detailCount.get())
    }

    @Test
    fun detailLoadingCoalescesConcurrentCalls() = runTest {
        val provider = CountingProvider(detailDelayMs = 50)
        val registry = SourceRegistry(listOf(provider))
        val result = result("2")

        val results = List(5) {
            async { registry.loadDetail(result) }
        }.awaitAll()

        assertEquals(1, results.distinct().size)
        assertEquals(1, provider.detailCount.get())
    }

    @Test
    fun failedDetailLoadingIsNotCached() = runTest {
        val provider = FlakyProvider()
        val registry = SourceRegistry(listOf(provider))
        val result = result("3")

        val first = runCatching { registry.loadDetail(result) }
        val second = registry.loadDetail(result)

        assertEquals(true, first.isFailure)
        assertEquals("Title 3", second.title)
        assertEquals(2, provider.detailCount.get())
    }

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

    @Test
    fun emptyRouteCandidateResolutionIsNotCached() = runTest {
        val provider = EmptyThenReadyProvider()
        val registry = SourceRegistry(listOf(provider))
        val episode = episode("7")

        val first = registry.resolveRouteCandidates(episode)
        val second = registry.resolveRouteCandidates(episode)

        assertEquals(emptyList<RouteCandidate>(), first)
        assertEquals(1, second.size)
        assertEquals("ep-7-recovered-hls", second.single().stream.id)
        assertEquals(2, provider.resolveCount.get())
    }

    @Test
    fun prefetchRouteCandidatesWarmsCache() = runTest {
        val provider = CountingProvider()
        val registry = SourceRegistry(listOf(provider))
        val episode = episode("4")

        val prefetched = registry.prefetchRouteCandidates(episode)
        registry.resolveRouteCandidates(episode)

        assertEquals(true, prefetched)
        assertEquals(1, provider.resolveCount.get())
    }

    @Test
    fun peekRouteCandidatesReturnsWarmCacheOnly() = runTest {
        val provider = CountingProvider()
        val registry = SourceRegistry(listOf(provider))
        val episode = episode("6")

        assertNull(registry.peekRouteCandidates(episode))

        val prefetched = registry.prefetchRouteCandidates(episode)
        val cached = registry.peekRouteCandidates(episode)

        assertEquals(true, prefetched)
        assertNotNull(cached)
        assertEquals("ep-6-hls", cached!!.single().stream.id)
        assertEquals(1, provider.resolveCount.get())
    }

    @Test
    fun prefetchRouteCandidatesReturnsFalseForFailures() = runTest {
        val provider = AlwaysFailingProvider()
        val registry = SourceRegistry(listOf(provider))

        val prefetched = registry.prefetchRouteCandidates(episode("5"))

        assertEquals(false, prefetched)
        assertEquals(1, provider.resolveCount.get())
    }

    @Test
    fun prefetchRouteCandidatesDoesNotWarmEmptyResults() = runTest {
        val provider = EmptyProvider()
        val registry = SourceRegistry(listOf(provider))
        val episode = episode("8")

        val prefetched = registry.prefetchRouteCandidates(episode)
        val cached = registry.peekRouteCandidates(episode)

        assertEquals(false, prefetched)
        assertNull(cached)
        assertEquals(1, provider.resolveCount.get())
    }

    private fun result(id: String): SearchResult {
        return SearchResult(
            providerId = "counting",
            title = "Title $id",
            url = "counting://detail/$id",
            raw = mapOf("subjectId" to "subject-$id"),
        )
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
        private val detailDelayMs: Long = 0L,
    ) : SourceProvider {
        val detailCount = AtomicInteger(0)
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
            val count = detailCount.incrementAndGet()
            if (detailDelayMs > 0) delay(detailDelayMs)
            return MediaDetail(
                providerId = manifest.id,
                title = result.title,
                url = result.url,
                summary = "detail-$count",
            )
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
        val detailCount = AtomicInteger(0)
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
            if (detailCount.incrementAndGet() == 1) {
                error("temporary detail failure")
            }
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

    private class AlwaysFailingProvider : SourceProvider {
        val resolveCount = AtomicInteger(0)

        override val manifest = SourceManifest(
            id = "counting",
            name = "Always Failing Provider",
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
            error("source failure")
        }
    }

    private class EmptyProvider : SourceProvider {
        val resolveCount = AtomicInteger(0)

        override val manifest = SourceManifest(
            id = "counting",
            name = "Empty Provider",
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
            return emptyList()
        }
    }

    private class EmptyThenReadyProvider : SourceProvider {
        val resolveCount = AtomicInteger(0)

        override val manifest = SourceManifest(
            id = "counting",
            name = "Empty Then Ready Provider",
            version = "1",
            author = "test",
            capabilities = setOf(SourceCapability.SEARCH, SourceCapability.DETAIL, SourceCapability.EPISODES, SourceCapability.STREAM),
        )

        override suspend fun search(query: String): List<SearchResult> = emptyList()

        override suspend fun loadDetail(result: SearchResult): MediaDetail {
            return MediaDetail(providerId = manifest.id, title = result.title, url = result.url)
        }

        override suspend fun resolveStreams(episode: Episode): List<MediaStream> {
            val count = resolveCount.incrementAndGet()
            if (count == 1) return emptyList()
            return listOf(
                MediaStream(
                    id = "${episode.id}-recovered-hls",
                    providerId = manifest.id,
                    url = "${episode.url}/recovered.m3u8",
                    protocol = StreamProtocol.HLS,
                    quality = "1080p",
                    sourceScore = 100,
                ),
            )
        }
    }
}
