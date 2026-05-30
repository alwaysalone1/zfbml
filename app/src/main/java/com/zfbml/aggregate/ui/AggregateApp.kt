package com.zfbml.aggregate.ui

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.zfbml.aggregate.AppGraph
import com.zfbml.aggregate.danmaku.DanmakuItem
import com.zfbml.aggregate.danmaku.DanmakuPlatform
import com.zfbml.aggregate.danmaku.DanmakuProfile
import com.zfbml.aggregate.danmaku.DanmakuSettings
import com.zfbml.aggregate.danmaku.DanmakuSurface
import com.zfbml.aggregate.player.ExoPlayerEngine
import com.zfbml.aggregate.player.PlayerViewSurface
import com.zfbml.aggregate.source.Episode
import com.zfbml.aggregate.source.MediaDetail
import com.zfbml.aggregate.source.MediaStream
import com.zfbml.aggregate.source.RouteCandidate
import com.zfbml.aggregate.source.SearchResult
import com.zfbml.aggregate.source.StreamProtocol
import com.zfbml.aggregate.source.SourceSearchReport
import com.zfbml.aggregate.source.catalog.BangumiCalendarRepository
import com.zfbml.aggregate.source.catalog.BangumiCategory
import com.zfbml.aggregate.source.catalog.BangumiCategoryResult
import com.zfbml.aggregate.source.catalog.BangumiScheduleDay
import com.zfbml.aggregate.torrent.TorrentEngineState
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AggregateApp(graph: AppGraph, initialQuery: String? = null) {
    var selectedTab by remember { mutableStateOf(AppTab.Discover) }
    var screen by remember { mutableStateOf<AppScreen>(AppScreen.Main) }
    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(900)
        showSplash = false
    }
    Surface(modifier = Modifier.fillMaxSize(), color = AnimeBackground) {
        if (showSplash) {
            BrandSplashScreen()
        } else {
            when (val current = screen) {
                AppScreen.Main -> MainScaffold(
                    graph = graph,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    initialQuery = initialQuery,
                    onOpenDetail = { screen = AppScreen.Detail(it) },
                )
                is AppScreen.Detail -> DetailScreen(
                    graph = graph,
                    result = current.result,
                    onBack = { screen = AppScreen.Main },
                    onPlay = { detail, episode, stream, routes -> screen = AppScreen.Player(detail, episode, stream, routes) },
                )
                is AppScreen.Player -> PlayerScreen(
                    graph = graph,
                    detail = current.detail,
                    episode = current.episode,
                    stream = current.stream,
                    routes = current.routes,
                    onBack = { screen = AppScreen.Detail(SearchResult(current.detail.providerId, current.detail.title, current.detail.url)) },
                )
            }
        }
    }
}

@Composable
private fun BrandSplashScreen() {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val logoScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.82f,
        animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing),
        label = "splashLogoScale",
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 720, delayMillis = 120, easing = FastOutSlowInEasing),
        label = "splashContentAlpha",
    )
    Box(
        modifier = Modifier.fillMaxSize().background(AnimeBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.alpha(contentAlpha),
        ) {
            BrandMark(
                modifier = Modifier
                    .size(106.dp)
                    .scale(logoScale),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "\u8FFD\u756A\u4E0D\u8FF7\u8DEF",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "ZFBML",
                    style = MaterialTheme.typography.titleMedium,
                    color = AnimeAccentCyan,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "\u756A\u5267\u805A\u5408\u3001\u5F39\u5E55\u548C\u591A\u7EBF\u8DEF\u64AD\u653E",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AnimeMuted,
                )
            }
        }
    }
}

private sealed interface AppScreen {
    data object Main : AppScreen
    data class Detail(val result: SearchResult) : AppScreen
    data class Player(
        val detail: MediaDetail,
        val episode: Episode,
        val stream: MediaStream,
        val routes: List<RouteCandidate>,
    ) : AppScreen
}

private enum class AppTab(val label: String, val icon: ImageVector) {
    Discover("\u9996\u9875", Icons.Filled.Home),
    Search("\u641c\u7d22", Icons.Filled.Search),
    Sources("\u9891\u9053", Icons.Filled.Subscriptions),
    Settings("\u6211\u7684", Icons.Filled.AccountCircle),
}

private enum class PlayerPanel {
    Danmaku,
    Quality,
    Speed,
    Route,
    Episode,
}

private val AnimeBackground = Color(0xFF0D0D10)
private val AnimePanel = Color(0xFF18181C)
private val AnimePanelSoft = Color(0xFF22242A)
private val AnimeBorder = Color(0xFF303139)
private val AnimeMuted = Color(0xFFB8BAC4)
private val AnimeAccentPink = Color(0xFFFF5C8A)
private val AnimeAccentCyan = Color(0xFF32D3E6)
private val AnimeAccentAmber = Color(0xFFFFC857)
private val AnimeAccentViolet = Color(0xFF8E7CFF)
private val AnimeAccentGreen = Color(0xFF64D67B)

@Composable
private fun BrandMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(AnimePanel, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .background(Color(0xFF2B1A2A), RoundedCornerShape(8.dp)),
        )
        Row(Modifier.fillMaxSize().padding(8.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(AnimeAccentPink, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(AnimeAccentViolet, RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .align(Alignment.BottomCenter)
                .padding(horizontal = 8.dp)
                .background(Color.Black.copy(alpha = 0.28f), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)),
        )
        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(42.dp))
        Text(
            text = "Z",
            style = MaterialTheme.typography.titleLarge,
            color = AnimeAccentCyan,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 18.dp, bottom = 12.dp),
        )
        Text(
            text = "\u25C6",
            style = MaterialTheme.typography.titleLarge,
            color = AnimeAccentAmber,
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 14.dp, top = 10.dp),
        )
    }
}

@Composable
private fun MainScaffold(
    graph: AppGraph,
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    initialQuery: String?,
    onOpenDetail: (SearchResult) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(AnimeBackground)) {
        if (maxWidth >= 840.dp) {
            Row(modifier = Modifier.fillMaxSize()) {
                AppNavigationRail(selectedTab = selectedTab, onTabSelected = onTabSelected)
                MainTabContent(
                    graph = graph,
                    selectedTab = selectedTab,
                    initialQuery = initialQuery,
                    onOpenDetail = onOpenDetail,
                    onTabSelected = onTabSelected,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                MainTabContent(
                    graph = graph,
                    selectedTab = selectedTab,
                    initialQuery = initialQuery,
                    onOpenDetail = onOpenDetail,
                    onTabSelected = onTabSelected,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
                AppNavigationBar(selectedTab = selectedTab, onTabSelected = onTabSelected)
            }
        }
    }
}

@Composable
private fun MainTabContent(
    graph: AppGraph,
    selectedTab: AppTab,
    initialQuery: String?,
    onOpenDetail: (SearchResult) -> Unit,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.background(AnimeBackground)) {
        when (selectedTab) {
            AppTab.Discover -> DiscoverScreen(
                graph = graph,
                onOpenDetail = onOpenDetail,
                onSearch = { onTabSelected(AppTab.Search) },
            )
            AppTab.Search -> SearchScreen(
                graph = graph,
                initialQuery = initialQuery,
                onOpenDetail = onOpenDetail,
            )
            AppTab.Sources -> SourcesScreen(graph = graph)
            AppTab.Settings -> SettingsScreen(graph = graph)
        }
    }
}

@Composable
private fun AppNavigationBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
) {
    NavigationBar(containerColor = AnimePanel) {
        AppTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
            )
        }
    }
}

@Composable
private fun AppNavigationRail(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight(),
        containerColor = AnimePanel,
        header = {
            Text(
                text = "Z",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AnimeAccentPink,
                modifier = Modifier.padding(vertical = 18.dp),
            )
        },
    ) {
        AppTab.entries.forEach { tab ->
            NavigationRailItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DiscoverScreen(
    graph: AppGraph,
    onOpenDetail: (SearchResult) -> Unit,
    onSearch: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val categories = remember { graph.bangumiCategoryRepository.categories }
    val pages = remember(categories) { homePages(categories) }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val fallbackFeatured = remember { featuredOnlineResults() }
    var homePicks by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var homePicksLoading by remember { mutableStateOf(false) }
    var guessBatch by remember { mutableStateOf(0) }
    var showCalendar by remember { mutableStateOf(false) }
    var categoryResults by remember { mutableStateOf<Map<String, BangumiCategoryResult>>(emptyMap()) }
    var categoryLoadingIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var categoryErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var schedule by remember { mutableStateOf<List<BangumiScheduleDay>>(emptyList()) }
    var selectedDayId by remember { mutableStateOf(BangumiCalendarRepository.currentBangumiWeekdayId()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        runCatching { graph.bangumiCalendarRepository.loadWeeklySchedule() }
            .onSuccess { days ->
                schedule = days
                val currentDay = days.firstOrNull { it.weekdayId == selectedDayId && it.items.isNotEmpty() }
                if (currentDay == null) {
                    days.firstOrNull { it.items.isNotEmpty() }?.let { selectedDayId = it.weekdayId }
                }
            }
            .onFailure { failure ->
                error = failure.message ?: failure::class.simpleName.orEmpty().ifBlank { "\u672a\u77e5\u9519\u8bef" }
            }
        loading = false
    }

    LaunchedEffect(Unit) {
        homePicksLoading = true
        homePicks = runCatching { graph.bangumiCategoryRepository.loadHomeRecommendations() }
            .getOrDefault(emptyList())
        homePicksLoading = false
    }

    val currentPage = pages.getOrNull(pagerState.currentPage)
    LaunchedEffect(currentPage) {
        val categoryId = (currentPage as? HomePage.Category)?.category?.id ?: return@LaunchedEffect
        if (categoryResults.containsKey(categoryId) || categoryId in categoryLoadingIds) return@LaunchedEffect
        categoryLoadingIds = categoryLoadingIds + categoryId
        categoryErrors = categoryErrors - categoryId
        runCatching { graph.bangumiCategoryRepository.loadCategory(categoryId) }
            .onSuccess { result -> categoryResults = categoryResults + (categoryId to result) }
            .onFailure { failure ->
                categoryErrors = categoryErrors + (
                    categoryId to (failure.message ?: failure::class.simpleName.orEmpty().ifBlank { "\u65e0\u8be6\u7ec6\u9519\u8bef" })
                )
            }
        categoryLoadingIds = categoryLoadingIds - categoryId
    }

    val featured = homePicks.ifEmpty { fallbackFeatured }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AnimeBackground),
    ) {
        HomePinnedHeader(
            onSearch = onSearch,
            calendarExpanded = showCalendar,
            onCalendar = {
                showCalendar = !showCalendar
                scope.launch { pagerState.animateScrollToPage(0) }
            },
        )
        HomeCategoryBar(
            pages = pages,
            selectedIndex = pagerState.currentPage,
            onSelected = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { pageIndex ->
            when (val page = pages[pageIndex]) {
                HomePage.Home -> HomeFeedPage(
                    featured = featured,
                    homeLoading = homePicksLoading,
                    schedule = schedule,
                    selectedDayId = selectedDayId,
                    onDaySelected = { selectedDayId = it },
                    scheduleLoading = loading,
                    scheduleError = error,
                    calendarExpanded = showCalendar,
                    guessBatch = guessBatch,
                    onShuffleGuess = { guessBatch += 1 },
                    onOpenDetail = onOpenDetail,
                )
                is HomePage.Category -> CategoryFeedPage(
                    category = page.category,
                    result = categoryResults[page.category.id],
                    loading = page.category.id in categoryLoadingIds,
                    error = categoryErrors[page.category.id],
                    fallback = featured,
                    onOpenDetail = onOpenDetail,
                )
            }
        }
    }
}

private sealed interface HomePage {
    data object Home : HomePage
    data class Category(val category: BangumiCategory) : HomePage
}

private fun homePages(categories: List<BangumiCategory>): List<HomePage> {
    val byId = categories.associateBy { it.id }
    val orderedCategoryIds = listOf(
        "chinese",
        "japanese",
        "american",
        "movie",
        "hot",
        "recommend",
        "high-score",
        "most-followed",
        "most-watched",
    )
    return listOf(HomePage.Home) + orderedCategoryIds.mapNotNull { id ->
        byId[id]?.let { HomePage.Category(it) }
    }
}

private fun HomePage.title(): String {
    return when (this) {
        HomePage.Home -> "\u9996\u9875"
        is HomePage.Category -> category.title
    }
}

@Composable
private fun HomePinnedHeader(
    onSearch: () -> Unit,
    calendarExpanded: Boolean,
    onCalendar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AnimeBackground)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrandMark(Modifier.size(42.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = "\u8ffd\u756a\u4e0d\u8ff7\u8def",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "ZFBML",
                    style = MaterialTheme.typography.labelLarge,
                    color = AnimeAccentCyan,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
            Button(
                onClick = onCalendar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (calendarExpanded) AnimeAccentCyan else AnimePanelSoft,
                    contentColor = if (calendarExpanded) AnimeBackground else Color.White,
                ),
                modifier = Modifier.height(42.dp).focusable(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
            ) {
                Icon(Icons.Filled.Bookmarks, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(5.dp))
                Text("\u65e5\u5386", maxLines = 1)
            }
        }
        Card(
            onClick = onSearch,
            modifier = Modifier.fillMaxWidth().height(48.dp).focusable(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = AnimePanel),
            border = BorderStroke(1.dp, AnimeBorder),
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = AnimeAccentCyan, modifier = Modifier.size(22.dp))
                Text(
                    text = "\u641c\u756a\u540d\u3001\u7c98\u8d34\u94fe\u63a5\u6216\u627e\u64ad\u653e\u7ebf\u8def",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AnimeMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text("\u641c\u7d22", style = MaterialTheme.typography.labelLarge, color = AnimeAccentCyan)
            }
        }
    }
}

