package com.zfbml.aggregate.danmaku

import com.zfbml.aggregate.source.Episode
import com.zfbml.aggregate.source.MediaDetail
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

        assertEquals(1, provider.matchCount.get())
        assertEquals(2, provider.fetchCount.get())
    }

    @Test
    fun matchAllCachesAutomaticProviderMatches() = runTest {
        val provider = CountingDanmakuProvider()
        val registry = DanmakuRegistry(listOf(provider))

        val first = registry.matchAll(detail(), episode("11"))
        val second = registry.matchAll(detail(), episode("11"))

        assertEquals(first, second)
        assertEquals(1, provider.matchCount.get())
        assertEquals(0, provider.fetchCount.get())
    }

    @Test
    fun matchAllCoalescesConcurrentAutomaticProviderMatches() = runTest {
        val provider = CountingDanmakuProvider(delayMs = 50)
        val registry = DanmakuRegistry(listOf(provider))

        val matches = List(5) {
            async { registry.matchAll(detail(), episode("12")) }
        }.awaitAll()

        assertEquals(1, matches.distinct().size)
        assertEquals(1, provider.matchCount.get())
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
    fun searchCandidatesUsesManualQueryTitle() = runTest {
        val provider = CountingDanmakuProvider(id = "manual-search")
        val registry = DanmakuRegistry(listOf(provider))
        val autoAliasEpisode = episode(
            id = "9",
            raw = mapOf(
                "subjectId" to "subject-9",
                "episodeId" to "9",
                "subjectNameCn" to "Auto Title",
                "subjectAliases" to "Auto Alias|Another Alias",
                "subjectName" to "Original Auto Title",
            ),
        )

        val matches = registry.searchCandidates(detail(), autoAliasEpisode, "  Manual Alias  ")
        val blankMatches = registry.searchCandidates(detail(), autoAliasEpisode, "   ")

        assertEquals(listOf("Manual Alias"), matches.map { it.title })
        assertEquals(emptyList<DanmakuMatch>(), blankMatches)
        assertEquals(listOf("Manual Alias"), provider.matchedTitles.toList())
        assertEquals(1, provider.matchCount.get())
    }

    @Test
    fun searchCandidatesCachesByManualQuery() = runTest {
        val provider = CountingDanmakuProvider(id = "manual-search", tokenFromTitle = true)
        val registry = DanmakuRegistry(listOf(provider))

        registry.searchCandidates(detail(), episode("13"), "Alias A")
        registry.searchCandidates(detail(), episode("13"), "Alias A")
        registry.searchCandidates(detail(), episode("13"), "Alias B")

        assertEquals(listOf("Alias A", "Alias B"), provider.matchedTitles.toList())
        assertEquals(2, provider.matchCount.get())
    }

    @Test
    fun matchAllSearchesSubjectAliasesBeforeDetailTitle() = runTest {
        val provider = CountingDanmakuProvider(tokenFromTitle = true)
        val registry = DanmakuRegistry(listOf(provider))
        val chineseTitle = "\u4e2d\u6587\u6807\u9898"
        val chineseAlias = "\u4e2d\u6587\u522b\u540d"
        val expectedTitles = listOf(chineseTitle, chineseAlias, "Display Title", "Alias One")

        val matches = registry.matchAll(
            detail(title = "Fallback Detail"),
            episode(
                id = "10",
                raw = mapOf(
                    "subjectId" to "subject-10",
                    "episodeId" to "10",
                    "subjectNameCn" to chineseTitle,
                    "subjectTitle" to "Display Title",
                    "subjectAliases" to "$chineseAlias|Alias One|Alias Two|Alias Three",
                    "subjectName" to "Original Title",
                ),
            ),
        )

        assertEquals(expectedTitles, matches.map { it.title })
        assertEquals(expectedTitles.size, provider.matchedTitles.size)
        assertEquals(expectedTitles.toSet(), provider.matchedTitles.toSet())
        assertEquals(4, provider.matchCount.get())
    }

    @Test
    fun candidateScoreRewardsExactTitleAndEpisodeSignals() {
        val exactLowerBase = danmakuCandidateMatchScore(
            baseScore = 82,
            queryTitle = "One Piece",
            candidateTitle = "One Piece",
            candidateEpisodeTitle = "Episode 12",
            candidateEpisodeOrder = 12,
            requestedEpisodeTitle = "Episode 12",
            requestedEpisodeNumber = 12,
        )
        val looseHigherBase = danmakuCandidateMatchScore(
            baseScore = 90,
            queryTitle = "One Piece",
            candidateTitle = "One Piece Special",
            candidateEpisodeTitle = "Episode 1",
            candidateEpisodeOrder = 1,
            requestedEpisodeTitle = "Episode 12",
            requestedEpisodeNumber = 12,
        )

        assertTrue(exactLowerBase > looseHigherBase)
        assertEquals(
            0,
            danmakuEpisodeMatchScore(
                candidateEpisodeTitle = "Episode 1",
                candidateEpisodeOrder = 1,
                requestedEpisodeTitle = "Episode 12",
                requestedEpisodeNumber = 12,
            ),
        )
    }

    @Test
    fun titleScoreNormalizesCommonAnimeAliases() {
        val score = danmakuTitleMatchScore(
            queryTitle = "\u6d77\u8d3c\u738b",
            candidateTitle = "\u822a\u6d77\u738b",
        )

        assertTrue(score > 0)
    }

    @Test
    fun episodeNumberParserReadsChineseAndNumericEpisodeText() {
        assertEquals(12, danmakuEpisodeNumberFromText("\u7b2c\u5341\u4e8c\u8bdd"))
        assertEquals(3, danmakuEpisodeNumberFromText("\u7b2c\u4e09\u96c6"))
        assertEquals(24, danmakuEpisodeNumberFromText("\u7b2c24\u96c6"))
        assertEquals(7, danmakuEpisodeNumberFromText("EP.07"))
        assertNull(danmakuEpisodeNumberFromText("OVA"))
    }

    @Test
    fun episodeScoreUsesChineseEpisodeNumbers() {
        val chineseEpisodeScore = danmakuEpisodeMatchScore(
            candidateEpisodeTitle = "\u7b2c\u5341\u4e8c\u8bdd",
            candidateEpisodeOrder = null,
            requestedEpisodeTitle = "\u7b2c12\u8bdd",
            requestedEpisodeNumber = 12,
        )
        val wrongEpisodeScore = danmakuEpisodeMatchScore(
            candidateEpisodeTitle = "\u7b2c\u4e00\u8bdd",
            candidateEpisodeOrder = null,
            requestedEpisodeTitle = "\u7b2c12\u8bdd",
            requestedEpisodeNumber = 12,
        )

        assertTrue(chineseEpisodeScore > wrongEpisodeScore)
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

    @Test
    fun manualMappingFetchesCalibratedTimelineBeforeAutomaticMatching() = runTest {
        val manual = CountingDanmakuProvider(id = "manual", score = 1)
        val automatic = CountingDanmakuProvider(id = "automatic", score = 200)
        val registry = DanmakuRegistry(
            providers = listOf(manual, automatic),
            initialManualMappings = listOf(
                manualMapping(
                    episodeIndex = 6,
                    providerId = "manual",
                    token = "manual-ep-6",
                ),
            ),
        )

        val timeline = registry.fetchBestTimeline(detail(), episode("6"))

        assertEquals(listOf("manual-manual-ep-6"), timeline.map { it.text })
        assertEquals(0, manual.matchCount.get())
        assertEquals(0, automatic.matchCount.get())
        assertEquals(1, manual.fetchCount.get())
        assertEquals(0, automatic.fetchCount.get())
    }

    @Test
    fun manualMappingFallsBackToAutomaticMatchWhenTimelineIsEmpty() = runTest {
        val manual = CountingDanmakuProvider(id = "manual", score = 1, returnEmptyTimeline = true)
        val automatic = CountingDanmakuProvider(id = "automatic", score = 200)
        val registry = DanmakuRegistry(
            providers = listOf(manual, automatic),
            initialManualMappings = listOf(
                manualMapping(
                    episodeIndex = 7,
                    providerId = "manual",
                    token = "manual-ep-7",
                ),
            ),
        )

        val timeline = registry.fetchBestTimeline(detail(), episode("7"))

        assertEquals(listOf("automatic-ep-7"), timeline.map { it.text })
        assertEquals(1, manual.fetchCount.get())
        assertEquals(1, automatic.matchCount.get())
        assertEquals(1, automatic.fetchCount.get())
    }

    @Test
    fun replacingManualMappingClearsCachedAutomaticTimeline() = runTest {
        val automatic = CountingDanmakuProvider(id = "automatic", score = 200)
        val manual = CountingDanmakuProvider(id = "manual", score = 1)
        val registry = DanmakuRegistry(listOf(automatic, manual))

        val automaticTimeline = registry.fetchBestTimeline(detail(), episode("8"))
        registry.addOrReplaceManualMapping(
            manualMapping(
                episodeIndex = 8,
                providerId = "manual",
                token = "manual-ep-8",
            ),
        )
        val correctedTimeline = registry.fetchBestTimeline(detail(), episode("8"))

        assertEquals(listOf("automatic-ep-8"), automaticTimeline.map { it.text })
        assertEquals(listOf("manual-manual-ep-8"), correctedTimeline.map { it.text })
        assertEquals(1, automatic.fetchCount.get())
        assertEquals(1, manual.fetchCount.get())
    }

    private fun detail(title: String = "Test Anime"): MediaDetail {
        return MediaDetail(
            providerId = "detail",
            title = title,
            url = "detail://anime",
        )
    }

    private fun episode(
        id: String,
        raw: Map<String, String> = mapOf("subjectId" to "subject-1", "episodeId" to id),
    ): Episode {
        return Episode(
            providerId = "detail",
            id = "ep-$id",
            title = "Episode $id",
            url = "detail://anime/episode/$id",
            index = id.toInt(),
            raw = raw,
        )
    }

    private fun manualMapping(
        episodeIndex: Int,
        providerId: String,
        token: String,
    ): DanmakuManualMapping {
        return DanmakuManualMapping(
            detailTitle = "test anime",
            detailProviderId = "detail",
            episodeIndex = episodeIndex,
            match = DanmakuMatch(
                providerId = providerId,
                platform = DanmakuPlatform.Local,
                title = "Manual Test Anime",
                episodeTitle = "Manual Episode $episodeIndex",
                score = 1,
                token = token,
            ),
        )
    }

    private class CountingDanmakuProvider(
        override val id: String = "counting-danmaku",
        private val score: Int = 100,
        private val delayMs: Long = 0L,
        private val returnEmptyTimeline: Boolean = false,
        private val tokenFromTitle: Boolean = false,
        private val activeMatches: AtomicInteger? = null,
        private val maxConcurrentMatches: AtomicInteger? = null,
    ) : DanmakuProvider {
        val matchCount = AtomicInteger(0)
        val fetchCount = AtomicInteger(0)
        val matchedTitles = CopyOnWriteArrayList<String>()

        override val platform = DanmakuPlatform.Local
        override val profile = DanmakuProfile(DanmakuPlatform.Local)
        override val authDomain: String? = null

        override suspend fun match(detail: MediaDetail, episode: Episode): List<DanmakuMatch> {
            matchCount.incrementAndGet()
            matchedTitles += detail.title
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
                        token = if (tokenFromTitle) "${episode.id}-${detail.title}" else episode.id,
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
