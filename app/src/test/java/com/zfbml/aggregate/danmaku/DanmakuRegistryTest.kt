package com.zfbml.aggregate.danmaku

import com.zfbml.aggregate.source.Episode
import com.zfbml.aggregate.source.MediaDetail
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
        private val delayMs: Long = 0L,
        private val returnEmptyTimeline: Boolean = false,
    ) : DanmakuProvider {
        val matchCount = AtomicInteger(0)
        val fetchCount = AtomicInteger(0)

        override val id = "counting-danmaku"
        override val platform = DanmakuPlatform.Local
        override val profile = DanmakuProfile(DanmakuPlatform.Local)
        override val authDomain: String? = null

        override suspend fun match(detail: MediaDetail, episode: Episode): List<DanmakuMatch> {
            matchCount.incrementAndGet()
            if (delayMs > 0) delay(delayMs)
            return listOf(
                DanmakuMatch(
                    providerId = id,
                    platform = platform,
                    title = detail.title,
                    episodeTitle = episode.title,
                    score = 100,
                    token = episode.id,
                ),
            )
        }

        override suspend fun fetchTimeline(match: DanmakuMatch): List<DanmakuItem> {
            fetchCount.incrementAndGet()
            if (delayMs > 0) delay(delayMs)
            if (returnEmptyTimeline) return emptyList()
            return listOf(
                DanmakuItem(
                    timeMs = 1_000L,
                    text = "cached-${match.token}",
                    mode = DanmakuMode.Scroll,
                    platform = platform,
                ),
            )
        }

        override fun normalize(raw: String): List<DanmakuItem> = emptyList()
    }
}