@Composable
private fun HomeCategoryBar(
    pages: List<HomePage>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(AnimeBackground)
            .padding(start = 18.dp, end = 18.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(pages.size) { index ->
            val selected = selectedIndex == index
            Card(
                onClick = { onSelected(index) },
                modifier = Modifier.height(42.dp).focusable(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = if (selected) AnimePanelSoft else Color.Transparent),
                border = BorderStroke(1.dp, if (selected) AnimeAccentCyan else AnimeBorder),
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp).fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = pages[index].title(),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) AnimeAccentCyan else AnimeMuted,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeFeedPage(
    featured: List<SearchResult>,
    homeLoading: Boolean,
    schedule: List<BangumiScheduleDay>,
    selectedDayId: Int,
    onDaySelected: (Int) -> Unit,
    scheduleLoading: Boolean,
    scheduleError: String?,
    calendarExpanded: Boolean,
    guessBatch: Int,
    onShuffleGuess: () -> Unit,
    onOpenDetail: (SearchResult) -> Unit,
) {
    val selectedDay = schedule.firstOrNull { it.weekdayId == selectedDayId }
    val selectedItems = selectedDay?.items.orEmpty()
    val feedSelection = splitSpotlightFeed(featured, spotlightCount = 5)
    val remainder = feedSelection.remainder.ifEmpty { featured.distinctBy { it.stableMediaKey() } }
    val continueItem = selectedItems.firstOrNull()
        ?: remainder.firstOrNull()
        ?: feedSelection.spotlight.firstOrNull()
    val guessItems = remainder.rotatingWindow(start = guessBatch * 5, count = 6)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            HomeHeroCarousel(
                title = "\u7cbe\u9009\u9996\u63a8",
                items = feedSelection.spotlight,
                onOpenDetail = onOpenDetail,
            )
        }
        if (homeLoading && featured.isEmpty()) {
            item {
                ScheduleStatusPanel(
                    title = "\u6b63\u5728\u52a0\u8f7d\u9996\u9875\u63a8\u8350",
                    subtitle = "Bangumi \u63a8\u8350\u3001\u9ad8\u5206\u548c\u70ed\u95e8\u699c\u5355\u540c\u6b65\u4e2d\u3002",
                )
            }
        }
        if (calendarExpanded) {
            item {
                ScheduleDaySelector(
                    days = schedule,
                    selectedDayId = selectedDayId,
                    onSelected = onDaySelected,
                )
            }
            if (scheduleLoading) {
                item {
                    ScheduleStatusPanel(
                        title = "\u6b63\u5728\u52a0\u8f7d\u65b0\u756a\u65f6\u95f4\u8868",
                        subtitle = "Bangumi \u6bcf\u65e5\u653e\u9001\u6570\u636e\u540c\u6b65\u4e2d\u3002",
                    )
                }
            }
            scheduleError?.let { message ->
                item {
                    ScheduleStatusPanel(
                        title = "\u65b0\u756a\u65f6\u95f4\u8868\u52a0\u8f7d\u5931\u8d25",
                        subtitle = message,
                    )
                }
            }
            item {
                SectionHeader(
                    title = selectedDay?.weekdayCn ?: "\u8ffd\u756a\u65e5\u5386",
                    action = if (selectedItems.isNotEmpty()) "${selectedItems.size} \u90e8" else "",
                    onAction = {},
                )
            }
            if (selectedItems.isEmpty() && !scheduleLoading) {
                item {
                    ScheduleStatusPanel(
                        title = "\u6682\u65e0\u5f53\u65e5\u653e\u9001\u6570\u636e",
                        subtitle = "\u53ef\u4ee5\u5207\u6362\u5176\u4ed6\u65e5\u671f\uff0c\u6216\u76f4\u63a5\u641c\u7d22\u756a\u540d\u3002",
                    )
                }
            } else {
                items(selectedItems.take(6)) { result ->
                    ScheduleAnimeRow(result = result, onClick = { onOpenDetail(result) })
                }
            }
        }
        continueItem?.let { result ->
            item {
                SectionHeader(title = "\u6b63\u5728\u64ad\u653e", action = "\u8ffd\u756a", onAction = {})
            }
            item {
                ContinueWatchingRow(result = result, onClick = { onOpenDetail(result) })
            }
        }
        item {
            SectionHeader(title = "\u731c\u4f60\u60f3\u8ffd", action = "\u6362\u4e00\u6279", onAction = onShuffleGuess)
        }
        item {
            PosterRail(items = guessItems, onOpenDetail = onOpenDetail)
        }
        if (selectedItems.isNotEmpty()) {
            item {
                SectionHeader(title = "\u4eca\u65e5\u70ed\u64ad", action = "\u65e5\u5386", onAction = {})
            }
            items(selectedItems.take(5)) { result ->
                ScheduleAnimeRow(result = result, onClick = { onOpenDetail(result) })
            }
        }
    }
}

@Composable
private fun CategoryFeedPage(
    category: BangumiCategory,
    result: BangumiCategoryResult?,
    loading: Boolean,
    error: String?,
    fallback: List<SearchResult>,
    onOpenDetail: (SearchResult) -> Unit,
) {
    val items = result?.items.orEmpty()
    val feedSelection = splitSpotlightFeed(items, fallback = fallback, spotlightCount = 5)
    val heroItems = feedSelection.spotlight
    val listItems = if (items.isEmpty()) emptyList() else feedSelection.remainder
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            HomeHeroCarousel(
                title = category.title,
                items = heroItems,
                onOpenDetail = onOpenDetail,
            )
        }
        item {
            CategoryInsightStrip(category = category, items = items)
        }
        if (loading) {
            item {
                ScheduleStatusPanel(
                    title = "\u6b63\u5728\u52a0\u8f7d${category.title}",
                    subtitle = "Bangumi \u5206\u7c7b\u699c\u5355\u540c\u6b65\u4e2d\u3002",
                )
            }
        }
        error?.let { message ->
            item {
                ScheduleStatusPanel(
                    title = "${category.title}\u52a0\u8f7d\u5931\u8d25",
                    subtitle = message,
                )
            }
        }
        item {
            SectionHeader(
                title = "\u7cbe\u9009\u70ed\u64ad\u65b0\u756a",
                action = if (listItems.isNotEmpty()) "\u5168\u90e8 ${listItems.size}" else "",
                onAction = {},
            )
        }
        if (listItems.isEmpty() && !loading) {
            item {
                ScheduleStatusPanel(
                    title = "\u6682\u65e0\u53ef\u5c55\u793a\u6761\u76ee",
                    subtitle = "\u53ef\u4ee5\u5207\u5230\u5176\u4ed6\u5206\u7c7b\uff0c\u6216\u76f4\u63a5\u641c\u7d22\u756a\u540d\u3002",
                )
            }
        } else {
            items(listItems) { item ->
                ScheduleAnimeRow(result = item, onClick = { onOpenDetail(item) })
            }
        }
    }
}

