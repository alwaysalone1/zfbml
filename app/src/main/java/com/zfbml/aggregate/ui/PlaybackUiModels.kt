package com.zfbml.aggregate.ui

import com.zfbml.aggregate.source.Episode
import com.zfbml.aggregate.source.DownloadPolicy
import com.zfbml.aggregate.source.MediaDetail
import com.zfbml.aggregate.source.MediaStream
import com.zfbml.aggregate.source.RouteCandidate
import com.zfbml.aggregate.source.SearchResult
import com.zfbml.aggregate.source.SourceCapability
import com.zfbml.aggregate.source.SourceManifest
import com.zfbml.aggregate.source.SourceSearchReport
import com.zfbml.aggregate.source.StreamProtocol
import com.zfbml.aggregate.source.catalog.BangumiCategory
import com.zfbml.aggregate.source.catalog.BangumiScheduleDay
import com.zfbml.aggregate.torrent.TorrentEngineState

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

internal data class DetailEpisodeSummaryUiState(
    val headline: String,
    val summary: String,
    val currentEpisodeLabel: String,
    val episodeCountLabel: String,
    val routeStatusLabel: String,
    val routeActionLabel: String,
    val tone: SourceLibraryTone,
    val chips: List<SearchResultChipUiState>,
)

internal data class DetailRouteResolutionUiState(
    val title: String,
    val subtitle: String,
    val detail: String,
    val chips: List<SourceLibraryChipUiState>,
    val showProgress: Boolean,
    val tone: SourceLibraryTone,
)

