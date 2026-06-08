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
    val loadingSteps: List<RouteLoadingStepUiState>,
    val routeCount: Int,
    val onlineCount: Int,
    val btCount: Int,
    val onlineSourceCount: Int,
    val btSourceCount: Int,
    val sourceCount: Int,
    val sourceCoverageLabel: String,
    val loadOriginLabel: String,
    val failedCount: Int,
    val message: String,
    val detail: String,
    val recommendationTitle: String,
    val recommendationDetail: String,
    val recommendationReason: String,
) {
    val canPlay: Boolean = bestRoute != null
}

internal data class RouteLoadingStepUiState(
    val title: String,
    val value: String,
    val active: Boolean,
)

internal data class PlayerOverlayState(
    val title: String,
    val episodeTitle: String,
    val sourceLabel: String,
    val qualityLabel: String,
    val routeLabel: String,
    val playbackState: String,
    val statusLabel: String,
    val notice: String?,
    val error: String?,
)

internal enum class PlayerSeekFeedbackPlacement {
    Center,
    Start,
    End,
}

internal data class RoutePanelUiState(
    val recommendedRoute: RouteCandidate?,
    val selectedRoute: RouteCandidate?,
    val totalCount: Int,
    val availableCount: Int,
    val onlineCount: Int,
    val btCount: Int,
    val failedCount: Int,
    val recommendationReason: String,
)

internal const val RouteAllSourcesId = "__all_sources__"

internal data class RouteSourceGroupUiState(
    val id: String,
    val name: String,
    val totalCount: Int,
    val playableCount: Int,
    val onlineCount: Int,
    val btCount: Int,
    val webOnlyCount: Int,
    val failedCount: Int,
    val hasSelected: Boolean,
    val hasRecommended: Boolean,
    val isAll: Boolean = false,
    val isFilterSelected: Boolean = false,
) {
    val sourceSummary: String
        get() = when {
            onlineCount > 0 && btCount > 0 -> "${playableCount}可播 · ${onlineCount}在线 · ${btCount}BT"
            onlineCount > 0 -> "${playableCount}可播 · ${onlineCount}在线"
            btCount > 0 -> "${playableCount}可播 · ${btCount}备用"
            playableCount > 0 -> "${playableCount}可播"
            webOnlyCount > 0 -> "${webOnlyCount}网页兜底"
            failedCount > 0 -> "${failedCount}条失败"
            else -> "待检测"
        }
}