@Composable
private fun HomeHeroCarousel(
    title: String,
    items: List<SearchResult>,
    onOpenDetail: (SearchResult) -> Unit,
) {
    val visible = items.ifEmpty { featuredOnlineResults() }.distinctBy { it.stableMediaKey() }.take(8)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text("\u6ed1\u52a8\u6311\u4e00\u90e8\u5f00\u59cb", style = MaterialTheme.typography.bodySmall, color = AnimeMuted)
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cardWidth = (maxWidth * 0.94f).coerceAtLeast(320.dp).coerceAtMost(560.dp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(visible.size) { index ->
                    val result = visible[index]
                    val companion = if (visible.size > 1) visible[(index + 1) % visible.size] else null
                    HeroCarouselCard(
                        result = result,
                        index = index,
                        companion = companion,
                        onClick = { onOpenDetail(result) },
                        modifier = Modifier.width(cardWidth).height(226.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCarouselCard(
    result: SearchResult,
    index: Int,
    companion: SearchResult?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = providerAccent(result.providerId)
    val subtitle = result.subtitle?.takeIf { it.isNotBlank() } ?: providerDisplayName(result.providerId)
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = AnimePanel),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().background(AnimePanelSoft)) {
            val showCompanion = companion != null && maxWidth >= 380.dp
            val posterWidth = if (showCompanion) 126.dp else 118.dp
            val titleStyle = if (showCompanion) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(accent)
                    .align(Alignment.TopStart),
            )
            Row(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(modifier = Modifier.width(posterWidth).fillMaxHeight()) {
                    PosterArtwork(
                        posterUrl = result.posterUrl,
                        accent = accent,
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(8.dp),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.58f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "#%02d \u7126\u70b9".format(index + 1),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            VideoMetaChip(result.raw["categoryTitle"] ?: "\u4eca\u65e5\u9996\u63a8")
                            VideoMetaChip(result.raw["rating"]?.let { "\u8bc4\u5206 $it" } ?: providerDisplayName(result.providerId))
                        }
                        Text(
                            result.title,
                            style = titleStyle,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AnimeMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = AnimeAccentCyan, modifier = Modifier.size(22.dp))
                        Text("\u8fdb\u5165\u8be6\u60c5", style = MaterialTheme.typography.labelLarge, color = AnimeAccentCyan)
                    }
                }
                companion?.takeIf { showCompanion }?.let { next ->
                    Column(
                        modifier = Modifier.width(68.dp).fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("NEXT", style = MaterialTheme.typography.labelSmall, color = AnimeMuted, fontWeight = FontWeight.Bold)
                        PosterArtwork(
                            posterUrl = next.posterUrl,
                            accent = providerAccent(next.providerId),
                            modifier = Modifier.size(width = 58.dp, height = 82.dp),
                            shape = RoundedCornerShape(7.dp),
                        )
                        Text(
                            next.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PosterRail(items: List<SearchResult>, onOpenDetail: (SearchResult) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items.ifEmpty { featuredOnlineResults() }.take(8)) { item ->
            PosterVideoCard(result = item, onClick = { onOpenDetail(item) })
        }
    }
}

@Composable
private fun CategoryInsightStrip(category: BangumiCategory, items: List<SearchResult>) {
    val topRating = items.mapNotNull { it.raw["rating"]?.toDoubleOrNull() }.maxOrNull()
    val watching = items.mapNotNull { it.raw["doing"]?.toIntOrNull() }.maxOrNull()
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        InsightTile(category.badge, category.subtitle, categoryAccent(category.id), Modifier.weight(1f))
        InsightTile(topRating?.let { "%.1f".format(it) } ?: "--", "\u6700\u9ad8\u8bc4\u5206", AnimeAccentGreen, Modifier.weight(1f))
        InsightTile(watching?.toString() ?: "--", "\u5728\u770b\u70ed\u5ea6", AnimeAccentCyan, Modifier.weight(1f))
    }
}

@Composable
private fun InsightTile(label: String, title: String, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(82.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(title, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun <T> List<T>.rotatingWindow(start: Int, count: Int): List<T> {
    if (isEmpty()) return emptyList()
    val safeStart = ((start % size) + size) % size
    val safeCount = count.coerceAtMost(size)
    return (0 until safeCount).map { index -> this[(safeStart + index) % size] }
}

private data class FeedSelection(
    val spotlight: List<SearchResult>,
    val remainder: List<SearchResult>,
)

private fun splitSpotlightFeed(
    items: List<SearchResult>,
    fallback: List<SearchResult> = emptyList(),
    spotlightCount: Int = 5,
): FeedSelection {
    val uniqueItems = items.distinctBy { it.stableMediaKey() }
    val source = uniqueItems.ifEmpty { fallback.distinctBy { it.stableMediaKey() } }
    if (source.isEmpty()) return FeedSelection(spotlight = emptyList(), remainder = emptyList())

    val spotlight = source
        .sortedWith(
            compareByDescending<SearchResult> { it.feedScore() }
                .thenBy { it.title },
        )
        .take(spotlightCount.coerceAtLeast(1))
    val spotlightKeys = spotlight.map { it.stableMediaKey() }.toSet()
    return FeedSelection(
        spotlight = spotlight,
        remainder = uniqueItems.filterNot { it.stableMediaKey() in spotlightKeys },
    )
}

private fun SearchResult.stableMediaKey(): String {
    return raw["subjectId"] ?: raw["id"] ?: url.ifBlank { title }
}

private fun SearchResult.feedScore(): Double {
    val rating = raw["rating"]?.toDoubleOrNull() ?: 0.0
    val doing = raw["doing"]?.toDoubleOrNull() ?: 0.0
    val collect = raw["collect"]?.toDoubleOrNull() ?: 0.0
    return rating * 10_000.0 + doing * 3.0 + collect
}

@Composable
private fun HomeTopBar(onSearch: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BrandMark(Modifier.size(46.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "\u8FFD\u756A\u4E0D\u8FF7\u8DEF",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = "ZFBML",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = AnimeAccentCyan,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            Text(
                text = "\u756A\u5267\u3001\u7535\u5F71\u3001\u5F39\u5E55\u4E0E\u591A\u7EBF\u8DEF\u64AD\u653E",
                style = MaterialTheme.typography.bodyMedium,
                color = AnimeMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(
            onClick = onSearch,
            modifier = Modifier
                .size(44.dp)
                .background(AnimePanelSoft, RoundedCornerShape(8.dp))
                .focusable(),
        ) {
            Icon(Icons.Filled.Search, contentDescription = "\u641C\u7D22", tint = Color.White)
        }
    }
}

@Composable
private fun HomeSearchEntry(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(42.dp).background(AnimePanelSoft, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = AnimeAccentCyan)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("\u641c\u7d22\u756a\u540d\u6216\u7c98\u8d34\u64ad\u653e\u94fe\u63a5", style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Bangumi \u8d44\u6599\u5e93 + \u591a\u6e90\u7ebf\u8def\u5339\u914d", style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("\u53bb\u641c", style = MaterialTheme.typography.labelLarge, color = AnimeAccentCyan)
        }
    }
}

@Composable
private fun FeaturedBanner(result: SearchResult, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(190.dp).focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF241824)),
    ) {
        Box(Modifier.fillMaxSize()) {
            PosterArtwork(
                posterUrl = result.posterUrl,
                accent = AnimeAccentPink.copy(alpha = 0.32f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(width = 180.dp, height = 190.dp),
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
            )
            Column(
                modifier = Modifier.fillMaxSize().padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("\u672C\u5468\u63A8\u8350", style = MaterialTheme.typography.labelLarge, color = AnimeAccentAmber)
                    Text(result.title, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2)
                    Text(
                        "\u9AD8\u6E05\u64AD\u653E\u3001\u5F39\u5E55\u3001TV \u4F53\u9A8C",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AnimeMuted,
                        maxLines = 2,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AnimeAccentPink, contentColor = Color.White),
                        modifier = Modifier.focusable(),
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("\u7ACB\u5373\u64AD\u653E")
                    }
                    VideoMetaChip("\u9AD8\u6E05")
                    VideoMetaChip("\u5F39\u5E55")
                }
            }
        }
    }
}

@Composable
private fun ScheduleHeroBanner(result: SearchResult, dayLabel: String, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(210.dp).focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1D2028)),
    ) {
        Box(Modifier.fillMaxSize()) {
            PosterArtwork(
                posterUrl = result.posterUrl,
                accent = AnimeAccentViolet.copy(alpha = 0.34f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(width = 188.dp, height = 210.dp),
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
            )
            Column(
                modifier = Modifier.fillMaxSize().padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(dayLabel, style = MaterialTheme.typography.labelLarge, color = AnimeAccentAmber)
                    Text(result.title, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2)
                    Text(
                        result.subtitle.orEmpty().ifBlank { "\u65b0\u756a\u65f6\u95f4\u8868 / \u756a\u5267\u8be6\u60c5 / \u591a\u7ebf\u8def\u64ad\u653e" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = AnimeMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AnimeAccentPink, contentColor = Color.White),
                        modifier = Modifier.focusable(),
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("\u8fdb\u5165\u8be6\u60c5")
                    }
                    VideoMetaChip("\u4eca\u65e5\u653e\u9001")
                    VideoMetaChip("\u53ef\u9009\u7ebf\u8def")
                }
            }
        }
    }
}

@Composable
private fun ScheduleDaySelector(
    days: List<BangumiScheduleDay>,
    selectedDayId: Int,
    onSelected: (Int) -> Unit,
) {
    val visibleDays = days.ifEmpty { fallbackScheduleDays() }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(visibleDays) { day ->
            val selected = day.weekdayId == selectedDayId
            Card(
                onClick = { onSelected(day.weekdayId) },
                modifier = Modifier.width(76.dp).height(50.dp).focusable(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = if (selected) AnimePanelSoft else AnimePanel),
                border = BorderStroke(1.dp, if (selected) AnimeAccentCyan else AnimeBorder),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(day.weekdayCn.removePrefix("\u661f\u671f"), style = MaterialTheme.typography.labelLarge, color = Color.White, maxLines = 1)
                    Text("${day.items.size}", style = MaterialTheme.typography.bodySmall, color = if (selected) AnimeAccentCyan else AnimeMuted, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun ScheduleStatusPanel(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
        }
    }
}

@Composable
private fun ScheduleAnimeRow(result: SearchResult, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PosterArtwork(
                posterUrl = result.posterUrl,
                accent = AnimeAccentViolet,
                modifier = Modifier.size(width = 72.dp, height = 96.dp),
                shape = RoundedCornerShape(6.dp),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(result.title, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(result.subtitle.orEmpty(), style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    result.raw["rating"]?.let { VideoMetaChip(it) }
                    result.raw["doing"]?.let { VideoMetaChip("$it \u5728\u770b") }
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = AnimeAccentCyan, modifier = Modifier.size(26.dp))
                Text("\u8be6\u60c5", style = MaterialTheme.typography.labelLarge, color = AnimeAccentCyan)
            }
        }
    }
}

private fun fallbackScheduleDays(): List<BangumiScheduleDay> {
    val names = listOf(
        1 to "\u661f\u671f\u4e00",
        2 to "\u661f\u671f\u4e8c",
        3 to "\u661f\u671f\u4e09",
        4 to "\u661f\u671f\u56db",
        5 to "\u661f\u671f\u4e94",
        6 to "\u661f\u671f\u516d",
        7 to "\u661f\u671f\u65e5",
    )
    return names.map { (id, name) ->
        BangumiScheduleDay(
            weekdayId = id,
            weekdayCn = name,
            weekdayEn = "",
            items = emptyList(),
        )
    }
}

@Composable
private fun ContinueWatchingRow(result: SearchResult, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PosterArtwork(
                posterUrl = result.posterUrl,
                accent = AnimeAccentCyan,
                modifier = Modifier.size(width = 112.dp, height = 68.dp),
                shape = RoundedCornerShape(6.dp),
                icon = Icons.Filled.PlayArrow,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("\u7EE7\u7EED\u89C2\u770B", style = MaterialTheme.typography.labelLarge, color = AnimeAccentCyan)
                Text(result.title, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                LinearProgressIndicator(progress = { 0.36f }, modifier = Modifier.fillMaxWidth(), color = AnimeAccentPink)
                Text("\u5DF2\u770B\u81F3 36%", style = MaterialTheme.typography.bodySmall, color = AnimeMuted)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        if (action.isNotBlank()) {
            TextButton(onClick = onAction, modifier = Modifier.focusable()) {
                Text(action, color = AnimeAccentCyan)
            }
        }
    }
}

@Composable
private fun VideoMetaChip(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = Color.White,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        softWrap = false,
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp),
    )
}

@Composable
private fun PosterArtwork(
    posterUrl: String?,
    accent: Color,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    icon: ImageVector = Icons.Filled.Movie,
    contentScale: ContentScale = ContentScale.Crop,
) {
    var bitmap by remember(posterUrl) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(posterUrl) {
        bitmap = null
        if (!posterUrl.isNullOrBlank()) {
            bitmap = loadRemotePoster(posterUrl)
        }
    }
    Box(
        modifier = modifier.clip(shape).background(accent),
        contentAlignment = Alignment.Center,
    ) {
        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
            )
        } else {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(42.dp))
        }
    }
}

private suspend fun loadRemotePoster(url: String): ImageBitmap? = withContext(Dispatchers.IO) {
    runCatching {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8_000
                readTimeout = 12_000
                setRequestProperty("User-Agent", "ZFBML/0.2.21")
            }
            connection.inputStream.use { input ->
                BitmapFactory.decodeStream(input)?.asImageBitmap()
            }
        } finally {
            connection?.disconnect()
        }
    }.getOrNull()
}