internal data class DetailEntryUiState(
    val headline: String,
    val summary: String,
    val providerLabel: String,
    val typeLabel: String,
    val detailStatusLabel: String,
    val episodeLabel: String,
    val actionLabel: String,
    val tone: SourceLibraryTone,
    val chips: List<SearchResultChipUiState>,
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

internal data class SearchSuggestionUiState(
    val keyword: String,
    val subtitle: String,
    val tone: SourceLibraryTone,
)

internal data class SearchLandingUiState(
    val headline: String,
    val summary: String,
    val inputTitle: String,
    val inputSubtitle: String,
    val inputPlaceholder: String,
    val suggestionTitle: String,
    val suggestions: List<SearchSuggestionUiState>,
    val scheduleSuggestionCount: Int,
    val searchableSourceCount: Int,
)

internal data class SearchIdleHintUiState(
    val title: String,
    val subtitle: String,
    val chips: List<SourceLibraryChipUiState>,
    val actionLabel: String,
)

internal data class SearchResultsSectionUiState(
    val headerTitle: String,
    val headerSubtitle: String,
    val emptyTitle: String,
    val emptySubtitle: String,
)

internal data class SearchResultChipUiState(
    val label: String,
    val tone: SourceLibraryTone,
)

internal data class SearchResultCardUiState(
    val title: String,
    val subtitle: String,
    val providerLabel: String,
    val typeLabel: String,
    val actionLabel: String,
    val posterUrl: String?,
    val providerId: String,
    val tone: SourceLibraryTone,
    val chips: List<SearchResultChipUiState>,
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

internal data class CategoryBrowseUiState(
    val headline: String,
    val summary: String,
    val itemCountValue: String,
    val itemCountLabel: String,
    val topRatingValue: String,
    val topRatingLabel: String,
    val heatValue: String,
    val heatLabel: String,
    val sourceValue: String,
    val sourceLabel: String,
    val listTitle: String,
    val listAction: String,
    val emptyTitle: String,
    val emptySubtitle: String,
    val hasItems: Boolean,
)

internal enum class SourceLibraryTone {
    Primary,
    Online,
    Backup,
    Cache,
    Web,
    Muted,
}

internal data class AppNavigationTabUiState(
    val id: String,
    val label: String,
    val statusLabel: String,
    val selected: Boolean,
    val tone: SourceLibraryTone,
)

internal data class AppNavigationUiState(
    val selectedTabId: String,
    val tabs: List<AppNavigationTabUiState>,
) {
    val selectedTab: AppNavigationTabUiState?
        get() = tabs.firstOrNull { it.selected }
}

internal data class SourceLibraryChipUiState(
    val label: String,
    val tone: SourceLibraryTone,
)

internal data class SourceStrategyUiState(
    val id: String,
    val title: String,
    val value: String,
    val subtitle: String,
    val tone: SourceLibraryTone,
)

internal data class SourceCardUiState(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val statusLabel: String,
    val statusTone: SourceLibraryTone,
    val featureText: String,
    val domainText: String,
    val isBt: Boolean,
)

internal data class SourceLibraryUiState(
    val headline: String,
    val summary: String,
    val providerCount: Int,
    val onlineCount: Int,
    val btCount: Int,
    val downloadableCount: Int,
    val webViewCount: Int,
    val chips: List<SourceLibraryChipUiState>,
    val strategies: List<SourceStrategyUiState>,
    val sourceCards: List<SourceCardUiState>,
    val sourceListTitle: String,
    val sourceListSummary: String,
    val emptyTitle: String,
    val emptySubtitle: String,
)

internal data class CacheCapabilityUiState(
    val id: String,
    val title: String,
    val value: String,
    val subtitle: String,
    val tone: SourceLibraryTone,
)

internal data class CacheLibraryUiState(
    val headline: String,
    val summary: String,
    val cacheableSourceCount: Int,
    val media3SourceCount: Int,
    val btSourceCount: Int,
    val webBlockedSourceCount: Int,
    val advancedEngineAvailable: Boolean,
    val chips: List<SourceLibraryChipUiState>,
    val capabilities: List<CacheCapabilityUiState>,
)

internal data class ProfileQuickActionUiState(
    val id: String,
    val title: String,
    val subtitle: String,
    val tone: SourceLibraryTone,
)

internal data class ProfileSettingUiState(
    val id: String,
    val title: String,
    val subtitle: String,
    val value: String,
    val tone: SourceLibraryTone,
)

internal data class ProfileCenterUiState(
    val version: String,
    val headline: String,
    val summary: String,
    val sourceCount: Int,
    val danmakuCount: Int,
    val cacheableSourceCount: Int,
    val chips: List<SourceLibraryChipUiState>,
    val quickActions: List<ProfileQuickActionUiState>,
    val settings: List<ProfileSettingUiState>,
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

internal data class PlayerTopOverlayUiState(
    val title: String,
    val subtitle: String,
    val compactNotice: PlayerNoticeUiState?,
    val routeStatus: PlayerRouteStatusUiState,
    val statusStrip: PlayerTopStatusStripUiState,
)

internal data class PlayerTopStatusStripUiState(
    val chips: List<PlayerStatusChipUiState>,
)

internal data class PlayerStatusChipUiState(
    val label: String,
    val value: String,
    val tone: SourceLibraryTone,
)

internal data class PlayerFullscreenStatusStripUiState(
    val statusLabel: String,
    val routeSummary: String,
    val tags: List<String>,
    val error: Boolean,
)

internal data class PlayerNoticeUiState(
    val title: String,
    val message: String,
    val tone: SourceLibraryTone,
    val error: Boolean,
)

internal data class PlayerRouteStatusUiState(
    val routeLabel: String,
    val statusLabel: String,
    val tone: SourceLibraryTone,
    val error: Boolean,
)

internal data class PortraitWatchInfoUiState(
    val title: String,
    val episodeTitle: String,
    val currentEpisodeLabel: String,
    val metaChips: List<String>,
    val playbackBrief: String,
    val actions: List<PortraitWatchActionUiState>,
    val diagnostic: PlayerNoticeUiState?,
)

internal data class PortraitWatchActionUiState(
    val kind: PlayerPanelKind,
    val title: String,
    val subtitle: String,
    val enabled: Boolean,
    val tone: SourceLibraryTone,
)

internal data class PortraitEpisodeRailUiState(
    val visible: Boolean,
    val title: String,
    val allEpisodesLabel: String,
    val items: List<PortraitEpisodeRailItemUiState>,
    val moreAction: PortraitEpisodeMoreActionUiState?,
)

internal data class PortraitEpisodeRailItemUiState(
    val episode: Episode,
    val indexLabel: String,
    val title: String,
    val selected: Boolean,
    val loading: Boolean,
    val enabled: Boolean,
    val tone: SourceLibraryTone,
)

internal data class PortraitEpisodeMoreActionUiState(
    val title: String,
    val subtitle: String,
    val count: Int,
)

internal data class PortraitRecoveryActionsUiState(
    val visible: Boolean,
    val actions: List<PlayerActionUiState>,
)

internal data class PortraitRouteInsightUiState(
    val chips: List<PortraitRouteInsightChipUiState>,
)

internal data class PortraitRouteInsightChipUiState(
    val label: String,
    val value: String,
    val tone: SourceLibraryTone,
)

internal data class TorrentPlaybackPreparationUiState(
    val title: String,
    val description: String,
    val bufferingProgress: Float,
    val statusLine: String,
    val readinessLine: String,
    val progressLine: String,
    val bufferingLine: String?,
    val connectionLine: String,
    val fileLine: String?,
    val sizeLine: String?,
    val errorMessage: String?,
)

internal data class PlayerActionBarUiState(
    val actions: List<PlayerActionUiState>,
)

internal data class PlayerActionUiState(
    val kind: PlayerActionKind,
    val title: String,
    val value: String?,
    val selected: Boolean,
    val enabled: Boolean,
    val tone: SourceLibraryTone,
)

internal enum class PlayerActionKind {
    Retry,
    NextRoute,
    Quality,
    Speed,
    Route,
    Episode,
    NextEpisode,
    Cache,
    More,
}

internal data class PlayerEpisodePanelUiState(
    val title: String,
    val summary: String,
    val listTitle: String,
    val emptyText: String,
    val chips: List<SearchResultChipUiState>,
    val items: List<PlayerEpisodeOptionUiState>,
) {
    val hasItems: Boolean = items.isNotEmpty()
}

internal data class PlayerEpisodeOptionUiState(
    val episode: Episode,
    val title: String,
    val indexLabel: String,
    val statusLabel: String,
    val actionLabel: String,
    val selected: Boolean,
    val loading: Boolean,
    val enabled: Boolean,
    val tone: SourceLibraryTone,
)

internal data class PlayerDanmakuSettingsUiState(
    val toggleTitle: String,
    val toggleSubtitle: String,
    val toggleSelected: Boolean,
    val densityLabel: String,
    val alphaLabel: String,
    val fontScaleLabel: String,
    val safetySummary: String,
    val tone: SourceLibraryTone,
)

internal data class PlayerQualityPanelUiState(
    val summary: String,
    val emptyText: String,
    val currentQualityLabel: String,
    val options: List<PlayerQualityOptionUiState>,
) {
    val hasOptions: Boolean = options.isNotEmpty()
}

internal data class PlayerQualityOptionUiState(
    val route: RouteCandidate,
    val title: String,
    val subtitle: String,
    val selected: Boolean,
    val actionLabel: String,
    val tone: SourceLibraryTone,
)

internal data class PlayerSpeedPanelUiState(
    val summary: String,
    val options: List<PlayerSpeedOptionUiState>,
)

internal data class PlayerSpeedOptionUiState(
    val speed: Float,
    val title: String,
    val subtitle: String?,
    val selected: Boolean,
    val actionLabel: String,
    val tone: SourceLibraryTone,
)

internal data class PlayerMorePanelUiState(
    val summaryBadge: String,
    val summaryPrimary: String,
    val summarySecondary: String,
    val actions: List<PlayerMoreActionUiState>,
)

internal data class PlayerPanelSheetUiState(
    val title: String,
    val subtitle: String,
    val context: PlayerPanelContextUiState,
    val tabs: List<PlayerPanelTabUiState>,
)

internal data class PlayerPanelContextUiState(
    val title: String,
    val metadata: String,
    val statusLabel: String,
)

internal data class PlayerPanelTabUiState(
    val kind: PlayerPanelKind,
    val label: String,
    val value: String?,
    val enabled: Boolean,
    val selected: Boolean,
    val highlighted: Boolean,
    val tone: SourceLibraryTone,
)

internal enum class PlayerPanelKind {
    More,
    Danmaku,
    Quality,
    Speed,
    Route,
    Episode,
}

internal data class PlayerMoreActionUiState(
    val kind: PlayerMoreActionKind,
    val title: String,
    val subtitle: String,
    val enabled: Boolean,
    val selected: Boolean,
    val tone: SourceLibraryTone,
)

internal enum class PlayerMoreActionKind {
    Quality,
    Speed,
    Episode,
    Route,
    Danmaku,
    Cache,
}

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
    val compactTitle: String,
    val compactSummary: String,
    val detailedTitle: String,
    val detailedSummary: String,
    val selectedRouteSummary: String?,
    val compactMetrics: List<RoutePanelMetricUiState>,
    val detailedMetrics: List<RoutePanelMetricUiState>,
)

internal data class RoutePanelMetricUiState(
    val label: String,
    val value: String,
    val tone: SourceLibraryTone,
)

internal data class RouteCandidateUiState(
    val streamId: String,
    val sourceId: String,
    val sourceName: String,
    val sourceInitial: String,
    val title: String,
    val primaryLabel: String,
    val protocolLabel: String,
    val sizeLabel: String?,
    val statusLabel: String,
    val actionLabel: String,
    val selected: Boolean,
    val recommended: Boolean,
    val playable: Boolean,
    val cacheLabel: String,
    val accentTone: SourceLibraryTone,
    val statusTone: SourceLibraryTone,
    val actionTone: SourceLibraryTone,
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
    val routeCountLabel: String
        get() = "${totalCount}线"
    val detailSummary: String
        get() = "${playableCount}/${totalCount} 可播 · ${onlineCount} 在线 · ${btCount} BT"
    val statusLabel: String
        get() = when {
            isFilterSelected && isAll -> "全部"
            isFilterSelected -> "已选"
            hasSelected -> "当前"
            hasRecommended -> "推荐"
            onlineCount > 0 -> "在线"
            btCount > 0 -> "备用"
            webOnlyCount > 0 -> "网页"
            failedCount > 0 -> "失败"
            else -> "待检"
        }
    val footerLabel: String
        get() = when {
            isFilterSelected && isAll -> "全部来源"
            isFilterSelected -> "当前方案"
            hasSelected -> "正在播放"
            hasRecommended -> "自动推荐"
            failedCount > 0 -> "${failedCount} 条失败已降级"
            onlineCount > 0 -> "在线播放"
            btCount > 0 -> "备用来源"
            webOnlyCount > 0 -> "网页兜底"
            else -> "待检测"
        }
    val tone: SourceLibraryTone
        get() = when {
            isFilterSelected || hasSelected -> SourceLibraryTone.Online
            hasRecommended -> SourceLibraryTone.Primary
            onlineCount > 0 -> SourceLibraryTone.Cache
            btCount > 0 -> SourceLibraryTone.Backup
            webOnlyCount > 0 -> SourceLibraryTone.Web
            failedCount > 0 -> SourceLibraryTone.Web
            else -> SourceLibraryTone.Muted
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

internal fun buildDetailEpisodeSummaryUiState(
    episodeCount: Int,
    selectedEpisode: Episode?,
    routeState: RouteUiState,
): DetailEpisodeSummaryUiState {
    val currentEpisodeLabel = selectedEpisode?.index?.let { "当前第 $it 集" }
        ?: selectedEpisode?.title?.takeIf { it.isNotBlank() }
        ?: "默认从第 1 集开始"
    val episodeCountLabel = if (episodeCount > 0) "共 $episodeCount 集" else "待载入选集"
    val routeStatusLabel = when (routeState.status) {
        RouteLoadStatus.Ready -> "已匹配"
        RouteLoadStatus.Loading -> "匹配中"
        RouteLoadStatus.Failed -> "异常"
        RouteLoadStatus.Empty -> "待补源"
        RouteLoadStatus.Idle -> "待选集"
    }
    val routeActionLabel = when (routeState.status) {
        RouteLoadStatus.Ready -> if (routeState.canPlay) "可播放" else "待确认"
        RouteLoadStatus.Loading -> "正在准备"
        RouteLoadStatus.Failed -> "查看异常"
        RouteLoadStatus.Empty -> "换集/稍后"
        RouteLoadStatus.Idle -> "选集后匹配"
    }
    val tone = when (routeState.status) {
        RouteLoadStatus.Ready -> if (routeState.loadOriginLabel == "预取命中") SourceLibraryTone.Cache else SourceLibraryTone.Online
        RouteLoadStatus.Loading -> SourceLibraryTone.Backup
        RouteLoadStatus.Failed -> SourceLibraryTone.Web
        RouteLoadStatus.Empty -> SourceLibraryTone.Backup
        RouteLoadStatus.Idle -> SourceLibraryTone.Muted
    }
    val summary = when (routeState.status) {
        RouteLoadStatus.Ready -> {
            listOfNotNull(
                routeState.sourceCoverageLabel.takeIf { it.isNotBlank() },
                routeState.recommendationTitle.takeIf { it.isNotBlank() },
                routeState.recommendationReason.takeIf { it.isNotBlank() },
            ).joinToString(" · ")
        }
        RouteLoadStatus.Loading -> "$currentEpisodeLabel 正在优先匹配在线播放和备用线路。"
        RouteLoadStatus.Failed -> routeState.detail.ifBlank { "当前集播放源异常，可重试或切换剧集。" }
        RouteLoadStatus.Empty -> "当前集暂未命中可播线路，可切换剧集或稍后重试。"
        RouteLoadStatus.Idle -> "选择剧集后会自动匹配最佳播放源。"
    }
    val chips = buildList {
        add(SearchResultChipUiState(currentEpisodeLabel, tone))
        add(SearchResultChipUiState(episodeCountLabel, SourceLibraryTone.Online))
        add(SearchResultChipUiState(routeState.sourceCoverageLabel, tone))
        add(SearchResultChipUiState(routeState.loadOriginLabel, SourceLibraryTone.Muted))
    }
    return DetailEpisodeSummaryUiState(
        headline = "选集与线路",
        summary = summary,
        currentEpisodeLabel = currentEpisodeLabel,
        episodeCountLabel = episodeCountLabel,
        routeStatusLabel = routeStatusLabel,
        routeActionLabel = routeActionLabel,
        tone = tone,
        chips = chips,
    )
}

internal fun buildDetailRouteResolutionUiState(routeState: RouteUiState): DetailRouteResolutionUiState {
    val episodeTitle = routeState.selectedEpisodeTitle.ifBlank { "\u9009\u4e2d\u5267\u96c6" }
    val chips = when (routeState.status) {
        RouteLoadStatus.Loading -> listOf(
            SourceLibraryChipUiState("\u5b9e\u65f6\u5339\u914d", SourceLibraryTone.Online),
            SourceLibraryChipUiState("\u5728\u7ebf\u4f18\u5148", SourceLibraryTone.Primary),
            SourceLibraryChipUiState("BT \u5907\u7528", SourceLibraryTone.Backup),
        )
        RouteLoadStatus.Failed -> listOf(
            SourceLibraryChipUiState("\u53ef\u91cd\u8bd5", SourceLibraryTone.Web),
            SourceLibraryChipUiState("\u6362\u96c6\u91cd\u8bd5", SourceLibraryTone.Backup),
            SourceLibraryChipUiState("\u6765\u6e90\u5f02\u5e38", SourceLibraryTone.Web),
        )
        RouteLoadStatus.Empty -> listOf(
            SourceLibraryChipUiState("\u5f85\u8865\u6e90", SourceLibraryTone.Muted),
            SourceLibraryChipUiState("\u6362\u96c6\u91cd\u8bd5", SourceLibraryTone.Backup),
            SourceLibraryChipUiState("\u7ee7\u7eed\u5339\u914d", SourceLibraryTone.Online),
        )
        RouteLoadStatus.Ready -> listOf(
            SourceLibraryChipUiState("\u5df2\u5c31\u7eea", SourceLibraryTone.Cache),
            SourceLibraryChipUiState("${routeState.routeCount} \u6761\u7ebf\u8def", SourceLibraryTone.Online),
            SourceLibraryChipUiState(routeState.sourceCoverageLabel, SourceLibraryTone.Primary),
        )
        RouteLoadStatus.Idle -> listOf(
            SourceLibraryChipUiState("\u5f85\u9009\u96c6", SourceLibraryTone.Muted),
            SourceLibraryChipUiState("\u81ea\u52a8\u5339\u914d", SourceLibraryTone.Online),
        )
    }
    return when (routeState.status) {
        RouteLoadStatus.Loading -> DetailRouteResolutionUiState(
            title = "\u6b63\u5728\u5339\u914d\u7ebf\u8def",
            subtitle = episodeTitle,
            detail = "\u4f18\u5148\u5339\u914d\u5728\u7ebf\u64ad\u653e\uff0cBT/RSS \u4f5c\u4e3a\u5907\u7528\u6765\u6e90\u8865\u9f50\u3002",
            chips = chips,
            showProgress = true,
            tone = SourceLibraryTone.Online,
        )
        RouteLoadStatus.Failed -> DetailRouteResolutionUiState(
            title = "\u7ebf\u8def\u52a0\u8f7d\u5931\u8d25",
            subtitle = episodeTitle,
            detail = routeState.detail.ifBlank { "\u53ef\u7a0d\u540e\u91cd\u8bd5\uff0c\u6216\u5207\u6362\u5267\u96c6\u7ee7\u7eed\u5339\u914d\u3002" },
            chips = chips,
            showProgress = false,
            tone = SourceLibraryTone.Web,
        )
        RouteLoadStatus.Empty -> DetailRouteResolutionUiState(
            title = "\u6682\u672a\u5339\u914d\u5230\u53ef\u64ad\u653e\u7ebf\u8def",
            subtitle = episodeTitle,
            detail = "\u53ef\u4ee5\u5207\u6362\u5267\u96c6\u6216\u7a0d\u540e\u91cd\u8bd5\uff0c\u8be6\u60c5\u9875\u4f1a\u7ee7\u7eed\u4fdd\u7559\u7ebf\u8def\u8bca\u65ad\u5165\u53e3\u3002",
            chips = chips,
            showProgress = false,
            tone = SourceLibraryTone.Muted,
        )
        RouteLoadStatus.Ready -> DetailRouteResolutionUiState(
            title = "\u64ad\u653e\u6e90\u5df2\u5c31\u7eea",
            subtitle = episodeTitle,
            detail = routeState.recommendationDetail,
            chips = chips,
            showProgress = false,
            tone = SourceLibraryTone.Cache,
        )
        RouteLoadStatus.Idle -> DetailRouteResolutionUiState(
            title = "\u7b49\u5f85\u9009\u62e9\u5267\u96c6",
            subtitle = episodeTitle,
            detail = "\u9009\u62e9\u5267\u96c6\u540e\u4f1a\u81ea\u52a8\u5339\u914d\u5728\u7ebf\u6e90\u3001BT \u5907\u7528\u548c\u7f13\u5b58\u80fd\u529b\u3002",
            chips = chips,
            showProgress = false,
            tone = SourceLibraryTone.Muted,
        )
    }
}

internal fun buildRouteCandidateUiState(
    route: RouteCandidate,
    selected: Boolean = false,
    recommended: Boolean = false,
    failed: Boolean = false,
): RouteCandidateUiState {
    val protocolLabel = route.protocol.uiProtocolName()
    val playable = !failed && route.protocol != StreamProtocol.WEBVIEW_ONLY
    val accentTone = when {
        selected -> SourceLibraryTone.Online
        recommended -> SourceLibraryTone.Primary
        failed -> SourceLibraryTone.Web
        route.protocol == StreamProtocol.BITTORRENT -> SourceLibraryTone.Backup
        route.protocol == StreamProtocol.WEBVIEW_ONLY -> SourceLibraryTone.Muted
        else -> SourceLibraryTone.Online
    }
    val statusTone = when {
        failed -> SourceLibraryTone.Web
        selected -> SourceLibraryTone.Online
        route.protocol == StreamProtocol.BITTORRENT -> SourceLibraryTone.Backup
        route.protocol == StreamProtocol.WEBVIEW_ONLY -> SourceLibraryTone.Muted
        route.protocol in media3StreamingProtocols -> SourceLibraryTone.Cache
        else -> SourceLibraryTone.Online
    }
    val statusLabel = when {
        failed -> "播放失败"
        selected -> "当前"
        route.protocol == StreamProtocol.BITTORRENT -> "备用源"
        route.protocol == StreamProtocol.WEBVIEW_ONLY -> "仅网页"
        route.protocol in media3StreamingProtocols -> "在线可播"
        else -> protocolLabel
    }
    val actionLabel = when {
        failed -> "重试"
        selected -> "播放中"
        route.protocol == StreamProtocol.BITTORRENT -> "边下边播"
        route.protocol == StreamProtocol.WEBVIEW_ONLY -> "网页兜底"
        recommended -> "推荐播放"
        else -> "播放"
    }
    val actionTone = when {
        selected -> SourceLibraryTone.Online
        recommended -> SourceLibraryTone.Primary
        failed -> SourceLibraryTone.Web
        route.protocol == StreamProtocol.BITTORRENT -> SourceLibraryTone.Backup
        route.protocol == StreamProtocol.WEBVIEW_ONLY -> SourceLibraryTone.Muted
        else -> SourceLibraryTone.Online
    }
    val cacheAction = buildPlayerCacheActionUiState(route.stream)
    return RouteCandidateUiState(
        streamId = route.stream.id,
        sourceId = route.sourceId,
        sourceName = route.sourceName,
        sourceInitial = route.sourceName.take(1).ifBlank { route.sourceId.take(1).uppercase() }.ifBlank { "源" },
        title = route.title,
        primaryLabel = routePrimaryLabelForUi(route),
        protocolLabel = protocolLabel,
        sizeLabel = route.sizeBytes?.let(::formatBytesForUi),
        statusLabel = statusLabel,
        actionLabel = actionLabel,
        selected = selected,
        recommended = recommended,
        playable = playable,
        cacheLabel = cacheAction.value,
        accentTone = accentTone,
        statusTone = statusTone,
        actionTone = actionTone,
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

internal fun buildSearchLandingUiState(
    scheduleState: HomeScheduleUiState,
    searchableSourceCount: Int,
    fallbackKeywords: List<String>,
): SearchLandingUiState {
    val searchable = searchableSourceCount.coerceAtLeast(0)
    val scheduleSuggestions = scheduleState.selectedItems
        .mapNotNull { result -> result.searchKeywordCandidate() }
        .distinctBy { it.lowercase() }
        .take(6)
        .map { keyword ->
            SearchSuggestionUiState(
                keyword = keyword,
                subtitle = if (scheduleState.selectedDayAction.isNotBlank()) {
                    "${scheduleState.selectedDayTitle} · ${scheduleState.selectedDayAction}"
                } else {
                    scheduleState.selectedDayTitle
                },
                tone = SourceLibraryTone.Primary,
            )
        }
    val fallbackSuggestions = fallbackKeywords
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .filterNot { keyword -> scheduleSuggestions.any { it.keyword.equals(keyword, ignoreCase = true) } }
        .distinctBy { it.lowercase() }
        .take((6 - scheduleSuggestions.size).coerceAtLeast(0))
        .map { keyword ->
            SearchSuggestionUiState(
                keyword = keyword,
                subtitle = "热门搜索",
                tone = SourceLibraryTone.Online,
            )
        }
    val suggestions = scheduleSuggestions + fallbackSuggestions
    val scheduleCount = scheduleSuggestions.size
    val summary = when {
        searchable == 0 -> "搜索源待接入；可先从首页或频道浏览条目，接入来源后这里会显示可搜索范围。"
        scheduleCount > 0 -> "${scheduleState.selectedDayTitle}已整理 $scheduleCount 个可搜条目，搜索后详情页会继续自动匹配播放线路。"
        else -> "已接入 $searchable 个可搜索来源；输入番名、别名或播放链接后，可按来源缩小索引范围。"
    }
    return SearchLandingUiState(
        headline = "找番",
        summary = summary,
        inputTitle = "全站找番",
        inputSubtitle = if (searchable > 0) "$searchable 个索引源 · 先进详情再匹配线路" else "等待可搜索来源",
        inputPlaceholder = "输入番名、别名或粘贴播放链接",
        suggestionTitle = if (scheduleCount > 0) "日程可搜" else "大家在找",
        suggestions = suggestions,
        scheduleSuggestionCount = scheduleCount,
        searchableSourceCount = searchable,
    )
}

internal fun buildSearchIdleHintUiState(
    landingState: SearchLandingUiState,
    indexState: SearchIndexUiState,
): SearchIdleHintUiState {
    val sourceCount = landingState.searchableSourceCount.coerceAtLeast(0)
    val scheduleCount = landingState.scheduleSuggestionCount.coerceAtLeast(0)
    val failedCount = indexState.failedSourceCount.coerceAtLeast(0)
    val title = when {
        sourceCount == 0 -> "\u641c\u7d22\u6e90\u5f85\u63a5\u5165"
        scheduleCount > 0 -> "\u5148\u4ece\u65e5\u7a0b\u5feb\u901f\u627e\u756a"
        else -> "\u627e\u5230\u540e\u5148\u8fdb\u756a\u5267\u8be6\u60c5"
    }
    val subtitle = when {
        sourceCount == 0 -> "\u63a5\u5165\u53ef\u641c\u7d22\u6765\u6e90\u540e\uff0c\u641c\u7d22\u9875\u4f1a\u663e\u793a\u7ed3\u679c\u3001\u6765\u6e90\u7b5b\u9009\u548c\u8be6\u60c5\u9875\u7ebf\u8def\u5339\u914d\u5165\u53e3\u3002"
        scheduleCount > 0 -> "\u65e5\u7a0b\u5efa\u8bae\u5df2\u63d0\u4f9b $scheduleCount \u4e2a\u5165\u53e3\uff1b\u6253\u5f00\u8be6\u60c5\u540e\u518d\u786e\u8ba4\u9009\u96c6\u3001\u7ebf\u8def\u3001\u7f13\u5b58\u548c\u5f39\u5e55\u3002"
        else -> "\u8f93\u5165\u756a\u540d\u3001\u522b\u540d\u6216\u64ad\u653e\u94fe\u63a5\u540e\uff0c\u53ef\u6309\u6765\u6e90\u7b5b\u9009\u7ed3\u679c\uff1b\u8be6\u60c5\u9875\u4f1a\u7ee7\u7eed\u5339\u914d\u53ef\u64ad\u7ebf\u8def\u3002"
    }
    val chips = listOf(
        SourceLibraryChipUiState(
            label = if (sourceCount > 0) "$sourceCount \u6e90\u53ef\u641c" else "\u5f85\u63a5\u641c\u7d22\u6e90",
            tone = if (sourceCount > 0) SourceLibraryTone.Online else SourceLibraryTone.Muted,
        ),
        SourceLibraryChipUiState(
            label = if (scheduleCount > 0) "$scheduleCount \u4e2a\u65e5\u7a0b\u5efa\u8bae" else "\u70ed\u95e8\u8bcd\u515c\u5e95",
            tone = if (scheduleCount > 0) SourceLibraryTone.Primary else SourceLibraryTone.Backup,
        ),
        SourceLibraryChipUiState(
            label = when {
                failedCount > 0 -> "$failedCount \u6e90\u5f02\u5e38"
                sourceCount > 0 -> "\u7d22\u5f15\u6b63\u5e38"
                else -> "\u6765\u6e90\u5f85\u914d\u7f6e"
            },
            tone = when {
                failedCount > 0 -> SourceLibraryTone.Web
                sourceCount > 0 -> SourceLibraryTone.Cache
                else -> SourceLibraryTone.Muted
            },
        ),
    )
    val actionLabel = when {
        sourceCount == 0 -> "\u5148\u63a5\u5165\u641c\u7d22\u6765\u6e90"
        scheduleCount > 0 -> "\u70b9\u65e5\u7a0b\u8bcd\u6216\u8f93\u5165\u756a\u540d"
        else -> "\u8f93\u5165\u756a\u540d\u5f00\u59cb\u641c\u7d22"
    }
    return SearchIdleHintUiState(
        title = title,
        subtitle = subtitle,
        chips = chips,
        actionLabel = actionLabel,
    )
}

internal fun buildSearchResultsSectionUiState(
    indexState: SearchIndexUiState,
    visibleResultCount: Int,
    loading: Boolean,
    searched: Boolean,
): SearchResultsSectionUiState {
    val visibleCount = visibleResultCount.coerceAtLeast(0)
    val totalCount = indexState.resultCount.coerceAtLeast(0)
    val sourceCount = indexState.searchableSourceCount.coerceAtLeast(0)
    val failedCount = indexState.failedSourceCount.coerceAtLeast(0)
    val selectedFilter = indexState.sourceFilters.firstOrNull { it.selected && !it.isAll }
    val headerSubtitle = when {
        loading -> "\u6b63\u5728\u5e76\u884c\u641c\u7d22 $sourceCount \u4e2a\u6765\u6e90"
        !searched -> "\u5148\u627e\u756a\uff0c\u518d\u8fdb\u8be6\u60c5\u786e\u8ba4\u9009\u96c6\u548c\u7ebf\u8def"
        selectedFilter != null -> "${selectedFilter.name} \u00b7 $visibleCount \u4e2a\u7ed3\u679c"
        totalCount > 0 && failedCount > 0 -> "$totalCount \u4e2a\u7ed3\u679c \u00b7 $failedCount \u4e2a\u6e90\u5f02\u5e38"
        totalCount > 0 -> "$totalCount \u4e2a\u7ed3\u679c \u00b7 \u8fdb\u5165\u8be6\u60c5\u540e\u7ee7\u7eed\u5339\u914d\u7ebf\u8def"
        failedCount > 0 -> "\u6682\u65e0\u7ed3\u679c \u00b7 $failedCount \u4e2a\u6e90\u5f02\u5e38"
        else -> "\u9009\u62e9\u756a\u5267\u8fdb\u5165\u8be6\u60c5"
    }
    val emptyTitle = when {
        !searched -> "\u7b49\u5f85\u641c\u7d22"
        selectedFilter != null && totalCount > 0 -> "\u5f53\u524d\u6765\u6e90\u6682\u65e0\u547d\u4e2d"
        failedCount > 0 -> "\u6682\u672a\u547d\u4e2d\u53ef\u7528\u7ed3\u679c"
        else -> "\u6ca1\u6709\u627e\u5230\u5408\u9002\u7ed3\u679c"
    }
    val emptySubtitle = when {
        !searched -> "\u53ef\u4ece\u65e5\u7a0b\u5efa\u8bae\u3001\u70ed\u95e8\u8bcd\u6216\u624b\u52a8\u8f93\u5165\u5f00\u59cb\u3002"
        selectedFilter != null && totalCount > 0 -> "\u53ef\u5207\u56de\u5168\u90e8\u7d22\u5f15\uff0c\u6216\u6362\u4e00\u4e2a\u5173\u952e\u8bcd\u7ee7\u7eed\u641c\u7d22\u3002"
        failedCount > 0 -> "\u53ef\u6362\u756a\u540d\u3001\u522b\u540d\u6216\u7a0d\u540e\u91cd\u8bd5\uff1b\u5f02\u5e38\u6e90\u5df2\u5728\u6765\u6e90\u7b5b\u9009\u4e2d\u6807\u51fa\u3002"
        else -> "\u53ef\u4ee5\u6362\u4e00\u4e2a\u756a\u540d\u3001\u522b\u540d\u6216\u5173\u952e\u8bcd\u518d\u8bd5\u3002"
    }
    return SearchResultsSectionUiState(
        headerTitle = "\u641c\u7d22\u7ed3\u679c",
        headerSubtitle = headerSubtitle,
        emptyTitle = emptyTitle,
        emptySubtitle = emptySubtitle,
    )
}

internal fun buildSearchResultCardUiState(result: SearchResult): SearchResultCardUiState {
    val providerKind = result.providerKindForSearch()
    val chips = buildList {
        result.raw["rating"]?.takeIf { it.isNotBlank() }?.let {
            add(SearchResultChipUiState("评分 $it", SourceLibraryTone.Primary))
        }
        result.raw["episodeCount"]?.takeIf { it.isNotBlank() }?.let {
            add(SearchResultChipUiState("${it} 集", SourceLibraryTone.Online))
        }
        result.raw["categoryTitle"]?.takeIf { it.isNotBlank() }?.let {
            add(SearchResultChipUiState(it, SourceLibraryTone.Backup))
        }
        result.raw["doing"]?.toIntOrNull()?.takeIf { it > 0 }?.let {
            add(SearchResultChipUiState("${it.compactBrowseCount()} 在看", SourceLibraryTone.Web))
        }
        if (isEmpty()) {
            add(SearchResultChipUiState(providerKind.typeLabel, providerKind.tone))
        }
    }.distinctBy { it.label }.take(3)
    return SearchResultCardUiState(
        title = result.title.ifBlank { "未命名条目" },
        subtitle = result.subtitle?.takeIf { it.isNotBlank() } ?: providerKind.subtitle,
        providerLabel = providerKind.providerLabel,
        typeLabel = providerKind.typeLabel,
        actionLabel = providerKind.actionLabel,
        posterUrl = result.posterUrl,
        providerId = result.providerId,
        tone = providerKind.tone,
        chips = chips,
    )
}

internal fun buildDetailEntryUiState(
    result: SearchResult,
    detail: MediaDetail?,
    loading: Boolean,
    error: String?,
): DetailEntryUiState {
    val resultState = buildSearchResultCardUiState(result)
    val episodeCount = detail?.episodes?.size
        ?: result.raw["episodeCount"]?.toIntOrNull()
        ?: 0
    val statusLabel = when {
        loading -> "详情加载中"
        !error.isNullOrBlank() -> "详情异常"
        detail != null -> "详情已就绪"
        else -> "等待详情"
    }
    val actionLabel = when {
        loading -> "读取中"
        !error.isNullOrBlank() -> "可重试"
        detail != null && episodeCount > 0 -> "准备播放"
        detail != null -> "查看详情"
        else -> "进入详情"
    }
    val summary = when {
        loading -> "${resultState.providerLabel} · 正在读取简介、选集和播放入口。"
        !error.isNullOrBlank() -> "详情加载失败：${error.orEmpty()}"
        detail != null && episodeCount > 0 -> "已载入 $episodeCount 集，首集会自动匹配播放线路。"
        detail != null -> "已载入详情，选集和播放线路会继续自动补齐。"
        else -> "${resultState.providerLabel} · 进入详情后会读取选集并自动匹配播放源。"
    }
    val statusTone = when {
        loading -> SourceLibraryTone.Backup
        !error.isNullOrBlank() -> SourceLibraryTone.Web
        detail != null -> SourceLibraryTone.Cache
        else -> SourceLibraryTone.Muted
    }
    val chips = buildList {
        add(SearchResultChipUiState(resultState.typeLabel, resultState.tone))
        add(SearchResultChipUiState(statusLabel, statusTone))
        if (episodeCount > 0) {
            add(SearchResultChipUiState("${episodeCount} 集", SourceLibraryTone.Online))
        }
        resultState.chips
            .filterNot { chip -> any { it.label == chip.label } }
            .take(2)
            .forEach(::add)
    }.take(4)
    return DetailEntryUiState(
        headline = detail?.title?.takeIf { it.isNotBlank() } ?: resultState.title,
        summary = summary,
        providerLabel = resultState.providerLabel,
        typeLabel = resultState.typeLabel,
        detailStatusLabel = statusLabel,
        episodeLabel = if (episodeCount > 0) "$episodeCount 集" else "待选集",
        actionLabel = actionLabel,
        tone = resultState.tone,
        chips = chips,
    )
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

internal fun buildCategoryBrowseUiState(
    category: BangumiCategory,
    items: List<SearchResult>,
    fallback: List<SearchResult> = emptyList(),
    loading: Boolean = false,
    error: String? = null,
): CategoryBrowseUiState {
    val fallbackVisible = items.isEmpty() && fallback.isNotEmpty()
    val metricItems = items.ifEmpty { fallback }
    val topRating = metricItems.mapNotNull { it.raw["rating"]?.toDoubleOrNull() }.maxOrNull()
    val highestHeat = metricItems.maxOfOrNull { result ->
        listOfNotNull(
            result.raw["doing"]?.toIntOrNull(),
            result.raw["collect"]?.toIntOrNull(),
            result.raw["wish"]?.toIntOrNull(),
        ).maxOrNull() ?: 0
    } ?: 0
    val sourceIds = metricItems.map { it.providerId }.distinct()
    val sourceValue = when (sourceIds.size) {
        0 -> "\u5f85\u540c\u6b65"
        1 -> providerDisplayIdForBrowse(sourceIds.single())
        else -> "${sourceIds.size} \u6e90"
    }
    val itemCountValue = when {
        items.isNotEmpty() -> items.size.toString()
        fallbackVisible -> fallback.size.toString()
        else -> "--"
    }
    val itemCountLabel = if (fallbackVisible) "\u515c\u5e95\u63a8\u8350" else "\u5206\u7c7b\u6761\u76ee"
    val topRatingValue = topRating?.let { "%.1f".format(it) } ?: "--"
    val heatValue = highestHeat.takeIf { it > 0 }?.compactBrowseCount() ?: "--"
    val headline = when {
        !error.isNullOrBlank() -> "${category.title}\u52a0\u8f7d\u5f02\u5e38"
        loading && items.isEmpty() -> "\u6b63\u5728\u540c\u6b65${category.title}"
        items.isNotEmpty() -> "${category.title}\u5df2\u7d22\u5f15 ${items.size} \u90e8"
        fallbackVisible -> "${category.title}\u5c55\u793a\u515c\u5e95\u63a8\u8350"
        else -> "${category.title}\u6682\u65e0\u6761\u76ee"
    }
    val summary = when {
        !error.isNullOrBlank() -> "\u5206\u7c7b\u699c\u5355\u6682\u4e0d\u53ef\u7528\uff1a$error"
        items.isNotEmpty() -> "\u6309\u8bc4\u5206\u3001\u5728\u770b\u548c\u6536\u85cf\u70ed\u5ea6\u7ec4\u7ec7\uff0c\u6700\u9ad8\u8bc4\u5206 $topRatingValue\uff0c\u6700\u9ad8\u70ed\u5ea6 $heatValue\u3002"
        fallbackVisible -> "Bangumi \u5206\u7c7b\u6682\u672a\u8fd4\u56de\uff0c\u5148\u7528\u9996\u9875\u63a8\u8350\u515c\u5e95\uff0c\u4fdd\u6301\u53ef\u6d4f\u89c8\u3002"
        loading -> "Bangumi \u5206\u7c7b\u699c\u5355\u540c\u6b65\u4e2d\uff0c\u5b8c\u6210\u540e\u4f1a\u8865\u5168\u8bc4\u5206\u548c\u70ed\u5ea6\u6307\u6807\u3002"
        else -> "\u53ef\u5207\u6362\u5230\u5176\u4ed6\u5206\u7c7b\uff0c\u6216\u76f4\u63a5\u641c\u7d22\u756a\u540d\u8fdb\u5165\u8be6\u60c5\u3002"
    }
    return CategoryBrowseUiState(
        headline = headline,
        summary = summary,
        itemCountValue = itemCountValue,
        itemCountLabel = itemCountLabel,
        topRatingValue = topRatingValue,
        topRatingLabel = "\u6700\u9ad8\u8bc4\u5206",
        heatValue = heatValue,
        heatLabel = "\u6700\u9ad8\u70ed\u5ea6",
        sourceValue = sourceValue,
        sourceLabel = "\u6570\u636e\u6765\u6e90",
        listTitle = if (category.id == "recommend") "\u7cbe\u9009\u63a8\u8350" else "\u7cbe\u9009\u70ed\u64ad${category.title}",
        listAction = if (items.isNotEmpty()) "\u5168\u90e8 ${items.size}" else "",
        emptyTitle = if (fallbackVisible) "\u5206\u7c7b\u6682\u65e0\u547d\u4e2d" else "\u6682\u65e0\u53ef\u5c55\u793a\u6761\u76ee",
        emptySubtitle = if (fallbackVisible) {
            "\u4e0a\u65b9\u5df2\u4f7f\u7528\u9996\u9875\u63a8\u8350\u515c\u5e95\uff0c\u53ef\u5207\u6362\u5206\u7c7b\u6216\u76f4\u63a5\u641c\u7d22\u3002"
        } else {
            "\u53ef\u4ee5\u5207\u5230\u5176\u4ed6\u5206\u7c7b\uff0c\u6216\u76f4\u63a5\u641c\u7d22\u756a\u540d\u3002"
        },
        hasItems = items.isNotEmpty(),
    )
}

private const val NAV_DISCOVER_ID = "discover"
private const val NAV_SEARCH_ID = "search"
private const val NAV_SOURCES_ID = "sources"
private const val NAV_SETTINGS_ID = "settings"

internal fun buildAppNavigationUiState(
    selectedTabId: String,
    todayCount: Int = 0,
    searchableSourceCount: Int,
    sourceCount: Int,
    cacheableSourceCount: Int,
): AppNavigationUiState {
    val selectedId = selectedTabId.trim().lowercase().ifBlank { NAV_DISCOVER_ID }
    val today = todayCount.coerceAtLeast(0)
    val searchable = searchableSourceCount.coerceAtLeast(0)
    val sources = sourceCount.coerceAtLeast(0)
    val cacheable = cacheableSourceCount.coerceAtLeast(0)

    fun tab(
        id: String,
        label: String,
        statusLabel: String,
        tone: SourceLibraryTone,
    ): AppNavigationTabUiState {
        return AppNavigationTabUiState(
            id = id,
            label = label,
            statusLabel = statusLabel,
            selected = id == selectedId,
            tone = tone,
        )
    }

    return AppNavigationUiState(
        selectedTabId = selectedId,
        tabs = listOf(
            tab(
                id = NAV_DISCOVER_ID,
                label = "首页",
                statusLabel = if (today > 0) "今日 $today" else "推荐",
                tone = if (today > 0) SourceLibraryTone.Primary else SourceLibraryTone.Online,
            ),
            tab(
                id = NAV_SEARCH_ID,
                label = "搜索",
                statusLabel = if (searchable > 0) "$searchable 源" else "待索引",
                tone = if (searchable > 0) SourceLibraryTone.Online else SourceLibraryTone.Muted,
            ),
            tab(
                id = NAV_SOURCES_ID,
                label = "频道",
                statusLabel = if (sources > 0) "$sources 来源" else "待接入",
                tone = if (sources > 0) SourceLibraryTone.Backup else SourceLibraryTone.Muted,
            ),
            tab(
                id = NAV_SETTINGS_ID,
                label = "我的",
                statusLabel = when {
                    cacheable > 0 -> "$cacheable 可缓存"
                    sources > 0 -> "待缓存"
                    else -> "我的"
                },
                tone = when {
                    cacheable > 0 -> SourceLibraryTone.Cache
                    sources > 0 -> SourceLibraryTone.Web
                    else -> SourceLibraryTone.Muted
                },
            ),
        ),
    )
}

internal fun buildSourceLibraryUiState(manifests: List<SourceManifest>): SourceLibraryUiState {
    val providerCount = manifests.size
    val onlineCount = manifests.count { SourceCapability.STREAM in it.capabilities && SourceCapability.BITTORRENT !in it.capabilities }
    val btCount = manifests.count { SourceCapability.BITTORRENT in it.capabilities }
    val downloadableCount = manifests.count { it.supportsDownload || SourceCapability.DOWNLOAD in it.capabilities }
    val webViewCount = manifests.count { it.requiresWebView || SourceCapability.WEBVIEW_SNIFF in it.capabilities }
    val chips = buildList {
        add(SourceLibraryChipUiState("${providerCount} 个来源", SourceLibraryTone.Primary))
        add(SourceLibraryChipUiState("${onlineCount} 在线", SourceLibraryTone.Online))
        if (btCount > 0) add(SourceLibraryChipUiState("${btCount} 备用", SourceLibraryTone.Backup))
        if (downloadableCount > 0) add(SourceLibraryChipUiState("${downloadableCount} 可缓存", SourceLibraryTone.Cache))
    }
    val headline = if (providerCount > 0) {
        "片库频道已接入 $providerCount 个来源"
    } else {
        "片库来源待接入"
    }
    val summary = when {
        providerCount == 0 -> "接入在线、BT 或规则来源后，这里会展示播放策略和线路能力。"
        onlineCount > 0 -> "默认优先使用 $onlineCount 个在线源开播，BT、嗅探和缓存能力作为备用补齐。"
        btCount > 0 -> "当前以 BT 资源站为主，适合边下边播；后续可补充 HLS/MP4 在线源。"
        else -> "当前来源偏索引或辅助能力，详情页会继续尝试匹配可播放线路。"
    }
    return SourceLibraryUiState(
        headline = headline,
        summary = summary,
        providerCount = providerCount,
        onlineCount = onlineCount,
        btCount = btCount,
        downloadableCount = downloadableCount,
        webViewCount = webViewCount,
        chips = chips,
        strategies = listOf(
            SourceStrategyUiState(
                id = "online",
                title = "先播在线",
                value = "${onlineCount.coerceAtLeast(0)} 源",
                subtitle = if (onlineCount > 0) "HLS/MP4 优先开播" else "等待在线源",
                tone = SourceLibraryTone.Online,
            ),
            SourceStrategyUiState(
                id = "backup",
                title = "备用补源",
                value = "${btCount.coerceAtLeast(0)} 源",
                subtitle = if (btCount > 0) "资源站作为补充" else "暂无 BT 备用",
                tone = SourceLibraryTone.Backup,
            ),
            SourceStrategyUiState(
                id = "cache",
                title = "离线缓存",
                value = "${downloadableCount.coerceAtLeast(0)} 源",
                subtitle = if (downloadableCount > 0) "可播线路可缓存" else "待接缓存能力",
                tone = SourceLibraryTone.Cache,
            ),
            SourceStrategyUiState(
                id = "web",
                title = "网页兜底",
                value = "${webViewCount.coerceAtLeast(0)} 源",
                subtitle = if (webViewCount > 0) "复杂页面再嗅探" else "优先原生线路",
                tone = SourceLibraryTone.Web,
            ),
        ),
        sourceCards = manifests.map { it.toSourceCardUiState() },
        sourceListTitle = if (providerCount > 0) "已接入线路" else "暂无线路来源",
        sourceListSummary = if (providerCount > 0) {
            "默认由详情页自动选择最佳线路，手动切换只在卡顿、失效或想换清晰度时进入。"
        } else {
            "先添加或启用来源，详情页才能自动匹配播放线路。"
        },
        emptyTitle = "暂无可展示来源",
        emptySubtitle = "接入在线源、BT 资源站或规则源后，这里会显示线路能力和兜底策略。",
    )
}

internal fun buildCacheLibraryUiState(
    manifests: List<SourceManifest>,
    advancedEngineAvailable: Boolean = false,
): CacheLibraryUiState {
    val media3SourceCount = manifests.count { manifest ->
        val cacheCapable = manifest.supportsDownload || SourceCapability.DOWNLOAD in manifest.capabilities
        val nativeStream = SourceCapability.STREAM in manifest.capabilities &&
            SourceCapability.BITTORRENT !in manifest.capabilities &&
            SourceCapability.WEBVIEW_SNIFF !in manifest.capabilities &&
            !manifest.requiresWebView
        cacheCapable && nativeStream
    }
    val btSourceCount = manifests.count { SourceCapability.BITTORRENT in it.capabilities }
    val webBlockedSourceCount = manifests.count {
        it.requiresWebView || SourceCapability.WEBVIEW_SNIFF in it.capabilities
    }
    val cacheableSourceCount = media3SourceCount + btSourceCount
    val headline = if (cacheableSourceCount > 0) {
        "离线片库已接入 $cacheableSourceCount 个缓存来源"
    } else {
        "离线片库待接入可缓存来源"
    }
    val summary = when {
        cacheableSourceCount == 0 -> "HLS / DASH / MP4 或 BT 来源接入后，这里会展示离线、预缓存和边下边播能力。"
        media3SourceCount > 0 && btSourceCount > 0 -> "在线播放可进 Media3 离线队列，BT 资源走边下边播缓存；WebView/DRM 线路会显示阻断原因。"
        media3SourceCount > 0 -> "HLS / DASH / MP4 线路可进入 Media3 离线队列，适合提前缓存后离线观看。"
        else -> "当前以 BT 边下边播缓存为主，Media3 离线队列等待在线可缓存源补齐。"
    }
    val chips = buildList {
        add(SourceLibraryChipUiState("${cacheableSourceCount} 可缓存", SourceLibraryTone.Cache))
        add(SourceLibraryChipUiState("${media3SourceCount} Media3", SourceLibraryTone.Online))
        if (btSourceCount > 0) add(SourceLibraryChipUiState("${btSourceCount} BT", SourceLibraryTone.Backup))
        if (webBlockedSourceCount > 0) add(SourceLibraryChipUiState("${webBlockedSourceCount} 阻断", SourceLibraryTone.Web))
    }
    return CacheLibraryUiState(
        headline = headline,
        summary = summary,
        cacheableSourceCount = cacheableSourceCount,
        media3SourceCount = media3SourceCount,
        btSourceCount = btSourceCount,
        webBlockedSourceCount = webBlockedSourceCount,
        advancedEngineAvailable = advancedEngineAvailable,
        chips = chips,
        capabilities = listOf(
            CacheCapabilityUiState(
                id = "media3",
                title = "在线视频缓存",
                value = if (media3SourceCount > 0) "${media3SourceCount} 源" else "待接",
                subtitle = if (media3SourceCount > 0) {
                    "HLS / DASH / MP4 可进入 Media3 离线队列"
                } else {
                    "等待可缓存的原生在线线路"
                },
                tone = SourceLibraryTone.Online,
            ),
            CacheCapabilityUiState(
                id = "bt",
                title = "边下边播",
                value = if (btSourceCount > 0) "${btSourceCount} 源" else "待源",
                subtitle = if (btSourceCount > 0) {
                    "BT 资源由本地边下边播缓存路径接管"
                } else {
                    "暂无 BT 备用缓存来源"
                },
                tone = SourceLibraryTone.Backup,
            ),
            CacheCapabilityUiState(
                id = "blocked",
                title = "不可缓存线路",
                value = if (webBlockedSourceCount > 0) "${webBlockedSourceCount} 源" else "无阻断",
                subtitle = if (webBlockedSourceCount > 0) {
                    "WebView / 嗅探类线路需要在线播放或切源"
                } else {
                    "当前来源未声明网页兜底阻断"
                },
                tone = if (webBlockedSourceCount > 0) SourceLibraryTone.Web else SourceLibraryTone.Muted,
            ),
            CacheCapabilityUiState(
                id = "advanced",
                title = "批量离线",
                value = if (advancedEngineAvailable) "可用" else "待接",
                subtitle = if (advancedEngineAvailable) {
                    "高级下载运行时已可接管复杂任务"
                } else {
                    "剧集、字幕和多清晰度批量任务等待高级运行时"
                },
                tone = if (advancedEngineAvailable) SourceLibraryTone.Cache else SourceLibraryTone.Muted,
            ),
        ),
    )
}

internal fun buildProfileCenterUiState(
    version: String,
    sourceCount: Int,
    danmakuCount: Int,
    cacheState: CacheLibraryUiState,
): ProfileCenterUiState {
    val summary = when {
        sourceCount == 0 -> "先接入来源后，这里会汇总追番、缓存、弹幕和播放源能力。"
        cacheState.cacheableSourceCount > 0 -> "继续看、缓存、弹幕和线路都收在这里；${cacheState.cacheableSourceCount} 个来源已具备缓存能力。"
        else -> "继续看、弹幕和线路已归拢；离线缓存等待可缓存来源补齐。"
    }
    return ProfileCenterUiState(
        version = version,
        headline = "我的追番中心",
        summary = summary,
        sourceCount = sourceCount,
        danmakuCount = danmakuCount,
        cacheableSourceCount = cacheState.cacheableSourceCount,
        chips = listOf(
            SourceLibraryChipUiState("v$version", SourceLibraryTone.Primary),
            SourceLibraryChipUiState("${sourceCount} 来源", SourceLibraryTone.Online),
            SourceLibraryChipUiState("${danmakuCount} 弹幕平台", SourceLibraryTone.Web),
            SourceLibraryChipUiState("${cacheState.cacheableSourceCount} 可缓存", SourceLibraryTone.Cache),
        ),
        quickActions = listOf(
            ProfileQuickActionUiState(
                id = "continue",
                title = "追番记录",
                subtitle = "继续看入口",
                tone = SourceLibraryTone.Primary,
            ),
            ProfileQuickActionUiState(
                id = "cache",
                title = "离线缓存",
                subtitle = "${cacheState.cacheableSourceCount} 源可缓存",
                tone = SourceLibraryTone.Cache,
            ),
            ProfileQuickActionUiState(
                id = "danmaku",
                title = "弹幕设置",
                subtitle = "${danmakuCount} 平台样式",
                tone = SourceLibraryTone.Web,
            ),
            ProfileQuickActionUiState(
                id = "sources",
                title = "线路管理",
                subtitle = "${sourceCount} 个来源",
                tone = SourceLibraryTone.Backup,
            ),
        ),
        settings = listOf(
            ProfileSettingUiState(
                id = "core",
                title = "播放内核",
                subtitle = "在线播放默认走 Media3，疑难格式后续再接入兜底内核",
                value = "Media3",
                tone = SourceLibraryTone.Online,
            ),
            ProfileSettingUiState(
                id = "cache",
                title = "离线缓存",
                subtitle = "HLS / DASH / MP4 走 Media3 队列，BT 由边下边播引擎接管",
                value = "${cacheState.cacheableSourceCount} 源",
                tone = SourceLibraryTone.Cache,
            ),
            ProfileSettingUiState(
                id = "danmaku",
                title = "弹幕样式",
                subtitle = "B站 / 腾讯 / 爱奇艺 / 优酷样式持续补齐",
                value = "${danmakuCount} 平台",
                tone = SourceLibraryTone.Web,
            ),
            ProfileSettingUiState(
                id = "sources",
                title = "播放源策略",
                subtitle = "自动最佳优先，手动换源保留给卡顿和失效场景",
                value = "${sourceCount} 来源",
                tone = SourceLibraryTone.Primary,
            ),
        ),
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

internal fun buildPlayerEpisodePanelUiState(
    detail: MediaDetail,
    currentEpisode: Episode,
    episodeLoadingId: String?,
): PlayerEpisodePanelUiState {
    val currentEpisodeLabel = currentEpisode.index?.let { "第 $it 集" }
        ?: currentEpisode.title.takeIf { it.isNotBlank() }
        ?: "当前集"
    val episodeCount = detail.episodes.size
    val loadingEpisode = detail.episodes.firstOrNull { it.id == episodeLoadingId }
    val summary = when {
        episodeCount <= 0 -> "当前条目没有可切换选集"
        loadingEpisode != null -> "${episodeTitleForPlayer(loadingEpisode)} 正在准备播放源"
        else -> "当前 $currentEpisodeLabel · 共 $episodeCount 集"
    }
    val chips = buildList {
        add(SearchResultChipUiState("正在看", SourceLibraryTone.Primary))
        add(SearchResultChipUiState("自动匹配", SourceLibraryTone.Online))
        if (loadingEpisode != null) {
            add(SearchResultChipUiState("切换中", SourceLibraryTone.Backup))
        } else if (episodeCount > 1) {
            add(SearchResultChipUiState("${episodeCount}集", SourceLibraryTone.Cache))
        }
    }
    val items = detail.episodes.map { episode ->
        val loading = episode.id == episodeLoadingId
        val selected = episode.id == currentEpisode.id
        val enabled = episodeLoadingId == null || loading
        PlayerEpisodeOptionUiState(
            episode = episode,
            title = episode.title.ifBlank { episodeTitleForPlayer(episode) },
            indexLabel = episodeTitleForPlayer(episode),
            statusLabel = when {
                loading -> "加载中"
                selected -> "当前"
                else -> ""
            },
            actionLabel = when {
                loading -> "加载中"
                selected -> "播放中"
                enabled -> "播放"
                else -> "等待"
            },
            selected = selected,
            loading = loading,
            enabled = enabled,
            tone = when {
                loading -> SourceLibraryTone.Backup
                selected -> SourceLibraryTone.Primary
                enabled -> SourceLibraryTone.Online
                else -> SourceLibraryTone.Muted
            },
        )
    }
    return PlayerEpisodePanelUiState(
        title = detail.title,
        summary = summary,
        listTitle = "全部选集",
        emptyText = "当前条目没有可切换选集",
        chips = chips,
        items = items,
    )
}

private fun episodeTitleForPlayer(episode: Episode): String {
    return episode.index?.let { "第 $it 集" }
        ?: episode.title.takeIf { it.isNotBlank() }
        ?: "特别篇"
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

internal fun buildPlayerDanmakuSettingsUiState(
    enabled: Boolean,
    density: Float,
    alpha: Float,
    fontScale: Float,
    safeArea: PlayerDanmakuSafeAreaUiState? = null,
): PlayerDanmakuSettingsUiState {
    val densityLabel = formatDanmakuDensityForUi(density)
    val alphaLabel = formatPercentForUi(alpha)
    val fontScaleLabel = formatScaleForUi(fontScale)
    val safetySummary = safeArea?.let { area ->
        "避让 顶${area.topInsetDp} / 底${area.bottomInsetDp} / 侧${area.startInsetDp + area.endInsetDp}"
    } ?: "自动避让播放器控制区"
    return PlayerDanmakuSettingsUiState(
        toggleTitle = if (enabled) "弹幕已开启" else "弹幕已关闭",
        toggleSubtitle = if (enabled) "点击关闭弹幕显示" else "点击开启弹幕显示",
        toggleSelected = enabled,
        densityLabel = densityLabel,
        alphaLabel = alphaLabel,
        fontScaleLabel = fontScaleLabel,
        safetySummary = safetySummary,
        tone = if (enabled) SourceLibraryTone.Primary else SourceLibraryTone.Muted,
    )
}

internal fun buildPlayerQualityPanelUiState(
    routes: List<RouteCandidate>,
    currentStream: MediaStream,
): PlayerQualityPanelUiState {
    val currentQualityLabel = currentStream.quality
        ?.takeIf { it.isNotBlank() && !it.equals("auto", ignoreCase = true) && it != "自动" }
        ?: "自动"
    val options = routes
        .groupBy { routeQualityLabelForUi(it) }
        .mapNotNull { (_, group) -> group.maxByOrNull { it.score } }
        .sortedByDescending { it.score }
        .map { route ->
            val label = routeQualityLabelForUi(route)
            val selected = label == currentQualityLabel || route.stream.id == currentStream.id
            PlayerQualityOptionUiState(
                route = route,
                title = label,
                subtitle = "${route.sourceName} · ${routePrimaryLabelForUi(route)}",
                selected = selected,
                actionLabel = if (selected) "使用中" else "切换",
                tone = when {
                    selected -> SourceLibraryTone.Primary
                    route.protocol == StreamProtocol.BITTORRENT -> SourceLibraryTone.Backup
                    route.protocol == StreamProtocol.WEBVIEW_ONLY -> SourceLibraryTone.Web
                    else -> SourceLibraryTone.Online
                },
            )
        }
    return PlayerQualityPanelUiState(
        summary = if (options.isEmpty()) {
            "当前播放源没有提供可切换清晰度"
        } else {
            "当前 $currentQualityLabel · ${options.size} 档可选"
        },
        emptyText = "当前播放源没有提供可切换清晰度",
        currentQualityLabel = currentQualityLabel,
        options = options,
    )
}

internal fun routeQualityLabelForUi(route: RouteCandidate): String {
    val quality = route.quality?.takeIf { it.isNotBlank() }
        ?: route.stream.quality?.takeIf { it.isNotBlank() }
        ?: route.routeName?.takeIf { it.isNotBlank() }
        ?: route.protocol.uiProtocolName()
    return if (quality.equals("auto", ignoreCase = true) || quality == "自动") {
        "自动"
    } else {
        quality
    }
}

internal fun buildPlayerSpeedPanelUiState(
    playbackSpeed: Float,
    speeds: List<Float> = defaultPlayerSpeedOptions,
): PlayerSpeedPanelUiState {
    val options = speeds.distinct().sorted().map { speed ->
        val selected = playbackSpeed.nearlyEquals(speed)
        PlayerSpeedOptionUiState(
            speed = speed,
            title = formatPlaybackSpeedForUi(speed),
            subtitle = when {
                speed.nearlyEquals(1f) -> "标准速度"
                speed < 1f -> "慢速回看"
                else -> "快速播放"
            },
            selected = selected,
            actionLabel = if (selected) "使用中" else "切换",
            tone = if (selected) SourceLibraryTone.Primary else SourceLibraryTone.Online,
        )
    }
    return PlayerSpeedPanelUiState(
        summary = "当前 ${formatPlaybackSpeedForUi(playbackSpeed)} · ${options.size} 档可选",
        options = options,
    )
}

internal fun buildPlayerMorePanelUiState(
    routeCount: Int,
    routeCoverageLabel: String,
    episodeCount: Int,
    routeLabel: String,
    quality: String,
    playbackSpeed: Float,
    danmakuEnabled: Boolean,
    cacheAction: PlayerCacheActionUiState,
): PlayerMorePanelUiState {
    val normalizedQuality = quality.ifBlank { "自动" }
    val speedLabel = formatPlaybackSpeedForUi(playbackSpeed)
    val normalizedEpisodeCount = episodeCount.coerceAtLeast(1)
    val normalizedRouteLabel = routeLabel.ifBlank { "自动推荐" }
    val routeSummary = when {
        routeCount > 1 -> routeCoverageLabel.ifBlank { "$routeCount 条线路" }
        else -> "自动推荐"
    }
    return PlayerMorePanelUiState(
        summaryBadge = "当前设置",
        summaryPrimary = listOf("清晰度 $normalizedQuality", "倍速 $speedLabel").joinToString(" · "),
        summarySecondary = listOf(
            "当前源 $normalizedRouteLabel",
            routeSummary,
            "$normalizedEpisodeCount 集",
        ).joinToString(" · "),
        actions = listOf(
            PlayerMoreActionUiState(
                kind = PlayerMoreActionKind.Quality,
                title = "清晰度",
                subtitle = normalizedQuality,
                enabled = true,
                selected = false,
                tone = SourceLibraryTone.Primary,
            ),
            PlayerMoreActionUiState(
                kind = PlayerMoreActionKind.Speed,
                title = "倍速",
                subtitle = speedLabel,
                enabled = true,
                selected = false,
                tone = SourceLibraryTone.Online,
            ),
            PlayerMoreActionUiState(
                kind = PlayerMoreActionKind.Episode,
                title = "选集",
                subtitle = "共 $normalizedEpisodeCount 集",
                enabled = episodeCount > 1,
                selected = false,
                tone = SourceLibraryTone.Backup,
            ),
            PlayerMoreActionUiState(
                kind = PlayerMoreActionKind.Route,
                title = "换源",
                subtitle = routeSummary,
                enabled = routeCount > 1,
                selected = false,
                tone = SourceLibraryTone.Online,
            ),
            PlayerMoreActionUiState(
                kind = PlayerMoreActionKind.Danmaku,
                title = "弹幕",
                subtitle = if (danmakuEnabled) "已开启" else "已关闭",
                enabled = true,
                selected = danmakuEnabled,
                tone = if (danmakuEnabled) SourceLibraryTone.Primary else SourceLibraryTone.Muted,
            ),
            PlayerMoreActionUiState(
                kind = PlayerMoreActionKind.Cache,
                title = cacheAction.title,
                subtitle = if (cacheAction.enabled) cacheAction.actionLabel else cacheAction.reason,
                enabled = cacheAction.enabled,
                selected = false,
                tone = if (cacheAction.enabled) SourceLibraryTone.Primary else SourceLibraryTone.Muted,
            ),
        ),
    )
}

internal fun buildPlayerPanelSheetUiState(
    selectedPanel: PlayerPanelKind,
    title: String,
    episodeIndex: Int?,
    sourceLabel: String,
    quality: String,
    playbackSpeed: Float,
    routeCount: Int,
    routeCoverageLabel: String,
    episodeCount: Int,
    danmakuEnabled: Boolean,
): PlayerPanelSheetUiState {
    val normalizedQuality = quality.ifBlank { "自动" }
    val speedLabel = formatPlaybackSpeedForUi(playbackSpeed)
    val normalizedSource = sourceLabel.ifBlank { "自动源" }
    val normalizedEpisodeCount = episodeCount.coerceAtLeast(1)
    val episodeLabel = episodeIndex?.let { "第 $it 集" } ?: "当前集"
    val routeValue = when {
        routeCount > 1 -> routeCoverageLabel.ifBlank { "$routeCount 条线路" }
        else -> "自动"
    }
    return PlayerPanelSheetUiState(
        title = selectedPanel.titleForUi(),
        subtitle = selectedPanel.subtitleForUi(),
        context = PlayerPanelContextUiState(
            title = title.ifBlank { "正在播放" },
            metadata = listOf(episodeLabel, normalizedSource, normalizedQuality, speedLabel).joinToString(" · "),
            statusLabel = "播放中",
        ),
        tabs = listOf(
            PlayerPanelTabUiState(
                kind = PlayerPanelKind.Quality,
                label = "清晰度",
                value = null,
                enabled = routeCount > 0,
                selected = selectedPanel == PlayerPanelKind.Quality,
                highlighted = false,
                tone = SourceLibraryTone.Primary,
            ),
            PlayerPanelTabUiState(
                kind = PlayerPanelKind.Speed,
                label = "倍速",
                value = null,
                enabled = true,
                selected = selectedPanel == PlayerPanelKind.Speed,
                highlighted = false,
                tone = SourceLibraryTone.Online,
            ),
            PlayerPanelTabUiState(
                kind = PlayerPanelKind.Route,
                label = "换源",
                value = routeValue,
                enabled = routeCount > 1,
                selected = selectedPanel == PlayerPanelKind.Route,
                highlighted = false,
                tone = SourceLibraryTone.Online,
            ),
            PlayerPanelTabUiState(
                kind = PlayerPanelKind.Episode,
                label = "选集",
                value = if (episodeCount > 1) "${normalizedEpisodeCount}集" else "单集",
                enabled = episodeCount > 1,
                selected = selectedPanel == PlayerPanelKind.Episode,
                highlighted = false,
                tone = SourceLibraryTone.Backup,
            ),
            PlayerPanelTabUiState(
                kind = PlayerPanelKind.Danmaku,
                label = "弹幕",
                value = if (danmakuEnabled) "开" else "关",
                enabled = true,
                selected = selectedPanel == PlayerPanelKind.Danmaku,
                highlighted = danmakuEnabled,
                tone = if (danmakuEnabled) SourceLibraryTone.Primary else SourceLibraryTone.Muted,
            ),
            PlayerPanelTabUiState(
                kind = PlayerPanelKind.More,
                label = "设置",
                value = null,
                enabled = true,
                selected = selectedPanel == PlayerPanelKind.More,
                highlighted = false,
                tone = SourceLibraryTone.Muted,
            ),
        ),
    )
}

internal fun buildPlayerTopStatusStripUiState(
    overlayState: PlayerOverlayState,
    episodeValue: String,
    routeCount: Int,
    routeCoverageLabel: String,
    playbackSpeed: Float,
): PlayerTopStatusStripUiState {
    return PlayerTopStatusStripUiState(
        chips = listOf(
            PlayerStatusChipUiState(
                label = "本集",
                value = episodeValue.ifBlank { overlayState.episodeTitle.ifBlank { "当前集" } },
                tone = SourceLibraryTone.Primary,
            ),
            PlayerStatusChipUiState(
                label = "来源",
                value = playerSourceStatusValueForUi(
                    sourceLabel = overlayState.sourceLabel,
                    routeCount = routeCount,
                    routeCoverageLabel = routeCoverageLabel,
                ),
                tone = SourceLibraryTone.Online,
            ),
            PlayerStatusChipUiState(
                label = "清晰度",
                value = overlayState.qualityLabel.ifBlank { "自动" },
                tone = SourceLibraryTone.Backup,
            ),
            PlayerStatusChipUiState(
                label = "倍速",
                value = formatPlaybackSpeedForUi(playbackSpeed),
                tone = SourceLibraryTone.Cache,
            ),
        ),
    )
}

internal fun buildPlayerTopOverlayUiState(
    overlayState: PlayerOverlayState,
    currentEpisode: Episode,
    episodeCount: Int,
    routeCount: Int,
    routeCoverageLabel: String,
    playbackSpeed: Float,
): PlayerTopOverlayUiState {
    val episodeValue = playerTopEpisodeValueForUi(
        currentEpisode = currentEpisode,
        episodeCount = episodeCount,
        overlayEpisodeTitle = overlayState.episodeTitle,
    )
    val subtitle = listOf(
        overlayState.episodeTitle.ifBlank { episodeValue },
        overlayState.playbackState,
    )
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(" · ")
        .ifBlank { "播放中" }
    return PlayerTopOverlayUiState(
        title = overlayState.title.ifBlank { "正在播放" },
        subtitle = subtitle,
        compactNotice = buildPlayerOverlayNoticeUiState(overlayState),
        routeStatus = buildPlayerRouteStatusUiState(overlayState),
        statusStrip = buildPlayerTopStatusStripUiState(
            overlayState = overlayState,
            episodeValue = episodeValue,
            routeCount = routeCount,
            routeCoverageLabel = routeCoverageLabel,
            playbackSpeed = playbackSpeed,
        ),
    )
}

private fun playerTopEpisodeValueForUi(
    currentEpisode: Episode,
    episodeCount: Int,
    overlayEpisodeTitle: String,
): String {
    val normalizedEpisodeCount = episodeCount.coerceAtLeast(0)
    return when {
        currentEpisode.index != null && normalizedEpisodeCount > 1 -> "${currentEpisode.index}/$normalizedEpisodeCount"
        currentEpisode.index != null -> "第 ${currentEpisode.index} 集"
        currentEpisode.title.isNotBlank() -> currentEpisode.title
        overlayEpisodeTitle.isNotBlank() -> overlayEpisodeTitle
        else -> "当前集"
    }
}

internal fun buildPlayerFullscreenStatusStripUiState(
    routeSummary: String,
    quality: String,
    routeCount: Int,
    routeCoverageLabel: String,
    episodeCount: Int,
    playbackSpeed: Float,
    hasPlaybackIssue: Boolean,
): PlayerFullscreenStatusStripUiState {
    return PlayerFullscreenStatusStripUiState(
        statusLabel = if (hasPlaybackIssue) "播放异常" else "正在播放",
        routeSummary = routeSummary.ifBlank { "自动线路" },
        tags = listOfNotNull(
            quality.ifBlank { "自动" },
            formatPlaybackSpeedForUi(playbackSpeed),
            routeCoverageLabel.takeIf { routeCount > 1 && it.isNotBlank() },
            episodeCount.takeIf { it > 1 }?.let { "$it 集" },
        ),
        error = hasPlaybackIssue,
    )
}

internal fun buildPlayerRouteStatusUiState(
    overlayState: PlayerOverlayState,
): PlayerRouteStatusUiState {
    val error = !overlayState.error.isNullOrBlank()
    val hasNotice = !overlayState.notice.isNullOrBlank()
    return PlayerRouteStatusUiState(
        routeLabel = overlayState.routeLabel.ifBlank { "自动线路" },
        statusLabel = overlayState.statusLabel.ifBlank { "自动" },
        tone = when {
            error -> SourceLibraryTone.Web
            hasNotice -> SourceLibraryTone.Backup
            else -> SourceLibraryTone.Online
        },
        error = error,
    )
}

internal fun buildPlayerOverlayNoticeUiState(
    overlayState: PlayerOverlayState,
): PlayerNoticeUiState? {
    val message = overlayState.error?.takeIf { it.isNotBlank() }
        ?: overlayState.notice?.takeIf { it.isNotBlank() }
        ?: return null
    val error = !overlayState.error.isNullOrBlank()
    return PlayerNoticeUiState(
        title = overlayState.statusLabel.ifBlank { if (error) "播放异常" else "播放提示" },
        message = message,
        tone = if (error) SourceLibraryTone.Web else SourceLibraryTone.Backup,
        error = error,
    )
}

internal fun buildPlayerFullscreenNoticeUiState(
    routeSummary: String,
    routeNotice: String?,
    errorMessage: String?,
): PlayerNoticeUiState? {
    val message = routeNotice?.takeIf { it.isNotBlank() }
        ?: errorMessage?.takeIf { it.isNotBlank() }
        ?: return null
    val error = routeNotice.isNullOrBlank() && !errorMessage.isNullOrBlank()
    return PlayerNoticeUiState(
        title = routeSummary.ifBlank { "自动线路" },
        message = message,
        tone = if (error) SourceLibraryTone.Web else SourceLibraryTone.Backup,
        error = error,
    )
}

internal fun buildPlayerActionBarUiState(
    quality: String,
    routeCount: Int,
    routeCoverageLabel: String,
    episodeCount: Int,
    nextEpisode: Episode?,
    playbackSpeed: Float,
    activePanel: PlayerPanelKind?,
    cacheAction: PlayerCacheActionUiState,
    hasPlaybackIssue: Boolean,
    canSelectNextRoute: Boolean,
): PlayerActionBarUiState {
    val actions = buildList {
        if (hasPlaybackIssue) {
            add(
                PlayerActionUiState(
                    kind = PlayerActionKind.Retry,
                    title = "重试",
                    value = "当前",
                    selected = true,
                    enabled = true,
                    tone = SourceLibraryTone.Primary,
                ),
            )
            add(
                PlayerActionUiState(
                    kind = PlayerActionKind.NextRoute,
                    title = "换个源",
                    value = if (canSelectNextRoute) "可切" else "无",
                    selected = false,
                    enabled = canSelectNextRoute,
                    tone = SourceLibraryTone.Online,
                ),
            )
        }
        add(
            PlayerActionUiState(
                kind = PlayerActionKind.Quality,
                title = "清晰度",
                value = quality.ifBlank { "自动" },
                selected = activePanel == PlayerPanelKind.Quality,
                enabled = routeCount > 0,
                tone = SourceLibraryTone.Primary,
            ),
        )
        add(
            PlayerActionUiState(
                kind = PlayerActionKind.Speed,
                title = "倍速",
                value = formatPlaybackSpeedForUi(playbackSpeed),
                selected = activePanel == PlayerPanelKind.Speed,
                enabled = true,
                tone = SourceLibraryTone.Online,
            ),
        )
        add(
            PlayerActionUiState(
                kind = PlayerActionKind.Route,
                title = "换源",
                value = if (routeCount > 1) routeCoverageLabel.ifBlank { "$routeCount 条" } else "自动",
                selected = activePanel == PlayerPanelKind.Route,
                enabled = routeCount > 1,
                tone = SourceLibraryTone.Online,
            ),
        )
        add(
            PlayerActionUiState(
                kind = PlayerActionKind.Episode,
                title = "选集",
                value = if (episodeCount > 1) "${episodeCount}集" else "单集",
                selected = activePanel == PlayerPanelKind.Episode,
                enabled = episodeCount > 1,
                tone = SourceLibraryTone.Backup,
            ),
        )
        add(
            PlayerActionUiState(
                kind = PlayerActionKind.NextEpisode,
                title = "下一集",
                value = nextEpisode.playerNextEpisodeValueForUi(),
                selected = false,
                enabled = nextEpisode != null,
                tone = SourceLibraryTone.Backup,
            ),
        )
        add(
            PlayerActionUiState(
                kind = PlayerActionKind.Cache,
                title = cacheAction.title,
                value = cacheAction.value,
                selected = false,
                enabled = cacheAction.enabled,
                tone = if (cacheAction.enabled) SourceLibraryTone.Cache else SourceLibraryTone.Muted,
            ),
        )
        add(
            PlayerActionUiState(
                kind = PlayerActionKind.More,
                title = "更多",
                value = "设置",
                selected = activePanel == PlayerPanelKind.More,
                enabled = true,
                tone = SourceLibraryTone.Muted,
            ),
        )
    }
    return PlayerActionBarUiState(actions = actions)
}

private fun Episode?.playerNextEpisodeValueForUi(): String {
    return this?.index?.let { "第${it}集" } ?: this?.title?.takeIf { it.isNotBlank() } ?: "无"
}

internal fun buildPortraitWatchInfoUiState(
    detail: MediaDetail,
    episode: Episode,
    stream: MediaStream,
    routes: List<RouteCandidate>,
    playbackState: String,
    routeNotice: String?,
    errorMessage: String?,
    hasPlaybackIssue: Boolean,
): PortraitWatchInfoUiState {
    val currentRoute = routes.firstOrNull { it.stream.id == stream.id || it.stream.url == stream.url }
    val quality = stream.quality.orEmpty().ifBlank { "自动" }
    val currentEpisodeLabel = episode.index?.let { "第 $it 集" } ?: "当前集"
    val playbackStateLabel = playerPlaybackStateLabelForUi(playbackState)
    val sourceName = playerSourceLabelForUi(stream, currentRoute)
    val routeModeLabel = if (routes.size > 1) "自动推荐 · 可换源" else "自动推荐"
    val playbackBrief = listOf(
        quality.takeIf { it != "自动" } ?: "自动清晰度",
        sourceName.takeIf { it.isNotBlank() },
        playbackStateLabel.takeIf { it.isNotBlank() },
        routeModeLabel,
    )
        .filterNotNull()
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(" · ")
    val actions = buildList {
        if (detail.episodes.size > 1) {
            add(
                PortraitWatchActionUiState(
                    kind = PlayerPanelKind.Episode,
                    title = "选集",
                    subtitle = episode.index?.let { "$it/${detail.episodes.size}" } ?: "${detail.episodes.size}集",
                    enabled = true,
                    tone = SourceLibraryTone.Primary,
                ),
            )
        }
        if (routes.size > 1) {
            add(
                PortraitWatchActionUiState(
                    kind = PlayerPanelKind.Route,
                    title = "换源",
                    subtitle = playerRouteCoverageLabel(routes),
                    enabled = true,
                    tone = SourceLibraryTone.Online,
                ),
            )
        }
    }
    val diagnostic = buildPlayerFullscreenNoticeUiState(
        routeSummary = "播放提示",
        routeNotice = routeNotice,
        errorMessage = errorMessage,
    ) ?: if (hasPlaybackIssue) {
        PlayerNoticeUiState(
            title = "播放提示",
            message = "当前播放源需要处理",
            tone = SourceLibraryTone.Muted,
            error = false,
        )
    } else {
        null
    }
    return PortraitWatchInfoUiState(
        title = detail.title.ifBlank { "正在播放" },
        episodeTitle = episode.title.ifBlank { currentEpisodeLabel },
        currentEpisodeLabel = currentEpisodeLabel,
        metaChips = listOf(currentEpisodeLabel, quality, playbackStateLabel).filter { it.isNotBlank() },
        playbackBrief = playbackBrief,
        actions = actions,
        diagnostic = diagnostic,
    )
}

internal fun buildPortraitEpisodeRailUiState(
    detail: MediaDetail,
    currentEpisode: Episode,
    episodeLoadingId: String?,
    maxCount: Int = 18,
): PortraitEpisodeRailUiState {
    val episodes = detail.episodes
    val visible = episodes.size > 1
    val visibleEpisodes = if (visible) {
        portraitEpisodeWindowForUi(episodes, currentEpisode, maxCount)
    } else {
        emptyList()
    }
    val items = visibleEpisodes.map { episode ->
        val loading = episode.id == episodeLoadingId
        val selected = episode.id == currentEpisode.id
        val enabled = episodeLoadingId == null || loading
        PortraitEpisodeRailItemUiState(
            episode = episode,
            indexLabel = episode.index?.let { "%02d".format(it) } ?: "SP",
            title = if (loading) "加载中" else episode.title.ifBlank { episodeTitleForPlayer(episode) },
            selected = selected,
            loading = loading,
            enabled = enabled,
            tone = when {
                loading -> SourceLibraryTone.Backup
                selected -> SourceLibraryTone.Online
                enabled -> SourceLibraryTone.Muted
                else -> SourceLibraryTone.Muted
            },
        )
    }
    return PortraitEpisodeRailUiState(
        visible = visible,
        title = "选集",
        allEpisodesLabel = "全部 ${episodes.size} 集",
        items = items,
        moreAction = if (visible && episodes.size > visibleEpisodes.size) {
            PortraitEpisodeMoreActionUiState(
                title = "全部",
                subtitle = "${episodes.size}集",
                count = episodes.size,
            )
        } else {
            null
        },
    )
}

private fun portraitEpisodeWindowForUi(
    episodes: List<Episode>,
    currentEpisode: Episode,
    maxCount: Int,
): List<Episode> {
    if (episodes.size <= maxCount) return episodes
    val currentIndex = episodes.indexOfFirst { it.id == currentEpisode.id }
    if (currentIndex < 0) return episodes.take(maxCount)
    val start = (currentIndex - 4).coerceIn(0, episodes.size - maxCount)
    return episodes.subList(start, start + maxCount).toList()
}

internal fun buildPortraitRecoveryActionsUiState(
    hasPlaybackIssue: Boolean,
    canSelectNextRoute: Boolean,
): PortraitRecoveryActionsUiState {
    if (!hasPlaybackIssue) {
        return PortraitRecoveryActionsUiState(visible = false, actions = emptyList())
    }
    return PortraitRecoveryActionsUiState(
        visible = true,
        actions = listOf(
            PlayerActionUiState(
                kind = PlayerActionKind.Retry,
                title = "重试当前",
                value = null,
                selected = true,
                enabled = true,
                tone = SourceLibraryTone.Primary,
            ),
            PlayerActionUiState(
                kind = PlayerActionKind.NextRoute,
                title = "换个源",
                value = if (canSelectNextRoute) "可切" else "无",
                selected = false,
                enabled = canSelectNextRoute,
                tone = if (canSelectNextRoute) SourceLibraryTone.Online else SourceLibraryTone.Muted,
            ),
        ),
    )
}

internal fun buildPortraitRouteInsightUiState(
    routes: List<RouteCandidate>,
    stream: MediaStream,
): PortraitRouteInsightUiState {
    val routeCoverageLabel = if (routes.isNotEmpty()) playerRouteCoverageLabel(routes) else "单线"
    val onlineRoutes = routes.filter { it.protocol != StreamProtocol.BITTORRENT && it.protocol != StreamProtocol.WEBVIEW_ONLY }
    val btRoutes = routes.filter { it.protocol == StreamProtocol.BITTORRENT }
    val currentLabel = stream.quality?.takeIf { it.isNotBlank() } ?: stream.protocol.uiProtocolName()
    return PortraitRouteInsightUiState(
        chips = listOf(
            PortraitRouteInsightChipUiState("覆盖", routeCoverageLabel, SourceLibraryTone.Online),
            PortraitRouteInsightChipUiState("在线", portraitRouteInsightCountLabel(onlineRoutes) ?: "待匹配", SourceLibraryTone.Primary),
            PortraitRouteInsightChipUiState("备用", portraitRouteInsightCountLabel(btRoutes) ?: "自动", SourceLibraryTone.Backup),
            PortraitRouteInsightChipUiState("当前", currentLabel, SourceLibraryTone.Cache),
        ),
    )
}

private fun portraitRouteInsightCountLabel(routes: List<RouteCandidate>): String? {
    if (routes.isEmpty()) return null
    val sourceCount = routes.map { it.sourceId }.distinct().size
    val routeCount = routes.distinctBy { it.stream.id }.size
    return when {
        sourceCount > 1 && routeCount > sourceCount -> "${sourceCount}源 · ${routeCount}线"
        sourceCount > 1 -> "${sourceCount}源"
        routeCount > 1 -> "${routeCount}线"
        else -> "单线"
    }
}

internal fun buildTorrentPlaybackPreparationUiState(
    state: TorrentEngineState,
): TorrentPlaybackPreparationUiState {
    val plan = state.plan
    val bufferingPercent = plan?.bufferingPercent ?: 0f
    return TorrentPlaybackPreparationUiState(
        title = "正在准备播放",
        description = "正在匹配视频文件并建立起播缓冲，完成后会自动进入播放。",
        bufferingProgress = (bufferingPercent / 100f).coerceIn(0f, 1f),
        statusLine = "状态: ${state.status ?: "等待中"}",
        readinessLine = "视频信息: ${if (state.hasMetadata) "已获取" else "匹配中"}  播放通道: ${if (plan?.localPlaybackUrl != null) "已就绪" else "准备中"}",
        progressLine = "整体: ${formatTorrentPercentForUi(state.progressPercent)}  视频: ${formatTorrentPercentForUi(state.selectedFileProgressPercent)}  起播: ${formatTorrentPercentForUi(bufferingPercent)}",
        bufferingLine = plan?.takeIf { it.playbackReadyBytes > 0L }?.let {
            "起播缓存: ${formatBytesForUi(it.selectedFileContiguousBytes)} / ${formatBytesForUi(it.playbackReadyBytes)}"
        },
        connectionLine = "连接: ${state.connectedPeers}  高速节点: ${state.connectedSeeds}  速度: ${formatBytesForUi(state.downloadRateBytesPerSecond.toLong())}/s",
        fileLine = plan?.selectedFileName?.let { "文件: $it" },
        sizeLine = plan?.selectedFileSizeBytes?.let { "大小: ${formatBytesForUi(it)}" },
        errorMessage = state.errorMessage,
    )
}

private fun formatTorrentPercentForUi(percent: Float): String {
    return "%.1f%%".format(percent.coerceIn(0f, 100f))
}

private fun playerSourceStatusValueForUi(
    sourceLabel: String,
    routeCount: Int,
    routeCoverageLabel: String,
): String {
    val normalizedSource = sourceLabel.ifBlank { "自动源" }
    return if (routeCount > 1 && routeCoverageLabel.isNotBlank()) {
        "$normalizedSource · $routeCoverageLabel"
    } else {
        normalizedSource
    }
}

private fun PlayerPanelKind.titleForUi(): String {
    return when (this) {
        PlayerPanelKind.More -> "播放设置"
        PlayerPanelKind.Danmaku -> "弹幕设置"
        PlayerPanelKind.Quality -> "清晰度"
        PlayerPanelKind.Speed -> "播放速度"
        PlayerPanelKind.Route -> "播放源"
        PlayerPanelKind.Episode -> "选集"
    }
}

private fun PlayerPanelKind.subtitleForUi(): String {
    return when (this) {
        PlayerPanelKind.More -> "清晰度 · 倍速 · 选集 · 换源"
        PlayerPanelKind.Danmaku -> "密度 · 透明度 · 字号"
        PlayerPanelKind.Quality -> "当前可用质量"
        PlayerPanelKind.Speed -> "0.5x 至 2.0x"
        PlayerPanelKind.Route -> "推荐优先 · 手动换源"
        PlayerPanelKind.Episode -> "合集进度 · 自动匹配"
    }
}

internal fun formatPlaybackSpeedForUi(speed: Float): String {
    val number = "%.2f".format(speed).trimEnd('0').trimEnd('.')
    return "${if (number.contains(".")) number else "$number.0"}x"
}

private val defaultPlayerSpeedOptions = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)

private fun Float.nearlyEquals(other: Float): Boolean {
    return kotlin.math.abs(this - other) < 0.001f
}

internal fun formatDanmakuDensityForUi(density: Float): String {
    return when {
        density < 0.45f -> "30%"
        density < 0.82f -> "60%"
        else -> "100%"
    }
}

internal fun formatPercentForUi(value: Float): String {
    return "%.0f%%".format(value.coerceIn(0f, 1f) * 100f)
}

internal fun formatScaleForUi(value: Float): String {
    return "%.0f%%".format(value * 100f)
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
    val media3Cacheable = stream.protocol in media3StreamingProtocols
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

private val media3StreamingProtocols = setOf(
    StreamProtocol.HLS,
    StreamProtocol.DASH,
    StreamProtocol.SMOOTH_STREAMING,
    StreamProtocol.PROGRESSIVE,
)

internal fun routePrimaryLabelForUi(route: RouteCandidate): String {
    return listOfNotNull(
        route.routeName?.takeIf { it.isNotBlank() },
        route.quality?.takeIf { it.isNotBlank() },
        route.subgroup?.takeIf { it.isNotBlank() }?.take(12),
    ).distinct().joinToString(" · ").ifBlank { route.protocol.uiProtocolName() }
}

internal fun formatBytesForUi(bytes: Long): String {
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var index = 0
    while (value >= 1024.0 && index < units.lastIndex) {
        value /= 1024.0
        index += 1
    }
    return if (index == 0) {
        "$bytes ${units[index]}"
    } else {
        "%.1f %s".format(value, units[index])
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
    val selectedRoute = routes.firstOrNull { it.stream.id == selectedStreamId }
    val availableCount = availableRoutes.size
    val onlineCount = availableRoutes.count { it.protocol != StreamProtocol.BITTORRENT }
    val btCount = availableRoutes.count { it.protocol == StreamProtocol.BITTORRENT }
    val failedCount = failedStreamIds.count { failedId -> routes.any { it.stream.id == failedId } }
    val recommendationReason = routeRecommendationReason(recommendedRoute)
    val compactMetrics = if (failedCount > 0) {
        listOf(
            RoutePanelMetricUiState("可用", availableCount.toString(), SourceLibraryTone.Cache),
            RoutePanelMetricUiState("失败", failedCount.toString(), SourceLibraryTone.Web),
        )
    } else {
        emptyList()
    }
    val detailedMetrics = buildList {
        add(RoutePanelMetricUiState("可用", availableCount.toString(), SourceLibraryTone.Cache))
        add(RoutePanelMetricUiState("在线", onlineCount.toString(), SourceLibraryTone.Online))
        add(RoutePanelMetricUiState("BT", btCount.toString(), SourceLibraryTone.Backup))
        if (failedCount > 0) {
            add(RoutePanelMetricUiState("失败", failedCount.toString(), SourceLibraryTone.Web))
        }
    }
    return RoutePanelUiState(
        recommendedRoute = recommendedRoute,
        selectedRoute = selectedRoute,
        totalCount = routes.size,
        availableCount = availableCount,
        onlineCount = onlineCount,
        btCount = btCount,
        failedCount = failedCount,
        recommendationReason = recommendationReason,
        compactTitle = "推荐源 · 可播 $availableCount 源",
        compactSummary = recommendedRoute?.let { recommendationReason } ?: "暂时没有推荐源",
        detailedTitle = "自动推荐 · 共 ${routes.size} 源",
        detailedSummary = recommendedRoute?.let { route -> "推荐 ${route.sourceName} · $recommendationReason" } ?: "暂无推荐源",
        selectedRouteSummary = selectedRoute?.let { route -> "当前 ${route.sourceName} · ${routePrimaryLabelForUi(route)}" },
        compactMetrics = compactMetrics,
        detailedMetrics = detailedMetrics,
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

private fun SearchResult.searchKeywordCandidate(): String? {
    val rawKeyword = listOf(
        raw["subjectNameCn"],
        raw["nameCn"],
        raw["cn"],
        raw["titleCn"],
        raw["subjectName"],
        raw["name"],
    ).firstOrNull { !it.isNullOrBlank() }
    return (rawKeyword ?: title).trim().takeIf { it.isNotBlank() }
}

private data class SearchProviderKind(
    val providerLabel: String,
    val typeLabel: String,
    val subtitle: String,
    val actionLabel: String,
    val tone: SourceLibraryTone,
)

private fun SearchResult.providerKindForSearch(): SearchProviderKind {
    return when (providerId.lowercase()) {
        "bangumi-catalog" -> SearchProviderKind(
            providerLabel = "Bangumi 资料库",
            typeLabel = "资料库",
            subtitle = "番剧资料 / 选集 / 自动匹配播放源",
            actionLabel = "进详情",
            tone = SourceLibraryTone.Online,
        )
        "direct-url", "direct" -> SearchProviderKind(
            providerLabel = "在线链接",
            typeLabel = "直链",
            subtitle = "直接播放入口 / 进入详情确认线路",
            actionLabel = "确认线路",
            tone = SourceLibraryTone.Primary,
        )
        "mikan", "dmhy", "nyaa", "acg-rip", "bangumi-moe", "bt" -> SearchProviderKind(
            providerLabel = "番剧频道",
            typeLabel = "BT/RSS",
            subtitle = "资源频道 / 边下边播候选",
            actionLabel = "看资源",
            tone = SourceLibraryTone.Backup,
        )
        else -> SearchProviderKind(
            providerLabel = providerDisplayIdForSearch(providerId),
            typeLabel = "视频源",
            subtitle = "来源命中 / 详情页继续匹配线路",
            actionLabel = "进详情",
            tone = SourceLibraryTone.Web,
        )
    }
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

private fun providerDisplayIdForBrowse(providerId: String): String {
    return when (providerId.lowercase()) {
        "bangumi-catalog" -> "Bangumi"
        "direct-url", "direct" -> "\u76f4\u94fe"
        "mikan", "dmhy", "nyaa", "acg-rip", "bangumi-moe", "bt" -> "BT"
        else -> providerDisplayIdForSearch(providerId)
    }
}

private fun Int.compactBrowseCount(): String {
    if (this < 10_000) return toString()
    val whole = this / 10_000
    val tenth = (this % 10_000) / 1_000
    return if (tenth > 0) "$whole.${tenth}\u4e07" else "$whole\u4e07"
}

private fun SourceManifest.toSourceCardUiState(): SourceCardUiState {
    val isBt = SourceCapability.BITTORRENT in capabilities
    val isStream = SourceCapability.STREAM in capabilities
    val statusLabel = when {
        isStream && !isBt -> "在线源"
        isBt -> "资源站"
        SourceCapability.SEARCH in capabilities -> "索引源"
        else -> "辅助源"
    }
    val statusTone = when {
        isStream && !isBt -> SourceLibraryTone.Online
        isBt -> SourceLibraryTone.Backup
        requiresWebView || SourceCapability.WEBVIEW_SNIFF in capabilities -> SourceLibraryTone.Web
        else -> SourceLibraryTone.Muted
    }
    val featureText = buildList {
        if (SourceCapability.SEARCH in capabilities) add("搜索")
        if (SourceCapability.DETAIL in capabilities) add("详情")
        if (SourceCapability.EPISODES in capabilities) add("选集")
        if (isStream) add("播放")
        if (supportsDownload || SourceCapability.DOWNLOAD in capabilities) add("缓存")
        if (requiresWebView || SourceCapability.WEBVIEW_SNIFF in capabilities) add("嗅探")
    }.joinToString(" · ").ifBlank { "基础来源" }
    return SourceCardUiState(
        id = id,
        name = name,
        version = version,
        author = author,
        statusLabel = statusLabel,
        statusTone = statusTone,
        featureText = featureText,
        domainText = domains.joinToString(" · ").ifBlank { "本地内置" },
        isBt = isBt,
    )
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