internal fun buildRouteUiState(
    selectedEpisode: Episode?,
    routes: List<RouteCandidate>,
    loading: Boolean,
    error: String?,
    selectedSourceId: String? = null,
    failedStreamIds: Set<String> = emptySet(),
    loadedFromCache: Boolean = false,
): RouteUiState {
    val sortedRoutes = sortRoutesForUi(routes, failedStreamIds)
    val visibleRoutes = sortedRoutes.filter { selectedSourceId == null || it.sourceId == selectedSourceId }
    val bestRoute = firstPlayableRouteForAutoplay(routes, failedStreamIds)
    val onlineRoutes = routes.filter { it.protocol != StreamProtocol.BITTORRENT && it.protocol != StreamProtocol.WEBVIEW_ONLY }
    val btRoutes = routes.filter { it.protocol == StreamProtocol.BITTORRENT }
    val onlineCount = onlineRoutes.size
    val btCount = btRoutes.size
    val onlineSourceCount = onlineRoutes.map { it.sourceId }.distinct().size
    val btSourceCount = btRoutes.map { it.sourceId }.distinct().size
    val sourceCount = routes.map { it.sourceId }.distinct().size
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
        RouteLoadStatus.Loading -> "正在匹配播放源"
        RouteLoadStatus.Ready -> "已找到推荐播放源"
        RouteLoadStatus.Empty -> "暂时没有可用播放源"
        RouteLoadStatus.Failed -> "播放源加载失败"
    }
    val detail = when (status) {
        RouteLoadStatus.Ready -> {
            val route = bestRoute
            if (route != null) {
                "${route.sourceName} · ${route.quality ?: route.protocol.uiProtocolName()} · 自动推荐"
            } else {
                "当前播放源均不可用，可手动刷新或切换剧集"
            }
        }
        RouteLoadStatus.Loading -> "$episodeTitle · 优先匹配在线播放"
        RouteLoadStatus.Failed -> error.orEmpty().ifBlank { "请稍后重试或换一个剧集" }
        RouteLoadStatus.Empty -> "可以切换剧集，或稍后再试其他来源"
        RouteLoadStatus.Idle -> "选择剧集后会自动开始匹配"
    }
    val recommendationTitle = when {
        bestRoute != null -> bestRoute.sourceName
        status == RouteLoadStatus.Loading -> "正在匹配播放源"
        status == RouteLoadStatus.Failed -> "播放源匹配失败"
        status == RouteLoadStatus.Empty -> "暂无可用播放源"
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
    val recommendationReason = bestRoute?.let { routeRecommendationReason(it) } ?: when (status) {
        RouteLoadStatus.Loading -> "\u6b63\u5728\u5339\u914d\u5728\u7ebf\u6e90"
        RouteLoadStatus.Failed -> "\u52a0\u8f7d\u5931\u8d25\uff0c\u53ef\u91cd\u8bd5"
        RouteLoadStatus.Empty -> "\u7b49\u5f85\u8865\u6e90"
        RouteLoadStatus.Idle -> "\u9009\u96c6\u540e\u81ea\u52a8\u63a8\u8350"
        RouteLoadStatus.Ready -> "\u6682\u65e0\u53ef\u64ad\u653e\u7ebf\u8def"
    }
    val loadOriginLabel = when {
        status == RouteLoadStatus.Ready && loadedFromCache -> "预取命中"
        status == RouteLoadStatus.Ready -> "实时匹配"
        status == RouteLoadStatus.Loading -> "实时匹配"
        status == RouteLoadStatus.Failed -> "可重试"
        status == RouteLoadStatus.Empty -> "待补源"
        else -> "待选集"
    }

    return RouteUiState(
        status = status,
        selectedEpisodeTitle = episodeTitle,
        bestRoute = bestRoute,
        visibleRoutes = visibleRoutes,
        loadingSteps = buildRouteLoadingSteps(
            status = status,
            episodeTitle = episodeTitle,
            onlineCount = onlineCount,
            btCount = btCount,
            onlineSourceCount = onlineSourceCount,
            btSourceCount = btSourceCount,
        ),
        routeCount = routes.size,
        onlineCount = onlineCount,
        btCount = btCount,
        onlineSourceCount = onlineSourceCount,
        btSourceCount = btSourceCount,
        sourceCount = sourceCount,
        sourceCoverageLabel = routeSourceCoverageLabel(
            status = status,
            routeCount = routes.size,
            sourceCount = sourceCount,
            onlineCount = onlineCount,
            onlineSourceCount = onlineSourceCount,
            btCount = btCount,
        ),
        loadOriginLabel = loadOriginLabel,
        failedCount = failedStreamIds.size,
        message = message,
        detail = detail,
        recommendationTitle = recommendationTitle,
        recommendationDetail = recommendationDetail,
        recommendationReason = recommendationReason,
    )
}

internal fun buildRouteLoadingSteps(
    status: RouteLoadStatus,
    episodeTitle: String,
    onlineCount: Int,
    btCount: Int,
    onlineSourceCount: Int = 0,
    btSourceCount: Int = 0,
): List<RouteLoadingStepUiState> {
    val hasSelectedEpisode = status != RouteLoadStatus.Idle
    val matching = status == RouteLoadStatus.Loading
    return listOf(
        RouteLoadingStepUiState(
            title = "选集",
            value = episodeTitle,
            active = hasSelectedEpisode,
        ),
        RouteLoadingStepUiState(
            title = "在线源",
            value = when {
                onlineCount > 0 -> routeSourceCountLabel(onlineSourceCount, onlineCount)
                matching -> "优先匹配"
                status == RouteLoadStatus.Failed -> "未命中"
                else -> "待匹配"
            },
            active = matching || onlineCount > 0,
        ),
        RouteLoadingStepUiState(
            title = "备用源",
            value = when {
                btCount > 0 -> routeSourceCountLabel(btSourceCount, btCount)
                matching -> "必要时启用"
                status == RouteLoadStatus.Failed -> "可重试"
                else -> "兜底"
            },
            active = matching || btCount > 0 || status == RouteLoadStatus.Failed,
        ),
    )
}

