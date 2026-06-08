package com.zfbml.aggregate.ui

import com.zfbml.aggregate.source.Episode
import com.zfbml.aggregate.source.DownloadPolicy
import com.zfbml.aggregate.source.MediaStream
import com.zfbml.aggregate.source.RouteCandidate
import com.zfbml.aggregate.source.SearchResult
import com.zfbml.aggregate.source.SourceCapability
import com.zfbml.aggregate.source.SourceManifest
import com.zfbml.aggregate.source.SourceSearchReport
import com.zfbml.aggregate.source.StreamProtocol
import com.zfbml.aggregate.source.catalog.BangumiScheduleDay

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

internal data class DetailPlaybackReadinessUiState(
    val headline: String,
    val summary: String,
    val primaryActionLabel: String,
    val routeLabel: String,
    val onlineLabel: String,
    val backupLabel: String,
    val cacheLabel: String,
    val cacheReason: String,
    val canPlay: Boolean,
    val cacheEnabled: Boolean,
)

internal enum class RoutePrefetchStatus {
    Queued,
    Warming,
    Ready,
    Empty,
}

internal data class RoutePrefetchItemUiState(
    val episodeId: String,
    val title: String,
    val status: RoutePrefetchStatus,
)

internal data class RoutePrefetchUiState(
    val items: List<RoutePrefetchItemUiState>,
    val headline: String,
    val summary: String,
) {
    val hasActivePrefetch: Boolean = items.any { it.status == RoutePrefetchStatus.Warming }
}

internal const val SearchAllSourcesId = "__all_search_sources__"

internal data class SearchSourceFilterUiState(
    val id: String,
    val name: String,
    val resultCount: Int,
    val failed: Boolean,
    val message: String?,
    val capabilityLabel: String,
    val selected: Boolean,
    val isAll: Boolean = false,
) {
    val statusLabel: String
        get() = when {
            isAll -> "\u5168\u90e8"
            failed && resultCount > 0 -> "\u90e8\u5206\u5f02\u5e38"
            failed -> "\u5f02\u5e38"
            resultCount > 0 -> "\u547d\u4e2d $resultCount"
            else -> "\u5df2\u7d22\u5f15"
        }
}

internal data class SearchIndexUiState(
    val headline: String,
    val summary: String,
    val resultCount: Int,
    val searchableSourceCount: Int,
    val failedSourceCount: Int,
    val selectedProviderId: String?,
    val sourceFilters: List<SearchSourceFilterUiState>,
)

internal data class ScheduleDayChipUiState(
    val weekdayId: Int,
    val label: String,
    val count: Int,
    val selected: Boolean,
    val today: Boolean,
)