@Composable
private fun PosterVideoCard(result: SearchResult, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(132.dp).focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PosterArtwork(
                posterUrl = result.posterUrl,
                accent = providerAccent(result.providerId),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp),
            )
            Text(result.title, style = MaterialTheme.typography.titleSmall, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(result.subtitle.orEmpty(), style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun WideVideoCard(result: SearchResult, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PosterArtwork(
                posterUrl = result.posterUrl,
                accent = providerAccent(result.providerId),
                modifier = Modifier.size(width = 128.dp, height = 72.dp),
                shape = RoundedCornerShape(6.dp),
                icon = Icons.Filled.PlayArrow,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(result.title, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(result.subtitle.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VideoMetaChip("\u53EF\u64AD\u653E")
                    VideoMetaChip("\u53EF\u6295\u5C4F")
                }
            }
        }
    }
}

private fun featuredOnlineResults(): List<SearchResult> = listOf(
    SearchResult(
        providerId = "bangumi-catalog",
        title = "\u846c\u9001\u7684\u8299\u8389\u83b2",
        url = "bangumi://subject/400602",
        posterUrl = "https://api.bgm.tv/v0/subjects/400602/image?type=large",
        subtitle = "Bangumi \u8d44\u6599\u5e93 / 2023 / TV",
        raw = mapOf("subjectId" to "400602", "subjectNameCn" to "\u846c\u9001\u7684\u8299\u8389\u83b2", "episodeCount" to "28"),
    ),
    SearchResult(
        providerId = "bangumi-catalog",
        title = "\u5929\u56fd\u5927\u9b54\u5883",
        url = "bangumi://subject/404804",
        posterUrl = "https://api.bgm.tv/v0/subjects/404804/image?type=large",
        subtitle = "Bangumi \u8d44\u6599\u5e93 / 2023 / TV",
        raw = mapOf("subjectId" to "404804", "subjectNameCn" to "\u5929\u56fd\u5927\u9b54\u5883", "episodeCount" to "13"),
    ),
    SearchResult(
        providerId = "bangumi-catalog",
        title = "\u524d\u8f88\u662f\u7537\u5b69\u5b50",
        url = "bangumi://subject/425988",
        posterUrl = "https://api.bgm.tv/v0/subjects/425988/image?type=large",
        subtitle = "Bangumi \u8d44\u6599\u5e93 / 2024 / TV",
        raw = mapOf("subjectId" to "425988", "subjectNameCn" to "\u524d\u8f88\u662f\u7537\u5b69\u5b50", "episodeCount" to "12"),
    ),
)

@Composable
private fun SearchScreen(
    graph: AppGraph,
    initialQuery: String?,
    onOpenDetail: (SearchResult) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var query by remember(initialQuery) { mutableStateOf(initialQuery?.takeIf(String::isNotBlank).orEmpty()) }
    var results by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    var searchMessage by remember { mutableStateOf<String?>(null) }

    fun runSearch() {
        if (query.trim().isBlank()) return
        scope.launch {
            searched = true
            loading = true
            searchMessage = null
            runCatching { graph.sourceRegistry.searchAllWithReport(query.trim()) }
                .onSuccess { report ->
                    results = report.results
                    searchMessage = report.statusMessage()
                }
                .onFailure { error ->
                    results = emptyList()
                    searchMessage = "\u641C\u7D22\u5931\u8D25: ${error.message ?: error::class.simpleName.orEmpty().ifBlank { "\u65E0\u8BE6\u7EC6\u9519\u8BEF" }}"
                }
            loading = false
        }
    }

    LaunchedEffect(initialQuery) {
        if (!searched && !initialQuery.isNullOrBlank()) {
            runSearch()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AnimeBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "\u641c\u7d22",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = "\u5206\u7c7b\u548c\u63a8\u8350\u5df2\u653e\u5728\u9996\u9875\uff0c\u8fd9\u91cc\u4e13\u95e8\u7528\u6765\u7cbe\u51c6\u627e\u7247\u3002",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AnimeMuted,
                )
            }
        }
        item {
            SearchControls(
                query = query,
                onQueryChange = { query = it },
                onSearch = ::runSearch,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            ResultsHeader()
        }
        searchStatusItems(
            loading = loading,
            searched = searched,
            searchMessage = searchMessage,
            results = results,
            onOpenDetail = onOpenDetail,
        )
    }
}

@Composable
private fun TrackingHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "\u8ffd\u756a\u9891\u9053",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Text(
            text = "Bangumi \u5206\u7c7b\u699c\u5355 + \u591a\u6e90\u7ebf\u8def\u5339\u914d\uff0c\u5148\u627e\u756a\uff0c\u518d\u9009\u96c6\u64ad\u653e\u3002",
            style = MaterialTheme.typography.bodyMedium,
            color = AnimeMuted,
        )
    }
}

@Composable
private fun CategoryButtonGrid(
    categories: List<BangumiCategory>,
    selectedCategoryId: String,
    onSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { category ->
                    CategoryButton(
                        category = category,
                        selected = category.id == selectedCategoryId,
                        onClick = { onSelected(category.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryButton(
    category: BangumiCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(82.dp).focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) AnimePanelSoft else AnimePanel),
        border = BorderStroke(1.dp, if (selected) AnimeAccentCyan else AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(42.dp).background(categoryAccent(category.id), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = category.badge,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(category.title, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(category.subtitle, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

private fun categoryAccent(categoryId: String): Color {
    return when (categoryId) {
        "recommend" -> AnimeAccentPink
        "japanese" -> AnimeAccentCyan
        "chinese" -> AnimeAccentAmber
        "american" -> AnimeAccentViolet
        "movie" -> Color(0xFFEC6F66)
        "hot" -> Color(0xFFFF8A3D)
        "high-score" -> AnimeAccentGreen
        "most-followed" -> Color(0xFF5FA8FF)
        "most-watched" -> Color(0xFFB178FF)
        else -> AnimeAccentCyan
    }
}

@Composable
private fun SourcesScreen(graph: AppGraph) {
    val providers = graph.sourceRegistry.manifests
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("\u9891\u9053", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text("\u7BA1\u7406\u7247\u6E90\u3001\u6E05\u6670\u5EA6\u3001\u5F39\u5E55\u5339\u914D\u548C\u64AD\u653E\u7B56\u7565", style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
        }
        items(providers) { manifest ->
            SourceCard(
                name = manifest.name,
                version = manifest.version,
                author = manifest.author,
                domains = manifest.domains.joinToString().ifBlank { "\u672C\u5730" },
                capabilities = manifest.capabilities.joinToString { it.name.lowercase() },
                accent = providerAccent(manifest.id),
            )
        }
    }
}

@Composable
private fun SourceCard(
    name: String,
    version: String,
    author: String,
    domains: String,
    capabilities: String,
    accent: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth().focusable(),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(52.dp).background(accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(name, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(version, style = MaterialTheme.typography.labelMedium, color = AnimeMuted)
                }
                Text(author, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1)
                Text(domains, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(capabilities, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun CacheScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("\u79BB\u7EBF\u7247\u5E93", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text("\u7EE7\u7EED\u770B\u3001\u9884\u7F13\u51B2\u3001\u4E0B\u8F7D\u540E\u79BB\u7EBF\u64AD\u653E", style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
        }
        item {
            StatusPanel(
                title = "\u5728\u7EBF\u89C6\u9891\u7F13\u5B58",
                subtitle = "HLS / DASH / MP4 \u53EF\u7528\u65F6\u81EA\u52A8\u8FDB\u5165\u79BB\u7EBF\u961F\u5217",
                value = "\u53EF\u64AD",
                accent = AnimeAccentCyan,
            )
        }
        item {
            StatusPanel(
                title = "\u8FB9\u7F13\u51B2\u8FB9\u64AD",
                subtitle = "\u4E3A\u957F\u89C6\u9891\u9884\u7559\u7684\u540E\u53F0\u7F13\u51B2\u80FD\u529B",
                value = "\u8C03\u8BD5",
                accent = AnimeAccentPink,
            )
        }
        item {
            StatusPanel(
                title = "\u6279\u91CF\u79BB\u7EBF",
                subtitle = "\u5267\u96C6\u3001\u5B57\u5E55\u548C\u591A\u6E05\u6670\u5EA6\u4EFB\u52A1\u7BA1\u7406",
                value = "\u5F85\u63A5",
                accent = AnimeAccentAmber,
            )
        }
    }
}

@Composable
private fun SettingsScreen(graph: AppGraph) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("\u8BBE\u7F6E", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text("\u7248\u672C 0.2.11", style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
        }
        item {
            StatusPanel(
                title = "\u64AD\u653E\u5185\u6838",
                subtitle = "Media3 ExoPlayer",
                value = "\u9ED8\u8BA4",
                accent = AnimeAccentCyan,
            )
        }
        item {
            StatusPanel(
                title = "\u5F39\u5E55",
                subtitle = "\u54D4\u54E9\u54D4\u54E9 / \u817E\u8BAF / \u7231\u5947\u827A / \u4F18\u9177",
                value = "${graph.danmakuRegistry.profiles.size} \u6E90",
                accent = AnimeAccentViolet,
            )
        }
        item {
            StatusPanel(
                title = "\u6570\u636E\u6E90",
                subtitle = "\u5185\u7F6E Provider + RSS + JSON/XPath",
                value = "${graph.sourceRegistry.manifests.size} \u4E2A",
                accent = AnimeAccentPink,
            )
        }
    }
}

@Composable
private fun StatusPanel(
    title: String,
    subtitle: String,
    value: String,
    accent: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth().focusable(),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(12.dp).background(accent))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(value, style = MaterialTheme.typography.labelLarge, color = accent)
        }
    }
}

@Composable
private fun SearchControls(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "\u76f4\u63a5\u641c\u7d22",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Text(
            text = "\u8f93\u5165\u756a\u540d\u3001\u5267\u540d\u6216\u89c6\u9891\u94fe\u63a5",
            style = MaterialTheme.typography.bodyMedium,
            color = AnimeMuted,
        )
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("\u641C\u7D22\u756A\u540D\u3001\u5267\u540D\u6216\u7C98\u8D34\u64AD\u653E\u94FE\u63A5") },
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .focusable(),
            colors = ButtonDefaults.buttonColors(containerColor = AnimeAccentPink, contentColor = Color.White),
            onClick = onSearch,
        ) {
            Text("\u641c\u7d22\u756a\u5267")
        }
    }
}

@Composable
private fun SearchHintPanel() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("\u641c\u7d22\u540e\u5148\u8fdb\u5165\u756a\u5267\u8be6\u60c5", style = MaterialTheme.typography.titleSmall, color = Color.White)
            Text("\u5728\u8be6\u60c5\u9875\u9009\u96c6\uff0c\u5e94\u7528\u4f18\u5148\u5339\u914d\u5728\u7ebf\u89c6\u9891\u7ebf\u8def\uff0cBT \u4f5c\u4e3a\u5907\u7528\u8865\u5145\u3002", style = MaterialTheme.typography.bodySmall, color = AnimeMuted)
        }
    }
}

@Composable
private fun ResultsHeader() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("\u641c\u7d22\u7ed3\u679c", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Text("\u9009\u62e9\u756a\u5267\u8fdb\u5165\u8be6\u60c5", style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.searchStatusItems(
    loading: Boolean,
    searched: Boolean,
    searchMessage: String?,
    results: List<SearchResult>,
    onOpenDetail: (SearchResult) -> Unit,
) {
    if (loading) {
        item {
            CircularProgressIndicator(color = AnimeAccentCyan)
        }
    }
    searchMessage?.let { message ->
        item {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = AnimeAccentAmber)
        }
    }
    if (results.isEmpty() && !loading) {
        if (searched) {
            item {
                EmptySearchState()
            }
        } else {
            item {
                SearchHintPanel()
            }
        }
    } else {
        items(results) { result ->
            ResultCard(result = result, onClick = { onOpenDetail(result) })
        }
    }
}

@Composable
private fun EmptySearchState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Text(
            text = "\u7EE7\u7EED\u6362\u5173\u952E\u8BCD\uFF0C\u6216\u68C0\u67E5\u6A21\u62DF\u5668/\u624B\u673A\u7F51\u7EDC\u3002",
            style = MaterialTheme.typography.bodyMedium,
            color = AnimeMuted,
            modifier = Modifier.padding(14.dp),
        )
    }
}

private fun SourceSearchReport.statusMessage(): String? {
    if (failures.isEmpty()) return null
    val visibleFailures = failures.take(3).joinToString("\uFF1B") { failure ->
        "${failure.providerName}: ${failure.message}"
    }
    val hiddenCount = failures.size - 3
    val suffix = if (hiddenCount > 0) {
        "\uFF1B\u53E6 $hiddenCount \u4E2A\u6E90\u5931\u8D25"
    } else {
        ""
    }
    return if (results.isEmpty()) {
        "\u641C\u7D22\u6CA1\u6709\u53EF\u7528\u7ED3\u679C\u3002$visibleFailures$suffix"
    } else {
        "\u90E8\u5206\u6E90\u5931\u8D25: $visibleFailures$suffix"
    }
}

@Composable
private fun SourceRailItem(name: String, version: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(name, style = MaterialTheme.typography.bodyMedium, color = Color.White, maxLines = 1)
            Text(version, style = MaterialTheme.typography.labelMedium, color = AnimeMuted)
        }
    }
}

@Composable
private fun AnimeHeroStrip() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        HeroTile("\u63A8\u8350", "\u70ED\u95E8\u5185\u5BB9", AnimeAccentPink, Modifier.weight(1f))
        HeroTile("\u5F39\u5E55", "\u539F\u751F\u6D6E\u5C42", AnimeAccentCyan, Modifier.weight(1f))
        HeroTile("TV", "\u5927\u5C4F\u9065\u63A7", AnimeAccentAmber, Modifier.weight(1f))
    }
}

