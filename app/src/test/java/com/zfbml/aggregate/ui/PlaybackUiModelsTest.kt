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
        assertEquals("暂无可播线路", state.recommendationTitle)
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
    ): RouteCandidate {
        return RouteCandidate(
            stream = MediaStream(
                id = id,
                providerId = "provider",
                url = "https://example.invalid/$id",
                protocol = protocol,
                quality = quality,
                sourceScore = score,
            ),
            sourceId = "provider",
            sourceName = "Provider",
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
