package com.zfbml.aggregate.ui

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
        assertTrue(state.selected)
        assertTrue(state.recommended)
        assertEquals(SourceLibraryTone.Online, state.accentTone)
        assertEquals(SourceLibraryTone.Online, state.actionTone)
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
        assertEquals(SourceLibraryTone.Muted, web.accentTone)
        assertEquals("播放失败", failed.statusLabel)
        assertEquals("重试", failed.actionLabel)
        assertFalse(failed.playable)
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
        assertTrue(ready.summary.contains("\u7f13\u5b58\u7ebf\u8def"))
        assertTrue(none.items.isEmpty())
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
        assertTrue(fallbackState.summary.contains("\u515c\u5e95"))
        assertFalse(emptyState.hasItems)
        assertEquals("--", emptyState.itemCountValue)
        assertTrue(emptyState.emptySubtitle.contains("\u641c\u7d22\u756a\u540d"))
        assertEquals("\u5267\u573a\u7248\u52a0\u8f7d\u5f02\u5e38", errorState.headline)
        assertTrue(errorState.summary.contains("HTTP 500"))
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
            version = "0.5.76",
            sourceCount = 4,
            danmakuCount = 3,
            cacheState = cacheState,
        )

        assertEquals("0.5.76", state.version)
        assertEquals("\u6211\u7684\u8ffd\u756a\u4e2d\u5fc3", state.headline)
        assertTrue(state.summary.contains("2 \u4e2a\u6765\u6e90"))
        assertEquals(4, state.sourceCount)
        assertEquals(3, state.danmakuCount)
        assertEquals(2, state.cacheableSourceCount)
        assertTrue(state.chips.any { it.label == "v0.5.76" })
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
            version = "0.5.76",
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
        assertEquals("当前", idle.items[0].statusLabel)
        assertEquals("播放中", idle.items[0].actionLabel)
        assertEquals(SourceLibraryTone.Primary, idle.items[0].tone)
        assertEquals("播放", idle.items[1].actionLabel)
        assertTrue(idle.items[1].enabled)
        assertTrue(loading.summary.contains("第 2 集"))
        assertEquals("加载中", loading.items[1].statusLabel)
        assertEquals("加载中", loading.items[1].actionLabel)
        assertEquals(SourceLibraryTone.Backup, loading.items[1].tone)
        assertFalse(loading.items[2].enabled)
        assertEquals("等待", loading.items[2].actionLabel)
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

        assertEquals(8, hidden.topInsetDp)
        assertEquals(8, hidden.bottomInsetDp)
        assertEquals(84, fullscreenControls.endInsetDp)
        assertEquals(72, fullscreenControls.startInsetDp)
        assertTrue(fullscreenPanel.endInsetDp > fullscreenControls.endInsetDp)
        assertTrue(fullscreenPanel.bottomInsetDp > hidden.bottomInsetDp)
        assertEquals(72, locked.startInsetDp)
        assertEquals(0, locked.endInsetDp)
        assertEquals(0, compact.endInsetDp)
        assertTrue(compact.bottomInsetDp > hidden.bottomInsetDp)
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
        assertEquals("60%", enabled.densityLabel)
        assertEquals("76%", enabled.alphaLabel)
        assertEquals("72%", enabled.fontScaleLabel)
        assertTrue(enabled.safetySummary.contains("避让"))
        assertEquals(SourceLibraryTone.Primary, enabled.tone)
        assertEquals("弹幕已关闭", disabled.toggleTitle)
        assertEquals("点击开启弹幕显示", disabled.toggleSubtitle)
        assertEquals("30%", disabled.densityLabel)
        assertEquals("100%", disabled.alphaLabel)
        assertEquals(SourceLibraryTone.Muted, disabled.tone)
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
        assertTrue(state.options.first { it.title == "1080p" }.selected)
        assertEquals("使用中", state.options.first { it.title == "1080p" }.actionLabel)
        assertEquals(SourceLibraryTone.Primary, state.options.first { it.title == "1080p" }.tone)
        assertEquals(SourceLibraryTone.Backup, state.options.first { it.title == "4K" }.tone)
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
        assertTrue(state.options.first { it.speed == 1.25f }.selected)
        assertEquals("使用中", state.options.first { it.speed == 1.25f }.actionLabel)
        assertEquals(SourceLibraryTone.Primary, state.options.first { it.speed == 1.25f }.tone)
        assertEquals("切换", state.options.first { it.speed == 2f }.actionLabel)
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
        assertEquals("番剧标题", state.context.title)
        assertEquals("第 8 集 · Animeko · 1080p · 1.25x", state.context.metadata)
        assertEquals("播放中", state.context.statusLabel)
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
        assertEquals("12集", state.tabs.first { it.kind == PlayerPanelKind.Episode }.value)
        assertTrue(state.tabs.first { it.kind == PlayerPanelKind.Danmaku }.highlighted)
        assertEquals("开", state.tabs.first { it.kind == PlayerPanelKind.Danmaku }.value)
        assertEquals(SourceLibraryTone.Primary, state.tabs.first { it.kind == PlayerPanelKind.Danmaku }.tone)
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
        assertEquals("正在播放", state.context.title)
        assertEquals("当前集 · 自动源 · 自动 · 1.0x", state.context.metadata)
        assertTrue(state.tabs.first { it.kind == PlayerPanelKind.More }.selected)
        assertFalse(state.tabs.first { it.kind == PlayerPanelKind.Route }.enabled)
        assertEquals("自动", state.tabs.first { it.kind == PlayerPanelKind.Route }.value)
        assertFalse(state.tabs.first { it.kind == PlayerPanelKind.Episode }.enabled)
        assertEquals("单集", state.tabs.first { it.kind == PlayerPanelKind.Episode }.value)
        assertFalse(state.tabs.first { it.kind == PlayerPanelKind.Danmaku }.highlighted)
        assertEquals("关", state.tabs.first { it.kind == PlayerPanelKind.Danmaku }.value)
        assertEquals(SourceLibraryTone.Muted, state.tabs.first { it.kind == PlayerPanelKind.Danmaku }.tone)
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
        assertEquals(listOf("自动", "1.0x"), fullscreen.tags)
        assertTrue(fullscreen.error)
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
