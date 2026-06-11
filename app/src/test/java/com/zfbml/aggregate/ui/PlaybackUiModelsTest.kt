package com.zfbml.aggregate.ui

import androidx.compose.ui.unit.dp
import com.zfbml.aggregate.danmaku.DanmakuMatch
import com.zfbml.aggregate.danmaku.DanmakuMatchSource
import com.zfbml.aggregate.danmaku.DanmakuPlatform
import com.zfbml.aggregate.source.Episode
import com.zfbml.aggregate.source.DownloadPolicy
import com.zfbml.aggregate.source.MediaDetail
import com.zfbml.aggregate.source.MediaStream
import com.zfbml.aggregate.source.RouteCandidate
import com.zfbml.aggregate.source.SearchResult
import com.zfbml.aggregate.source.SourceCapability
import com.zfbml.aggregate.source.SourceManifest
import com.zfbml.aggregate.source.SourceSearchFailure
import com.zfbml.aggregate.source.SourceSearchReport
import com.zfbml.aggregate.source.StreamProtocol
import com.zfbml.aggregate.source.catalog.BangumiCategory
import com.zfbml.aggregate.source.catalog.BangumiScheduleDay
import com.zfbml.aggregate.torrent.TorrentEngineState
import com.zfbml.aggregate.torrent.TorrentPlaybackPlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackUiModelsTest {
    @Test
    fun routeUiStatePromotesPlayableOnlineRoute() {
        val bt = route("bt", StreamProtocol.BITTORRENT, 900, quality = "1080p")
        val hls = route("hls", StreamProtocol.HLS, 450, quality = "720p")

        val state = buildRouteUiState(
            selectedEpisode = episode(),
            routes = listOf(bt, hls),
            loading = false,
            error = null,
        )

        assertEquals(RouteLoadStatus.Ready, state.status)
        assertEquals("hls", state.bestRoute?.stream?.id)
        assertEquals(1, state.onlineCount)
        assertEquals(1, state.btCount)
        assertEquals("Provider", state.recommendationTitle)
        assertTrue(state.recommendationDetail.contains("720p"))
        assertTrue(state.recommendationReason.contains("\u5728\u7ebf\u64ad\u653e\u4f18\u5148"))
        assertTrue(state.recommendationReason.contains("720p"))
        assertTrue(state.canPlay)
    }

    @Test
    fun routeUiStateMovesFailedStreamBehindFallback() {
        val primary = route("primary", StreamProtocol.HLS, 900, quality = "1080p")
        val fallback = route("fallback", StreamProtocol.PROGRESSIVE, 300, quality = "720p")

        val state = buildRouteUiState(
            selectedEpisode = episode(),
            routes = listOf(primary, fallback),
            loading = false,
            error = null,
            failedStreamIds = setOf("primary"),
        )

        assertEquals("fallback", state.bestRoute?.stream?.id)
        assertEquals("fallback", state.visibleRoutes.first().stream.id)
        assertEquals(1, state.failedCount)
    }

    @Test
    fun routeUiStateReportsLoadingAndEmptyClearly() {
        val loading = buildRouteUiState(episode(), emptyList(), loading = true, error = null)
        val empty = buildRouteUiState(episode(), emptyList(), loading = false, error = null)
        val failed = buildRouteUiState(episode(), emptyList(), loading = false, error = "HTTP 500")

        assertEquals(RouteLoadStatus.Loading, loading.status)
        assertFalse(loading.canPlay)
        assertEquals(RouteLoadStatus.Empty, empty.status)
        assertEquals(RouteLoadStatus.Failed, failed.status)
        assertEquals("HTTP 500", failed.detail)
    }

    @Test
    fun routeUiStateExposesLoadingStepsForDetailCard() {
        val loading = buildRouteUiState(episode(), emptyList(), loading = true, error = null)

        assertEquals(3, loading.loadingSteps.size)
        assertTrue(loading.loadingSteps.all { it.active })
        assertEquals(loading.selectedEpisodeTitle, loading.loadingSteps.first().value)
    }

    @Test
    fun routeUiStateLoadingStepsSummarizeResolvedSources() {
        val hls = route("hls", StreamProtocol.HLS, 450, quality = "720p", sourceId = "online", sourceName = "Online")
        val bt = route("bt", StreamProtocol.BITTORRENT, 900, quality = "1080p", sourceId = "bt", sourceName = "BT")

        val ready = buildRouteUiState(
            selectedEpisode = episode(),
            routes = listOf(hls, bt),
            loading = false,
            error = null,
        )

        assertEquals(RouteLoadStatus.Ready, ready.status)
        assertTrue(ready.loadingSteps[1].active)
        assertTrue(ready.loadingSteps[2].active)
        assertEquals("1线", ready.loadingSteps[1].value)
        assertEquals("1线", ready.loadingSteps[2].value)
        assertEquals("2源 · 2线", ready.sourceCoverageLabel)
    }

    @Test
    fun routeUiStateLabelsCachedAndLiveRouteOrigins() {
        val hls = route("hls", StreamProtocol.HLS, 450, quality = "720p", sourceId = "online", sourceName = "Online")

        val cached = buildRouteUiState(
            selectedEpisode = episode(),
            routes = listOf(hls),
            loading = false,
            error = null,
            loadedFromCache = true,
        )
        val live = buildRouteUiState(
            selectedEpisode = episode(),
            routes = listOf(hls),
            loading = false,
            error = null,
            loadedFromCache = false,
        )
        val loading = buildRouteUiState(
            selectedEpisode = episode(),
            routes = emptyList(),
            loading = true,
            error = null,
        )

        assertEquals("预取命中", cached.loadOriginLabel)
        assertEquals("实时匹配", live.loadOriginLabel)
        assertEquals("实时匹配", loading.loadOriginLabel)
    }

    @Test
    fun routeUiStateSummarizesOnlineSourceCoverage() {
        val first = route("hls-a", StreamProtocol.HLS, 600, quality = "1080p", sourceId = "online-a", sourceName = "Online A")
        val second = route("hls-b", StreamProtocol.HLS, 400, quality = "720p", sourceId = "online-b", sourceName = "Online B")

        val state = buildRouteUiState(
            selectedEpisode = episode(),
            routes = listOf(first, second),
            loading = false,
            error = null,
        )

        assertEquals(2, state.sourceCount)
        assertEquals(2, state.onlineSourceCount)
        assertEquals("2源 · 2线", state.sourceCoverageLabel)
        assertEquals("2源 · 2线", state.loadingSteps[1].value)
    }

    @Test
    fun playerRouteCoverageLabelDistinguishesSourcesFromRoutes() {
        val onlineA = route("hls-a", StreamProtocol.HLS, 600, quality = "1080p", sourceId = "online-a", sourceName = "Online A")
        val onlineB = route("hls-b", StreamProtocol.HLS, 500, quality = "720p", sourceId = "online-b", sourceName = "Online B")
        val onlineBBackup = route("mp4-b", StreamProtocol.PROGRESSIVE, 400, quality = "480p", sourceId = "online-b", sourceName = "Online B")

        assertEquals("无线路", playerRouteCoverageLabel(emptyList()))
        assertEquals("单线", playerRouteCoverageLabel(listOf(onlineA)))
        assertEquals("2线", playerRouteCoverageLabel(listOf(onlineB, onlineBBackup)))
        assertEquals("2源", playerRouteCoverageLabel(listOf(onlineA, onlineB)))
        assertEquals("2源 · 3线", playerRouteCoverageLabel(listOf(onlineA, onlineB, onlineBBackup)))
    }

    @Test
    fun routeUiStateDoesNotAutoplayWebViewOnlyRoute() {
        val webView = route("webview", StreamProtocol.WEBVIEW_ONLY, 1_000, quality = "1080p")

        val state = buildRouteUiState(
            selectedEpisode = episode(),
            routes = listOf(webView),
            loading = false,
            error = null,
        )

        assertEquals(RouteLoadStatus.Empty, state.status)
        assertEquals(null, state.bestRoute)
        assertFalse(state.canPlay)
        assertEquals("暂无可用播放源", state.recommendationTitle)
    }

    @Test
    fun detailPlaybackReadinessSummarizesReadyOnlineRouteAndCache() {
        val hls = route("hls", StreamProtocol.HLS, 900, quality = "1080p", sourceName = "Online")
        val state = buildRouteUiState(
            selectedEpisode = episode(),
            routes = listOf(hls),
            loading = false,
            error = null,
        )

        val readiness = buildDetailPlaybackReadinessUiState(state)

        assertTrue(readiness.canPlay)
        assertTrue(readiness.cacheEnabled)
        assertEquals("\u64ad\u653e\u5c31\u7eea", readiness.headline)
        assertEquals("\u64ad\u653e\u63a8\u8350", readiness.primaryActionLabel)
        assertEquals("1 \u5728\u7ebf", readiness.onlineLabel)
        assertEquals("\u5907\u7528\u5f85\u547d", readiness.backupLabel)
        assertEquals("\u53ef\u79bb\u7ebf", readiness.cacheLabel)
        assertEquals(SourceLibraryTone.Cache, readiness.tone)
        assertEquals(SourceLibraryTone.Primary, readiness.cacheTone)
        assertEquals(
            listOf("\u7ebf\u8def", "\u5728\u7ebf", "\u5907\u7528", "\u7f13\u5b58"),
            readiness.chips.map { it.label },
        )
        assertTrue(readiness.chips.any { it.label == "\u7f13\u5b58" && it.value == "\u53ef\u79bb\u7ebf" && it.tone == SourceLibraryTone.Primary })
        assertTrue(readiness.summary.contains("Online"))
    }

    @Test
    fun detailPlaybackReadinessExplainsBtAndNonPlayableStates() {
        val btState = buildRouteUiState(
            selectedEpisode = episode(),
            routes = listOf(route("bt", StreamProtocol.BITTORRENT, 700, quality = "1080p")),
            loading = false,
            error = null,
        )
        val webOnlyState = buildRouteUiState(
            selectedEpisode = episode(),
            routes = listOf(route("web", StreamProtocol.WEBVIEW_ONLY, 900, quality = "1080p")),
            loading = false,
            error = null,
        )
        val loadingState = buildRouteUiState(
            selectedEpisode = episode(),
            routes = emptyList(),
            loading = true,
            error = null,
        )

        val bt = buildDetailPlaybackReadinessUiState(btState)
        val webOnly = buildDetailPlaybackReadinessUiState(webOnlyState)
        val loading = buildDetailPlaybackReadinessUiState(loadingState)

        assertTrue(bt.canPlay)
        assertFalse(bt.cacheEnabled)
        assertEquals("\u8fb9\u4e0b\u8fb9\u64ad", bt.primaryActionLabel)
        assertEquals("1 BT", bt.backupLabel)
        assertEquals("\u8fb9\u4e0b\u8fb9\u64ad", bt.cacheLabel)
        assertFalse(webOnly.canPlay)
        assertEquals("\u6682\u65e0\u53ef\u64ad\u7ebf\u8def", webOnly.headline)
        assertEquals("\u7b49\u5f85\u8865\u6e90", webOnly.primaryActionLabel)
        assertFalse(loading.canPlay)
        assertEquals("\u6b63\u5728\u5339\u914d\u64ad\u653e\u6e90", loading.headline)
        assertEquals("\u5339\u914d\u4e2d", loading.primaryActionLabel)
        assertEquals("\u4f18\u5148\u5339\u914d", loading.onlineLabel)
        assertEquals(SourceLibraryTone.Online, loading.tone)
        assertEquals(SourceLibraryTone.Muted, loading.cacheTone)
        assertTrue(loading.chips.any { it.label == "\u7f13\u5b58" && it.value == "\u5f85\u7ebf\u8def" })
    }

    @Test
    fun detailHeroActionUiStateSummarizesPlayableRouteAndRouteEntry() {
        val state = buildRouteUiState(
            selectedEpisode = episode(index = 6),
            routes = listOf(
                route("hls", StreamProtocol.HLS, 900, quality = "1080p", sourceId = "online", sourceName = "Online"),
                route("backup", StreamProtocol.PROGRESSIVE, 500, quality = "720p", sourceId = "backup", sourceName = "Backup"),
            ),
            loading = false,
            error = null,
        )

        val action = buildDetailHeroActionUiState(
            selectedEpisode = episode(index = 6),
            routeState = state,
        )

        assertEquals("\u64ad\u653e\u7b2c 6 \u96c6", action.primaryActionLabel)
        assertEquals("\u81ea\u52a8\u6700\u4f73", action.routeTitle)
        assertEquals("2 \u4e2a\u6765\u6e90", action.routeValue)
        assertEquals(SourceLibraryTone.Online, action.routeTone)
    }

    @Test
    fun detailHeroActionUiStateExplainsLoadingAndFailedRoutes() {
        val loading = buildDetailHeroActionUiState(
            selectedEpisode = episode(index = 2),
            routeState = buildRouteUiState(episode(index = 2), emptyList(), loading = true, error = null),
        )
        val failed = buildDetailHeroActionUiState(
            selectedEpisode = episode(index = 2),
            routeState = buildRouteUiState(episode(index = 2), emptyList(), loading = false, error = "HTTP 500"),
        )

        assertEquals("\u5339\u914d\u4e2d", loading.primaryActionLabel)
        assertEquals("\u5339\u914d\u4e2d", loading.routeTitle)
        assertEquals("\u4f18\u5148\u5728\u7ebf", loading.routeValue)
        assertEquals(SourceLibraryTone.Backup, loading.routeTone)

        assertEquals("\u91cd\u8bd5\u5339\u914d", failed.primaryActionLabel)
        assertEquals("\u64ad\u653e\u6e90\u5f02\u5e38", failed.routeTitle)
        assertEquals("\u67e5\u770b\u539f\u56e0", failed.routeValue)
        assertEquals(SourceLibraryTone.Web, failed.routeTone)
    }

    @Test
    fun detailHeroChromeUiStateBuildsHeroCopyChipsAndLayout() {
        val selectedEpisode = episode(id = "ep-3", index = 3)
        val detail = MediaDetail(
            providerId = "bangumi-catalog",
            title = "Alpha",
            url = "bangumi://subject/1",
            posterUrl = "https://example.invalid/alpha.jpg",
            summary = "Alpha summary",
            episodes = listOf(episode(index = 1), selectedEpisode),
        )

        val state = buildDetailHeroChromeUiState(detail, selectedEpisode)

        assertEquals("Alpha", state.title)
        assertEquals("Alpha summary", state.summary)
        assertEquals("https://example.invalid/alpha.jpg", state.posterUrl)
        assertEquals("bangumi-catalog", state.providerId)
        assertEquals(listOf("\u7b2c 3 \u96c6", "2 \u96c6", "\u81ea\u52a8\u5339\u914d"), state.chips.map { it.label })
        assertEquals(listOf(SourceLibraryTone.Primary, SourceLibraryTone.Online, SourceLibraryTone.Cache), state.chips.map { it.tone })
        assertEquals(360.dp, state.minHeight)
        assertEquals(116.dp, state.posterWidth)
        assertEquals(164.dp, state.posterHeight)
        assertEquals(48.dp, state.primaryButtonHeight)
        assertEquals(126.dp, state.routeButtonMinWidth)
        assertEquals(156.dp, state.routeButtonMaxWidth)
        assertEquals(3, state.summaryMaxLines)
        assertEquals(0.34f, state.backgroundPosterAlpha, 0.001f)
    }

    @Test
    fun detailHeroChromeUiStateFallsBackForMissingTitleSummaryAndEpisodes() {
        val detail = MediaDetail(
            providerId = "direct-url",
            title = "",
            url = "https://example.invalid/detail",
            summary = null,
            episodes = emptyList(),
        )

        val state = buildDetailHeroChromeUiState(detail, selectedEpisode = null)

        assertEquals("\u672a\u547d\u540d\u6761\u76ee", state.title)
        assertTrue(state.summary.contains("\u81ea\u52a8\u5339\u914d\u64ad\u653e\u6e90"))
        assertEquals(listOf("\u81ea\u52a8\u9009\u96c6", "\u5f85\u9009\u96c6", "\u81ea\u52a8\u5339\u914d"), state.chips.map { it.label })
        assertEquals("direct-url", state.providerId)
        assertEquals(8.dp, state.cornerRadius)
        assertEquals(16.dp, state.contentPadding)
    }

    @Test
    fun detailFirstPlayUiStateSummarizesReadyRecommendation() {
        val routeState = buildRouteUiState(
            selectedEpisode = episode(index = 8),
            routes = listOf(
                route("hls", StreamProtocol.HLS, 900, quality = "1080p", sourceName = "Online"),
                route("backup", StreamProtocol.PROGRESSIVE, 500, quality = "720p", sourceName = "Backup"),
            ),
            loading = false,
            error = null,
        )

        val state = buildDetailFirstPlayUiState(
            selectedEpisode = episode(index = 8),
            routeState = routeState,
        )

        assertEquals("\u5373\u5c06\u64ad\u653e", state.title)
        assertTrue(state.decision.contains("\u7b2c 8 \u96c6"))
        assertTrue(state.decision.contains("Online"))
        assertEquals("\u63a8\u8350\u64ad\u653e", state.actionLabel)
        assertFalse(state.showProgress)
        assertTrue(state.useReadyIcon)
        assertEquals(SourceLibraryTone.Cache, state.tone)
        assertTrue(state.chips.any { it.label == "\u6e05\u6670\u5ea6" && it.value == "1080p" })
        assertTrue(state.chips.any { it.label == "\u53ef\u5207\u6362" })
    }

    @Test
    fun detailFirstPlayUiStateExplainsLoadingAndEmptyStates() {
        val loading = buildDetailFirstPlayUiState(
            selectedEpisode = episode(index = 2),
            routeState = buildRouteUiState(episode(index = 2), emptyList(), loading = true, error = null),
        )
        val empty = buildDetailFirstPlayUiState(
            selectedEpisode = episode(index = 2),
            routeState = buildRouteUiState(episode(index = 2), emptyList(), loading = false, error = null),
        )

        assertEquals("\u5339\u914d\u64ad\u653e\u6e90", loading.title)
        assertTrue(loading.decision.contains("\u4f18\u5148\u5339\u914d"))
        assertEquals("\u81ea\u52a8\u5339\u914d", loading.actionLabel)
        assertTrue(loading.showProgress)
        assertFalse(loading.useReadyIcon)
        assertEquals(SourceLibraryTone.Online, loading.tone)
        assertTrue(loading.chips.any { it.label == "\u52a0\u8f7d" })

        assertEquals("\u7b49\u5f85\u53ef\u7528\u64ad\u653e\u6e90", empty.title)
        assertEquals("\u81ea\u52a8\u5339\u914d", empty.actionLabel)
        assertFalse(empty.showProgress)
        assertEquals(SourceLibraryTone.Backup, empty.tone)
        assertTrue(empty.chips.any { it.label == "\u6e05\u6670\u5ea6" && it.value == "\u5f85\u8865\u6e90" })
    }

    @Test
    fun detailRouteStatusUiStateKeepsReadyCompactCardFocused() {
        val routeState = buildRouteUiState(
            selectedEpisode = episode(index = 8),
            routes = listOf(
                route("hls", StreamProtocol.HLS, 900, quality = "1080p", sourceName = "Online"),
                route("backup", StreamProtocol.PROGRESSIVE, 500, quality = "720p", sourceName = "Backup"),
            ),
            loading = false,
            error = null,
        )

        val state = buildDetailRouteStatusUiState(routeState, expanded = false)

        assertEquals(routeState.message, state.title)
        assertTrue(state.subtitle.contains("\u81ea\u52a8\u6700\u4f73"))
        assertTrue(state.subtitle.contains("1080p"))
        assertEquals("\u5207\u6362", state.actionLabel)
        assertFalse(state.showProgress)
        assertTrue(state.compact)
        assertFalse(state.showRecommendation)
        assertFalse(state.showDiagnostics)
        assertTrue(state.useReadyIcon)
        assertEquals(SourceLibraryTone.Cache, state.tone)
        assertFalse(state.error)
        assertEquals("\u63a8\u8350\u6e90", state.recommendation.label)
        assertEquals("\u64ad\u653e\u63a8\u8350", state.recommendation.actionLabel)
        assertTrue(state.focusChips.any { it.label == "\u6765\u6e90\u8986\u76d6" })
    }

    @Test
    fun detailRouteStatusUiStateExplainsLoadingAndFailedDiagnostics() {
        val loading = buildDetailRouteStatusUiState(
            routeState = buildRouteUiState(episode(index = 2), emptyList(), loading = true, error = null),
            expanded = false,
        )
        val failed = buildDetailRouteStatusUiState(
            routeState = buildRouteUiState(episode(index = 2), emptyList(), loading = false, error = "HTTP 500"),
            expanded = true,
        )

        assertEquals("\u8be6\u60c5", loading.actionLabel)
        assertTrue(loading.showProgress)
        assertFalse(loading.compact)
        assertTrue(loading.showRecommendation)
        assertTrue(loading.showDiagnostics)
        assertFalse(loading.useReadyIcon)
        assertEquals(SourceLibraryTone.Online, loading.tone)
        assertFalse(loading.error)
        assertTrue(loading.focusChips.any { it.label == "\u63a8\u8350\u6e90" && it.value == "\u5339\u914d\u4e2d" })
        assertTrue(loading.metrics.any { it.label == "\u5f02\u5e38" && it.value == "0" && !it.critical })

        assertEquals("\u6536\u8d77", failed.actionLabel)
        assertFalse(failed.showProgress)
        assertTrue(failed.showRecommendation)
        assertTrue(failed.showDiagnostics)
        assertEquals(SourceLibraryTone.Web, failed.tone)
        assertTrue(failed.error)
        assertTrue(failed.focusChips.any { it.label == "\u63a8\u8350\u6e90" && it.value == "\u5931\u8d25" })
        assertTrue(failed.metrics.any { it.label == "\u5f02\u5e38" && it.critical })
    }

    @Test
    fun detailEpisodeSummaryPromotesCurrentEpisodeAndCachedReadyRoute() {
        val state = buildRouteUiState(
            selectedEpisode = episode(index = 3),
            routes = listOf(route("hls", StreamProtocol.HLS, 900, quality = "1080p")),
            loading = false,
            error = null,
            loadedFromCache = true,
        )

        val summary = buildDetailEpisodeSummaryUiState(
            episodeCount = 12,
            selectedEpisode = episode(index = 3),
            routeState = state,
        )

        assertEquals("选集与线路", summary.headline)
        assertEquals("当前第 3 集", summary.currentEpisodeLabel)
        assertEquals("共 12 集", summary.episodeCountLabel)
        assertEquals("已匹配", summary.routeStatusLabel)
        assertEquals("可播放", summary.routeActionLabel)
        assertEquals(SourceLibraryTone.Cache, summary.tone)
        assertTrue(summary.summary.contains("Provider"))
        assertTrue(summary.chips.any { it.label == "预取命中" })
    }

    @Test
    fun detailEpisodeSummaryExplainsNonReadyRouteStates() {
        val loading = buildDetailEpisodeSummaryUiState(
            episodeCount = 12,
            selectedEpisode = episode(index = 2),
            routeState = buildRouteUiState(episode(index = 2), emptyList(), loading = true, error = null),
        )
        val empty = buildDetailEpisodeSummaryUiState(
            episodeCount = 12,
            selectedEpisode = episode(index = 2),
            routeState = buildRouteUiState(episode(index = 2), emptyList(), loading = false, error = null),
        )
        val failed = buildDetailEpisodeSummaryUiState(
            episodeCount = 12,
            selectedEpisode = episode(index = 2),
            routeState = buildRouteUiState(episode(index = 2), emptyList(), loading = false, error = "HTTP 500"),
        )
        val idle = buildDetailEpisodeSummaryUiState(
            episodeCount = 0,
            selectedEpisode = null,
            routeState = buildRouteUiState(null, emptyList(), loading = false, error = null),
        )

        assertEquals("匹配中", loading.routeStatusLabel)
        assertEquals("正在准备", loading.routeActionLabel)
        assertEquals(SourceLibraryTone.Backup, loading.tone)
        assertTrue(loading.summary.contains("优先匹配"))
        assertEquals("待补源", empty.routeStatusLabel)
        assertEquals("换集/稍后", empty.routeActionLabel)
        assertEquals("异常", failed.routeStatusLabel)
        assertEquals("查看异常", failed.routeActionLabel)
        assertTrue(failed.summary.contains("HTTP 500"))
        assertEquals("待载入选集", idle.episodeCountLabel)
        assertEquals("选集后匹配", idle.routeActionLabel)
        assertEquals(SourceLibraryTone.Muted, idle.tone)
    }

    @Test
    fun detailEpisodeOptionUiStateLabelsSelectedAndPendingEpisodes() {
        val selected = buildDetailEpisodeOptionUiState(episode(index = 3), selected = true)
        val pending = buildDetailEpisodeOptionUiState(episode(index = 4), selected = false)

        assertEquals("03", selected.indexLabel)
        assertEquals("\u5df2\u9009", selected.actionLabel)
        assertTrue(selected.subtitle.contains("\u5df2\u5339\u914d\u7ebf\u8def"))
        assertTrue(selected.selected)
        assertEquals(SourceLibraryTone.Online, selected.tone)

        assertEquals("04", pending.indexLabel)
        assertEquals("\u627e\u7ebf\u8def", pending.actionLabel)
        assertTrue(pending.subtitle.contains("\u5728\u7ebf\u64ad\u653e"))
        assertFalse(pending.selected)
        assertEquals(SourceLibraryTone.Muted, pending.tone)
    }

    @Test
    fun detailEpisodeOptionUiStateFallsBackForSpecialEpisode() {
        val special = Episode(
            providerId = "bangumi-catalog",
            id = "sp",
            title = "",
            url = "bangumi://subject/1/episode/sp",
            index = null,
        )

        val state = buildDetailEpisodeOptionUiState(special, selected = false)

        assertEquals("SP", state.indexLabel)
        assertEquals("\u7279\u522b\u7bc7", state.title)
        assertEquals("sp", state.episodeId)
    }

    @Test
    fun detailRouteResolutionUiStateExplainsLoadingPreparation() {
        val state = buildDetailRouteResolutionUiState(
            buildRouteUiState(episode(index = 4), emptyList(), loading = true, error = null),
        )

        assertEquals("\u6b63\u5728\u5339\u914d\u7ebf\u8def", state.title)
        assertTrue(state.subtitle.isNotBlank())
        assertTrue(state.detail.contains("BT/RSS"))
        assertTrue(state.showProgress)
        assertEquals(SourceLibraryTone.Online, state.tone)
        assertEquals(
            listOf("\u5b9e\u65f6\u5339\u914d", "\u5728\u7ebf\u4f18\u5148", "BT \u5907\u7528"),
            state.chips.map { it.label },
        )
    }

    @Test
    fun detailRouteResolutionUiStateExplainsFailedAndEmptyStates() {
        val failed = buildDetailRouteResolutionUiState(
            buildRouteUiState(episode(index = 5), emptyList(), loading = false, error = "HTTP 500"),
        )
        val empty = buildDetailRouteResolutionUiState(
            buildRouteUiState(episode(index = 5), emptyList(), loading = false, error = null),
        )

        assertEquals("\u7ebf\u8def\u52a0\u8f7d\u5931\u8d25", failed.title)
        assertTrue(failed.detail.contains("HTTP 500"))
        assertFalse(failed.showProgress)
        assertEquals(SourceLibraryTone.Web, failed.tone)
        assertTrue(failed.chips.any { it.label == "\u53ef\u91cd\u8bd5" })

        assertEquals("\u6682\u672a\u5339\u914d\u5230\u53ef\u64ad\u653e\u7ebf\u8def", empty.title)
        assertTrue(empty.detail.contains("\u7ebf\u8def\u8bca\u65ad"))
        assertFalse(empty.showProgress)
        assertEquals(SourceLibraryTone.Muted, empty.tone)
        assertEquals("\u5f85\u8865\u6e90", empty.chips.first().label)
    }

    @Test
    fun autoplayRouteSkipsWebViewOnlyEvenWhenItScoresHighest() {
        val webView = route("webview", StreamProtocol.WEBVIEW_ONLY, 8_000, quality = "1080p")
        val playable = route("hls", StreamProtocol.HLS, 100, quality = "720p")

        val route = firstPlayableRouteForAutoplay(listOf(webView, playable))

        assertEquals("hls", route?.stream?.id)
    }

    @Test
    fun autoplayRouteReturnsNullWhenOnlyWebViewOnlyRoutesExist() {
        val webView = route("webview", StreamProtocol.WEBVIEW_ONLY, 8_000, quality = "1080p")

        val route = firstPlayableRouteForAutoplay(listOf(webView))

        assertEquals(null, route)
    }

    @Test
    fun nextPlayableRouteSkipsCurrentFailedAndWebViewOnly() {
        val current = route("current", StreamProtocol.HLS, 900, quality = "1080p")
        val webView = route("webview", StreamProtocol.WEBVIEW_ONLY, 2_000, quality = "1080p")
        val fallback = route("fallback", StreamProtocol.PROGRESSIVE, 300, quality = "720p")

        val next = nextPlayableRoute(
            routes = listOf(current, webView, fallback),
            currentStreamId = "current",
            failedStreamIds = setOf("older-failed"),
        )

        assertEquals("fallback", next?.stream?.id)
    }

    @Test
    fun preferredRouteForNextEpisodeKeepsCurrentSourceWhenPlayable() {
        val globalBest = route(
            id = "global-best",
            protocol = StreamProtocol.HLS,
            score = 1_000,
            quality = "1080p",
            sourceId = "source-a",
            sourceName = "Source A",
        )
        val sameSource = route(
            id = "same-source",
            protocol = StreamProtocol.HLS,
            score = 100,
            quality = "720p",
            sourceId = "source-b",
            sourceName = "Source B",
        )

        val preferred = preferredRouteForNextEpisode(
            routes = listOf(globalBest, sameSource),
            currentSourceId = "source-b",
            currentProviderId = "provider-b",
        )

        assertEquals("same-source", preferred?.stream?.id)
    }

    @Test
    fun preferredRouteForNextEpisodeFallsBackWhenCurrentSourceIsNotPlayable() {
        val webViewOnly = route(
            id = "same-source-webview",
            protocol = StreamProtocol.WEBVIEW_ONLY,
            score = 2_000,
            quality = "1080p",
            sourceId = "source-b",
            sourceName = "Source B",
        )
        val fallback = route(
            id = "fallback",
            protocol = StreamProtocol.HLS,
            score = 300,
            quality = "720p",
            sourceId = "source-a",
            sourceName = "Source A",
        )

        val preferred = preferredRouteForNextEpisode(
            routes = listOf(webViewOnly, fallback),
            currentSourceId = "source-b",
            currentProviderId = "provider-b",
        )

        assertEquals("fallback", preferred?.stream?.id)
    }

    @Test
    fun recommendedSourceIdForRoutesUsesBestPlayableSource() {
        val webViewOnly = route(
            id = "webview",
            protocol = StreamProtocol.WEBVIEW_ONLY,
            score = 3_000,
            quality = "1080p",
            sourceId = "source-web",
            sourceName = "Web",
        )
        val bt = route(
            id = "bt",
            protocol = StreamProtocol.BITTORRENT,
            score = 1_000,
            quality = "1080p",
            sourceId = "source-bt",
            sourceName = "BT",
        )
        val hls = route(
            id = "hls",
            protocol = StreamProtocol.HLS,
            score = 200,
            quality = "720p",
            sourceId = "source-online",
            sourceName = "Online",
        )

        val sourceId = recommendedSourceIdForRoutes(listOf(webViewOnly, bt, hls))

        assertEquals("source-online", sourceId)
    }

    @Test
    fun routeRecommendationReasonExplainsProtocolQualityAndFallbackRole() {
        val hls = route("hls", StreamProtocol.HLS, 400, quality = "1080p")
        val bt = route("bt", StreamProtocol.BITTORRENT, 400, quality = "1080p")
        val webView = route("web", StreamProtocol.WEBVIEW_ONLY, 400, quality = "720p")

        val hlsReason = routeRecommendationReason(hls)
        val btReason = routeRecommendationReason(bt)
        val webReason = routeRecommendationReason(webView)

        assertTrue(hlsReason.contains("\u5728\u7ebf\u64ad\u653e\u4f18\u5148"))
        assertTrue(hlsReason.contains("1080p"))
        assertTrue(btReason.contains("\u5907\u7528"))
        assertTrue(webReason.contains("\u7f51\u9875\u55c5\u63a2"))
    }

    @Test
    fun routePanelUiStateSummarizesRecommendationAndFailures() {
        val failed = route("failed", StreamProtocol.HLS, 900, quality = "1080p")
        val bt = route("bt", StreamProtocol.BITTORRENT, 800, quality = "1080p")
        val fallback = route("fallback", StreamProtocol.HLS, 200, quality = "720p")

        val state = buildRoutePanelUiState(
            routes = listOf(failed, bt, fallback),
            selectedStreamId = "failed",
            failedStreamIds = setOf("failed"),
        )

        assertEquals("fallback", state.recommendedRoute?.stream?.id)
        assertTrue(state.recommendationReason.contains("\u5728\u7ebf\u64ad\u653e\u4f18\u5148"))
        assertTrue(state.recommendationReason.contains("720p"))
        assertEquals("failed", state.selectedRoute?.stream?.id)
        assertEquals(3, state.totalCount)
        assertEquals(2, state.availableCount)
        assertEquals(1, state.onlineCount)
        assertEquals(1, state.btCount)
        assertEquals(1, state.failedCount)
        assertEquals("推荐源 · 可播 2 源", state.compactTitle)
        assertTrue(state.compactSummary.contains("720p"))
        assertEquals("自动推荐 · 共 3 源", state.detailedTitle)
        assertTrue(state.detailedSummary.contains("推荐 Provider"))
        assertEquals("当前 Provider · 1080p", state.selectedRouteSummary)
        assertEquals(listOf("可用", "失败"), state.compactMetrics.map { it.label })
        assertEquals(listOf("可用", "在线", "BT", "失败"), state.detailedMetrics.map { it.label })
        assertEquals(SourceLibraryTone.Cache, state.detailedMetrics.first().tone)
        assertEquals(SourceLibraryTone.Web, state.detailedMetrics.last().tone)
        assertEquals(0.06f, state.containerAlpha)
        assertEquals(0.08f, state.borderAlpha)
        assertEquals(8.dp, state.cornerRadius)
        assertEquals(12.dp, state.contentPadding)
        assertEquals(10.dp, state.contentSpacing)
        assertEquals(10.dp, state.headerSpacing)
        assertEquals(SourceLibraryTone.Online, state.iconTone)
        assertEquals(20.dp, state.iconSize)
        assertEquals(2.dp, state.textSpacing)
        assertEquals(SourceLibraryTone.Muted, state.summaryTone)
        assertEquals(0.62f, state.selectedRouteSummaryAlpha)
        assertEquals("详细", state.expandToggleLabel)
        assertEquals("简单", state.collapseToggleLabel)
        assertEquals(58.dp, state.toggleWidth)
        assertEquals(32.dp, state.toggleHeight)
        assertEquals(8.dp, state.toggleCornerRadius)
        assertEquals(SourceLibraryTone.Online, state.toggleTone)
        assertEquals(0.16f, state.toggleActiveContainerAlpha)
        assertEquals(0.08f, state.toggleInactiveContainerAlpha)
        assertEquals(0.74f, state.toggleInactiveContentAlpha)
        assertEquals(8.dp, state.metricSpacing)
        assertEquals(SourceLibraryTone.Backup, state.noticeTone)
        assertEquals(2, state.noticeMaxLines)
        val firstMetric = state.detailedMetrics.first()
        assertEquals(30.dp, firstMetric.height)
        assertEquals(8.dp, firstMetric.cornerRadius)
        assertEquals(0.28f, firstMetric.containerAlpha)
        assertEquals(9.dp, firstMetric.horizontalPadding)
        assertEquals(4.dp, firstMetric.spacing)
        assertEquals(0.7f, firstMetric.labelAlpha)
    }

    @Test
    fun routeCandidateUiStateSummarizesRecommendedOnlineRoute() {
        val route = route(
            id = "hls",
            protocol = StreamProtocol.HLS,
            score = 900,
            quality = "1080p",
            sourceId = "online",
            sourceName = "Online",
            sizeBytes = 1_572_864L,
        )

        val state = buildRouteCandidateUiState(route, recommended = true)

        assertEquals("hls", state.streamId)
        assertEquals("Online", state.sourceName)
        assertEquals("O", state.sourceInitial)
        assertEquals("1080p", state.primaryLabel)
        assertEquals("HLS", state.protocolLabel)
        assertEquals("1.5 MB", state.sizeLabel)
        assertEquals("在线可播", state.statusLabel)
        assertEquals("推荐播放", state.actionLabel)
        assertEquals(listOf("推荐", "在线可播"), state.badges.map { it.label })
        assertEquals(listOf(SourceLibraryTone.Primary, SourceLibraryTone.Cache), state.badges.map { it.tone })
        assertFalse(state.failed)
        assertTrue(state.enabled)
        assertTrue(state.prominent)
        assertTrue(state.highlighted)
        assertEquals(0.08f, state.containerAlpha)
        assertEquals(0.035f, state.disabledContainerAlpha)
        assertEquals(0.85f, state.borderAlpha)
        assertEquals(SourceLibraryTone.Primary, state.borderTone)
        assertEquals(10.dp, state.rowPadding)
        assertEquals(10.dp, state.rowSpacing)
        assertEquals(8.dp, state.rowCornerRadius)
        assertEquals(4.dp, state.railWidth)
        assertEquals(46.dp, state.compactRailHeight)
        assertEquals(58.dp, state.detailedRailHeight)
        assertEquals(5.dp, state.textColumnSpacing)
        assertEquals(6.dp, state.titleBadgeSpacing)
        assertEquals(5.dp, state.trailingSpacing)
        assertEquals(SourceLibraryTone.Muted, state.compactSubtitleTone)
        assertEquals(SourceLibraryTone.Online, state.detailedSubtitleTone)
        assertEquals(30.dp, state.actionLabelHeight)
        assertEquals(8.dp, state.actionLabelCornerRadius)
        assertEquals(8.dp, state.actionLabelHorizontalPadding)
        assertEquals(4.dp, state.actionLabelSpacing)
        assertEquals(13.dp, state.actionLabelIconSize)
        assertEquals(0.13f, state.actionLabelContainerAlpha)
        assertEquals("1080p", state.compactTitle)
        assertEquals("Online", state.compactSubtitle)
        assertEquals("Online", state.detailedTitle)
        assertEquals("1080p", state.detailedSubtitle)
        assertTrue(state.detailLine.contains("HLS"))
        assertFalse(state.selected)
        assertTrue(state.recommended)
        assertTrue(state.playable)
        assertEquals("可离线", state.cacheLabel)
        assertEquals(SourceLibraryTone.Primary, state.accentTone)
        assertEquals(SourceLibraryTone.Cache, state.statusTone)
    }

    @Test
    fun routeCandidateUiStateMarksCurrentPlaybackRoute() {
        val state = buildRouteCandidateUiState(
            route("hls", StreamProtocol.HLS, 900, quality = "720p"),
            selected = true,
            recommended = true,
        )

        assertEquals("当前", state.statusLabel)
        assertEquals("播放中", state.actionLabel)
        assertEquals(listOf("推荐", "当前"), state.badges.map { it.label })
        assertTrue(state.selected)
        assertTrue(state.recommended)
        assertTrue(state.enabled)
        assertTrue(state.prominent)
        assertTrue(state.highlighted)
        assertEquals(SourceLibraryTone.Online, state.accentTone)
        assertEquals(SourceLibraryTone.Online, state.actionTone)
        assertEquals(SourceLibraryTone.Online, state.borderTone)
    }

    @Test
    fun routeCandidateUiStateExplainsFallbackWebAndFailedRoutes() {
        val bt = buildRouteCandidateUiState(route("bt", StreamProtocol.BITTORRENT, 800, quality = "1080p"))
        val web = buildRouteCandidateUiState(route("web", StreamProtocol.WEBVIEW_ONLY, 800, quality = "1080p"))
        val failed = buildRouteCandidateUiState(
            route("failed", StreamProtocol.HLS, 900, quality = "1080p"),
            failed = true,
        )

        assertEquals("备用源", bt.statusLabel)
        assertEquals("边下边播", bt.actionLabel)
        assertEquals("边下边播", bt.cacheLabel)
        assertTrue(bt.playable)
        assertEquals(SourceLibraryTone.Backup, bt.accentTone)
        assertEquals("仅网页", web.statusLabel)
        assertEquals("网页兜底", web.actionLabel)
        assertEquals("嗅探", web.cacheLabel)
        assertFalse(web.playable)
        assertFalse(web.enabled)
        assertFalse(web.highlighted)
        assertEquals(0.045f, web.containerAlpha)
        assertEquals(0.08f, web.borderAlpha)
        assertNull(web.borderTone)
        assertEquals(SourceLibraryTone.Muted, web.accentTone)
        assertEquals("播放失败", failed.statusLabel)
        assertEquals("重试", failed.actionLabel)
        assertEquals(listOf("播放失败"), failed.badges.map { it.label })
        assertEquals(SourceLibraryTone.Web, failed.badges.single().tone)
        assertTrue(failed.failed)
        assertTrue(failed.enabled)
        assertFalse(failed.prominent)
        assertTrue(failed.highlighted)
        assertFalse(failed.playable)
        assertEquals(0.045f, failed.containerAlpha)
        assertEquals(0.85f, failed.borderAlpha)
        assertEquals(SourceLibraryTone.Web, failed.borderTone)
        assertEquals(SourceLibraryTone.Web, failed.accentTone)
    }

    @Test
    fun routePanelVisibleRoutesPrioritizePlayableRoutesBeforeFallbacks() {
        val webView = route("web", StreamProtocol.WEBVIEW_ONLY, 8_000, quality = "1080p")
        val failed = route("failed", StreamProtocol.HLS, 8_000, quality = "1080p")
        val duplicateLowerQuality = route("hls", StreamProtocol.PROGRESSIVE, 1, quality = "480p")
        val playable = route("hls", StreamProtocol.HLS, 100, quality = "720p")

        val routes = routePanelVisibleRoutes(
            routes = listOf(webView, failed, duplicateLowerQuality, playable),
            failedStreamIds = setOf("failed"),
        )

        assertEquals(listOf("hls", "failed", "web"), routes.map { it.stream.id })
        assertEquals(StreamProtocol.HLS, routes.first().protocol)
    }

    @Test
    fun routePanelVisibleRoutesFilterSourceBeforeDeduplicating() {
        val sourceA = route(
            id = "shared",
            protocol = StreamProtocol.HLS,
            score = 900,
            quality = "1080p",
            sourceId = "source-a",
            sourceName = "Source A",
        )
        val sourceB = route(
            id = "shared",
            protocol = StreamProtocol.HLS,
            score = 100,
            quality = "720p",
            sourceId = "source-b",
            sourceName = "Source B",
        )

        val routes = routePanelVisibleRoutes(
            routes = listOf(sourceA, sourceB),
            selectedSourceId = "source-b",
        )

        assertEquals(1, routes.size)
        assertEquals("source-b", routes.first().sourceId)
    }

    @Test
    fun routeSourceGroupsPromoteSelectedThenRecommendedSource() {
        val recommended = route(
            id = "recommended",
            protocol = StreamProtocol.HLS,
            score = 900,
            quality = "1080p",
            sourceId = "source-a",
            sourceName = "Source A",
        )
        val selected = route(
            id = "selected",
            protocol = StreamProtocol.PROGRESSIVE,
            score = 200,
            quality = "720p",
            sourceId = "source-b",
            sourceName = "Source B",
        )

        val groups = buildRouteSourceGroups(
            routes = listOf(recommended, selected),
            selectedSourceId = "source-b",
            recommendedSourceId = "source-a",
        )

        assertEquals("source-b", groups[0].id)
        assertTrue(groups[0].isFilterSelected)
        assertEquals("已选", groups[0].statusLabel)
        assertEquals("当前方案", groups[0].footerLabel)
        assertEquals(SourceLibraryTone.Online, groups[0].tone)
        assertEquals("source-a", groups[1].id)
        assertTrue(groups[1].hasRecommended)
        assertEquals("推荐", groups[1].statusLabel)
        assertEquals("自动推荐", groups[1].footerLabel)
        assertEquals(SourceLibraryTone.Primary, groups[1].tone)
    }

    @Test
    fun routeSourceGroupsSummarizeAllSourcesAndFailedRoutes() {
        val failedOnline = route("failed-hls", StreamProtocol.HLS, 900, quality = "1080p", sourceId = "online", sourceName = "Online")
        val online = route("ok-hls", StreamProtocol.HLS, 800, quality = "720p", sourceId = "online", sourceName = "Online")
        val bt = route("bt", StreamProtocol.BITTORRENT, 500, quality = "1080p", sourceId = "bt", sourceName = "BT")
        val webOnly = route("web", StreamProtocol.WEBVIEW_ONLY, 100, quality = "1080p", sourceId = "web", sourceName = "Web")

        val groups = buildRouteSourceGroups(
            routes = listOf(failedOnline, online, bt, webOnly),
            selectedStreamId = "ok-hls",
            recommendedStreamId = "ok-hls",
            failedStreamIds = setOf("failed-hls"),
            includeAll = true,
        )

        val all = groups.first()
        assertEquals(RouteAllSourcesId, all.id)
        assertTrue(all.isAll)
        assertTrue(all.isFilterSelected)
        assertEquals(4, all.totalCount)
        assertEquals(2, all.playableCount)
        assertEquals(1, all.onlineCount)
        assertEquals(1, all.btCount)
        assertEquals(1, all.webOnlyCount)
        assertEquals(1, all.failedCount)
        assertEquals("全部", all.statusLabel)
        assertEquals("全部来源", all.footerLabel)
        assertEquals("2/4 可播 · 1 在线 · 1 BT", all.detailSummary)
        assertEquals("4线", all.routeCountLabel)

        val onlineGroup = groups.first { it.id == "online" }
        assertTrue(onlineGroup.hasSelected)
        assertTrue(onlineGroup.hasRecommended)
        assertEquals("1可播 · 1在线", onlineGroup.sourceSummary)
        assertEquals("当前", onlineGroup.statusLabel)
        assertEquals("正在播放", onlineGroup.footerLabel)
        assertEquals(SourceLibraryTone.Online, onlineGroup.tone)
    }

    @Test
    fun playerRouteSourceStripUiStateBuildsListTitlesAndChipPresentation() {
        val recommended = route(
            id = "recommended",
            protocol = StreamProtocol.HLS,
            score = 900,
            quality = "1080p",
            sourceId = "source-a",
            sourceName = "Source A",
        )
        val selected = route(
            id = "selected",
            protocol = StreamProtocol.PROGRESSIVE,
            score = 200,
            quality = "720p",
            sourceId = "source-b",
            sourceName = "Source B",
        )
        val failedOnly = route(
            id = "failed",
            protocol = StreamProtocol.HLS,
            score = 100,
            quality = "720p",
            sourceId = "source-c",
            sourceName = "Source C",
        )

        val singleSource = buildPlayerRouteSourceStripUiState(
            routes = listOf(recommended),
            selectedSourceId = null,
            selectedStreamId = recommended.stream.id,
            recommendedStreamId = recommended.stream.id,
            detailedMode = false,
        )
        val compact = buildPlayerRouteSourceStripUiState(
            routes = listOf(recommended, selected),
            selectedSourceId = null,
            selectedStreamId = selected.stream.id,
            recommendedStreamId = recommended.stream.id,
            detailedMode = false,
        )
        val detailed = buildPlayerRouteSourceStripUiState(
            routes = listOf(recommended, selected, failedOnly),
            selectedSourceId = selected.sourceId,
            selectedStreamId = selected.stream.id,
            recommendedStreamId = recommended.stream.id,
            failedStreamIds = setOf(failedOnly.stream.id),
            detailedMode = true,
        )

        assertFalse(singleSource.visible)
        assertTrue(singleSource.chips.isEmpty())
        assertEquals("\u63a8\u8350\u6e90 (1)", singleSource.routeListTitle)
        assertEquals(listOf("recommended"), singleSource.visibleRoutes.map { it.stream.id })
        assertTrue(compact.visible)
        assertEquals("\u64ad\u653e\u6e90\u5206\u7ec4", compact.title)
        assertEquals("\u63a8\u8350\u6e90 (2)", compact.routeListTitle)
        assertEquals("\u5168\u90e8\u64ad\u653e\u6e90", compact.selectedSourceName)
        assertEquals(listOf("recommended", "selected"), compact.visibleRoutes.map { it.stream.id })
        assertEquals(3, compact.chips.size)
        assertEquals(RouteAllSourcesId, compact.chips.first().group.id)
        assertEquals(132.dp, compact.chips.first().width)
        assertEquals(46.dp, compact.chips.first().height)
        assertFalse(compact.chips.first().detailVisible)
        assertTrue(compact.chips.first().emphasized)
        assertEquals(0.08f, compact.chips.first().containerAlpha)
        assertEquals(0.85f, compact.chips.first().borderAlpha)
        assertEquals(0.72f, compact.titleAlpha)
        assertEquals(7.dp, compact.containerSpacing)
        assertEquals(8.dp, compact.chipSpacing)
        assertEquals("\u6309\u6765\u6e90\u7b5b\u9009", detailed.title)
        assertEquals("\u5df2\u7b5b\u9009\u6765\u6e90 \u00b7 Source B (1)", detailed.routeListTitle)
        assertEquals("Source B", detailed.selectedSourceName)
        assertEquals(listOf("selected"), detailed.visibleRoutes.map { it.stream.id })
        val selectedChip = detailed.chips.first { it.group.id == selected.sourceId }
        assertEquals(152.dp, selectedChip.width)
        assertEquals(74.dp, selectedChip.height)
        assertTrue(selectedChip.detailVisible)
        assertEquals(10.dp, selectedChip.contentPadding)
        assertEquals(0.66f, selectedChip.detailAlpha)
        assertTrue(selectedChip.emphasized)
        assertFalse(selectedChip.footerError)
        val failedChip = detailed.chips.first { it.group.id == failedOnly.sourceId }
        assertFalse(failedChip.emphasized)
        assertEquals(0.045f, failedChip.containerAlpha)
        assertEquals(0.34f, failedChip.borderAlpha)
        assertTrue(failedChip.footerError)
    }

    @Test
    fun detailRouteSourceSelectorUiStateSummarizesAutoChoice() {
        val online = route("ok-hls", StreamProtocol.HLS, 800, quality = "720p", sourceId = "online", sourceName = "Online")
        val bt = route("bt", StreamProtocol.BITTORRENT, 500, quality = "1080p", sourceId = "bt", sourceName = "BT")

        val state = buildDetailRouteSourceSelectorUiState(
            routes = listOf(online, bt),
            selectedSourceId = null,
            recommendedSourceId = "online",
        )

        assertEquals("播放方案", state.title)
        assertEquals("推荐", state.recommended.label)
        assertEquals("Online", state.recommended.value)
        assertEquals(SourceLibraryTone.Primary, state.recommended.tone)
        assertEquals("当前", state.current.label)
        assertEquals("自动最佳", state.current.value)
        assertTrue(state.autoChoice.selected)
        assertEquals("使用中", state.autoChoice.actionLabel)
        assertEquals(SourceLibraryTone.Online, state.autoChoice.tone)
        assertTrue(state.autoChoice.subtitle.contains("Online"))
        assertTrue(state.autoChoice.badges.any { it.label == "推荐入口" })
        assertTrue(state.autoChoice.badges.any { it.label == "当前" })
        assertTrue(state.autoChoice.badges.any { it.label == "1 在线" })
        assertTrue(state.autoChoice.badges.any { it.label == "1 备用" })
        assertEquals(listOf("online", "bt"), state.groups.map { it.id })
    }

    @Test
    fun detailRouteSourceSelectorUiStateTracksManualSourceSelection() {
        val recommended = route("recommended", StreamProtocol.HLS, 900, quality = "1080p", sourceId = "source-a", sourceName = "Source A")
        val selected = route("selected", StreamProtocol.PROGRESSIVE, 200, quality = "720p", sourceId = "source-b", sourceName = "Source B")

        val state = buildDetailRouteSourceSelectorUiState(
            routes = listOf(recommended, selected),
            selectedSourceId = "source-b",
            recommendedSourceId = "source-a",
        )

        assertEquals("Source A", state.recommended.value)
        assertEquals("Source B", state.current.value)
        assertFalse(state.autoChoice.selected)
        assertEquals("使用", state.autoChoice.actionLabel)
        assertEquals(SourceLibraryTone.Primary, state.autoChoice.tone)
        assertFalse(state.autoChoice.badges.any { it.label == "当前" })
        assertEquals("source-b", state.groups.first().id)
        assertTrue(state.groups.first().isFilterSelected)
    }

    @Test
    fun routePrefetchWindowPrioritizesFutureEpisodesBeforePrevious() {
        val episodes = (1..5).map { episode(id = "ep-$it", index = it) }

        val middle = routePrefetchWindow(episodes, episodes[2], maxCount = 3)
        val first = routePrefetchWindow(episodes, episodes[0], maxCount = 2)
        val last = routePrefetchWindow(episodes, episodes[4], maxCount = 2)

        assertEquals(listOf("ep-4", "ep-5", "ep-2"), middle.map { it.id })
        assertEquals(listOf("ep-2", "ep-3"), first.map { it.id })
        assertEquals(listOf("ep-4", "ep-3"), last.map { it.id })
    }

    @Test
    fun routePrefetchUiStateTracksWarmingReadyAndEmptyEpisodes() {
        val episodes = (1..4).map { episode(id = "ep-$it", index = it) }

        val state = buildRoutePrefetchUiState(
            episodes = episodes,
            currentEpisode = episodes[1],
            warmingEpisodeIds = setOf("ep-3"),
            warmedEpisodeIds = setOf("ep-4"),
            emptyEpisodeIds = setOf("ep-1"),
            maxCount = 3,
        )

        assertEquals(listOf("ep-3", "ep-4", "ep-1"), state.items.map { it.episodeId })
        assertEquals(RoutePrefetchStatus.Warming, state.items[0].status)
        assertEquals(RoutePrefetchStatus.Ready, state.items[1].status)
        assertEquals(RoutePrefetchStatus.Empty, state.items[2].status)
        assertTrue(state.hasActivePrefetch)
        assertTrue(state.showProgress)
        assertEquals("\u9884\u70ed", state.badgeLabel)
        assertEquals(SourceLibraryTone.Online, state.tone)
        assertEquals("\u9884\u70ed\u4e2d", state.items[0].statusLabel)
        assertEquals(SourceLibraryTone.Online, state.items[0].tone)
        assertEquals("\u5df2\u547d\u4e2d", state.items[1].statusLabel)
        assertEquals(SourceLibraryTone.Cache, state.items[1].tone)
        assertEquals("\u5f85\u8865\u6e90", state.items[2].statusLabel)
        assertEquals(SourceLibraryTone.Backup, state.items[2].tone)
        assertTrue(state.headline.contains("\u9884\u70ed"))
    }

    @Test
    fun routePrefetchUiStateReportsAllReadyAndNoNeighborCases() {
        val episodes = listOf(episode(id = "ep-1", index = 1), episode(id = "ep-2", index = 2))

        val ready = buildRoutePrefetchUiState(
            episodes = episodes,
            currentEpisode = episodes[0],
            warmedEpisodeIds = setOf("ep-2"),
        )
        val none = buildRoutePrefetchUiState(
            episodes = listOf(episodes[0]),
            currentEpisode = episodes[0],
        )

        assertEquals(RoutePrefetchStatus.Ready, ready.items.single().status)
        assertFalse(ready.hasActivePrefetch)
        assertFalse(ready.showProgress)
        assertEquals(SourceLibraryTone.Cache, ready.tone)
        assertEquals("\u5df2\u547d\u4e2d", ready.items.single().statusLabel)
        assertTrue(ready.summary.contains("\u7f13\u5b58\u7ebf\u8def"))
        assertTrue(none.items.isEmpty())
        assertEquals(SourceLibraryTone.Muted, none.tone)
        assertTrue(none.headline.contains("\u65e0\u90bb\u96c6"))
    }

    @Test
    fun searchLandingUiStateUsesScheduleItemsBeforeFallbackKeywords() {
        val scheduleState = buildHomeScheduleUiState(
            schedule = listOf(
                scheduleDay(
                    2,
                    "\u661f\u671f\u4e8c",
                    listOf(
                        searchResult(
                            providerId = "bangumi-catalog",
                            title = "Alpha Original",
                            raw = mapOf("subjectNameCn" to "Alpha CN"),
                        ),
                        searchResult(providerId = "bangumi-catalog", title = "Beta"),
                    ),
                ),
            ),
            selectedDayId = 2,
            currentDayId = 2,
        )

        val state = buildSearchLandingUiState(
            scheduleState = scheduleState,
            searchableSourceCount = 3,
            fallbackKeywords = listOf("Alpha CN", "Gamma"),
        )

        assertEquals("\u627e\u756a", state.headline)
        assertEquals("\u65e5\u7a0b\u53ef\u641c", state.suggestionTitle)
        assertEquals(2, state.scheduleSuggestionCount)
        assertEquals(3, state.searchableSourceCount)
        assertEquals(listOf("Alpha CN", "Beta", "Gamma"), state.suggestions.map { it.keyword })
        assertEquals(SourceLibraryTone.Primary, state.suggestions.first().tone)
        assertEquals(SourceLibraryTone.Online, state.suggestions.last().tone)
        assertTrue(state.summary.contains("\u661f\u671f\u4e8c"))
        assertTrue(state.inputSubtitle.contains("3 \u4e2a\u7d22\u5f15\u6e90"))
    }

    @Test
    fun searchLandingUiStateFallsBackWhenScheduleIsEmptyOrSourcesMissing() {
        val emptySchedule = buildHomeScheduleUiState(
            schedule = emptyList(),
            selectedDayId = 1,
            currentDayId = 1,
        )

        val state = buildSearchLandingUiState(
            scheduleState = emptySchedule,
            searchableSourceCount = 0,
            fallbackKeywords = listOf("", "Gamma", "Gamma", "Delta"),
        )

        assertEquals("\u5927\u5bb6\u5728\u627e", state.suggestionTitle)
        assertEquals(0, state.scheduleSuggestionCount)
        assertEquals(0, state.searchableSourceCount)
        assertEquals(listOf("Gamma", "Delta"), state.suggestions.map { it.keyword })
        assertTrue(state.summary.contains("\u641c\u7d22\u6e90\u5f85\u63a5\u5165"))
        assertEquals("\u7b49\u5f85\u53ef\u641c\u7d22\u6765\u6e90", state.inputSubtitle)
    }

    @Test
    fun searchIdleHintUiStateExplainsScheduleSuggestionsAndIndexHealth() {
        val scheduleState = buildHomeScheduleUiState(
            schedule = listOf(
                scheduleDay(
                    2,
                    "\u661f\u671f\u4e8c",
                    listOf(
                        searchResult(providerId = "bangumi-catalog", title = "Alpha"),
                        searchResult(providerId = "bangumi-catalog", title = "Beta"),
                    ),
                ),
            ),
            selectedDayId = 2,
            currentDayId = 2,
        )
        val landingState = buildSearchLandingUiState(
            scheduleState = scheduleState,
            searchableSourceCount = 3,
            fallbackKeywords = listOf("Gamma"),
        )
        val indexState = buildSearchIndexUiState(
            manifests = listOf(
                manifest("bangumi-catalog", "Bangumi"),
                manifest("bt", "BT Source", setOf(SourceCapability.SEARCH, SourceCapability.BITTORRENT)),
                manifest("direct-url", "Direct", setOf(SourceCapability.SEARCH, SourceCapability.STREAM)),
            ),
            report = null,
            results = emptyList(),
        )

        val state = buildSearchIdleHintUiState(landingState, indexState)

        assertEquals("\u5148\u4ece\u65e5\u7a0b\u5feb\u901f\u627e\u756a", state.title)
        assertTrue(state.subtitle.contains("2 \u4e2a\u5165\u53e3"))
        assertEquals("\u70b9\u65e5\u7a0b\u8bcd\u6216\u8f93\u5165\u756a\u540d", state.actionLabel)
        assertEquals(
            listOf("3 \u6e90\u53ef\u641c", "2 \u4e2a\u65e5\u7a0b\u5efa\u8bae", "\u7d22\u5f15\u6b63\u5e38"),
            state.chips.map { it.label },
        )
        assertEquals(SourceLibraryTone.Online, state.chips[0].tone)
        assertEquals(SourceLibraryTone.Primary, state.chips[1].tone)
        assertEquals(SourceLibraryTone.Cache, state.chips[2].tone)
    }

    @Test
    fun searchIdleHintUiStateFallsBackWhenNoSearchSourceIsAvailable() {
        val emptySchedule = buildHomeScheduleUiState(
            schedule = emptyList(),
            selectedDayId = 1,
            currentDayId = 1,
        )
        val landingState = buildSearchLandingUiState(
            scheduleState = emptySchedule,
            searchableSourceCount = 0,
            fallbackKeywords = listOf("Gamma"),
        )
        val indexState = buildSearchIndexUiState(
            manifests = emptyList(),
            report = null,
            results = emptyList(),
        )

        val state = buildSearchIdleHintUiState(landingState, indexState)

        assertEquals("\u641c\u7d22\u6e90\u5f85\u63a5\u5165", state.title)
        assertTrue(state.subtitle.contains("\u6765\u6e90\u7b5b\u9009"))
        assertEquals("\u5148\u63a5\u5165\u641c\u7d22\u6765\u6e90", state.actionLabel)
        assertEquals(
            listOf("\u5f85\u63a5\u641c\u7d22\u6e90", "\u70ed\u95e8\u8bcd\u515c\u5e95", "\u6765\u6e90\u5f85\u914d\u7f6e"),
            state.chips.map { it.label },
        )
        assertTrue(state.chips.all { it.tone == SourceLibraryTone.Muted || it.tone == SourceLibraryTone.Backup })
    }

    @Test
    fun searchResultCardUiStateSummarizesBangumiMetadata() {
        val state = buildSearchResultCardUiState(
            SearchResult(
                providerId = "bangumi-catalog",
                title = "Alpha",
                url = "bangumi://subject/1",
                subtitle = "Bangumi 资料库 / 2024 / TV",
                raw = mapOf(
                    "rating" to "8.7",
                    "episodeCount" to "12",
                    "categoryTitle" to "日本动画",
                    "doing" to "13200",
                ),
            ),
        )

        assertEquals("Alpha", state.title)
        assertEquals("Bangumi 资料库", state.providerLabel)
        assertEquals("资料库", state.typeLabel)
        assertEquals("进详情", state.actionLabel)
        assertEquals(SourceLibraryTone.Online, state.tone)
        assertEquals(listOf("评分 8.7", "12 集", "日本动画"), state.chips.map { it.label })
        assertEquals(SourceLibraryTone.Primary, state.chips.first().tone)
    }

    @Test
    fun searchResultCardUiStateExplainsDirectAndBtFallbackKinds() {
        val direct = buildSearchResultCardUiState(searchResult(providerId = "direct-url", title = "Direct"))
        val bt = buildSearchResultCardUiState(searchResult(providerId = "mikan", title = "BT"))

        assertEquals("在线链接", direct.providerLabel)
        assertEquals("直链", direct.typeLabel)
        assertEquals("确认线路", direct.actionLabel)
        assertEquals(SourceLibraryTone.Primary, direct.tone)
        assertEquals(listOf("直链"), direct.chips.map { it.label })

        assertEquals("番剧频道", bt.providerLabel)
        assertEquals("BT/RSS", bt.typeLabel)
        assertEquals("看资源", bt.actionLabel)
        assertEquals(SourceLibraryTone.Backup, bt.tone)
        assertEquals(listOf("BT/RSS"), bt.chips.map { it.label })
    }

    @Test
    fun detailEntryUiStateSummarizesLoadedDetailAndEpisodes() {
        val result = SearchResult(
            providerId = "bangumi-catalog",
            title = "Alpha Result",
            url = "bangumi://subject/1",
            raw = mapOf("rating" to "8.7"),
        )
        val detail = MediaDetail(
            providerId = "bangumi-catalog",
            title = "Alpha Detail",
            url = "bangumi://subject/1",
            episodes = listOf(episode(id = "ep-1", index = 1), episode(id = "ep-2", index = 2)),
        )

        val state = buildDetailEntryUiState(
            result = result,
            detail = detail,
            loading = false,
            error = null,
        )

        assertEquals("Alpha Detail", state.headline)
        assertEquals("Bangumi 资料库", state.providerLabel)
        assertEquals("资料库", state.typeLabel)
        assertEquals("详情已就绪", state.detailStatusLabel)
        assertEquals("2 集", state.episodeLabel)
        assertEquals("准备播放", state.actionLabel)
        assertEquals(SourceLibraryTone.Online, state.tone)
        assertTrue(state.summary.contains("2 集"))
        assertTrue(state.chips.any { it.label == "详情已就绪" })
        assertTrue(state.chips.any { it.label == "评分 8.7" })
    }

    @Test
    fun detailEntryUiStateExplainsLoadingAndErrorStates() {
        val result = searchResult(providerId = "direct-url", title = "Direct", raw = mapOf("episodeCount" to "1"))

        val loading = buildDetailEntryUiState(
            result = result,
            detail = null,
            loading = true,
            error = null,
        )
        val failed = buildDetailEntryUiState(
            result = result,
            detail = null,
            loading = false,
            error = "HTTP 500",
        )

        assertEquals("详情加载中", loading.detailStatusLabel)
        assertEquals("读取中", loading.actionLabel)
        assertEquals("1 集", loading.episodeLabel)
        assertTrue(loading.summary.contains("正在读取"))
        assertEquals("详情异常", failed.detailStatusLabel)
        assertEquals("可重试", failed.actionLabel)
        assertTrue(failed.summary.contains("HTTP 500"))
        assertTrue(failed.chips.any { it.label == "详情异常" })
    }

    @Test
    fun detailEntryUiStateExposesCardChromeAndStatusTone() {
        val result = searchResult(providerId = "bangumi-catalog", title = "Alpha")
        val ready = buildDetailEntryUiState(
            result = result,
            detail = MediaDetail(
                providerId = "bangumi-catalog",
                title = "Alpha",
                url = "bangumi://subject/1",
                episodes = listOf(episode(index = 1)),
            ),
            loading = false,
            error = null,
        )
        val loading = buildDetailEntryUiState(result, detail = null, loading = true, error = null)
        val failed = buildDetailEntryUiState(result, detail = null, loading = false, error = "HTTP 500")

        assertEquals(SourceLibraryTone.Cache, ready.statusTone)
        assertEquals(SourceLibraryTone.Backup, loading.statusTone)
        assertEquals(SourceLibraryTone.Web, failed.statusTone)
        assertEquals(8.dp, ready.cardCornerRadius)
        assertEquals(14.dp, ready.cardPadding)
        assertEquals(12.dp, ready.rowSpacing)
        assertEquals(42.dp, ready.iconBoxSize)
        assertEquals(22.dp, ready.iconSize)
        assertEquals(8.dp, ready.iconCornerRadius)
        assertEquals(0.18f, ready.iconContainerAlpha, 0.001f)
        assertEquals(6.dp, ready.contentSpacing)
        assertEquals(8.dp, ready.titleRowSpacing)
        assertEquals(6.dp, ready.chipSpacing)
        assertEquals(104.dp, ready.sideMaxWidth)
        assertEquals(4.dp, ready.sideSpacing)
    }

    @Test
    fun searchIndexUiStateSummarizesSourcesResultsAndFailures() {
        val manifests = listOf(
            manifest("bangumi-catalog", "Bangumi", setOf(SourceCapability.SEARCH, SourceCapability.DETAIL, SourceCapability.EPISODES)),
            manifest("bt", "BT Source", setOf(SourceCapability.SEARCH, SourceCapability.STREAM, SourceCapability.BITTORRENT)),
            manifest("direct-url", "Direct", setOf(SourceCapability.SEARCH, SourceCapability.STREAM)),
        )
        val results = listOf(
            searchResult("bangumi-catalog", "Alpha"),
            searchResult("bangumi-catalog", "Beta"),
            searchResult("bt", "Gamma"),
        )
        val report = SourceSearchReport(
            results = results,
            failures = listOf(SourceSearchFailure("direct-url", "Direct", "timeout")),
        )

        val state = buildSearchIndexUiState(
            manifests = manifests,
            report = report,
            results = results,
        )

        assertEquals(3, state.resultCount)
        assertEquals(3, state.searchableSourceCount)
        assertEquals(1, state.failedSourceCount)
        assertEquals(SearchAllSourcesId, state.sourceFilters.first().id)
        assertTrue(state.sourceFilters.first().selected)
        assertTrue(state.headline.contains("\u547d\u4e2d 3"))
        assertTrue(state.headline.contains("\u6e90\u5f02\u5e38"))

        val bangumi = state.sourceFilters.first { it.id == "bangumi-catalog" }
        val bt = state.sourceFilters.first { it.id == "bt" }
        val direct = state.sourceFilters.first { it.id == "direct-url" }

        assertEquals(2, bangumi.resultCount)
        assertTrue(bt.capabilityLabel.contains("BT"))
        assertTrue(direct.failed)
        assertEquals("\u5f02\u5e38", direct.statusLabel)
    }

    @Test
    fun searchIndexUiStateTracksSelectedSourceAndFiltersResults() {
        val manifests = listOf(
            manifest("bangumi-catalog", "Bangumi"),
            manifest("bt", "BT Source", setOf(SourceCapability.SEARCH, SourceCapability.BITTORRENT)),
        )
        val results = listOf(
            searchResult("bangumi-catalog", "Alpha"),
            searchResult("bt", "Beta"),
            searchResult("bt", "Gamma"),
        )
        val report = SourceSearchReport(results = results, failures = emptyList())

        val state = buildSearchIndexUiState(
            manifests = manifests,
            report = report,
            results = results,
            selectedProviderId = "bt",
        )
        val filtered = searchResultsForProvider(results, state.selectedProviderId)

        assertEquals("bt", state.selectedProviderId)
        assertFalse(state.sourceFilters.first { it.id == SearchAllSourcesId }.selected)
        assertTrue(state.sourceFilters.first { it.id == "bt" }.selected)
        assertTrue(state.summary.contains("BT Source"))
        assertEquals(listOf("Beta", "Gamma"), filtered.map { it.title })
        assertEquals(results, searchResultsForProvider(results, null))
        assertTrue(searchResultsForProvider(results, "missing").isEmpty())
    }

    @Test
    fun searchResultsForProviderDeduplicatesAllSourcesButKeepsSourceFiltersRaw() {
        val results = listOf(
            searchResult("animeko-online", "Alpha!!", raw = mapOf("mediaKind" to "online")),
            searchResult(
                "bangumi-catalog",
                "Alpha",
                raw = mapOf("subjectId" to "1", "rating" to "8.2", "episodeCount" to "12"),
            ),
            searchResult("bt", "Beta"),
            searchResult("bt", "beta"),
        )

        val allSources = searchResultsForProvider(results, null)

        assertEquals(listOf("Alpha", "Beta"), allSources.map { it.title })
        assertEquals(listOf("Alpha!!"), searchResultsForProvider(results, "animeko-online").map { it.title })
        assertEquals(listOf("Beta", "beta"), searchResultsForProvider(results, "bt").map { it.title })
    }

    @Test
    fun searchResultsSectionUiStateSummarizesLoadingAndSelectedSource() {
        val manifests = listOf(
            manifest("bangumi-catalog", "Bangumi"),
            manifest("bt", "BT Source", setOf(SourceCapability.SEARCH, SourceCapability.BITTORRENT)),
        )
        val results = listOf(
            searchResult("bangumi-catalog", "Alpha"),
            searchResult("bt", "Beta"),
            searchResult("bt", "Gamma"),
        )
        val report = SourceSearchReport(results = results, failures = emptyList())
        val loadingState = buildSearchResultsSectionUiState(
            indexState = buildSearchIndexUiState(
                manifests = manifests,
                report = null,
                results = emptyList(),
            ),
            visibleResultCount = 0,
            loading = true,
            searched = false,
        )
        val selectedIndex = buildSearchIndexUiState(
            manifests = manifests,
            report = report,
            results = results,
            selectedProviderId = "bt",
        )
        val selectedState = buildSearchResultsSectionUiState(
            indexState = selectedIndex,
            visibleResultCount = 2,
            loading = false,
            searched = true,
        )
        val selectedEmpty = buildSearchResultsSectionUiState(
            indexState = selectedIndex,
            visibleResultCount = 0,
            loading = false,
            searched = true,
        )

        assertEquals("\u641c\u7d22\u7ed3\u679c", loadingState.headerTitle)
        assertEquals("\u6b63\u5728\u5e76\u884c\u641c\u7d22 2 \u4e2a\u6765\u6e90", loadingState.headerSubtitle)
        assertEquals("BT Source \u00b7 2 \u4e2a\u7ed3\u679c", selectedState.headerSubtitle)
        assertEquals("\u5f53\u524d\u6765\u6e90\u6682\u65e0\u547d\u4e2d", selectedEmpty.emptyTitle)
        assertTrue(selectedEmpty.emptySubtitle.contains("\u5168\u90e8\u7d22\u5f15"))
    }

    @Test
    fun searchResultsSectionUiStateExplainsNoHitsAndFailures() {
        val manifests = listOf(
            manifest("bangumi-catalog", "Bangumi"),
            manifest("direct-url", "Direct", setOf(SourceCapability.SEARCH, SourceCapability.STREAM)),
        )
        val report = SourceSearchReport(
            results = emptyList(),
            failures = listOf(SourceSearchFailure("direct-url", "Direct", "timeout")),
        )
        val indexState = buildSearchIndexUiState(
            manifests = manifests,
            report = report,
            results = emptyList(),
        )

        val state = buildSearchResultsSectionUiState(
            indexState = indexState,
            visibleResultCount = 0,
            loading = false,
            searched = true,
        )

        assertEquals("\u6682\u65e0\u7ed3\u679c \u00b7 1 \u4e2a\u6e90\u5f02\u5e38", state.headerSubtitle)
        assertEquals("\u6682\u672a\u547d\u4e2d\u53ef\u7528\u7ed3\u679c", state.emptyTitle)
        assertTrue(state.emptySubtitle.contains("\u5f02\u5e38\u6e90"))
    }

    @Test
    fun homeScheduleUiStateSummarizesSelectedTodayAndWeek() {
        val days = listOf(
            scheduleDay(1, "\u661f\u671f\u4e00", listOf(searchResult("bangumi-catalog", "Alpha"), searchResult("bangumi-catalog", "Beta"))),
            scheduleDay(2, "\u661f\u671f\u4e8c", listOf(searchResult("bangumi-catalog", "Gamma"))),
            scheduleDay(3, "\u661f\u671f\u4e09", emptyList()),
        )

        val state = buildHomeScheduleUiState(
            schedule = days,
            selectedDayId = 2,
            currentDayId = 1,
        )

        assertEquals(2, state.todayCount)
        assertEquals(3, state.weekCount)
        assertEquals("\u4eca\u65e5 2 \u90e8", state.nextUpdateLabel)
        assertEquals(1, state.selectedItems.size)
        assertEquals("\u661f\u671f\u4e8c", state.selectedDayTitle)
        assertEquals("1 \u90e8", state.selectedDayAction)
        assertTrue(state.headline.contains("\u661f\u671f\u4e8c"))
        assertTrue(state.summary.contains("\u672c\u5468\u5df2\u7d22\u5f15 3"))
        assertTrue(state.dayChips.first { it.weekdayId == 1 }.today)
        assertTrue(state.dayChips.first { it.weekdayId == 2 }.selected)
        assertEquals("\u4e8c", state.dayChips.first { it.weekdayId == 2 }.label)
    }

    @Test
    fun homeScheduleUiStateFallsBackToCurrentDayAndExplainsEmptySchedule() {
        val currentDayFallback = buildHomeScheduleUiState(
            schedule = listOf(
                scheduleDay(4, "\u661f\u671f\u56db", listOf(searchResult("bangumi-catalog", "Delta"))),
                scheduleDay(5, "\u661f\u671f\u4e94", emptyList()),
            ),
            selectedDayId = 99,
            currentDayId = 4,
        )
        val empty = buildHomeScheduleUiState(
            schedule = emptyList(),
            selectedDayId = 1,
            currentDayId = 1,
        )

        assertEquals(4, currentDayFallback.selectedDay?.weekdayId)
        assertEquals(1, currentDayFallback.todayCount)
        assertEquals("\u4eca\u65e5\u66f4\u65b0 1 \u90e8", currentDayFallback.headline)
        assertEquals(0, empty.todayCount)
        assertEquals(0, empty.weekCount)
        assertEquals("\u5f85\u540c\u6b65", empty.nextUpdateLabel)
        assertEquals("\u8ffd\u756a\u65e5\u5386", empty.selectedDayTitle)
        assertTrue(empty.summary.contains("Bangumi"))
    }

    @Test
    fun homeSchedulePresentationUiStateBuildsDigestHeroRowsAndDayChrome() {
        val alpha = SearchResult(
            providerId = "bangumi-catalog",
            title = "Alpha",
            url = "bangumi://subject/alpha",
            subtitle = "\u5468\u4e00 23:00",
            raw = mapOf("rating" to "9.4", "doing" to "1234"),
        )
        val state = buildHomeScheduleUiState(
            schedule = listOf(scheduleDay(1, "\u661f\u671f\u4e00", listOf(alpha))),
            selectedDayId = 1,
            currentDayId = 1,
        )

        val digest = buildHomeScheduleDigestUiState(state, loading = false, error = null)
        val dayChip = state.dayChips.single()
        val hero = buildHomeScheduleHeroUiState(alpha, state.selectedDayTitle)
        val row = buildHomeScheduleAnimeRowUiState(alpha)

        assertEquals(state.headline, digest.title)
        assertEquals(SourceLibraryTone.Online, digest.tone)
        assertFalse(digest.showProgress)
        assertEquals(listOf("\u4eca\u65e5 1", "\u672c\u5468 1", "\u4e0b\u4e00\u6279 \u4eca\u65e5 1 \u90e8"), digest.chips.map { it.label })
        assertEquals(38.dp, digest.iconBoxSize)
        assertEquals(8.dp, digest.cornerRadius)
        assertEquals("1", dayChip.countLabel)
        assertEquals(SourceLibraryTone.Online, dayChip.tone)
        assertEquals(76.dp, dayChip.width)
        assertEquals(5.dp, dayChip.todayDotSize)
        assertEquals("Alpha", hero.title)
        assertEquals("\u8fdb\u5165\u8be6\u60c5", hero.actionLabel)
        assertEquals(listOf("\u8bc4\u5206 9.4", "1234 \u5728\u770b"), hero.chips.map { it.label })
        assertEquals(210.dp, hero.height)
        assertEquals("Alpha", row.title)
        assertEquals("\u5468\u4e00 23:00", row.subtitle)
        assertEquals(listOf("9.4", "1234 \u5728\u770b"), row.chips.map { it.label })
        assertEquals("\u8be6\u60c5", row.actionLabel)
        assertEquals(72.dp, row.posterWidth)
    }

    @Test
    fun homeSchedulePresentationUiStateHandlesLoadingErrorAndMissingMetadata() {
        val emptySchedule = buildHomeScheduleUiState(
            schedule = emptyList(),
            selectedDayId = 1,
            currentDayId = 1,
        )
        val loading = buildHomeScheduleDigestUiState(emptySchedule, loading = true, error = null)
        val failed = buildHomeScheduleDigestUiState(emptySchedule, loading = false, error = "timeout")
        val row = buildHomeScheduleAnimeRowUiState(searchResult("direct-url", "Direct"))

        assertTrue(loading.showProgress)
        assertEquals(SourceLibraryTone.Backup, loading.tone)
        assertEquals("\u65f6\u95f4\u8868\u540c\u6b65\u5f02\u5e38", failed.title)
        assertEquals("timeout", failed.subtitle)
        assertEquals(SourceLibraryTone.Web, failed.tone)
        assertEquals("Direct", row.title)
        assertTrue(row.subtitle.isNotBlank())
        assertEquals(1, row.chips.size)
        assertEquals(SourceLibraryTone.Muted, row.chips.single().tone)
    }

    @Test
    fun homeBrowseChromeUiStateBuildsOrderedCategoryTabsAndHeaderCopy() {
        val categories = listOf(
            category("recommend", "\u63a8\u8350"),
            category("japanese", "\u65e5\u672c"),
            category("movie", "\u5267\u573a\u7248"),
            category("chinese", "\u56fd\u4ea7"),
        )

        val state = buildHomeBrowseChromeUiState(
            categories = categories,
            selectedTabId = "japanese",
            calendarExpanded = true,
        )

        assertEquals("\u8ffd\u756a\u4e0d\u8ff7\u8def", state.headline)
        assertEquals("ZFBML", state.brandLabel)
        assertEquals("\u6536\u8d77", state.calendarActionLabel)
        assertEquals(SourceLibraryTone.Online, state.calendarTone)
        assertEquals(42.dp, state.brandMarkSize)
        assertEquals(48.dp, state.searchHeight)
        assertEquals(
            listOf(HomeBrowseHomeTabId, "chinese", "japanese", "movie", "recommend"),
            state.tabs.map { it.id },
        )
        assertTrue(state.tabs.first { it.id == "japanese" }.selected)
        assertEquals(SourceLibraryTone.Online, state.tabs.first { it.id == "japanese" }.tone)
        assertTrue(state.subtitle.contains("\u65e5\u5386"))
        assertTrue(state.searchPlaceholder.contains("\u64ad\u653e\u7ebf\u8def"))
    }

    @Test
    fun homeBrowseChromeUiStateExplainsHomeFallbackWithoutCategories() {
        val state = buildHomeBrowseChromeUiState(
            categories = emptyList(),
            selectedTabId = "",
            calendarExpanded = false,
        )

        assertEquals(listOf(HomeBrowseHomeTabId), state.tabs.map { it.id })
        assertTrue(state.tabs.single().selected)
        assertEquals("\u65e5\u5386", state.calendarActionLabel)
        assertEquals(SourceLibraryTone.Muted, state.calendarTone)
        assertTrue(state.subtitle.contains("\u5206\u7c7b\u6d4f\u89c8"))
    }

    @Test
    fun homeWatchHubUiStateBuildsContinueCalendarAndRecommendationCards() {
        val state = buildHomeWatchHubUiState(
            continueItem = searchResult("bangumi-catalog", "Alpha"),
            todayCount = 3,
            recommendationCount = 8,
        )

        assertEquals(78.dp, state.cardHeight)
        assertEquals(8.dp, state.cardCornerRadius)
        assertEquals(8.dp, state.cardSpacing)
        assertEquals(
            listOf(HomeWatchHubAction.Continue, HomeWatchHubAction.Calendar, HomeWatchHubAction.Recommendation),
            state.cards.map { it.action },
        )
        assertEquals("Alpha", state.cards.first { it.action == HomeWatchHubAction.Continue }.subtitle)
        assertTrue(state.cards.first { it.action == HomeWatchHubAction.Continue }.enabled)
        assertEquals(1.18f, state.cards.first { it.action == HomeWatchHubAction.Continue }.weight, 0.001f)
        assertEquals("3\u90e8\u653e\u9001", state.cards.first { it.action == HomeWatchHubAction.Calendar }.subtitle)
        assertEquals(SourceLibraryTone.Online, state.cards.first { it.action == HomeWatchHubAction.Calendar }.tone)
        assertEquals("8\u90e8\u53ef\u9009", state.cards.first { it.action == HomeWatchHubAction.Recommendation }.subtitle)
        assertEquals(SourceLibraryTone.Cache, state.cards.first { it.action == HomeWatchHubAction.Recommendation }.tone)
    }

    @Test
    fun homeWatchHubUiStateDisablesUnavailableContinueAndRecommendations() {
        val state = buildHomeWatchHubUiState(
            continueItem = null,
            todayCount = -1,
            recommendationCount = 0,
        )

        val continueCard = state.cards.first { it.action == HomeWatchHubAction.Continue }
        val calendarCard = state.cards.first { it.action == HomeWatchHubAction.Calendar }
        val recommendationCard = state.cards.first { it.action == HomeWatchHubAction.Recommendation }

        assertEquals("\u6682\u65e0\u8fdb\u5ea6", continueCard.subtitle)
        assertFalse(continueCard.enabled)
        assertEquals("\u67e5\u770b\u65e5\u5386", calendarCard.subtitle)
        assertTrue(calendarCard.enabled)
        assertEquals("\u5148\u53bb\u641c\u7d22", recommendationCard.subtitle)
        assertFalse(recommendationCard.enabled)
    }

    @Test
    fun homeSectionHeaderUiStateBuildsSharedHeaderChrome() {
        val state = buildHomeSectionHeaderUiState(
            title = "\u731c\u4f60\u60f3\u8ffd",
            actionLabel = "\u6362\u4e00\u6279",
        )

        assertEquals("\u731c\u4f60\u60f3\u8ffd", state.title)
        assertEquals("\u6362\u4e00\u6279", state.actionLabel)
        assertEquals(38.dp, state.height)
        assertEquals(SourceLibraryTone.Primary, state.titleTone)
        assertEquals(SourceLibraryTone.Online, state.actionTone)
    }

    @Test
    fun homeContinueWatchingUiStateBuildsProgressAndLayoutContract() {
        val result = SearchResult(
            providerId = "bangumi-catalog",
            title = "Alpha",
            url = "bangumi://subject/1",
            subtitle = "\u7b2c 3 \u96c6",
        )

        val state = buildHomeContinueWatchingUiState(result, progressFraction = 1.4f)

        assertEquals(result, state.result)
        assertEquals("\u7ee7\u7eed\u89c2\u770b", state.eyebrow)
        assertEquals("Alpha", state.title)
        assertEquals("\u7b2c 3 \u96c6", state.subtitle)
        assertEquals(1f, state.progressFraction, 0.001f)
        assertEquals("\u5df2\u770b\u81f3 100%", state.progressLabel)
        assertEquals(112.dp, state.posterWidth)
        assertEquals(68.dp, state.posterHeight)
        assertEquals(14.dp, state.contentPadding)
    }

    @Test
    fun homePosterRailUiStateDeduplicatesItemsAndUsesFallback() {
        val alpha = SearchResult(
            providerId = "bangumi-catalog",
            title = "Alpha",
            url = "bangumi://subject/1",
            raw = mapOf("subjectId" to "1"),
        )
        val duplicateAlpha = alpha.copy(title = "Alpha duplicate", url = "bangumi://subject/1?dup")
        val beta = SearchResult(
            providerId = "direct-url",
            title = "Beta",
            url = "https://example.invalid/beta",
            subtitle = "\u76f4\u94fe",
        )

        val state = buildHomePosterRailUiState(
            items = listOf(alpha, duplicateAlpha, beta),
            fallback = listOf(searchResult("bangumi-catalog", "Fallback")),
            maxItems = 8,
        )
        val fallbackState = buildHomePosterRailUiState(
            items = emptyList(),
            fallback = listOf(beta),
        )

        assertEquals(12.dp, state.itemSpacing)
        assertEquals(132.dp, state.cardWidth)
        assertEquals(176.dp, state.posterHeight)
        assertEquals(2, state.items.size)
        assertEquals(listOf("Alpha", "Beta"), state.items.map { it.title })
        assertEquals("\u76f4\u94fe", state.items.last().subtitle)
        assertEquals(SourceLibraryTone.Online, state.items.first().tone)
        assertEquals(SourceLibraryTone.Primary, state.items.last().tone)
        assertEquals(listOf("Beta"), fallbackState.items.map { it.title })
    }

    @Test
    fun homeSpotlightCarouselUiStateBuildsCardsCompanionsAndLayout() {
        val alpha = SearchResult(
            providerId = "bangumi-catalog",
            title = "Alpha",
            url = "bangumi://subject/1",
            subtitle = "Bangumi / TV",
            raw = mapOf("subjectId" to "1", "categoryTitle" to "\u65b0\u756a", "rating" to "9.1"),
        )
        val duplicateAlpha = alpha.copy(title = "Alpha duplicate", url = "bangumi://subject/1?dup")
        val beta = searchResult("direct-url", "Beta")

        val state = buildHomeSpotlightCarouselUiState(
            title = "\u7cbe\u9009\u9996\u63a8",
            items = listOf(alpha, duplicateAlpha, beta),
            maxItems = 8,
        )

        assertEquals("\u7cbe\u9009\u9996\u63a8", state.title)
        assertEquals("\u6ed1\u52a8\u6311\u4e00\u90e8\u5f00\u59cb", state.helper)
        assertEquals(12.dp, state.itemSpacing)
        assertEquals(0.94f, state.cardWidthFraction, 0.001f)
        assertEquals(320.dp, state.minCardWidth)
        assertEquals(560.dp, state.maxCardWidth)
        assertEquals(226.dp, state.cardHeight)
        assertEquals(2, state.cards.size)

        val first = state.cards.first()
        assertEquals(alpha, first.result)
        assertEquals(beta, first.companion)
        assertEquals("#01 \u7126\u70b9", first.focusLabel)
        assertEquals("\u65b0\u756a", first.primaryChip)
        assertEquals("\u8bc4\u5206 9.1", first.secondaryChip)
        assertEquals("Bangumi / TV", first.subtitle)
        assertEquals("\u8fdb\u5165\u8be6\u60c5", first.actionLabel)
    }

    @Test
    fun homeSpotlightCarouselUiStateUsesFallbackWhenItemsAreEmpty() {
        val fallback = listOf(searchResult("bangumi-catalog", "Fallback"))

        val state = buildHomeSpotlightCarouselUiState(
            title = "\u7cbe\u9009",
            items = emptyList(),
            fallback = fallback,
        )

        assertEquals(1, state.cards.size)
        assertEquals(fallback.first(), state.cards.single().result)
        assertNull(state.cards.single().companion)
        assertEquals("#01 \u7126\u70b9", state.cards.single().focusLabel)
    }

    @Test
    fun categoryBrowseUiStateSummarizesCoverageRatingHeatAndSource() {
        val category = category(id = "hot", title = "\u70ed\u95e8")
        val items = listOf(
            searchResult(
                providerId = "bangumi-catalog",
                title = "Alpha",
                raw = mapOf("rating" to "8.8", "doing" to "1200"),
            ),
            searchResult(
                providerId = "bangumi-catalog",
                title = "Beta",
                raw = mapOf("rating" to "8.1", "collect" to "3500"),
            ),
        )

        val state = buildCategoryBrowseUiState(category = category, items = items)

        assertTrue(state.hasItems)
        assertEquals("\u70ed\u95e8\u5df2\u7d22\u5f15 2 \u90e8", state.headline)
        assertEquals("2", state.itemCountValue)
        assertEquals("\u5206\u7c7b\u6761\u76ee", state.itemCountLabel)
        assertEquals("8.8", state.topRatingValue)
        assertEquals("3500", state.heatValue)
        assertEquals("Bangumi", state.sourceValue)
        assertEquals("\u7cbe\u9009\u70ed\u64ad\u70ed\u95e8", state.listTitle)
        assertEquals("\u5168\u90e8 2", state.listAction)
        assertTrue(state.summary.contains("\u6700\u9ad8\u8bc4\u5206 8.8"))
        assertEquals(9.dp, state.headerSpacing)
        assertEquals(8.dp, state.metricSpacing)
        assertEquals(
            listOf("2", "8.8", "3500", "Bangumi"),
            state.metrics.map { it.value },
        )
        assertEquals(
            listOf(
                SourceLibraryTone.Primary,
                SourceLibraryTone.Cache,
                SourceLibraryTone.Online,
                SourceLibraryTone.Backup,
            ),
            state.metrics.map { it.tone },
        )
        assertEquals(128.dp, state.metrics.first().width)
        assertEquals(82.dp, state.metrics.first().height)
        assertEquals(10.dp, state.metrics.first().padding)
    }

    @Test
    fun categoryBrowseUiStateExplainsFallbackEmptyAndErrors() {
        val category = category(id = "movie", title = "\u5267\u573a\u7248")
        val fallback = listOf(searchResult(providerId = "direct-url", title = "Fallback"))

        val fallbackState = buildCategoryBrowseUiState(
            category = category,
            items = emptyList(),
            fallback = fallback,
        )
        val emptyState = buildCategoryBrowseUiState(
            category = category,
            items = emptyList(),
        )
        val errorState = buildCategoryBrowseUiState(
            category = category,
            items = emptyList(),
            error = "HTTP 500",
        )

        assertFalse(fallbackState.hasItems)
        assertEquals("\u5267\u573a\u7248\u5c55\u793a\u515c\u5e95\u63a8\u8350", fallbackState.headline)
        assertEquals("1", fallbackState.itemCountValue)
        assertEquals("\u515c\u5e95\u63a8\u8350", fallbackState.itemCountLabel)
        assertEquals("\u76f4\u94fe", fallbackState.sourceValue)
        assertEquals(listOf("1", "--", "--", "\u76f4\u94fe"), fallbackState.metrics.map { it.value })
        assertTrue(fallbackState.summary.contains("\u515c\u5e95"))
        assertFalse(emptyState.hasItems)
        assertEquals("--", emptyState.itemCountValue)
        assertEquals(listOf("--", "--", "--", "\u5f85\u540c\u6b65"), emptyState.metrics.map { it.value })
        assertTrue(emptyState.emptySubtitle.contains("\u641c\u7d22\u756a\u540d"))
        assertEquals("\u5267\u573a\u7248\u52a0\u8f7d\u5f02\u5e38", errorState.headline)
        assertTrue(errorState.summary.contains("HTTP 500"))
    }

    @Test
    fun categoryBrowseItemUiStateBuildsMetadataChipsAndLayout() {
        val result = SearchResult(
            providerId = "bangumi-catalog",
            title = "Alpha",
            url = "bangumi://subject/1",
            subtitle = "TV / 2024",
            raw = mapOf("rating" to "8.8", "doing" to "1200"),
        )

        val state = buildCategoryBrowseItemUiState(result)

        assertEquals(result, state.result)
        assertEquals("Alpha", state.title)
        assertEquals("TV / 2024", state.subtitle)
        assertEquals(listOf("\u8bc4\u5206 8.8", "1200 \u5728\u770b", "Bangumi \u8d44\u6599\u5e93"), state.chips.map { it.label })
        assertEquals(listOf(SourceLibraryTone.Cache, SourceLibraryTone.Primary, SourceLibraryTone.Online), state.chips.map { it.tone })
        assertEquals(SourceLibraryTone.Online, state.tone)
        assertEquals("\u8be6\u60c5", state.actionLabel)
        assertEquals(88.dp, state.posterWidth)
        assertEquals(118.dp, state.posterHeight)
        assertEquals(12.dp, state.rowPadding)
        assertEquals(8.dp, state.chipSpacing)
    }

    @Test
    fun categoryBrowseItemUiStateFallsBackToProviderAndCollectHeat() {
        val result = SearchResult(
            providerId = "direct-url",
            title = "",
            url = "https://example.invalid/direct",
            raw = mapOf("collect" to "3500"),
        )

        val state = buildCategoryBrowseItemUiState(result)

        assertEquals("\u672a\u547d\u540d\u6761\u76ee", state.title)
        assertEquals("\u5728\u7ebf\u94fe\u63a5", state.subtitle)
        assertEquals(listOf("3500 \u6536\u85cf", "\u5728\u7ebf\u94fe\u63a5"), state.chips.map { it.label })
        assertEquals(SourceLibraryTone.Primary, state.tone)
        assertEquals(SourceLibraryTone.Primary, state.chips.last().tone)
    }

    @Test
    fun categoryBrowseListUiStateSkipsHeroItemsButKeepsRowsAvailable() {
        val category = category(id = "hot", title = "\u70ed\u95e8")
        val alpha = SearchResult(
            providerId = "bangumi-catalog",
            title = "Alpha",
            url = "bangumi://subject/1",
            raw = mapOf("subjectId" to "1"),
        )
        val beta = SearchResult(
            providerId = "bangumi-catalog",
            title = "Beta",
            url = "bangumi://subject/2",
            raw = mapOf("subjectId" to "2"),
        )

        val state = buildCategoryBrowseListUiState(
            category = category,
            items = listOf(alpha, beta),
            heroItems = listOf(alpha),
        )
        val allHero = buildCategoryBrowseListUiState(
            category = category,
            items = listOf(alpha),
            heroItems = listOf(alpha),
        )

        assertFalse(state.usesFallback)
        assertFalse(state.showEmptyState)
        assertEquals("\u7cbe\u9009\u70ed\u64ad\u70ed\u95e8", state.title)
        assertEquals("\u5168\u90e8 2", state.actionLabel)
        assertEquals(listOf("Beta"), state.rows.map { it.title })
        assertEquals(listOf("Alpha"), allHero.rows.map { it.title })
        assertEquals(10.dp, state.itemSpacing)
    }

    @Test
    fun categoryBrowseListUiStateUsesFallbackRowsWhenCategoryIsEmpty() {
        val category = category(id = "movie", title = "\u5267\u573a\u7248")
        val fallback = listOf(
            SearchResult(
                providerId = "direct-url",
                title = "Fallback",
                url = "https://example.invalid/fallback",
            ),
        )

        val state = buildCategoryBrowseListUiState(
            category = category,
            items = emptyList(),
            fallback = fallback,
            heroItems = emptyList(),
        )
        val empty = buildCategoryBrowseListUiState(
            category = category,
            items = emptyList(),
            fallback = emptyList(),
        )

        assertTrue(state.usesFallback)
        assertFalse(state.showEmptyState)
        assertEquals("\u5267\u573a\u7248\u515c\u5e95\u63a8\u8350", state.title)
        assertEquals("\u515c\u5e95 1", state.actionLabel)
        assertEquals(listOf("Fallback"), state.rows.map { it.title })
        assertFalse(empty.usesFallback)
        assertTrue(empty.showEmptyState)
        assertEquals("\u6682\u65e0\u53ef\u5c55\u793a\u6761\u76ee", empty.emptyTitle)
    }

    @Test
    fun appNavigationUiStateSummarizesSelectedTabAndCapabilities() {
        val state = buildAppNavigationUiState(
            selectedTabId = "search",
            todayCount = 7,
            searchableSourceCount = 3,
            sourceCount = 4,
            cacheableSourceCount = 2,
        )

        assertEquals("search", state.selectedTabId)
        assertEquals(listOf("discover", "search", "sources", "settings"), state.tabs.map { it.id })
        assertEquals("搜索", state.selectedTab?.label)
        assertEquals("3 源", state.selectedTab?.statusLabel)
        assertTrue(state.tabs.first { it.id == "search" }.selected)

        val discover = state.tabs.first { it.id == "discover" }
        val sources = state.tabs.first { it.id == "sources" }
        val settings = state.tabs.first { it.id == "settings" }
        assertEquals("今日 7", discover.statusLabel)
        assertEquals(SourceLibraryTone.Primary, discover.tone)
        assertEquals("4 来源", sources.statusLabel)
        assertEquals(SourceLibraryTone.Backup, sources.tone)
        assertEquals("2 可缓存", settings.statusLabel)
        assertEquals(SourceLibraryTone.Cache, settings.tone)
        assertEquals("ZFBML", state.chrome.brandLabel)
        assertEquals("搜索", state.chrome.selectedTitle)
        assertTrue(state.chrome.selectedSummary.contains("3 个搜索源"))
        assertEquals(SourceLibraryTone.Online, state.chrome.selectedTone)
        assertEquals(108.dp, state.chrome.railWidth)
        assertEquals(76.dp, state.chrome.bottomBarHeight)
    }

    @Test
    fun appNavigationUiStateExplainsEmptySourceCoverage() {
        val state = buildAppNavigationUiState(
            selectedTabId = "",
            todayCount = -1,
            searchableSourceCount = -1,
            sourceCount = 0,
            cacheableSourceCount = 0,
        )

        assertEquals("discover", state.selectedTabId)
        assertEquals("首页", state.selectedTab?.label)
        assertEquals("推荐", state.tabs.first { it.id == "discover" }.statusLabel)
        assertEquals("待索引", state.tabs.first { it.id == "search" }.statusLabel)
        assertEquals(SourceLibraryTone.Muted, state.tabs.first { it.id == "search" }.tone)
        assertEquals("待接入", state.tabs.first { it.id == "sources" }.statusLabel)
        assertEquals(SourceLibraryTone.Muted, state.tabs.first { it.id == "sources" }.tone)
        assertEquals("我的", state.tabs.first { it.id == "settings" }.statusLabel)
        assertEquals(SourceLibraryTone.Muted, state.tabs.first { it.id == "settings" }.tone)
        assertEquals("首页", state.chrome.selectedTitle)
        assertTrue(state.chrome.selectedSummary.contains("推荐"))
        assertEquals(SourceLibraryTone.Online, state.chrome.selectedTone)
    }

    @Test
    fun appNavigationUiStateUsesHomeScheduleTodayCount() {
        val scheduleState = buildHomeScheduleUiState(
            schedule = listOf(
                scheduleDay(1, "\u661f\u671f\u4e00", listOf(searchResult("bangumi-catalog", "Alpha"))),
                scheduleDay(
                    2,
                    "\u661f\u671f\u4e8c",
                    listOf(
                        searchResult("bangumi-catalog", "Beta"),
                        searchResult("bangumi-catalog", "Gamma"),
                    ),
                ),
            ),
            selectedDayId = 2,
            currentDayId = 2,
        )

        val navigationState = buildAppNavigationUiState(
            selectedTabId = "discover",
            todayCount = scheduleState.todayCount,
            searchableSourceCount = 1,
            sourceCount = 1,
            cacheableSourceCount = 0,
        )

        assertEquals(2, scheduleState.todayCount)
        assertEquals("今日 2", navigationState.selectedTab?.statusLabel)
        assertEquals(SourceLibraryTone.Primary, navigationState.selectedTab?.tone)
    }

    @Test
    fun brandSplashUiStateSummarizesSourceCacheAndDanmakuReadiness() {
        val state = buildBrandSplashUiState(
            sourceCount = 8,
            searchableSourceCount = 5,
            cacheableSourceCount = 2,
            danmakuProviderCount = 4,
        )

        assertEquals("追番不迷路", state.headline)
        assertEquals("ZFBML", state.brand)
        assertEquals("5 个搜索源 · 弹幕自动匹配", state.tagline)
        assertEquals("缓存与片单已就绪", state.progressLabel)
        assertEquals(1_100, state.startupDurationMillis)
        assertEquals(112.dp, state.logoSize)
        assertEquals(190.dp, state.orbitSize)
        assertEquals(164.dp, state.progressWidth)
        assertEquals(listOf("今日片单", "5 源搜索", "4 路弹幕"), state.statusPills.map { it.label })
        assertEquals(listOf(SourceLibraryTone.Primary, SourceLibraryTone.Online, SourceLibraryTone.Backup), state.statusPills.map { it.tone })
        assertEquals(5, state.posterTiles.size)
        assertEquals(1, state.posterTiles.count { it.emphasized })
        assertEquals(4, state.danmakuStreaks.size)
        assertEquals(BrandSplashStreakAnchor.TopStart, state.danmakuStreaks.first().anchor)
        assertEquals(3, state.signalRails.size)
        assertEquals(54.dp, state.signalRails.last().offsetRange)
    }

    @Test
    fun brandSplashUiStateExplainsEmptySourceCoverage() {
        val state = buildBrandSplashUiState(
            sourceCount = -1,
            searchableSourceCount = -1,
            cacheableSourceCount = 0,
            danmakuProviderCount = 0,
        )

        assertEquals("今晚继续追", state.tagline)
        assertEquals("片单已就绪", state.progressLabel)
        assertEquals("源站待接入", state.statusPills[1].label)
        assertEquals(SourceLibraryTone.Muted, state.statusPills[1].tone)
        assertEquals("弹幕同步", state.statusPills[2].label)
    }

    @Test
    fun sourceLibraryUiStateSummarizesStrategiesAndCards() {
        val manifests = listOf(
            manifest(
                id = "online",
                name = "Online",
                capabilities = setOf(
                    SourceCapability.SEARCH,
                    SourceCapability.DETAIL,
                    SourceCapability.EPISODES,
                    SourceCapability.STREAM,
                    SourceCapability.DOWNLOAD,
                ),
                supportsDownload = true,
                domains = setOf("online.example"),
            ),
            manifest(
                id = "bt",
                name = "BT",
                capabilities = setOf(SourceCapability.SEARCH, SourceCapability.STREAM, SourceCapability.BITTORRENT),
            ),
            manifest(
                id = "web",
                name = "Web",
                capabilities = setOf(SourceCapability.SEARCH, SourceCapability.WEBVIEW_SNIFF),
                requiresWebView = true,
            ),
        )

        val state = buildSourceLibraryUiState(manifests)

        assertEquals(3, state.providerCount)
        assertEquals(1, state.onlineCount)
        assertEquals(1, state.btCount)
        assertEquals(1, state.downloadableCount)
        assertEquals(1, state.webViewCount)
        assertTrue(state.headline.contains("3 \u4e2a\u6765\u6e90"))
        assertTrue(state.summary.contains("1 \u4e2a\u5728\u7ebf\u6e90"))
        assertTrue(state.chips.any { it.label == "1 \u53ef\u7f13\u5b58" })

        val onlineStrategy = state.strategies.first { it.id == "online" }
        val webStrategy = state.strategies.first { it.id == "web" }
        assertEquals("1 \u6e90", onlineStrategy.value)
        assertEquals(SourceLibraryTone.Online, onlineStrategy.tone)
        assertEquals("\u590d\u6742\u9875\u9762\u518d\u55c5\u63a2", webStrategy.subtitle)

        val onlineCard = state.sourceCards.first { it.id == "online" }
        val btCard = state.sourceCards.first { it.id == "bt" }
        val webCard = state.sourceCards.first { it.id == "web" }
        assertEquals("\u5728\u7ebf\u6e90", onlineCard.statusLabel)
        assertTrue(onlineCard.featureText.contains("\u7f13\u5b58"))
        assertEquals("online.example", onlineCard.domainText)
        assertTrue(btCard.isBt)
        assertEquals(SourceLibraryTone.Backup, btCard.statusTone)
        assertEquals(SourceLibraryTone.Web, webCard.statusTone)
        assertTrue(webCard.featureText.contains("\u55c5\u63a2"))
    }

    @Test
    fun sourceLibraryUiStateExplainsEmptyProviderList() {
        val state = buildSourceLibraryUiState(emptyList())

        assertEquals(0, state.providerCount)
        assertEquals("\u7247\u5e93\u6765\u6e90\u5f85\u63a5\u5165", state.headline)
        assertEquals("\u6682\u65e0\u7ebf\u8def\u6765\u6e90", state.sourceListTitle)
        assertTrue(state.sourceCards.isEmpty())
        assertEquals("0 \u6e90", state.strategies.first { it.id == "online" }.value)
        assertTrue(state.emptySubtitle.contains("\u89c4\u5219\u6e90"))
    }

    @Test
    fun cacheLibraryUiStateSummarizesMedia3BtBlockedAndAdvancedCapabilities() {
        val manifests = listOf(
            manifest(
                id = "hls",
                name = "HLS",
                capabilities = setOf(SourceCapability.SEARCH, SourceCapability.STREAM, SourceCapability.DOWNLOAD),
                supportsDownload = true,
            ),
            manifest(
                id = "bt",
                name = "BT",
                capabilities = setOf(SourceCapability.SEARCH, SourceCapability.STREAM, SourceCapability.BITTORRENT),
                supportsDownload = true,
            ),
            manifest(
                id = "web",
                name = "Web",
                capabilities = setOf(SourceCapability.SEARCH, SourceCapability.WEBVIEW_SNIFF),
                requiresWebView = true,
            ),
        )

        val state = buildCacheLibraryUiState(
            manifests = manifests,
            advancedEngineAvailable = true,
        )

        assertEquals(2, state.cacheableSourceCount)
        assertEquals(1, state.media3SourceCount)
        assertEquals(1, state.btSourceCount)
        assertEquals(1, state.webBlockedSourceCount)
        assertTrue(state.advancedEngineAvailable)
        assertEquals("\u79bb\u7ebf\u7247\u5e93\u5df2\u63a5\u5165 2 \u4e2a\u7f13\u5b58\u6765\u6e90", state.headline)
        assertTrue(state.summary.contains("Media3"))
        assertTrue(state.summary.contains("BT"))
        assertTrue(state.chips.any { it.label == "1 Media3" })
        assertTrue(state.chips.any { it.label == "1 \u963b\u65ad" })

        val media3 = state.capabilities.first { it.id == "media3" }
        val bt = state.capabilities.first { it.id == "bt" }
        val blocked = state.capabilities.first { it.id == "blocked" }
        val advanced = state.capabilities.first { it.id == "advanced" }
        assertEquals("1 \u6e90", media3.value)
        assertEquals(SourceLibraryTone.Online, media3.tone)
        assertEquals("1 \u6e90", bt.value)
        assertEquals(SourceLibraryTone.Backup, bt.tone)
        assertEquals("1 \u6e90", blocked.value)
        assertEquals(SourceLibraryTone.Web, blocked.tone)
        assertEquals("\u53ef\u7528", advanced.value)
        assertEquals(SourceLibraryTone.Cache, advanced.tone)
    }

    @Test
    fun cacheLibraryUiStateExplainsEmptyAndPendingAdvancedRuntime() {
        val state = buildCacheLibraryUiState(emptyList())

        assertEquals(0, state.cacheableSourceCount)
        assertEquals("\u79bb\u7ebf\u7247\u5e93\u5f85\u63a5\u5165\u53ef\u7f13\u5b58\u6765\u6e90", state.headline)
        assertTrue(state.summary.contains("HLS"))
        assertEquals("0 \u53ef\u7f13\u5b58", state.chips.first().label)
        assertEquals("\u5f85\u63a5", state.capabilities.first { it.id == "media3" }.value)
        assertEquals("\u5f85\u6e90", state.capabilities.first { it.id == "bt" }.value)
        assertEquals("\u65e0\u963b\u65ad", state.capabilities.first { it.id == "blocked" }.value)
        assertEquals("\u5f85\u63a5", state.capabilities.first { it.id == "advanced" }.value)
        assertEquals(SourceLibraryTone.Muted, state.capabilities.first { it.id == "advanced" }.tone)
    }

    @Test
    fun profileCenterUiStateSummarizesQuickActionsAndSettings() {
        val cacheState = buildCacheLibraryUiState(
            manifests = listOf(
                manifest(
                    id = "hls",
                    name = "HLS",
                    capabilities = setOf(SourceCapability.SEARCH, SourceCapability.STREAM, SourceCapability.DOWNLOAD),
                    supportsDownload = true,
                ),
                manifest(
                    id = "bt",
                    name = "BT",
                    capabilities = setOf(SourceCapability.SEARCH, SourceCapability.STREAM, SourceCapability.BITTORRENT),
                ),
            ),
        )

        val state = buildProfileCenterUiState(
            version = "0.5.104",
            sourceCount = 4,
            danmakuCount = 3,
            cacheState = cacheState,
        )

        assertEquals("0.5.104", state.version)
        assertEquals("\u6211\u7684\u8ffd\u756a\u4e2d\u5fc3", state.headline)
        assertTrue(state.summary.contains("2 \u4e2a\u6765\u6e90"))
        assertEquals(4, state.sourceCount)
        assertEquals(3, state.danmakuCount)
        assertEquals(2, state.cacheableSourceCount)
        assertTrue(state.chips.any { it.label == "v0.5.104" })
        assertEquals(listOf("continue", "cache", "danmaku", "sources"), state.quickActions.map { it.id })
        assertEquals("2 \u6e90\u53ef\u7f13\u5b58", state.quickActions.first { it.id == "cache" }.subtitle)
        assertEquals(SourceLibraryTone.Cache, state.quickActions.first { it.id == "cache" }.tone)
        assertEquals("Media3", state.settings.first { it.id == "core" }.value)
        assertEquals("2 \u6e90", state.settings.first { it.id == "cache" }.value)
        assertEquals("3 \u5e73\u53f0", state.settings.first { it.id == "danmaku" }.value)
    }

    @Test
    fun profileCenterUiStateExplainsMissingSources() {
        val cacheState = buildCacheLibraryUiState(emptyList())

        val state = buildProfileCenterUiState(
            version = "0.5.104",
            sourceCount = 0,
            danmakuCount = 0,
            cacheState = cacheState,
        )

        assertEquals(0, state.sourceCount)
        assertEquals(0, state.cacheableSourceCount)
        assertTrue(state.summary.contains("\u5148\u63a5\u5165\u6765\u6e90"))
        assertEquals("0 \u6e90\u53ef\u7f13\u5b58", state.quickActions.first { it.id == "cache" }.subtitle)
        assertEquals("0 \u6765\u6e90", state.settings.first { it.id == "sources" }.value)
    }

    @Test
    fun playerEpisodePanelUiStateSummarizesCurrentAndLoadingEpisodes() {
        val episodes = listOf(
            episode(id = "ep-1", index = 1),
            episode(id = "ep-2", index = 2),
            episode(id = "special", index = 99),
        )
        val detail = MediaDetail(
            providerId = "provider",
            title = "Alpha",
            url = "https://example.invalid/alpha",
            episodes = episodes,
        )

        val idle = buildPlayerEpisodePanelUiState(
            detail = detail,
            currentEpisode = episodes[0],
            episodeLoadingId = null,
        )
        val loading = buildPlayerEpisodePanelUiState(
            detail = detail,
            currentEpisode = episodes[0],
            episodeLoadingId = "ep-2",
        )

        assertTrue(idle.hasItems)
        assertEquals("Alpha", idle.title)
        assertEquals("当前 第 1 集 · 共 3 集", idle.summary)
        assertEquals("全部选集", idle.listTitle)
        assertEquals(listOf("正在看", "自动匹配", "3集"), idle.chips.map { it.label })
        assertEquals(9.dp, idle.listSpacing)
        assertEquals(0.72f, idle.listTitleAlpha)
        assertEquals(0.06f, idle.summaryCardContainerAlpha)
        assertEquals(0.08f, idle.summaryCardBorderAlpha)
        assertEquals(8.dp, idle.summaryCardCornerRadius)
        assertEquals(12.dp, idle.summaryCardPadding)
        assertEquals(10.dp, idle.summaryCardSpacing)
        assertEquals(SourceLibraryTone.Primary, idle.summaryIconTone)
        assertEquals(40.dp, idle.summaryIconBoxSize)
        assertEquals(22.dp, idle.summaryIconSize)
        assertEquals(8.dp, idle.summaryIconCornerRadius)
        assertEquals(0.18f, idle.summaryIconContainerAlpha)
        assertEquals(3.dp, idle.summaryTextSpacing)
        assertEquals(SourceLibraryTone.Muted, idle.summaryTone)
        assertEquals(7.dp, idle.summaryChipSpacing)
        assertEquals("切换选集后自动选择最佳播放源", idle.helperText)
        assertEquals(0.64f, idle.helperTextAlpha)
        assertEquals("01", idle.items[0].compactIndexLabel)
        assertEquals("当前", idle.items[0].statusLabel)
        assertEquals("播放中", idle.items[0].actionLabel)
        assertEquals(listOf("当前"), idle.items[0].badges.map { it.label })
        assertTrue(idle.items[0].highlighted)
        assertTrue(idle.items[0].prominent)
        assertTrue(idle.items[0].actionEnabled)
        assertEquals(1f, idle.items[0].railAlpha)
        assertEquals(0.94f, idle.items[0].titleAlpha)
        assertEquals(0.86f, idle.items[0].subtitleAlpha)
        assertEquals(0.08f, idle.items[0].containerAlpha)
        assertEquals(0.032f, idle.items[0].disabledContainerAlpha)
        assertEquals(0.85f, idle.items[0].borderAlpha)
        assertEquals(10.dp, idle.items[0].rowPadding)
        assertEquals(10.dp, idle.items[0].rowSpacing)
        assertEquals(8.dp, idle.items[0].rowCornerRadius)
        assertEquals(4.dp, idle.items[0].railWidth)
        assertEquals(52.dp, idle.items[0].railHeight)
        assertEquals(42.dp, idle.items[0].indexBoxSize)
        assertEquals(8.dp, idle.items[0].indexBoxCornerRadius)
        assertEquals(0.16f, idle.items[0].indexBoxContainerAlpha)
        assertEquals(20.dp, idle.items[0].loadingIndicatorSize)
        assertEquals(4.dp, idle.items[0].textColumnSpacing)
        assertEquals(6.dp, idle.items[0].titleBadgeSpacing)
        assertEquals(SourceLibraryTone.Muted, idle.items[0].subtitleTone)
        assertEquals(30.dp, idle.items[0].actionLabelHeight)
        assertEquals(8.dp, idle.items[0].actionLabelCornerRadius)
        assertEquals(8.dp, idle.items[0].actionLabelHorizontalPadding)
        assertEquals(4.dp, idle.items[0].actionLabelSpacing)
        assertEquals(13.dp, idle.items[0].actionLabelIconSize)
        assertEquals(0.13f, idle.items[0].actionLabelContainerAlpha)
        assertEquals(0.06f, idle.items[0].actionLabelDisabledContainerAlpha)
        assertEquals(SourceLibraryTone.Primary, idle.items[0].tone)
        assertEquals("播放", idle.items[1].actionLabel)
        assertEquals("02", idle.items[1].compactIndexLabel)
        assertTrue(idle.items[1].badges.isEmpty())
        assertFalse(idle.items[1].highlighted)
        assertFalse(idle.items[1].prominent)
        assertTrue(idle.items[1].enabled)
        assertEquals(0.045f, idle.items[1].containerAlpha)
        assertEquals(0.08f, idle.items[1].borderAlpha)
        assertTrue(loading.summary.contains("第 2 集"))
        assertEquals("加载中", loading.items[1].statusLabel)
        assertEquals("加载中", loading.items[1].actionLabel)
        assertEquals(listOf("加载中"), loading.items[1].badges.map { it.label })
        assertTrue(loading.items[1].highlighted)
        assertFalse(loading.items[1].prominent)
        assertEquals(SourceLibraryTone.Backup, loading.items[1].tone)
        assertFalse(loading.items[2].enabled)
        assertEquals("等待", loading.items[2].actionLabel)
        assertFalse(loading.items[2].actionEnabled)
        assertEquals(0.38f, loading.items[2].railAlpha)
        assertEquals(0.42f, loading.items[2].titleAlpha)
        assertEquals(0.38f, loading.items[2].subtitleAlpha)
        assertEquals(0.045f, loading.items[2].containerAlpha)
        assertEquals(0.08f, loading.items[2].borderAlpha)
        assertEquals(SourceLibraryTone.Muted, loading.items[2].tone)
    }

    @Test
    fun playerEpisodePanelUiStateExplainsEmptyEpisodeList() {
        val current = episode(id = "ep-1", index = 1)
        val detail = MediaDetail(
            providerId = "provider",
            title = "Empty",
            url = "https://example.invalid/empty",
            episodes = emptyList(),
        )

        val state = buildPlayerEpisodePanelUiState(
            detail = detail,
            currentEpisode = current,
            episodeLoadingId = null,
        )

        assertFalse(state.hasItems)
        assertEquals("当前条目没有可切换选集", state.summary)
        assertEquals("当前条目没有可切换选集", state.emptyText)
        assertEquals(listOf("正在看", "自动匹配"), state.chips.map { it.label })
    }

    @Test
    fun portraitEpisodeRailUiStateWindowsEpisodesAndLoadingState() {
        val episodes = (1..24).map { index -> episode(id = "ep-$index", index = index) }
        val detail = MediaDetail(
            providerId = "provider",
            title = "Alpha",
            url = "https://example.invalid/alpha",
            episodes = episodes,
        )

        val state = buildPortraitEpisodeRailUiState(
            detail = detail,
            currentEpisode = episodes[9],
            episodeLoadingId = "ep-11",
            maxCount = 6,
        )

        assertTrue(state.visible)
        assertEquals("选集", state.title)
        assertEquals("全部 24 集", state.allEpisodesLabel)
        assertEquals(listOf("ep-6", "ep-7", "ep-8", "ep-9", "ep-10", "ep-11"), state.items.map { it.episode.id })
        assertEquals("10", state.items.first { it.episode.id == "ep-10" }.indexLabel)
        assertTrue(state.items.first { it.episode.id == "ep-10" }.selected)
        assertEquals(SourceLibraryTone.Online, state.items.first { it.episode.id == "ep-10" }.tone)
        assertEquals("加载中", state.items.first { it.episode.id == "ep-11" }.title)
        assertTrue(state.items.first { it.episode.id == "ep-11" }.loading)
        assertEquals(SourceLibraryTone.Backup, state.items.first { it.episode.id == "ep-11" }.tone)
        assertFalse(state.items.first { it.episode.id == "ep-6" }.enabled)
        assertEquals("全部", checkNotNull(state.moreAction).title)
        assertEquals("24集", checkNotNull(state.moreAction).subtitle)
    }

    @Test
    fun portraitEpisodeRailUiStateHidesSingleEpisode() {
        val onlyEpisode = episode(id = "ep-1", index = 1)
        val detail = MediaDetail(
            providerId = "provider",
            title = "Single",
            url = "https://example.invalid/single",
            episodes = listOf(onlyEpisode),
        )

        val state = buildPortraitEpisodeRailUiState(
            detail = detail,
            currentEpisode = onlyEpisode,
            episodeLoadingId = null,
            maxCount = 6,
        )

        assertFalse(state.visible)
        assertTrue(state.items.isEmpty())
        assertNull(state.moreAction)
        assertEquals("全部 1 集", state.allEpisodesLabel)
    }

    @Test
    fun nextEpisodeForPlayerUsesPlaybackListOrder() {
        val episodes = listOf(
            episode(id = "ep-1", index = 1),
            episode(id = "special", index = 99),
            episode(id = "ep-2", index = 2),
        )

        assertEquals("special", nextEpisodeForPlayer(episodes, episodes[0])?.id)
        assertEquals("ep-2", nextEpisodeForPlayer(episodes, episodes[1])?.id)
        assertEquals(null, nextEpisodeForPlayer(episodes, episodes[2]))
        assertEquals(null, nextEpisodeForPlayer(episodes, episode(id = "outside", index = 99)))
    }

    @Test
    fun playerProgressPollDelayPrioritizesVisiblePlayingControls() {
        assertEquals(
            100L,
            playerProgressPollDelayMs(isPlaying = true, controlsVisible = true, panelOpen = false),
        )
        assertEquals(
            160L,
            playerProgressPollDelayMs(isPlaying = true, controlsVisible = true, panelOpen = true),
        )
        assertEquals(
            250L,
            playerProgressPollDelayMs(isPlaying = true, controlsVisible = false, panelOpen = false),
        )
        assertEquals(
            300L,
            playerProgressPollDelayMs(isPlaying = false, controlsVisible = true, panelOpen = false),
        )
        assertEquals(
            500L,
            playerProgressPollDelayMs(isPlaying = false, controlsVisible = false, panelOpen = false),
        )
    }

    @Test
    fun playerDanmakuSafeAreaTracksControlsPanelsAndLockState() {
        val hidden = buildPlayerDanmakuSafeAreaUiState(
            compact = false,
            controlsVisible = false,
            controlsLocked = false,
            panelOpen = false,
        )
        val fullscreenControls = buildPlayerDanmakuSafeAreaUiState(
            compact = false,
            controlsVisible = true,
            controlsLocked = false,
            panelOpen = false,
        )
        val fullscreenPanel = buildPlayerDanmakuSafeAreaUiState(
            compact = false,
            controlsVisible = true,
            controlsLocked = false,
            panelOpen = true,
            noticeVisible = true,
        )
        val locked = buildPlayerDanmakuSafeAreaUiState(
            compact = false,
            controlsVisible = false,
            controlsLocked = true,
            panelOpen = false,
        )
        val compact = buildPlayerDanmakuSafeAreaUiState(
            compact = true,
            controlsVisible = true,
            controlsLocked = false,
            panelOpen = false,
            noticeVisible = true,
        )
        val centerBlocked = buildPlayerDanmakuSafeAreaUiState(
            compact = false,
            controlsVisible = true,
            controlsLocked = false,
            panelOpen = false,
            centerOverlayVisible = true,
            seekFeedbackVisible = true,
        )

        assertEquals(8, hidden.topInsetDp)
        assertEquals(8, hidden.bottomInsetDp)
        assertEquals(0, hidden.centerExcludedHeightDp)
        assertEquals(84, fullscreenControls.endInsetDp)
        assertEquals(72, fullscreenControls.startInsetDp)
        assertTrue(fullscreenPanel.endInsetDp > fullscreenControls.endInsetDp)
        assertTrue(fullscreenPanel.bottomInsetDp > hidden.bottomInsetDp)
        assertEquals(0, fullscreenPanel.centerExcludedHeightDp)
        assertEquals(72, locked.startInsetDp)
        assertEquals(0, locked.endInsetDp)
        assertEquals(0, compact.endInsetDp)
        assertTrue(compact.bottomInsetDp > hidden.bottomInsetDp)
        assertEquals(172, centerBlocked.centerExcludedHeightDp)
    }

    @Test
    fun playerDanmakuSettingsUiStateFormatsToggleSlidersAndSafeArea() {
        val safeArea = buildPlayerDanmakuSafeAreaUiState(
            compact = false,
            controlsVisible = true,
            controlsLocked = false,
            panelOpen = true,
            noticeVisible = true,
        )

        val enabled = buildPlayerDanmakuSettingsUiState(
            enabled = true,
            density = 0.62f,
            alpha = 0.76f,
            fontScale = 0.72f,
            safeArea = safeArea,
        )
        val disabled = buildPlayerDanmakuSettingsUiState(
            enabled = false,
            density = 0.3f,
            alpha = 1.2f,
            fontScale = 1.08f,
        )

        assertEquals("弹幕已开启", enabled.toggleTitle)
        assertEquals("点击关闭弹幕显示", enabled.toggleSubtitle)
        assertTrue(enabled.toggleSelected)
        assertEquals("关闭", enabled.toggleActionLabel)
        assertEquals(listOf("显示中"), enabled.toggleBadges.map { it.label })
        assertTrue(enabled.toggleHighlighted)
        assertTrue(enabled.toggleProminent)
        assertTrue(enabled.toggleActionEnabled)
        assertEquals(1f, enabled.toggleIconAlpha)
        assertEquals(0.94f, enabled.toggleTitleAlpha)
        assertEquals(0.86f, enabled.toggleSubtitleAlpha)
        assertEquals(48.dp, enabled.toggleRowState.minHeight)
        assertEquals(8.dp, enabled.toggleRowState.cornerRadius)
        assertEquals(SourceLibraryTone.Primary, enabled.toggleRowState.containerTone)
        assertEquals(0.18f, enabled.toggleRowState.containerAlpha)
        assertEquals(SourceLibraryTone.Primary, enabled.toggleRowState.borderTone)
        assertEquals(0.72f, enabled.toggleRowState.borderAlpha)
        assertEquals(12.dp, enabled.toggleRowState.horizontalPadding)
        assertEquals(9.dp, enabled.toggleRowState.verticalPadding)
        assertEquals(10.dp, enabled.toggleRowState.contentSpacing)
        assertEquals(SourceLibraryTone.Primary, enabled.toggleRowState.iconTone)
        assertEquals(19.dp, enabled.toggleRowState.iconSize)
        assertEquals(2.dp, enabled.toggleRowState.textSpacing)
        assertEquals(6.dp, enabled.toggleRowState.titleBadgeSpacing)
        assertEquals(SourceLibraryTone.Muted, enabled.toggleRowState.subtitleTone)
        assertEquals(1f, enabled.toggleRowState.trailingEnabledAlpha)
        assertEquals(0.42f, enabled.toggleRowState.trailingDisabledAlpha)
        assertEquals(SourceLibraryTone.Primary, enabled.toggleRowState.selectedIconTone)
        assertEquals(18.dp, enabled.toggleRowState.selectedIconSize)
        assertEquals("密度", enabled.densitySlider.title)
        assertEquals("60%", enabled.densitySlider.valueText)
        assertEquals(0.62f, enabled.densitySlider.value)
        assertEquals(0.3f, enabled.densitySlider.valueRange.start)
        assertEquals(1f, enabled.densitySlider.valueRange.endInclusive)
        assertEquals(2, enabled.densitySlider.steps)
        assertEquals(SourceLibraryTone.Online, enabled.densitySlider.tone)
        assertEquals(4.dp, enabled.densitySlider.verticalSpacing)
        assertEquals(30.dp, enabled.densitySlider.sliderHeight)
        assertNull(enabled.densitySlider.titleTone)
        assertEquals(1f, enabled.densitySlider.titleAlpha)
        assertEquals(SourceLibraryTone.Online, enabled.densitySlider.valueTone)
        assertEquals(1f, enabled.densitySlider.valueAlpha)
        assertEquals(SourceLibraryTone.Online, enabled.densitySlider.thumbTone)
        assertEquals(SourceLibraryTone.Online, enabled.densitySlider.activeTrackTone)
        assertNull(enabled.densitySlider.inactiveTrackTone)
        assertEquals(0.22f, enabled.densitySlider.inactiveTrackAlpha)
        assertEquals("透明度", enabled.alphaSlider.title)
        assertEquals("76%", enabled.alphaSlider.valueText)
        assertEquals(0.76f, enabled.alphaSlider.value)
        assertEquals(0.35f, enabled.alphaSlider.valueRange.start)
        assertEquals(1f, enabled.alphaSlider.valueRange.endInclusive)
        assertEquals(12, enabled.alphaSlider.steps)
        assertEquals(SourceLibraryTone.Cache, enabled.alphaSlider.tone)
        assertEquals(SourceLibraryTone.Cache, enabled.alphaSlider.valueTone)
        assertEquals(SourceLibraryTone.Cache, enabled.alphaSlider.thumbTone)
        assertEquals(SourceLibraryTone.Cache, enabled.alphaSlider.activeTrackTone)
        assertEquals(0.22f, enabled.alphaSlider.inactiveTrackAlpha)
        assertEquals("字号", enabled.fontScaleSlider.title)
        assertEquals("72%", enabled.fontScaleSlider.valueText)
        assertEquals(0.72f, enabled.fontScaleSlider.value)
        assertEquals(0.62f, enabled.fontScaleSlider.valueRange.start)
        assertEquals(1.08f, enabled.fontScaleSlider.valueRange.endInclusive)
        assertEquals(8, enabled.fontScaleSlider.steps)
        assertEquals(SourceLibraryTone.Backup, enabled.fontScaleSlider.tone)
        assertEquals(SourceLibraryTone.Backup, enabled.fontScaleSlider.valueTone)
        assertEquals(SourceLibraryTone.Backup, enabled.fontScaleSlider.thumbTone)
        assertEquals(SourceLibraryTone.Backup, enabled.fontScaleSlider.activeTrackTone)
        assertEquals(30.dp, enabled.fontScaleSlider.sliderHeight)
        assertEquals("弹幕源待校准", enabled.mapping.title)
        assertEquals("搜索弹幕", enabled.mapping.actionLabel)
        assertEquals(listOf("自动匹配"), enabled.mapping.badges.map { it.label })
        assertTrue(enabled.mapping.actionEnabled)
        assertFalse(enabled.mapping.selected)
        assertFalse(enabled.mapping.prominent)
        assertEquals(SourceLibraryTone.Muted, enabled.mapping.trailingTone)
        assertEquals(48.dp, enabled.mapping.rowState.minHeight)
        assertTrue(enabled.safetySummary.contains("避让"))
        assertTrue(enabled.safetySummary.contains("\u4e2d0"))
        assertEquals(SourceLibraryTone.Primary, enabled.tone)
        assertEquals("弹幕已关闭", disabled.toggleTitle)
        assertEquals("点击开启弹幕显示", disabled.toggleSubtitle)
        assertEquals("开启", disabled.toggleActionLabel)
        assertEquals(listOf("已隐藏"), disabled.toggleBadges.map { it.label })
        assertFalse(disabled.toggleHighlighted)
        assertFalse(disabled.toggleProminent)
        assertTrue(disabled.toggleActionEnabled)
        assertEquals(0.5f, disabled.toggleIconAlpha)
        assertEquals(0.76f, disabled.toggleTitleAlpha)
        assertEquals(0.68f, disabled.toggleSubtitleAlpha)
        assertNull(disabled.toggleRowState.containerTone)
        assertEquals(0.06f, disabled.toggleRowState.containerAlpha)
        assertNull(disabled.toggleRowState.borderTone)
        assertEquals(0.08f, disabled.toggleRowState.borderAlpha)
        assertNull(disabled.toggleRowState.iconTone)
        assertEquals("30%", disabled.densitySlider.valueText)
        assertEquals(0.3f, disabled.densitySlider.value)
        assertEquals("100%", disabled.alphaSlider.valueText)
        assertEquals(1f, disabled.alphaSlider.value)
        assertEquals("108%", disabled.fontScaleSlider.valueText)
        assertEquals(1.08f, disabled.fontScaleSlider.value)
        assertEquals(SourceLibraryTone.Muted, disabled.tone)
    }

    @Test
    fun playerDanmakuMappingUiStateSummarizesAutomaticAndManualMatches() {
        val automatic = buildPlayerDanmakuSettingsUiState(
            enabled = true,
            density = 0.6f,
            alpha = 0.8f,
            fontScale = 0.7f,
            matches = listOf(
                danmakuMatch("danmaku-bilibili", score = 90),
                danmakuMatch("danmaku-tencent", score = 80),
            ),
            matching = false,
            timelineCount = 345,
        ).mapping
        val manual = buildPlayerDanmakuSettingsUiState(
            enabled = true,
            density = 0.6f,
            alpha = 0.8f,
            fontScale = 0.7f,
            matches = listOf(
                danmakuMatch("danmaku-bilibili", source = DanmakuMatchSource.Manual, score = 100_000),
                danmakuMatch("danmaku-tencent", score = 80),
            ),
            matching = false,
            timelineCount = 120,
        ).mapping
        val loading = buildPlayerDanmakuSettingsUiState(
            enabled = true,
            density = 0.6f,
            alpha = 0.8f,
            fontScale = 0.7f,
            matching = true,
        ).mapping

        assertEquals("弹幕自动匹配", automatic.title)
        assertEquals("手动校准", automatic.actionLabel)
        assertEquals(listOf("自动匹配", "2 候选", "345 条"), automatic.badges.map { it.label })
        assertTrue(automatic.subtitle.contains("345"))
        assertEquals(SourceLibraryTone.Cache, automatic.trailingTone)
        assertTrue(automatic.highlighted)
        assertFalse(automatic.prominent)
        assertEquals("弹幕候选", automatic.candidateListTitle)
        assertEquals(8.dp, automatic.candidateSpacing)
        assertEquals(listOf("Bilibili", "腾讯视频"), automatic.candidates.map { it.title })
        assertEquals("Episode 1 · 评分 90", automatic.candidates.first().subtitle)
        assertEquals("设为本集", automatic.candidates.first().actionLabel)
        assertEquals(listOf("候选", "B站"), automatic.candidates.first().badges.map { it.label })
        assertFalse(automatic.candidates.first().selected)
        assertTrue(automatic.candidates.first().enabled)
        assertTrue(automatic.candidates.first().actionEnabled)

        assertEquals("弹幕映射已校准", manual.title)
        assertEquals("重新校准", manual.actionLabel)
        assertEquals(listOf("人工校准", "2 候选", "120 条"), manual.badges.map { it.label })
        assertTrue(manual.selected)
        assertTrue(manual.prominent)
        assertEquals(SourceLibraryTone.Primary, manual.trailingTone)
        assertEquals(SourceLibraryTone.Primary, manual.rowState.containerTone)
        assertEquals("已校准", manual.candidates.first().actionLabel)
        assertEquals(listOf("人工", "B站"), manual.candidates.first().badges.map { it.label })
        assertTrue(manual.candidates.first().selected)
        assertFalse(manual.candidates.first().enabled)
        assertFalse(manual.candidates.first().actionEnabled)
        assertEquals(SourceLibraryTone.Primary, manual.candidates.first().rowState.containerTone)

        assertEquals("正在匹配弹幕", loading.title)
        assertEquals("匹配中", loading.actionLabel)
        assertFalse(loading.actionEnabled)
        assertEquals(SourceLibraryTone.Online, loading.trailingTone)
        assertTrue(loading.highlighted)
    }

    @Test
    fun playerSelectableRowUiStateSuppressesAccentWhenDisabled() {
        val state = buildPlayerSelectableRowUiState(
            enabled = false,
            highlighted = true,
            prominent = true,
        )

        assertNull(state.containerTone)
        assertEquals(0.06f, state.containerAlpha)
        assertNull(state.borderTone)
        assertEquals(0.08f, state.borderAlpha)
        assertNull(state.iconTone)
        assertEquals(48.dp, state.minHeight)
        assertEquals(8.dp, state.cornerRadius)
    }

    @Test
    fun playerQualityPanelUiStateGroupsQualityAndMarksCurrent() {
        val current = route("hls-1080", StreamProtocol.HLS, 900, quality = "1080p", sourceName = "Online A")
        val lower720 = route("hls-720-low", StreamProtocol.HLS, 100, quality = "720p", sourceName = "Online A")
        val better720 = route("hls-720-better", StreamProtocol.HLS, 500, quality = "720p", sourceName = "Online B")
        val bt = route("bt-4k", StreamProtocol.BITTORRENT, 300, quality = "4K", sourceName = "BT")

        val state = buildPlayerQualityPanelUiState(
            routes = listOf(lower720, current, bt, better720),
            currentStream = current.stream,
        )

        assertTrue(state.hasOptions)
        assertEquals("1080p", state.currentQualityLabel)
        assertEquals("当前 1080p · 3 档可选", state.summary)
        assertEquals(listOf("1080p", "720p", "4K"), state.options.map { it.title })
        assertEquals("hls-720-better", state.options.first { it.title == "720p" }.route.stream.id)
        val currentOption = state.options.first { it.title == "1080p" }
        val btOption = state.options.first { it.title == "4K" }
        assertTrue(currentOption.selected)
        assertEquals("使用中", currentOption.actionLabel)
        assertEquals(listOf(currentOption.actionLabel), currentOption.badges.map { it.label })
        assertTrue(currentOption.highlighted)
        assertTrue(currentOption.prominent)
        assertTrue(currentOption.enabled)
        assertTrue(currentOption.actionEnabled)
        assertEquals(1f, currentOption.iconAlpha)
        assertEquals(0.94f, currentOption.titleAlpha)
        assertEquals(0.86f, currentOption.subtitleAlpha)
        assertEquals(SourceLibraryTone.Primary, currentOption.rowState.containerTone)
        assertEquals(0.18f, currentOption.rowState.containerAlpha)
        assertEquals(SourceLibraryTone.Primary, currentOption.rowState.borderTone)
        assertEquals(0.72f, currentOption.rowState.borderAlpha)
        assertEquals(19.dp, currentOption.rowState.iconSize)
        assertEquals(SourceLibraryTone.Muted, currentOption.rowState.subtitleTone)
        assertEquals(SourceLibraryTone.Primary, currentOption.tone)
        assertEquals(SourceLibraryTone.Backup, btOption.tone)
        assertEquals(listOf(StreamProtocol.BITTORRENT.uiProtocolName()), btOption.badges.map { it.label })
        assertFalse(btOption.highlighted)
        assertFalse(btOption.prominent)
        assertEquals(0.76f, btOption.iconAlpha)
        assertNull(btOption.rowState.containerTone)
        assertEquals(0.06f, btOption.rowState.containerAlpha)
        assertNull(btOption.rowState.borderTone)
        assertEquals(0.08f, btOption.rowState.borderAlpha)
    }

    @Test
    fun playerQualityPanelUiStateExplainsAutoAndEmptyOptions() {
        val auto = route("auto", StreamProtocol.HLS, 100, quality = "auto")

        val state = buildPlayerQualityPanelUiState(
            routes = listOf(auto),
            currentStream = auto.stream,
        )
        val empty = buildPlayerQualityPanelUiState(
            routes = emptyList(),
            currentStream = auto.stream,
        )

        assertEquals("自动", state.currentQualityLabel)
        assertEquals("自动", state.options.single().title)
        assertTrue(state.options.single().selected)
        assertFalse(empty.hasOptions)
        assertEquals("当前播放源没有提供可切换清晰度", empty.emptyText)
        assertEquals("当前播放源没有提供可切换清晰度", empty.summary)
    }

    @Test
    fun playerSpeedPanelUiStateFormatsOptionsAndSelection() {
        val state = buildPlayerSpeedPanelUiState(
            playbackSpeed = 1.25f,
            speeds = listOf(2f, 1f, 0.5f, 1.25f, 1f),
        )

        assertEquals("当前 1.25x · 4 档可选", state.summary)
        assertEquals(listOf(0.5f, 1f, 1.25f, 2f), state.options.map { it.speed })
        assertEquals(listOf("0.5x", "1.0x", "1.25x", "2.0x"), state.options.map { it.title })
        assertEquals("慢速回看", state.options.first { it.speed == 0.5f }.subtitle)
        assertEquals("标准速度", state.options.first { it.speed == 1f }.subtitle)
        assertEquals("快速播放", state.options.first { it.speed == 2f }.subtitle)
        val selectedSpeed = state.options.first { it.speed == 1.25f }
        val fastSpeed = state.options.first { it.speed == 2f }
        assertTrue(selectedSpeed.selected)
        assertEquals("使用中", selectedSpeed.actionLabel)
        assertEquals(listOf(selectedSpeed.actionLabel), selectedSpeed.badges.map { it.label })
        assertTrue(selectedSpeed.highlighted)
        assertTrue(selectedSpeed.prominent)
        assertTrue(selectedSpeed.enabled)
        assertTrue(selectedSpeed.actionEnabled)
        assertEquals(1f, selectedSpeed.iconAlpha)
        assertEquals(0.94f, selectedSpeed.titleAlpha)
        assertEquals(0.86f, selectedSpeed.subtitleAlpha)
        assertEquals(SourceLibraryTone.Primary, selectedSpeed.rowState.containerTone)
        assertEquals(SourceLibraryTone.Primary, selectedSpeed.rowState.borderTone)
        assertEquals(0.18f, selectedSpeed.rowState.containerAlpha)
        assertEquals(0.72f, selectedSpeed.rowState.borderAlpha)
        assertEquals(SourceLibraryTone.Primary, selectedSpeed.tone)
        assertEquals("切换", fastSpeed.actionLabel)
        assertTrue(fastSpeed.badges.isEmpty())
        assertFalse(fastSpeed.highlighted)
        assertFalse(fastSpeed.prominent)
        assertEquals(0.76f, fastSpeed.iconAlpha)
        assertNull(fastSpeed.rowState.containerTone)
        assertEquals(0.06f, fastSpeed.rowState.containerAlpha)
    }

    @Test
    fun playerMorePanelUiStateBuildsSummaryAndActionGrid() {
        val state = buildPlayerMorePanelUiState(
            routeCount = 3,
            routeCoverageLabel = "在线 2 · BT 1",
            episodeCount = 12,
            routeLabel = "Animeko",
            quality = "1080p",
            playbackSpeed = 1.5f,
            danmakuEnabled = true,
            cacheAction = PlayerCacheActionUiState(
                enabled = true,
                title = "缓存",
                value = "可离线",
                reason = "Media3 离线缓存队列",
                actionLabel = "缓存本集",
            ),
        )

        assertEquals("当前设置", state.summaryBadge)
        assertEquals("清晰度 1080p · 倍速 1.5x", state.summaryPrimary)
        assertEquals("当前源 Animeko · 在线 2 · BT 1 · 12 集", state.summarySecondary)
        assertEquals(
            listOf(
                PlayerMoreActionKind.Quality,
                PlayerMoreActionKind.Speed,
                PlayerMoreActionKind.Episode,
                PlayerMoreActionKind.Route,
                PlayerMoreActionKind.Danmaku,
                PlayerMoreActionKind.Cache,
            ),
            state.actions.map { it.kind },
        )
        assertTrue(state.actions.first { it.kind == PlayerMoreActionKind.Episode }.enabled)
        assertEquals("共 12 集", state.actions.first { it.kind == PlayerMoreActionKind.Episode }.subtitle)
        assertEquals("在线 2 · BT 1", state.actions.first { it.kind == PlayerMoreActionKind.Route }.subtitle)
        assertTrue(state.actions.first { it.kind == PlayerMoreActionKind.Danmaku }.selected)
        assertEquals("已开启", state.actions.first { it.kind == PlayerMoreActionKind.Danmaku }.subtitle)
        assertEquals("缓存本集", state.actions.first { it.kind == PlayerMoreActionKind.Cache }.subtitle)
        assertEquals(SourceLibraryTone.Primary, state.actions.first { it.kind == PlayerMoreActionKind.Cache }.tone)
        val danmaku = state.actions.first { it.kind == PlayerMoreActionKind.Danmaku }
        assertTrue(danmaku.highlighted)
        assertTrue(danmaku.prominent)
        assertTrue(danmaku.actionEnabled)
        assertEquals(1f, danmaku.tileAlpha)
        assertEquals(0.16f, danmaku.containerAlpha)
        assertEquals(0.62f, danmaku.borderAlpha)
        assertEquals(0.18f, danmaku.iconContainerAlpha)
        assertEquals(1f, danmaku.iconAlpha)
        assertEquals(1f, danmaku.titleAlpha)
        assertEquals(0.56f, danmaku.subtitleAlpha)
        val cache = state.actions.first { it.kind == PlayerMoreActionKind.Cache }
        assertFalse(cache.highlighted)
        assertFalse(cache.prominent)
        assertTrue(cache.actionEnabled)
        assertEquals(0.08f, cache.containerAlpha)
        assertEquals(0.06f, cache.borderAlpha)
        assertEquals(0.88f, cache.iconAlpha)
    }

    @Test
    fun playerMorePanelUiStateDisablesUnavailableActions() {
        val state = buildPlayerMorePanelUiState(
            routeCount = 1,
            routeCoverageLabel = "",
            episodeCount = 0,
            routeLabel = "",
            quality = "",
            playbackSpeed = 1f,
            danmakuEnabled = false,
            cacheAction = PlayerCacheActionUiState(
                enabled = false,
                title = "缓存",
                value = "嗅探",
                reason = "网页嗅探源需现场播放，暂不支持离线",
                actionLabel = "不可缓存",
            ),
        )

        assertEquals("清晰度 自动 · 倍速 1.0x", state.summaryPrimary)
        assertEquals("当前源 自动推荐 · 自动推荐 · 1 集", state.summarySecondary)
        assertFalse(state.actions.first { it.kind == PlayerMoreActionKind.Episode }.enabled)
        assertEquals("共 1 集", state.actions.first { it.kind == PlayerMoreActionKind.Episode }.subtitle)
        assertFalse(state.actions.first { it.kind == PlayerMoreActionKind.Route }.enabled)
        assertEquals("自动推荐", state.actions.first { it.kind == PlayerMoreActionKind.Route }.subtitle)
        assertFalse(state.actions.first { it.kind == PlayerMoreActionKind.Danmaku }.selected)
        assertEquals("已关闭", state.actions.first { it.kind == PlayerMoreActionKind.Danmaku }.subtitle)
        assertFalse(state.actions.first { it.kind == PlayerMoreActionKind.Cache }.enabled)
        assertEquals("网页嗅探源需现场播放，暂不支持离线", state.actions.first { it.kind == PlayerMoreActionKind.Cache }.subtitle)
        assertEquals(SourceLibraryTone.Muted, state.actions.first { it.kind == PlayerMoreActionKind.Cache }.tone)
        val disabledRoute = state.actions.first { it.kind == PlayerMoreActionKind.Route }
        assertFalse(disabledRoute.highlighted)
        assertFalse(disabledRoute.prominent)
        assertFalse(disabledRoute.actionEnabled)
        assertEquals(0.42f, disabledRoute.tileAlpha)
        assertEquals(0.52f, disabledRoute.titleAlpha)
        assertEquals(0.38f, disabledRoute.subtitleAlpha)
        val disabledCache = state.actions.first { it.kind == PlayerMoreActionKind.Cache }
        assertFalse(disabledCache.actionEnabled)
        assertEquals(0.42f, disabledCache.tileAlpha)
        assertEquals(0.38f, disabledCache.subtitleAlpha)
    }

    @Test
    fun playerFullscreenSideDockUiStateBuildsOrderedDockActions() {
        val rich = buildPlayerFullscreenSideDockUiState(
            danmakuEnabled = true,
            routeCount = 3,
            episodeCount = 12,
        )
        val limited = buildPlayerFullscreenSideDockUiState(
            danmakuEnabled = false,
            routeCount = 1,
            episodeCount = 1,
        )

        assertEquals(56.dp, rich.width)
        assertEquals(8.dp, rich.cornerRadius)
        assertEquals(0.34f, rich.containerAlpha)
        assertEquals(0.08f, rich.borderAlpha)
        assertEquals(6.dp, rich.verticalPadding)
        assertEquals(4.dp, rich.actionSpacing)
        assertEquals(
            listOf(
                PlayerMoreActionKind.Danmaku,
                PlayerMoreActionKind.Quality,
                PlayerMoreActionKind.Speed,
                PlayerMoreActionKind.Episode,
                PlayerMoreActionKind.Route,
                PlayerMoreActionKind.More,
            ),
            rich.actions.map { it.kind },
        )
        val danmaku = rich.actions.first { it.kind == PlayerMoreActionKind.Danmaku }
        assertEquals("弹幕开", danmaku.label)
        assertTrue(danmaku.selected)
        assertTrue(danmaku.enabled)
        assertEquals(SourceLibraryTone.Primary, danmaku.tone)
        assertEquals(48.dp, danmaku.width)
        assertEquals(48.dp, danmaku.height)
        assertEquals(8.dp, danmaku.cornerRadius)
        assertEquals(18.dp, danmaku.iconSize)
        assertEquals(3.dp, danmaku.contentSpacing)
        assertEquals(SourceLibraryTone.Primary, danmaku.containerTone)
        assertEquals(0.16f, danmaku.containerAlpha)
        assertEquals(SourceLibraryTone.Primary, danmaku.contentTone)
        assertEquals(1f, danmaku.contentAlpha)
        assertNull(danmaku.disabledContainerTone)
        assertEquals(0f, danmaku.disabledContainerAlpha)
        assertNull(danmaku.disabledContentTone)
        assertEquals(0.32f, danmaku.disabledContentAlpha)
        assertEquals("选集", rich.actions.first { it.kind == PlayerMoreActionKind.Episode }.label)
        assertTrue(rich.actions.first { it.kind == PlayerMoreActionKind.Episode }.enabled)
        assertTrue(rich.actions.first { it.kind == PlayerMoreActionKind.Route }.enabled)
        assertEquals("更多", rich.actions.last().label)

        val disabledDanmaku = limited.actions.first { it.kind == PlayerMoreActionKind.Danmaku }
        assertEquals("弹幕关", disabledDanmaku.label)
        assertFalse(disabledDanmaku.selected)
        assertTrue(disabledDanmaku.enabled)
        assertEquals(SourceLibraryTone.Muted, disabledDanmaku.tone)
        assertNull(disabledDanmaku.containerTone)
        assertEquals(0f, disabledDanmaku.containerAlpha)
        assertNull(disabledDanmaku.contentTone)
        assertEquals(0.82f, disabledDanmaku.contentAlpha)
        assertFalse(limited.actions.first { it.kind == PlayerMoreActionKind.Episode }.enabled)
        assertFalse(limited.actions.first { it.kind == PlayerMoreActionKind.Route }.enabled)
    }

    @Test
    fun playerPanelSheetUiStateBuildsHeaderContextAndTabs() {
        val state = buildPlayerPanelSheetUiState(
            selectedPanel = PlayerPanelKind.Route,
            title = "番剧标题",
            episodeIndex = 8,
            sourceLabel = "Animeko",
            quality = "1080p",
            playbackSpeed = 1.25f,
            routeCount = 4,
            routeCoverageLabel = "在线 3 · BT 1",
            episodeCount = 12,
            danmakuEnabled = true,
        )

        assertEquals("播放源", state.title)
        assertEquals("推荐优先 · 手动换源", state.subtitle)
        assertEquals("收起", state.dismissLabel)
        assertEquals(1f, state.titleAlpha)
        assertEquals(0.56f, state.subtitleAlpha)
        assertEquals(0.82f, state.dismissLabelAlpha)
        assertEquals(12.dp, state.headerSpacing)
        assertEquals(4.dp, state.headerTextSpacing)
        assertEquals(34.dp, state.dismissButtonHeight)
        assertEquals("番剧标题", state.context.title)
        assertEquals("第 8 集 · Animeko · 1080p · 1.25x", state.context.metadata)
        assertEquals("播放中", state.context.statusLabel)
        assertEquals(SourceLibraryTone.Cache, state.context.statusTone)
        assertEquals(SourceLibraryTone.Primary, state.context.iconTone)
        assertEquals(0.06f, state.context.containerAlpha)
        assertEquals(0.08f, state.context.borderAlpha)
        assertEquals(0.18f, state.context.iconContainerAlpha)
        assertEquals(1f, state.context.titleAlpha)
        assertEquals(1f, state.context.metadataAlpha)
        assertEquals(8.dp, state.context.cornerRadius)
        assertEquals(11.dp, state.context.horizontalPadding)
        assertEquals(9.dp, state.context.verticalPadding)
        assertEquals(10.dp, state.context.rowSpacing)
        assertEquals(32.dp, state.context.iconBoxSize)
        assertEquals(18.dp, state.context.iconSize)
        assertEquals(8.dp, state.context.iconCornerRadius)
        assertEquals(3.dp, state.context.textSpacing)
        assertEquals(38.dp, state.tabStrip.height)
        assertEquals(7.dp, state.tabStrip.itemSpacing)
        assertEquals(1.dp, state.tabStrip.contentPaddingHorizontal)
        assertEquals(
            listOf(
                PlayerPanelKind.Quality,
                PlayerPanelKind.Speed,
                PlayerPanelKind.Route,
                PlayerPanelKind.Episode,
                PlayerPanelKind.Danmaku,
                PlayerPanelKind.More,
            ),
            state.tabs.map { it.kind },
        )
        assertTrue(state.tabs.first { it.kind == PlayerPanelKind.Route }.selected)
        assertEquals("在线 3 · BT 1", state.tabs.first { it.kind == PlayerPanelKind.Route }.value)
        assertTrue(state.tabs.first { it.kind == PlayerPanelKind.Route }.enabled)
        val routeTab = state.tabs.first { it.kind == PlayerPanelKind.Route }
        assertTrue(routeTab.actionEnabled)
        assertTrue(routeTab.prominent)
        assertTrue(routeTab.usesVisualTone)
        assertEquals(SourceLibraryTone.Primary, routeTab.visualTone)
        assertEquals(0.18f, routeTab.containerAlpha)
        assertEquals(1f, routeTab.contentAlpha)
        assertEquals(0.76f, routeTab.valueAlpha)
        assertEquals(86.dp, routeTab.width)
        assertEquals(36.dp, routeTab.height)
        assertEquals(999.dp, routeTab.cornerRadius)
        assertEquals(8.dp, routeTab.horizontalPadding)
        assertEquals(0.dp, routeTab.verticalPadding)
        assertEquals(4.dp, routeTab.contentSpacing)
        assertEquals(15.dp, routeTab.iconSize)
        assertEquals("12集", state.tabs.first { it.kind == PlayerPanelKind.Episode }.value)
        assertTrue(state.tabs.first { it.kind == PlayerPanelKind.Danmaku }.highlighted)
        assertEquals("开", state.tabs.first { it.kind == PlayerPanelKind.Danmaku }.value)
        assertEquals(SourceLibraryTone.Primary, state.tabs.first { it.kind == PlayerPanelKind.Danmaku }.tone)
        val danmakuTab = state.tabs.first { it.kind == PlayerPanelKind.Danmaku }
        assertTrue(danmakuTab.actionEnabled)
        assertFalse(danmakuTab.prominent)
        assertTrue(danmakuTab.usesVisualTone)
        assertEquals(SourceLibraryTone.Online, danmakuTab.visualTone)
        assertEquals(0.06f, danmakuTab.containerAlpha)
        assertEquals(1f, danmakuTab.contentAlpha)
        assertEquals(0.76f, danmakuTab.valueAlpha)
    }

    @Test
    fun playerPanelSheetUiStateNormalizesUnavailableTabs() {
        val state = buildPlayerPanelSheetUiState(
            selectedPanel = PlayerPanelKind.More,
            title = "",
            episodeIndex = null,
            sourceLabel = "",
            quality = "",
            playbackSpeed = 1f,
            routeCount = 1,
            routeCoverageLabel = "",
            episodeCount = 0,
            danmakuEnabled = false,
        )

        assertEquals("播放设置", state.title)
        assertEquals("清晰度 · 倍速 · 选集 · 换源", state.subtitle)
        assertEquals("收起", state.dismissLabel)
        assertEquals(1f, state.titleAlpha)
        assertEquals(0.56f, state.subtitleAlpha)
        assertEquals(0.82f, state.dismissLabelAlpha)
        assertEquals(12.dp, state.headerSpacing)
        assertEquals(4.dp, state.headerTextSpacing)
        assertEquals(34.dp, state.dismissButtonHeight)
        assertEquals("正在播放", state.context.title)
        assertEquals("当前集 · 自动源 · 自动 · 1.0x", state.context.metadata)
        assertEquals(SourceLibraryTone.Cache, state.context.statusTone)
        assertEquals(SourceLibraryTone.Primary, state.context.iconTone)
        assertEquals(0.06f, state.context.containerAlpha)
        assertEquals(0.08f, state.context.borderAlpha)
        assertEquals(1f, state.context.titleAlpha)
        assertEquals(1f, state.context.metadataAlpha)
        assertTrue(state.tabs.first { it.kind == PlayerPanelKind.More }.selected)
        val moreTab = state.tabs.first { it.kind == PlayerPanelKind.More }
        assertTrue(moreTab.actionEnabled)
        assertTrue(moreTab.prominent)
        assertTrue(moreTab.usesVisualTone)
        assertEquals(SourceLibraryTone.Primary, moreTab.visualTone)
        assertEquals(0.18f, moreTab.containerAlpha)
        assertEquals(1f, moreTab.contentAlpha)
        assertFalse(state.tabs.first { it.kind == PlayerPanelKind.Route }.enabled)
        assertEquals("自动", state.tabs.first { it.kind == PlayerPanelKind.Route }.value)
        val routeTab = state.tabs.first { it.kind == PlayerPanelKind.Route }
        assertFalse(routeTab.actionEnabled)
        assertFalse(routeTab.prominent)
        assertFalse(routeTab.usesVisualTone)
        assertEquals(0.035f, routeTab.containerAlpha)
        assertEquals(0.32f, routeTab.contentAlpha)
        assertEquals(0.48f, routeTab.valueAlpha)
        assertFalse(state.tabs.first { it.kind == PlayerPanelKind.Episode }.enabled)
        assertEquals("单集", state.tabs.first { it.kind == PlayerPanelKind.Episode }.value)
        assertFalse(state.tabs.first { it.kind == PlayerPanelKind.Danmaku }.highlighted)
        assertEquals("关", state.tabs.first { it.kind == PlayerPanelKind.Danmaku }.value)
        assertEquals(SourceLibraryTone.Muted, state.tabs.first { it.kind == PlayerPanelKind.Danmaku }.tone)
        val danmakuTab = state.tabs.first { it.kind == PlayerPanelKind.Danmaku }
        assertTrue(danmakuTab.actionEnabled)
        assertFalse(danmakuTab.usesVisualTone)
        assertEquals(0.72f, danmakuTab.contentAlpha)
        assertEquals(0.76f, danmakuTab.valueAlpha)
    }

    @Test
    fun playerPanelShellUiStateAdaptsLandscapeAndPortraitConstraints() {
        val narrowLandscape = buildPlayerPanelShellUiState(maxWidth = 600.dp, maxHeight = 360.dp)
        val wideLandscape = buildPlayerPanelShellUiState(maxWidth = 960.dp, maxHeight = 540.dp)
        val portrait = buildPlayerPanelShellUiState(maxWidth = 360.dp, maxHeight = 640.dp)
        val compactPortrait = buildPlayerPanelShellUiState(maxWidth = 360.dp, maxHeight = 400.dp)

        assertTrue(narrowLandscape.landscape)
        assertEquals(600.dp * 0.54f, narrowLandscape.panelWidth)
        assertEquals(0.14f, narrowLandscape.scrimAlpha)
        assertEquals(10.dp, narrowLandscape.landscapeEndPadding)
        assertEquals(14.dp, narrowLandscape.landscapeTopPadding)
        assertEquals(8.dp, narrowLandscape.bottomStartRadius)
        assertEquals(8.dp, narrowLandscape.bottomEndRadius)
        assertEquals(392.dp, wideLandscape.panelWidth)
        assertFalse(portrait.landscape)
        assertEquals(360.dp, portrait.panelWidth)
        assertEquals(640.dp * 0.58f, portrait.portraitPanelHeight)
        assertEquals(320.dp, portrait.portraitPanelMinHeight)
        assertEquals(0.32f, portrait.scrimAlpha)
        assertEquals(8.dp, portrait.topStartRadius)
        assertEquals(8.dp, portrait.topEndRadius)
        assertEquals(0.dp, portrait.bottomStartRadius)
        assertEquals(0.dp, portrait.bottomEndRadius)
        assertEquals(400.dp * 0.72f, compactPortrait.portraitPanelHeight)
        assertEquals(400.dp * 0.66f, compactPortrait.portraitPanelMinHeight)
        assertEquals(0xF217171C.toInt(), portrait.surfaceColorArgb)
        assertEquals(0.08f, portrait.borderAlpha)
        assertEquals(16.dp, portrait.contentPadding)
        assertEquals(12.dp, portrait.contentSpacing)
    }

    @Test
    fun playerTopStatusStripUiStateBuildsStableChips() {
        val overlay = PlayerOverlayState(
            title = "标题",
            episodeTitle = "第 3 集",
            sourceLabel = "Animeko",
            qualityLabel = "1080p",
            routeLabel = "Animeko · HLS",
            playbackState = "播放中",
            statusLabel = "播放中",
            notice = null,
            error = null,
        )

        val state = buildPlayerTopStatusStripUiState(
            overlayState = overlay,
            episodeValue = "3/12",
            routeCount = 4,
            routeCoverageLabel = "在线 3 · BT 1",
            playbackSpeed = 1.5f,
        )

        assertEquals(listOf("本集", "来源", "清晰度", "倍速"), state.chips.map { it.label })
        assertEquals("3/12", state.chips.first { it.label == "本集" }.value)
        assertEquals("Animeko · 在线 3 · BT 1", state.chips.first { it.label == "来源" }.value)
        assertEquals("1080p", state.chips.first { it.label == "清晰度" }.value)
        assertEquals("1.5x", state.chips.first { it.label == "倍速" }.value)
        assertEquals(SourceLibraryTone.Cache, state.chips.first { it.label == "倍速" }.tone)
        assertEquals(32.dp, state.stripHeight)
        assertEquals(7.dp, state.itemSpacing)
        assertEquals(2.dp, state.endPadding)
        val episodeChip = state.chips.first()
        assertEquals(30.dp, episodeChip.height)
        assertEquals(88.dp, episodeChip.minWidth)
        assertEquals(210.dp, episodeChip.maxWidth)
        assertEquals(999.dp, episodeChip.cornerRadius)
        assertEquals(PlayerChromeBaseColor.Black, episodeChip.containerBaseColor)
        assertEquals(0.28f, episodeChip.containerAlpha, 0.001f)
        assertEquals(1.dp, episodeChip.borderWidth)
        assertEquals(0.2f, episodeChip.borderAlpha, 0.001f)
        assertEquals(10.dp, episodeChip.horizontalPadding)
        assertEquals(5.dp, episodeChip.contentSpacing)
        assertEquals(1f, episodeChip.labelAlpha, 0.001f)
        assertEquals(PlayerChromeBaseColor.White, episodeChip.valueBaseColor)
        assertEquals(0.86f, episodeChip.valueAlpha, 0.001f)
    }

    @Test
    fun playerTopOverlayUiStateBuildsHeaderRouteAndStatusStrip() {
        val overlay = PlayerOverlayState(
            title = "标题",
            episodeTitle = "第 3 集",
            sourceLabel = "Animeko",
            qualityLabel = "1080p",
            routeLabel = "Animeko · HLS",
            playbackState = "播放中",
            statusLabel = "播放中",
            notice = null,
            error = null,
        )

        val state = buildPlayerTopOverlayUiState(
            overlayState = overlay,
            currentEpisode = episode(id = "ep-3", index = 3),
            episodeCount = 12,
            routeCount = 4,
            routeCoverageLabel = "在线 3 · BT 1",
            playbackSpeed = 1.25f,
        )

        assertEquals("标题", state.title)
        assertEquals("第 3 集 · 播放中", state.subtitle)
        assertNull(state.compactNotice)
        assertEquals("Animeko · HLS", state.routeStatus.routeLabel)
        assertEquals("3/12", state.statusStrip.chips.first { it.label == "本集" }.value)
        assertEquals("1.25x", state.statusStrip.chips.first { it.label == "倍速" }.value)
    }

    @Test
    fun playerTopOverlayUiStateNormalizesFallbackTitleAndNotice() {
        val overlay = PlayerOverlayState(
            title = "",
            episodeTitle = "",
            sourceLabel = "",
            qualityLabel = "",
            routeLabel = "",
            playbackState = "",
            statusLabel = "",
            notice = "正在切换线路",
            error = null,
        )
        val current = Episode(
            providerId = "provider",
            id = "special",
            title = "",
            url = "https://example.invalid/special",
            index = null,
        )

        val state = buildPlayerTopOverlayUiState(
            overlayState = overlay,
            currentEpisode = current,
            episodeCount = 1,
            routeCount = 1,
            routeCoverageLabel = "",
            playbackSpeed = 1f,
        )

        assertEquals("正在播放", state.title)
        assertEquals("当前集", state.subtitle)
        assertEquals("正在切换线路", checkNotNull(state.compactNotice).message)
        assertEquals("当前集", state.statusStrip.chips.first { it.label == "本集" }.value)
        assertEquals("自动源", state.statusStrip.chips.first { it.label == "来源" }.value)
    }

    @Test
    fun playerStatusStripUiStateNormalizesFallbacksAndIssues() {
        val top = buildPlayerTopStatusStripUiState(
            overlayState = PlayerOverlayState(
                title = "",
                episodeTitle = "",
                sourceLabel = "",
                qualityLabel = "",
                routeLabel = "",
                playbackState = "缓冲中",
                statusLabel = "缓冲中",
                notice = null,
                error = null,
            ),
            episodeValue = "",
            routeCount = 1,
            routeCoverageLabel = "",
            playbackSpeed = 1f,
        )
        val fullscreen = buildPlayerFullscreenStatusStripUiState(
            routeSummary = "",
            quality = "",
            routeCount = 1,
            routeCoverageLabel = "",
            episodeCount = 1,
            playbackSpeed = 1f,
            hasPlaybackIssue = true,
        )

        assertEquals("当前集", top.chips.first { it.label == "本集" }.value)
        assertEquals("自动源", top.chips.first { it.label == "来源" }.value)
        assertEquals("自动", top.chips.first { it.label == "清晰度" }.value)
        assertEquals("播放异常", fullscreen.statusLabel)
        assertEquals("自动线路", fullscreen.routeSummary)
        assertEquals(listOf("自动", "1.0x"), fullscreen.tags.map { it.label })
        assertTrue(fullscreen.error)
        assertEquals(32.dp, fullscreen.height)
        assertEquals(8.dp, fullscreen.cornerRadius)
        assertEquals(PlayerChromeBaseColor.Black, fullscreen.containerBaseColor)
        assertEquals(0.26f, fullscreen.containerAlpha, 0.001f)
        assertEquals(1.dp, fullscreen.borderWidth)
        assertEquals(PlayerChromeBaseColor.White, fullscreen.borderBaseColor)
        assertEquals(0.08f, fullscreen.borderAlpha, 0.001f)
        assertEquals(0.28f, fullscreen.errorBorderAlpha, 0.001f)
        assertEquals(10.dp, fullscreen.horizontalPadding)
        assertEquals(8.dp, fullscreen.itemSpacing)
        assertEquals(SourceLibraryTone.Cache, fullscreen.statusTone)
        assertEquals(PlayerChromeBaseColor.White, fullscreen.routeTextBaseColor)
        assertEquals(0.86f, fullscreen.routeTextAlpha, 0.001f)
        assertEquals(PlayerChromeBaseColor.White, fullscreen.tags.first().textBaseColor)
        assertEquals(0.62f, fullscreen.tags.first().textAlpha, 0.001f)
    }

    @Test
    fun playerActionBarUiStateBuildsDefaultPlaybackActions() {
        val state = buildPlayerActionBarUiState(
            quality = "1080p",
            routeCount = 3,
            routeCoverageLabel = "在线 2 · BT 1",
            episodeCount = 12,
            nextEpisode = Episode(
                providerId = "animeko",
                id = "ep-6",
                title = "下一话",
                url = "https://example.invalid/ep6",
                index = 6,
            ),
            playbackSpeed = 1.25f,
            activePanel = PlayerPanelKind.Speed,
            cacheAction = PlayerCacheActionUiState(
                enabled = true,
                title = "缓存",
                value = "可离线",
                reason = "Media3 离线缓存队列",
                actionLabel = "缓存本集",
            ),
            hasPlaybackIssue = false,
            canSelectNextRoute = true,
        )

        assertEquals(
            listOf(
                PlayerActionKind.Quality,
                PlayerActionKind.Speed,
                PlayerActionKind.Route,
                PlayerActionKind.Episode,
                PlayerActionKind.NextEpisode,
                PlayerActionKind.Cache,
                PlayerActionKind.More,
            ),
            state.actions.map { it.kind },
        )
        assertEquals("1080p", state.actions.first { it.kind == PlayerActionKind.Quality }.value)
        assertTrue(state.actions.first { it.kind == PlayerActionKind.Speed }.selected)
        assertEquals("1.25x", state.actions.first { it.kind == PlayerActionKind.Speed }.value)
        assertEquals("在线 2 · BT 1", state.actions.first { it.kind == PlayerActionKind.Route }.value)
        assertTrue(state.actions.first { it.kind == PlayerActionKind.Route }.enabled)
        assertEquals("12集", state.actions.first { it.kind == PlayerActionKind.Episode }.value)
        assertEquals("第6集", state.actions.first { it.kind == PlayerActionKind.NextEpisode }.value)
        assertEquals(SourceLibraryTone.Cache, state.actions.first { it.kind == PlayerActionKind.Cache }.tone)
    }

    @Test
    fun playerActionBarUiStatePrioritizesPlaybackIssueActions() {
        val state = buildPlayerActionBarUiState(
            quality = "",
            routeCount = 1,
            routeCoverageLabel = "",
            episodeCount = 1,
            nextEpisode = null,
            playbackSpeed = 1f,
            activePanel = PlayerPanelKind.More,
            cacheAction = PlayerCacheActionUiState(
                enabled = false,
                title = "缓存",
                value = "嗅探",
                reason = "网页嗅探源需现场播放，暂不支持离线",
                actionLabel = "不可缓存",
            ),
            hasPlaybackIssue = true,
            canSelectNextRoute = false,
        )

        assertEquals(PlayerActionKind.Retry, state.actions[0].kind)
        assertEquals(PlayerActionKind.NextRoute, state.actions[1].kind)
        assertTrue(state.actions[0].selected)
        assertEquals("当前", state.actions[0].value)
        assertFalse(state.actions[1].enabled)
        assertEquals("无", state.actions[1].value)
        assertEquals("自动", state.actions.first { it.kind == PlayerActionKind.Quality }.value)
        assertFalse(state.actions.first { it.kind == PlayerActionKind.Route }.enabled)
        assertEquals("自动", state.actions.first { it.kind == PlayerActionKind.Route }.value)
        assertFalse(state.actions.first { it.kind == PlayerActionKind.Episode }.enabled)
        assertEquals("单集", state.actions.first { it.kind == PlayerActionKind.Episode }.value)
        assertFalse(state.actions.first { it.kind == PlayerActionKind.Cache }.enabled)
        assertTrue(state.actions.first { it.kind == PlayerActionKind.More }.selected)
    }

    @Test
    fun playerTextActionChromeUiStateTracksSelectedAndDisabledPresentation() {
        val selected = buildPlayerTextActionChromeUiState(selected = true, enabled = true)
        val normal = buildPlayerTextActionChromeUiState(selected = false, enabled = true)
        val disabled = buildPlayerTextActionChromeUiState(selected = false, enabled = false)

        assertEquals(34.dp, selected.height)
        assertEquals(999.dp, selected.cornerRadius)
        assertEquals(10.dp, selected.horizontalPadding)
        assertEquals(0.dp, selected.verticalPadding)
        assertEquals(5.dp, selected.contentSpacing)
        assertEquals(15.dp, selected.iconSize)
        assertEquals(SourceLibraryTone.Primary, selected.containerTone)
        assertEquals(0.18f, selected.containerAlpha, 0.001f)
        assertEquals(SourceLibraryTone.Primary, selected.contentTone)
        assertEquals(1f, selected.contentAlpha, 0.001f)
        assertEquals(0.68f, selected.valueAlpha, 0.001f)
        assertNull(normal.containerTone)
        assertEquals(0.16f, normal.containerAlpha, 0.001f)
        assertNull(normal.contentTone)
        assertEquals(0.9f, normal.contentAlpha, 0.001f)
        assertEquals(0.1f, disabled.containerAlpha, 0.001f)
        assertEquals(0.34f, disabled.contentAlpha, 0.001f)
        assertEquals(0.36f, disabled.disabledButtonContentAlpha, 0.001f)
        assertEquals(0.5f, disabled.valueAlpha, 0.001f)
    }

    @Test
    fun playerCircleButtonChromeUiStateTracksProminentSelectedAndDisabledPresentation() {
        val normal = buildPlayerCircleButtonChromeUiState(selected = false, prominent = false, enabled = true)
        val selected = buildPlayerCircleButtonChromeUiState(selected = true, prominent = true, enabled = true)
        val disabled = buildPlayerCircleButtonChromeUiState(selected = false, prominent = false, enabled = false)

        assertEquals(44.dp, normal.size)
        assertEquals(22.dp, normal.iconSize)
        assertNull(normal.containerTone)
        assertEquals(PlayerChromeBaseColor.Black, normal.containerBaseColor)
        assertEquals(0.46f, normal.containerAlpha, 0.001f)
        assertNull(normal.iconTone)
        assertEquals(PlayerChromeBaseColor.White, normal.iconBaseColor)
        assertEquals(0.9f, normal.iconAlpha, 0.001f)
        assertEquals(62.dp, selected.size)
        assertEquals(34.dp, selected.iconSize)
        assertEquals(SourceLibraryTone.Primary, selected.containerTone)
        assertEquals(1f, selected.containerAlpha, 0.001f)
        assertEquals(1f, selected.iconAlpha, 0.001f)
        assertNull(disabled.containerTone)
        assertEquals(PlayerChromeBaseColor.White, disabled.containerBaseColor)
        assertEquals(0.08f, disabled.containerAlpha, 0.001f)
        assertEquals(PlayerChromeBaseColor.White, disabled.iconBaseColor)
        assertEquals(0.3f, disabled.iconAlpha, 0.001f)
    }

    @Test
    fun portraitRecoveryActionsUiStateOnlyShowsForPlaybackIssues() {
        val hidden = buildPortraitRecoveryActionsUiState(
            hasPlaybackIssue = false,
            canSelectNextRoute = true,
        )
        val canFallback = buildPortraitRecoveryActionsUiState(
            hasPlaybackIssue = true,
            canSelectNextRoute = true,
        )
        val noFallback = buildPortraitRecoveryActionsUiState(
            hasPlaybackIssue = true,
            canSelectNextRoute = false,
        )

        assertFalse(hidden.visible)
        assertTrue(hidden.actions.isEmpty())
        assertTrue(canFallback.visible)
        assertEquals(listOf(PlayerActionKind.Retry, PlayerActionKind.NextRoute), canFallback.actions.map { it.kind })
        assertEquals("重试当前", canFallback.actions[0].title)
        assertTrue(canFallback.actions[0].enabled)
        assertEquals(SourceLibraryTone.Primary, canFallback.actions[0].tone)
        assertEquals("换个源", canFallback.actions[1].title)
        assertEquals("可切", canFallback.actions[1].value)
        assertTrue(canFallback.actions[1].enabled)
        assertEquals(SourceLibraryTone.Online, canFallback.actions[1].tone)
        assertFalse(noFallback.actions[1].enabled)
        assertEquals("无", noFallback.actions[1].value)
        assertEquals(SourceLibraryTone.Muted, noFallback.actions[1].tone)
    }

    @Test
    fun playerCompactRecoveryUiStateMirrorsPlaybackIssueActions() {
        val hidden = buildPlayerCompactRecoveryUiState(
            hasPlaybackIssue = false,
            canSelectNextRoute = true,
        )
        val canFallback = buildPlayerCompactRecoveryUiState(
            hasPlaybackIssue = true,
            canSelectNextRoute = true,
        )
        val noFallback = buildPlayerCompactRecoveryUiState(
            hasPlaybackIssue = true,
            canSelectNextRoute = false,
        )

        assertFalse(hidden.visible)
        assertTrue(hidden.actions.isEmpty())
        assertEquals(34.dp, hidden.rowHeight)
        assertEquals(8.dp, hidden.actionSpacing)
        assertTrue(canFallback.visible)
        assertEquals(listOf(PlayerActionKind.Retry, PlayerActionKind.NextRoute), canFallback.actions.map { it.action.kind })
        val retry = canFallback.actions[0]
        val next = canFallback.actions[1]
        assertEquals("重试", retry.label)
        assertEquals("当前", retry.action.value)
        assertTrue(retry.action.selected)
        assertTrue(retry.action.enabled)
        assertEquals(SourceLibraryTone.Primary, retry.action.tone)
        assertEquals(1f, retry.weight)
        assertEquals(34.dp, retry.height)
        assertEquals(8.dp, retry.cornerRadius)
        assertEquals(SourceLibraryTone.Primary, retry.containerTone)
        assertEquals(0.18f, retry.containerAlpha)
        assertEquals(SourceLibraryTone.Primary, retry.contentTone)
        assertEquals(1f, retry.contentAlpha)
        assertNull(retry.disabledContainerTone)
        assertEquals(0.05f, retry.disabledContainerAlpha)
        assertNull(retry.disabledContentTone)
        assertEquals(0.34f, retry.disabledContentAlpha)
        assertEquals("换个源", next.label)
        assertEquals("可切", next.action.value)
        assertTrue(next.action.enabled)
        assertEquals(SourceLibraryTone.Online, next.action.tone)
        assertNull(next.containerTone)
        assertEquals(0.08f, next.containerAlpha)
        assertNull(next.contentTone)
        assertEquals(0.68f, next.contentAlpha)
        assertFalse(noFallback.actions[1].action.enabled)
        assertEquals("无", noFallback.actions[1].action.value)
        assertEquals(SourceLibraryTone.Muted, noFallback.actions[1].action.tone)
    }

    @Test
    fun portraitRouteInsightUiStateSummarizesCoverageAndCurrentRoute() {
        val onlineA = route(
            id = "hls-a",
            protocol = StreamProtocol.HLS,
            score = 600,
            quality = "1080p",
            sourceId = "online-a",
            sourceName = "Online A",
        )
        val onlineB = route(
            id = "hls-b",
            protocol = StreamProtocol.HLS,
            score = 500,
            quality = "720p",
            sourceId = "online-b",
            sourceName = "Online B",
        )
        val onlineBBackup = route(
            id = "mp4-b",
            protocol = StreamProtocol.PROGRESSIVE,
            score = 400,
            quality = "480p",
            sourceId = "online-b",
            sourceName = "Online B",
        )
        val bt = route(
            id = "bt",
            protocol = StreamProtocol.BITTORRENT,
            score = 300,
            quality = "1080p",
            sourceId = "bt",
            sourceName = "BT",
        )

        val state = buildPortraitRouteInsightUiState(
            routes = listOf(onlineA, onlineB, onlineBBackup, bt),
            stream = onlineB.stream,
        )

        assertEquals(listOf("覆盖", "在线", "备用", "当前"), state.chips.map { it.label })
        assertEquals("3源 · 4线", state.chips.first { it.label == "覆盖" }.value)
        assertEquals("2源 · 3线", state.chips.first { it.label == "在线" }.value)
        assertEquals("单线", state.chips.first { it.label == "备用" }.value)
        assertEquals("720p", state.chips.first { it.label == "当前" }.value)
        assertEquals(SourceLibraryTone.Online, state.chips.first { it.label == "覆盖" }.tone)
        assertEquals(SourceLibraryTone.Primary, state.chips.first { it.label == "在线" }.tone)
        assertEquals(SourceLibraryTone.Backup, state.chips.first { it.label == "备用" }.tone)
        assertEquals(SourceLibraryTone.Cache, state.chips.first { it.label == "当前" }.tone)
    }

    @Test
    fun portraitRouteInsightUiStateFallsBackForEmptyRoutes() {
        val stream = MediaStream(
            id = "dash",
            providerId = "provider",
            url = "https://example.invalid/manifest.mpd",
            protocol = StreamProtocol.DASH,
        )

        val state = buildPortraitRouteInsightUiState(routes = emptyList(), stream = stream)

        assertEquals("单线", state.chips.first { it.label == "覆盖" }.value)
        assertEquals("待匹配", state.chips.first { it.label == "在线" }.value)
        assertEquals("自动", state.chips.first { it.label == "备用" }.value)
        assertEquals("DASH", state.chips.first { it.label == "当前" }.value)
    }

    @Test
    fun portraitWatchInfoUiStateSummarizesCurrentEpisodeAndRoutes() {
        val episodes = listOf(
            episode(id = "ep-1", index = 1),
            episode(id = "ep-2", index = 2),
            episode(id = "ep-3", index = 3),
        )
        val routes = listOf(
            route(
                id = "hls",
                protocol = StreamProtocol.HLS,
                score = 500,
                quality = "1080p",
                sourceId = "animeko",
                sourceName = "Animeko",
                providerId = "animeko",
            ),
            route(
                id = "bt",
                protocol = StreamProtocol.BITTORRENT,
                score = 100,
                quality = "1080p",
                sourceId = "bt",
                sourceName = "BT",
                providerId = "bt",
            ),
        )
        val detail = MediaDetail(
            providerId = "animeko",
            title = "番剧标题",
            url = "https://example.invalid/detail",
            episodes = episodes,
        )

        val state = buildPortraitWatchInfoUiState(
            detail = detail,
            episode = episodes[1],
            stream = routes[0].stream,
            routes = routes,
            playbackState = "READY",
            routeNotice = null,
            errorMessage = null,
            hasPlaybackIssue = false,
        )

        assertEquals("番剧标题", state.title)
        assertEquals("第 2 集", state.currentEpisodeLabel)
        assertEquals("第 2 集", state.episodeTitle)
        assertEquals(listOf("第 2 集", "1080p", "播放就绪"), state.metaChips)
        assertTrue(state.playbackBrief.contains("Animeko"))
        assertTrue(state.playbackBrief.contains("自动推荐 · 可换源"))
        assertEquals(listOf(PlayerPanelKind.Episode, PlayerPanelKind.Route), state.actions.map { it.kind })
        assertEquals("2/3", state.actions.first { it.kind == PlayerPanelKind.Episode }.subtitle)
        assertEquals("2源", state.actions.first { it.kind == PlayerPanelKind.Route }.subtitle)
        assertNull(state.diagnostic)
    }

    @Test
    fun portraitWatchInfoUiStateFallsBackForBlankAndErrorState() {
        val episode = Episode(
            providerId = "provider",
            id = "ep-empty",
            title = "",
            url = "https://example.invalid/empty",
            index = null,
        )
        val detail = MediaDetail(
            providerId = "provider",
            title = "",
            url = "https://example.invalid/detail",
            episodes = listOf(episode),
        )
        val stream = MediaStream(
            id = "stream",
            providerId = "provider",
            url = "https://example.invalid/stream.m3u8",
            protocol = StreamProtocol.HLS,
        )

        val state = buildPortraitWatchInfoUiState(
            detail = detail,
            episode = episode,
            stream = stream,
            routes = emptyList(),
            playbackState = "",
            routeNotice = null,
            errorMessage = "播放失败",
            hasPlaybackIssue = true,
        )
        val diagnostic = checkNotNull(state.diagnostic)

        assertEquals("正在播放", state.title)
        assertEquals("当前集", state.currentEpisodeLabel)
        assertEquals("当前集", state.episodeTitle)
        assertEquals(listOf("当前集", "自动", "播放中"), state.metaChips)
        assertTrue(state.actions.isEmpty())
        assertEquals("播放失败", diagnostic.message)
        assertTrue(diagnostic.error)
        assertEquals(SourceLibraryTone.Web, diagnostic.tone)
    }

    @Test
    fun playerNoticeUiStatePrioritizesErrorAndRouteNotice() {
        val errorOverlay = PlayerOverlayState(
            title = "标题",
            episodeTitle = "第 1 集",
            sourceLabel = "Animeko",
            qualityLabel = "1080p",
            routeLabel = "HLS",
            playbackState = "播放中",
            statusLabel = "异常",
            notice = "正在切换",
            error = "播放失败",
        )
        val noticeOverlay = errorOverlay.copy(notice = "已切换到 1080p", error = null, statusLabel = "切源中")
        val errorState = checkNotNull(buildPlayerOverlayNoticeUiState(errorOverlay))
        val noticeState = checkNotNull(buildPlayerOverlayNoticeUiState(noticeOverlay))
        val fullscreenNotice = checkNotNull(buildPlayerFullscreenNoticeUiState(
            routeSummary = "Animeko · HLS",
            routeNotice = "已切换到备用源",
            errorMessage = "上一条线路失败",
        ))
        val fullscreenError = checkNotNull(buildPlayerFullscreenNoticeUiState(
            routeSummary = "",
            routeNotice = null,
            errorMessage = "播放失败",
        ))

        assertEquals("播放失败", errorState.message)
        assertTrue(errorState.error)
        assertEquals(SourceLibraryTone.Web, errorState.tone)
        assertEquals("已切换到 1080p", noticeState.message)
        assertFalse(noticeState.error)
        assertEquals(SourceLibraryTone.Backup, noticeState.tone)
        assertEquals("已切换到备用源", fullscreenNotice.message)
        assertFalse(fullscreenNotice.error)
        assertEquals("Animeko · HLS", fullscreenNotice.title)
        assertEquals("自动线路", fullscreenError.title)
        assertEquals("播放失败", fullscreenError.message)
        assertTrue(fullscreenError.error)
        assertNull(buildPlayerFullscreenNoticeUiState("", null, null))
    }

    @Test
    fun playerRouteStatusUiStateMapsNoticeAndErrorTone() {
        val normal = buildPlayerRouteStatusUiState(
            PlayerOverlayState(
                title = "",
                episodeTitle = "",
                sourceLabel = "",
                qualityLabel = "",
                routeLabel = "自动最佳",
                playbackState = "播放中",
                statusLabel = "播放中",
                notice = null,
                error = null,
            ),
        )
        val notice = buildPlayerRouteStatusUiState(
            PlayerOverlayState(
                title = "",
                episodeTitle = "",
                sourceLabel = "",
                qualityLabel = "",
                routeLabel = "",
                playbackState = "播放中",
                statusLabel = "切源中",
                notice = "已切换",
                error = null,
            ),
        )
        val error = buildPlayerRouteStatusUiState(
            PlayerOverlayState(
                title = "",
                episodeTitle = "",
                sourceLabel = "",
                qualityLabel = "",
                routeLabel = "HLS",
                playbackState = "播放中",
                statusLabel = "异常",
                notice = null,
                error = "失败",
            ),
        )

        assertEquals("自动最佳", normal.routeLabel)
        assertEquals(SourceLibraryTone.Online, normal.tone)
        assertFalse(normal.error)
        assertEquals("自动线路", notice.routeLabel)
        assertEquals(SourceLibraryTone.Backup, notice.tone)
        assertFalse(notice.error)
        assertEquals(SourceLibraryTone.Web, error.tone)
        assertTrue(error.error)
    }

    @Test
    fun playerCacheActionStateExplainsAllowedAndBlockedStreams() {
        val hls = buildPlayerCacheActionUiState(
            MediaStream(
                id = "hls",
                providerId = "online",
                url = "https://example.invalid/live.m3u8",
                protocol = StreamProtocol.HLS,
            ),
        )
        val webView = buildPlayerCacheActionUiState(
            MediaStream(
                id = "web",
                providerId = "web",
                url = "https://example.invalid/watch",
                protocol = StreamProtocol.WEBVIEW_ONLY,
                downloadPolicy = DownloadPolicy.BlockedWebViewOnly,
            ),
        )
        val drm = buildPlayerCacheActionUiState(
            MediaStream(
                id = "drm",
                providerId = "drm",
                url = "https://example.invalid/manifest.mpd",
                protocol = StreamProtocol.DASH,
                downloadPolicy = DownloadPolicy.BlockedDrm,
            ),
        )
        val bt = buildPlayerCacheActionUiState(
            MediaStream(
                id = "bt",
                providerId = "bt",
                url = "magnet:?xt=urn:btih:test",
                protocol = StreamProtocol.BITTORRENT,
                downloadPolicy = DownloadPolicy.CacheOnly,
            ),
        )
        val rtsp = buildPlayerCacheActionUiState(
            MediaStream(
                id = "rtsp",
                providerId = "rtsp",
                url = "rtsp://example.invalid/live",
                protocol = StreamProtocol.RTSP,
            ),
        )

        assertTrue(hls.enabled)
        assertEquals("\u53ef\u79bb\u7ebf", hls.value)
        assertEquals("\u7f13\u5b58\u672c\u96c6", hls.actionLabel)
        assertFalse(webView.enabled)
        assertTrue(webView.reason.contains("\u7f51\u9875\u55c5\u63a2"))
        assertFalse(drm.enabled)
        assertEquals("DRM", drm.value)
        assertFalse(bt.enabled)
        assertEquals("\u8fb9\u4e0b\u8fb9\u64ad", bt.value)
        assertFalse(rtsp.enabled)
        assertEquals("RTSP", rtsp.value)
    }

    @Test
    fun playerStartupOverlayUiStateFormatsMetadataAndChrome() {
        val loading = buildPlayerStartupOverlayUiState(
            playbackState = " ",
            videoSize = " 1920x1080 ",
            protocol = StreamProtocol.HLS,
        )
        val progressive = buildPlayerStartupOverlayUiState(
            playbackState = "Buffering",
            videoSize = " ",
            protocol = StreamProtocol.PROGRESSIVE,
        )

        assertEquals("\u6b63\u5728\u52a0\u8f7d\u753b\u9762", loading.title)
        assertEquals("\u7f13\u51b2\u4e2d / HLS / 1920x1080", loading.metadataLine)
        assertEquals(SourceLibraryTone.Online, loading.progressTone)
        assertEquals(300.dp, loading.width)
        assertEquals(8.dp, loading.cornerRadius)
        assertEquals(1.dp, loading.borderWidth)
        assertEquals(0.84f, loading.containerAlpha, 0.001f)
        assertEquals(0.08f, loading.borderAlpha, 0.001f)
        assertEquals(18.dp, loading.contentPadding)
        assertEquals(10.dp, loading.contentSpacing)
        assertEquals(30.dp, loading.progressSize)
        assertEquals(1f, loading.titleAlpha, 0.001f)
        assertEquals(1f, loading.metadataAlpha, 0.001f)
        assertEquals("Buffering / MP4", progressive.metadataLine)
    }

    @Test
    fun torrentPlaybackPreparationUiStateSummarizesPlanAndNetwork() {
        val stream = MediaStream(
            id = "bt",
            providerId = "bt",
            url = "magnet:?xt=urn:btih:test",
            protocol = StreamProtocol.BITTORRENT,
        )
        val state = buildTorrentPlaybackPreparationUiState(
            TorrentEngineState(
                stream = stream,
                plan = TorrentPlaybackPlan(
                    stream = stream,
                    selectedFileName = "episode-01.mkv",
                    selectedFileSizeBytes = 1_073_741_824L,
                    selectedFileContiguousBytes = 104_857_600L,
                    playbackReadyBytes = 209_715_200L,
                    selectedFileProgressPercent = 42.5f,
                    localPlaybackUrl = "http://127.0.0.1:8080/video",
                    bufferingPercent = 50f,
                ),
                hasMetadata = true,
                status = "缓冲中",
                progressPercent = 12.5f,
                selectedFileProgressPercent = 42.5f,
                downloadRateBytesPerSecond = 1_048_576,
                connectedPeers = 12,
                connectedSeeds = 3,
            ),
        )

        assertEquals("正在准备播放", state.title)
        assertEquals(0.5f, state.bufferingProgress, 0.001f)
        assertEquals("状态: 缓冲中", state.statusLine)
        assertEquals("视频信息: 已获取  播放通道: 已就绪", state.readinessLine)
        assertEquals("整体: 12.5%  视频: 42.5%  起播: 50.0%", state.progressLine)
        assertEquals("起播缓存: 100.0 MB / 200.0 MB", state.bufferingLine)
        assertEquals("连接: 12  高速节点: 3  速度: 1.0 MB/s", state.connectionLine)
        assertEquals("文件: episode-01.mkv", state.fileLine)
        assertEquals("大小: 1.0 GB", state.sizeLine)
        assertNull(state.errorMessage)
        assertEquals(560.dp, state.contentWidth)
        assertEquals(24.dp, state.contentPadding)
        assertEquals(12.dp, state.contentSpacing)
        assertEquals(SourceLibraryTone.Cache, state.progressTone)
        assertEquals(SourceLibraryTone.Muted, state.progressTrackTone)
        assertEquals(0.22f, state.progressTrackAlpha, 0.001f)
        assertEquals(1f, state.titleAlpha, 0.001f)
        assertEquals(1f, state.descriptionAlpha, 0.001f)
        assertEquals(1f, state.primaryLineAlpha, 0.001f)
        assertEquals(1f, state.secondaryLineAlpha, 0.001f)
        assertEquals(SourceLibraryTone.Web, state.errorTone)
    }

    @Test
    fun torrentPlaybackPreparationUiStateFallsBackWithoutPlan() {
        val state = buildTorrentPlaybackPreparationUiState(
            TorrentEngineState(
                hasMetadata = false,
                errorMessage = "种子解析失败",
            ),
        )

        assertEquals(0f, state.bufferingProgress, 0.001f)
        assertEquals("状态: 等待中", state.statusLine)
        assertEquals("视频信息: 匹配中  播放通道: 准备中", state.readinessLine)
        assertEquals("整体: 0.0%  视频: 0.0%  起播: 0.0%", state.progressLine)
        assertEquals("连接: 0  高速节点: 0  速度: 0 B/s", state.connectionLine)
        assertNull(state.bufferingLine)
        assertNull(state.fileLine)
        assertNull(state.sizeLine)
        assertEquals("种子解析失败", state.errorMessage)
    }

    @Test
    fun playerSeekTargetClampsToKnownDurationAndStart() {
        assertEquals(
            0L,
            playerSeekTargetMs(currentPositionMs = 5_000L, deltaMs = -10_000L, durationMs = 120_000L),
        )
        assertEquals(
            120_000L,
            playerSeekTargetMs(currentPositionMs = 115_000L, deltaMs = 10_000L, durationMs = 120_000L),
        )
        assertEquals(
            42_000L,
            playerSeekTargetMs(currentPositionMs = 32_000L, deltaMs = 10_000L, durationMs = 0L),
        )
    }

    @Test
    fun playerSeekBarUiStateFormatsSeekableProgressAndChrome() {
        val state = buildPlayerSeekBarUiState(
            positionMs = 65_000L,
            durationMs = 3_725_000L,
        )

        assertEquals("01:05", state.positionLabel)
        assertEquals("1:02:05", state.durationLabel)
        assertEquals(65_000L, state.displayPositionMs)
        assertEquals(3_725_000L, state.durationMs)
        assertEquals(65_000f / 3_725_000f, state.progressFraction ?: -1f, 0.001f)
        assertTrue(state.seekable)
        assertEquals(65_000f, state.value, 0.001f)
        assertEquals(0f, state.valueRange.start, 0.001f)
        assertEquals(3_725_000f, state.valueRange.endInclusive, 0.001f)
        assertEquals(0, state.steps)
        assertEquals(8.dp, state.rowSpacing)
        assertEquals(48.dp, state.timeLabelWidth)
        assertNull(state.timeLabelTone)
        assertEquals(1f, state.currentTimeAlpha)
        assertEquals(0.78f, state.durationTimeAlpha)
        assertEquals(30.dp, state.sliderHeight)
        assertEquals(SourceLibraryTone.Primary, state.sliderThumbTone)
        assertEquals(SourceLibraryTone.Primary, state.sliderActiveTrackTone)
        assertNull(state.sliderInactiveTrackTone)
        assertEquals(0.24f, state.sliderInactiveTrackAlpha)
        assertEquals(3.dp, state.loadingTrackHeight)
        assertEquals(SourceLibraryTone.Online, state.loadingTrackTone)
        assertNull(state.loadingTrackBackgroundTone)
        assertEquals(0.18f, state.loadingTrackBackgroundAlpha)
        assertEquals("00:00", formatPlaybackTimeForUi(-5_000L))
    }

    @Test
    fun playerSeekBarUiStateUsesPendingSeekAndUnknownDuration() {
        val pending = buildPlayerSeekBarUiState(
            positionMs = 10_000L,
            durationMs = 120_000L,
            pendingSeekMs = 140_000L,
        )
        val unknown = buildPlayerSeekBarUiState(
            positionMs = 42_000L,
            durationMs = 0L,
        )

        assertEquals(120_000L, pending.displayPositionMs)
        assertEquals("02:00", pending.positionLabel)
        assertEquals(1f, pending.progressFraction ?: -1f, 0.001f)
        assertFalse(unknown.seekable)
        assertEquals("00:00", unknown.positionLabel)
        assertEquals("--:--", unknown.durationLabel)
        assertEquals(0L, unknown.displayPositionMs)
        assertEquals(0f, unknown.value, 0.001f)
        assertEquals(0f, unknown.valueRange.start, 0.001f)
        assertEquals(1f, unknown.valueRange.endInclusive, 0.001f)
        assertNull(unknown.progressFraction)
    }

    @Test
    fun playerEdgeProgressUiStateClampsProgressAndExposesChrome() {
        val normal = buildPlayerEdgeProgressUiState(positionMs = 30_000L, durationMs = 120_000L)
        val overflow = buildPlayerEdgeProgressUiState(positionMs = 150_000L, durationMs = 120_000L)
        val unknown = buildPlayerEdgeProgressUiState(positionMs = 30_000L, durationMs = 0L)
        val negative = buildPlayerEdgeProgressUiState(positionMs = -10_000L, durationMs = 120_000L)

        assertEquals(0.25f, normal.progressFraction, 0.001f)
        assertEquals(2.dp, normal.height)
        assertEquals(SourceLibraryTone.Primary, normal.progressTone)
        assertNull(normal.trackTone)
        assertEquals(0f, normal.trackAlpha, 0.001f)
        assertEquals(1f, overflow.progressFraction, 0.001f)
        assertEquals(0f, unknown.progressFraction, 0.001f)
        assertEquals(0f, negative.progressFraction, 0.001f)
    }

    @Test
    fun playerCompactInteractionUiStateFormatsProgressAndDanmakuEntry() {
        val enabled = buildPlayerCompactInteractionUiState(
            progressFraction = 1.2f,
            danmakuEnabled = true,
        )
        val disabled = buildPlayerCompactInteractionUiState(
            progressFraction = null,
            danmakuEnabled = false,
        )

        assertEquals(1f, enabled.progress.progressFraction ?: -1f, 0.001f)
        assertEquals(3.dp, enabled.progress.height)
        assertEquals(999.dp, enabled.progress.cornerRadius)
        assertEquals(SourceLibraryTone.Primary, enabled.progress.progressTone)
        assertNull(enabled.progress.trackTone)
        assertEquals(0.18f, enabled.progress.trackAlpha)
        assertEquals(7.dp, enabled.columnSpacing)
        assertEquals(36.dp, enabled.actionRowHeight)
        assertEquals(7.dp, enabled.actionRowSpacing)
        assertEquals(38.dp, enabled.fullscreenActionWidth)
        assertEquals("全屏播放", enabled.fullscreenContentDescription)
        assertEquals("发条弹幕", enabled.danmaku.title)
        assertEquals("开", enabled.danmaku.toggleLabel)
        assertTrue(enabled.danmaku.enabled)
        assertEquals(0.32f, enabled.danmaku.containerAlpha)
        assertEquals(999.dp, enabled.danmaku.cornerRadius)
        assertEquals(11.dp, enabled.danmaku.startPadding)
        assertEquals(5.dp, enabled.danmaku.endPadding)
        assertEquals(7.dp, enabled.danmaku.contentSpacing)
        assertEquals(SourceLibraryTone.Primary, enabled.danmaku.iconTone)
        assertEquals(1f, enabled.danmaku.iconAlpha)
        assertEquals(16.dp, enabled.danmaku.iconSize)
        assertNull(enabled.danmaku.textTone)
        assertEquals(0.76f, enabled.danmaku.textAlpha)
        assertEquals(32.dp, enabled.danmaku.toggleWidth)
        assertEquals(26.dp, enabled.danmaku.toggleHeight)
        assertEquals(SourceLibraryTone.Primary, enabled.danmaku.toggleContainerTone)
        assertEquals(0.2f, enabled.danmaku.toggleContainerAlpha)
        assertEquals(SourceLibraryTone.Primary, enabled.danmaku.toggleContentTone)
        assertEquals(1f, enabled.danmaku.toggleContentAlpha)

        assertNull(disabled.progress.progressFraction)
        assertEquals("弹幕关闭", disabled.danmaku.title)
        assertEquals("关", disabled.danmaku.toggleLabel)
        assertFalse(disabled.danmaku.enabled)
        assertNull(disabled.danmaku.iconTone)
        assertEquals(0.42f, disabled.danmaku.iconAlpha)
        assertNull(disabled.danmaku.toggleContainerTone)
        assertEquals(0.08f, disabled.danmaku.toggleContainerAlpha)
        assertNull(disabled.danmaku.toggleContentTone)
        assertEquals(0.56f, disabled.danmaku.toggleContentAlpha)
    }

    @Test
    fun playerFullscreenDanmakuInputUiStateExposesChrome() {
        val enabled = buildPlayerFullscreenDanmakuInputUiState(danmakuEnabled = true)
        val disabled = buildPlayerFullscreenDanmakuInputUiState(danmakuEnabled = false)

        assertEquals("点我发弹幕", enabled.title)
        assertEquals("开", enabled.toggleLabel)
        assertTrue(enabled.enabled)
        assertEquals(36.dp, enabled.height)
        assertEquals(999.dp, enabled.cornerRadius)
        assertEquals(PlayerChromeBaseColor.Black, enabled.containerBaseColor)
        assertEquals(0.32f, enabled.containerAlpha, 0.001f)
        assertEquals(10.dp, enabled.startPadding)
        assertEquals(6.dp, enabled.endPadding)
        assertEquals(8.dp, enabled.contentSpacing)
        assertEquals(SourceLibraryTone.Primary, enabled.iconTone)
        assertEquals(PlayerChromeBaseColor.White, enabled.iconBaseColor)
        assertEquals(1f, enabled.iconAlpha, 0.001f)
        assertEquals(18.dp, enabled.iconSize)
        assertEquals(PlayerChromeBaseColor.White, enabled.textBaseColor)
        assertEquals(0.7f, enabled.textAlpha, 0.001f)
        assertEquals(42.dp, enabled.toggleWidth)
        assertEquals(26.dp, enabled.toggleHeight)
        assertEquals(999.dp, enabled.toggleCornerRadius)
        assertEquals(SourceLibraryTone.Primary, enabled.toggleContainerTone)
        assertEquals(PlayerChromeBaseColor.White, enabled.toggleContainerBaseColor)
        assertEquals(0.22f, enabled.toggleContainerAlpha, 0.001f)
        assertEquals(SourceLibraryTone.Primary, enabled.toggleContentTone)
        assertEquals(PlayerChromeBaseColor.White, enabled.toggleContentBaseColor)
        assertEquals(1f, enabled.toggleContentAlpha, 0.001f)

        assertEquals("弹幕已关闭", disabled.title)
        assertEquals("关", disabled.toggleLabel)
        assertFalse(disabled.enabled)
        assertNull(disabled.iconTone)
        assertEquals(0.46f, disabled.iconAlpha, 0.001f)
        assertNull(disabled.toggleContainerTone)
        assertEquals(0.08f, disabled.toggleContainerAlpha, 0.001f)
        assertNull(disabled.toggleContentTone)
        assertEquals(0.56f, disabled.toggleContentAlpha, 0.001f)
    }

    @Test
    fun playerFullscreenSeekClusterUiStateExposesShortcutChrome() {
        val state = buildPlayerFullscreenSeekClusterUiState()

        assertEquals(36.dp, state.height)
        assertEquals(999.dp, state.cornerRadius)
        assertEquals(PlayerChromeBaseColor.Black, state.containerBaseColor)
        assertEquals(0.30f, state.containerAlpha, 0.001f)
        assertEquals(1.dp, state.borderWidth)
        assertEquals(PlayerChromeBaseColor.White, state.borderBaseColor)
        assertEquals(0.08f, state.borderAlpha, 0.001f)
        assertEquals(1.dp, state.dividerWidth)
        assertEquals(18.dp, state.dividerHeight)
        assertEquals(PlayerChromeBaseColor.White, state.dividerBaseColor)
        assertEquals(0.10f, state.dividerAlpha, 0.001f)
        assertEquals(listOf(PlayerSeekShortcutKind.Backward, PlayerSeekShortcutKind.Forward), state.buttons.map { it.kind })

        val backward = state.buttons.first()
        assertEquals("后退 10 秒", backward.contentDescription)
        assertEquals(-10_000L, backward.deltaMs)
        assertEquals(42.dp, backward.width)
        assertEquals(36.dp, backward.height)
        assertEquals(18.dp, backward.iconSize)
        assertEquals(PlayerChromeBaseColor.White, backward.iconBaseColor)
        assertEquals(0.84f, backward.iconAlpha, 0.001f)

        val forward = state.buttons.last()
        assertEquals("快进 10 秒", forward.contentDescription)
        assertEquals(10_000L, forward.deltaMs)
    }

    @Test
    fun playerDoubleTapSeekDeltaUsesTapSide() {
        assertEquals(-10_000L, playerDoubleTapSeekDeltaMs(tapX = 120f, surfaceWidthPx = 400))
        assertEquals(10_000L, playerDoubleTapSeekDeltaMs(tapX = 280f, surfaceWidthPx = 400))
        assertEquals(15_000L, playerDoubleTapSeekDeltaMs(tapX = 280f, surfaceWidthPx = 400, stepMs = 15_000L))
        assertEquals(null, playerDoubleTapSeekDeltaMs(tapX = 120f, surfaceWidthPx = 0))
        assertEquals(null, playerDoubleTapSeekDeltaMs(tapX = 120f, surfaceWidthPx = 400, stepMs = 0L))
    }

    @Test
    fun playerSeekFeedbackPlacementSeparatesGestureFromButtonSeek() {
        assertEquals(PlayerSeekFeedbackPlacement.Center, playerSeekFeedbackPlacement(deltaMs = -10_000L, fromGesture = false))
        assertEquals(PlayerSeekFeedbackPlacement.Start, playerSeekFeedbackPlacement(deltaMs = -10_000L, fromGesture = true))
        assertEquals(PlayerSeekFeedbackPlacement.End, playerSeekFeedbackPlacement(deltaMs = 10_000L, fromGesture = true))
        assertEquals(PlayerSeekFeedbackPlacement.Center, playerSeekFeedbackPlacement(deltaMs = 0L, fromGesture = true))
    }

    @Test
    fun playerSeekFeedbackUiStateExposesAntiObstructionChrome() {
        val center = buildPlayerSeekFeedbackUiState(
            text = "快进 10 秒",
            placement = PlayerSeekFeedbackPlacement.Center,
            compact = false,
        )
        val start = buildPlayerSeekFeedbackUiState(
            text = "后退 10 秒",
            placement = PlayerSeekFeedbackPlacement.Start,
            compact = true,
        )

        assertEquals("快进 10 秒", center.text)
        assertEquals(PlayerSeekFeedbackPlacement.Center, center.placement)
        assertEquals(0.dp, center.horizontalMargin)
        assertEquals(96.dp, center.verticalOffset)
        assertEquals(8.dp, center.cornerRadius)
        assertEquals(PlayerChromeBaseColor.Black, center.containerBaseColor)
        assertEquals(0.58f, center.containerAlpha, 0.001f)
        assertEquals(1.dp, center.borderWidth)
        assertEquals(SourceLibraryTone.Primary, center.borderTone)
        assertEquals(0.32f, center.borderAlpha, 0.001f)
        assertEquals(12.dp, center.horizontalPadding)
        assertEquals(7.dp, center.verticalPadding)
        assertEquals(7.dp, center.contentSpacing)
        assertFalse(center.iconVisible)
        assertEquals(18.dp, center.iconSize)
        assertEquals(SourceLibraryTone.Primary, center.iconTone)
        assertEquals(PlayerChromeBaseColor.White, center.textBaseColor)
        assertEquals(1f, center.textAlpha, 0.001f)
        assertEquals(28.dp, start.horizontalMargin)
        assertEquals(74.dp, start.verticalOffset)
        assertTrue(start.iconVisible)
    }

    @Test
    fun playerSeekRevealPolicyKeepsGestureSeekLightweight() {
        assertTrue(playerSeekShouldRevealControls(fromGesture = false))
        assertFalse(playerSeekShouldRevealControls(fromGesture = true))
    }

    @Test
    fun playerOverlayStateUsesShortStatusLabels() {
        val candidate = route("hls", StreamProtocol.HLS, 900, quality = "1080p")
        val playing = buildPlayerOverlayState(
            title = "Title",
            episodeTitle = "第 1 集",
            stream = candidate.stream,
            route = candidate,
            playbackState = "播放中",
            notice = null,
            error = null,
        )
        val switching = buildPlayerOverlayState(
            title = "Title",
            episodeTitle = "第 1 集",
            stream = candidate.stream,
            route = candidate,
            playbackState = "BUFFERING",
            notice = "已切换到 Provider 1080p",
            error = null,
        )
        val ready = buildPlayerOverlayState(
            title = "Title",
            episodeTitle = "第 1 集",
            stream = candidate.stream,
            route = candidate,
            playbackState = "READY",
            notice = null,
            error = null,
        )
        val failed = buildPlayerOverlayState(
            title = "Title",
            episodeTitle = "第 1 集",
            stream = candidate.stream,
            route = candidate,
            playbackState = "错误",
            notice = null,
            error = "Source failed",
        )

        assertEquals("Provider", playing.sourceLabel)
        assertEquals("1080p", playing.qualityLabel)
        assertEquals("1080p", playing.routeLabel)
        assertEquals("播放中", playing.statusLabel)
        assertEquals("缓冲中", switching.playbackState)
        assertEquals("切源中", switching.statusLabel)
        assertEquals("播放就绪", ready.playbackState)
        assertEquals("已就绪", ready.statusLabel)
        assertEquals("异常", failed.statusLabel)
    }

    private fun manifest(
        id: String,
        name: String,
        capabilities: Set<SourceCapability> = setOf(SourceCapability.SEARCH),
        domains: Set<String> = emptySet(),
        requiresWebView: Boolean = false,
        supportsDownload: Boolean = false,
    ): SourceManifest {
        return SourceManifest(
            id = id,
            name = name,
            version = "1.0",
            author = "test",
            capabilities = capabilities,
            domains = domains,
            requiresWebView = requiresWebView,
            supportsDownload = supportsDownload,
        )
    }

    private fun danmakuMatch(
        providerId: String,
        source: DanmakuMatchSource = DanmakuMatchSource.Automatic,
        score: Int,
    ): DanmakuMatch {
        val platform = when (providerId) {
            "danmaku-bilibili" -> DanmakuPlatform.Bilibili
            "danmaku-tencent" -> DanmakuPlatform.Tencent
            "danmaku-iqiyi" -> DanmakuPlatform.Iqiyi
            "danmaku-youku" -> DanmakuPlatform.Youku
            else -> DanmakuPlatform.Local
        }
        return DanmakuMatch(
            providerId = providerId,
            platform = platform,
            title = "Test Anime",
            episodeTitle = "Episode 1",
            score = score,
            token = providerId,
            source = source,
        )
    }

    private fun category(id: String, title: String): BangumiCategory {
        return BangumiCategory(
            id = id,
            title = title,
            subtitle = "test",
            badge = "T",
        )
    }

    private fun searchResult(
        providerId: String,
        title: String,
        raw: Map<String, String> = emptyMap(),
    ): SearchResult {
        return SearchResult(
            providerId = providerId,
            title = title,
            url = "zfbml://$providerId/$title",
            raw = raw,
        )
    }

    private fun scheduleDay(
        weekdayId: Int,
        weekdayCn: String,
        items: List<SearchResult>,
    ): BangumiScheduleDay {
        return BangumiScheduleDay(
            weekdayId = weekdayId,
            weekdayCn = weekdayCn,
            weekdayEn = "",
            items = items,
        )
    }

    private fun episode(id: String = "ep-1", index: Int = 1): Episode {
        return Episode(
            providerId = "bangumi-catalog",
            id = id,
            title = "第 $index 集",
            url = "bangumi://subject/1/episode/$index",
            index = index,
        )
    }

    private fun route(
        id: String,
        protocol: StreamProtocol,
        score: Int,
        quality: String,
        sourceId: String = "provider",
        sourceName: String = "Provider",
        providerId: String = sourceId,
        sizeBytes: Long? = null,
    ): RouteCandidate {
        return RouteCandidate(
            stream = MediaStream(
                id = id,
                providerId = providerId,
                url = "https://example.invalid/$id",
                protocol = protocol,
                quality = quality,
                sourceScore = score,
            ),
            sourceId = sourceId,
            sourceName = sourceName,
            title = "Title",
            routeName = quality,
            episodeTitle = "第 1 集",
            episodeIndex = 1,
            quality = quality,
            subgroup = null,
            protocol = protocol,
            score = score,
            publishedAt = null,
            sizeBytes = sizeBytes,
        )
    }
}