@Composable
private fun HeroTile(label: String, title: String, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(92.dp),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = accent)
            Text(title, style = MaterialTheme.typography.bodyMedium, color = Color.White, maxLines = 1)
        }
    }
}

@Composable
private fun AnimeFeatureShelf() {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(featureShelfItems()) { item ->
            ResultCard(result = item, onClick = {})
        }
    }
}

private fun featureShelfItems(): List<SearchResult> = featuredOnlineResults()

@Composable
private fun ResultCard(result: SearchResult, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PosterArtwork(
                posterUrl = result.posterUrl,
                accent = providerAccent(result.providerId),
                modifier = Modifier
                    .width(112.dp)
                    .height(64.dp),
                shape = RoundedCornerShape(6.dp),
                icon = Icons.Filled.PlayArrow,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(result.title, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                result.subtitle?.takeIf(String::isNotBlank)?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = AnimeMuted, maxLines = 1)
                }
                Text(providerDisplayName(result.providerId), style = MaterialTheme.typography.bodySmall, color = AnimeAccentCyan, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

private fun providerDisplayName(providerId: String): String {
    return when (providerId.lowercase()) {
        "bangumi-catalog" -> "Bangumi \u8d44\u6599\u5e93"
        "builtin-demo", "demo" -> "\u5185\u7F6E\u7247\u5E93"
        "direct-url", "direct" -> "\u5728\u7EBF\u94FE\u63A5"
        "mikan", "dmhy", "nyaa", "acg-rip", "bangumi-moe", "bt" -> "\u756A\u5267\u9891\u9053"
        else -> "\u89C6\u9891\u6765\u6E90"
    }
}

private fun providerAccent(providerId: String): Color {
    return when (providerId.lowercase()) {
        "bangumi-catalog" -> AnimeAccentCyan
        "bt" -> AnimeAccentPink
        "mikan", "dmhy" -> AnimeAccentPink
        "acg-rip", "bangumi-moe" -> AnimeAccentAmber
        "nyaa" -> AnimeAccentViolet
        "direct" -> AnimeAccentCyan
        "demo" -> AnimeAccentAmber
        else -> AnimeAccentViolet
    }
}

@Composable
private fun DetailScreen(
    graph: AppGraph,
    result: SearchResult,
    onBack: () -> Unit,
    onPlay: (MediaDetail, Episode, MediaStream, List<RouteCandidate>) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var detail by remember(result) { mutableStateOf<MediaDetail?>(null) }
    var loading by remember(result) { mutableStateOf(true) }
    var error by remember(result) { mutableStateOf<String?>(null) }
    var selectedEpisode by remember(result) { mutableStateOf<Episode?>(null) }
    var routes by remember(result) { mutableStateOf<List<RouteCandidate>>(emptyList()) }
    var routesLoading by remember(result) { mutableStateOf(false) }
    var routesError by remember(result) { mutableStateOf<String?>(null) }
    var routeSourceFilter by remember(result) { mutableStateOf<String?>(null) }

    fun loadRoutesFor(episode: Episode, autoPlay: Boolean = false) {
        selectedEpisode = episode
        routes = emptyList()
        routesError = null
        routeSourceFilter = null
        routesLoading = true
        scope.launch {
            runCatching { graph.sourceRegistry.resolveRouteCandidates(episode) }
                .onSuccess { candidates ->
                    routes = candidates
                    if (autoPlay) {
                        val media = detail
                        val firstRoute = candidates.firstOrNull()
                        if (media != null && firstRoute != null) {
                            onPlay(media, episode, firstRoute.stream, candidates)
                        }
                    }
                }
                .onFailure { failure ->
                    routesError = failure.message ?: failure::class.simpleName.orEmpty().ifBlank { "\u672a\u77e5\u9519\u8bef" }
                }
            routesLoading = false
        }
    }

    LaunchedEffect(result) {
        loading = true
        error = null
        routes = emptyList()
        routesError = null
        routeSourceFilter = null
        routesLoading = false
        runCatching { graph.sourceRegistry.loadDetail(result) }
            .onSuccess { media ->
                detail = media
                media.episodes.firstOrNull()?.let { episode ->
                    loadRoutesFor(episode)
                }
            }
            .onFailure { error = it.message }
        loading = false
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AnimeBackground).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(AnimePanelSoft, RoundedCornerShape(8.dp)).focusable(),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "\u8FD4\u56DE", tint = Color.White)
                }
                Text(result.title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (loading) {
            item {
                CircularProgressIndicator(color = AnimeAccentCyan)
            }
        }
        error?.let {
            item { Text("\u52A0\u8F7D\u5931\u8D25: $it", color = MaterialTheme.colorScheme.error) }
        }
        detail?.let { media ->
            item {
                DetailHero(media = media) {
                    media.episodes.firstOrNull()?.let { episode ->
                        loadRoutesFor(episode, autoPlay = true)
                    }
                }
            }
            item {
                SectionHeader(title = "\u9009\u96c6", action = "${media.episodes.size} \u96c6", onAction = {})
            }
            item {
                EpisodeSelectorRow(
                    episodes = media.episodes,
                    selectedEpisodeId = selectedEpisode?.id,
                    onEpisodeSelected = { loadRoutesFor(it) },
                )
            }
            item {
                SectionHeader(
                    title = "\u64ad\u653e\u7ebf\u8def",
                    action = selectedEpisode?.index?.let { "\u7b2c $it \u96c6" }.orEmpty(),
                    onAction = {},
                )
            }
            val visibleRoutes = routes.filter { route ->
                routeSourceFilter == null || route.sourceId == routeSourceFilter
            }
            if (routes.isNotEmpty()) {
                item {
                    RouteSourceSelector(
                        routes = routes,
                        selectedSourceId = routeSourceFilter,
                        onSelected = { routeSourceFilter = it },
                    )
                }
            }
            if (routesLoading) {
                item {
                    RouteLoadingPanel(selectedEpisode = selectedEpisode)
                }
            }
            routesError?.let { message ->
                item { Text("\u7ebf\u8def\u52a0\u8f7d\u5931\u8d25: $message", color = MaterialTheme.colorScheme.error) }
            }
            if (!routesLoading && selectedEpisode != null && routes.isEmpty() && routesError == null) {
                item {
                    EmptyRoutePanel()
                }
            }
            items(visibleRoutes) { route ->
                RouteCandidateRow(
                    route = route,
                    onClick = {
                        val episode = selectedEpisode ?: return@RouteCandidateRow
                        onPlay(media, episode, route.stream, routes)
                    },
                )
            }
        }
    }
}

@Composable
private fun DetailHero(media: MediaDetail, onPlayFirst: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PosterArtwork(
                posterUrl = media.posterUrl,
                accent = providerAccent(media.providerId),
                modifier = Modifier.size(width = 96.dp, height = 132.dp),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(media.title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VideoMetaChip("\u591A\u7EBF\u8DEF")
                    VideoMetaChip("\u5F39\u5E55")
                }
                Text(
                    media.summary.orEmpty().ifBlank { "\u5DF2\u4E3A\u4F60\u5339\u914D\u53EF\u64AD\u653E\u7EBF\u8DEF\uFF0C\u9009\u62E9\u5267\u96C6\u5373\u53EF\u5F00\u59CB\u89C2\u770B\u3002" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = AnimeMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Button(
                    onClick = onPlayFirst,
                    colors = ButtonDefaults.buttonColors(containerColor = AnimeAccentPink, contentColor = Color.White),
                    modifier = Modifier.focusable(),
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("\u64AD\u653E\u7B2C 1 \u96C6")
                }
            }
        }
    }
}

@Composable
private fun EpisodeSelectorRow(
    episodes: List<Episode>,
    selectedEpisodeId: String?,
    onEpisodeSelected: (Episode) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(episodes) { episode ->
            val selected = episode.id == selectedEpisodeId
            Card(
                onClick = { onEpisodeSelected(episode) },
                modifier = Modifier.width(96.dp).height(54.dp).focusable(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = if (selected) AnimePanelSoft else AnimePanel),
                border = BorderStroke(1.dp, if (selected) AnimeAccentCyan else AnimeBorder),
            ) {
                Box(Modifier.fillMaxSize().padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = episode.index?.let { "\u7b2c $it \u96c6" } ?: episode.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) AnimeAccentCyan else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun EpisodeVideoRow(episode: Episode, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) AnimePanelSoft else AnimePanel),
        border = BorderStroke(1.dp, if (selected) AnimeAccentCyan else AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(width = 108.dp, height = 62.dp).background(AnimePanelSoft, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = AnimeAccentCyan)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(episode.title, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("\u70B9\u51FB\u4F18\u5148\u5339\u914D\u5728\u7EBF\u89C6\u9891\u7EBF\u8DEF\uFF0CBT \u4F5C\u4E3A\u5907\u7528", style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1)
            }
            Text(if (selected) "\u5DF2\u9009" else "\u627E\u7EBF\u8DEF", style = MaterialTheme.typography.labelLarge, color = AnimeAccentCyan)
        }
    }
}

@Composable
private fun RouteLoadingPanel(selectedEpisode: Episode?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(color = AnimeAccentCyan, modifier = Modifier.size(28.dp))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("\u6B63\u5728\u5339\u914D\u7EBF\u8DEF", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(selectedEpisode?.title.orEmpty().ifBlank { "\u9009\u4E2D\u5267\u96C6" }, style = MaterialTheme.typography.bodySmall, color = AnimeMuted)
            }
        }
    }
}

@Composable
private fun EmptyRoutePanel() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("\u6682\u672A\u5339\u914D\u5230\u53EF\u64AD\u653E\u7EBF\u8DEF", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text("\u53EF\u4EE5\u6362\u4E00\u4E2A\u756A\u540D\u641C\u7D22\uFF0C\u6216\u9009\u62E9\u5176\u4ED6\u96C6\u6570\u91CD\u8BD5\u3002", style = MaterialTheme.typography.bodySmall, color = AnimeMuted)
        }
    }
}

@Composable
private fun RouteSourceSelector(
    routes: List<RouteCandidate>,
    selectedSourceId: String?,
    onSelected: (String?) -> Unit,
) {
    val groups = routes
        .groupBy { it.sourceId }
        .map { (sourceId, sourceRoutes) ->
            RouteSourceGroup(
                id = sourceId,
                name = sourceRoutes.firstOrNull()?.sourceName ?: sourceId,
                count = sourceRoutes.size,
                isOnline = sourceRoutes.any { it.protocol != StreamProtocol.BITTORRENT },
            )
        }
        .sortedWith(compareByDescending<RouteSourceGroup> { it.isOnline }.thenBy { it.name })
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            RouteSourceChip(
                label = "\u5168\u90e8 ${routes.size}",
                selected = selectedSourceId == null,
                onClick = { onSelected(null) },
            )
        }
        items(groups) { group ->
            RouteSourceChip(
                label = "${group.name} ${group.count}",
                selected = selectedSourceId == group.id,
                onClick = { onSelected(group.id) },
            )
        }
    }
}

