package com.zfbml.aggregate.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
                    onPlay = { detail, episode, stream -> screen = AppScreen.Player(detail, episode, stream) },
                )
                is AppScreen.Player -> PlayerScreen(
                    graph = graph,
                    detail = current.detail,
                    episode = current.episode,
                    stream = current.stream,
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
    data class Player(val detail: MediaDetail, val episode: Episode, val stream: MediaStream) : AppScreen
}

private enum class AppTab(val label: String, val icon: ImageVector) {
    Discover("\u9996\u9875", Icons.Filled.Home),
    Search("\u7247\u5E93", Icons.Filled.VideoLibrary),
    Sources("\u9891\u9053", Icons.Filled.Subscriptions),
    Settings("\u6211\u7684", Icons.Filled.AccountCircle),
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

@Composable
private fun DiscoverScreen(
    graph: AppGraph,
    onOpenDetail: (SearchResult) -> Unit,
    onSearch: () -> Unit,
) {
    val featured = remember { featuredOnlineResults() }
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

    val selectedDay = schedule.firstOrNull { it.weekdayId == selectedDayId }
    val selectedItems = selectedDay?.items.orEmpty()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            HomeTopBar(onSearch = onSearch)
        }
        item {
            HomeSearchEntry(onClick = onSearch)
        }
        item {
            ScheduleDaySelector(
                days = schedule,
                selectedDayId = selectedDayId,
                onSelected = { selectedDayId = it },
            )
        }
        if (loading) {
            item {
                ScheduleStatusPanel(
                    title = "\u6b63\u5728\u52a0\u8f7d\u65b0\u756a\u65f6\u95f4\u8868",
                    subtitle = "Bangumi \u6bcf\u65e5\u653e\u9001\u6570\u636e\u540c\u6b65\u4e2d",
                )
            }
        }
        error?.let { message ->
            item {
                ScheduleStatusPanel(
                    title = "\u65b0\u756a\u65f6\u95f4\u8868\u52a0\u8f7d\u5931\u8d25",
                    subtitle = message,
                )
            }
        }
        item {
            SectionHeader(
                title = selectedDay?.weekdayCn ?: "\u65b0\u756a\u65f6\u95f4\u8868",
                action = if (selectedItems.isNotEmpty()) "${selectedItems.size} \u90e8" else "",
                onAction = onSearch,
            )
        }
        if (selectedItems.isNotEmpty()) {
            items(selectedItems.take(HOME_SCHEDULE_LIMIT)) { result ->
                ScheduleAnimeRow(result = result, onClick = { onOpenDetail(result) })
            }
            if (selectedItems.size > HOME_SCHEDULE_LIMIT) {
                item {
                    ScheduleStatusPanel(
                        title = "\u5df2\u6536\u8d77\u66f4\u591a\u6761\u76ee",
                        subtitle = "\u4f7f\u7528\u641c\u7d22\u53ef\u4ee5\u66f4\u5feb\u627e\u5230\u60f3\u770b\u7684\u756a\u3002",
                    )
                }
            }
        } else {
            item {
                ScheduleStatusPanel(
                    title = "\u6682\u65e0\u5f53\u65e5\u653e\u9001\u6570\u636e",
                    subtitle = "\u5148\u5c55\u793a\u672c\u5730\u63a8\u8350\uff0c\u4f60\u4e5f\u53ef\u4ee5\u76f4\u63a5\u641c\u7d22\u756a\u540d\u3002",
                )
            }
        }
        item {
            SectionHeader(title = "\u63a8\u8350\u5165\u53e3", action = "", onAction = onSearch)
        }
        items(featured) { result ->
            WideVideoCard(result = result, onClick = { onOpenDetail(result) })
        }
    }
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
                setRequestProperty("User-Agent", "ZFBML/0.2.7")
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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AnimeBackground)
            .padding(24.dp),
    ) {
        if (maxWidth < 720.dp) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
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
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Column(
                    modifier = Modifier
                        .width(340.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SearchControls(
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = ::runSearch,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SearchHintPanel()
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ResultsHeader()
                    if (loading) {
                        CircularProgressIndicator(color = AnimeAccentCyan)
                    }
                    searchMessage?.let { message ->
                        Text(message, style = MaterialTheme.typography.bodyMedium, color = AnimeAccentAmber)
                    }
                    if (results.isEmpty() && !loading) {
                        if (searched) {
                            EmptySearchState()
                        } else {
                            AnimeFeatureShelf()
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(results) { result ->
                                ResultCard(result = result, onClick = { onOpenDetail(result) })
                            }
                        }
                    }
                }
            }
        }
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
            Text("\u7248\u672C 0.2.6", style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
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
            text = "\u627E\u7247\u770B\u7247",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Text(
            text = "\u8F93\u5165\u756A\u540D\u3001\u5267\u540D\u6216\u89C6\u9891\u94FE\u63A5",
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
            Text("\u641C\u7D22\u7247\u6E90")
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
            Text("\u5728\u8be6\u60c5\u9875\u9009\u96c6\uff0c\u518d\u7531\u5e94\u7528\u81ea\u52a8\u5339\u914d Mikan / DMHY / Nyaa \u7b49\u64ad\u653e\u7ebf\u8def\u3002", style = MaterialTheme.typography.bodySmall, color = AnimeMuted)
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
    onPlay: (MediaDetail, Episode, MediaStream) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var detail by remember(result) { mutableStateOf<MediaDetail?>(null) }
    var loading by remember(result) { mutableStateOf(true) }
    var error by remember(result) { mutableStateOf<String?>(null) }
    var selectedEpisode by remember(result) { mutableStateOf<Episode?>(null) }
    var routes by remember(result) { mutableStateOf<List<RouteCandidate>>(emptyList()) }
    var routesLoading by remember(result) { mutableStateOf(false) }
    var routesError by remember(result) { mutableStateOf<String?>(null) }

    fun loadRoutesFor(episode: Episode, autoPlay: Boolean = false) {
        selectedEpisode = episode
        routes = emptyList()
        routesError = null
        routesLoading = true
        scope.launch {
            runCatching { graph.sourceRegistry.resolveRouteCandidates(episode) }
                .onSuccess { candidates ->
                    routes = candidates
                    if (autoPlay) {
                        val media = detail
                        val firstRoute = candidates.firstOrNull()
                        if (media != null && firstRoute != null) {
                            onPlay(media, episode, firstRoute.stream)
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
            items(routes) { route ->
                RouteCandidateRow(
                    route = route,
                    onClick = {
                        val episode = selectedEpisode ?: return@RouteCandidateRow
                        onPlay(media, episode, route.stream)
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
                Text("\u70B9\u51FB\u5339\u914D Mikan / DMHY / Nyaa \u7B49\u64AD\u653E\u7EBF\u8DEF", style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1)
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
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val engine = remember { ExoPlayerEngine(context) }
    val state by engine.state.collectAsState()
    val torrentState by graph.torrentEngine.state.collectAsState()
    val torrentPlaybackUrl = torrentState.plan?.localPlaybackUrl
    var danmakuItems by remember { mutableStateOf<List<DanmakuItem>>(emptyList()) }
    var danmakuEnabled by remember { mutableStateOf(true) }
    var density by remember { mutableFloatStateOf(1f) }
    val profile = remember { DanmakuProfile(DanmakuPlatform.Bilibili, supportsAdvanced = true) }

    LaunchedEffect(stream) {
        if (stream.protocol == StreamProtocol.BITTORRENT) {
            graph.torrentEngine.prepare(stream)
        } else {
            engine.prepare(stream)
        }
        val match = graph.danmakuRegistry.matchAll(detail, episode).firstOrNull()
        danmakuItems = match?.let { graph.danmakuRegistry.provider(it.providerId)?.fetchTimeline(it) }.orEmpty()
    }
    LaunchedEffect(stream.id, torrentPlaybackUrl) {
        if (stream.protocol == StreamProtocol.BITTORRENT && torrentPlaybackUrl != null) {
            engine.prepare(
                stream.copy(
                    id = "${stream.id}:local",
                    url = torrentPlaybackUrl,
                    protocol = StreamProtocol.PROGRESSIVE,
                    headers = emptyMap(),
                ),
            )
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            engine.release()
            graph.torrentEngine.release()
        }
    }

    Box(Modifier.fillMaxSize().background(AnimeBackground)) {
        if (stream.protocol == StreamProtocol.BITTORRENT && torrentPlaybackUrl == null) {
            TorrentPlaceholderSurface(
                stream = stream,
                state = torrentState,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            PlayerViewSurface(engine = engine, modifier = Modifier.fillMaxSize())
        }
        DanmakuSurface(
            items = danmakuItems,
            playbackMsProvider = engine::currentPositionMs,
            profile = profile,
            settings = DanmakuSettings(enabled = danmakuEnabled, density = density),
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(AnimePanel.copy(alpha = 0.78f))
                .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(48.dp).focusable(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AnimePanelSoft, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("\u8FD4\u56DE")
                }
                Button(
                    onClick = {
                        if (state.isPlaying) {
                            engine.player.pause()
                        } else {
                            engine.player.play()
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp).focusable(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AnimeAccentPink, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    Icon(if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (state.isPlaying) "\u6682\u505C" else "\u64AD\u653E")
                }
                Button(
                    onClick = { danmakuEnabled = !danmakuEnabled },
                    modifier = Modifier.weight(1f).height(48.dp).focusable(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AnimePanelSoft, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    Icon(Icons.Filled.ClosedCaption, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (danmakuEnabled) "\u5F39\u5E55\u5F00" else "\u5F39\u5E55\u5173")
                }
                Button(
                    onClick = {
                        if (stream.protocol != StreamProtocol.BITTORRENT) {
                            graph.media3DownloadCoordinator.enqueue(stream, "${detail.title} ${episode.title}")
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp).focusable(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AnimePanelSoft, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    Icon(Icons.Filled.Bookmarks, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("\u79BB\u7EBF")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("\u6E05\u6670\u5EA6 ${stream.quality.orEmpty().ifBlank { "\u81EA\u52A8" }}", color = AnimeMuted, maxLines = 1)
                val errorMessage = if (stream.protocol == StreamProtocol.BITTORRENT) {
                    torrentState.errorMessage
                } else {
                    state.errorMessage
                }
                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, maxLines = 1) }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Text("\u5F39\u5E55\u5BC6\u5EA6", color = Color.White, modifier = Modifier.width(80.dp))
                Slider(
                    value = density,
                    onValueChange = { density = it },
                    valueRange = 0.2f..1.5f,
                    modifier = Modifier.weight(1f).focusable(),
                )
            }
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