internal fun routeSourceCoverageLabel(
    status: RouteLoadStatus,
    routeCount: Int,
    sourceCount: Int,
    onlineCount: Int,
    onlineSourceCount: Int,
    btCount: Int,
): String {
    return when {
        sourceCount > 1 -> "${sourceCount}源 · ${routeCount}线"
        onlineSourceCount > 1 -> "${onlineSourceCount}在线源"
        onlineCount > 0 && btCount > 0 -> "在线+备用"
        onlineCount > 1 -> "${onlineCount}在线"
        routeCount > 1 -> "${routeCount}线"
        routeCount == 1 -> "单源可播"
        status == RouteLoadStatus.Loading -> "匹配中"
        else -> "待匹配"
    }
}

internal fun playerRouteCoverageLabel(routes: List<RouteCandidate>): String {
    val visibleRoutes = routes.distinctBy { it.stream.id }
    val routeCount = visibleRoutes.size
    val sourceCount = visibleRoutes.map { it.sourceId }.distinct().size
    return when {
        routeCount <= 0 -> "无线路"
        sourceCount > 1 && routeCount > sourceCount -> "${sourceCount}源 · ${routeCount}线"
        sourceCount > 1 -> "${sourceCount}源"
        routeCount > 1 -> "${routeCount}线"
        else -> "单线"
    }
}

private fun routeSourceCountLabel(sourceCount: Int, routeCount: Int): String {
    return if (sourceCount > 1) {
        "${sourceCount}源 · ${routeCount}线"
    } else {
        "${routeCount}线"
    }
}

