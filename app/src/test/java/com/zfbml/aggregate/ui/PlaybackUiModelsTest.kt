package com.zfbml.aggregate.ui

import com.zfbml.aggregate.source.Episode
import com.zfbml.aggregate.source.MediaStream
import com.zfbml.aggregate.source.RouteCandidate
import com.zfbml.aggregate.source.SearchResult
import com.zfbml.aggregate.source.SourceCapability
import com.zfbml.aggregate.source.SourceManifest
import com.zfbml.aggregate.source.SourceSearchFailure
import com.zfbml.aggregate.source.SourceSearchReport
import com.zfbml.aggregate.source.StreamProtocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertEquals("source-a", groups[1].id)
        assertTrue(groups[1].hasRecommended)
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

        val onlineGroup = groups.first { it.id == "online" }
        assertTrue(onlineGroup.hasSelected)
        assertTrue(onlineGroup.hasRecommended)
        assertEquals("1可播 · 1在线", onlineGroup.sourceSummary)
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
    ): SourceManifest {
        return SourceManifest(
            id = id,
            name = name,
            version = "1.0",
            author = "test",
            capabilities = capabilities,
        )
    }

    private fun searchResult(providerId: String, title: String): SearchResult {
        return SearchResult(
            providerId = providerId,
            title = title,
            url = "zfbml://$providerId/$title",
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
            sizeBytes = null,
        )
    }
}
