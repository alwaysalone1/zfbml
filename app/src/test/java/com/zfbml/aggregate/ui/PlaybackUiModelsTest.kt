package com.zfbml.aggregate.ui

import com.zfbml.aggregate.source.Episode
import com.zfbml.aggregate.source.MediaStream
import com.zfbml.aggregate.source.RouteCandidate
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
        assertEquals("暂无可用线路", state.recommendationTitle)
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
        assertEquals("failed", state.selectedRoute?.stream?.id)
        assertEquals(3, state.totalCount)
        assertEquals(2, state.availableCount)
        assertEquals(1, state.onlineCount)
        assertEquals(1, state.btCount)
        assertEquals(1, state.failedCount)
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

        assertEquals("1080p", playing.routeLabel)
        assertEquals("播放中", playing.statusLabel)
        assertEquals("缓冲中", switching.playbackState)
        assertEquals("切源中", switching.statusLabel)
        assertEquals("播放就绪", ready.playbackState)
        assertEquals("已就绪", ready.statusLabel)
        assertEquals("异常", failed.statusLabel)
    }

    private fun episode(): Episode {
        return Episode(
            providerId = "bangumi-catalog",
            id = "ep-1",
            title = "第 1 集",
            url = "bangumi://subject/1/episode/1",
            index = 1,
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
