package com.zfbml.aggregate.ui

import com.zfbml.aggregate.source.Episode
import com.zfbml.aggregate.source.MediaStream
import com.zfbml.aggregate.source.RouteCandidate
import com.zfbml.aggregate.source.StreamProtocol

internal enum class RouteLoadStatus {
    Idle,
    Loading,
    Ready,
    Empty,
    Failed,
}

internal data class RouteUiState(
    val status: RouteLoadStatus,
    val selectedEpisodeTitle: String,
    val bestRoute: RouteCandidate?,
    val visibleRoutes: List<RouteCandidate>,
    val routeCount: Int,
    val onlineCount: Int,
    val btCount: Int,
    val sourceCount: Int,
    val failedCount: Int,
    val message: String,
    val detail: String,
    val recommendationTitle: String,
    val recommendationDetail: String,
) {
    val canPlay: Boolean = bestRoute != null
}

internal data class PlayerOverlayState(
    val title: String,
    val episodeTitle: String,
    val routeLabel: String,
    val playbackState: String,
    val notice: String?,
    val error: String?,
)

internal data class RoutePanelUiState(
    val recommendedRoute: RouteCandidate?,
    val selectedRoute: RouteCandidate?,
    val totalCount: Int,
    val availableCount: Int,
    val onlineCount: Int,
    val btCount: Int,
    val failedCount: Int,
)

internal fun buildRouteUiState(
    selectedEpisode: Episode?,
    routes: List<RouteCandidate>,
    loading: Boolean,
    error: String?,
    selectedSourceId: String? = null,
    failedStreamIds: Set<String> = emptySet(),
): RouteUiState {
    val sortedRoutes = sortRoutesForUi(routes, failedStreamIds)
    val visibleRoutes = sortedRoutes.filter { selectedSourceId == null || it.sourceId == selectedSourceId }
    val bestRoute = sortedRoutes.firstOrNull { route ->
        route.stream.id !in failedStreamIds &&
            route.stream.protocol != StreamProtocol.WEBVIEW_ONLY
    }
    val onlineCount = routes.count { it.protocol != StreamProtocol.BITTORRENT && it.protocol != StreamProtocol.WEBVIEW_ONLY }
    val btCount = routes.count { it.protocol == StreamProtocol.BITTORRENT }
    val status = when {
        loading -> RouteLoadStatus.Loading
        bestRoute != null -> RouteLoadStatus.Ready
        error != null -> RouteLoadStatus.Failed
        selectedEpisode != null -> RouteLoadStatus.Empty
        else -> RouteLoadStatus.Idle
    }
    val episodeTitle = selectedEpisode?.title.orEmpty().ifBlank { "未选择剧集" }
    val message = when (status) {
        RouteLoadStatus.Idle -> "等待选择剧集"
        RouteLoadStatus.Loading -> "正在自动匹配最佳线路"
        RouteLoadStatus.Ready -> "已匹配 ${routes.size} 条线路"
        RouteLoadStatus.Empty -> "暂时没有可用播放线路"
        RouteLoadStatus.Failed -> "线路加载失败"
    }
    val detail = when (status) {
        RouteLoadStatus.Ready -> {
            val route = bestRoute
            if (route != null) {
                "${route.sourceName} · ${route.quality ?: route.protocol.uiProtocolName()} · 自动推荐"
            } else {
                "当前线路均不可用，可手动刷新或切换剧集"
            }
        }
        RouteLoadStatus.Loading -> "$episodeTitle · 在线源优先，BT 兜底"
        RouteLoadStatus.Failed -> error.orEmpty().ifBlank { "请稍后重试或换一个剧集" }
        RouteLoadStatus.Empty -> "可以切换剧集，或稍后再试其他来源"
        RouteLoadStatus.Idle -> "选择剧集后会自动开始匹配"
    }
    val recommendationTitle = when {
        bestRoute != null -> bestRoute.sourceName
        status == RouteLoadStatus.Loading -> "正在匹配在线源"
        status == RouteLoadStatus.Failed -> "线路匹配失败"
        status == RouteLoadStatus.Empty -> "暂无可播线路"
        else -> "等待自动匹配"
    }
    val recommendationDetail = bestRoute?.let { route ->
        listOfNotNull(
            route.routeName?.takeIf { it.isNotBlank() },
            route.quality?.takeIf { it.isNotBlank() },
            route.subgroup?.takeIf { it.isNotBlank() },
            route.protocol.uiProtocolName(),
        ).distinct().joinToString(" · ")
    } ?: detail

    return RouteUiState(
        status = status,
        selectedEpisodeTitle = episodeTitle,
        bestRoute = bestRoute,
        visibleRoutes = visibleRoutes,
        routeCount = routes.size,
        onlineCount = onlineCount,
        btCount = btCount,
        sourceCount = routes.map { it.sourceId }.distinct().size,
        failedCount = failedStreamIds.size,
        message = message,
        detail = detail,
        recommendationTitle = recommendationTitle,
        recommendationDetail = recommendationDetail,
    )
}