@Composable
private fun RouteSourceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.height(42.dp).focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) AnimeAccentCyan.copy(alpha = 0.22f) else AnimePanel),
        border = BorderStroke(1.dp, if (selected) AnimeAccentCyan else AnimeBorder),
    ) {
        Box(Modifier.padding(horizontal = 14.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) AnimeAccentCyan else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class RouteSourceGroup(
    val id: String,
    val name: String,
    val count: Int,
    val isOnline: Boolean,
)

@Composable
private fun RouteCandidateRow(route: RouteCandidate, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(42.dp).background(providerAccent(route.sourceId), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(route.sourceName.take(1), style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(route.sourceName, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    route.quality?.let { VideoMetaChip(it) }
                    route.subgroup?.takeIf(String::isNotBlank)?.let { VideoMetaChip(it.take(10)) }
                }
                Text(route.title, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    route.routeName.orEmpty().ifBlank { route.protocol.displayName() },
                    style = MaterialTheme.typography.bodySmall,
                    color = AnimeAccentCyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(route.protocol.displayName(), style = MaterialTheme.typography.labelLarge, color = AnimeAccentAmber)
                route.sizeBytes?.let { Text(formatBytes(it), style = MaterialTheme.typography.bodySmall, color = AnimeMuted) }
            }
        }
    }
}

private fun StreamProtocol.displayName(): String {
    return when (this) {
        StreamProtocol.BITTORRENT -> "BT"
        StreamProtocol.HLS -> "HLS"
        StreamProtocol.DASH -> "DASH"
        StreamProtocol.PROGRESSIVE -> "MP4"
        StreamProtocol.SMOOTH_STREAMING -> "Smooth"
        StreamProtocol.RTSP -> "RTSP"
        StreamProtocol.WEBVIEW_ONLY -> "WebView"
        StreamProtocol.UNKNOWN -> "\u672A\u77E5"
    }
}

@Composable
private fun PlayerScreen(
    graph: AppGraph,
    detail: MediaDetail,
    episode: Episode,
    stream: MediaStream,
    routes: List<RouteCandidate>,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val engine = remember { ExoPlayerEngine(context) }
    val state by engine.state.collectAsState()
    val torrentState by graph.torrentEngine.state.collectAsState()
    val torrentPlaybackUrl = torrentState.plan?.localPlaybackUrl
    var currentEpisode by remember(episode.id) { mutableStateOf(episode) }
    var playerRoutes by remember(episode.id, stream.id) { mutableStateOf(routes) }
    val routeOptions = remember(playerRoutes) { playerRoutes.distinctBy { it.stream.id } }
    var currentStream by remember(stream.id) { mutableStateOf(stream) }
    var failedStreamIds by remember(stream.id, playerRoutes) { mutableStateOf<Set<String>>(emptySet()) }
    var routeNotice by remember(stream.id) { mutableStateOf<String?>(null) }
    var danmakuItems by remember { mutableStateOf<List<DanmakuItem>>(emptyList()) }
    var danmakuEnabled by remember { mutableStateOf(true) }
    var density by remember { mutableFloatStateOf(1f) }
    var danmakuAlpha by remember { mutableFloatStateOf(0.9f) }
    var danmakuFontScale by remember { mutableFloatStateOf(1f) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    var activePanel by remember(stream.id) { mutableStateOf<PlayerPanel?>(null) }
    var episodeLoadingId by remember { mutableStateOf<String?>(null) }
    var controlsVisible by remember(currentStream.id) { mutableStateOf(true) }
    var playbackPositionMs by remember(currentStream.id) { mutableStateOf(0L) }
    var playbackDurationMs by remember(currentStream.id) { mutableStateOf(0L) }
    val profile = remember { DanmakuProfile(DanmakuPlatform.Bilibili, supportsAdvanced = true) }

    LaunchedEffect(playbackSpeed) {
        engine.player.setPlaybackSpeed(playbackSpeed)
    }
    LaunchedEffect(currentStream.id, currentEpisode.id) {
        if (currentStream.protocol == StreamProtocol.BITTORRENT) {
            graph.torrentEngine.prepare(currentStream)
        } else {
            graph.torrentEngine.release()
            engine.prepare(currentStream)
        }
        val match = graph.danmakuRegistry.matchAll(detail, currentEpisode).firstOrNull()
        danmakuItems = match?.let { graph.danmakuRegistry.provider(it.providerId)?.fetchTimeline(it) }.orEmpty()
    }
    LaunchedEffect(currentStream.id, torrentPlaybackUrl) {
        if (currentStream.protocol == StreamProtocol.BITTORRENT && torrentPlaybackUrl != null) {
            engine.prepare(
                currentStream.copy(
                    id = "${currentStream.id}:local",
                    url = torrentPlaybackUrl,
                    protocol = StreamProtocol.PROGRESSIVE,
                    headers = emptyMap(),
                ),
            )
        }
    }
    LaunchedEffect(currentStream.id) {
        while (true) {
            playbackPositionMs = engine.currentPositionMs().coerceAtLeast(0L)
            playbackDurationMs = normalizePlaybackDurationMs(engine.player.duration)
            delay(500)
        }
    }
    LaunchedEffect(controlsVisible) {
        if (!controlsVisible) {
            activePanel = null
        }
    }
    LaunchedEffect(state.errorMessage, currentStream.id, routeOptions) {
        val errorMessage = state.errorMessage ?: return@LaunchedEffect
        if (currentStream.protocol == StreamProtocol.BITTORRENT || currentStream.id in failedStreamIds) return@LaunchedEffect
        val failedIds = failedStreamIds + currentStream.id
        failedStreamIds = failedIds
        val nextRoute = routeOptions.firstOrNull { route ->
            route.stream.id !in failedIds &&
                route.stream.protocol != StreamProtocol.WEBVIEW_ONLY
        }
        if (nextRoute != null) {
            routeNotice = "线路失败，已自动切换到 ${nextRoute.sourceName} ${nextRoute.routeName.orEmpty().ifBlank { nextRoute.protocol.displayName() }}"
            currentStream = nextRoute.stream
        } else {
            routeNotice = "当前线路失败：$errorMessage"
        }
    }
    LaunchedEffect(state.hasRenderedFirstFrame, state.isPlaying, currentStream.id) {
        if ((state.hasRenderedFirstFrame || state.isPlaying) && routeNotice != null) {
            routeNotice = null
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            engine.release()
            graph.torrentEngine.release()
        }
    }

    val currentRoute = routeOptions.firstOrNull { it.stream.id == currentStream.id || it.stream.url == currentStream.url }
    val routeLabel = playerRouteLabel(currentStream, currentRoute)
    val effectiveErrorMessage = if (currentStream.protocol == StreamProtocol.BITTORRENT) {
        torrentState.errorMessage
    } else {
        state.errorMessage
    }
    fun selectRoute(route: RouteCandidate) {
        controlsVisible = true
        activePanel = null
        failedStreamIds = emptySet()
        routeNotice = null
        currentStream = route.stream
    }

    fun selectEpisode(target: Episode) {
        if (target.id == currentEpisode.id || episodeLoadingId != null) return
        controlsVisible = true
        routeNotice = "正在加载 ${target.title} 的播放线路..."
        episodeLoadingId = target.id
        scope.launch {
            val result = runCatching { graph.sourceRegistry.resolveRouteCandidates(target) }
            result
                .onSuccess { candidates ->
                    val firstRoute = candidates.firstOrNull()
                    if (firstRoute != null) {
                        currentEpisode = target
                        playerRoutes = candidates
                        failedStreamIds = emptySet()
                        routeNotice = null
                        activePanel = null
                        currentStream = firstRoute.stream
                    } else {
                        routeNotice = "${target.title} 暂时没有可用播放线路"
                    }
                }
                .onFailure { failure ->
                    routeNotice = "选集加载失败：${failure.message ?: failure::class.simpleName.orEmpty().ifBlank { "未知错误" }}"
                }
            episodeLoadingId = null
        }
    }

    val revealControls = { controlsVisible = true }
    val danmakuTopPadding = if (controlsVisible) 58.dp else 8.dp
    val danmakuBottomPadding = when {
        !controlsVisible -> 8.dp
        activePanel != null -> 240.dp
        else -> 150.dp
    }

    Box(Modifier.fillMaxSize().background(AnimeBackground)) {
        if (currentStream.protocol == StreamProtocol.BITTORRENT && torrentPlaybackUrl == null) {
            TorrentPlaceholderSurface(
                stream = currentStream,
                state = torrentState,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            PlayerViewSurface(
                engine = engine,
                modifier = Modifier.fillMaxSize(),
                onSurfaceTap = revealControls,
            )
        }
        if (
            (currentStream.protocol != StreamProtocol.BITTORRENT || torrentPlaybackUrl != null) &&
            !state.hasRenderedFirstFrame &&
            state.errorMessage == null
        ) {
            VideoStartupOverlay(
                playbackState = state.playbackStateLabel,
                videoSize = state.videoSizeLabel,
                protocol = if (currentStream.protocol == StreamProtocol.BITTORRENT) StreamProtocol.PROGRESSIVE else currentStream.protocol,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        DanmakuSurface(
            items = danmakuItems,
            playbackMsProvider = engine::currentPositionMs,
            profile = profile,
            settings = DanmakuSettings(
                enabled = danmakuEnabled,
                alpha = danmakuAlpha,
                density = density,
                fontScale = danmakuFontScale,
            ),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = danmakuTopPadding, bottom = danmakuBottomPadding),
        )
        if (!controlsVisible) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(10f)
                    .background(Color.Black.copy(alpha = 0.001f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        revealControls()
                    },
            )
        }
        PlayerEdgeProgress(
            positionMs = playbackPositionMs,
            durationMs = playbackDurationMs,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().zIndex(3f),
        )
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).zIndex(4f),
        ) {
            PlayerTopOverlay(
                title = detail.title,
                episodeTitle = currentEpisode.title,
                routeLabel = routeLabel,
                playbackState = state.playbackStateLabel,
                onBack = onBack,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        AnimatedVisibility(
            visible = controlsVisible && activePanel == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center).zIndex(4f),
        ) {
            PlayerCenterControls(
                isPlaying = state.isPlaying,
                onSeekBackward = {
                    controlsVisible = true
                    engine.player.seekTo((engine.currentPositionMs() - 10_000L).coerceAtLeast(0L))
                },
                onTogglePlay = {
                    controlsVisible = true
                    if (state.isPlaying) {
                        engine.player.pause()
                    } else {
                        engine.player.play()
                    }
                },
                onSeekForward = {
                    controlsVisible = true
                    val duration = normalizePlaybackDurationMs(engine.player.duration)
                    val target = engine.currentPositionMs() + 10_000L
                    engine.player.seekTo(if (duration > 0L) target.coerceAtMost(duration) else target)
                },
            )
        }
        AnimatedVisibility(
            visible = controlsVisible && activePanel == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).zIndex(4f),
        ) {
            PlayerBottomControls(
                currentStream = currentStream,
                currentRoute = currentRoute,
                routeOptions = routeOptions,
                routeNotice = routeNotice,
                errorMessage = effectiveErrorMessage,
                danmakuEnabled = danmakuEnabled,
                density = density,
                playbackSpeed = playbackSpeed,
                activePanel = activePanel,
                episodeCount = detail.episodes.size,
                positionMs = playbackPositionMs,
                durationMs = playbackDurationMs,
                onSeek = {
                    controlsVisible = true
                    engine.player.seekTo(it)
                },
                onShowPanel = { panel ->
                    controlsVisible = true
                    activePanel = panel
                },
                onToggleDanmaku = {
                    controlsVisible = true
                    danmakuEnabled = !danmakuEnabled
                },
                onOffline = {
                    controlsVisible = true
                    if (currentStream.protocol != StreamProtocol.BITTORRENT) {
                        graph.media3DownloadCoordinator.enqueue(currentStream, "${detail.title} ${currentEpisode.title}")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        AnimatedVisibility(
            visible = controlsVisible && activePanel != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.matchParentSize().zIndex(5f),
        ) {
            activePanel?.let { panel ->
                PlayerOptionPanel(
                    panel = panel,
                    detail = detail,
                    currentEpisode = currentEpisode,
                    routeOptions = routeOptions,
                    selectedStreamId = currentStream.id,
                    currentStream = currentStream,
                    danmakuEnabled = danmakuEnabled,
                    density = density,
                    danmakuAlpha = danmakuAlpha,
                    danmakuFontScale = danmakuFontScale,
                    playbackSpeed = playbackSpeed,
                    episodeLoadingId = episodeLoadingId,
                    routeNotice = routeNotice,
                    onDismiss = { activePanel = null },
                    onRouteSelected = ::selectRoute,
                    onEpisodeSelected = ::selectEpisode,
                    onToggleDanmaku = {
                        controlsVisible = true
                        danmakuEnabled = !danmakuEnabled
                    },
                    onDensityChange = {
                        controlsVisible = true
                        density = it
                    },
                    onDanmakuAlphaChange = {
                        controlsVisible = true
                        danmakuAlpha = it
                    },
                    onDanmakuFontScaleChange = {
                        controlsVisible = true
                        danmakuFontScale = it
                    },
                    onSpeedSelected = {
                        controlsVisible = true
                        playbackSpeed = it
                        activePanel = null
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun PlayerTopOverlay(
    title: String,
    episodeTitle: String,
    routeLabel: String,
    playbackState: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.82f),
                        Color.Black.copy(alpha = 0.46f),
                        Color.Transparent,
                    ),
                ),
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PlayerCircleButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回详情",
                onClick = onBack,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = listOf(episodeTitle, playbackState).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            PlayerPill(text = routeLabel, color = AnimeAccentCyan, modifier = Modifier.width(168.dp))
        }
    }
}

@Composable
private fun PlayerCenterControls(
    isPlaying: Boolean,
    onSeekBackward: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeekForward: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.34f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        PlayerCircleButton(
            icon = Icons.Filled.Replay10,
            contentDescription = "后退 10 秒",
            onClick = onSeekBackward,
        )
        PlayerCircleButton(
            icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (isPlaying) "暂停" else "播放",
            onClick = onTogglePlay,
            prominent = true,
            selected = true,
        )
        PlayerCircleButton(
            icon = Icons.Filled.Forward10,
            contentDescription = "快进 10 秒",
            onClick = onSeekForward,
        )
    }
}

@Composable
private fun PlayerBottomControls(
    currentStream: MediaStream,
    currentRoute: RouteCandidate?,
    routeOptions: List<RouteCandidate>,
    routeNotice: String?,
    errorMessage: String?,
    danmakuEnabled: Boolean,
    density: Float,
    playbackSpeed: Float,
    activePanel: PlayerPanel?,
    episodeCount: Int,
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    onShowPanel: (PlayerPanel) -> Unit,
    onToggleDanmaku: () -> Unit,
    onOffline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingSeekMs by remember(currentStream.id) { mutableStateOf<Long?>(null) }
    val displayPositionMs = if (durationMs > 0L) {
        (pendingSeekMs ?: positionMs).coerceIn(0L, durationMs)
    } else {
        0L
    }
    val routeName = currentRoute?.routeName.orEmpty().ifBlank { currentStream.protocol.displayName() }
    val sourceName = currentRoute?.sourceName ?: currentStream.metadata["routeProviderName"] ?: currentStream.providerId
    val quality = currentStream.quality.orEmpty().ifBlank { "自动" }
    val routeSummary = if (quality != "自动" && routeName.contains(quality, ignoreCase = true)) {
        "$sourceName · $routeName"
    } else {
        "$sourceName · $routeName · $quality"
    }

    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.56f),
                        Color.Black.copy(alpha = 0.88f),
                    ),
                ),
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = formatPlaybackTime(displayPositionMs),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                modifier = Modifier.width(48.dp),
            )
            if (durationMs > 0L) {
                Slider(
                    value = displayPositionMs.toFloat(),
                    onValueChange = { pendingSeekMs = it.toLong() },
                    onValueChangeFinished = {
                        pendingSeekMs?.let(onSeek)
                        pendingSeekMs = null
                    },
                    valueRange = 0f..durationMs.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = AnimeAccentPink,
                        activeTrackColor = AnimeAccentPink,
                        inactiveTrackColor = Color.White.copy(alpha = 0.24f),
                    ),
                    modifier = Modifier.weight(1f).height(30.dp).focusable(),
                )
            } else {
                LinearProgressIndicator(
                    modifier = Modifier.weight(1f).height(3.dp),
                    color = AnimeAccentCyan,
                    trackColor = Color.White.copy(alpha = 0.18f),
                )
            }
            Text(
                text = if (durationMs > 0L) formatPlaybackTime(durationMs) else "--:--",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.78f),
                modifier = Modifier.width(48.dp),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = routeSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                (routeNotice ?: errorMessage)?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (routeNotice != null) AnimeAccentAmber else MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                } ?: Text(
                    text = "${currentStream.protocol.displayName()} · ${currentStream.codec.orEmpty().ifBlank { "自适应解码" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.64f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        PlayerDanmakuInputBar(
            danmakuEnabled = danmakuEnabled,
            modifier = Modifier.fillMaxWidth(),
        )

        PlayerActionBar(
            quality = quality,
            routeCount = routeOptions.size,
            episodeCount = episodeCount,
            danmakuEnabled = danmakuEnabled,
            density = density,
            playbackSpeed = playbackSpeed,
            activePanel = activePanel,
            offlineEnabled = currentStream.protocol != StreamProtocol.BITTORRENT,
            onToggleDanmaku = onToggleDanmaku,
            onShowPanel = onShowPanel,
            onOffline = onOffline,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PlayerDanmakuInputBar(
    danmakuEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.38f))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Filled.ClosedCaption,
            contentDescription = null,
            tint = if (danmakuEnabled) AnimeAccentPink else Color.White.copy(alpha = 0.46f),
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = if (danmakuEnabled) "发个友善的弹幕" else "弹幕已关闭",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "发送",
            style = MaterialTheme.typography.labelMedium,
            color = if (danmakuEnabled) AnimeAccentPink else Color.White.copy(alpha = 0.36f),
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun PlayerActionBar(
    quality: String,
    routeCount: Int,
    episodeCount: Int,
    danmakuEnabled: Boolean,
    density: Float,
    playbackSpeed: Float,
    activePanel: PlayerPanel?,
    offlineEnabled: Boolean,
    onToggleDanmaku: () -> Unit,
    onShowPanel: (PlayerPanel) -> Unit,
    onOffline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = 2.dp),
    ) {
        item {
            PlayerTextAction(
                icon = Icons.Filled.ClosedCaption,
                text = if (danmakuEnabled) "弹幕 开" else "弹幕 关",
                selected = danmakuEnabled,
                onClick = onToggleDanmaku,
            )
        }
        item {
            PlayerTextAction(
                icon = Icons.Filled.Settings,
                text = "弹幕 ${formatDanmakuDensity(density)}",
                selected = activePanel == PlayerPanel.Danmaku,
                onClick = { onShowPanel(PlayerPanel.Danmaku) },
            )
        }
        item {
            PlayerTextAction(
                icon = Icons.Filled.HighQuality,
                text = "清晰度 $quality",
                selected = activePanel == PlayerPanel.Quality,
                enabled = routeCount > 0,
                onClick = { onShowPanel(PlayerPanel.Quality) },
            )
        }
        item {
            PlayerTextAction(
                icon = Icons.Filled.Speed,
                text = "倍速 ${formatPlaybackSpeed(playbackSpeed)}",
                selected = activePanel == PlayerPanel.Speed,
                onClick = { onShowPanel(PlayerPanel.Speed) },
            )
        }
        item {
            PlayerTextAction(
                icon = Icons.Filled.VideoLibrary,
                text = "线路 ${routeCount.coerceAtLeast(1)}",
                selected = activePanel == PlayerPanel.Route,
                enabled = routeCount > 1,
                onClick = { onShowPanel(PlayerPanel.Route) },
            )
        }
        item {
            PlayerTextAction(
                icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                text = "选集",
                selected = activePanel == PlayerPanel.Episode,
                enabled = episodeCount > 1,
                onClick = { onShowPanel(PlayerPanel.Episode) },
            )
        }
        item {
            PlayerTextAction(
                icon = Icons.Filled.CloudDownload,
                text = "缓存",
                enabled = offlineEnabled,
                onClick = onOffline,
            )
        }
    }
}