internal fun buildRouteSourceGroups(
    routes: List<RouteCandidate>,
    selectedSourceId: String? = null,
    selectedStreamId: String? = null,
    recommendedSourceId: String? = null,
    recommendedStreamId: String? = null,
    failedStreamIds: Set<String> = emptySet(),
    includeAll: Boolean = false,
): List<RouteSourceGroupUiState> {
    val sourceGroups = routes
        .groupBy { it.sourceId }
        .map { (sourceId, sourceRoutes) ->
            sourceRoutes.toRouteSourceGroup(
                id = sourceId,
                name = sourceRoutes.firstOrNull()?.sourceName ?: sourceId,
                selectedSourceId = selectedSourceId,
                selectedStreamId = selectedStreamId,
                recommendedSourceId = recommendedSourceId,
                recommendedStreamId = recommendedStreamId,
                failedStreamIds = failedStreamIds,
            )
        }
        .sortedWith(
            compareByDescending<RouteSourceGroupUiState> { it.isFilterSelected }
                .thenByDescending { it.hasSelected }
                .thenByDescending { it.hasRecommended }
                .thenByDescending { it.onlineCount > 0 }
                .thenByDescending { it.playableCount }
                .thenBy { it.name },
        )

    if (!includeAll) return sourceGroups

    val allGroup = routes.toRouteSourceGroup(
        id = RouteAllSourcesId,
        name = "全部播放源",
        selectedSourceId = selectedSourceId,
        selectedStreamId = selectedStreamId,
        recommendedSourceId = recommendedSourceId,
        recommendedStreamId = recommendedStreamId,
        failedStreamIds = failedStreamIds,
        isAll = true,
    )
    return listOf(allGroup) + sourceGroups
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

internal fun firstPlayableRouteForAutoplay(
    routes: List<RouteCandidate>,
    failedStreamIds: Set<String> = emptySet(),
): RouteCandidate? {
    return sortRoutesForUi(routes, failedStreamIds)
        .distinctBy { it.stream.id }
        .firstOrNull { route ->
            route.stream.id !in failedStreamIds &&
                route.stream.protocol != StreamProtocol.WEBVIEW_ONLY
        }
}

internal fun routePanelVisibleRoutes(
    routes: List<RouteCandidate>,
    selectedSourceId: String? = null,
    failedStreamIds: Set<String> = emptySet(),
): List<RouteCandidate> {
    val sourceFilteredRoutes = selectedSourceId
        ?.let { sourceId -> routes.filter { it.sourceId == sourceId } }
        ?: routes
    return sourceFilteredRoutes
        .sortedWith(
            compareBy<RouteCandidate> { routePanelAvailabilityRank(it, failedStreamIds) }
                .thenByDescending { routeUiScore(it, failedStreamIds) }
                .thenBy { it.sourceName }
                .thenBy { it.routeName.orEmpty() }
                .thenBy { it.title },
        )
        .distinctBy { it.stream.id }
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

internal fun preferredRouteForNextEpisode(
    routes: List<RouteCandidate>,
    currentSourceId: String?,
    currentProviderId: String?,
    failedStreamIds: Set<String> = emptySet(),
): RouteCandidate? {
    val playableRoutes = sortRoutesForUi(routes, failedStreamIds)
        .distinctBy { it.stream.id }
        .filter { route ->
            route.stream.id !in failedStreamIds &&
                route.stream.protocol != StreamProtocol.WEBVIEW_ONLY
        }
    if (playableRoutes.isEmpty()) return null

    return playableRoutes.firstOrNull { route ->
        currentSourceId != null && route.sourceId == currentSourceId
    } ?: playableRoutes.firstOrNull { route ->
        currentProviderId != null && route.stream.providerId == currentProviderId
    } ?: playableRoutes.first()
}

internal fun routePrefetchWindow(
    episodes: List<Episode>,
    currentEpisode: Episode,
    maxCount: Int = 2,
): List<Episode> {
    if (maxCount <= 0 || episodes.size <= 1) return emptyList()
    val currentIndex = episodes.indexOfFirst { it.id == currentEpisode.id }
    if (currentIndex < 0) return emptyList()

    val result = mutableListOf<Episode>()
    for (index in (currentIndex + 1) until episodes.size) {
        result.add(episodes[index])
        if (result.size >= maxCount) return result
    }
    for (index in (currentIndex - 1) downTo 0) {
        result.add(episodes[index])
        if (result.size >= maxCount) return result
    }
    return result
}

internal fun nextEpisodeForPlayer(
    episodes: List<Episode>,
    currentEpisode: Episode,
): Episode? {
    val currentIndex = episodes.indexOfFirst { it.id == currentEpisode.id }
    if (currentIndex < 0) return null
    return episodes.getOrNull(currentIndex + 1)
}

internal fun playerProgressPollDelayMs(
    isPlaying: Boolean,
    controlsVisible: Boolean,
    panelOpen: Boolean,
): Long {
    return when {
        isPlaying && controlsVisible && !panelOpen -> 100L
        isPlaying && controlsVisible -> 160L
        isPlaying -> 250L
        controlsVisible -> 300L
        else -> 500L
    }
}

internal fun playerSeekTargetMs(
    currentPositionMs: Long,
    deltaMs: Long,
    durationMs: Long,
): Long {
    val targetMs = currentPositionMs + deltaMs
    return if (durationMs > 0L) {
        targetMs.coerceIn(0L, durationMs)
    } else {
        targetMs.coerceAtLeast(0L)
    }
}

internal fun playerDoubleTapSeekDeltaMs(
    tapX: Float,
    surfaceWidthPx: Int,
    stepMs: Long = 10_000L,
): Long? {
    if (surfaceWidthPx <= 0 || stepMs <= 0L) return null
    return if (tapX < surfaceWidthPx / 2f) -stepMs else stepMs
}

internal fun playerSeekFeedbackPlacement(
    deltaMs: Long,
    fromGesture: Boolean,
): PlayerSeekFeedbackPlacement {
    return when {
        !fromGesture || deltaMs == 0L -> PlayerSeekFeedbackPlacement.Center
        deltaMs < 0L -> PlayerSeekFeedbackPlacement.Start
        else -> PlayerSeekFeedbackPlacement.End
    }
}

internal fun playerSeekShouldRevealControls(fromGesture: Boolean): Boolean {
    return !fromGesture
}

internal fun recommendedSourceIdForRoutes(
    routes: List<RouteCandidate>,
    failedStreamIds: Set<String> = emptySet(),
): String? {
    return firstPlayableRouteForAutoplay(routes, failedStreamIds)?.sourceId
}

internal fun routeRecommendationReason(route: RouteCandidate?): String {
    if (route == null) return "\u6682\u65e0\u53ef\u64ad\u653e\u7ebf\u8def"
    val protocolReason = when (route.protocol) {
        StreamProtocol.HLS,
        StreamProtocol.DASH,
        StreamProtocol.SMOOTH_STREAMING -> "\u5728\u7ebf\u64ad\u653e\u4f18\u5148"
        StreamProtocol.PROGRESSIVE -> "\u76f4\u8fde\u64ad\u653e\u4f18\u5148"
        StreamProtocol.BITTORRENT -> "\u5728\u7ebf\u6e90\u4e0d\u8db3\u65f6\u5907\u7528"
        StreamProtocol.RTSP -> "\u5b9e\u65f6\u6d41\u5019\u9009"
        StreamProtocol.WEBVIEW_ONLY -> "\u7f51\u9875\u55c5\u63a2\u515c\u5e95"
        StreamProtocol.UNKNOWN -> "\u672a\u77e5\u534f\u8bae\u5019\u9009"
    }
    val qualityReason = routeDecisionQualityLabel(route)?.let { "\u6e05\u6670\u5ea6 $it" }
    val scoreReason = if (route.score > 0 || route.stream.sourceScore > 0) {
        "\u7efc\u5408\u8bc4\u5206\u9760\u524d"
    } else {
        null
    }
    return listOfNotNull(protocolReason, qualityReason, scoreReason)
        .distinct()
        .joinToString(" \u00b7 ")
}

internal fun buildRoutePanelUiState(
    routes: List<RouteCandidate>,
    selectedStreamId: String,
    failedStreamIds: Set<String> = emptySet(),
): RoutePanelUiState {
    val availableRoutes = routes.filter { route ->
        route.stream.id !in failedStreamIds &&
            route.stream.protocol != StreamProtocol.WEBVIEW_ONLY
    }
    val recommendedRoute = firstPlayableRouteForAutoplay(routes, failedStreamIds)
    return RoutePanelUiState(
        recommendedRoute = recommendedRoute,
        selectedRoute = routes.firstOrNull { it.stream.id == selectedStreamId },
        totalCount = routes.size,
        availableCount = availableRoutes.size,
        onlineCount = availableRoutes.count { it.protocol != StreamProtocol.BITTORRENT },
        btCount = availableRoutes.count { it.protocol == StreamProtocol.BITTORRENT },
        failedCount = failedStreamIds.count { failedId -> routes.any { it.stream.id == failedId } },
        recommendationReason = routeRecommendationReason(recommendedRoute),
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
    val playbackStateLabel = playerPlaybackStateLabelForUi(playbackState)
    return PlayerOverlayState(
        title = title,
        episodeTitle = episodeTitle,
        sourceLabel = playerSourceLabelForUi(stream, route),
        qualityLabel = playerQualityLabelForUi(stream, route),
        routeLabel = playerRouteLabelForUi(stream, route),
        playbackState = playbackStateLabel,
        statusLabel = playerStatusLabelForUi(playbackStateLabel, notice, error),
        notice = notice,
        error = error,
    )
}

private fun routePanelAvailabilityRank(route: RouteCandidate, failedStreamIds: Set<String>): Int {
    return when {
        route.stream.id in failedStreamIds -> 2
        route.stream.protocol == StreamProtocol.WEBVIEW_ONLY -> 3
        else -> 0
    }
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

private fun routeDecisionQualityLabel(route: RouteCandidate): String? {
    val quality = listOfNotNull(route.quality, route.stream.quality, route.routeName)
        .firstOrNull { it.isNotBlank() }
        ?.lowercase()
        ?: return null
    return when {
        "2160" in quality || "4k" in quality -> "4K"
        "1080" in quality -> "1080p"
        "720" in quality -> "720p"
        "auto" in quality || "\u81ea\u52a8" in quality -> "\u81ea\u52a8"
        else -> null
    }
}

private fun playerSourceLabelForUi(stream: MediaStream, route: RouteCandidate?): String {
    return route?.sourceName
        ?: stream.metadata["routeProviderName"]
        ?: stream.providerId.takeIf { it.isNotBlank() }
        ?: "自动源"
}

private fun playerQualityLabelForUi(stream: MediaStream, route: RouteCandidate?): String {
    val quality = route?.quality
        ?: stream.quality?.takeIf { it.isNotBlank() }
    return quality
        ?.takeIf { !it.equals("auto", ignoreCase = true) && it != "自动" }
        ?: "自动"
}

private fun playerRouteLabelForUi(stream: MediaStream, route: RouteCandidate?): String {
    val quality = route?.quality
        ?: stream.quality?.takeIf { it.isNotBlank() }
    val visibleQuality = quality?.takeIf { value ->
        value.isNotBlank() && !value.equals("auto", ignoreCase = true) && value != "自动"
    }
    val routeName = route?.routeName?.takeIf { name ->
        name.isNotBlank() && visibleQuality?.let { qualityText ->
            !name.contains(qualityText, ignoreCase = true)
        } != false
    }
    val label = listOfNotNull(visibleQuality, routeName)
        .distinct()
        .joinToString(" · ")
    return label.ifBlank {
        if (stream.protocol == StreamProtocol.BITTORRENT) "边下边播" else "自动最佳"
    }
}

private fun List<RouteCandidate>.toRouteSourceGroup(
    id: String,
    name: String,
    selectedSourceId: String?,
    selectedStreamId: String?,
    recommendedSourceId: String?,
    recommendedStreamId: String?,
    failedStreamIds: Set<String>,
    isAll: Boolean = false,
): RouteSourceGroupUiState {
    return RouteSourceGroupUiState(
        id = id,
        name = name,
        totalCount = size,
        playableCount = count { it.stream.id !in failedStreamIds && it.protocol != StreamProtocol.WEBVIEW_ONLY },
        onlineCount = count { route ->
            route.stream.id !in failedStreamIds &&
                route.protocol != StreamProtocol.BITTORRENT &&
                route.protocol != StreamProtocol.WEBVIEW_ONLY
        },
        btCount = count { it.stream.id !in failedStreamIds && it.protocol == StreamProtocol.BITTORRENT },
        webOnlyCount = count { it.protocol == StreamProtocol.WEBVIEW_ONLY },
        failedCount = count { it.stream.id in failedStreamIds },
        hasSelected = selectedStreamId != null && any { it.stream.id == selectedStreamId },
        hasRecommended = recommendedStreamId != null && any { it.stream.id == recommendedStreamId } ||
            recommendedSourceId != null && (isAll || any { it.sourceId == recommendedSourceId }),
        isAll = isAll,
        isFilterSelected = if (isAll) selectedSourceId == null else id == selectedSourceId,
    )
}

private fun playerPlaybackStateLabelForUi(playbackState: String): String {
    return when (playbackState.uppercase()) {
        "IDLE" -> "等待播放"
        "BUFFERING" -> "缓冲中"
        "READY" -> "播放就绪"
        "ENDED" -> "已播完"
        else -> playbackState.ifBlank { "播放中" }
    }
}

private fun playerStatusLabelForUi(playbackState: String, notice: String?, error: String?): String {
    return when {
        !error.isNullOrBlank() -> "异常"
        !notice.isNullOrBlank() -> "切源中"
        playbackState.contains("等待", ignoreCase = true) -> "等待"
        playbackState.contains("缓冲", ignoreCase = true) -> "缓冲中"
        playbackState.contains("就绪", ignoreCase = true) -> "已就绪"
        playbackState.contains("播放", ignoreCase = true) -> "播放中"
        playbackState.contains("已播完", ignoreCase = true) -> "已播完"
        playbackState.isNotBlank() -> playbackState
        else -> "自动"
    }
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
