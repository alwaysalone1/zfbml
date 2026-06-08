package com.zfbml.aggregate.danmaku

import com.zfbml.aggregate.source.Episode
import com.zfbml.aggregate.source.MediaDetail
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DanmakuRegistryTest {
    @Test
    fun fetchBestTimelineCachesEpisode() = runTest {
        val provider = CountingDanmakuProvider()
        val registry = DanmakuRegistry(listOf(provider))

        val first = registry.fetchBestTimeline(detail(), episode("1"))
        val second = registry.fetchBestTimeline(detail(), episode("1"))

        assertEquals(first, second)
        assertEquals(1, provider.matchCount.get())
        assertEquals(1, provider.fetchCount.get())
    }

    @Test
    fun fetchBestTimelineCoalescesConcurrentCalls() = runTest {
        val provider = CountingDanmakuProvider(delayMs = 50)
        val registry = DanmakuRegistry(listOf(provider))

        val timelines = List(5) {
            async { registry.fetchBestTimeline(detail(), episode("2")) }
        }.awaitAll()

        assertEquals(1, timelines.distinct().size)
        assertEquals(1, provider.matchCount.get())
        assertEquals(1, provider.fetchCount.get())
    }

    @Test
    fun emptyTimelineIsNotCached() = runTest {
        val provider = CountingDanmakuProvider(returnEmptyTimeline = true)
        val registry = DanmakuRegistry(listOf(provider))

        registry.fetchBestTimeline(detail(), episode("3"))
        registry.fetchBestTimeline(detail(), episode("3"))

        assertEquals(2, provider.matchCount.get())
        assertEquals(2, provider.fetchCount.get())
    }

    @Test
    fun matchAllQueriesProvidersConcurrently() = runTest {
        val activeMatches = AtomicInteger(0)
        val maxConcurrentMatches = AtomicInteger(0)
        val first = CountingDanmakuProvider(
            id = "match-a",
            delayMs = 50,
            activeMatches = activeMatches,
            maxConcurrentMatches = maxConcurrentMatches,
        )
        val second = CountingDanmakuProvider(
            id = "match-b",
            delayMs = 50,
            activeMatches = activeMatches,
            maxConcurrentMatches = maxConcurrentMatches,
        )
        val registry = DanmakuRegistry(listOf(first, second))

        val matches = registry.matchAll(detail(), episode("4"))

        assertEquals(setOf("match-a", "match-b"), matches.map { it.providerId }.toSet())
        assertTrue(maxConcurrentMatches.get() > 1)
    }

    @Test
    fun fetchBestTimelineFallsBackWhenBestMatchIsEmpty() = runTest {
        val emptyBest = CountingDanmakuProvider(
            id = "empty-best",
            score = 200,
            returnEmptyTimeline = true,
        )
        val filledFallback = CountingDanmakuProvider(
            id = "filled-fallback",
            score = 100,
        )
        val registry = DanmakuRegistry(listOf(emptyBest, filledFallback))

        val timeline = registry.fetchBestTimeline(detail(), episode("5"))

        assertEquals(listOf("filled-fallback-ep-5"), timeline.map { it.text })
        assertEquals(1, emptyBest.fetchCount.get())
        assertEquals(1, filledFallback.fetchCount.get())
    }

    private fun detail(): MediaDetail {
        return MediaDetail(
            providerId = "detail",
            title = "Test Anime",
            url = "detail://anime",
        )
    }

    private fun episode(id: String): Episode {
        return Episode(
            providerId = "detail",
            id = "ep-$id",
            title = "Episode $id",
            url = "detail://anime/episode/$id",
            index = id.toInt(),
            raw = mapOf("subjectId" to "subject-1", "episodeId" to id),
        )
    }

    private class CountingDanmakuProvider(
        override val id: String = "counting-danmaku",
        private val score: Int = 100,
        private val delayMs: Long = 0L,
        private val returnEmptyTimeline: Boolean = false,
        private val activeMatches: AtomicInteger? = null,
        private val maxConcurrentMatches: AtomicInteger? = null,
    ) : DanmakuProvider {
        val matchCount = AtomicInteger(0)
        val fetchCount = AtomicInteger(0)

        override val platform = DanmakuPlatform.Local
        override val profile = DanmakuProfile(DanmakuPlatform.Local)
        override val authDomain: String? = null

        override suspend fun match(detail: MediaDetail, episode: Episode): List<DanmakuMatch> {
            matchCount.incrementAndGet()
            activeMatches?.incrementAndGet()?.let { active ->
                updateMaxConcurrentMatches(active)
            }
            return try {
                if (delayMs > 0) delay(delayMs)
                listOf(
                    DanmakuMatch(
                        providerId = id,
                        platform = platform,
                        title = detail.title,
                        episodeTitle = episode.title,
                        score = score,
                        token = episode.id,
                    ),
                )
            } finally {
                activeMatches?.decrementAndGet()
            }
        }

        override suspend fun fetchTimeline(match: DanmakuMatch): List<DanmakuItem> {
            fetchCount.incrementAndGet()
            if (delayMs > 0) delay(delayMs)
            if (returnEmptyTimeline) return emptyList()
            return listOf(
                DanmakuItem(
                    timeMs = 1_000L,
                    text = "$id-${match.token}",
                    mode = DanmakuMode.Scroll,
                    platform = platform,
                ),
            )
        }

        override fun normalize(raw: String): List<DanmakuItem> = emptyList()

        private fun updateMaxConcurrentMatches(active: Int) {
            val maxCounter = maxConcurrentMatches ?: return
            while (true) {
                val currentMax = maxCounter.get()
                if (active <= currentMax || maxCounter.compareAndSet(currentMax, active)) return
            }
        }
    }
}