internal fun sortRoutesForUi(
    routes: List<RouteCandidate>,
    failedStreamIds: Set<String> = emptySet(),
): List<RouteCandidate> {
    return routes.sortedWith(
        compareByDescending<RouteCandidate> { routeUiScore(it, failedStreamIds) }
            .thenBy { it.sourceName }
            .thenBy { it.routeName.orEmpty() }
            .thenBy { it.title },
    )
}

internal fun nextPlayableRoute(
    routes: List<RouteCandidate>,
    currentStreamId: String,
    failedStreamIds: Set<String> = emptySet(),
): RouteCandidate? {
    val excludedIds = failedStreamIds + currentStreamId
    return sortRoutesForUi(routes, excludedIds)
        .distinctBy { it.stream.id }
        .firstOrNull { route ->
            route.stream.id !in excludedIds &&
                route.stream.protocol != StreamProtocol.WEBVIEW_ONLY
        }
}

internal fun buildRoutePanelUiState(
    routes: List<RouteCandidate>,
    selectedStreamId: String,
    failedStreamIds: Set<String> = emptySet(),
): RoutePanelUiState {
    val sortedRoutes = sortRoutesForUi(routes, failedStreamIds).distinctBy { it.stream.id }
    val availableRoutes = routes.filter { route ->
        route.stream.id !in failedStreamIds &&
            route.stream.protocol != StreamProtocol.WEBVIEW_ONLY
    }
    return RoutePanelUiState(
        recommendedRoute = sortedRoutes.firstOrNull { route ->
            route.stream.id !in failedStreamIds &&
                route.stream.protocol != StreamProtocol.WEBVIEW_ONLY
        },
        selectedRoute = routes.firstOrNull { it.stream.id == selectedStreamId },
        totalCount = routes.size,
        availableCount = availableRoutes.size,
        onlineCount = availableRoutes.count { it.protocol != StreamProtocol.BITTORRENT },
        btCount = availableRoutes.count { it.protocol == StreamProtocol.BITTORRENT },
        failedCount = failedStreamIds.count { failedId -> routes.any { it.stream.id == failedId } },
    )
}

internal fun buildPlayerOverlayState(
    title: String,
    episodeTitle: String,
    stream: MediaStream,
    route: RouteCandidate?,
    playbackState: String,
    notice: String?,
    error: String?,
): PlayerOverlayState {
    return PlayerOverlayState(
        title = title,
        episodeTitle = episodeTitle,
        routeLabel = playerRouteLabelForUi(stream, route),
        playbackState = playbackState,
        notice = notice,
        error = error,
    )
}

private fun routeUiScore(route: RouteCandidate, failedStreamIds: Set<String>): Int {
    var score = route.score + route.stream.sourceScore
    score += when (route.protocol) {
        StreamProtocol.HLS -> 1_400
        StreamProtocol.DASH -> 1_320
        StreamProtocol.PROGRESSIVE -> 1_180
        StreamProtocol.SMOOTH_STREAMING -> 980
        StreamProtocol.BITTORRENT -> -320
        StreamProtocol.RTSP -> 80
        StreamProtocol.UNKNOWN -> 0
        StreamProtocol.WEBVIEW_ONLY -> -800
    }
    val quality = listOfNotNull(route.quality, route.stream.quality, route.routeName).joinToString(" ").lowercase()
    score += when {
        "1080" in quality -> 42
        "720" in quality -> 28
        "2160" in quality || "4k" in quality -> 16
        "auto" in quality -> 8
        else -> 0
    }
    if (route.stream.id in failedStreamIds) score -= 2_000
    return score
}

private fun playerRouteLabelForUi(stream: MediaStream, route: RouteCandidate?): String {
    return listOfNotNull(
        route?.sourceName ?: stream.metadata["routeProviderName"],
        stream.quality?.takeIf { it.isNotBlank() },
        stream.protocol.uiProtocolName(),
    ).joinToString(" · ").ifBlank { stream.protocol.uiProtocolName() }
}

internal fun StreamProtocol.uiProtocolName(): String {
    return when (this) {
        StreamProtocol.BITTORRENT -> "BT"
        StreamProtocol.HLS -> "HLS"
        StreamProtocol.DASH -> "DASH"
        StreamProtocol.PROGRESSIVE -> "MP4"
        StreamProtocol.SMOOTH_STREAMING -> "Smooth"
        StreamProtocol.RTSP -> "RTSP"
        StreamProtocol.WEBVIEW_ONLY -> "WebView"
        StreamProtocol.UNKNOWN -> "未知"
    }
}