internal data class HomeScheduleUiState(
    val selectedDay: BangumiScheduleDay?,
    val selectedItems: List<SearchResult>,
    val dayChips: List<ScheduleDayChipUiState>,
    val headline: String,
    val summary: String,
    val selectedDayTitle: String,
    val selectedDayAction: String,
    val emptyTitle: String,
    val emptySubtitle: String,
    val todayCount: Int,
    val weekCount: Int,
    val nextUpdateLabel: String,
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

internal data class PlayerDanmakuSafeAreaUiState(
    val topInsetDp: Int,
    val bottomInsetDp: Int,
    val startInsetDp: Int,
    val endInsetDp: Int,
)

internal data class PlayerCacheActionUiState(
    val enabled: Boolean,
    val title: String,
    val value: String,
    val reason: String,
    val actionLabel: String,
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

internal fun buildDetailPlaybackReadinessUiState(
    state: RouteUiState,
): DetailPlaybackReadinessUiState {
    val cacheAction = state.bestRoute?.stream?.let(::buildPlayerCacheActionUiState)
    val headline = when (state.status) {
        RouteLoadStatus.Ready -> "\u64ad\u653e\u5c31\u7eea"
        RouteLoadStatus.Loading -> "\u6b63\u5728\u5339\u914d\u64ad\u653e\u6e90"
        RouteLoadStatus.Failed -> "\u64ad\u653e\u6e90\u5f02\u5e38"
        RouteLoadStatus.Empty -> "\u6682\u65e0\u53ef\u64ad\u7ebf\u8def"
        RouteLoadStatus.Idle -> "\u9009\u96c6\u540e\u81ea\u52a8\u51c6\u5907"
    }
    val summary = when (state.status) {
        RouteLoadStatus.Ready -> {
            val route = state.bestRoute
            listOfNotNull(
                state.recommendationTitle.takeIf { it.isNotBlank() },
                state.recommendationReason.takeIf { it.isNotBlank() },
                route?.let { playerQualityLabelForUi(it.stream, it) },
            ).distinct().joinToString(" \u00b7 ")
        }
        RouteLoadStatus.Loading -> "\u4f18\u5148\u5339\u914d\u5728\u7ebf\u64ad\u653e\uff0c\u5fc5\u8981\u65f6\u542f\u7528 BT \u5907\u7528\u7ebf\u8def\u3002"
        RouteLoadStatus.Failed -> state.detail.ifBlank { "\u53ef\u91cd\u8bd5\u6216\u5207\u6362\u5176\u4ed6\u5267\u96c6\u3002" }
        RouteLoadStatus.Empty -> "\u5f53\u524d\u96c6\u6682\u65e0 HLS / MP4 / BT \u53ef\u64ad\u7ebf\u8def\uff0c\u53ef\u5207\u6362\u5267\u96c6\u6216\u7a0d\u540e\u91cd\u8bd5\u3002"
        RouteLoadStatus.Idle -> "\u8fdb\u5165\u8be6\u60c5\u540e\u4f1a\u81ea\u52a8\u4e3a\u9996\u96c6\u51c6\u5907\u6700\u4f18\u64ad\u653e\u7ebf\u8def\u3002"
    }
    val primaryActionLabel = when (state.status) {
        RouteLoadStatus.Ready -> if (state.bestRoute?.protocol == StreamProtocol.BITTORRENT) "\u8fb9\u4e0b\u8fb9\u64ad" else "\u64ad\u653e\u63a8\u8350"
        RouteLoadStatus.Loading -> "\u5339\u914d\u4e2d"
        RouteLoadStatus.Failed -> "\u67e5\u770b\u5f02\u5e38"
        RouteLoadStatus.Empty -> "\u7b49\u5f85\u8865\u6e90"
        RouteLoadStatus.Idle -> "\u81ea\u52a8\u51c6\u5907"
    }
    val onlineLabel = when {
        state.onlineCount > 0 -> "${state.onlineCount} \u5728\u7ebf"
        state.status == RouteLoadStatus.Loading -> "\u4f18\u5148\u5339\u914d"
        else -> "\u6682\u65e0\u5728\u7ebf"
    }
    val backupLabel = when {
        state.btCount > 0 -> "${state.btCount} BT"
        state.status == RouteLoadStatus.Loading -> "\u5907\u7528\u5f85\u547d"
        else -> "\u5907\u7528\u5f85\u547d"
    }
    return DetailPlaybackReadinessUiState(
        headline = headline,
        summary = summary,
        primaryActionLabel = primaryActionLabel,
        routeLabel = state.sourceCoverageLabel,
        onlineLabel = onlineLabel,
        backupLabel = backupLabel,
        cacheLabel = cacheAction?.value ?: "\u5f85\u7ebf\u8def",
        cacheReason = cacheAction?.reason ?: "\u64ad\u653e\u6e90\u5c31\u7eea\u540e\u5224\u65ad\u7f13\u5b58\u80fd\u529b",
        canPlay = state.canPlay,
        cacheEnabled = cacheAction?.enabled == true,
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

internal fun buildRoutePrefetchUiState(
    episodes: List<Episode>,
    currentEpisode: Episode?,
    warmingEpisodeIds: Set<String> = emptySet(),
    warmedEpisodeIds: Set<String> = emptySet(),
    emptyEpisodeIds: Set<String> = emptySet(),
    maxCount: Int = 2,
): RoutePrefetchUiState {
    val prefetchEpisodes = currentEpisode
        ?.let { routePrefetchWindow(episodes, it, maxCount) }
        ?: emptyList()
    val items = prefetchEpisodes.map { episode ->
        RoutePrefetchItemUiState(
            episodeId = episode.id,
            title = episode.index?.let { "\u7b2c $it \u96c6" }
                ?: episode.title.takeIf { it.isNotBlank() }
                ?: "\u90bb\u8fd1\u5267\u96c6",
            status = when (episode.id) {
                in warmedEpisodeIds -> RoutePrefetchStatus.Ready
                in warmingEpisodeIds -> RoutePrefetchStatus.Warming
                in emptyEpisodeIds -> RoutePrefetchStatus.Empty
                else -> RoutePrefetchStatus.Queued
            },
        )
    }
    val headline = when {
        items.isEmpty() -> "\u65e0\u90bb\u96c6\u53ef\u9884\u70ed"
        items.all { it.status == RoutePrefetchStatus.Ready } -> "\u90bb\u96c6\u7ebf\u8def\u5df2\u9884\u70ed"
        items.any { it.status == RoutePrefetchStatus.Warming } -> "\u6b63\u5728\u9884\u70ed\u90bb\u96c6"
        items.any { it.status == RoutePrefetchStatus.Queued } -> "\u90bb\u96c6\u7b49\u5f85\u9884\u70ed"
        else -> "\u90bb\u96c6\u6682\u5f85\u8865\u6e90"
    }
    val summary = when {
        items.isEmpty() -> "\u5f53\u524d\u5267\u96c6\u6682\u65e0\u53ef\u9884\u70ed\u7684\u524d\u540e\u96c6"
        items.all { it.status == RoutePrefetchStatus.Ready } -> "\u5207\u6362\u5230\u76f8\u90bb\u96c6\u65f6\u53ef\u76f4\u63a5\u547d\u4e2d\u7f13\u5b58\u7ebf\u8def"
        items.any { it.status == RoutePrefetchStatus.Warming } -> "\u6b63\u5728\u540e\u53f0\u63d0\u524d\u5339\u914d\u4e0b\u4e00\u6279\u64ad\u653e\u6e90"
        items.any { it.status == RoutePrefetchStatus.Queued } -> "\u64ad\u653e\u6e90\u5c31\u7eea\u540e\u4f1a\u81ea\u52a8\u5f00\u59cb\u9884\u70ed"
        else -> "\u90bb\u8fd1\u5267\u96c6\u6682\u672a\u547d\u4e2d\u53ef\u64ad\u653e\u7ebf\u8def"
    }
    return RoutePrefetchUiState(items = items, headline = headline, summary = summary)
}

internal fun buildSearchIndexUiState(
    manifests: List<SourceManifest>,
    report: SourceSearchReport?,
    results: List<SearchResult>,
    selectedProviderId: String? = null,
): SearchIndexUiState {
    val searchableManifests = manifests.filter { SourceCapability.SEARCH in it.capabilities }
    val resultCounts = results.groupingBy { it.providerId }.eachCount()
    val failuresByProvider = report?.failures?.associateBy { it.providerId }.orEmpty()
    val manifestById = manifests.associateBy { it.id }
    val providerIds = (searchableManifests.map { it.id } + resultCounts.keys + failuresByProvider.keys).distinct()
    val selectedId = selectedProviderId?.takeIf { it in providerIds }
    val failedCount = failuresByProvider.size
    val totalResults = results.size
    val selectedName = selectedId?.let { id ->
        manifestById[id]?.name ?: providerDisplayIdForSearch(id)
    }
    val headline = when {
        report == null -> "\u641c\u7d22\u7d22\u5f15\u5df2\u5c31\u7eea"
        totalResults > 0 && failedCount > 0 -> "\u547d\u4e2d $totalResults \u4e2a\u7ed3\u679c\uff0c$failedCount \u4e2a\u6e90\u5f02\u5e38"
        totalResults > 0 -> "\u547d\u4e2d $totalResults \u4e2a\u7ed3\u679c"
        failedCount > 0 -> "\u6682\u65e0\u547d\u4e2d\uff0c$failedCount \u4e2a\u6e90\u5f02\u5e38"
        else -> "\u6682\u65e0\u547d\u4e2d"
    }
    val summary = when {
        selectedId != null -> {
            val count = resultCounts[selectedId] ?: 0
            val suffix = failuresByProvider[selectedId]
                ?.let { "\uff0c\u8be5\u6e90\u5f02\u5e38: ${it.message}" }
                .orEmpty()
            "${selectedName ?: providerDisplayIdForSearch(selectedId)} \u00b7 $count \u4e2a\u7ed3\u679c$suffix"
        }
        report == null -> "\u5df2\u63a5\u5165 ${searchableManifests.size} \u4e2a\u53ef\u641c\u7d22\u6765\u6e90\uff0c\u641c\u7d22\u540e\u53ef\u6309\u6765\u6e90\u7b5b\u9009\u3002"
        totalResults > 0 -> "\u53ef\u6309\u6765\u6e90\u7f29\u5c0f\u7d22\u5f15\u8303\u56f4\uff0c\u8be6\u60c5\u9875\u4f1a\u7ee7\u7eed\u5339\u914d\u6700\u4f18\u7ebf\u8def\u3002"
        else -> "\u53ef\u6362\u756a\u540d\u3001\u522b\u540d\u6216\u5173\u952e\u8bcd\uff1b\u5f02\u5e38\u6e90\u4f1a\u5728\u4e0b\u65b9\u6807\u51fa\u3002"
    }
    val allFilter = SearchSourceFilterUiState(
        id = SearchAllSourcesId,
        name = "\u5168\u90e8\u7d22\u5f15",
        resultCount = totalResults,
        failed = failedCount > 0,
        message = null,
        capabilityLabel = "${searchableManifests.size} \u6e90",
        selected = selectedId == null,
        isAll = true,
    )
    val sourceFilters = providerIds.map { providerId ->
        val manifest = manifestById[providerId]
        val failure = failuresByProvider[providerId]
        SearchSourceFilterUiState(
            id = providerId,
            name = manifest?.name ?: providerDisplayIdForSearch(providerId),
            resultCount = resultCounts[providerId] ?: 0,
            failed = failure != null,
            message = failure?.message,
            capabilityLabel = manifest?.searchCapabilityLabel().orEmpty().ifBlank { "\u7d22\u5f15" },
            selected = selectedId == providerId,
        )
    }.sortedWith(
        compareByDescending<SearchSourceFilterUiState> { it.selected }
            .thenByDescending { it.resultCount }
            .thenBy { it.failed }
            .thenBy { it.name },
    )

    return SearchIndexUiState(
        headline = headline,
        summary = summary,
        resultCount = totalResults,
        searchableSourceCount = searchableManifests.size,
        failedSourceCount = failedCount,
        selectedProviderId = selectedId,
        sourceFilters = listOf(allFilter) + sourceFilters,
    )
}

internal fun searchResultsForProvider(
    results: List<SearchResult>,
    selectedProviderId: String?,
): List<SearchResult> {
    return selectedProviderId
        ?.let { providerId -> results.filter { it.providerId == providerId } }
        ?: results
}

internal fun buildHomeScheduleUiState(
    schedule: List<BangumiScheduleDay>,
    selectedDayId: Int,
    currentDayId: Int,
): HomeScheduleUiState {
    val selectedDay = schedule.firstOrNull { it.weekdayId == selectedDayId }
        ?: schedule.firstOrNull { it.weekdayId == currentDayId }
        ?: schedule.firstOrNull { it.items.isNotEmpty() }
        ?: schedule.firstOrNull()
    val selectedItems = selectedDay?.items.orEmpty()
    val todayCount = schedule.firstOrNull { it.weekdayId == currentDayId }?.items.orEmpty().size
    val weekCount = schedule.sumOf { it.items.size }
    val nextUpdateDay = schedule.firstActiveScheduleDayFrom(currentDayId)
    val nextUpdateLabel = when {
        nextUpdateDay == null -> "\u5f85\u540c\u6b65"
        nextUpdateDay.weekdayId == currentDayId -> "\u4eca\u65e5 ${nextUpdateDay.items.size} \u90e8"
        else -> "${compactScheduleWeekdayLabel(nextUpdateDay.weekdayCn, nextUpdateDay.weekdayId)} ${nextUpdateDay.items.size} \u90e8"
    }
    val dayChips = schedule.map { day ->
        ScheduleDayChipUiState(
            weekdayId = day.weekdayId,
            label = compactScheduleWeekdayLabel(day.weekdayCn, day.weekdayId),
            count = day.items.size,
            selected = day.weekdayId == selectedDay?.weekdayId,
            today = day.weekdayId == currentDayId,
        )
    }
    val selectedDayName = selectedDay?.weekdayCn ?: "\u8ffd\u756a\u65e5\u5386"
    val selectedDayAction = if (selectedItems.isNotEmpty()) {
        "${selectedItems.size} \u90e8"
    } else {
        ""
    }
    val headline = when {
        weekCount == 0 -> "\u65b0\u756a\u65f6\u95f4\u8868\u5f85\u540c\u6b65"
        selectedDay?.weekdayId == currentDayId && selectedItems.isNotEmpty() -> "\u4eca\u65e5\u66f4\u65b0 ${selectedItems.size} \u90e8"
        selectedItems.isNotEmpty() -> "$selectedDayName\u66f4\u65b0 ${selectedItems.size} \u90e8"
        else -> "$selectedDayName\u6682\u65e0\u653e\u9001"
    }
    val summary = when {
        weekCount == 0 -> "\u540c\u6b65 Bangumi \u6bcf\u65e5\u653e\u9001\u540e\uff0c\u8fd9\u91cc\u4f1a\u663e\u793a\u4eca\u65e5\u3001\u672c\u5468\u548c\u4e0b\u4e00\u6279\u66f4\u65b0\u3002"
        selectedItems.isNotEmpty() -> "\u672c\u5468\u5df2\u7d22\u5f15 $weekCount \u90e8\u653e\u9001\uff0c\u4e0b\u4e00\u6279\u66f4\u65b0\uff1a$nextUpdateLabel\u3002"
        else -> "\u672c\u5468\u5df2\u7d22\u5f15 $weekCount \u90e8\u653e\u9001\uff0c\u53ef\u5207\u6362\u5230\u5176\u4ed6\u65e5\u671f\u7ee7\u7eed\u770b\u3002"
    }
    return HomeScheduleUiState(
        selectedDay = selectedDay,
        selectedItems = selectedItems,
        dayChips = dayChips,
        headline = headline,
        summary = summary,
        selectedDayTitle = selectedDayName,
        selectedDayAction = selectedDayAction,
        emptyTitle = "\u6682\u65e0\u5f53\u65e5\u653e\u9001\u6570\u636e",
        emptySubtitle = "\u53ef\u4ee5\u5207\u6362\u5176\u4ed6\u65e5\u671f\uff0c\u6216\u76f4\u63a5\u641c\u7d22\u756a\u540d\u3002",
        todayCount = todayCount,
        weekCount = weekCount,
        nextUpdateLabel = nextUpdateLabel,
    )
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

internal fun buildPlayerDanmakuSafeAreaUiState(
    compact: Boolean,
    controlsVisible: Boolean,
    controlsLocked: Boolean,
    panelOpen: Boolean,
    noticeVisible: Boolean = false,
): PlayerDanmakuSafeAreaUiState {
    val visibleControls = controlsVisible && !controlsLocked
    val topInset = when {
        !visibleControls -> 8
        compact -> 40
        else -> 58
    }
    val bottomInset = when {
        !visibleControls -> 8
        panelOpen && compact -> 22
        panelOpen -> 112
        compact -> if (noticeVisible) 112 else 86
        else -> if (noticeVisible) 162 else 128
    }
    val startInset = when {
        !compact && controlsLocked -> 72
        !compact && visibleControls && !panelOpen -> 72
        else -> 0
    }
    val endInset = when {
        compact || controlsLocked || !visibleControls -> 0
        panelOpen -> 414
        else -> 84
    }
    return PlayerDanmakuSafeAreaUiState(
        topInsetDp = topInset,
        bottomInsetDp = bottomInset,
        startInsetDp = startInset,
        endInsetDp = endInset,
    )
}

internal fun buildPlayerCacheActionUiState(stream: MediaStream): PlayerCacheActionUiState {
    val media3Cacheable = stream.protocol in setOf(
        StreamProtocol.HLS,
        StreamProtocol.DASH,
        StreamProtocol.SMOOTH_STREAMING,
        StreamProtocol.PROGRESSIVE,
    )
    return when {
        stream.downloadPolicy == DownloadPolicy.BlockedDrm || stream.drmInfo != null -> PlayerCacheActionUiState(
            enabled = false,
            title = "\u7f13\u5b58",
            value = "DRM",
            reason = "DRM \u53d7\u9650\uff0c\u4e0d\u52a0\u5165\u79bb\u7ebf\u961f\u5217",
            actionLabel = "\u4e0d\u53ef\u7f13\u5b58",
        )
        stream.downloadPolicy == DownloadPolicy.BlockedWebViewOnly ||
            stream.protocol == StreamProtocol.WEBVIEW_ONLY -> PlayerCacheActionUiState(
            enabled = false,
            title = "\u7f13\u5b58",
            value = "\u55c5\u63a2",
            reason = "\u7f51\u9875\u55c5\u63a2\u6e90\u9700\u73b0\u573a\u64ad\u653e\uff0c\u6682\u4e0d\u652f\u6301\u79bb\u7ebf",
            actionLabel = "\u4e0d\u53ef\u7f13\u5b58",
        )
        stream.protocol == StreamProtocol.BITTORRENT -> PlayerCacheActionUiState(
            enabled = false,
            title = "\u7f13\u5b58",
            value = "\u8fb9\u4e0b\u8fb9\u64ad",
            reason = "BT \u7ebf\u8def\u7531\u79cd\u5b50\u5f15\u64ce\u8fb9\u4e0b\u8fb9\u64ad",
            actionLabel = "\u67e5\u770b BT \u7f13\u5b58",
        )
        stream.downloadPolicy == DownloadPolicy.CacheOnly && media3Cacheable -> PlayerCacheActionUiState(
            enabled = true,
            title = "\u7f13\u5b58",
            value = "\u4ec5\u7f13\u5b58",
            reason = "Media3 \u4f1a\u6309\u8be5\u7ebf\u8def\u7684\u7f13\u5b58\u7b56\u7565\u52a0\u5165\u961f\u5217",
            actionLabel = "\u7f13\u5b58\u672c\u96c6",
        )
        stream.downloadPolicy == DownloadPolicy.Allowed && media3Cacheable -> PlayerCacheActionUiState(
            enabled = true,
            title = "\u7f13\u5b58",
            value = "\u53ef\u79bb\u7ebf",
            reason = "Media3 \u79bb\u7ebf\u7f13\u5b58\u961f\u5217",
            actionLabel = "\u7f13\u5b58\u672c\u96c6",
        )
        else -> PlayerCacheActionUiState(
            enabled = false,
            title = "\u7f13\u5b58",
            value = stream.protocol.uiProtocolName(),
            reason = "${stream.protocol.uiProtocolName()} \u534f\u8bae\u6682\u672a\u63a5\u5165\u79bb\u7ebf\u7f13\u5b58",
            actionLabel = "\u4e0d\u53ef\u7f13\u5b58",
        )
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

private fun SourceManifest.searchCapabilityLabel(): String {
    val labels = listOfNotNull(
        "\u7d22\u5f15",
        if (SourceCapability.STREAM in capabilities) "\u64ad\u653e" else null,
        if (SourceCapability.BITTORRENT in capabilities) "BT" else null,
        if (SourceCapability.WEBVIEW_SNIFF in capabilities) "\u55c5\u63a2" else null,
        if (supportsDownload || SourceCapability.DOWNLOAD in capabilities) "\u7f13\u5b58" else null,
        if (SourceCapability.EPISODES in capabilities) "\u9009\u96c6" else null,
    )
    return labels.distinct().take(3).joinToString(" \u00b7 ")
}

private fun List<BangumiScheduleDay>.firstActiveScheduleDayFrom(currentDayId: Int): BangumiScheduleDay? {
    val orderedWeekdays = (0 until 7).map { offset ->
        ((currentDayId - 1 + offset).floorMod(7)) + 1
    }
    return orderedWeekdays.firstNotNullOfOrNull { weekdayId ->
        firstOrNull { it.weekdayId == weekdayId && it.items.isNotEmpty() }
    }
}

private fun compactScheduleWeekdayLabel(weekdayCn: String, weekdayId: Int): String {
    return weekdayCn
        .removePrefix("\u661f\u671f")
        .ifBlank { weekdayId.toString() }
}

private fun Int.floorMod(divisor: Int): Int {
    return ((this % divisor) + divisor) % divisor
}

private fun providerDisplayIdForSearch(providerId: String): String {
    return providerId.replace('-', ' ').replaceFirstChar { it.uppercase() }
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
