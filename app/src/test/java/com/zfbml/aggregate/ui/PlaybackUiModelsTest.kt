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
            version = "0.5.52",
            sourceCount = 4,
            danmakuCount = 3,
            cacheState = cacheState,
        )

        assertEquals("0.5.52", state.version)
        assertEquals("\u6211\u7684\u8ffd\u756a\u4e2d\u5fc3", state.headline)
        assertTrue(state.summary.contains("2 \u4e2a\u6765\u6e90"))
        assertEquals(4, state.sourceCount)
        assertEquals(3, state.danmakuCount)
        assertEquals(2, state.cacheableSourceCount)
        assertTrue(state.chips.any { it.label == "v0.5.52" })
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
            version = "0.5.52",
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