@Composable
private fun PlayerOptionPanel(
    panel: PlayerPanel,
    detail: MediaDetail,
    currentEpisode: Episode,
    routeOptions: List<RouteCandidate>,
    selectedStreamId: String,
    currentStream: MediaStream,
    danmakuEnabled: Boolean,
    density: Float,
    danmakuAlpha: Float,
    danmakuFontScale: Float,
    playbackSpeed: Float,
    episodeLoadingId: String?,
    routeNotice: String?,
    onDismiss: () -> Unit,
    onRouteSelected: (RouteCandidate) -> Unit,
    onEpisodeSelected: (Episode) -> Unit,
    onToggleDanmaku: () -> Unit,
    onDensityChange: (Float) -> Unit,
    onDanmakuAlphaChange: (Float) -> Unit,
    onDanmakuFontScaleChange: (Float) -> Unit,
    onSpeedSelected: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val landscape = maxWidth > maxHeight
        val panelWidth = if (maxWidth < 420.dp) maxWidth else 380.dp
        val panelHeight = if (maxHeight < 420.dp) maxHeight else 390.dp
        val panelModifier = if (landscape) {
            Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(panelWidth)
        } else {
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().heightIn(max = panelHeight)
        }
        val panelShape = if (landscape) {
            RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
        } else {
            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
        }

        Surface(
            modifier = panelModifier,
            shape = panelShape,
            color = Color.Black.copy(alpha = 0.9f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PlayerPanelHeader(title = playerPanelTitle(panel), onDismiss = onDismiss)
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    when (panel) {
                        PlayerPanel.Danmaku -> PlayerDanmakuSettingsPanel(
                            danmakuEnabled = danmakuEnabled,
                            density = density,
                            alpha = danmakuAlpha,
                            fontScale = danmakuFontScale,
                            onToggleDanmaku = onToggleDanmaku,
                            onDensityChange = onDensityChange,
                            onAlphaChange = onDanmakuAlphaChange,
                            onFontScaleChange = onDanmakuFontScaleChange,
                        )
                        PlayerPanel.Quality -> PlayerQualityPanel(
                            routes = routeOptions,
                            currentStream = currentStream,
                            onRouteSelected = onRouteSelected,
                        )
                        PlayerPanel.Speed -> PlayerSpeedPanel(
                            playbackSpeed = playbackSpeed,
                            onSpeedSelected = onSpeedSelected,
                        )
                        PlayerPanel.Route -> PlayerRoutePanel(
                            routes = routeOptions,
                            selectedStreamId = selectedStreamId,
                            routeNotice = routeNotice,
                            onRouteSelected = onRouteSelected,
                        )
                        PlayerPanel.Episode -> PlayerEpisodePanel(
                            detail = detail,
                            currentEpisode = currentEpisode,
                            episodeLoadingId = episodeLoadingId,
                            onEpisodeSelected = onEpisodeSelected,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerPanelHeader(title: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onDismiss, modifier = Modifier.height(34.dp)) {
            Text("关闭", color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun PlayerDanmakuSettingsPanel(
    danmakuEnabled: Boolean,
    density: Float,
    alpha: Float,
    fontScale: Float,
    onToggleDanmaku: () -> Unit,
    onDensityChange: (Float) -> Unit,
    onAlphaChange: (Float) -> Unit,
    onFontScaleChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PlayerSelectableRow(
            title = if (danmakuEnabled) "弹幕已开启" else "弹幕已关闭",
            subtitle = "点击切换弹幕显示状态",
            selected = danmakuEnabled,
            icon = Icons.Filled.ClosedCaption,
            onClick = onToggleDanmaku,
        )
        PlayerSliderSetting(
            title = "密度",
            valueText = formatDanmakuDensity(density),
            value = density.coerceIn(0.5f, 1.5f),
            valueRange = 0.5f..1.5f,
            steps = 1,
            onValueChange = onDensityChange,
        )
        PlayerSliderSetting(
            title = "透明度",
            valueText = formatPercentLabel(alpha),
            value = alpha.coerceIn(0.35f, 1f),
            valueRange = 0.35f..1f,
            steps = 12,
            onValueChange = onAlphaChange,
        )
        PlayerSliderSetting(
            title = "字号",
            valueText = formatScaleLabel(fontScale),
            value = fontScale.coerceIn(0.75f, 1.35f),
            valueRange = 0.75f..1.35f,
            steps = 11,
            onValueChange = onFontScaleChange,
        )
    }
}

@Composable
private fun PlayerQualityPanel(
    routes: List<RouteCandidate>,
    currentStream: MediaStream,
    onRouteSelected: (RouteCandidate) -> Unit,
) {
    val qualityRoutes = remember(routes) {
        routes
            .groupBy { playerQualityLabel(it) }
            .mapNotNull { (_, group) -> group.maxByOrNull { it.score } }
            .sortedByDescending { it.score }
    }
    if (qualityRoutes.isEmpty()) {
        Text("当前线路没有提供可切换清晰度", style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
        return
    }
    val currentQuality = currentStream.quality.orEmpty().ifBlank { "自动" }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(qualityRoutes, key = { it.stream.id }) { route ->
            val quality = playerQualityLabel(route)
            PlayerSelectableRow(
                title = quality,
                subtitle = "${route.sourceName} · ${route.routeName.orEmpty().ifBlank { route.protocol.displayName() }}",
                selected = quality == currentQuality || route.stream.id == currentStream.id,
                icon = Icons.Filled.HighQuality,
                onClick = { onRouteSelected(route) },
            )
        }
    }
}

@Composable
private fun PlayerSpeedPanel(
    playbackSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
) {
    val speeds = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(speeds) { speed ->
            PlayerSelectableRow(
                title = formatPlaybackSpeed(speed),
                subtitle = if (speed == 1f) "标准速度" else null,
                selected = speed == playbackSpeed,
                icon = Icons.Filled.Speed,
                onClick = { onSpeedSelected(speed) },
            )
        }
    }
}

@Composable
private fun PlayerRoutePanel(
    routes: List<RouteCandidate>,
    selectedStreamId: String,
    routeNotice: String?,
    onRouteSelected: (RouteCandidate) -> Unit,
) {
    if (routeNotice != null) {
        Text(routeNotice, style = MaterialTheme.typography.bodySmall, color = AnimeAccentAmber)
    }
    if (routes.isEmpty()) {
        Text("暂时没有可用播放线路", style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(routes, key = { it.stream.id }) { route ->
            PlayerSelectableRow(
                title = "${route.sourceName} · ${route.routeName.orEmpty().ifBlank { route.protocol.displayName() }}",
                subtitle = listOfNotNull(route.quality, route.subgroup, route.protocol.displayName()).joinToString(" · "),
                selected = route.stream.id == selectedStreamId,
                icon = Icons.Filled.VideoLibrary,
                onClick = { onRouteSelected(route) },
            )
        }
    }
}

@Composable
private fun PlayerEpisodePanel(
    detail: MediaDetail,
    currentEpisode: Episode,
    episodeLoadingId: String?,
    onEpisodeSelected: (Episode) -> Unit,
) {
    if (detail.episodes.isEmpty()) {
        Text("当前条目没有可切换选集", style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(detail.episodes, key = { it.id }) { item ->
            val loading = episodeLoadingId == item.id
            PlayerSelectableRow(
                title = item.title,
                subtitle = item.index?.let { "第 $it 集" },
                selected = item.id == currentEpisode.id,
                enabled = episodeLoadingId == null || loading,
                icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                trailing = if (loading) "加载中" else null,
                onClick = { onEpisodeSelected(item) },
            )
        }
    }
}

@Composable
private fun PlayerSliderSetting(
    title: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = Color.White, modifier = Modifier.weight(1f))
            Text(valueText, style = MaterialTheme.typography.labelMedium, color = AnimeAccentCyan)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = AnimeAccentPink,
                activeTrackColor = AnimeAccentPink,
                inactiveTrackColor = Color.White.copy(alpha = 0.22f),
            ),
            modifier = Modifier.fillMaxWidth().height(30.dp),
        )
    }
}

@Composable
private fun PlayerSelectableRow(
    title: String,
    subtitle: String? = null,
    selected: Boolean,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    trailing: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    selected -> AnimeAccentPink.copy(alpha = 0.18f)
                    else -> Color.White.copy(alpha = 0.06f)
                },
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) AnimeAccentPink else Color.White.copy(alpha = if (enabled) 0.76f else 0.32f),
                modifier = Modifier.size(19.dp),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = if (enabled) 0.94f else 0.42f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            subtitle?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = AnimeMuted.copy(alpha = if (enabled) 0.86f else 0.38f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        trailing?.let {
            Text(it, style = MaterialTheme.typography.labelSmall, color = AnimeAccentAmber, maxLines = 1)
        }
        if (selected) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = AnimeAccentPink, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PlayerTextAction(
    icon: ImageVector?,
    text: String,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.height(34.dp).focusable(),
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (selected) AnimeAccentPink else Color.White.copy(alpha = 0.9f),
            disabledContentColor = Color.White.copy(alpha = 0.36f),
        ),
        contentPadding = PaddingValues(horizontal = 0.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(5.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}

@Composable
private fun PlayerInfoPill(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = Color.White.copy(alpha = 0.88f),
        maxLines = 1,
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.34f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    )
}

@Composable
private fun PlayerEdgeProgress(
    positionMs: Long,
    durationMs: Long,
    modifier: Modifier = Modifier,
) {
    val progress = if (durationMs > 0L) {
        (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier.height(2.dp),
        color = AnimeAccentPink,
        trackColor = Color.Transparent,
    )
}

@Composable
private fun PlayerCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    prominent: Boolean = false,
    enabled: Boolean = true,
) {
    val size = if (prominent) 62.dp else 44.dp
    val iconSize = if (prominent) 34.dp else 22.dp
    val backgroundColor = when {
        !enabled -> Color.White.copy(alpha = 0.08f)
        selected -> AnimeAccentPink
        else -> Color.Black.copy(alpha = 0.46f)
    }
    val iconColor = when {
        !enabled -> Color.White.copy(alpha = 0.3f)
        selected -> Color.White
        else -> Color.White.copy(alpha = 0.9f)
    }

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .focusable(),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
private fun PlayerPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.42f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

private fun playerRouteLabel(stream: MediaStream, route: RouteCandidate?): String {
    return listOf(
        route?.sourceName ?: stream.metadata["routeProviderName"],
        stream.quality?.takeIf { it.isNotBlank() },
        stream.protocol.displayName(),
    )
        .filterNotNull()
        .joinToString(" · ")
        .ifBlank { stream.protocol.displayName() }
}

private fun playerPanelTitle(panel: PlayerPanel): String {
    return when (panel) {
        PlayerPanel.Danmaku -> "弹幕设置"
        PlayerPanel.Quality -> "清晰度"
        PlayerPanel.Speed -> "播放速度"
        PlayerPanel.Route -> "播放线路"
        PlayerPanel.Episode -> "选集"
    }
}

private fun playerQualityLabel(route: RouteCandidate): String {
    return route.quality
        ?: route.stream.quality?.takeIf { it.isNotBlank() }
        ?: route.routeName?.takeIf { it.isNotBlank() }
        ?: route.protocol.displayName()
}

@Composable
private fun PlayerRouteSelector(
    routes: List<RouteCandidate>,
    selectedStreamId: String,
    onSelected: (RouteCandidate) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp),
    ) {
        items(routes) { route ->
            val selected = route.stream.id == selectedStreamId
            Button(
                onClick = { onSelected(route) },
                modifier = Modifier.height(36.dp).focusable(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) AnimeAccentCyan else Color.Black.copy(alpha = 0.42f),
                    contentColor = if (selected) AnimeBackground else Color.White.copy(alpha = 0.88f),
                ),
                contentPadding = PaddingValues(horizontal = 10.dp),
            ) {
                Text(
                    text = "${route.sourceName} ${route.routeName.orEmpty().ifBlank { route.protocol.displayName() }}",
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun VideoStartupOverlay(
    playbackState: String,
    videoSize: String?,
    protocol: StreamProtocol,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.width(300.dp),
        shape = RoundedCornerShape(8.dp),
        color = AnimePanel.copy(alpha = 0.84f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CircularProgressIndicator(color = AnimeAccentCyan, modifier = Modifier.size(30.dp))
            Text("正在加载画面", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text(
                listOfNotNull(
                    playbackState,
                    protocol.displayName(),
                    videoSize,
                ).joinToString(" / "),
                style = MaterialTheme.typography.bodySmall,
                color = AnimeMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TorrentPlaceholderSurface(
    stream: MediaStream,
    state: TorrentEngineState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(AnimeBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.width(560.dp).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text("\u6B63\u5728\u51C6\u5907\u64AD\u653E", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Text(
                "\u6B63\u5728\u5339\u914D\u89C6\u9891\u6587\u4EF6\u5E76\u5EFA\u7ACB\u8D77\u64AD\u7F13\u51B2\uFF0C\u5B8C\u6210\u540E\u4F1A\u81EA\u52A8\u8FDB\u5165\u64AD\u653E\u3002",
                style = MaterialTheme.typography.bodyLarge,
                color = AnimeMuted,
            )
            LinearProgressIndicator(
                progress = { (state.plan?.bufferingPercent ?: 0f) / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            Text("\u72B6\u6001: ${state.status ?: "\u7B49\u5F85\u4E2D"}", color = Color.White)
            Text(
                "\u89C6\u9891\u4FE1\u606F: ${if (state.hasMetadata) "\u5DF2\u83B7\u53D6" else "\u5339\u914D\u4E2D"}  \u64AD\u653E\u901A\u9053: ${if (state.plan?.localPlaybackUrl != null) "\u5DF2\u5C31\u7EEA" else "\u51C6\u5907\u4E2D"}",
                color = Color.White,
            )
            Text("\u6574\u4F53: ${formatPercent(state.progressPercent)}  \u89C6\u9891: ${formatPercent(state.selectedFileProgressPercent)}  \u8D77\u64AD: ${formatPercent(state.plan?.bufferingPercent ?: 0f)}", color = Color.White)
            state.plan?.takeIf { it.playbackReadyBytes > 0L }?.let { plan ->
                Text(
                    "\u8D77\u64AD\u7F13\u5B58: ${formatBytes(plan.selectedFileContiguousBytes)} / ${formatBytes(plan.playbackReadyBytes)}",
                    color = Color.White,
                )
            }
            Text("\u8FDE\u63A5: ${state.connectedPeers}  \u9AD8\u901F\u8282\u70B9: ${state.connectedSeeds}  \u901F\u5EA6: ${formatBytesPerSecond(state.downloadRateBytesPerSecond)}", color = Color.White)
            state.plan?.selectedFileName?.let { name ->
                Text("\u6587\u4EF6: $name", style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            state.plan?.selectedFileSizeBytes?.let { size ->
                Text("\u5927\u5C0F: ${formatBytes(size)}", style = MaterialTheme.typography.bodySmall, color = AnimeMuted)
            }
            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

private fun formatBytesPerSecond(bytesPerSecond: Int): String {
    return "${formatBytes(bytesPerSecond.toLong())}/s"
}

private fun formatPercent(percent: Float): String {
    return "%.1f%%".format(percent)
}

private fun normalizePlaybackDurationMs(durationMs: Long): Long {
    val maxReasonableDurationMs = 24L * 60L * 60L * 1000L
    return durationMs.takeIf { it in 1L..maxReasonableDurationMs } ?: 0L
}

private fun formatPlaybackTime(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

private fun nextDanmakuDensity(density: Float): Float {
    return when {
        density < 0.75f -> 1f
        density < 1.25f -> 1.5f
        else -> 0.5f
    }
}

private fun formatDanmakuDensity(density: Float): String {
    return when {
        density < 0.75f -> "50%"
        density < 1.25f -> "100%"
        else -> "150%"
    }
}

private fun formatPlaybackSpeed(speed: Float): String {
    val number = "%.2f".format(speed).trimEnd('0').trimEnd('.')
    return "${if (number.contains(".")) number else "$number.0"}x"
}

private fun formatPercentLabel(value: Float): String {
    return "%.0f%%".format(value.coerceIn(0f, 1f) * 100f)
}

private fun formatScaleLabel(value: Float): String {
    return "%.0f%%".format(value * 100f)
}

private fun formatBytes(bytes: Long): String {
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var index = 0
    while (value >= 1024.0 && index < units.lastIndex) {
        value /= 1024.0
        index += 1
    }
    return if (index == 0) {
        "${bytes} ${units[index]}"
    } else {
        "%.1f %s".format(value, units[index])
    }
}

private const val HOME_SCHEDULE_LIMIT = 12
private const val HOME_RECOMMENDATION_LIMIT = 10
