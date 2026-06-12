package com.zfbml.aggregate.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.zfbml.aggregate.AppGraph
import com.zfbml.aggregate.danmaku.DanmakuItem
import com.zfbml.aggregate.danmaku.DanmakuManualMapping
import com.zfbml.aggregate.danmaku.DanmakuMatch
import com.zfbml.aggregate.danmaku.DanmakuPlatform
import com.zfbml.aggregate.danmaku.DanmakuProfile
import com.zfbml.aggregate.danmaku.DanmakuSafeArea
import com.zfbml.aggregate.danmaku.DanmakuSettings
import com.zfbml.aggregate.danmaku.DanmakuSurface
import com.zfbml.aggregate.player.ExoPlayerEngine
import com.zfbml.aggregate.player.PlayerViewSurface
import com.zfbml.aggregate.source.Episode
import com.zfbml.aggregate.source.MediaDetail
import com.zfbml.aggregate.source.MediaStream
import com.zfbml.aggregate.source.RouteCandidate
import com.zfbml.aggregate.source.SearchResult
import com.zfbml.aggregate.source.SourceCapability
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
    var selectedTab by remember(initialQuery) {
        mutableStateOf(if (initialQuery.isNullOrBlank()) AppTab.Discover else AppTab.Search)
    }
    var screen by remember { mutableStateOf<AppScreen>(AppScreen.Main) }
    var showSplash by remember { mutableStateOf(true) }
    val splashState = remember(graph) {
        val manifests = graph.sourceRegistry.manifests
        buildBrandSplashUiState(
            sourceCount = manifests.size,
            searchableSourceCount = manifests.count { SourceCapability.SEARCH in it.capabilities },
            cacheableSourceCount = manifests.count { it.supportsDownload || SourceCapability.DOWNLOAD in it.capabilities },
            danmakuProviderCount = 4,
        )
    }
    LaunchedEffect(splashState.startupDurationMillis) {
        delay(splashState.startupDurationMillis.toLong())
        showSplash = false
    }
    Surface(modifier = Modifier.fillMaxSize(), color = AnimeBackground) {
        if (showSplash) {
            BrandSplashScreen(splashState)
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
private fun BrandSplashScreen(state: BrandSplashUiState) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val logoScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.78f,
        animationSpec = tween(durationMillis = 640, easing = FastOutSlowInEasing),
        label = "splashLogoScale",
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 720, delayMillis = 120, easing = FastOutSlowInEasing),
        label = "splashContentAlpha",
    )
    val glowScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.66f,
        animationSpec = tween(durationMillis = 760, delayMillis = 80, easing = FastOutSlowInEasing),
        label = "splashGlowScale",
    )
    val railProgress by animateFloatAsState(
        targetValue = if (started) 1f else 0.08f,
        animationSpec = tween(durationMillis = 820, delayMillis = 160, easing = FastOutSlowInEasing),
        label = "splashRailProgress",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF171018),
                        AnimeBackground,
                        Color(0xFF10141A),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(252.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AnimeAccentPink.copy(alpha = 0.18f),
                            AnimeAccentCyan.copy(alpha = 0.07f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        SplashSignalRails(
            rails = state.signalRails,
            progress = railProgress,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 78.dp)
                .alpha(contentAlpha),
        )
        SplashPosterRibbon(
            tiles = state.posterTiles,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 74.dp)
                .alpha(contentAlpha),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.alpha(contentAlpha),
        ) {
            Box(contentAlignment = Alignment.Center) {
                SplashDanmakuOrbit(
                    streaks = state.danmakuStreaks,
                    progress = railProgress,
                    modifier = Modifier
                        .size(state.orbitSize)
                        .scale(glowScale),
                )
                Box(
                    modifier = Modifier
                        .size(state.glowSize)
                        .scale(glowScale)
                        .clip(CircleShape)
                        .background(AnimeAccentCyan.copy(alpha = 0.1f)),
                )
                BrandMark(
                    modifier = Modifier
                        .size(state.logoSize)
                        .scale(logoScale),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = state.headline,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = state.brand,
                    style = MaterialTheme.typography.titleMedium,
                    color = AnimeAccentCyan,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = state.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AnimeMuted,
                )
                SplashStatusPills(pills = state.statusPills, progress = railProgress, modifier = Modifier.padding(top = 2.dp))
            }
            SplashProgressRail(label = state.progressLabel, progress = railProgress, modifier = Modifier.width(state.progressWidth))
        }
    }
}

@Composable
private fun SplashDanmakuOrbit(
    streaks: List<BrandSplashStreakUiState>,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        streaks.forEach { streak ->
            SplashDanmakuStreak(
                width = streak.width,
                color = sourceLibraryToneColor(streak.tone),
                alpha = streak.alpha,
                modifier = Modifier
                    .align(streak.anchor.toAlignment())
                    .offset(
                        x = (streak.baseOffsetX.value + progress * streak.progressOffsetX.value).dp,
                        y = streak.offsetY,
                    ),
            )
        }
    }
}

private fun BrandSplashStreakAnchor.toAlignment(): Alignment {
    return when (this) {
        BrandSplashStreakAnchor.TopStart -> Alignment.TopStart
        BrandSplashStreakAnchor.TopEnd -> Alignment.TopEnd
        BrandSplashStreakAnchor.BottomStart -> Alignment.BottomStart
        BrandSplashStreakAnchor.BottomEnd -> Alignment.BottomEnd
    }
}

@Composable
private fun SplashDanmakuStreak(
    width: Dp,
    color: Color,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(width)
            .height(4.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        color.copy(alpha = alpha),
                        Color.White.copy(alpha = alpha * 0.5f),
                        Color.Transparent,
                    ),
                ),
            ),
    )
}

@Composable
private fun SplashPosterRibbon(
    tiles: List<BrandSplashPosterTileUiState>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(58.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tiles.forEach { tile ->
            val color = sourceLibraryToneColor(tile.tone)
            Box(
                modifier = Modifier
                    .width(tile.width)
                    .height(tile.height)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = if (tile.emphasized) 0.56f else 0.28f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.BottomStart,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (tile.emphasized) 0.72f else 0.58f)
                        .height(3.dp)
                        .offset(x = 5.dp, y = (-8).dp)
                        .background(Color.White.copy(alpha = 0.62f), RoundedCornerShape(999.dp)),
                )
                Box(
                    modifier = Modifier
                        .size(if (tile.emphasized) 14.dp else 10.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.24f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(if (tile.emphasized) 6.dp else 4.dp)
                            .height(if (tile.emphasized) 8.dp else 6.dp)
                            .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(2.dp)),
                    )
                }
            }
        }
    }
}

@Composable
private fun SplashStatusPills(
    pills: List<BrandSplashStatusPillUiState>,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.widthIn(max = 286.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        pills.forEach { pill ->
            val color = sourceLibraryToneColor(pill.tone)
            val visibleProgress = (progress - pill.revealDelayFraction).coerceIn(0.38f, 1f)
            Row(
                modifier = Modifier
                    .height(26.dp)
                    .offset(y = ((1f - visibleProgress) * 4f).dp)
                    .alpha(0.62f + visibleProgress * 0.38f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(color.copy(alpha = 0.08f + visibleProgress * 0.08f))
                    .border(1.dp, color.copy(alpha = 0.18f + visibleProgress * 0.2f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.78f + visibleProgress * 0.22f)),
                )
                Text(
                    text = pill.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.72f + visibleProgress * 0.22f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}

@Composable
private fun SplashSignalRails(
    rails: List<BrandSplashSignalRailUiState>,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        rails.forEach { rail ->
            val color = sourceLibraryToneColor(rail.tone)
            Box(
                modifier = Modifier
                    .width(rail.width)
                    .height(3.dp)
                    .offset(x = ((progress.coerceIn(0f, 1f) - 0.5f) * rail.offsetRange.value).dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                color.copy(alpha = rail.startAlpha + progress.coerceIn(0f, 1f) * 0.28f),
                                Color.White.copy(alpha = 0.12f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun SplashProgressRail(
    label: String,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.1f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(AnimeAccentPink, AnimeAccentCyan),
                        ),
                    ),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.72f),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
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

private val AppTab.navigationId: String
    get() = name.lowercase()

private fun AppNavigationUiState.tabState(tab: AppTab): AppNavigationTabUiState {
    return tabs.firstOrNull { it.id == tab.navigationId } ?: AppNavigationTabUiState(
        id = tab.navigationId,
        label = tab.label,
        statusLabel = "",
        selected = selectedTabId == tab.navigationId,
        tone = SourceLibraryTone.Muted,
    )
}

private enum class PlayerPanel {
    More,
    Danmaku,
    Quality,
    Speed,
    Route,
    Episode,
}

private fun PlayerPanel.asPlayerPanelKind(): PlayerPanelKind {
    return when (this) {
        PlayerPanel.More -> PlayerPanelKind.More
        PlayerPanel.Danmaku -> PlayerPanelKind.Danmaku
        PlayerPanel.Quality -> PlayerPanelKind.Quality
        PlayerPanel.Speed -> PlayerPanelKind.Speed
        PlayerPanel.Route -> PlayerPanelKind.Route
        PlayerPanel.Episode -> PlayerPanelKind.Episode
    }
}

private fun PlayerPanelKind.asPlayerPanel(): PlayerPanel {
    return when (this) {
        PlayerPanelKind.More -> PlayerPanel.More
        PlayerPanelKind.Danmaku -> PlayerPanel.Danmaku
        PlayerPanelKind.Quality -> PlayerPanel.Quality
        PlayerPanelKind.Speed -> PlayerPanel.Speed
        PlayerPanelKind.Route -> PlayerPanel.Route
        PlayerPanelKind.Episode -> PlayerPanel.Episode
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun Activity.setPlayerImmersive(immersive: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.let { controller ->
            if (immersive) {
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsets.Type.systemBars())
            } else {
                controller.show(WindowInsets.Type.systemBars())
            }
        }
    } else {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = if (immersive) {
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        } else {
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }
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
        modifier = modifier.background(Color(0xFF191B22), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(7.dp)
                .background(Color(0xFF25242F), RoundedCornerShape(8.dp)),
        )
        Row(Modifier.fillMaxSize().padding(7.dp)) {
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
                    .background(AnimeAccentCyan, RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)),
            )
        }
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(44.dp)
                .align(Alignment.TopStart)
                .padding(start = 13.dp)
                .background(AnimeAccentAmber, RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .align(Alignment.BottomCenter)
                .padding(horizontal = 7.dp)
                .background(Color.Black.copy(alpha = 0.32f), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)),
        )
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(999.dp)),
            )
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(4.dp)
                    .background(AnimeAccentCyan.copy(alpha = 0.82f), RoundedCornerShape(999.dp)),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 26.dp, bottom = 20.dp)
                .width(52.dp)
                .height(4.dp)
                .background(AnimeAccentCyan, RoundedCornerShape(999.dp)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 12.dp)
                .width(31.dp)
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.88f), RoundedCornerShape(999.dp)),
        )
        Box(
            modifier = Modifier
                .size(55.dp)
                .background(Color.Black.copy(alpha = 0.28f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 12.dp, top = 7.dp)
                .size(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(3.dp)
                    .background(AnimeAccentAmber, RoundedCornerShape(999.dp)),
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(20.dp)
                    .background(AnimeAccentAmber, RoundedCornerShape(999.dp)),
            )
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f)),
            )
        }
        Text(
            text = "Z",
            style = MaterialTheme.typography.labelLarge,
            color = AnimeAccentCyan,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 15.dp, bottom = 10.dp),
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
    val sourceManifests = graph.sourceRegistry.manifests
    val advancedDownloadAvailable = graph.advancedDownloadProvider.isAvailable()
    val currentDayId = remember { BangumiCalendarRepository.currentBangumiWeekdayId() }
    var schedule by remember { mutableStateOf<List<BangumiScheduleDay>>(emptyList()) }
    var selectedDayId by remember { mutableStateOf(currentDayId) }
    var scheduleLoading by remember { mutableStateOf(true) }
    var scheduleError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(graph.bangumiCalendarRepository, currentDayId) {
        scheduleLoading = true
        scheduleError = null
        runCatching { graph.bangumiCalendarRepository.loadWeeklySchedule() }
            .onSuccess { days ->
                schedule = days
                val currentSelection = days.firstOrNull {
                    it.weekdayId == selectedDayId && it.items.isNotEmpty()
                }
                if (currentSelection == null) {
                    days.firstOrNull { it.items.isNotEmpty() }?.let { selectedDayId = it.weekdayId }
                }
            }
            .onFailure { failure ->
                scheduleError = failure.message ?: failure::class.simpleName.orEmpty().ifBlank { "未知错误" }
            }
        scheduleLoading = false
    }

    val scheduleUiState = remember(schedule, selectedDayId, currentDayId) {
        buildHomeScheduleUiState(
            schedule = schedule,
            selectedDayId = selectedDayId,
            currentDayId = currentDayId,
        )
    }
    val navigationState = remember(selectedTab, sourceManifests, advancedDownloadAvailable, scheduleUiState.todayCount) {
        val cacheState = buildCacheLibraryUiState(
            manifests = sourceManifests,
            advancedEngineAvailable = advancedDownloadAvailable,
        )
        buildAppNavigationUiState(
            selectedTabId = selectedTab.navigationId,
            todayCount = scheduleUiState.todayCount,
            searchableSourceCount = sourceManifests.count { SourceCapability.SEARCH in it.capabilities },
            sourceCount = sourceManifests.size,
            cacheableSourceCount = cacheState.cacheableSourceCount,
        )
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(AnimeBackground)) {
        if (maxWidth >= 840.dp) {
            Row(modifier = Modifier.fillMaxSize()) {
                AppNavigationRail(
                    navigationState = navigationState,
                    onTabSelected = onTabSelected,
                )
                MainTabContent(
                    graph = graph,
                    selectedTab = selectedTab,
                    initialQuery = initialQuery,
                    onOpenDetail = onOpenDetail,
                    onTabSelected = onTabSelected,
                    scheduleUiState = scheduleUiState,
                    selectedDayId = selectedDayId,
                    currentDayId = currentDayId,
                    onDaySelected = { selectedDayId = it },
                    scheduleLoading = scheduleLoading,
                    scheduleError = scheduleError,
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
                    scheduleUiState = scheduleUiState,
                    selectedDayId = selectedDayId,
                    currentDayId = currentDayId,
                    onDaySelected = { selectedDayId = it },
                    scheduleLoading = scheduleLoading,
                    scheduleError = scheduleError,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
                AppNavigationBar(
                    navigationState = navigationState,
                    onTabSelected = onTabSelected,
                )
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
    scheduleUiState: HomeScheduleUiState,
    selectedDayId: Int,
    currentDayId: Int,
    onDaySelected: (Int) -> Unit,
    scheduleLoading: Boolean,
    scheduleError: String?,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.background(AnimeBackground)) {
        when (selectedTab) {
            AppTab.Discover -> DiscoverScreen(
                graph = graph,
                scheduleUiState = scheduleUiState,
                selectedDayId = selectedDayId,
                currentDayId = currentDayId,
                onDaySelected = onDaySelected,
                scheduleLoading = scheduleLoading,
                scheduleError = scheduleError,
                onOpenDetail = onOpenDetail,
                onSearch = { onTabSelected(AppTab.Search) },
            )
            AppTab.Search -> SearchScreen(
                graph = graph,
                initialQuery = initialQuery,
                scheduleUiState = scheduleUiState,
                onOpenDetail = onOpenDetail,
            )
            AppTab.Sources -> SourcesScreen(graph = graph)
            AppTab.Settings -> SettingsScreen(graph = graph)
        }
    }
}

@Composable
private fun AppNavigationBar(
    navigationState: AppNavigationUiState,
    onTabSelected: (AppTab) -> Unit,
) {
    val chrome = navigationState.chrome
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AnimePanel,
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(chrome.bottomBarHeight)
                .padding(horizontal = chrome.bottomPaddingHorizontal, vertical = chrome.bottomPaddingVertical),
            horizontalArrangement = Arrangement.spacedBy(chrome.itemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppTab.entries.forEach { tab ->
                val itemState = navigationState.tabState(tab)
                AppBottomNavItem(
                    tab = tab,
                    state = itemState,
                    selected = itemState.selected,
                    chrome = chrome,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AppNavigationRail(
    navigationState: AppNavigationUiState,
    onTabSelected: (AppTab) -> Unit,
) {
    val chrome = navigationState.chrome
    val selectedColor = sourceLibraryToneColor(chrome.selectedTone)
    Surface(
        modifier = Modifier.width(chrome.railWidth).fillMaxHeight(),
        color = AnimePanel,
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = chrome.railPaddingHorizontal, vertical = chrome.railPaddingVertical),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BrandMark(Modifier.size(chrome.railBrandSize))
            Text(
                chrome.brandLabel,
                style = MaterialTheme.typography.labelMedium,
                color = AnimeAccentCyan,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                chrome.brandSubtitle,
                style = MaterialTheme.typography.labelSmall,
                color = AnimeMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            AppTab.entries.forEach { tab ->
                val itemState = navigationState.tabState(tab)
                AppRailNavItem(
                    tab = tab,
                    state = itemState,
                    selected = itemState.selected,
                    chrome = chrome,
                    onClick = { onTabSelected(tab) },
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                chrome.selectedTitle,
                style = MaterialTheme.typography.labelSmall,
                color = selectedColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                chrome.selectedSummary,
                style = MaterialTheme.typography.labelSmall,
                color = AnimeMuted,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AppBottomNavItem(
    tab: AppTab,
    state: AppNavigationTabUiState,
    selected: Boolean,
    chrome: AppNavigationChromeUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedAccent = sourceLibraryToneColor(state.tone)
    val accent = if (selected) selectedAccent else Color.White.copy(alpha = 0.62f)
    TextButton(
        onClick = onClick,
        modifier = modifier.height(chrome.bottomItemHeight).focusable(),
        shape = RoundedCornerShape(chrome.itemCornerRadius),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) selectedAccent.copy(alpha = 0.16f) else Color.Transparent,
            contentColor = accent,
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (selected) 1.dp else 4.dp),
        ) {
            Icon(tab.icon, contentDescription = state.label, modifier = Modifier.size(if (selected) 21.dp else 20.dp))
            Text(
                text = state.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            if (selected && state.statusLabel.isNotBlank()) {
                Text(
                    text = state.statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = selectedAccent.copy(alpha = 0.82f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun AppRailNavItem(
    tab: AppTab,
    state: AppNavigationTabUiState,
    selected: Boolean,
    chrome: AppNavigationChromeUiState,
    onClick: () -> Unit,
) {
    val selectedAccent = sourceLibraryToneColor(state.tone)
    val accent = if (selected) selectedAccent else Color.White.copy(alpha = 0.62f)
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(chrome.railItemHeight).focusable(),
        shape = RoundedCornerShape(chrome.itemCornerRadius),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) selectedAccent.copy(alpha = 0.16f) else Color.Transparent,
            contentColor = accent,
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Icon(tab.icon, contentDescription = state.label, modifier = Modifier.size(if (selected) 23.dp else 21.dp))
            Text(
                text = state.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Text(
                text = state.statusLabel,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) selectedAccent.copy(alpha = 0.82f) else Color.White.copy(alpha = 0.42f),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DiscoverScreen(
    graph: AppGraph,
    scheduleUiState: HomeScheduleUiState,
    selectedDayId: Int,
    currentDayId: Int,
    onDaySelected: (Int) -> Unit,
    scheduleLoading: Boolean,
    scheduleError: String?,
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
    val selectedHomePage = pages.getOrNull(pagerState.currentPage) ?: HomePage.Home
    val homeChromeState = remember(categories, selectedHomePage, showCalendar) {
        buildHomeBrowseChromeUiState(
            categories = categories,
            selectedTabId = selectedHomePage.homeBrowseTabId(),
            calendarExpanded = showCalendar,
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AnimeBackground),
    ) {
        HomePinnedHeader(
            state = homeChromeState,
            onSearch = onSearch,
            onCalendar = {
                showCalendar = !showCalendar
                scope.launch { pagerState.animateScrollToPage(0) }
            },
        )
        HomeCategoryBar(
            state = homeChromeState,
            onSelected = { tabId ->
                pages.indexOfFirst { page -> page.homeBrowseTabId() == tabId }
                    .takeIf { it >= 0 }
                    ?.let { index -> scope.launch { pagerState.animateScrollToPage(index) } }
            },
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { pageIndex ->
            when (val page = pages[pageIndex]) {
                HomePage.Home -> HomeFeedPage(
                    featured = featured,
                    homeLoading = homePicksLoading,
                    scheduleUiState = scheduleUiState,
                    selectedDayId = selectedDayId,
                    currentDayId = currentDayId,
                    onDaySelected = onDaySelected,
                    scheduleLoading = scheduleLoading,
                    scheduleError = scheduleError,
                    calendarExpanded = showCalendar,
                    onToggleCalendar = { showCalendar = !showCalendar },
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
    return listOf(HomePage.Home) + HomeBrowseCategoryOrder.mapNotNull { id ->
        byId[id]?.let { HomePage.Category(it) }
    }
}

private fun HomePage.homeBrowseTabId(): String {
    return when (this) {
        HomePage.Home -> HomeBrowseHomeTabId
        is HomePage.Category -> category.id
    }
}

@Composable
private fun HomePinnedHeader(
    state: HomeBrowseChromeUiState,
    onSearch: () -> Unit,
    onCalendar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AnimeBackground)
            .padding(horizontal = state.headerHorizontalPadding, vertical = state.headerVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(state.headerSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrandMark(Modifier.size(state.brandMarkSize))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = state.headline,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = state.brandLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = AnimeAccentCyan,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    text = state.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = AnimeMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val calendarColor = sourceLibraryToneColor(state.calendarTone)
            Button(
                onClick = onCalendar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.calendarExpanded) calendarColor else AnimePanelSoft,
                    contentColor = if (state.calendarExpanded) AnimeBackground else Color.White,
                ),
                modifier = Modifier.height(42.dp).focusable(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
            ) {
                Icon(Icons.Filled.Bookmarks, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(5.dp))
                Text(state.calendarActionLabel, maxLines = 1)
            }
        }
        Card(
            onClick = onSearch,
            modifier = Modifier.fillMaxWidth().height(state.searchHeight).focusable(),
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
                    text = state.searchPlaceholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AnimeMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(state.searchActionLabel, style = MaterialTheme.typography.labelLarge, color = AnimeAccentCyan)
            }
        }
    }
}

@Composable
private fun HomeCategoryBar(
    state: HomeBrowseChromeUiState,
    onSelected: (String) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(AnimeBackground)
            .padding(start = state.headerHorizontalPadding, end = state.headerHorizontalPadding, bottom = state.categoryBottomPadding),
        horizontalArrangement = Arrangement.spacedBy(state.categoryTabSpacing),
    ) {
        items(state.tabs) { tab ->
            val selected = tab.selected
            val accent = sourceLibraryToneColor(tab.tone)
            Card(
                onClick = { onSelected(tab.id) },
                modifier = Modifier.height(state.categoryTabHeight).focusable(),
                shape = RoundedCornerShape(state.categoryTabCornerRadius),
                colors = CardDefaults.cardColors(containerColor = if (selected) AnimePanelSoft else Color.Transparent),
                border = BorderStroke(1.dp, if (selected) accent else AnimeBorder),
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = state.categoryTabHorizontalPadding).fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) accent else AnimeMuted,
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
    scheduleUiState: HomeScheduleUiState,
    selectedDayId: Int,
    currentDayId: Int,
    onDaySelected: (Int) -> Unit,
    scheduleLoading: Boolean,
    scheduleError: String?,
    calendarExpanded: Boolean,
    onToggleCalendar: () -> Unit,
    guessBatch: Int,
    onShuffleGuess: () -> Unit,
    onOpenDetail: (SearchResult) -> Unit,
) {
    val selectedItems = scheduleUiState.selectedItems
    val feedSelection = splitSpotlightFeed(featured, spotlightCount = 5)
    val remainder = feedSelection.remainder.ifEmpty { featured.distinctBy { it.stableMediaKey() } }
    val continueItem = selectedItems.firstOrNull()
        ?: remainder.firstOrNull()
        ?: feedSelection.spotlight.firstOrNull()
    val guessItems = remainder.rotatingWindow(start = guessBatch * 5, count = 6)
    val recommendationCount = remainder.size.coerceAtLeast(feedSelection.spotlight.size)
    val watchHubState = remember(continueItem, scheduleUiState.todayCount, recommendationCount) {
        buildHomeWatchHubUiState(
            continueItem = continueItem,
            todayCount = scheduleUiState.todayCount,
            recommendationCount = recommendationCount,
        )
    }
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
        item {
            HomeWatchHub(
                state = watchHubState,
                onContinue = { continueItem?.let(onOpenDetail) },
                onCalendar = onToggleCalendar,
                onRecommendation = { feedSelection.spotlight.firstOrNull()?.let(onOpenDetail) },
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
                ScheduleDigestCard(
                    state = scheduleUiState,
                    loading = scheduleLoading,
                    error = scheduleError,
                )
            }
            item {
                ScheduleDaySelector(
                    dayChips = scheduleUiState.dayChips.ifEmpty {
                        fallbackScheduleDayChips(
                            selectedDayId = selectedDayId,
                            currentDayId = currentDayId,
                        )
                    },
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
                    title = scheduleUiState.selectedDayTitle,
                    action = scheduleUiState.selectedDayAction,
                    onAction = {},
                )
            }
            if (selectedItems.isEmpty() && !scheduleLoading) {
                item {
                    ScheduleStatusPanel(
                        title = scheduleUiState.emptyTitle,
                        subtitle = scheduleUiState.emptySubtitle,
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
                SectionHeader(title = "\u7ee7\u7eed\u89c2\u770b", action = "\u8ffd\u756a", onAction = {})
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
    val browseState = remember(category, items, fallback, loading, error) {
        buildCategoryBrowseUiState(
            category = category,
            items = items,
            fallback = fallback,
            loading = loading,
            error = error,
        )
    }
    val feedSelection = splitSpotlightFeed(items, fallback = fallback, spotlightCount = 5)
    val heroItems = feedSelection.spotlight
    val listState = remember(category, items, fallback, heroItems, loading) {
        buildCategoryBrowseListUiState(
            category = category,
            items = items,
            fallback = fallback,
            heroItems = heroItems,
            loading = loading,
        )
    }
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
            CategoryInsightStrip(state = browseState)
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
                title = listState.title,
                action = listState.actionLabel,
                onAction = {},
            )
        }
        if (listState.showEmptyState) {
            item {
                ScheduleStatusPanel(
                    title = listState.emptyTitle,
                    subtitle = listState.emptySubtitle,
                )
            }
        } else {
            items(listState.rows) { item ->
                CategoryBrowseItemRow(state = item, onClick = { onOpenDetail(item.result) })
            }
        }
    }
}

@Composable
private fun HomeWatchHub(
    state: HomeWatchHubUiState,
    onContinue: () -> Unit,
    onCalendar: () -> Unit,
    onRecommendation: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(state.cardSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        state.cards.forEach { card ->
            HomeWatchHubCard(
                state = card,
                hubState = state,
                onClick = when (card.action) {
                    HomeWatchHubAction.Continue -> onContinue
                    HomeWatchHubAction.Calendar -> onCalendar
                    HomeWatchHubAction.Recommendation -> onRecommendation
                },
                modifier = Modifier.weight(card.weight),
            )
        }
    }
}

@Composable
private fun HomeWatchHubCard(
    state: HomeWatchHubCardUiState,
    hubState: HomeWatchHubUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = sourceLibraryToneColor(state.tone)
    val icon = state.action.homeWatchHubIcon()
    Card(
        onClick = onClick,
        enabled = state.enabled,
        modifier = modifier.height(hubState.cardHeight).focusable(),
        shape = RoundedCornerShape(hubState.cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = if (state.enabled) AnimePanel else Color.White.copy(alpha = 0.035f),
            disabledContainerColor = Color.White.copy(alpha = 0.035f),
        ),
        border = BorderStroke(1.dp, if (state.enabled) accent.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.06f)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(hubState.cardPadding),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    state.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (state.enabled) Color.White else Color.White.copy(alpha = 0.42f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (state.enabled) accent else Color.White.copy(alpha = 0.28f),
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                state.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (state.enabled) AnimeMuted else Color.White.copy(alpha = 0.32f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun HomeWatchHubAction.homeWatchHubIcon(): ImageVector {
    return when (this) {
        HomeWatchHubAction.Continue -> Icons.Filled.PlayArrow
        HomeWatchHubAction.Calendar -> Icons.Filled.Bookmarks
        HomeWatchHubAction.Recommendation -> Icons.Filled.VideoLibrary
    }
}

@Composable
private fun HomeHeroCarousel(
    title: String,
    items: List<SearchResult>,
    onOpenDetail: (SearchResult) -> Unit,
) {
    val state = remember(title, items) {
        buildHomeSpotlightCarouselUiState(
            title = title,
            items = items,
            fallback = featuredOnlineResults(),
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(state.title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text(state.helper, style = MaterialTheme.typography.bodySmall, color = AnimeMuted)
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cardWidth = (maxWidth * state.cardWidthFraction)
                .coerceAtLeast(state.minCardWidth)
                .coerceAtMost(state.maxCardWidth)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(state.itemSpacing)) {
                items(state.cards) { card ->
                    HeroCarouselCard(
                        state = card,
                        carouselState = state,
                        onClick = { onOpenDetail(card.result) },
                        modifier = Modifier.width(cardWidth).height(state.cardHeight),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCarouselCard(
    state: HomeSpotlightCardUiState,
    carouselState: HomeSpotlightCarouselUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val result = state.result
    val accent = providerAccent(result.providerId)
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.focusable(),
        shape = RoundedCornerShape(carouselState.cardCornerRadius),
        colors = CardDefaults.elevatedCardColors(containerColor = AnimePanel),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().background(AnimePanelSoft)) {
            val companion = state.companion
            val showCompanion = companion != null && maxWidth >= carouselState.companionMinWidth
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
                modifier = Modifier.fillMaxSize().padding(carouselState.cardPadding),
                horizontalArrangement = Arrangement.spacedBy(carouselState.contentSpacing),
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
                            text = state.focusLabel,
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
                            VideoMetaChip(state.primaryChip)
                            VideoMetaChip(state.secondaryChip)
                        }
                        Text(
                            state.title,
                            style = titleStyle,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            state.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AnimeMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = AnimeAccentCyan, modifier = Modifier.size(22.dp))
                        Text(state.actionLabel, style = MaterialTheme.typography.labelLarge, color = AnimeAccentCyan)
                    }
                }
                companion?.takeIf { showCompanion }?.let { next ->
                    Column(
                        modifier = Modifier.width(carouselState.companionColumnWidth).fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(state.companionLabel, style = MaterialTheme.typography.labelSmall, color = AnimeMuted, fontWeight = FontWeight.Bold)
                        PosterArtwork(
                            posterUrl = next.posterUrl,
                            accent = providerAccent(next.providerId),
                            modifier = Modifier.size(
                                width = carouselState.companionPosterWidth,
                                height = carouselState.companionPosterHeight,
                            ),
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
    val state = remember(items) {
        buildHomePosterRailUiState(
            items = items,
            fallback = featuredOnlineResults(),
        )
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(state.itemSpacing)) {
        items(state.items) { item ->
            PosterVideoCard(state = item, railState = state, onClick = { onOpenDetail(item.result) })
        }
    }
}

@Composable
private fun CategoryInsightStrip(state: CategoryBrowseUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(state.headerSpacing), modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = state.headline,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = state.summary,
                style = MaterialTheme.typography.bodySmall,
                color = AnimeMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(state.metricSpacing), modifier = Modifier.fillMaxWidth()) {
            items(state.metrics) { metric ->
                InsightTile(metric)
            }
        }
    }
}

@Composable
private fun InsightTile(state: CategoryBrowseMetricUiState) {
    val accent = sourceLibraryToneColor(state.tone)
    Card(
        modifier = Modifier.width(state.width).height(state.height),
        shape = RoundedCornerShape(state.cornerRadius),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Column(Modifier.fillMaxSize().padding(state.padding), verticalArrangement = Arrangement.SpaceBetween) {
            Text(state.value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(state.label, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun CategoryBrowseItemRow(state: CategoryBrowseItemUiState, onClick: () -> Unit) {
    val accent = sourceLibraryToneColor(state.tone)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().focusable(),
        shape = RoundedCornerShape(state.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(state.rowPadding),
            horizontalArrangement = Arrangement.spacedBy(state.rowSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PosterArtwork(
                posterUrl = state.result.posterUrl,
                accent = accent.copy(alpha = 0.72f),
                modifier = Modifier.size(width = state.posterWidth, height = state.posterHeight),
                shape = RoundedCornerShape(state.posterCornerRadius),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(state.contentSpacing)) {
                Text(state.title, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(state.subtitle, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(state.chipSpacing)) {
                    items(state.chips) { chip ->
                        RouteStatusBadge(chip.label, sourceLibraryToneColor(chip.tone))
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(state.contentSpacing)) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = accent, modifier = Modifier.size(state.actionIconSize))
                Text(state.actionLabel, style = MaterialTheme.typography.labelLarge, color = accent)
            }
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
    val state = remember(result, dayLabel) { buildHomeScheduleHeroUiState(result, dayLabel) }
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(state.height).focusable(),
        shape = RoundedCornerShape(state.cornerRadius),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1D2028)),
    ) {
        Box(Modifier.fillMaxSize()) {
            PosterArtwork(
                posterUrl = state.result.posterUrl,
                accent = AnimeAccentViolet.copy(alpha = 0.34f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(width = state.posterWidth, height = state.height),
                shape = RoundedCornerShape(topEnd = state.cornerRadius, bottomEnd = state.cornerRadius),
            )
            Column(
                modifier = Modifier.fillMaxSize().padding(state.contentPadding),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(state.contentSpacing)) {
                    Text(state.dayLabel, style = MaterialTheme.typography.labelLarge, color = AnimeAccentAmber)
                    Text(state.title, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2)
                    Text(
                        state.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AnimeMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(state.actionSpacing), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AnimeAccentPink, contentColor = Color.White),
                        modifier = Modifier.focusable(),
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(state.actionLabel)
                    }
                    state.chips.forEach { chip ->
                        RouteStatusBadge(chip.label, sourceLibraryToneColor(chip.tone))
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleDigestCard(
    state: HomeScheduleUiState,
    loading: Boolean,
    error: String?,
) {
    val digestState = remember(state, loading, error) { buildHomeScheduleDigestUiState(state, loading, error) }
    val accent = if (error != null) MaterialTheme.colorScheme.error else sourceLibraryToneColor(digestState.tone)
    Surface(
        modifier = Modifier.fillMaxWidth().focusable(),
        color = AnimePanel,
        shape = RoundedCornerShape(digestState.cornerRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(digestState.padding),
            verticalArrangement = Arrangement.spacedBy(digestState.contentSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(digestState.contentSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(digestState.iconBoxSize)
                        .clip(RoundedCornerShape(digestState.cornerRadius))
                        .background(accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (digestState.showProgress) {
                        CircularProgressIndicator(color = accent, strokeWidth = 2.dp, modifier = Modifier.size(digestState.iconSize))
                    } else {
                        Icon(Icons.Filled.Bookmarks, contentDescription = null, tint = accent, modifier = Modifier.size(digestState.iconSize))
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = digestState.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = digestState.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = AnimeMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(digestState.chipSpacing)) {
                items(digestState.chips) { chip ->
                    RouteStatusBadge(chip.label, sourceLibraryToneColor(chip.tone))
                }
            }
        }
    }
}

@Composable
private fun ScheduleDaySelector(
    dayChips: List<ScheduleDayChipUiState>,
    onSelected: (Int) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(dayChips) { day ->
            val accent = sourceLibraryToneColor(day.tone)
            Card(
                onClick = { onSelected(day.weekdayId) },
                modifier = Modifier.width(day.width).height(day.height).focusable(),
                shape = RoundedCornerShape(day.cornerRadius),
                colors = CardDefaults.cardColors(containerColor = if (day.selected) AnimePanelSoft else AnimePanel),
                border = BorderStroke(1.dp, if (day.selected || day.today) accent else AnimeBorder),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = day.horizontalPadding, vertical = day.verticalPadding),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (day.today) {
                            Box(
                                modifier = Modifier
                                    .size(day.todayDotSize)
                                    .clip(CircleShape)
                                    .background(AnimeAccentPink),
                            )
                        }
                        Text(day.label, style = MaterialTheme.typography.labelLarge, color = Color.White, maxLines = 1)
                    }
                    Text(day.countLabel, style = MaterialTheme.typography.bodySmall, color = if (day.selected) accent else AnimeMuted, maxLines = 1)
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
    val state = remember(result) { buildHomeScheduleAnimeRowUiState(result) }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().focusable(),
        shape = RoundedCornerShape(state.cornerRadius),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(state.rowPadding),
            horizontalArrangement = Arrangement.spacedBy(state.rowSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PosterArtwork(
                posterUrl = state.result.posterUrl,
                accent = AnimeAccentViolet,
                modifier = Modifier.size(width = state.posterWidth, height = state.posterHeight),
                shape = RoundedCornerShape(state.posterCornerRadius),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(state.contentSpacing)) {
                Text(state.title, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(state.subtitle, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.chips.forEach { chip ->
                        RouteStatusBadge(chip.label, sourceLibraryToneColor(chip.tone))
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(state.actionSpacing)) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = AnimeAccentCyan, modifier = Modifier.size(state.actionIconSize))
                Text(state.actionLabel, style = MaterialTheme.typography.labelLarge, color = AnimeAccentCyan)
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

private fun fallbackScheduleDayChips(
    selectedDayId: Int,
    currentDayId: Int,
): List<ScheduleDayChipUiState> {
    return fallbackScheduleDays().map { day ->
        ScheduleDayChipUiState(
            weekdayId = day.weekdayId,
            label = day.weekdayCn.removePrefix("\u661f\u671f"),
            count = 0,
            selected = day.weekdayId == selectedDayId,
            today = day.weekdayId == currentDayId,
            tone = when {
                day.weekdayId == selectedDayId -> SourceLibraryTone.Online
                day.weekdayId == currentDayId -> SourceLibraryTone.Primary
                else -> SourceLibraryTone.Muted
            },
        )
    }
}

@Composable
private fun ContinueWatchingRow(result: SearchResult, onClick: () -> Unit) {
    val state = remember(result) { buildHomeContinueWatchingUiState(result) }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().focusable(),
        shape = RoundedCornerShape(state.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(state.contentPadding),
            horizontalArrangement = Arrangement.spacedBy(state.rowSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PosterArtwork(
                posterUrl = state.result.posterUrl,
                accent = AnimeAccentCyan,
                modifier = Modifier.size(width = state.posterWidth, height = state.posterHeight),
                shape = RoundedCornerShape(state.posterCornerRadius),
                icon = Icons.Filled.PlayArrow,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(state.textSpacing)) {
                Text(state.eyebrow, style = MaterialTheme.typography.labelLarge, color = AnimeAccentCyan)
                Text(state.title, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(state.subtitle, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                LinearProgressIndicator(progress = { state.progressFraction }, modifier = Modifier.fillMaxWidth(), color = AnimeAccentPink)
                Text(state.progressLabel, style = MaterialTheme.typography.bodySmall, color = AnimeMuted)
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = remember(title, action) { buildHomeSectionHeaderUiState(title, action) }
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = state.height),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(state.title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        if (state.actionLabel.isNotBlank()) {
            TextButton(onClick = onAction, modifier = Modifier.focusable()) {
                Text(state.actionLabel, color = sourceLibraryToneColor(state.actionTone))
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
                setRequestProperty("User-Agent", "ZFBML/0.5.196")
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
private fun PosterVideoCard(
    state: HomePosterCardUiState,
    railState: HomePosterRailUiState,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(railState.cardWidth).focusable(),
        shape = RoundedCornerShape(railState.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(railState.cardContentSpacing)) {
            PosterArtwork(
                posterUrl = state.result.posterUrl,
                accent = sourceLibraryToneColor(state.tone),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(railState.posterHeight),
            )
            Text(state.title, style = MaterialTheme.typography.titleSmall, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(state.subtitle, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
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

private fun defaultSearchKeywords(): List<String> = listOf(
    "\u5b64\u72ec\u6447\u6eda",
    "\u51e1\u4eba\u4fee\u4ed9\u4f20",
    "\u9b3c\u706d\u4e4b\u5203",
    "\u9b54\u6cd5\u5c11\u5973\u5c0f\u5706",
    "\u590f\u76ee\u53cb\u4eba\u5e10",
)

@Composable
private fun SearchScreen(
    graph: AppGraph,
    initialQuery: String?,
    scheduleUiState: HomeScheduleUiState,
    onOpenDetail: (SearchResult) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var query by remember(initialQuery) { mutableStateOf(initialQuery?.takeIf(String::isNotBlank).orEmpty()) }
    var results by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    var searchMessage by remember { mutableStateOf<String?>(null) }
    var searchReport by remember { mutableStateOf<SourceSearchReport?>(null) }
    var selectedSearchProviderId by remember { mutableStateOf<String?>(null) }
    val searchIndexUiState = buildSearchIndexUiState(
        manifests = graph.sourceRegistry.manifests,
        report = searchReport,
        results = results,
        selectedProviderId = selectedSearchProviderId,
    )
    val searchLandingUiState = remember(scheduleUiState, searchIndexUiState.searchableSourceCount) {
        buildSearchLandingUiState(
            scheduleState = scheduleUiState,
            searchableSourceCount = searchIndexUiState.searchableSourceCount,
            fallbackKeywords = defaultSearchKeywords(),
        )
    }
    val searchIdleHintUiState = remember(searchLandingUiState, searchIndexUiState) {
        buildSearchIdleHintUiState(
            landingState = searchLandingUiState,
            indexState = searchIndexUiState,
        )
    }
    val visibleResults = searchResultsForProvider(results, searchIndexUiState.selectedProviderId)
    val searchResultsSectionUiState = remember(searchIndexUiState, visibleResults.size, loading, searched) {
        buildSearchResultsSectionUiState(
            indexState = searchIndexUiState,
            visibleResultCount = visibleResults.size,
            loading = loading,
            searched = searched,
        )
    }

    fun runSearch(searchTerm: String = query) {
        val normalizedQuery = searchTerm.trim()
        if (normalizedQuery.isBlank()) return
        query = normalizedQuery
        scope.launch {
            searched = true
            loading = true
            results = emptyList()
            searchMessage = null
            searchReport = null
            selectedSearchProviderId = null
            runCatching { graph.sourceRegistry.searchAllWithReport(normalizedQuery) }
                .onSuccess { report ->
                    results = report.results
                    searchReport = report
                    searchMessage = report.statusMessage()
                }
                .onFailure { error ->
                    results = emptyList()
                    searchReport = null
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
                Text(searchLandingUiState.headline, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text(searchLandingUiState.summary, style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
            }
        }
        item {
            SearchControls(
                state = searchLandingUiState,
                query = query,
                onQueryChange = { query = it },
                onSearch = ::runSearch,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (!searched && results.isEmpty()) {
            item {
                SearchSuggestionStrip(
                    title = searchLandingUiState.suggestionTitle,
                    suggestions = searchLandingUiState.suggestions,
                    onSelected = { runSearch(it) },
                )
            }
        }
        if (searched || loading) {
            item {
                SearchIndexOverviewCard(
                    state = searchIndexUiState,
                    onSourceSelected = { sourceId ->
                        selectedSearchProviderId = sourceId
                    },
                )
            }
        }
        item {
            ResultsHeader(state = searchResultsSectionUiState)
        }
        searchStatusItems(
            loading = loading,
            searched = searched,
            searchMessage = searchMessage,
            results = visibleResults,
            idleHintState = searchIdleHintUiState,
            sectionState = searchResultsSectionUiState,
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
    val sourceLibraryState = remember(providers) { buildSourceLibraryUiState(providers) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SourceLibraryHero(state = sourceLibraryState)
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(sourceLibraryState.strategies, key = { it.id }) { strategy ->
                    SourceStrategyCard(strategy)
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(sourceLibraryState.sourceListTitle, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Text(sourceLibraryState.sourceListSummary, style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
            }
        }
        if (sourceLibraryState.sourceCards.isEmpty()) {
            item {
                ScheduleStatusPanel(
                    title = sourceLibraryState.emptyTitle,
                    subtitle = sourceLibraryState.emptySubtitle,
                )
            }
        }
        items(sourceLibraryState.sourceCards, key = { it.id }) { source ->
            SourceCard(
                state = source,
                accent = providerAccent(source.id),
            )
        }
    }
}

@Composable
private fun SourceLibraryHero(
    state: SourceLibraryUiState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AnimePanel,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrandMark(modifier = Modifier.size(68.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(state.headline, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Text(state.summary, style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    items(state.chips) { chip ->
                        RouteStatusBadge(chip.label, sourceLibraryToneColor(chip.tone))
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceStrategyCard(
    strategy: SourceStrategyUiState,
) {
    val accent = sourceLibraryToneColor(strategy.tone)
    Card(
        modifier = Modifier.width(150.dp).height(96.dp).focusable(),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(strategy.title, style = MaterialTheme.typography.labelMedium, color = AnimeMuted, maxLines = 1)
            Text(strategy.value, style = MaterialTheme.typography.titleLarge, color = accent, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(strategy.subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.72f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SourceCard(
    state: SourceCardUiState,
    accent: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth().focusable(),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)).background(accent.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(if (state.isBt) Icons.Filled.Subscriptions else Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(state.name, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    RouteStatusBadge(state.statusLabel, sourceLibraryToneColor(state.statusTone))
                }
                Text(state.featureText, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.78f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(state.domainText, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(state.version, style = MaterialTheme.typography.labelMedium, color = accent, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(state.author, style = MaterialTheme.typography.labelSmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

private fun sourceLibraryToneColor(tone: SourceLibraryTone): Color {
    return when (tone) {
        SourceLibraryTone.Primary -> AnimeAccentPink
        SourceLibraryTone.Online -> AnimeAccentCyan
        SourceLibraryTone.Backup -> AnimeAccentAmber
        SourceLibraryTone.Cache -> AnimeAccentGreen
        SourceLibraryTone.Web -> AnimeAccentViolet
        SourceLibraryTone.Muted -> AnimeMuted
    }
}

private fun playerChromeBaseColor(color: PlayerChromeBaseColor): Color {
    return when (color) {
        PlayerChromeBaseColor.Black -> Color.Black
        PlayerChromeBaseColor.White -> Color.White
    }
}

private fun animeSurfaceBaseColor(color: AnimeSurfaceBaseColor): Color {
    return when (color) {
        AnimeSurfaceBaseColor.Panel -> AnimePanel
        AnimeSurfaceBaseColor.PanelSoft -> AnimePanelSoft
        AnimeSurfaceBaseColor.Border -> AnimeBorder
    }
}

@Composable
private fun playerNoticeColor(state: PlayerNoticeUiState): Color {
    return if (state.error) MaterialTheme.colorScheme.error else sourceLibraryToneColor(state.tone)
}

@Composable
private fun playerRouteStatusColor(state: PlayerRouteStatusUiState): Color {
    return if (state.error) MaterialTheme.colorScheme.error else sourceLibraryToneColor(state.tone)
}

@Composable
private fun CacheScreen(graph: AppGraph) {
    val cacheState = remember(graph.sourceRegistry.manifests) {
        buildCacheLibraryUiState(
            manifests = graph.sourceRegistry.manifests,
            advancedEngineAvailable = graph.advancedDownloadProvider.isAvailable(),
        )
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            CacheLibraryHero(state = cacheState)
        }
        items(cacheState.capabilities, key = { it.id }) { capability ->
            CacheCapabilityPanel(capability = capability)
        }
    }
}

@Composable
private fun SettingsScreen(graph: AppGraph) {
    val sourceCount = graph.sourceRegistry.manifests.size
    val danmakuCount = graph.danmakuRegistry.profiles.size
    val cacheState = remember(graph.sourceRegistry.manifests) {
        buildCacheLibraryUiState(
            manifests = graph.sourceRegistry.manifests,
            advancedEngineAvailable = graph.advancedDownloadProvider.isAvailable(),
        )
    }
    val profileState = remember(sourceCount, danmakuCount, cacheState) {
        buildProfileCenterUiState(
            version = "0.5.196",
            sourceCount = sourceCount,
            danmakuCount = danmakuCount,
            cacheState = cacheState,
        )
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ProfileHeroCard(state = profileState)
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(profileState.quickActions, key = { it.id }) { action ->
                    ProfileQuickCard(action = action)
                }
            }
        }
        item {
            CacheLibraryHero(state = cacheState)
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(cacheState.capabilities, key = { it.id }) { capability ->
                    CacheCapabilityMiniCard(capability = capability)
                }
            }
        }
        item {
            Text("播放体验", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        }
        items(profileState.settings, key = { it.id }) { setting ->
            ProfileSettingRow(setting = setting)
        }
    }
}

@Composable
private fun CacheLibraryHero(
    state: CacheLibraryUiState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().focusable(),
        color = AnimePanel,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(58.dp).clip(RoundedCornerShape(8.dp)).background(AnimeAccentGreen.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = AnimeAccentGreen, modifier = Modifier.size(28.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(state.headline, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Text(state.summary, style = MaterialTheme.typography.bodyMedium, color = AnimeMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    items(state.chips) { chip ->
                        RouteStatusBadge(chip.label, sourceLibraryToneColor(chip.tone))
                    }
                }
            }
        }
    }
}

@Composable
private fun CacheCapabilityPanel(capability: CacheCapabilityUiState) {
    StatusPanel(
        title = capability.title,
        subtitle = capability.subtitle,
        value = capability.value,
        accent = sourceLibraryToneColor(capability.tone),
    )
}

@Composable
private fun CacheCapabilityMiniCard(capability: CacheCapabilityUiState) {
    val accent = sourceLibraryToneColor(capability.tone)
    Card(
        modifier = Modifier.width(154.dp).height(96.dp).focusable(),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(capability.title, style = MaterialTheme.typography.labelMedium, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(capability.value, style = MaterialTheme.typography.titleLarge, color = accent, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(capability.subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.72f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ProfileHeroCard(
    state: ProfileCenterUiState,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().focusable(),
        color = AnimePanel,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrandMark(modifier = Modifier.size(66.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(state.headline, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text(state.summary, style = MaterialTheme.typography.bodyMedium, color = AnimeMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    items(state.chips) { chip ->
                        RouteStatusBadge(chip.label, sourceLibraryToneColor(chip.tone))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileQuickCard(
    action: ProfileQuickActionUiState,
) {
    val accent = sourceLibraryToneColor(action.tone)
    Card(
        modifier = Modifier.width(136.dp).height(92.dp).focusable(),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(profileEntryIcon(action.id), contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            Text(action.title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(action.subtitle, style = MaterialTheme.typography.labelSmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ProfileSettingRow(
    setting: ProfileSettingUiState,
) {
    val accent = sourceLibraryToneColor(setting.tone)
    Card(
        modifier = Modifier.fillMaxWidth().focusable(),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(profileEntryIcon(setting.id), contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(setting.title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(setting.subtitle, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(setting.value, style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

private fun profileEntryIcon(id: String): ImageVector {
    return when (id) {
        "cache" -> Icons.Filled.CloudDownload
        "danmaku" -> Icons.Filled.ClosedCaption
        "sources" -> Icons.Filled.VideoLibrary
        else -> Icons.Filled.PlayArrow
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
    state: SearchLandingUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(AnimeAccentPink),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(state.inputTitle, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(state.inputSubtitle, style = MaterialTheme.typography.bodySmall, color = AnimeMuted)
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(state.inputPlaceholder) },
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .focusable(),
                colors = ButtonDefaults.buttonColors(containerColor = AnimeAccentPink, contentColor = Color.White),
                onClick = onSearch,
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("\u5f00\u59cb\u641c\u7d22")
            }
        }
    }
}

@Composable
private fun SearchSuggestionStrip(
    title: String,
    suggestions: List<SearchSuggestionUiState>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(suggestions) { suggestion ->
                val accent = sourceLibraryToneColor(suggestion.tone)
                TextButton(
                    onClick = { onSelected(suggestion.keyword) },
                    modifier = Modifier.height(56.dp).widthIn(min = 132.dp, max = 210.dp).focusable(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        contentColor = Color.White,
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(suggestion.keyword, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(suggestion.subtitle, style = MaterialTheme.typography.labelSmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHintPanel(state: SearchIdleHintUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = AnimeAccentCyan, modifier = Modifier.size(18.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(state.title, style = MaterialTheme.typography.titleSmall, color = Color.White)
                    Text(state.subtitle, style = MaterialTheme.typography.bodySmall, color = AnimeMuted)
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(state.chips) { chip ->
                    RouteStatusBadge(chip.label, sourceLibraryToneColor(chip.tone))
                }
            }
            Text(state.actionLabel, style = MaterialTheme.typography.labelMedium, color = AnimeAccentCyan)
        }
    }
}

@Composable
private fun SearchIndexOverviewCard(
    state: SearchIndexUiState,
    onSourceSelected: (String?) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = state.headline,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = state.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = AnimeMuted,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = state.resultCount.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = AnimeAccentCyan,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "\u7ed3\u679c",
                        style = MaterialTheme.typography.labelSmall,
                        color = AnimeMuted,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SearchIndexMetric(
                    label = "\u6765\u6e90",
                    value = state.searchableSourceCount.toString(),
                    accent = AnimeAccentCyan,
                    modifier = Modifier.weight(1f),
                )
                SearchIndexMetric(
                    label = "\u5f02\u5e38",
                    value = state.failedSourceCount.toString(),
                    accent = if (state.failedSourceCount > 0) AnimeAccentAmber else AnimeMuted,
                    modifier = Modifier.weight(1f),
                )
                SearchIndexMetric(
                    label = "\u7b5b\u9009",
                    value = if (state.selectedProviderId == null) "\u5168\u90e8" else "1",
                    accent = AnimeAccentPink,
                    modifier = Modifier.weight(1f),
                )
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.sourceFilters, key = { it.id }) { filter ->
                    SearchSourceFilterButton(
                        filter = filter,
                        onClick = {
                            onSourceSelected(if (filter.isAll) null else filter.id)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchIndexMetric(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .border(BorderStroke(1.dp, AnimeBorder), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = accent, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelSmall, color = AnimeMuted, maxLines = 1)
    }
}

@Composable
private fun SearchSourceFilterButton(
    filter: SearchSourceFilterUiState,
    onClick: () -> Unit,
) {
    val borderColor = when {
        filter.selected -> AnimeAccentCyan
        filter.failed -> AnimeAccentAmber.copy(alpha = 0.72f)
        filter.resultCount > 0 -> AnimeAccentPink.copy(alpha = 0.58f)
        else -> AnimeBorder
    }
    Surface(
        modifier = Modifier
            .widthIn(min = 122.dp, max = 188.dp)
            .heightIn(min = 54.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (filter.selected) AnimePanelSoft else Color.Transparent,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = filter.name,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${filter.statusLabel} \u00b7 ${filter.capabilityLabel}",
                style = MaterialTheme.typography.labelSmall,
                color = if (filter.failed) AnimeAccentAmber else AnimeMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ResultsHeader(state: SearchResultsSectionUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(state.headerTitle, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Text(state.headerSubtitle, style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.searchStatusItems(
    loading: Boolean,
    searched: Boolean,
    searchMessage: String?,
    results: List<SearchResult>,
    idleHintState: SearchIdleHintUiState,
    sectionState: SearchResultsSectionUiState,
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
                EmptySearchState(state = sectionState)
            }
        } else {
            item {
                SearchHintPanel(idleHintState)
            }
        }
    } else {
        items(results) { result ->
            ResultCard(state = buildSearchResultCardUiState(result), onClick = { onOpenDetail(result) })
        }
    }
}

@Composable
private fun EmptySearchState(state: SearchResultsSectionUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(state.emptyTitle, style = MaterialTheme.typography.titleSmall, color = Color.White)
            Text(state.emptySubtitle, style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
        }
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
            ResultCard(state = buildSearchResultCardUiState(item), onClick = {})
        }
    }
}

private fun featureShelfItems(): List<SearchResult> = featuredOnlineResults()

@Composable
private fun ResultCard(state: SearchResultCardUiState, onClick: () -> Unit) {
    val accent = sourceLibraryToneColor(state.tone)
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
                posterUrl = state.posterUrl,
                accent = providerAccent(state.providerId),
                modifier = Modifier
                    .width(112.dp)
                    .height(64.dp),
                shape = RoundedCornerShape(6.dp),
                icon = Icons.Filled.PlayArrow,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        state.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    RouteStatusBadge(state.typeLabel, accent)
                }
                Text(state.subtitle, style = MaterialTheme.typography.bodyMedium, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(state.chips) { chip ->
                        RouteStatusBadge(chip.label, sourceLibraryToneColor(chip.tone))
                    }
                }
            }
            Column(
                modifier = Modifier.widthIn(max = 96.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(state.providerLabel, style = MaterialTheme.typography.labelMedium, color = accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(state.actionLabel, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.72f), maxLines = 1)
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
    var routesFromCache by remember(result) { mutableStateOf(false) }
    var routesExpanded by remember(result) { mutableStateOf(false) }
    var routePrefetchingEpisodeIds by remember(result) { mutableStateOf<Set<String>>(emptySet()) }
    var routePrefetchedEpisodeIds by remember(result) { mutableStateOf<Set<String>>(emptySet()) }
    var routePrefetchEmptyEpisodeIds by remember(result) { mutableStateOf<Set<String>>(emptySet()) }

    fun loadRoutesFor(episode: Episode, autoPlay: Boolean = false) {
        val cachedRoutes = graph.sourceRegistry.peekRouteCandidates(episode)
            ?.let { sortRoutesForUi(it) }
        selectedEpisode = episode
        routes = cachedRoutes ?: emptyList()
        routesError = null
        routeSourceFilter = cachedRoutes?.let { recommendedSourceIdForRoutes(it) }
        routesFromCache = cachedRoutes != null
        routesExpanded = false
        routesLoading = cachedRoutes == null
        if (cachedRoutes != null) {
            if (autoPlay) {
                val media = detail
                val firstRoute = firstPlayableRouteForAutoplay(cachedRoutes)
                if (media != null && firstRoute != null) {
                    onPlay(media, episode, firstRoute.stream, cachedRoutes)
                }
            }
            return
        }
        scope.launch {
            runCatching { graph.sourceRegistry.resolveRouteCandidates(episode) }
                .onSuccess { candidates ->
                    if (selectedEpisode?.id != episode.id) return@onSuccess
                    val sortedCandidates = sortRoutesForUi(candidates)
                    routes = sortedCandidates
                    routeSourceFilter = recommendedSourceIdForRoutes(sortedCandidates)
                    routesFromCache = false
                    if (autoPlay) {
                        val media = detail
                        val firstRoute = firstPlayableRouteForAutoplay(sortedCandidates)
                        if (media != null && firstRoute != null) {
                            onPlay(media, episode, firstRoute.stream, sortedCandidates)
                        }
                    }
                }
                .onFailure { failure ->
                    if (selectedEpisode?.id != episode.id) return@onFailure
                    routesError = failure.message ?: failure::class.simpleName.orEmpty().ifBlank { "\u672a\u77e5\u9519\u8bef" }
                    routesFromCache = false
                }
            if (selectedEpisode?.id == episode.id) {
                routesLoading = false
            }
        }
    }

    LaunchedEffect(result) {
        loading = true
        error = null
        routes = emptyList()
        routesError = null
        routeSourceFilter = null
        routesFromCache = false
        routesExpanded = false
        routesLoading = false
        routePrefetchingEpisodeIds = emptySet()
        routePrefetchedEpisodeIds = emptySet()
        routePrefetchEmptyEpisodeIds = emptySet()
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

    val routeUiState = buildRouteUiState(
        selectedEpisode = selectedEpisode,
        routes = routes,
        loading = routesLoading,
        error = routesError,
        selectedSourceId = routeSourceFilter,
        loadedFromCache = routesFromCache,
    )
    val detailEntryState = buildDetailEntryUiState(
        result = result,
        detail = detail,
        loading = loading,
        error = error,
    )
    val detailPlaybackReadiness = buildDetailPlaybackReadinessUiState(routeUiState)
    val detailHeroActionState = buildDetailHeroActionUiState(
        selectedEpisode = selectedEpisode,
        routeState = routeUiState,
    )
    val detailFirstPlayState = buildDetailFirstPlayUiState(
        selectedEpisode = selectedEpisode,
        routeState = routeUiState,
    )
    val detailRouteResolutionState = buildDetailRouteResolutionUiState(routeUiState)
    val detailRouteStatusState = buildDetailRouteStatusUiState(
        routeState = routeUiState,
        expanded = routesExpanded,
    )
    val routePrefetchUiState = detail?.let { media ->
        buildRoutePrefetchUiState(
            episodes = media.episodes,
            currentEpisode = selectedEpisode,
            warmingEpisodeIds = routePrefetchingEpisodeIds,
            warmedEpisodeIds = routePrefetchedEpisodeIds,
            emptyEpisodeIds = routePrefetchEmptyEpisodeIds,
        )
    } ?: buildRoutePrefetchUiState(emptyList(), null)
    LaunchedEffect(detail?.url, selectedEpisode?.id, routesLoading, routesError, routes) {
        val media = detail ?: return@LaunchedEffect
        val episode = selectedEpisode ?: return@LaunchedEffect
        if (routesLoading || routesError != null || routes.isEmpty()) return@LaunchedEffect
        routePrefetchWindow(media.episodes, episode).forEach { prefetchEpisode ->
            val prefetchId = prefetchEpisode.id
            if (graph.sourceRegistry.peekRouteCandidates(prefetchEpisode) != null) {
                routePrefetchingEpisodeIds = routePrefetchingEpisodeIds - prefetchId
                routePrefetchedEpisodeIds = routePrefetchedEpisodeIds + prefetchId
                routePrefetchEmptyEpisodeIds = routePrefetchEmptyEpisodeIds - prefetchId
                return@forEach
            }
            if (prefetchId in routePrefetchingEpisodeIds || prefetchId in routePrefetchedEpisodeIds) {
                return@forEach
            }
            routePrefetchingEpisodeIds = routePrefetchingEpisodeIds + prefetchId
            routePrefetchEmptyEpisodeIds = routePrefetchEmptyEpisodeIds - prefetchId
            launch {
                val warmed = graph.sourceRegistry.prefetchRouteCandidates(prefetchEpisode)
                routePrefetchingEpisodeIds = routePrefetchingEpisodeIds - prefetchId
                if (warmed) {
                    routePrefetchedEpisodeIds = routePrefetchedEpisodeIds + prefetchId
                    routePrefetchEmptyEpisodeIds = routePrefetchEmptyEpisodeIds - prefetchId
                } else {
                    routePrefetchEmptyEpisodeIds = routePrefetchEmptyEpisodeIds + prefetchId
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AnimeBackground),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(AnimePanelSoft, RoundedCornerShape(8.dp)).focusable(),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "\u8FD4\u56DE", tint = Color.White)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(result.title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("追番详情", style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1)
                }
            }
        }
        item {
            DetailEntryStatusCard(
                state = detailEntryState,
                modifier = Modifier.padding(horizontal = 18.dp),
            )
        }
        if (loading) {
            item {
                Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AnimeAccentCyan)
                }
            }
        }
        error?.let {
            item {
                Text(
                    "\u52A0\u8F7D\u5931\u8D25: $it",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 18.dp),
                )
            }
        }
        detail?.let { media ->
            val episodeSummary = buildDetailEpisodeSummaryUiState(
                episodeCount = media.episodes.size,
                selectedEpisode = selectedEpisode,
                routeState = routeUiState,
            )
            item {
                DetailHero(
                    media = media,
                    selectedEpisode = selectedEpisode,
                    routeUiState = routeUiState,
                    playbackReadiness = detailPlaybackReadiness,
                    actionState = detailHeroActionState,
                    firstPlayState = detailFirstPlayState,
                    onPlay = {
                        val episode = selectedEpisode ?: media.episodes.firstOrNull()
                        if (episode != null) {
                            val bestRoute = routeUiState.bestRoute
                            if (bestRoute != null) {
                                onPlay(media, episode, bestRoute.stream, sortRoutesForUi(routes))
                            } else {
                                loadRoutesFor(episode, autoPlay = true)
                            }
                        }
                    },
                    onToggleRoutes = { routesExpanded = !routesExpanded },
                    modifier = Modifier.padding(horizontal = 18.dp),
                )
            }
            item {
                DetailEpisodeSectionHeader(
                    state = episodeSummary,
                    modifier = Modifier.padding(horizontal = 18.dp),
                )
            }
            item {
                EpisodeSelectorRow(
                    episodes = media.episodes,
                    selectedEpisodeId = selectedEpisode?.id,
                    onEpisodeSelected = { loadRoutesFor(it) },
                    modifier = Modifier.padding(horizontal = 18.dp),
                )
            }
            item {
                DetailRouteStatusCard(
                    state = detailRouteStatusState,
                    onToggleExpanded = { routesExpanded = !routesExpanded },
                    onPlayBest = {
                        val episode = selectedEpisode ?: return@DetailRouteStatusCard
                        routeUiState.bestRoute?.let { route ->
                            onPlay(media, episode, route.stream, sortRoutesForUi(routes))
                        }
                    },
                    modifier = Modifier.padding(horizontal = 18.dp),
                )
            }
            if (routePrefetchUiState.items.isNotEmpty()) {
                item {
                    DetailRoutePrefetchCard(
                        state = routePrefetchUiState,
                        modifier = Modifier.padding(horizontal = 18.dp),
                    )
                }
            }
            if (routesExpanded) {
                if (routes.isNotEmpty()) {
                    item {
                        RouteSourceSelector(
                            routes = routes,
                            selectedSourceId = routeSourceFilter,
                            recommendedSourceId = routeUiState.bestRoute?.sourceId,
                            onSelected = { routeSourceFilter = it },
                            modifier = Modifier.padding(horizontal = 18.dp),
                        )
                    }
                }
                if (routesLoading) {
                    item {
                        DetailRouteResolutionPanel(
                            state = detailRouteResolutionState,
                            modifier = Modifier.padding(horizontal = 18.dp),
                        )
                    }
                }
                if (routesError != null) {
                    item {
                        DetailRouteResolutionPanel(
                            state = detailRouteResolutionState,
                            modifier = Modifier.padding(horizontal = 18.dp),
                        )
                    }
                }
                if (!routesLoading && selectedEpisode != null && routes.isEmpty() && routesError == null) {
                    item {
                        DetailRouteResolutionPanel(
                            state = detailRouteResolutionState,
                            modifier = Modifier.padding(horizontal = 18.dp),
                        )
                    }
                }
                items(routeUiState.visibleRoutes, key = { it.stream.id }) { route ->
                    RouteCandidateRow(
                        route = route,
                        recommended = route.stream.id == routeUiState.bestRoute?.stream?.id,
                        onClick = {
                            val episode = selectedEpisode ?: return@RouteCandidateRow
                            onPlay(media, episode, route.stream, sortRoutesForUi(routes))
                        },
                        modifier = Modifier.padding(horizontal = 18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailHero(
    media: MediaDetail,
    selectedEpisode: Episode?,
    routeUiState: RouteUiState,
    playbackReadiness: DetailPlaybackReadinessUiState,
    actionState: DetailHeroActionUiState,
    firstPlayState: DetailFirstPlayUiState,
    onPlay: () -> Unit,
    onToggleRoutes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val heroState = remember(media, selectedEpisode, routeUiState.status) {
        buildDetailHeroChromeUiState(media, selectedEpisode)
    }
    val heroAccent = providerAccent(heroState.providerId)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = heroState.minHeight)
            .clip(RoundedCornerShape(heroState.cornerRadius))
            .background(AnimePanel),
    ) {
        PosterArtwork(
            posterUrl = heroState.posterUrl,
            accent = heroAccent,
            modifier = Modifier.matchParentSize().alpha(heroState.backgroundPosterAlpha),
            shape = RoundedCornerShape(heroState.cornerRadius),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = heroState.overlayTopAlpha),
                            AnimeBackground.copy(alpha = heroState.overlayMiddleAlpha),
                            AnimeBackground.copy(alpha = heroState.overlayBottomAlpha),
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(heroState.contentPadding),
            verticalArrangement = Arrangement.spacedBy(heroState.sectionSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(heroState.headerSpacing),
                verticalAlignment = Alignment.Bottom,
            ) {
                PosterArtwork(
                    posterUrl = heroState.posterUrl,
                    accent = heroAccent,
                    modifier = Modifier.size(width = heroState.posterWidth, height = heroState.posterHeight),
                    shape = RoundedCornerShape(heroState.posterCornerRadius),
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(heroState.titleSpacing)) {
                    Text(
                        text = heroState.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(heroState.chipSpacing)) {
                        heroState.chips.forEach { chip ->
                            RouteStatusBadge(chip.label, sourceLibraryToneColor(chip.tone))
                        }
                    }
                    Text(
                        text = heroState.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.82f),
                        maxLines = heroState.summaryMaxLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            DetailFirstPlayStrip(state = firstPlayState)
            DetailPlaybackReadinessStrip(state = playbackReadiness)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(heroState.actionSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(containerColor = AnimeAccentPink, contentColor = Color.White),
                    modifier = Modifier.weight(1f).height(heroState.primaryButtonHeight).focusable(),
                    shape = RoundedCornerShape(heroState.cornerRadius),
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(actionState.primaryActionLabel)
                }
                DetailRouteEntryButton(
                    state = actionState,
                    onClick = onToggleRoutes,
                    modifier = Modifier
                        .widthIn(min = heroState.routeButtonMinWidth, max = heroState.routeButtonMaxWidth)
                        .height(heroState.primaryButtonHeight),
                )
            }
        }
    }
}

@Composable
private fun LegacyDetailHero(
    media: MediaDetail,
    selectedEpisode: Episode?,
    routeUiState: RouteUiState,
    playbackReadiness: DetailPlaybackReadinessUiState,
    actionState: DetailHeroActionUiState,
    firstPlayState: DetailFirstPlayUiState,
    onPlay: () -> Unit,
    onToggleRoutes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val heroState = remember(media, selectedEpisode) { buildDetailHeroChromeUiState(media, selectedEpisode) }
    val heroAccent = providerAccent(heroState.providerId)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = heroState.minHeight)
            .clip(RoundedCornerShape(heroState.cornerRadius))
            .background(AnimePanel),
    ) {
        PosterArtwork(
            posterUrl = heroState.posterUrl,
            accent = heroAccent,
            modifier = Modifier.matchParentSize().alpha(heroState.backgroundPosterAlpha),
            shape = RoundedCornerShape(heroState.cornerRadius),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = heroState.overlayTopAlpha),
                            AnimeBackground.copy(alpha = heroState.overlayMiddleAlpha),
                            AnimeBackground.copy(alpha = heroState.overlayBottomAlpha),
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(heroState.contentPadding),
            verticalArrangement = Arrangement.spacedBy(heroState.sectionSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(heroState.headerSpacing),
                verticalAlignment = Alignment.Bottom,
            ) {
                PosterArtwork(
                    posterUrl = heroState.posterUrl,
                    accent = heroAccent,
                    modifier = Modifier.size(width = heroState.posterWidth, height = heroState.posterHeight),
                    shape = RoundedCornerShape(heroState.posterCornerRadius),
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(heroState.titleSpacing)) {
                    Text(
                        heroState.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VideoMetaChip(selectedEpisode?.index?.let { "第 $it 集" } ?: "自动选集")
                        VideoMetaChip("${media.episodes.size.coerceAtLeast(1)} 集")
                        VideoMetaChip("自动匹配")
                    }
                    Text(
                        media.summary.orEmpty().ifBlank { "已为你自动匹配播放源，优先选择稳定的在线播放体验。" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.82f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            DetailFirstPlayStrip(
                state = firstPlayState,
            )
            DetailPlaybackReadinessStrip(state = playbackReadiness)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(containerColor = AnimeAccentPink, contentColor = Color.White),
                    modifier = Modifier.weight(1f).height(48.dp).focusable(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(actionState.primaryActionLabel)
                }
                DetailRouteEntryButton(
                    state = actionState,
                    onClick = onToggleRoutes,
                    modifier = Modifier.widthIn(min = 126.dp, max = 156.dp).height(48.dp),
                )
            }
        }
    }
}

@Composable
private fun DetailEntryStatusCard(
    state: DetailEntryUiState,
    modifier: Modifier = Modifier,
) {
    val accent = sourceLibraryToneColor(state.tone)
    Surface(
        modifier = modifier.fillMaxWidth().focusable(),
        color = AnimePanel,
        shape = RoundedCornerShape(state.cardCornerRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(state.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(state.rowSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(state.iconBoxSize)
                    .clip(RoundedCornerShape(state.iconCornerRadius))
                    .background(accent.copy(alpha = state.iconContainerAlpha)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Movie, contentDescription = null, tint = accent, modifier = Modifier.size(state.iconSize))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(state.contentSpacing)) {
                Row(horizontalArrangement = Arrangement.spacedBy(state.titleRowSpacing), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.headline,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    RouteStatusBadge(state.detailStatusLabel, sourceLibraryToneColor(state.statusTone))
                }
                Text(state.summary, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(state.chipSpacing)) {
                    items(state.chips) { chip ->
                        RouteStatusBadge(chip.label, sourceLibraryToneColor(chip.tone))
                    }
                }
            }
            Column(
                modifier = Modifier.widthIn(max = state.sideMaxWidth),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(state.sideSpacing),
            ) {
                Text(state.providerLabel, style = MaterialTheme.typography.labelMedium, color = accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(state.episodeLabel, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.72f), maxLines = 1)
                Text(state.actionLabel, style = MaterialTheme.typography.labelSmall, color = AnimeMuted, maxLines = 1)
            }
        }
    }
}

@Composable
private fun DetailRouteEntryButton(
    state: DetailHeroActionUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = sourceLibraryToneColor(state.routeTone)
    Card(
        onClick = onClick,
        modifier = modifier.focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.28f)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.36f)),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.VideoLibrary, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    state.routeTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    state.routeValue,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.68f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun DetailFirstPlayStrip(
    state: DetailFirstPlayUiState,
) {
    val accent = sourceLibraryToneColor(state.tone)
    val containerColor = playerChromeBaseColor(state.containerBaseColor)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(state.cardCornerRadius))
            .background(containerColor.copy(alpha = state.containerAlpha))
            .padding(horizontal = state.horizontalPadding, vertical = state.verticalPadding),
        verticalArrangement = Arrangement.spacedBy(state.contentSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(state.headerSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(state.iconBoxSize)
                    .clip(RoundedCornerShape(state.iconBoxCornerRadius))
                    .background(accent.copy(alpha = state.iconBoxContainerAlpha)),
                contentAlignment = Alignment.Center,
            ) {
                if (state.showProgress) {
                    CircularProgressIndicator(color = accent, modifier = Modifier.size(state.progressIconSize))
                } else {
                    Icon(
                        imageVector = if (state.useReadyIcon) Icons.Filled.Check else Icons.Filled.VideoLibrary,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(state.statusIconSize),
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(state.textColumnSpacing)) {
                Text(
                    state.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = accent.copy(alpha = state.titleAlpha),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    state.decision,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = state.decisionAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                state.actionLabel,
                style = MaterialTheme.typography.labelSmall,
                color = accent.copy(alpha = state.actionLabelAlpha),
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(state.actionCornerRadius))
                    .background(accent.copy(alpha = state.actionContainerAlpha))
                    .padding(horizontal = state.actionHorizontalPadding, vertical = state.actionVerticalPadding),
            )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(state.chipSpacing), modifier = Modifier.fillMaxWidth()) {
            items(state.chips) { chip ->
                DetailDecisionChip(chip)
            }
        }
    }
}

@Composable
private fun DetailPlaybackReadinessStrip(state: DetailPlaybackReadinessUiState) {
    val accent = sourceLibraryToneColor(state.tone)
    val containerColor = playerChromeBaseColor(state.containerBaseColor)
    val summaryColor = sourceLibraryToneColor(state.summaryTone)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(state.cardCornerRadius))
            .background(containerColor.copy(alpha = state.containerAlpha))
            .border(state.borderWidth, accent.copy(alpha = state.borderAlpha), RoundedCornerShape(state.cardCornerRadius))
            .padding(horizontal = state.horizontalPadding, vertical = state.verticalPadding),
        verticalArrangement = Arrangement.spacedBy(state.contentSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(state.headerSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(state.statusDotSize)
                    .clip(CircleShape)
                    .background(accent),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(state.textColumnSpacing)) {
                Text(
                    text = state.headline,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = state.headlineAlpha),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = state.summary,
                    style = MaterialTheme.typography.labelSmall,
                    color = summaryColor.copy(alpha = state.summaryAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            RouteStatusBadge(state.primaryActionLabel, accent)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(state.chipSpacing), modifier = Modifier.fillMaxWidth()) {
            items(state.chips) { chip ->
                DetailDecisionChip(chip)
            }
        }
    }
}

@Composable
private fun DetailDecisionChip(state: DetailFirstPlayChipUiState) {
    val accent = sourceLibraryToneColor(state.tone)
    val containerColor = playerChromeBaseColor(state.containerBaseColor)
    val labelColor = sourceLibraryToneColor(state.labelTone)
    Row(
        modifier = Modifier
            .height(state.height)
            .clip(RoundedCornerShape(state.cornerRadius))
            .background(containerColor.copy(alpha = state.containerAlpha))
            .border(state.borderWidth, accent.copy(alpha = state.borderAlpha), RoundedCornerShape(state.cornerRadius))
            .padding(horizontal = state.horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(state.contentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            state.label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor.copy(alpha = state.labelAlpha),
            maxLines = 1,
        )
        Text(
            state.value.ifBlank { state.fallbackValue },
            style = MaterialTheme.typography.labelSmall,
            color = accent.copy(alpha = state.valueAlpha),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DetailRouteStatusCard(
    state: DetailRouteStatusUiState,
    onToggleExpanded: () -> Unit,
    onPlayBest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = if (state.error) MaterialTheme.colorScheme.error else sourceLibraryToneColor(state.tone)
    val contentSpacing = if (state.compact) state.compactContentSpacing else state.expandedContentSpacing
    val iconBoxSize = if (state.compact) state.iconBoxCompactSize else state.iconBoxExpandedSize
    val progressIconSize = if (state.compact) state.progressIconCompactSize else state.progressIconExpandedSize
    val readyIconSize = if (state.compact) state.readyIconCompactSize else state.readyIconExpandedSize
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(state.cardCornerRadius),
        color = AnimePanel,
        border = BorderStroke(state.cardBorderWidth, AnimeBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(state.contentPadding),
            verticalArrangement = Arrangement.spacedBy(contentSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(state.headerSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(iconBoxSize)
                        .clip(RoundedCornerShape(state.iconBoxCornerRadius))
                        .background(accent.copy(alpha = state.iconBoxContainerAlpha)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.showProgress) {
                        CircularProgressIndicator(color = accent, modifier = Modifier.size(progressIconSize))
                    } else {
                        Icon(
                            imageVector = if (state.useReadyIcon) Icons.Filled.Check else Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(readyIconSize),
                        )
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(state.titleColumnSpacing)) {
                    Text(state.title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(state.subtitle, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                TextButton(onClick = onToggleExpanded, modifier = Modifier.height(state.actionButtonHeight).focusable()) {
                    Text(state.actionLabel, color = AnimeAccentCyan, style = MaterialTheme.typography.labelLarge)
                }
            }
            if (state.showProgress) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(state.progressHeight).clip(RoundedCornerShape(state.progressCornerRadius)),
                    color = accent,
                    trackColor = Color.White.copy(alpha = state.progressTrackAlpha),
                )
            }
            if (state.showRecommendation) {
                RouteRecommendationBand(
                    state = state.recommendation,
                    accent = accent,
                    onPlayBest = onPlayBest,
                )
            }
            if (state.showDiagnostics) {
                RouteSourceFocusRow(chips = state.focusChips)
                RouteLoadingStepRow(steps = state.loadingSteps, accent = accent)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(state.diagnosticsSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    state.metrics.forEach { metric ->
                        RouteMetricChip(
                            state = metric,
                            accent = if (metric.critical) MaterialTheme.colorScheme.error else sourceLibraryToneColor(metric.tone),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRoutePrefetchCard(
    state: RoutePrefetchUiState,
    modifier: Modifier = Modifier,
) {
    val accent = sourceLibraryToneColor(state.tone)
    val cardContainerColor = playerChromeBaseColor(state.cardContainerBaseColor)
    val headlineColor = playerChromeBaseColor(state.headlineBaseColor)
    val summaryColor = sourceLibraryToneColor(state.summaryTone)
    val progressTrackColor = playerChromeBaseColor(state.progressTrackBaseColor)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(state.cardCornerRadius),
        color = cardContainerColor.copy(alpha = state.cardContainerAlpha),
        border = BorderStroke(state.cardBorderWidth, accent.copy(alpha = state.cardBorderAlpha)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(state.contentPadding),
            verticalArrangement = Arrangement.spacedBy(state.contentSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(state.headerSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(state.iconBoxSize)
                        .clip(RoundedCornerShape(state.iconBoxCornerRadius))
                        .background(accent.copy(alpha = state.iconBoxContainerAlpha)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.showProgress) {
                        CircularProgressIndicator(color = accent, modifier = Modifier.size(state.iconSize))
                    } else {
                        Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = accent, modifier = Modifier.size(state.iconSize))
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        state.headline,
                        style = MaterialTheme.typography.titleSmall,
                        color = headlineColor.copy(alpha = state.headlineAlpha),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        state.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = summaryColor.copy(alpha = state.summaryAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                RouteStatusBadge(state.badgeLabel, accent)
            }
            if (state.showProgress) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(state.progressHeight)
                        .clip(RoundedCornerShape(state.progressCornerRadius)),
                    color = accent,
                    trackColor = progressTrackColor.copy(alpha = state.progressTrackAlpha),
                )
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(state.itemSpacing), modifier = Modifier.fillMaxWidth()) {
                items(state.items, key = { it.episodeId }) { item ->
                    DetailRoutePrefetchChip(item)
                }
            }
        }
    }
}

@Composable
private fun DetailRoutePrefetchChip(item: RoutePrefetchItemUiState) {
    val accent = sourceLibraryToneColor(item.tone)
    val titleColor = playerChromeBaseColor(item.titleBaseColor)
    Row(
        modifier = Modifier
            .height(item.chipHeight)
            .clip(RoundedCornerShape(item.chipCornerRadius))
            .background(accent.copy(alpha = item.containerAlpha))
            .border(item.borderWidth, accent.copy(alpha = item.borderAlpha), RoundedCornerShape(item.chipCornerRadius))
            .padding(horizontal = item.horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(item.contentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(item.indicatorSize)
                .clip(CircleShape)
                .background(accent.copy(alpha = item.indicatorAlpha)),
        )
        Text(
            item.title,
            style = MaterialTheme.typography.labelSmall,
            color = titleColor.copy(alpha = item.titleAlpha),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
        Text(
            item.statusLabel,
            style = MaterialTheme.typography.labelSmall,
            color = accent.copy(alpha = item.statusAlpha),
            maxLines = 1,
        )
    }
}

@Composable
private fun RouteRecommendationBand(
    state: DetailRouteRecommendationUiState,
    accent: Color,
    onPlayBest: () -> Unit,
) {
    val actionContainerColor = sourceLibraryToneColor(state.actionContainerTone)
    val actionContentColor = playerChromeBaseColor(state.actionContentBaseColor)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = state.minHeight)
            .clip(RoundedCornerShape(state.cornerRadius))
            .background(accent.copy(alpha = state.containerAlpha))
            .padding(state.contentPadding),
        horizontalArrangement = Arrangement.spacedBy(state.contentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(state.indicatorWidth)
                .height(state.indicatorHeight)
                .clip(RoundedCornerShape(state.indicatorCornerRadius))
                .background(accent),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(state.textColumnSpacing)) {
            Text(state.label, style = MaterialTheme.typography.labelMedium, color = accent.copy(alpha = state.labelAlpha), maxLines = 1)
            Text(state.title, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = state.titleAlpha), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(state.reason, style = MaterialTheme.typography.labelSmall, color = accent.copy(alpha = state.reasonAlpha), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(state.detail, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = state.detailAlpha), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (state.canPlay) {
            Button(
                onClick = onPlayBest,
                shape = RoundedCornerShape(state.actionCornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = actionContainerColor.copy(alpha = state.actionContainerAlpha),
                    contentColor = actionContentColor.copy(alpha = state.actionContentAlpha),
                ),
                modifier = Modifier.height(state.actionButtonHeight).focusable(),
                contentPadding = PaddingValues(horizontal = state.actionHorizontalPadding),
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(state.actionIconSize))
                Spacer(Modifier.width(state.actionContentSpacing))
                Text(state.actionLabel, maxLines = 1)
            }
        }
    }
}

@Composable
private fun RouteSourceFocusRow(chips: List<DetailRouteFocusChipUiState>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        chips.take(3).forEach { chip ->
            RouteSourceFocusChip(
                state = chip,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RouteSourceFocusChip(
    state: DetailRouteFocusChipUiState,
    modifier: Modifier = Modifier,
) {
    val accent = sourceLibraryToneColor(state.tone)
    Column(
        modifier = modifier
            .heightIn(min = state.minHeight)
            .clip(RoundedCornerShape(state.cornerRadius))
            .background(accent.copy(alpha = state.containerAlpha))
            .padding(horizontal = state.horizontalPadding, vertical = state.verticalPadding),
        verticalArrangement = Arrangement.spacedBy(state.contentSpacing),
    ) {
        Text(
            state.label,
            style = MaterialTheme.typography.labelSmall,
            color = accent.copy(alpha = state.labelAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            state.value,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = state.valueAlpha),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RouteLoadingStepRow(
    steps: List<RouteLoadingStepUiState>,
    accent: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        steps.take(3).forEach { step ->
            RouteDiagnosticStep(
                state = step,
                parentAccent = accent,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RouteDiagnosticStep(
    state: RouteLoadingStepUiState,
    parentAccent: Color,
    modifier: Modifier = Modifier,
) {
    val accent = state.accentTone?.let(::sourceLibraryToneColor) ?: parentAccent
    val inactiveContainerColor = playerChromeBaseColor(state.inactiveContainerBaseColor)
    val labelColor = if (state.active) accent else sourceLibraryToneColor(state.inactiveLabelTone)
    Column(
        modifier = modifier
            .heightIn(min = state.minHeight)
            .clip(RoundedCornerShape(state.cornerRadius))
            .background(
                if (state.active) {
                    accent.copy(alpha = state.activeContainerAlpha)
                } else {
                    inactiveContainerColor.copy(alpha = state.inactiveContainerAlpha)
                },
            )
            .padding(horizontal = state.horizontalPadding, vertical = state.verticalPadding),
        verticalArrangement = Arrangement.spacedBy(state.contentSpacing),
    ) {
        Text(
            state.title,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor.copy(alpha = state.labelAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            state.value,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = state.valueAlpha),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RouteMetricChip(
    state: DetailRouteMetricUiState,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val containerColor = playerChromeBaseColor(state.containerBaseColor)
    val labelColor = sourceLibraryToneColor(state.labelTone)
    Row(
        modifier = modifier
            .height(state.height)
            .clip(RoundedCornerShape(state.cornerRadius))
            .background(containerColor.copy(alpha = state.containerAlpha))
            .padding(horizontal = state.horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(state.contentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            state.label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor.copy(alpha = state.labelAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            state.value,
            style = MaterialTheme.typography.labelLarge,
            color = accent.copy(alpha = state.valueAlpha),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun DetailEpisodeSectionHeader(
    state: DetailEpisodeSummaryUiState,
    modifier: Modifier = Modifier,
) {
    val accent = sourceLibraryToneColor(state.tone)
    val summaryColor = sourceLibraryToneColor(state.summaryTone)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(state.cardCornerRadius),
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(state.contentSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(state.headerSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(state.textColumnSpacing)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(state.titleRowSpacing), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            state.headline,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White.copy(alpha = state.headlineAlpha),
                            fontWeight = FontWeight.Bold,
                        )
                        RouteStatusBadge(state.routeStatusLabel, accent)
                    }
                    Text(
                        state.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = summaryColor.copy(alpha = state.summaryAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    state.routeActionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = accent.copy(alpha = state.actionLabelAlpha),
                    maxLines = 1,
                    modifier = Modifier
                        .clip(RoundedCornerShape(state.actionCornerRadius))
                        .background(accent.copy(alpha = state.actionContainerAlpha))
                        .padding(horizontal = state.actionHorizontalPadding, vertical = state.actionVerticalPadding),
                )
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(state.chipSpacing), modifier = Modifier.fillMaxWidth()) {
                items(state.chips) { chip ->
                    RouteStatusBadge(chip.label, sourceLibraryToneColor(chip.tone))
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
    modifier: Modifier = Modifier,
) {
    val selectorState = remember(episodes, selectedEpisodeId) {
        buildDetailEpisodeSelectorUiState(episodes, selectedEpisodeId)
    }
    LazyRow(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(selectorState.itemSpacing)) {
        items(selectorState.items, key = { it.episodeId }) { state ->
            val accent = sourceLibraryToneColor(state.tone)
            Card(
                onClick = { onEpisodeSelected(state.episode) },
                modifier = Modifier.width(state.cardWidth).height(state.cardHeight).focusable(),
                shape = RoundedCornerShape(state.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = if (state.selected) AnimePanelSoft else AnimePanel),
                border = BorderStroke(state.borderWidth, if (state.selected) accent else AnimeBorder),
            ) {
                Column(
                    Modifier.fillMaxSize().padding(state.contentPadding),
                    verticalArrangement = Arrangement.spacedBy(state.contentSpacing),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(state.headerSpacing), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = state.indexLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = (if (state.selected) accent else Color.White).copy(alpha = state.indexAlpha),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                        Text(
                            text = state.actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent.copy(alpha = state.actionAlpha),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = (if (state.selected) Color.White else AnimeMuted).copy(alpha = state.titleAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = state.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = AnimeMuted.copy(alpha = state.subtitleAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRouteResolutionPanel(
    state: DetailRouteResolutionUiState,
    modifier: Modifier = Modifier,
) {
    val accent = sourceLibraryToneColor(state.tone)
    val subtitleColor = sourceLibraryToneColor(state.subtitleTone)
    val detailColor = sourceLibraryToneColor(state.detailTone)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(state.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(state.borderWidth, accent.copy(alpha = state.borderAlpha)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(state.contentPadding),
            verticalArrangement = Arrangement.spacedBy(state.contentSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(state.headerSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.showProgress) {
                    CircularProgressIndicator(color = accent, modifier = Modifier.size(state.progressSize))
                } else {
                    Box(
                        modifier = Modifier
                            .size(state.iconBoxSize)
                            .background(
                                accent.copy(alpha = state.iconBoxContainerAlpha),
                                RoundedCornerShape(state.iconBoxCornerRadius),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = accent, modifier = Modifier.size(state.statusIconSize))
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(state.textColumnSpacing)) {
                    Text(
                        state.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = state.titleAlpha),
                    )
                    Text(
                        state.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor.copy(alpha = state.subtitleAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                state.detail,
                style = MaterialTheme.typography.bodySmall,
                color = detailColor.copy(alpha = state.detailAlpha),
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(state.chipSpacing)) {
                items(state.chips) { chip ->
                    RouteStatusBadge(chip.label, sourceLibraryToneColor(chip.tone))
                }
            }
        }
    }
}

@Composable
private fun RouteSourceSelector(
    routes: List<RouteCandidate>,
    selectedSourceId: String?,
    recommendedSourceId: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = remember(routes, selectedSourceId, recommendedSourceId) {
        buildDetailRouteSourceSelectorUiState(
            routes = routes,
            selectedSourceId = selectedSourceId,
            recommendedSourceId = recommendedSourceId,
        )
    }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(state.containerSpacing)) {
        RouteSourceSelectorHeader(state)
        RouteSourceAutoChoiceCard(
            state = state.autoChoice,
            onClick = { onSelected(null) },
        )
        LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(state.groupSpacing)) {
            items(state.groups) { group ->
                RouteSourceFilterPill(
                    group = group,
                    onClick = { onSelected(group.id) },
                )
            }
        }
    }
}

@Composable
private fun RouteSourceSelectorHeader(
    state: DetailRouteSourceSelectorUiState,
) {
    val subtitleColor = sourceLibraryToneColor(state.subtitleTone)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(state.headerSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(state.headerTextSpacing)) {
            Text(
                text = state.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = state.titleAlpha),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = state.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor.copy(alpha = state.subtitleAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        RouteSourceStatusPill(state.recommended)
        RouteSourceStatusPill(state.current)
    }
}

@Composable
private fun RouteSourceAutoChoiceCard(
    state: DetailRouteSourceAutoChoiceUiState,
    onClick: () -> Unit,
) {
    val accent = sourceLibraryToneColor(state.tone)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().focusable(),
        shape = RoundedCornerShape(state.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = if (state.selected) AnimePanelSoft else AnimePanel),
        border = BorderStroke(state.borderWidth, accent.copy(alpha = if (state.selected) state.selectedBorderAlpha else state.idleBorderAlpha)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(state.contentPadding),
            horizontalArrangement = Arrangement.spacedBy(state.rowSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(state.iconBoxSize)
                    .clip(RoundedCornerShape(state.iconBoxCornerRadius))
                    .background(accent.copy(alpha = state.iconBoxContainerAlpha)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = accent, modifier = Modifier.size(state.iconSize))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(state.textColumnSpacing)) {
                Row(horizontalArrangement = Arrangement.spacedBy(state.titleRowSpacing), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        state.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = state.titleAlpha),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
                Text(
                    state.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = sourceLibraryToneColor(state.subtitleTone).copy(alpha = state.subtitleAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(state.badgeSpacing), verticalAlignment = Alignment.CenterVertically) {
                    state.badges.forEach { badge ->
                        RouteStatusBadge(badge.label, sourceLibraryToneColor(badge.tone))
                    }
                }
            }
            Text(
                state.actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = accent.copy(alpha = state.actionLabelAlpha),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun RouteSourceStatusPill(state: DetailRouteSourceStatusPillUiState) {
    val color = sourceLibraryToneColor(state.tone)
    val valueColor = playerChromeBaseColor(state.valueBaseColor)
    Row(
        modifier = Modifier
            .height(state.height)
            .clip(RoundedCornerShape(state.cornerRadius))
            .background(color.copy(alpha = state.containerAlpha))
            .padding(horizontal = state.horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(state.contentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(state.label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = state.labelAlpha), maxLines = 1)
        Text(state.value, style = MaterialTheme.typography.labelSmall, color = valueColor.copy(alpha = state.valueAlpha), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun RouteSourceFilterPill(
    group: RouteSourceGroupUiState,
    onClick: () -> Unit,
) {
    val accent = sourceLibraryToneColor(group.tone)
    val emphasized = group.isFilterSelected || group.hasRecommended
    val contentColor = when {
        group.isFilterSelected -> Color.White.copy(alpha = group.selectedContentAlpha)
        group.hasRecommended -> Color.White.copy(alpha = group.recommendedContentAlpha)
        else -> Color.White.copy(alpha = group.idleContentAlpha)
    }
    TextButton(
        onClick = onClick,
        modifier = Modifier.width(group.filterWidth).height(group.filterHeight).focusable(),
        shape = RoundedCornerShape(group.filterCornerRadius),
        colors = ButtonDefaults.textButtonColors(
            containerColor = when {
                group.isFilterSelected -> accent.copy(alpha = group.selectedContainerAlpha)
                group.hasRecommended -> accent.copy(alpha = group.recommendedContainerAlpha)
                else -> Color.White.copy(alpha = group.idleContainerAlpha)
            },
            contentColor = contentColor,
        ),
        border = BorderStroke(group.filterBorderWidth, if (emphasized) accent else AnimeBorder),
        contentPadding = PaddingValues(horizontal = group.filterHorizontalPadding, vertical = group.filterVerticalPadding),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(group.filterHeaderSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = group.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    fontWeight = if (emphasized) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = group.routeCountLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (emphasized) accent else AnimeMuted,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
            Text(
                text = group.sourceSummary,
                style = MaterialTheme.typography.labelSmall,
                color = if (emphasized) accent else AnimeMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(group.filterFooterSpacing), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = group.footerLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (emphasized) accent else Color.White.copy(alpha = group.idleFooterAlpha),
                    fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                )
                if (!emphasized) {
                    RouteStatusBadge(group.statusLabel, accent)
                }
            }
        }
    }
}

@Composable
private fun RouteStatusBadge(label: String, color: Color) {
    val chrome = remember { buildRouteStatusBadgeChromeUiState() }
    Box(
        modifier = Modifier
            .height(chrome.height)
            .clip(RoundedCornerShape(chrome.cornerRadius))
            .background(color.copy(alpha = chrome.containerAlpha))
            .padding(horizontal = chrome.horizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = chrome.textAlpha),
            maxLines = 1,
        )
    }
}

@Composable
private fun RoutePlayActionLabel(state: RouteCandidateUiState) {
    val color = sourceLibraryToneColor(state.actionTone)
    Row(
        modifier = Modifier
            .height(state.detailActionLabelHeight)
            .clip(RoundedCornerShape(state.detailActionLabelCornerRadius))
            .background(color.copy(alpha = state.detailActionLabelContainerAlpha))
            .padding(horizontal = state.detailActionLabelHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(state.detailActionLabelSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            playerRouteActionIcon(state.actionIconKind),
            contentDescription = state.actionIconContentDescription,
            tint = color.copy(alpha = state.detailActionLabelIconAlpha),
            modifier = Modifier.size(state.detailActionLabelIconSize),
        )
        Text(
            state.actionLabel,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = state.detailActionLabelTextAlpha),
            maxLines = 1,
        )
    }
}

private fun RouteCandidate.primaryRouteLabel(): String {
    return routePrimaryLabelForUi(this)
}

private fun defaultDanmakuSearchQuery(detail: MediaDetail, episode: Episode): String {
    val aliases = buildList {
        add(episode.raw["subjectNameCn"])
        add(episode.raw["subjectTitle"])
        episode.raw["subjectAliases"]
            ?.split("|")
            ?.forEach { add(it) }
        add(episode.raw["subjectName"])
        add(detail.title)
    }
        .filterNotNull()
        .map { it.trim() }
        .filter { it.length >= 2 }
        .distinctBy { it.normalizedDanmakuSearchKey() }
    return aliases.firstOrNull { it.hasCjkText() } ?: aliases.firstOrNull().orEmpty().ifBlank { detail.title }
}

private fun String.normalizedDanmakuSearchKey(): String {
    return lowercase().filter { char ->
        char.isLetterOrDigit() ||
            char in '\u4e00'..'\u9fff' ||
            char in '\u3040'..'\u30ff' ||
            char in '\u3400'..'\u4dbf'
    }
}

private fun String.hasCjkText(): Boolean {
    return any { char ->
        char in '\u4e00'..'\u9fff' ||
            char in '\u3040'..'\u30ff' ||
            char in '\u3400'..'\u4dbf'
    }
}

@Composable
private fun RouteCandidateRow(
    route: RouteCandidate,
    recommended: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = buildRouteCandidateUiState(route = route, recommended = recommended)
    val primaryColor = sourceLibraryToneColor(state.detailPrimaryTone)
    val titleColor = sourceLibraryToneColor(state.detailTitleTone)
    val protocolColor = sourceLibraryToneColor(state.detailProtocolTone)
    val sizeColor = sourceLibraryToneColor(state.detailSizeTone)
    val sourceInitialColor = playerChromeBaseColor(state.detailSourceInitialBaseColor)
    val sourceNameColor = playerChromeBaseColor(state.detailSourceNameBaseColor)
    val containerColor = animeSurfaceBaseColor(state.detailCardContainerBaseColor)
    val borderColor = state.detailBorderTone?.let(::sourceLibraryToneColor)
        ?: animeSurfaceBaseColor(state.detailBorderBaseColor)
    val railColor = sourceLibraryToneColor(state.detailRailTone)
    val sourceBoxColor = sourceLibraryToneColor(state.detailSourceBoxTone)
    Card(
        onClick = onClick,
        enabled = state.detailClickEnabled,
        modifier = modifier.fillMaxWidth().focusable(enabled = state.detailClickEnabled),
        shape = RoundedCornerShape(state.detailCardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor,
        ),
        border = BorderStroke(
            state.detailBorderWidth,
            if (state.recommended) {
                borderColor.copy(alpha = state.detailRecommendedBorderAlpha)
            } else {
                borderColor.copy(alpha = state.detailIdleBorderAlpha)
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(state.detailContentAlpha)
                .padding(state.detailContentPadding),
            horizontalArrangement = Arrangement.spacedBy(state.detailRowSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(state.detailRailWidth)
                    .height(state.detailRailHeight)
                    .clip(RoundedCornerShape(state.detailRailCornerRadius))
                    .background(railColor.copy(alpha = state.detailRailAlpha)),
            )
            Box(
                modifier = Modifier
                    .size(state.detailSourceBoxSize)
                    .background(sourceBoxColor.copy(alpha = state.detailSourceBoxAlpha), RoundedCornerShape(state.detailSourceBoxCornerRadius)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    state.sourceInitial,
                    style = MaterialTheme.typography.titleMedium,
                    color = sourceInitialColor.copy(alpha = state.detailSourceInitialAlpha),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(state.detailTextColumnSpacing)) {
                Row(horizontalArrangement = Arrangement.spacedBy(state.detailTitleRowSpacing), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        state.sourceName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        color = sourceNameColor.copy(alpha = state.detailSourceNameAlpha),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    state.badges.forEach { badge ->
                        RouteStatusBadge(badge.label, sourceLibraryToneColor(badge.tone))
                    }
                }
                Text(
                    state.primaryLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = primaryColor.copy(alpha = state.detailPrimaryAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    state.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = titleColor.copy(alpha = state.detailTitleAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(state.detailTrailingSpacing)) {
                Text(
                    state.protocolLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = protocolColor.copy(alpha = state.detailProtocolAlpha),
                )
                state.sizeLabel?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = sizeColor.copy(alpha = state.detailSizeAlpha),
                    )
                }
                RoutePlayActionLabel(state)
            }
        }
    }
}

private fun StreamProtocol.displayName(): String {
    return uiProtocolName()
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
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()
    val engine = remember { ExoPlayerEngine(context) }
    val state by engine.state.collectAsState()
    val torrentState by graph.torrentEngine.state.collectAsState()
    val torrentPlaybackUrl = torrentState.plan?.localPlaybackUrl
    var currentEpisode by remember(episode.id) { mutableStateOf(episode) }
    var playerRoutes by remember(episode.id, stream.id) { mutableStateOf(routes) }
    var currentStream by remember(stream.id) { mutableStateOf(stream) }
    var failedStreamIds by remember(stream.id, playerRoutes) { mutableStateOf<Set<String>>(emptySet()) }
    val routeOptions = remember(playerRoutes, failedStreamIds) {
        sortRoutesForUi(playerRoutes, failedStreamIds).distinctBy { it.stream.id }
    }
    val routeCoverageLabel = remember(routeOptions) { playerRouteCoverageLabel(routeOptions) }
    var routeNotice by remember(stream.id) { mutableStateOf<String?>(null) }
    var danmakuItems by remember { mutableStateOf<List<DanmakuItem>>(emptyList()) }
    var danmakuMatches by remember { mutableStateOf<List<DanmakuMatch>>(emptyList()) }
    var danmakuMatching by remember { mutableStateOf(false) }
    var danmakuSearchQuery by remember(detail.providerId, detail.url) {
        mutableStateOf(defaultDanmakuSearchQuery(detail, episode))
    }
    var danmakuEnabled by remember { mutableStateOf(true) }
    var density by remember { mutableFloatStateOf(0.32f) }
    var danmakuAlpha by remember { mutableFloatStateOf(0.76f) }
    var danmakuFontScale by remember { mutableFloatStateOf(0.72f) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    var activePanel by remember(stream.id) { mutableStateOf<PlayerPanel?>(null) }
    var episodeLoadingId by remember { mutableStateOf<String?>(null) }
    var controlsVisible by remember(currentStream.id) { mutableStateOf(true) }
    var controlsLocked by remember(currentStream.id) { mutableStateOf(false) }
    var controlsRevealSerial by remember(currentStream.id) { mutableIntStateOf(0) }
    var playbackPositionMs by remember(currentStream.id) { mutableStateOf(0L) }
    var playbackDurationMs by remember(currentStream.id) { mutableStateOf(0L) }
    var seekFeedbackText by remember(currentStream.id) { mutableStateOf<String?>(null) }
    var seekFeedbackPlacement by remember(currentStream.id) { mutableStateOf(PlayerSeekFeedbackPlacement.Center) }
    var seekFeedbackSerial by remember(currentStream.id) { mutableIntStateOf(0) }
    val profile = remember {
        DanmakuProfile(
            platform = DanmakuPlatform.Bilibili,
            fontScale = 0.76f,
            strokeWidthPx = 2.5f,
            shadowRadiusPx = 2.5f,
            maxTracks = 7,
            maxItemsPerMinute = 260,
            supportsAdvanced = true,
        )
    }

    fun revealControls() {
        controlsVisible = true
        controlsRevealSerial += 1
    }

    fun toggleControls() {
        if (controlsLocked) return
        if (controlsVisible && activePanel == null) {
            controlsVisible = false
        } else {
            revealControls()
        }
    }

    fun enterFullscreen() {
        revealControls()
        activity?.setPlayerImmersive(true)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }

    fun exitFullscreen() {
        controlsLocked = false
        revealControls()
        activity?.setPlayerImmersive(false)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    fun lockFullscreenControls() {
        controlsLocked = true
        controlsVisible = false
        activePanel = null
    }

    fun unlockFullscreenControls() {
        controlsLocked = false
        revealControls()
    }

    fun seekBy(deltaMs: Long, fromGesture: Boolean = false) {
        if (playerSeekShouldRevealControls(fromGesture)) {
            revealControls()
        }
        val duration = normalizePlaybackDurationMs(engine.player.duration)
        val currentPosition = engine.currentPositionMs()
        val target = playerSeekTargetMs(
            currentPositionMs = currentPosition,
            deltaMs = deltaMs,
            durationMs = duration,
        )
        engine.player.seekTo(target)
        val direction = if (deltaMs >= 0L) "快进" else "后退"
        seekFeedbackText = "$direction ${kotlin.math.abs(deltaMs) / 1000L} 秒 · ${formatPlaybackTimeForUi(target)}"
        seekFeedbackPlacement = playerSeekFeedbackPlacement(deltaMs, fromGesture)
        seekFeedbackSerial += 1
    }

    LaunchedEffect(playbackSpeed) {
        engine.player.setPlaybackSpeed(playbackSpeed)
    }
    LaunchedEffect(currentStream.id) {
        revealControls()
        if (currentStream.protocol == StreamProtocol.BITTORRENT) {
            graph.torrentEngine.prepare(currentStream)
        } else {
            graph.torrentEngine.release()
            engine.prepare(currentStream)
        }
    }
    LaunchedEffect(detail.providerId, detail.url, currentEpisode.providerId, currentEpisode.id) {
        danmakuMatching = true
        danmakuItems = emptyList()
        try {
            graph.ensureDanmakuManualMappingsLoaded()
            danmakuMatches = runCatching {
                graph.danmakuRegistry.matchAll(detail, currentEpisode)
            }.getOrDefault(emptyList())
            danmakuItems = runCatching {
                graph.danmakuRegistry.fetchBestTimeline(detail, currentEpisode)
            }.getOrDefault(emptyList())
        } finally {
            danmakuMatching = false
        }
    }
    LaunchedEffect(currentEpisode.id, detail.episodes, playerRoutes) {
        if (playerRoutes.isEmpty()) return@LaunchedEffect
        routePrefetchWindow(detail.episodes, currentEpisode).forEach { prefetchEpisode ->
            launch {
                graph.sourceRegistry.prefetchRouteCandidates(prefetchEpisode)
            }
        }
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
    LaunchedEffect(currentStream.id, state.isPlaying, controlsVisible, activePanel != null) {
        while (true) {
            playbackPositionMs = engine.currentPositionMs().coerceAtLeast(0L)
            playbackDurationMs = normalizePlaybackDurationMs(engine.player.duration)
            delay(
                playerProgressPollDelayMs(
                    isPlaying = state.isPlaying,
                    controlsVisible = controlsVisible,
                    panelOpen = activePanel != null,
                ),
            )
        }
    }
    LaunchedEffect(seekFeedbackSerial) {
        if (seekFeedbackText != null) {
            delay(850)
            seekFeedbackText = null
        }
    }
    LaunchedEffect(controlsVisible) {
        if (!controlsVisible) {
            activePanel = null
        }
    }
    LaunchedEffect(controlsLocked) {
        if (controlsLocked) {
            activePanel = null
        }
    }
    LaunchedEffect(state.hasRenderedFirstFrame, currentStream.id) {
        if (state.hasRenderedFirstFrame) {
            revealControls()
        }
    }
    LaunchedEffect(controlsVisible, activePanel, state.isPlaying, currentStream.id, controlsRevealSerial) {
        if (controlsVisible && activePanel == null && state.isPlaying) {
            val revealSerial = controlsRevealSerial
            delay(6_000)
            if (controlsVisible && activePanel == null && state.isPlaying && controlsRevealSerial == revealSerial) {
                controlsVisible = false
            }
        }
    }
    LaunchedEffect(state.errorMessage, currentStream.id, routeOptions) {
        val errorMessage = state.errorMessage ?: return@LaunchedEffect
        if (currentStream.protocol == StreamProtocol.BITTORRENT || currentStream.id in failedStreamIds) return@LaunchedEffect
        val failedIds = failedStreamIds + currentStream.id
        failedStreamIds = failedIds
        val nextRoute = nextPlayableRoute(playerRoutes, currentStream.id, failedIds)
        if (nextRoute != null) {
            routeNotice = "播放源失败，已自动切到 ${nextRoute.primaryRouteLabel()}"
            currentStream = nextRoute.stream
        } else {
            routeNotice = "当前播放源失败：$errorMessage"
        }
    }
    LaunchedEffect(state.hasRenderedFirstFrame, state.isPlaying, currentStream.id, routeNotice) {
        val notice = routeNotice ?: return@LaunchedEffect
        if (state.hasRenderedFirstFrame || state.isPlaying) {
            delay(1_800)
            if (routeNotice == notice && (state.hasRenderedFirstFrame || state.isPlaying)) {
                routeNotice = null
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            activity?.setPlayerImmersive(false)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            engine.release()
            graph.torrentEngine.release()
        }
    }

    val currentRoute = routeOptions.firstOrNull { it.stream.id == currentStream.id || it.stream.url == currentStream.url }
    val effectiveErrorMessage = if (currentStream.protocol == StreamProtocol.BITTORRENT) {
        torrentState.errorMessage
    } else {
        state.errorMessage
    }
    val hasPlaybackIssue = effectiveErrorMessage != null
    val nextRoute = nextPlayableRoute(playerRoutes, currentStream.id, failedStreamIds)
    val cacheActionState = remember(currentStream) {
        buildPlayerCacheActionUiState(currentStream)
    }

    fun enqueueCurrentStreamForOffline() {
        revealControls()
        if (!cacheActionState.enabled) {
            routeNotice = cacheActionState.reason
            return
        }
        graph.media3DownloadCoordinator.enqueue(currentStream, "${detail.title} ${currentEpisode.title}")
        routeNotice = "\u5df2\u52a0\u5165\u79bb\u7ebf\u7f13\u5b58 \u00b7 ${cacheActionState.reason}"
    }

    fun refreshDanmakuMapping(query: String = danmakuSearchQuery) {
        revealControls()
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            routeNotice = "请输入番名后再搜索弹幕候选"
            return
        }
        danmakuSearchQuery = cleanQuery
        routeNotice = "正在按「$cleanQuery」搜索弹幕候选..."
        scope.launch {
            danmakuMatching = true
            try {
                graph.ensureDanmakuManualMappingsLoaded()
                val matches = runCatching {
                    graph.danmakuRegistry.searchCandidates(detail, currentEpisode, cleanQuery)
                }.getOrDefault(emptyList())
                danmakuMatches = matches
                danmakuItems = runCatching {
                    graph.danmakuRegistry.fetchBestTimeline(detail, currentEpisode)
                }.getOrDefault(emptyList())
                routeNotice = when {
                    danmakuItems.isNotEmpty() -> "已加载 ${danmakuItems.size} 条弹幕 · 「$cleanQuery」${matches.size} 个候选"
                    matches.isNotEmpty() -> "已按「$cleanQuery」找到 ${matches.size} 个弹幕候选，可选择校准"
                    else -> "「$cleanQuery」暂未找到弹幕候选，可尝试中文名、原名或别名"
                }
            } finally {
                danmakuMatching = false
            }
        }
    }

    fun calibrateDanmakuMapping(match: DanmakuMatch) {
        revealControls()
        routeNotice = "正在校准当前集弹幕映射..."
        scope.launch {
            danmakuMatching = true
            try {
                graph.addOrReplaceDanmakuManualMapping(
                    DanmakuManualMapping(
                        detailTitle = detail.title,
                        detailProviderId = detail.providerId,
                        detailUrl = detail.url,
                        episodeId = currentEpisode.id,
                        episodeIndex = currentEpisode.index,
                        match = match,
                    ),
                )
                val matches = graph.danmakuRegistry.matchAll(detail, currentEpisode)
                danmakuMatches = matches
                danmakuItems = graph.danmakuRegistry.fetchBestTimeline(detail, currentEpisode)
                routeNotice = if (danmakuItems.isNotEmpty()) {
                    "已校准弹幕映射 · 加载 ${danmakuItems.size} 条"
                } else {
                    "已保存校准映射，但该候选暂未返回弹幕"
                }
            } finally {
                danmakuMatching = false
            }
        }
    }

    fun selectRoute(route: RouteCandidate) {
        revealControls()
        activePanel = null
        failedStreamIds = emptySet()
        routeNotice = null
        currentStream = route.stream
    }

    fun retryCurrentRoute() {
        revealControls()
        activePanel = null
        failedStreamIds = failedStreamIds - currentStream.id
        routeNotice = "正在重试当前播放源..."
        if (currentStream.protocol == StreamProtocol.BITTORRENT) {
            scope.launch {
                graph.torrentEngine.prepare(currentStream)
            }
        } else {
            graph.torrentEngine.release()
            engine.prepare(currentStream)
        }
    }

    fun selectNextRoute() {
        revealControls()
        activePanel = null
        val failedIds = failedStreamIds + currentStream.id
        failedStreamIds = failedIds
        val route = nextPlayableRoute(playerRoutes, currentStream.id, failedIds)
        if (route != null) {
            routeNotice = "已切换到 ${route.primaryRouteLabel()}"
            currentStream = route.stream
        } else {
            routeNotice = "没有更多可用播放源，可重试当前源或手动换源"
        }
    }

    fun selectEpisode(target: Episode) {
        if (target.id == currentEpisode.id || episodeLoadingId != null) return
        revealControls()
        val previousSourceId = currentRoute?.sourceId
        val previousProviderId = currentStream.providerId

        fun applyEpisodeRoutes(candidates: List<RouteCandidate>) {
            val sortedCandidates = sortRoutesForUi(candidates)
            val preferredRoute = preferredRouteForNextEpisode(
                routes = sortedCandidates,
                currentSourceId = previousSourceId,
                currentProviderId = previousProviderId,
            )
            if (preferredRoute != null) {
                currentEpisode = target
                playerRoutes = sortedCandidates
                failedStreamIds = emptySet()
                val episodeLabel = target.index?.let { "第 $it 集" } ?: target.title
                val routeLabel = preferredRoute.routeName.orEmpty()
                    .ifBlank { preferredRoute.quality.orEmpty() }
                    .ifBlank { preferredRoute.protocol.displayName() }
                val keptSource = preferredRoute.sourceId == previousSourceId ||
                    preferredRoute.stream.providerId == previousProviderId
                routeNotice = if (keptSource) {
                    "已切到 $episodeLabel · 沿用 $routeLabel"
                } else {
                    "已切到 $episodeLabel · 原播放源不可用，改用 $routeLabel"
                }
                activePanel = null
                revealControls()
                currentStream = preferredRoute.stream
            } else {
                routeNotice = "${target.title} 暂时没有可用播放源"
            }
        }

        val cachedRoutes = graph.sourceRegistry.peekRouteCandidates(target)
        if (cachedRoutes != null) {
            applyEpisodeRoutes(cachedRoutes)
            return
        }

        routeNotice = "正在加载 ${target.title} 的播放源..."
        episodeLoadingId = target.id
        scope.launch {
            val result = runCatching { graph.sourceRegistry.resolveRouteCandidates(target) }
            result
                .onSuccess { candidates -> applyEpisodeRoutes(candidates) }
                .onFailure { failure ->
                    routeNotice = "选集加载失败：${failure.message ?: failure::class.simpleName.orEmpty().ifBlank { "未知错误" }}"
                }
            episodeLoadingId = null
        }
    }

    val overlayState = buildPlayerOverlayState(
        title = detail.title,
        episodeTitle = currentEpisode.title,
        stream = currentStream,
        route = currentRoute,
        playbackState = state.playbackStateLabel,
        notice = routeNotice,
        error = effectiveErrorMessage,
    )
    val nextEpisode = remember(detail.episodes, currentEpisode.id) {
        nextEpisodeForPlayer(detail.episodes, currentEpisode)
    }

    @Composable
    fun VideoStage(
        modifier: Modifier,
        compact: Boolean,
        safeAreaState: PlayerDanmakuSafeAreaUiState,
    ) {
        val currentDensity = LocalDensity.current
        val danmakuSafeArea = with(currentDensity) {
            DanmakuSafeArea(
                topInsetPx = safeAreaState.topInsetDp.dp.toPx(),
                bottomInsetPx = safeAreaState.bottomInsetDp.dp.toPx(),
                startInsetPx = safeAreaState.startInsetDp.dp.toPx(),
                endInsetPx = safeAreaState.endInsetDp.dp.toPx(),
                centerExcludedHeightPx = safeAreaState.centerExcludedHeightDp.dp.toPx(),
            )
        }
        Box(modifier.background(Color.Black)) {
            if (currentStream.protocol == StreamProtocol.BITTORRENT && torrentPlaybackUrl == null) {
                TorrentPlaceholderSurface(
                    state = torrentState,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                PlayerViewSurface(
                    engine = engine,
                    onSurfaceTap = ::toggleControls,
                    modifier = Modifier.fillMaxSize(),
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
                isPlaying = state.isPlaying,
                playbackSpeed = playbackSpeed,
                safeArea = danmakuSafeArea,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(3.5f)
                    .background(Color.Black.copy(alpha = 0.001f))
                    .pointerInput(currentStream.id, controlsLocked, activePanel != null) {
                        detectTapGestures(
                            onTap = {
                                toggleControls()
                            },
                            onDoubleTap = { tapOffset ->
                                if (!controlsLocked && activePanel == null) {
                                    playerDoubleTapSeekDeltaMs(
                                        tapX = tapOffset.x,
                                        surfaceWidthPx = size.width,
                                    )?.let { deltaMs -> seekBy(deltaMs, fromGesture = true) }
                                }
                            },
                        )
                    },
            )
            PlayerEdgeProgress(
                positionMs = playbackPositionMs,
                durationMs = playbackDurationMs,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().zIndex(3f),
            )
            AnimatedVisibility(
                visible = controlsVisible && !controlsLocked,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).zIndex(4f),
            ) {
                PlayerTopOverlay(
                    overlayState = overlayState,
                    onBack = onBack,
                    onOpenRoutePanel = {
                        revealControls()
                        activePanel = PlayerPanel.Route
                    },
                    onExitFullscreen = ::exitFullscreen,
                    compact = compact,
                    currentEpisode = currentEpisode,
                    episodeCount = detail.episodes.size,
                    routeCount = routeOptions.size,
                    routeCoverageLabel = routeCoverageLabel,
                    playbackSpeed = playbackSpeed,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            AnimatedVisibility(
                visible = controlsVisible && activePanel == null && !controlsLocked,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center).zIndex(4f),
            ) {
                PlayerCenterControls(
                    isPlaying = state.isPlaying,
                    compact = compact,
                    onSeekBackward = {
                        seekBy(-10_000L)
                    },
                    onTogglePlay = {
                        revealControls()
                        if (state.isPlaying) {
                            engine.player.pause()
                        } else {
                            engine.player.play()
                        }
                    },
                    onSeekForward = {
                        seekBy(10_000L)
                    },
                )
            }
            val seekFeedbackState = remember(seekFeedbackText, seekFeedbackPlacement, compact) {
                buildPlayerSeekFeedbackUiState(
                    text = seekFeedbackText.orEmpty(),
                    placement = seekFeedbackPlacement,
                    compact = compact,
                )
            }
            AnimatedVisibility(
                visible = seekFeedbackText != null && activePanel == null && !controlsLocked,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(seekFeedbackState.placement.feedbackAlignment())
                    .padding(horizontal = seekFeedbackState.horizontalMargin)
                    .offset(y = seekFeedbackState.verticalOffset)
                    .zIndex(4.6f),
            ) {
                PlayerSeekFeedbackPill(
                    state = seekFeedbackState,
                )
            }
            AnimatedVisibility(
                visible = controlsVisible && activePanel == null && !controlsLocked,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter).zIndex(4f),
            ) {
                PlayerBottomControls(
                    currentStream = currentStream,
                    currentRoute = currentRoute,
                    cacheActionState = cacheActionState,
                    routeOptions = routeOptions,
                    routeCoverageLabel = routeCoverageLabel,
                    routeNotice = routeNotice,
                    errorMessage = effectiveErrorMessage,
                    danmakuEnabled = danmakuEnabled,
                    density = density,
                    playbackSpeed = playbackSpeed,
                    activePanel = activePanel,
                    episodeCount = detail.episodes.size,
                    nextEpisode = nextEpisode,
                    positionMs = playbackPositionMs,
                    durationMs = playbackDurationMs,
                    compact = compact,
                    hasPlaybackIssue = hasPlaybackIssue,
                    canSelectNextRoute = nextRoute != null,
                    onSeek = {
                        revealControls()
                        engine.player.seekTo(it)
                    },
                    onSeekBackward = {
                        revealControls()
                        seekBy(-10_000L)
                    },
                    onSeekForward = {
                        revealControls()
                        seekBy(10_000L)
                    },
                    onShowPanel = { panel ->
                        revealControls()
                        activePanel = panel
                    },
                    onEnterFullscreen = ::enterFullscreen,
                    onNextEpisode = {
                        nextEpisode?.let(::selectEpisode)
                    },
                    onToggleDanmaku = {
                        revealControls()
                        danmakuEnabled = !danmakuEnabled
                    },
                    onOffline = ::enqueueCurrentStreamForOffline,
                    onRetryRoute = ::retryCurrentRoute,
                    onNextRoute = ::selectNextRoute,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            AnimatedVisibility(
                visible = !compact && activePanel == null && (controlsVisible || controlsLocked),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp).zIndex(4.5f),
            ) {
                PlayerFullscreenLockButton(
                    locked = controlsLocked,
                    onClick = {
                        if (controlsLocked) {
                            unlockFullscreenControls()
                        } else {
                            lockFullscreenControls()
                        }
                    },
                )
            }
            AnimatedVisibility(
                visible = !compact && activePanel == null && controlsVisible && !controlsLocked,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp).zIndex(4.5f),
            ) {
                PlayerFullscreenSideDock(
                    danmakuEnabled = danmakuEnabled,
                    routeCount = routeOptions.size,
                    episodeCount = detail.episodes.size,
                    onToggleDanmaku = {
                        revealControls()
                        danmakuEnabled = !danmakuEnabled
                    },
                    onShowPanel = { panel ->
                        revealControls()
                        activePanel = panel
                    },
                )
            }
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(AnimeBackground)) {
        val portrait = maxHeight > maxWidth
        val currentDanmakuSafeAreaState = buildPlayerDanmakuSafeAreaUiState(
            compact = portrait,
            controlsVisible = controlsVisible,
            controlsLocked = controlsLocked,
            panelOpen = activePanel != null,
            noticeVisible = routeNotice != null || effectiveErrorMessage != null,
            centerOverlayVisible = controlsVisible && activePanel == null && !controlsLocked,
            seekFeedbackVisible = seekFeedbackText != null,
        )
        LaunchedEffect(activity, portrait) {
            activity?.setPlayerImmersive(!portrait)
        }
        BackHandler {
            when {
                controlsLocked -> unlockFullscreenControls()
                activePanel != null -> {
                    activePanel = null
                    revealControls()
                }
                !portrait -> exitFullscreen()
                else -> onBack()
            }
        }
        if (portrait) {
            Column(Modifier.fillMaxSize()) {
                VideoStage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    compact = true,
                    safeAreaState = currentDanmakuSafeAreaState,
                )
                PortraitWatchInfoPanel(
                    detail = detail,
                    episode = currentEpisode,
                    stream = currentStream,
                    routes = routeOptions,
                    playbackState = state.playbackStateLabel,
                    routeNotice = routeNotice,
                    errorMessage = effectiveErrorMessage,
                    episodeLoadingId = episodeLoadingId,
                    hasPlaybackIssue = hasPlaybackIssue,
                    canSelectNextRoute = nextRoute != null,
                    onShowPanel = { panel ->
                        revealControls()
                        controlsVisible = true
                        activePanel = panel
                    },
                    onRetryRoute = ::retryCurrentRoute,
                    onNextRoute = ::selectNextRoute,
                    onEpisodeSelected = ::selectEpisode,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            VideoStage(
                modifier = Modifier.fillMaxSize(),
                compact = false,
                safeAreaState = currentDanmakuSafeAreaState,
            )
        }
        AnimatedVisibility(
            visible = controlsVisible && activePanel != null && !controlsLocked,
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
                    cacheActionState = cacheActionState,
                    danmakuEnabled = danmakuEnabled,
                    danmakuMatches = danmakuMatches,
                    danmakuMatching = danmakuMatching,
                    danmakuTimelineCount = danmakuItems.size,
                    danmakuSearchQuery = danmakuSearchQuery,
                    density = density,
                    danmakuAlpha = danmakuAlpha,
                    danmakuFontScale = danmakuFontScale,
                    danmakuSafeAreaState = currentDanmakuSafeAreaState,
                    playbackSpeed = playbackSpeed,
                    episodeLoadingId = episodeLoadingId,
                    routeNotice = routeNotice,
                    failedStreamIds = failedStreamIds,
                    onDismiss = { activePanel = null },
                    onRouteSelected = ::selectRoute,
                    onEpisodeSelected = ::selectEpisode,
                    onToggleDanmaku = {
                        revealControls()
                        danmakuEnabled = !danmakuEnabled
                    },
                    onDensityChange = {
                        revealControls()
                        density = it
                    },
                    onDanmakuAlphaChange = {
                        revealControls()
                        danmakuAlpha = it
                    },
                    onDanmakuFontScaleChange = {
                        revealControls()
                        danmakuFontScale = it
                    },
                    onSearchDanmaku = ::refreshDanmakuMapping,
                    onDanmakuSearchQueryChange = { danmakuSearchQuery = it },
                    onDanmakuCandidateSelected = ::calibrateDanmakuMapping,
                    onSpeedSelected = {
                        revealControls()
                        playbackSpeed = it
                        activePanel = null
                    },
                    onShowPanel = { nextPanel ->
                        revealControls()
                        activePanel = nextPanel
                    },
                    onOffline = ::enqueueCurrentStreamForOffline,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun PortraitWatchInfoPanel(
    detail: MediaDetail,
    episode: Episode,
    stream: MediaStream,
    routes: List<RouteCandidate>,
    playbackState: String,
    routeNotice: String?,
    errorMessage: String?,
    episodeLoadingId: String?,
    hasPlaybackIssue: Boolean,
    canSelectNextRoute: Boolean,
    onShowPanel: (PlayerPanel) -> Unit,
    onRetryRoute: () -> Unit,
    onNextRoute: () -> Unit,
    onEpisodeSelected: (Episode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val panelState = remember(detail, episode, stream, routes, playbackState, routeNotice, errorMessage, hasPlaybackIssue) {
        buildPortraitWatchInfoUiState(
            detail = detail,
            episode = episode,
            stream = stream,
            routes = routes,
            playbackState = playbackState,
            routeNotice = routeNotice,
            errorMessage = errorMessage,
            hasPlaybackIssue = hasPlaybackIssue,
        )
    }
    val episodeRailState = remember(detail, episode.id, episodeLoadingId) {
        buildPortraitEpisodeRailUiState(
            detail = detail,
            currentEpisode = episode,
            episodeLoadingId = episodeLoadingId,
            maxCount = 18,
        )
    }
    val recoveryState = remember(hasPlaybackIssue, canSelectNextRoute) {
        buildPortraitRecoveryActionsUiState(
            hasPlaybackIssue = hasPlaybackIssue,
            canSelectNextRoute = canSelectNextRoute,
        )
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(AnimeBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 15.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = panelState.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    panelState.metaChips.forEach { chip ->
                        VideoMetaChip(chip)
                    }
                }
                Text(
                    text = panelState.episodeTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AnimeMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = AnimePanelSoft.copy(alpha = 0.56f),
                border = BorderStroke(1.dp, AnimeBorder),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(AnimeAccentPink),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                                RouteStatusBadge("继续看", AnimeAccentPink)
                                Text(
                                    text = panelState.currentEpisodeLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.78f),
                                    maxLines = 1,
                                )
                            }
                            Text(
                                text = panelState.episodeTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = panelState.playbackBrief,
                                style = MaterialTheme.typography.bodySmall,
                                color = AnimeMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    if (panelState.actions.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            panelState.actions.forEach { action ->
                                PortraitPlaybackAction(
                                    state = action,
                                    icon = playerPanelTabIcon(action.kind),
                                    onClick = { onShowPanel(action.kind.asPlayerPanel()) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                    panelState.diagnostic?.let { diagnostic ->
                        PortraitRouteInsightRow(
                            routes = routes,
                            stream = stream,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = diagnostic.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = playerNoticeColor(diagnostic),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (recoveryState.visible) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            recoveryState.actions.forEach { action ->
                                PortraitRecoveryActionButton(
                                    action = action,
                                    onClick = when (action.kind) {
                                        PlayerActionKind.Retry -> onRetryRoute
                                        PlayerActionKind.NextRoute -> onNextRoute
                                        else -> ({})
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        }
        if (episodeRailState.visible) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(episodeRailState.title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { onShowPanel(PlayerPanel.Episode) }) {
                        Text(episodeRailState.allEpisodesLabel, color = AnimeAccentCyan)
                    }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(episodeRailState.items, key = { it.episode.id }) { railItem ->
                        val accent = sourceLibraryToneColor(railItem.tone)
                        Card(
                            onClick = { onEpisodeSelected(railItem.episode) },
                            enabled = railItem.enabled,
                            modifier = Modifier.width(82.dp).height(48.dp).focusable(enabled = railItem.focusEnabled),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = if (railItem.selected) AnimePanelSoft else AnimePanel),
                            border = BorderStroke(
                                1.dp,
                                if (railItem.selected || railItem.loading) accent else AnimeBorder,
                            ),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 9.dp, vertical = 7.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = railItem.indexLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (railItem.selected || railItem.loading) accent else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                )
                                Text(
                                    text = railItem.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when {
                                        railItem.selected -> Color.White
                                        railItem.loading -> accent
                                        else -> AnimeMuted
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    episodeRailState.moreAction?.let { moreAction ->
                        item {
                            PortraitEpisodeMoreCard(
                                state = moreAction,
                                onClick = { onShowPanel(PlayerPanel.Episode) },
                            )
                        }
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("简介", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    text = detail.summary.orEmpty().ifBlank { "暂无简介" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = AnimeMuted,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PortraitRecoveryActionButton(
    action: PlayerActionUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = sourceLibraryToneColor(action.tone)
    val containerColor = playerChromeBaseColor(action.portraitRecoveryContainerBaseColor)
    val contentColor = accent.copy(alpha = action.portraitRecoveryContentAlpha)
    TextButton(
        onClick = onClick,
        enabled = action.enabled,
        modifier = modifier
            .height(action.portraitRecoveryHeight)
            .background(
                containerColor.copy(alpha = action.portraitRecoveryContainerAlpha),
                RoundedCornerShape(action.portraitRecoveryCornerRadius),
            ),
        shape = RoundedCornerShape(action.portraitRecoveryCornerRadius),
    ) {
        Icon(
            playerActionIcon(action.kind),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(action.portraitRecoveryIconSize),
        )
        Spacer(Modifier.width(action.portraitRecoveryContentSpacing))
        Text(action.title, color = contentColor)
    }
}

@Composable
private fun PortraitPlaybackAction(
    state: PortraitWatchActionUiState,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = sourceLibraryToneColor(state.tone)
    val titleColor = playerChromeBaseColor(state.titleBaseColor)
    TextButton(
        onClick = onClick,
        enabled = state.enabled,
        modifier = modifier.height(state.height).focusable(enabled = state.focusEnabled),
        shape = RoundedCornerShape(state.cornerRadius),
        colors = ButtonDefaults.textButtonColors(
            containerColor = accent.copy(alpha = state.containerAlpha),
            contentColor = accent.copy(alpha = state.iconAlpha),
        ),
        contentPadding = PaddingValues(horizontal = state.horizontalPadding, vertical = state.verticalPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(state.contentSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(state.iconSize))
            Text(
                text = state.title,
                style = MaterialTheme.typography.labelMedium,
                color = titleColor.copy(alpha = state.titleAlpha),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                text = state.subtitle,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                color = accent.copy(alpha = state.subtitleAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PortraitEpisodeMoreCard(state: PortraitEpisodeMoreActionUiState, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(72.dp).height(48.dp).focusable(enabled = state.focusEnabled),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AnimePanelSoft),
        border = BorderStroke(1.dp, AnimeAccentCyan.copy(alpha = 0.42f)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 9.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(state.title, style = MaterialTheme.typography.labelLarge, color = AnimeAccentCyan, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(state.subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.78f), maxLines = 1)
        }
    }
}

@Composable
private fun PortraitRouteInsightRow(
    routes: List<RouteCandidate>,
    stream: MediaStream,
    modifier: Modifier = Modifier,
) {
    val state = remember(routes, stream) {
        buildPortraitRouteInsightUiState(routes = routes, stream = stream)
    }

    LazyRow(
        modifier = modifier.height(31.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        contentPadding = PaddingValues(end = 2.dp),
    ) {
        items(state.chips, key = { it.label }) { chip ->
            PortraitRouteInsightChip(chip = chip)
        }
    }
}

@Composable
private fun PortraitRouteInsightChip(chip: PortraitRouteInsightChipUiState) {
    val color = sourceLibraryToneColor(chip.tone)
    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(chip.label, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
        Text(
            text = chip.value,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.86f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PlayerTopOverlay(
    overlayState: PlayerOverlayState,
    onBack: () -> Unit,
    onOpenRoutePanel: () -> Unit,
    onExitFullscreen: () -> Unit,
    compact: Boolean,
    currentEpisode: Episode,
    episodeCount: Int,
    routeCount: Int,
    routeCoverageLabel: String,
    playbackSpeed: Float,
    modifier: Modifier = Modifier,
) {
    val topOverlayState = remember(overlayState, currentEpisode, episodeCount, routeCount, routeCoverageLabel, playbackSpeed) {
        buildPlayerTopOverlayUiState(
            overlayState = overlayState,
            currentEpisode = currentEpisode,
            episodeCount = episodeCount,
            routeCount = routeCount,
            routeCoverageLabel = routeCoverageLabel,
            playbackSpeed = playbackSpeed,
        )
    }
    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = if (compact) {
                        listOf(Color.Black.copy(alpha = 0.46f), Color.Transparent)
                    } else {
                        listOf(
                            Color.Black.copy(alpha = 0.82f),
                            Color.Black.copy(alpha = 0.46f),
                            Color.Transparent,
                        )
                    },
                ),
            )
            .padding(horizontal = if (compact) 10.dp else 14.dp, vertical = if (compact) 8.dp else 12.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 0.dp else 8.dp),
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
            if (compact) {
                Spacer(Modifier.weight(1f))
                topOverlayState.compactNotice?.let {
                    PlayerCompactStatusPill(
                        state = it,
                    )
                }
            } else {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = topOverlayState.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = topOverlayState.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                PlayerTopRouteStatus(
                    state = topOverlayState.routeStatus,
                    onClick = onOpenRoutePanel,
                    modifier = Modifier.widthIn(
                        min = topOverlayState.routeStatus.minWidth,
                        max = topOverlayState.routeStatus.maxWidth,
                    ),
                )
                PlayerCircleButton(
                    icon = Icons.Filled.FullscreenExit,
                    contentDescription = "退出全屏",
                    onClick = onExitFullscreen,
                )
            }
        }
        if (!compact) {
            PlayerTopStatusStrip(
                state = topOverlayState.statusStrip,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PlayerTopStatusStrip(
    state: PlayerTopStatusStripUiState,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.height(state.stripHeight),
        horizontalArrangement = Arrangement.spacedBy(state.itemSpacing),
        contentPadding = PaddingValues(end = state.endPadding),
    ) {
        items(state.chips, key = { it.label }) { chip ->
            PlayerTopStatusChip(
                state = chip,
            )
        }
    }
}

@Composable
private fun PlayerTopStatusChip(
    state: PlayerStatusChipUiState,
    modifier: Modifier = Modifier,
) {
    val accent = sourceLibraryToneColor(state.tone)
    val containerColor = playerChromeBaseColor(state.containerBaseColor)
    val valueColor = playerChromeBaseColor(state.valueBaseColor)
    Row(
        modifier = modifier
            .height(state.height)
            .widthIn(min = state.minWidth, max = state.maxWidth)
            .clip(RoundedCornerShape(state.cornerRadius))
            .background(containerColor.copy(alpha = state.containerAlpha))
            .border(state.borderWidth, accent.copy(alpha = state.borderAlpha), RoundedCornerShape(state.cornerRadius))
            .padding(horizontal = state.horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(state.contentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = state.label,
            style = MaterialTheme.typography.labelSmall,
            color = accent.copy(alpha = state.labelAlpha),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
        Text(
            text = state.value,
            modifier = Modifier.weight(1f, fill = false),
            style = MaterialTheme.typography.labelSmall,
            color = valueColor.copy(alpha = state.valueAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PlayerCompactStatusPill(
    state: PlayerNoticeUiState,
    modifier: Modifier = Modifier,
) {
    val accent = playerNoticeColor(state)
    val containerColor = playerChromeBaseColor(state.compactContainerBaseColor)
    Text(
        text = state.message,
        style = MaterialTheme.typography.labelSmall,
        color = accent.copy(alpha = state.compactTextAlpha),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .widthIn(max = state.compactMaxWidth)
            .height(state.compactHeight)
            .clip(RoundedCornerShape(state.compactCornerRadius))
            .background(containerColor.copy(alpha = state.compactContainerAlpha))
            .border(
                state.compactBorderWidth,
                accent.copy(alpha = state.compactBorderAlpha),
                RoundedCornerShape(state.compactCornerRadius),
            )
            .padding(horizontal = state.compactHorizontalPadding, vertical = state.compactVerticalPadding),
    )
}

@Composable
private fun PlayerTopRouteStatus(
    state: PlayerRouteStatusUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = playerRouteStatusColor(state)
    val containerColor = playerChromeBaseColor(state.containerBaseColor)
    val routeTextColor = playerChromeBaseColor(state.routeTextBaseColor)
    Surface(
        modifier = modifier.height(state.height).clickable(onClick = onClick).focusable(enabled = state.focusEnabled),
        shape = RoundedCornerShape(state.cornerRadius),
        color = containerColor.copy(alpha = state.containerAlpha),
        border = BorderStroke(state.borderWidth, accent.copy(alpha = state.borderAlpha)),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = state.horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(state.contentSpacing),
        ) {
            Box(
                modifier = Modifier
                    .size(state.indicatorSize)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = state.indicatorAlpha)),
            )
            Text(
                text = state.statusLabel,
                style = MaterialTheme.typography.labelSmall,
                color = accent.copy(alpha = state.statusTextAlpha),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                text = state.routeLabel,
                style = MaterialTheme.typography.labelMedium,
                color = routeTextColor.copy(alpha = state.routeTextAlpha),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "播放源",
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun PlayerCenterControls(
    isPlaying: Boolean,
    compact: Boolean,
    onSeekBackward: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeekForward: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (compact) {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.22f))
                .padding(5.dp),
            contentAlignment = Alignment.Center,
        ) {
            PlayerCircleButton(
                icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "暂停" else "播放",
                onClick = onTogglePlay,
                prominent = true,
                selected = true,
            )
        }
    } else {
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
}

@Composable
private fun PlayerSeekFeedbackPill(
    state: PlayerSeekFeedbackUiState,
    modifier: Modifier = Modifier,
) {
    val containerColor = playerChromeBaseColor(state.containerBaseColor)
    val textColor = playerChromeBaseColor(state.textBaseColor)
    val accent = sourceLibraryToneColor(state.borderTone)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(state.cornerRadius))
            .background(containerColor.copy(alpha = state.containerAlpha))
            .border(state.borderWidth, accent.copy(alpha = state.borderAlpha), RoundedCornerShape(state.cornerRadius))
            .padding(horizontal = state.horizontalPadding, vertical = state.verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(state.contentSpacing),
    ) {
        if (state.iconVisible) {
            state.placement.feedbackIcon()?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = sourceLibraryToneColor(state.iconTone),
                    modifier = Modifier.size(state.iconSize),
                )
            }
        }
        Text(
            text = state.text,
            style = MaterialTheme.typography.labelLarge,
            color = textColor.copy(alpha = state.textAlpha),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun PlayerSeekFeedbackPlacement.feedbackAlignment(): Alignment {
    return when (this) {
        PlayerSeekFeedbackPlacement.Center -> Alignment.Center
        PlayerSeekFeedbackPlacement.Start -> Alignment.CenterStart
        PlayerSeekFeedbackPlacement.End -> Alignment.CenterEnd
    }
}

private fun PlayerSeekFeedbackPlacement.feedbackIcon(): ImageVector? {
    return when (this) {
        PlayerSeekFeedbackPlacement.Center -> null
        PlayerSeekFeedbackPlacement.Start -> Icons.Filled.Replay10
        PlayerSeekFeedbackPlacement.End -> Icons.Filled.Forward10
    }
}

@Composable
private fun PlayerBottomControls(
    currentStream: MediaStream,
    currentRoute: RouteCandidate?,
    cacheActionState: PlayerCacheActionUiState,
    routeOptions: List<RouteCandidate>,
    routeCoverageLabel: String,
    routeNotice: String?,
    errorMessage: String?,
    danmakuEnabled: Boolean,
    density: Float,
    playbackSpeed: Float,
    activePanel: PlayerPanel?,
    episodeCount: Int,
    nextEpisode: Episode?,
    positionMs: Long,
    durationMs: Long,
    compact: Boolean,
    hasPlaybackIssue: Boolean,
    canSelectNextRoute: Boolean,
    onSeek: (Long) -> Unit,
    onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit,
    onShowPanel: (PlayerPanel) -> Unit,
    onEnterFullscreen: () -> Unit,
    onNextEpisode: () -> Unit,
    onToggleDanmaku: () -> Unit,
    onOffline: () -> Unit,
    onRetryRoute: () -> Unit,
    onNextRoute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingSeekMs by remember(currentStream.id) { mutableStateOf<Long?>(null) }
    val seekBarState = remember(positionMs, durationMs, pendingSeekMs) {
        buildPlayerSeekBarUiState(
            positionMs = positionMs,
            durationMs = durationMs,
            pendingSeekMs = pendingSeekMs,
        )
    }
    val compactInteractionState = remember(seekBarState.progressFraction, danmakuEnabled) {
        buildPlayerCompactInteractionUiState(
            progressFraction = seekBarState.progressFraction,
            danmakuEnabled = danmakuEnabled,
        )
    }
    val compactRecoveryState = remember(hasPlaybackIssue, canSelectNextRoute) {
        buildPlayerCompactRecoveryUiState(
            hasPlaybackIssue = hasPlaybackIssue,
            canSelectNextRoute = canSelectNextRoute,
        )
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
        verticalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 7.dp),
    ) {
        if (!compact) {
            val timeLabelColor = seekBarState.timeLabelTone
                ?.let { sourceLibraryToneColor(it) }
                ?: Color.White
            val sliderInactiveTrackColor = seekBarState.sliderInactiveTrackTone
                ?.let { sourceLibraryToneColor(it) }
                ?: Color.White
            val loadingTrackColor = sourceLibraryToneColor(seekBarState.loadingTrackTone)
            val loadingTrackBackgroundColor = seekBarState.loadingTrackBackgroundTone
                ?.let { sourceLibraryToneColor(it) }
                ?: Color.White
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(seekBarState.rowSpacing),
            ) {
                Text(
                    text = seekBarState.positionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = timeLabelColor.copy(alpha = seekBarState.currentTimeAlpha),
                    modifier = Modifier.width(seekBarState.timeLabelWidth),
                )
                if (seekBarState.seekable) {
                    Slider(
                        value = seekBarState.value,
                        onValueChange = { pendingSeekMs = it.toLong() },
                        onValueChangeFinished = {
                            pendingSeekMs?.let(onSeek)
                            pendingSeekMs = null
                        },
                        valueRange = seekBarState.valueRange,
                        steps = seekBarState.steps,
                        colors = SliderDefaults.colors(
                            thumbColor = sourceLibraryToneColor(seekBarState.sliderThumbTone),
                            activeTrackColor = sourceLibraryToneColor(seekBarState.sliderActiveTrackTone),
                            inactiveTrackColor = sliderInactiveTrackColor.copy(alpha = seekBarState.sliderInactiveTrackAlpha),
                        ),
                        modifier = Modifier.weight(1f).height(seekBarState.sliderHeight).focusable(enabled = seekBarState.sliderFocusEnabled),
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier.weight(1f).height(seekBarState.loadingTrackHeight),
                        color = loadingTrackColor,
                        trackColor = loadingTrackBackgroundColor.copy(alpha = seekBarState.loadingTrackBackgroundAlpha),
                    )
                }
                Text(
                    text = seekBarState.durationLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = timeLabelColor.copy(alpha = seekBarState.durationTimeAlpha),
                    modifier = Modifier.width(seekBarState.timeLabelWidth),
                )
            }

            val noticeState = buildPlayerFullscreenNoticeUiState(
                routeSummary = routeSummary,
                routeNotice = routeNotice,
                errorMessage = errorMessage,
            )
            if (noticeState != null) {
                PlayerFullscreenNoticeStrip(
                    state = noticeState,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

        } else {
            PlayerCompactInteractionRow(
                state = compactInteractionState,
                onToggleDanmaku = onToggleDanmaku,
                onOpenDanmakuSettings = { onShowPanel(PlayerPanel.Danmaku) },
                onEnterFullscreen = onEnterFullscreen,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (compact && compactRecoveryState.visible) {
            PlayerCompactRecoveryRow(
                state = compactRecoveryState,
                onRetryRoute = onRetryRoute,
                onNextRoute = onNextRoute,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (!compact) {
            PlayerFullscreenControlRow(
                routeSummary = routeSummary,
                quality = quality,
                routeCount = routeOptions.size,
                routeCoverageLabel = routeCoverageLabel,
                episodeCount = episodeCount,
                nextEpisode = nextEpisode,
                danmakuEnabled = danmakuEnabled,
                playbackSpeed = playbackSpeed,
                activePanel = activePanel,
                cacheActionState = cacheActionState,
                hasPlaybackIssue = hasPlaybackIssue,
                canSelectNextRoute = canSelectNextRoute,
                onToggleDanmaku = onToggleDanmaku,
                onSeekBackward = onSeekBackward,
                onSeekForward = onSeekForward,
                onShowPanel = onShowPanel,
                onNextEpisode = onNextEpisode,
                onOffline = onOffline,
                onRetryRoute = onRetryRoute,
                onNextRoute = onNextRoute,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PlayerFullscreenNoticeStrip(
    state: PlayerNoticeUiState,
    modifier: Modifier = Modifier,
) {
    val accent = playerNoticeColor(state)
    val containerColor = playerChromeBaseColor(state.fullscreenContainerBaseColor)
    val titleColor = playerChromeBaseColor(state.fullscreenTitleBaseColor)
    Row(
        modifier = modifier
            .height(state.fullscreenHeight)
            .clip(RoundedCornerShape(state.fullscreenCornerRadius))
            .background(containerColor.copy(alpha = state.fullscreenContainerAlpha))
            .border(
                state.fullscreenBorderWidth,
                accent.copy(alpha = state.fullscreenBorderAlpha),
                RoundedCornerShape(state.fullscreenCornerRadius),
            )
            .padding(horizontal = state.fullscreenHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(state.fullscreenContentSpacing),
    ) {
        Box(
            modifier = Modifier
                .size(state.fullscreenIndicatorSize)
                .clip(CircleShape)
                .background(accent.copy(alpha = state.fullscreenIndicatorAlpha)),
        )
        Text(
            text = state.title,
            style = MaterialTheme.typography.labelMedium,
            color = titleColor.copy(alpha = state.fullscreenTitleAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(state.fullscreenTitleWeight),
        )
        Text(
            text = state.message,
            style = MaterialTheme.typography.labelSmall,
            color = accent.copy(alpha = state.fullscreenMessageAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(state.fullscreenMessageWeight),
        )
    }
}

@Composable
private fun PlayerCompactInteractionRow(
    state: PlayerCompactInteractionUiState,
    onToggleDanmaku: () -> Unit,
    onOpenDanmakuSettings: () -> Unit,
    onEnterFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(state.columnSpacing),
    ) {
        PlayerCompactProgressLine(state = state.progress, modifier = Modifier.fillMaxWidth())
        Row(
            modifier = Modifier.fillMaxWidth().height(state.actionRowHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(state.actionRowSpacing),
        ) {
            PlayerCompactDanmakuInputBar(
                state = state.danmaku,
                onOpenDanmakuSettings = onOpenDanmakuSettings,
                onToggleDanmaku = onToggleDanmaku,
                modifier = Modifier.weight(1f),
            )
            PlayerTinyIconAction(
                state = state.fullscreenAction,
                icon = Icons.Filled.Fullscreen,
                onClick = onEnterFullscreen,
            )
        }
    }
}

@Composable
private fun PlayerCompactDanmakuInputBar(
    state: PlayerCompactDanmakuUiState,
    onOpenDanmakuSettings: () -> Unit,
    onToggleDanmaku: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val iconColor = state.iconTone
        ?.let { sourceLibraryToneColor(it) }
        ?: Color.White
    val textColor = state.textTone
        ?.let { sourceLibraryToneColor(it) }
        ?: Color.White
    val toggleContainerColor = state.toggleContainerTone
        ?.let { sourceLibraryToneColor(it) }
        ?: Color.White
    val toggleContentColor = state.toggleContentTone
        ?.let { sourceLibraryToneColor(it) }
        ?: Color.White
    Row(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(state.cornerRadius))
            .background(Color.Black.copy(alpha = state.containerAlpha))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onOpenDanmakuSettings,
            )
            .padding(start = state.startPadding, end = state.endPadding),
        horizontalArrangement = Arrangement.spacedBy(state.contentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.ClosedCaption,
            contentDescription = null,
            tint = iconColor.copy(alpha = state.iconAlpha),
            modifier = Modifier.size(state.iconSize),
        )
        Text(
            text = state.title,
            style = MaterialTheme.typography.bodySmall,
            color = textColor.copy(alpha = state.textAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        TextButton(
            onClick = onToggleDanmaku,
            modifier = Modifier.width(state.toggleWidth).height(state.toggleHeight).focusable(enabled = state.toggleFocusEnabled),
            shape = RoundedCornerShape(state.cornerRadius),
            colors = ButtonDefaults.textButtonColors(
                containerColor = toggleContainerColor.copy(alpha = state.toggleContainerAlpha),
                contentColor = toggleContentColor.copy(alpha = state.toggleContentAlpha),
            ),
            contentPadding = PaddingValues(0.dp),
        ) {
            Text(
                text = state.toggleLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun PlayerCompactProgressLine(
    state: PlayerCompactProgressUiState,
    modifier: Modifier = Modifier,
) {
    val trackColor = state.trackTone
        ?.let { sourceLibraryToneColor(it) }
        ?: Color.White
    val progressFraction = state.progressFraction
    if (progressFraction != null) {
        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = modifier.height(state.height).clip(RoundedCornerShape(state.cornerRadius)),
            color = sourceLibraryToneColor(state.progressTone),
            trackColor = trackColor.copy(alpha = state.trackAlpha),
        )
    } else {
        Box(
            modifier
                .height(state.height)
                .clip(RoundedCornerShape(state.cornerRadius))
                .background(trackColor.copy(alpha = state.trackAlpha)),
        )
    }
}

@Composable
private fun PlayerTinyIconAction(
    state: PlayerCompactFullscreenActionUiState,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = state.containerTone
        ?.let { sourceLibraryToneColor(it) }
        ?: playerChromeBaseColor(state.containerBaseColor)
    val contentColor = state.contentTone
        ?.let { sourceLibraryToneColor(it) }
        ?: playerChromeBaseColor(state.contentBaseColor)
    TextButton(
        onClick = onClick,
        enabled = state.enabled,
        modifier = modifier.width(state.width).height(state.height).focusable(enabled = state.focusEnabled),
        shape = RoundedCornerShape(state.cornerRadius),
        colors = ButtonDefaults.textButtonColors(
            containerColor = containerColor.copy(alpha = state.containerAlpha),
            contentColor = contentColor.copy(alpha = state.contentAlpha),
            disabledContainerColor = playerChromeBaseColor(state.disabledContainerBaseColor)
                .copy(alpha = state.disabledContainerAlpha),
            disabledContentColor = playerChromeBaseColor(state.disabledContentBaseColor)
                .copy(alpha = state.disabledContentAlpha),
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Icon(
            icon,
            contentDescription = state.contentDescription,
            modifier = Modifier.size(state.iconSize),
        )
    }
}

@Composable
private fun PlayerTinyToggle(
    state: PlayerCompactRecoveryActionUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.width(44.dp),
) {
    val containerColor = state.containerTone
        ?.let { sourceLibraryToneColor(it) }
        ?: Color.White
    val contentColor = state.contentTone
        ?.let { sourceLibraryToneColor(it) }
        ?: Color.White
    val disabledContainerColor = state.disabledContainerTone
        ?.let { sourceLibraryToneColor(it) }
        ?: Color.White
    val disabledContentColor = state.disabledContentTone
        ?.let { sourceLibraryToneColor(it) }
        ?: Color.White
    TextButton(
        onClick = onClick,
        enabled = state.action.enabled,
        modifier = modifier.height(state.height).focusable(enabled = state.action.actionFocusEnabled),
        shape = RoundedCornerShape(state.cornerRadius),
        colors = ButtonDefaults.textButtonColors(
            containerColor = containerColor.copy(alpha = state.containerAlpha),
            contentColor = contentColor.copy(alpha = state.contentAlpha),
            disabledContainerColor = disabledContainerColor.copy(alpha = state.disabledContainerAlpha),
            disabledContentColor = disabledContentColor.copy(alpha = state.disabledContentAlpha),
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(state.label, style = MaterialTheme.typography.labelMedium, maxLines = 1, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PlayerCompactRecoveryRow(
    state: PlayerCompactRecoveryUiState,
    onRetryRoute: () -> Unit,
    onNextRoute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(state.rowHeight),
        horizontalArrangement = Arrangement.spacedBy(state.actionSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        state.actions.forEach { actionState ->
            PlayerTinyToggle(
                state = actionState,
                onClick = when (actionState.action.kind) {
                    PlayerActionKind.Retry -> onRetryRoute
                    PlayerActionKind.NextRoute -> onNextRoute
                    else -> onRetryRoute
                },
                modifier = Modifier.weight(actionState.weight),
            )
        }
    }
}

@Composable
private fun PlayerFullscreenLockButton(
    locked: Boolean,
    onClick: () -> Unit,
) {
    val state = remember(locked) {
        buildPlayerFullscreenLockButtonUiState(locked = locked)
    }
    val containerColor = state.containerTone
        ?.let { sourceLibraryToneColor(it) }
        ?: playerChromeBaseColor(state.containerBaseColor)
    val contentColor = state.contentTone
        ?.let { sourceLibraryToneColor(it) }
        ?: playerChromeBaseColor(state.contentBaseColor)
    TextButton(
        onClick = onClick,
        modifier = Modifier.width(state.width).height(state.height).focusable(enabled = state.focusEnabled),
        shape = RoundedCornerShape(state.cornerRadius),
        colors = ButtonDefaults.textButtonColors(
            containerColor = containerColor.copy(alpha = state.containerAlpha),
            contentColor = contentColor.copy(alpha = state.contentAlpha),
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Icon(
            imageVector = if (state.locked) Icons.Filled.LockOpen else Icons.Filled.Lock,
            contentDescription = state.contentDescription,
            modifier = Modifier.size(state.iconSize),
        )
    }
}

@Composable
private fun PlayerFullscreenSideDock(
    danmakuEnabled: Boolean,
    routeCount: Int,
    episodeCount: Int,
    onToggleDanmaku: () -> Unit,
    onShowPanel: (PlayerPanel) -> Unit,
) {
    val state = remember(danmakuEnabled, routeCount, episodeCount) {
        buildPlayerFullscreenSideDockUiState(
            danmakuEnabled = danmakuEnabled,
            routeCount = routeCount,
            episodeCount = episodeCount,
        )
    }
    Column(
        modifier = Modifier
            .width(state.width)
            .clip(RoundedCornerShape(state.cornerRadius))
            .background(Color.Black.copy(alpha = state.containerAlpha))
            .border(1.dp, Color.White.copy(alpha = state.borderAlpha), RoundedCornerShape(state.cornerRadius))
            .padding(vertical = state.verticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(state.actionSpacing),
    ) {
        state.actions.forEach { action ->
            PlayerFullscreenDockButton(
                state = action,
                icon = playerMoreActionIcon(action.kind),
                onClick = playerFullscreenDockClick(
                    kind = action.kind,
                    onToggleDanmaku = onToggleDanmaku,
                    onShowPanel = onShowPanel,
                ),
            )
        }
    }
}

@Composable
private fun PlayerFullscreenDockButton(
    state: PlayerFullscreenDockActionUiState,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val containerColor = state.containerTone
        ?.let { sourceLibraryToneColor(it) }
        ?: Color.White
    val contentColor = state.contentTone
        ?.let { sourceLibraryToneColor(it) }
        ?: Color.White
    val disabledContainerColor = state.disabledContainerTone
        ?.let { sourceLibraryToneColor(it) }
        ?: Color.White
    val disabledContentColor = state.disabledContentTone
        ?.let { sourceLibraryToneColor(it) }
        ?: Color.White
    TextButton(
        onClick = onClick,
        enabled = state.enabled,
        modifier = Modifier.width(state.width).height(state.height).focusable(enabled = state.focusEnabled),
        shape = RoundedCornerShape(state.cornerRadius),
        colors = ButtonDefaults.textButtonColors(
            containerColor = containerColor.copy(alpha = state.containerAlpha),
            contentColor = contentColor.copy(alpha = state.contentAlpha),
            disabledContainerColor = disabledContainerColor.copy(alpha = state.disabledContainerAlpha),
            disabledContentColor = disabledContentColor.copy(alpha = state.disabledContentAlpha),
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(state.contentSpacing),
        ) {
            Icon(icon, contentDescription = state.label, modifier = Modifier.size(state.iconSize))
            Text(
                state.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun playerFullscreenDockClick(
    kind: PlayerMoreActionKind,
    onToggleDanmaku: () -> Unit,
    onShowPanel: (PlayerPanel) -> Unit,
): () -> Unit {
    fun show(panel: PlayerPanel): () -> Unit = { onShowPanel(panel) }
    return when (kind) {
        PlayerMoreActionKind.Danmaku -> onToggleDanmaku
        PlayerMoreActionKind.Quality -> show(PlayerPanel.Quality)
        PlayerMoreActionKind.Speed -> show(PlayerPanel.Speed)
        PlayerMoreActionKind.Episode -> show(PlayerPanel.Episode)
        PlayerMoreActionKind.Route -> show(PlayerPanel.Route)
        PlayerMoreActionKind.Cache -> show(PlayerPanel.More)
        PlayerMoreActionKind.More -> show(PlayerPanel.More)
    }
}

@Composable
private fun PlayerFullscreenControlRow(
    routeSummary: String,
    quality: String,
    routeCount: Int,
    routeCoverageLabel: String,
    episodeCount: Int,
    nextEpisode: Episode?,
    danmakuEnabled: Boolean,
    playbackSpeed: Float,
    activePanel: PlayerPanel?,
    cacheActionState: PlayerCacheActionUiState,
    hasPlaybackIssue: Boolean,
    canSelectNextRoute: Boolean,
    onToggleDanmaku: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit,
    onShowPanel: (PlayerPanel) -> Unit,
    onNextEpisode: () -> Unit,
    onOffline: () -> Unit,
    onRetryRoute: () -> Unit,
    onNextRoute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val controlRowState = remember { buildPlayerFullscreenControlRowUiState() }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(controlRowState.verticalSpacing),
    ) {
        PlayerFullscreenStatusStrip(
            routeSummary = routeSummary,
            quality = quality,
            routeCount = routeCount,
            routeCoverageLabel = routeCoverageLabel,
            episodeCount = episodeCount,
            playbackSpeed = playbackSpeed,
            hasPlaybackIssue = hasPlaybackIssue,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(controlRowState.actionRowSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayerDanmakuInputBar(
                danmakuEnabled = danmakuEnabled,
                onToggleDanmaku = onToggleDanmaku,
                onOpenDanmakuSettings = { onShowPanel(PlayerPanel.Danmaku) },
                modifier = Modifier.weight(controlRowState.danmakuInputWeight),
            )
            PlayerFullscreenSeekCluster(
                onSeekBackward = onSeekBackward,
                onSeekForward = onSeekForward,
            )
            PlayerActionBar(
                quality = quality,
                routeCount = routeCount,
                routeCoverageLabel = routeCoverageLabel,
                episodeCount = episodeCount,
                nextEpisode = nextEpisode,
                playbackSpeed = playbackSpeed,
                activePanel = activePanel,
                cacheActionState = cacheActionState,
                hasPlaybackIssue = hasPlaybackIssue,
                canSelectNextRoute = canSelectNextRoute,
                onShowPanel = onShowPanel,
                onNextEpisode = onNextEpisode,
                onOffline = onOffline,
                onRetryRoute = onRetryRoute,
                onNextRoute = onNextRoute,
                modifier = Modifier.weight(controlRowState.actionBarWeight),
            )
        }
    }
}

@Composable
private fun PlayerFullscreenStatusStrip(
    routeSummary: String,
    quality: String,
    routeCount: Int,
    routeCoverageLabel: String,
    episodeCount: Int,
    playbackSpeed: Float,
    hasPlaybackIssue: Boolean,
    modifier: Modifier = Modifier,
) {
    val state = remember(
        routeSummary,
        quality,
        routeCount,
        routeCoverageLabel,
        episodeCount,
        playbackSpeed,
        hasPlaybackIssue,
    ) {
        buildPlayerFullscreenStatusStripUiState(
            routeSummary = routeSummary,
            quality = quality,
            routeCount = routeCount,
            routeCoverageLabel = routeCoverageLabel,
            episodeCount = episodeCount,
            playbackSpeed = playbackSpeed,
            hasPlaybackIssue = hasPlaybackIssue,
        )
    }
    Row(
        modifier = modifier
            .height(state.height)
            .clip(RoundedCornerShape(state.cornerRadius))
            .background(playerChromeBaseColor(state.containerBaseColor).copy(alpha = state.containerAlpha))
            .border(
                state.borderWidth,
                if (state.error) {
                    MaterialTheme.colorScheme.error.copy(alpha = state.errorBorderAlpha)
                } else {
                    playerChromeBaseColor(state.borderBaseColor).copy(alpha = state.borderAlpha)
                },
                RoundedCornerShape(state.cornerRadius),
            )
            .padding(horizontal = state.horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(state.itemSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RouteStatusBadge(
            state.statusLabel,
            if (state.error) MaterialTheme.colorScheme.error else sourceLibraryToneColor(state.statusTone),
        )
        Text(
            text = state.routeSummary,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = playerChromeBaseColor(state.routeTextBaseColor).copy(alpha = state.routeTextAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        state.tags.forEach { tag ->
            PlayerStatusTinyText(tag)
        }
    }
}

@Composable
private fun PlayerStatusTinyText(state: PlayerFullscreenStatusTagUiState) {
    Text(
        text = state.label,
        style = MaterialTheme.typography.labelSmall,
        color = playerChromeBaseColor(state.textBaseColor).copy(alpha = state.textAlpha),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun PlayerDanmakuInputBar(
    danmakuEnabled: Boolean,
    onToggleDanmaku: () -> Unit,
    onOpenDanmakuSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val state = remember(danmakuEnabled) {
        buildPlayerFullscreenDanmakuInputUiState(danmakuEnabled = danmakuEnabled)
    }
    val iconColor = state.iconTone
        ?.let { sourceLibraryToneColor(it) }
        ?: playerChromeBaseColor(state.iconBaseColor)
    val toggleContainerColor = state.toggleContainerTone
        ?.let { sourceLibraryToneColor(it) }
        ?: playerChromeBaseColor(state.toggleContainerBaseColor)
    val toggleContentColor = state.toggleContentTone
        ?.let { sourceLibraryToneColor(it) }
        ?: playerChromeBaseColor(state.toggleContentBaseColor)
    Row(
        modifier = modifier
            .height(state.height)
            .clip(RoundedCornerShape(state.cornerRadius))
            .background(playerChromeBaseColor(state.containerBaseColor).copy(alpha = state.containerAlpha))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onOpenDanmakuSettings,
            )
            .padding(start = state.startPadding, end = state.endPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(state.contentSpacing),
    ) {
        Icon(
            Icons.Filled.ClosedCaption,
            contentDescription = null,
            tint = iconColor.copy(alpha = state.iconAlpha),
            modifier = Modifier.size(state.iconSize),
        )
        Text(
            text = state.title,
            style = MaterialTheme.typography.bodyMedium,
            color = playerChromeBaseColor(state.textBaseColor).copy(alpha = state.textAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        TextButton(
            onClick = onToggleDanmaku,
            modifier = Modifier.width(state.toggleWidth).height(state.toggleHeight).focusable(enabled = state.toggleFocusEnabled),
            shape = RoundedCornerShape(state.toggleCornerRadius),
            colors = ButtonDefaults.textButtonColors(
                containerColor = toggleContainerColor.copy(alpha = state.toggleContainerAlpha),
                contentColor = toggleContentColor.copy(alpha = state.toggleContentAlpha),
            ),
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
        ) {
            Text(
                text = state.toggleLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun PlayerFullscreenSeekCluster(
    onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = remember { buildPlayerFullscreenSeekClusterUiState() }
    Row(
        modifier = modifier
            .height(state.height)
            .clip(RoundedCornerShape(state.cornerRadius))
            .background(playerChromeBaseColor(state.containerBaseColor).copy(alpha = state.containerAlpha))
            .border(
                state.borderWidth,
                playerChromeBaseColor(state.borderBaseColor).copy(alpha = state.borderAlpha),
                RoundedCornerShape(state.cornerRadius),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        state.buttons.forEachIndexed { index, button ->
            PlayerFullscreenSeekButton(
                state = button,
                icon = playerFullscreenSeekIcon(button.kind),
                onClick = when (button.kind) {
                    PlayerSeekShortcutKind.Backward -> onSeekBackward
                    PlayerSeekShortcutKind.Forward -> onSeekForward
                },
            )
            if (index < state.buttons.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(state.dividerWidth)
                        .height(state.dividerHeight)
                        .background(
                            playerChromeBaseColor(state.dividerBaseColor)
                                .copy(alpha = state.dividerAlpha),
                        ),
                )
            }
        }
    }
}

@Composable
private fun PlayerFullscreenSeekButton(
    state: PlayerFullscreenSeekButtonUiState,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(width = state.width, height = state.height).focusable(enabled = state.focusEnabled),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = state.contentDescription,
            tint = playerChromeBaseColor(state.iconBaseColor).copy(alpha = state.iconAlpha),
            modifier = Modifier.size(state.iconSize),
        )
    }
}

private fun playerFullscreenSeekIcon(kind: PlayerSeekShortcutKind): ImageVector {
    return when (kind) {
        PlayerSeekShortcutKind.Backward -> Icons.Filled.Replay10
        PlayerSeekShortcutKind.Forward -> Icons.Filled.Forward10
    }
}

@Composable
private fun PlayerActionBar(
    quality: String,
    routeCount: Int,
    routeCoverageLabel: String,
    episodeCount: Int,
    nextEpisode: Episode?,
    playbackSpeed: Float,
    activePanel: PlayerPanel?,
    cacheActionState: PlayerCacheActionUiState,
    hasPlaybackIssue: Boolean,
    canSelectNextRoute: Boolean,
    onShowPanel: (PlayerPanel) -> Unit,
    onNextEpisode: () -> Unit,
    onOffline: () -> Unit,
    onRetryRoute: () -> Unit,
    onNextRoute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = remember(
        quality,
        routeCount,
        routeCoverageLabel,
        episodeCount,
        nextEpisode,
        playbackSpeed,
        activePanel,
        cacheActionState,
        hasPlaybackIssue,
        canSelectNextRoute,
    ) {
        buildPlayerActionBarUiState(
            quality = quality,
            routeCount = routeCount,
            routeCoverageLabel = routeCoverageLabel,
            episodeCount = episodeCount,
            nextEpisode = nextEpisode,
            playbackSpeed = playbackSpeed,
            activePanel = activePanel?.asPlayerPanelKind(),
            cacheAction = cacheActionState,
            hasPlaybackIssue = hasPlaybackIssue,
            canSelectNextRoute = canSelectNextRoute,
        )
    }
    val actions = state.actions.map { actionState ->
        PlayerActionSpec(
            state = actionState,
            icon = playerActionIcon(actionState.kind),
            onClick = playerActionClick(
                kind = actionState.kind,
                onShowPanel = onShowPanel,
                onNextEpisode = onNextEpisode,
                onOffline = onOffline,
                onRetryRoute = onRetryRoute,
                onNextRoute = onNextRoute,
            ),
        )
    }
    BoxWithConstraints(modifier = modifier) {
        val compactValues = maxWidth < if (actions.size > 6) 560.dp else 460.dp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (compactValues) 4.dp else 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions.forEach { action ->
                PlayerTextAction(
                    icon = action.icon,
                    title = action.state.title,
                    value = if (compactValues) null else action.state.value,
                    selected = action.state.selected,
                    enabled = action.state.enabled,
                    focusEnabled = action.state.actionFocusEnabled,
                    onClick = action.onClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private data class PlayerActionSpec(
    val state: PlayerActionUiState,
    val icon: ImageVector?,
    val onClick: () -> Unit,
)

private fun playerActionIcon(kind: PlayerActionKind): ImageVector {
    return when (kind) {
        PlayerActionKind.Retry -> Icons.Filled.Refresh
        PlayerActionKind.NextRoute -> Icons.Filled.VideoLibrary
        PlayerActionKind.Quality -> Icons.Filled.HighQuality
        PlayerActionKind.Speed -> Icons.Filled.Speed
        PlayerActionKind.Route -> Icons.Filled.VideoLibrary
        PlayerActionKind.Episode -> Icons.AutoMirrored.Filled.PlaylistPlay
        PlayerActionKind.NextEpisode -> Icons.Filled.SkipNext
        PlayerActionKind.Cache -> Icons.Filled.CloudDownload
        PlayerActionKind.More -> Icons.Filled.MoreVert
    }
}

private fun playerActionClick(
    kind: PlayerActionKind,
    onShowPanel: (PlayerPanel) -> Unit,
    onNextEpisode: () -> Unit,
    onOffline: () -> Unit,
    onRetryRoute: () -> Unit,
    onNextRoute: () -> Unit,
): () -> Unit {
    fun show(panel: PlayerPanel): () -> Unit = { onShowPanel(panel) }
    return when (kind) {
        PlayerActionKind.Retry -> onRetryRoute
        PlayerActionKind.NextRoute -> onNextRoute
        PlayerActionKind.Quality -> show(PlayerPanel.Quality)
        PlayerActionKind.Speed -> show(PlayerPanel.Speed)
        PlayerActionKind.Route -> show(PlayerPanel.Route)
        PlayerActionKind.Episode -> show(PlayerPanel.Episode)
        PlayerActionKind.NextEpisode -> onNextEpisode
        PlayerActionKind.Cache -> onOffline
        PlayerActionKind.More -> show(PlayerPanel.More)
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
    cacheActionState: PlayerCacheActionUiState,
    danmakuEnabled: Boolean,
    danmakuMatches: List<DanmakuMatch>,
    danmakuMatching: Boolean,
    danmakuTimelineCount: Int,
    danmakuSearchQuery: String,
    density: Float,
    danmakuAlpha: Float,
    danmakuFontScale: Float,
    danmakuSafeAreaState: PlayerDanmakuSafeAreaUiState,
    playbackSpeed: Float,
    episodeLoadingId: String?,
    routeNotice: String?,
    failedStreamIds: Set<String>,
    onDismiss: () -> Unit,
    onRouteSelected: (RouteCandidate) -> Unit,
    onEpisodeSelected: (Episode) -> Unit,
    onToggleDanmaku: () -> Unit,
    onDensityChange: (Float) -> Unit,
    onDanmakuAlphaChange: (Float) -> Unit,
    onDanmakuFontScaleChange: (Float) -> Unit,
    onSearchDanmaku: (String) -> Unit,
    onDanmakuSearchQueryChange: (String) -> Unit,
    onDanmakuCandidateSelected: (DanmakuMatch) -> Unit,
    onSpeedSelected: (Float) -> Unit,
    onShowPanel: (PlayerPanel) -> Unit,
    onOffline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val routeCoverageLabel = remember(routeOptions) { playerRouteCoverageLabel(routeOptions) }
    BoxWithConstraints(modifier = modifier) {
        val panelShellState = remember(maxWidth, maxHeight) {
            buildPlayerPanelShellUiState(maxWidth, maxHeight)
        }
        val currentRoute = routeOptions.firstOrNull { route ->
            route.stream.id == currentStream.id || route.stream.url == currentStream.url
        }
        val currentSourceLabel = currentRoute?.sourceName ?: currentStream.metadata["routeProviderName"] ?: currentStream.providerId
        val currentQualityLabel = currentStream.quality.orEmpty().ifBlank { "自动" }
        val selectedPanelKind = panel.asPlayerPanelKind()
        val panelSheetState = remember(
            selectedPanelKind,
            detail.title,
            currentEpisode.index,
            currentSourceLabel,
            currentQualityLabel,
            playbackSpeed,
            routeOptions.size,
            routeCoverageLabel,
            detail.episodes.size,
            danmakuEnabled,
        ) {
            buildPlayerPanelSheetUiState(
                selectedPanel = selectedPanelKind,
                title = detail.title,
                episodeIndex = currentEpisode.index,
                sourceLabel = currentSourceLabel,
                quality = currentQualityLabel,
                playbackSpeed = playbackSpeed,
                routeCount = routeOptions.size,
                routeCoverageLabel = routeCoverageLabel,
                episodeCount = detail.episodes.size,
                danmakuEnabled = danmakuEnabled,
            )
        }
        val panelInteractionSource = remember { MutableInteractionSource() }
        val panelModifier = if (panelShellState.landscape) {
            Modifier
                .align(Alignment.CenterEnd)
                .padding(
                    end = panelShellState.landscapeEndPadding,
                    top = panelShellState.landscapeTopPadding,
                    bottom = panelShellState.landscapeBottomPadding,
                )
                .fillMaxHeight()
                .width(panelShellState.panelWidth)
        } else {
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .heightIn(
                    min = panelShellState.portraitPanelMinHeight,
                    max = panelShellState.portraitPanelHeight,
                )
        }
        val panelShape = RoundedCornerShape(
            topStart = panelShellState.topStartRadius,
            topEnd = panelShellState.topEndRadius,
            bottomStart = panelShellState.bottomStartRadius,
            bottomEnd = panelShellState.bottomEndRadius,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = panelShellState.scrimAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )
        Surface(
            modifier = panelModifier.clickable(
                interactionSource = panelInteractionSource,
                indication = null,
                onClick = {},
            ),
            shape = panelShape,
            color = Color(panelShellState.surfaceColorArgb),
            border = BorderStroke(1.dp, Color.White.copy(alpha = panelShellState.borderAlpha)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(panelShellState.contentPadding),
                verticalArrangement = Arrangement.spacedBy(panelShellState.contentSpacing),
            ) {
                PlayerPanelHeader(
                    state = panelSheetState,
                    onDismiss = onDismiss,
                )
                PlayerPanelContextBar(
                    state = panelSheetState.context,
                    modifier = Modifier.fillMaxWidth(),
                )
                PlayerPanelQuickTabs(
                    state = panelSheetState,
                    onSelected = { kind -> onShowPanel(kind.asPlayerPanel()) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    when (panel) {
                        PlayerPanel.More -> PlayerMorePanel(
                            routeCount = routeOptions.size,
                            routeCoverageLabel = routeCoverageLabel,
                            episodeCount = detail.episodes.size,
                            routeLabel = currentRoute?.primaryRouteLabel() ?: currentStream.protocol.displayName(),
                            quality = currentQualityLabel,
                            playbackSpeed = playbackSpeed,
                            danmakuEnabled = danmakuEnabled,
                            cacheActionState = cacheActionState,
                            onShowPanel = onShowPanel,
                            onOffline = onOffline,
                        )
                        PlayerPanel.Danmaku -> PlayerDanmakuSettingsPanel(
                            danmakuEnabled = danmakuEnabled,
                            matches = danmakuMatches,
                            matching = danmakuMatching,
                            timelineCount = danmakuTimelineCount,
                            searchQuery = danmakuSearchQuery,
                            density = density,
                            alpha = danmakuAlpha,
                            fontScale = danmakuFontScale,
                            safeArea = danmakuSafeAreaState,
                            onToggleDanmaku = onToggleDanmaku,
                            onSearchDanmaku = onSearchDanmaku,
                            onSearchQueryChange = onDanmakuSearchQueryChange,
                            onCandidateSelected = onDanmakuCandidateSelected,
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
                            failedStreamIds = failedStreamIds,
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
private fun PlayerPanelHeader(state: PlayerPanelSheetUiState, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(state.headerSpacing),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(state.headerTextSpacing),
        ) {
            Text(
                text = state.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = state.titleAlpha),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = state.subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = state.subtitleAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        TextButton(onClick = onDismiss, modifier = Modifier.height(state.dismissButtonHeight)) {
            Text(state.dismissLabel, color = Color.White.copy(alpha = state.dismissLabelAlpha), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun PlayerPanelContextBar(
    state: PlayerPanelContextUiState,
    modifier: Modifier = Modifier,
) {
    val iconColor = sourceLibraryToneColor(state.iconTone)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(state.cornerRadius),
        color = Color.White.copy(alpha = state.containerAlpha),
        border = BorderStroke(1.dp, Color.White.copy(alpha = state.borderAlpha)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = state.horizontalPadding, vertical = state.verticalPadding),
            horizontalArrangement = Arrangement.spacedBy(state.rowSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(state.iconBoxSize)
                    .clip(RoundedCornerShape(state.iconCornerRadius))
                    .background(iconColor.copy(alpha = state.iconContainerAlpha)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(state.iconSize),
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(state.textSpacing)) {
                Text(
                    state.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = state.titleAlpha),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    state.metadata,
                    style = MaterialTheme.typography.labelSmall,
                    color = AnimeMuted.copy(alpha = state.metadataAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            RouteStatusBadge(state.statusLabel, sourceLibraryToneColor(state.statusTone))
        }
    }
}

@Composable
private fun PlayerPanelQuickTabs(
    state: PlayerPanelSheetUiState,
    onSelected: (PlayerPanelKind) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.height(state.tabStrip.height),
        horizontalArrangement = Arrangement.spacedBy(state.tabStrip.itemSpacing),
        contentPadding = PaddingValues(horizontal = state.tabStrip.contentPaddingHorizontal),
    ) {
        items(state.tabs, key = { it.kind }) { tab ->
            PlayerPanelQuickTab(
                tab = tab,
                icon = playerPanelTabIcon(tab.kind),
                onClick = { onSelected(tab.kind) },
            )
        }
    }
}

@Composable
private fun PlayerPanelQuickTab(
    tab: PlayerPanelTabUiState,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val baseColor = if (tab.usesVisualTone) {
        sourceLibraryToneColor(tab.visualTone)
    } else {
        Color.White
    }
    val contentColor = baseColor.copy(alpha = tab.contentAlpha)
    TextButton(
        onClick = onClick,
        enabled = tab.actionEnabled,
        modifier = Modifier.width(tab.width).height(tab.height).focusable(enabled = tab.tabFocusEnabled),
        shape = RoundedCornerShape(tab.cornerRadius),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (tab.prominent) sourceLibraryToneColor(tab.visualTone).copy(alpha = tab.containerAlpha) else Color.White.copy(alpha = tab.containerAlpha),
            contentColor = contentColor,
            disabledContainerColor = Color.White.copy(alpha = tab.containerAlpha),
            disabledContentColor = contentColor,
        ),
        contentPadding = PaddingValues(horizontal = tab.horizontalPadding, vertical = tab.verticalPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tab.contentSpacing),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(tab.iconSize))
            Text(
                text = tab.label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            tab.value?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = baseColor.copy(alpha = tab.valueAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PlayerMorePanel(
    routeCount: Int,
    routeCoverageLabel: String,
    episodeCount: Int,
    routeLabel: String,
    quality: String,
    playbackSpeed: Float,
    danmakuEnabled: Boolean,
    cacheActionState: PlayerCacheActionUiState,
    onShowPanel: (PlayerPanel) -> Unit,
    onOffline: () -> Unit,
) {
    val state = remember(
        routeCount,
        routeCoverageLabel,
        episodeCount,
        routeLabel,
        quality,
        playbackSpeed,
        danmakuEnabled,
        cacheActionState,
    ) {
        buildPlayerMorePanelUiState(
            routeCount = routeCount,
            routeCoverageLabel = routeCoverageLabel,
            episodeCount = episodeCount,
            routeLabel = routeLabel,
            quality = quality,
            playbackSpeed = playbackSpeed,
            danmakuEnabled = danmakuEnabled,
            cacheAction = cacheActionState,
        )
    }
    val actions = state.actions.map { actionState ->
        PlayerMoreAction(
            state = actionState,
            icon = playerMoreActionIcon(actionState.kind),
            onClick = playerMoreActionClick(actionState.kind, onShowPanel, onOffline),
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PlayerMoreSummaryCard(
            state = state,
            modifier = Modifier.fillMaxWidth(),
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 6.dp),
        ) {
            items(actions.chunked(2)) { rowActions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    rowActions.forEach { action ->
                        PlayerMoreActionTile(action, modifier = Modifier.weight(1f))
                    }
                    if (rowActions.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerMoreSummaryCard(
    state: PlayerMorePanelUiState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = AnimePanelSoft.copy(alpha = 0.68f),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AnimeAccentPink.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = AnimeAccentPink, modifier = Modifier.size(19.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                    RouteStatusBadge(state.summaryBadge, AnimeAccentPink)
                    Text(
                        text = state.summaryPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.82f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(
                    text = state.summarySecondary,
                    style = MaterialTheme.typography.bodySmall,
                    color = AnimeMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private data class PlayerMoreAction(
    val state: PlayerMoreActionUiState,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

private fun playerMoreActionIcon(kind: PlayerMoreActionKind): ImageVector {
    return when (kind) {
        PlayerMoreActionKind.Quality -> Icons.Filled.HighQuality
        PlayerMoreActionKind.Speed -> Icons.Filled.Speed
        PlayerMoreActionKind.Episode -> Icons.AutoMirrored.Filled.PlaylistPlay
        PlayerMoreActionKind.Route -> Icons.Filled.VideoLibrary
        PlayerMoreActionKind.Danmaku -> Icons.Filled.ClosedCaption
        PlayerMoreActionKind.Cache -> Icons.Filled.CloudDownload
        PlayerMoreActionKind.More -> Icons.Filled.MoreVert
    }
}

private fun playerPanelTabIcon(kind: PlayerPanelKind): ImageVector {
    return when (kind) {
        PlayerPanelKind.Quality -> Icons.Filled.HighQuality
        PlayerPanelKind.Speed -> Icons.Filled.Speed
        PlayerPanelKind.Route -> Icons.Filled.VideoLibrary
        PlayerPanelKind.Episode -> Icons.AutoMirrored.Filled.PlaylistPlay
        PlayerPanelKind.Danmaku -> Icons.Filled.ClosedCaption
        PlayerPanelKind.More -> Icons.Filled.MoreVert
    }
}

private fun playerMoreActionClick(
    kind: PlayerMoreActionKind,
    onShowPanel: (PlayerPanel) -> Unit,
    onOffline: () -> Unit,
): () -> Unit {
    fun show(panel: PlayerPanel): () -> Unit = { onShowPanel(panel) }
    return when (kind) {
        PlayerMoreActionKind.Quality -> show(PlayerPanel.Quality)
        PlayerMoreActionKind.Speed -> show(PlayerPanel.Speed)
        PlayerMoreActionKind.Episode -> show(PlayerPanel.Episode)
        PlayerMoreActionKind.Route -> show(PlayerPanel.Route)
        PlayerMoreActionKind.Danmaku -> show(PlayerPanel.Danmaku)
        PlayerMoreActionKind.Cache -> onOffline
        PlayerMoreActionKind.More -> show(PlayerPanel.More)
    }
}

@Composable
private fun PlayerMoreActionTile(action: PlayerMoreAction, modifier: Modifier = Modifier) {
    val state = action.state
    val accent = sourceLibraryToneColor(state.tone)
    val border = if (state.highlighted) {
        BorderStroke(1.dp, accent.copy(alpha = state.borderAlpha))
    } else {
        BorderStroke(1.dp, Color.White.copy(alpha = state.borderAlpha))
    }
    val container = if (state.prominent) {
        accent.copy(alpha = state.containerAlpha)
    } else {
        Color.White.copy(alpha = state.containerAlpha)
    }
    Surface(
        modifier = modifier
            .height(58.dp)
            .alpha(state.tileAlpha)
            .clickable(enabled = state.actionEnabled, onClick = action.onClick),
        shape = RoundedCornerShape(8.dp),
        color = container,
        border = border,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (state.prominent) {
                            accent.copy(alpha = state.iconContainerAlpha)
                        } else {
                            Color.Black.copy(alpha = state.iconContainerAlpha)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = state.title,
                    tint = if (state.prominent) accent.copy(alpha = state.iconAlpha) else Color.White.copy(alpha = state.iconAlpha),
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = state.titleAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = state.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = state.subtitleAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PlayerDanmakuSettingsPanel(
    danmakuEnabled: Boolean,
    matches: List<DanmakuMatch>,
    matching: Boolean,
    timelineCount: Int,
    searchQuery: String,
    density: Float,
    alpha: Float,
    fontScale: Float,
    safeArea: PlayerDanmakuSafeAreaUiState,
    onToggleDanmaku: () -> Unit,
    onSearchDanmaku: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCandidateSelected: (DanmakuMatch) -> Unit,
    onDensityChange: (Float) -> Unit,
    onAlphaChange: (Float) -> Unit,
    onFontScaleChange: (Float) -> Unit,
) {
    val state = remember(danmakuEnabled, density, alpha, fontScale, matches, matching, timelineCount, safeArea) {
        buildPlayerDanmakuSettingsUiState(
            enabled = danmakuEnabled,
            density = density,
            alpha = alpha,
            fontScale = fontScale,
            matches = matches,
            matching = matching,
            timelineCount = timelineCount,
            safeArea = safeArea,
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PlayerSelectableRow(
            title = state.toggleTitle,
            subtitle = state.toggleSubtitle,
            selected = state.toggleSelected,
            icon = Icons.Filled.ClosedCaption,
            trailing = state.toggleActionLabel,
            badges = state.toggleBadges,
            actionEnabled = state.toggleActionEnabled,
            iconAlpha = state.toggleIconAlpha,
            titleAlpha = state.toggleTitleAlpha,
            subtitleAlpha = state.toggleSubtitleAlpha,
            trailingTone = state.tone,
            rowState = state.toggleRowState,
            onClick = onToggleDanmaku,
        )
        PlayerSelectableRow(
            title = state.mapping.title,
            subtitle = state.mapping.subtitle,
            selected = state.mapping.selected,
            icon = Icons.Filled.Search,
            enabled = state.mapping.actionEnabled,
            trailing = state.mapping.actionLabel,
            badges = state.mapping.badges,
            actionEnabled = state.mapping.actionEnabled,
            iconAlpha = state.mapping.iconAlpha,
            titleAlpha = state.mapping.titleAlpha,
            subtitleAlpha = state.mapping.subtitleAlpha,
            trailingTone = state.mapping.trailingTone,
            rowState = state.mapping.rowState,
            onClick = { onSearchDanmaku(searchQuery) },
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = !matching,
            singleLine = true,
            label = { Text("弹幕番名") },
            placeholder = { Text("中文名 / 原名 / 别名") },
            trailingIcon = {
                IconButton(
                    enabled = !matching && searchQuery.isNotBlank(),
                    onClick = { onSearchDanmaku(searchQuery) },
                ) {
                    Icon(Icons.Filled.Search, contentDescription = "搜索弹幕候选")
                }
            },
        )
        if (state.mapping.candidates.isNotEmpty()) {
            Text(
                state.mapping.candidateListTitle,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = state.mapping.candidateListTitleAlpha),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Column(verticalArrangement = Arrangement.spacedBy(state.mapping.candidateSpacing)) {
                state.mapping.candidates.forEach { candidate ->
                    PlayerSelectableRow(
                        title = candidate.title,
                        subtitle = candidate.subtitle,
                        selected = candidate.selected,
                        icon = Icons.Filled.ClosedCaption,
                        enabled = candidate.enabled,
                        trailing = candidate.actionLabel,
                        badges = candidate.badges,
                        actionEnabled = candidate.actionEnabled,
                        iconAlpha = candidate.iconAlpha,
                        titleAlpha = candidate.titleAlpha,
                        subtitleAlpha = candidate.subtitleAlpha,
                        trailingTone = candidate.trailingTone,
                        rowState = candidate.rowState,
                        onClick = { onCandidateSelected(candidate.match) },
                    )
                }
            }
        }
        PlayerSliderSetting(
            state = state.densitySlider,
            onValueChange = onDensityChange,
        )
        PlayerSliderSetting(
            state = state.alphaSlider,
            onValueChange = onAlphaChange,
        )
        PlayerSliderSetting(
            state = state.fontScaleSlider,
            onValueChange = onFontScaleChange,
        )
        Text(
            state.safetySummary,
            style = MaterialTheme.typography.labelSmall,
            color = sourceLibraryToneColor(state.tone),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PlayerQualityPanel(
    routes: List<RouteCandidate>,
    currentStream: MediaStream,
    onRouteSelected: (RouteCandidate) -> Unit,
) {
    val state = remember(routes, currentStream.id, currentStream.quality) {
        buildPlayerQualityPanelUiState(routes, currentStream)
    }
    if (!state.hasOptions) {
        Text(state.emptyText, style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text(
                state.summary,
                style = MaterialTheme.typography.labelMedium,
                color = AnimeMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        items(state.options, key = { it.route.stream.id }) { option ->
            PlayerSelectableRow(
                title = option.title,
                subtitle = option.subtitle,
                selected = option.selected,
                icon = Icons.Filled.HighQuality,
                enabled = option.enabled,
                trailing = option.actionLabel,
                badges = option.badges,
                actionEnabled = option.actionEnabled,
                iconAlpha = option.iconAlpha,
                titleAlpha = option.titleAlpha,
                subtitleAlpha = option.subtitleAlpha,
                trailingTone = option.tone,
                rowState = option.rowState,
                onClick = { onRouteSelected(option.route) },
            )
        }
    }
}

@Composable
private fun PlayerSpeedPanel(
    playbackSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
) {
    val state = remember(playbackSpeed) {
        buildPlayerSpeedPanelUiState(playbackSpeed)
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text(
                state.summary,
                style = MaterialTheme.typography.labelMedium,
                color = AnimeMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        items(state.options) { option ->
            PlayerSelectableRow(
                title = option.title,
                subtitle = option.subtitle,
                selected = option.selected,
                icon = Icons.Filled.Speed,
                enabled = option.enabled,
                trailing = option.actionLabel,
                badges = option.badges,
                actionEnabled = option.actionEnabled,
                iconAlpha = option.iconAlpha,
                titleAlpha = option.titleAlpha,
                subtitleAlpha = option.subtitleAlpha,
                trailingTone = option.tone,
                rowState = option.rowState,
                onClick = { onSpeedSelected(option.speed) },
            )
        }
    }
}

@Composable
private fun PlayerRoutePanel(
    routes: List<RouteCandidate>,
    selectedStreamId: String,
    routeNotice: String?,
    failedStreamIds: Set<String>,
    onRouteSelected: (RouteCandidate) -> Unit,
) {
    if (routes.isEmpty()) {
        PlayerRoutePanelEmpty(state = buildPlayerRoutePanelEmptyUiState())
        return
    }
    val currentSourceId = remember(routes, selectedStreamId) {
        routes.firstOrNull { it.stream.id == selectedStreamId }?.sourceId
    }
    var selectedSourceId by remember(routes, selectedStreamId) { mutableStateOf(currentSourceId) }
    var detailedMode by remember(routes, selectedStreamId) { mutableStateOf(false) }
    val availableSourceIds = remember(routes) { routes.map { it.sourceId }.toSet() }
    LaunchedEffect(availableSourceIds, selectedSourceId) {
        if (selectedSourceId != null && selectedSourceId !in availableSourceIds) {
            selectedSourceId = null
        }
    }
    val panelState = remember(routes, selectedStreamId, failedStreamIds) {
        buildRoutePanelUiState(routes, selectedStreamId, failedStreamIds)
    }
    val recommendedStreamId = panelState.recommendedRoute?.stream?.id
    val sourceStripState = remember(routes, selectedSourceId, selectedStreamId, recommendedStreamId, failedStreamIds, detailedMode) {
        buildPlayerRouteSourceStripUiState(
            routes = routes,
            selectedStreamId = selectedStreamId,
            selectedSourceId = selectedSourceId,
            recommendedStreamId = recommendedStreamId,
            failedStreamIds = failedStreamIds,
            detailedMode = detailedMode,
        )
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            RoutePanelSummaryCard(
                state = panelState,
                notice = routeNotice,
                detailedMode = detailedMode,
                onToggleDetailed = { detailedMode = !detailedMode },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (sourceStripState.visible) {
            item {
                PlayerRouteSourceStrip(
                    state = sourceStripState,
                    onSourceSelected = { selectedSourceId = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        item {
            val routeListTitleColor = playerChromeBaseColor(sourceStripState.routeListTitleBaseColor)
            Text(
                sourceStripState.routeListTitle,
                style = MaterialTheme.typography.labelMedium,
                color = routeListTitleColor.copy(alpha = sourceStripState.routeListTitleAlpha),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        items(sourceStripState.visibleRoutes, key = { it.stream.id }) { route ->
            val failed = route.stream.id in failedStreamIds
            val recommended = route.stream.id == panelState.recommendedRoute?.stream?.id
            val selected = route.stream.id == selectedStreamId
            PlayerRouteOptionRow(
                route = route,
                selected = selected,
                recommended = recommended,
                failed = failed,
                detailedMode = detailedMode,
                onClick = { onRouteSelected(route) },
            )
        }
    }
}

@Composable
private fun PlayerRoutePanelEmpty(
    state: PlayerRoutePanelEmptyUiState,
    modifier: Modifier = Modifier,
) {
    val titleColor = playerChromeBaseColor(state.titleBaseColor)
    val summaryColor = sourceLibraryToneColor(state.summaryTone)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(state.verticalSpacing)) {
        Text(
            state.title,
            style = MaterialTheme.typography.titleSmall,
            color = titleColor.copy(alpha = state.titleAlpha),
            fontWeight = FontWeight.SemiBold,
            maxLines = state.titleMaxLines,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            state.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = summaryColor.copy(alpha = state.summaryAlpha),
            maxLines = state.summaryMaxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PlayerRouteSourceStrip(
    state: PlayerRouteSourceStripUiState,
    onSourceSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleColor = playerChromeBaseColor(state.titleBaseColor)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(state.containerSpacing)) {
        Text(
            state.title,
            style = MaterialTheme.typography.labelMedium,
            color = titleColor.copy(alpha = state.titleAlpha),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(state.chipSpacing)) {
            items(state.chips, key = { it.group.id }) { chip ->
                PlayerRouteSourceChip(
                    state = chip,
                    onClick = {
                        onSourceSelected(if (chip.group.isAll) null else chip.group.id)
                    },
                )
            }
        }
    }
}

@Composable
private fun PlayerRouteSourceChip(
    state: PlayerRouteSourceChipUiState,
    onClick: () -> Unit,
) {
    val group = state.group
    val borderColor = sourceLibraryToneColor(state.borderTone)
    val statusColor = sourceLibraryToneColor(state.statusTone)
    val containerColor = playerChromeBaseColor(state.containerBaseColor)
    val titleColor = playerChromeBaseColor(state.titleBaseColor)
    val detailColor = playerChromeBaseColor(state.detailBaseColor)
    val footerColor = if (state.footerError) MaterialTheme.colorScheme.error else sourceLibraryToneColor(state.footerTone)
    Card(
        onClick = onClick,
        modifier = Modifier.width(state.width).height(state.height),
        shape = RoundedCornerShape(state.cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = containerColor.copy(alpha = state.containerAlpha),
        ),
        border = BorderStroke(state.borderWidth, borderColor.copy(alpha = state.borderAlpha)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(state.contentPadding),
            verticalArrangement = if (state.detailVisible) Arrangement.SpaceBetween else Arrangement.Center,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(state.headerSpacing), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    group.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = titleColor.copy(alpha = state.titleAlpha),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                RouteStatusBadge(group.statusLabel, statusColor)
            }
            if (state.detailVisible) {
                Text(
                    group.detailSummary,
                    style = MaterialTheme.typography.labelSmall,
                    color = detailColor.copy(alpha = state.detailAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    group.footerLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = footerColor.copy(alpha = state.footerAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PlayerRouteOptionRow(
    route: RouteCandidate,
    selected: Boolean,
    recommended: Boolean,
    failed: Boolean,
    detailedMode: Boolean,
    onClick: () -> Unit,
) {
    val state = buildRouteCandidateUiState(
        route = route,
        selected = selected,
        recommended = recommended,
        failed = failed,
    )
    val accent = sourceLibraryToneColor(state.accentTone)
    val actionColor = sourceLibraryToneColor(state.actionTone)
    val primaryTitle = if (detailedMode) state.detailedTitle else state.compactTitle
    val secondaryTitle = if (detailedMode) state.detailedSubtitle else state.compactSubtitle
    val secondaryTone = if (detailedMode) state.detailedSubtitleTone else state.compactSubtitleTone
    val secondaryAlpha = if (detailedMode) state.detailedSubtitleAlpha else state.compactSubtitleAlpha
    val detailLineColor = sourceLibraryToneColor(state.detailLineTone)
    val protocolColor = sourceLibraryToneColor(state.protocolTone)
    val sizeColor = sourceLibraryToneColor(state.sizeTone)
    val rowContainerColor = playerChromeBaseColor(state.rowContainerBaseColor)
    val rowDisabledContainerColor = playerChromeBaseColor(state.rowDisabledContainerBaseColor)
    val rowBorderBaseColor = playerChromeBaseColor(state.rowBorderBaseColor)
    val rowTitleColor = playerChromeBaseColor(state.rowTitleBaseColor)
    val borderColor = state.borderTone?.let(::sourceLibraryToneColor) ?: rowBorderBaseColor
    Card(
        onClick = onClick,
        enabled = state.enabled,
        modifier = Modifier.fillMaxWidth().focusable(enabled = state.rowFocusEnabled),
        shape = RoundedCornerShape(state.rowCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = rowContainerColor.copy(alpha = state.containerAlpha),
            disabledContainerColor = rowDisabledContainerColor.copy(alpha = state.disabledContainerAlpha),
        ),
        border = BorderStroke(state.rowBorderWidth, borderColor.copy(alpha = state.borderAlpha)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(state.rowPadding),
            horizontalArrangement = Arrangement.spacedBy(state.rowSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(state.railWidth)
                    .height(if (detailedMode) state.detailedRailHeight else state.compactRailHeight)
                    .clip(RoundedCornerShape(state.rowCornerRadius))
                    .background(accent.copy(alpha = state.rowRailAlpha)),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(state.textColumnSpacing)) {
                Row(horizontalArrangement = Arrangement.spacedBy(state.titleBadgeSpacing), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        primaryTitle,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        color = rowTitleColor.copy(alpha = state.rowTitleAlpha),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    state.badges.forEach { badge ->
                        RouteStatusBadge(badge.label, sourceLibraryToneColor(badge.tone))
                    }
                }
                Text(
                    secondaryTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = sourceLibraryToneColor(secondaryTone).copy(alpha = secondaryAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (detailedMode) {
                    Text(
                        state.detailLine,
                        style = MaterialTheme.typography.bodySmall,
                        color = detailLineColor.copy(alpha = state.detailLineAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(state.trailingSpacing)) {
                if (detailedMode) {
                    Text(
                        state.protocolLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = protocolColor.copy(alpha = state.protocolAlpha),
                        maxLines = 1,
                    )
                    state.sizeLabel?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.labelSmall,
                            color = sizeColor.copy(alpha = state.sizeAlpha),
                            maxLines = 1,
                        )
                    }
                }
                PlayerRouteActionLabel(state = state, color = actionColor)
            }
        }
    }
}

@Composable
private fun PlayerRouteActionLabel(
    state: RouteCandidateUiState,
    color: Color,
) {
    Row(
        modifier = Modifier
            .height(state.actionLabelHeight)
            .clip(RoundedCornerShape(state.actionLabelCornerRadius))
            .background(color.copy(alpha = state.actionLabelContainerAlpha))
            .padding(horizontal = state.actionLabelHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(state.actionLabelSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            playerRouteActionIcon(state.actionIconKind),
            contentDescription = state.actionIconContentDescription,
            tint = color.copy(alpha = state.actionLabelIconAlpha),
            modifier = Modifier.size(state.actionLabelIconSize),
        )
        Text(
            state.actionLabel,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = state.actionLabelTextAlpha),
            maxLines = 1,
        )
    }
}

private fun playerRouteActionIcon(kind: RouteActionIconKind): ImageVector {
    return when (kind) {
        RouteActionIconKind.Play -> Icons.Filled.PlayArrow
        RouteActionIconKind.Current -> Icons.Filled.Check
        RouteActionIconKind.Download -> Icons.Filled.CloudDownload
        RouteActionIconKind.Web -> Icons.Filled.VideoLibrary
        RouteActionIconKind.Retry -> Icons.Filled.Refresh
    }
}

@Composable
private fun RoutePanelSummaryCard(
    state: RoutePanelUiState,
    notice: String?,
    detailedMode: Boolean,
    onToggleDetailed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedRouteSummaryColor = playerChromeBaseColor(state.selectedRouteSummaryBaseColor)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(state.cornerRadius),
        color = Color.White.copy(alpha = state.containerAlpha),
        border = BorderStroke(state.borderWidth, Color.White.copy(alpha = state.borderAlpha)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(state.contentPadding),
            verticalArrangement = Arrangement.spacedBy(state.contentSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(state.headerSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.VideoLibrary,
                    contentDescription = null,
                    tint = sourceLibraryToneColor(state.iconTone),
                    modifier = Modifier.size(state.iconSize),
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(state.textSpacing)) {
                    Text(
                        if (detailedMode) state.detailedTitle else state.compactTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = state.titleAlpha),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        if (detailedMode) state.detailedSummary else state.compactSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = sourceLibraryToneColor(state.summaryTone).copy(alpha = state.summaryAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (detailedMode) state.selectedRouteSummary?.let { summary ->
                        Text(
                            summary,
                            style = MaterialTheme.typography.labelSmall,
                            color = selectedRouteSummaryColor.copy(alpha = state.selectedRouteSummaryAlpha),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                TextButton(
                    onClick = onToggleDetailed,
                    modifier = Modifier.width(state.toggleWidth).height(state.toggleHeight).focusable(enabled = state.toggleFocusEnabled),
                    shape = RoundedCornerShape(state.toggleCornerRadius),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (detailedMode) {
                            sourceLibraryToneColor(state.toggleTone).copy(alpha = state.toggleActiveContainerAlpha)
                        } else {
                            Color.White.copy(alpha = state.toggleInactiveContainerAlpha)
                        },
                        contentColor = if (detailedMode) {
                            sourceLibraryToneColor(state.toggleTone)
                        } else {
                            Color.White.copy(alpha = state.toggleInactiveContentAlpha)
                        },
                    ),
                    contentPadding = PaddingValues(state.toggleContentPadding),
                ) {
                    Text(
                        if (detailedMode) state.collapseToggleLabel else state.expandToggleLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }
            val metrics = if (detailedMode) state.detailedMetrics else state.compactMetrics
            if (metrics.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(state.metricSpacing)) {
                    metrics.forEach { metric ->
                        RoutePanelMetricChip(metric)
                    }
                }
            }
            notice?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = sourceLibraryToneColor(state.noticeTone).copy(alpha = state.noticeAlpha),
                    maxLines = state.noticeMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RoutePanelMetricChip(
    metric: RoutePanelMetricUiState,
) {
    val color = sourceLibraryToneColor(metric.tone)
    val containerColor = playerChromeBaseColor(metric.containerBaseColor)
    Row(
        modifier = Modifier
            .height(metric.height)
            .clip(RoundedCornerShape(metric.cornerRadius))
            .background(containerColor.copy(alpha = metric.containerAlpha))
            .padding(horizontal = metric.horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(metric.spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            metric.label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = metric.labelAlpha),
            maxLines = 1,
        )
        Text(
            metric.value,
            style = MaterialTheme.typography.labelMedium,
            color = color.copy(alpha = metric.valueAlpha),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun PlayerEpisodePanel(
    detail: MediaDetail,
    currentEpisode: Episode,
    episodeLoadingId: String?,
    onEpisodeSelected: (Episode) -> Unit,
) {
    val state = remember(detail, currentEpisode.id, episodeLoadingId) {
        buildPlayerEpisodePanelUiState(
            detail = detail,
            currentEpisode = currentEpisode,
            episodeLoadingId = episodeLoadingId,
        )
    }
    if (!state.hasItems) {
        Text(state.emptyText, style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(state.listSpacing)) {
        item {
            PlayerEpisodeSummaryCard(
                state = state,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Text(
                state.listTitle,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = state.listTitleAlpha),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
        items(state.items, key = { it.episode.id }) { item ->
            PlayerEpisodeOptionRow(
                state = item,
                onClick = { onEpisodeSelected(item.episode) },
            )
        }
    }
}

@Composable
private fun PlayerEpisodeSummaryCard(
    state: PlayerEpisodePanelUiState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(state.summaryCardCornerRadius),
        color = Color.White.copy(alpha = state.summaryCardContainerAlpha),
        border = BorderStroke(1.dp, Color.White.copy(alpha = state.summaryCardBorderAlpha)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(state.summaryCardPadding),
            horizontalArrangement = Arrangement.spacedBy(state.summaryCardSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val accent = sourceLibraryToneColor(state.summaryIconTone)
            Box(
                modifier = Modifier
                    .size(state.summaryIconBoxSize)
                    .clip(RoundedCornerShape(state.summaryIconCornerRadius))
                    .background(accent.copy(alpha = state.summaryIconContainerAlpha)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.PlaylistPlay,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(state.summaryIconSize),
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(state.summaryTextSpacing)) {
                Text(
                    state.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    state.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = sourceLibraryToneColor(state.summaryTone),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(state.summaryChipSpacing), verticalAlignment = Alignment.CenterVertically) {
                    state.chips.forEach { chip ->
                        RouteStatusBadge(chip.label, sourceLibraryToneColor(chip.tone))
                    }
                    Text(
                        state.helperText,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = state.helperTextAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerEpisodeOptionRow(
    state: PlayerEpisodeOptionUiState,
    onClick: () -> Unit,
) {
    val accent = sourceLibraryToneColor(state.tone)
    Card(
        onClick = onClick,
        enabled = state.enabled,
        modifier = Modifier.fillMaxWidth().focusable(enabled = state.rowFocusEnabled),
        shape = RoundedCornerShape(state.rowCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = state.containerAlpha),
            disabledContainerColor = Color.White.copy(alpha = state.disabledContainerAlpha),
        ),
        border = BorderStroke(1.dp, if (state.highlighted) accent.copy(alpha = state.borderAlpha) else Color.White.copy(alpha = state.borderAlpha)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(state.rowPadding),
            horizontalArrangement = Arrangement.spacedBy(state.rowSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(state.railWidth)
                    .height(state.railHeight)
                    .clip(RoundedCornerShape(state.rowCornerRadius))
                    .background(accent.copy(alpha = state.railAlpha)),
            )
            Box(
                modifier = Modifier
                    .size(state.indexBoxSize)
                    .clip(RoundedCornerShape(state.indexBoxCornerRadius))
                    .background(accent.copy(alpha = state.indexBoxContainerAlpha)),
                contentAlignment = Alignment.Center,
            ) {
                if (state.loading) {
                    CircularProgressIndicator(color = accent, modifier = Modifier.size(state.loadingIndicatorSize))
                } else {
                    Text(
                        state.compactIndexLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = accent,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(state.textColumnSpacing)) {
                Row(horizontalArrangement = Arrangement.spacedBy(state.titleBadgeSpacing), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        state.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = state.titleAlpha),
                        fontWeight = if (state.prominent) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    state.badges.forEach { badge ->
                        RouteStatusBadge(badge.label, sourceLibraryToneColor(badge.tone))
                    }
                }
                Text(
                    state.indexLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = sourceLibraryToneColor(state.subtitleTone).copy(alpha = state.subtitleAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            EpisodeActionLabel(state = state)
        }
    }
}

@Composable
private fun EpisodeActionLabel(
    state: PlayerEpisodeOptionUiState,
) {
    val color = sourceLibraryToneColor(state.tone)
    Row(
        modifier = Modifier
            .height(state.actionLabelHeight)
            .clip(RoundedCornerShape(state.actionLabelCornerRadius))
            .background(
                color.copy(
                    alpha = if (state.actionEnabled) {
                        state.actionLabelContainerAlpha
                    } else {
                        state.actionLabelDisabledContainerAlpha
                    },
                ),
            )
            .padding(horizontal = state.actionLabelHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(state.actionLabelSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = color, modifier = Modifier.size(state.actionLabelIconSize))
        Text(state.actionLabel, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
    }
}

@Composable
private fun PlayerSliderSetting(
    state: PlayerDanmakuSliderUiState,
    onValueChange: (Float) -> Unit,
) {
    val titleColor = state.titleTone
        ?.let { sourceLibraryToneColor(it) }
        ?: Color.White
    val valueColor = sourceLibraryToneColor(state.valueTone)
    val thumbColor = sourceLibraryToneColor(state.thumbTone)
    val activeTrackColor = sourceLibraryToneColor(state.activeTrackTone)
    val inactiveTrackColor = state.inactiveTrackTone
        ?.let { sourceLibraryToneColor(it) }
        ?: Color.White
    Column(verticalArrangement = Arrangement.spacedBy(state.verticalSpacing)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                state.title,
                style = MaterialTheme.typography.bodyMedium,
                color = titleColor.copy(alpha = state.titleAlpha),
                modifier = Modifier.weight(1f),
            )
            Text(
                state.valueText,
                style = MaterialTheme.typography.labelMedium,
                color = valueColor.copy(alpha = state.valueAlpha),
            )
        }
        Slider(
            value = state.value,
            onValueChange = onValueChange,
            valueRange = state.valueRange,
            steps = state.steps,
            colors = SliderDefaults.colors(
                thumbColor = thumbColor,
                activeTrackColor = activeTrackColor,
                inactiveTrackColor = inactiveTrackColor.copy(alpha = state.inactiveTrackAlpha),
            ),
            modifier = Modifier.fillMaxWidth().height(state.sliderHeight),
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
    badges: List<SourceLibraryChipUiState> = emptyList(),
    actionEnabled: Boolean,
    iconAlpha: Float,
    titleAlpha: Float,
    subtitleAlpha: Float,
    trailingTone: SourceLibraryTone,
    rowState: PlayerSelectableRowUiState,
    onClick: () -> Unit,
) {
    val trailingColor = sourceLibraryToneColor(trailingTone)
    val containerColor = rowState.containerTone
        ?.let { sourceLibraryToneColor(it) }
        ?: Color.White
    val borderColor = rowState.borderTone
        ?.let { sourceLibraryToneColor(it) }
        ?: Color.White
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = rowState.minHeight)
            .clip(RoundedCornerShape(rowState.cornerRadius))
            .background(containerColor.copy(alpha = rowState.containerAlpha))
            .border(
                1.dp,
                borderColor.copy(alpha = rowState.borderAlpha),
                RoundedCornerShape(rowState.cornerRadius),
            )
            .clickable(enabled = enabled) { onClick() }
            .focusable(enabled = rowState.rowFocusEnabled)
            .padding(horizontal = rowState.horizontalPadding, vertical = rowState.verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(rowState.contentSpacing),
    ) {
        if (icon != null) {
            val iconColor = rowState.iconTone
                ?.let { sourceLibraryToneColor(it) }
                ?: Color.White.copy(alpha = iconAlpha)
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(rowState.iconSize),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(rowState.textSpacing)) {
            Row(horizontalArrangement = Arrangement.spacedBy(rowState.titleBadgeSpacing), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = titleAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                badges.forEach { badge ->
                    RouteStatusBadge(badge.label, sourceLibraryToneColor(badge.tone))
                }
            }
            subtitle?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = sourceLibraryToneColor(rowState.subtitleTone).copy(alpha = subtitleAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        trailing?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                color = trailingColor.copy(alpha = if (actionEnabled) rowState.trailingEnabledAlpha else rowState.trailingDisabledAlpha),
                maxLines = 1,
            )
        }
        if (selected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = sourceLibraryToneColor(rowState.selectedIconTone),
                modifier = Modifier.size(rowState.selectedIconSize),
            )
        }
    }
}

@Composable
private fun PlayerTextAction(
    icon: ImageVector?,
    title: String,
    value: String? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    focusEnabled: Boolean = enabled,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val chrome = remember(selected, enabled, focusEnabled) {
        buildPlayerTextActionChromeUiState(selected = selected, enabled = enabled, focusEnabled = focusEnabled)
    }
    val containerBaseColor = chrome.containerTone?.let(::sourceLibraryToneColor) ?: Color.Black
    val disabledContainerBaseColor = chrome.disabledContainerTone?.let(::sourceLibraryToneColor) ?: Color.Black
    val contentBaseColor = chrome.contentTone?.let(::sourceLibraryToneColor) ?: Color.White
    val disabledContentBaseColor = chrome.disabledButtonContentTone?.let(::sourceLibraryToneColor) ?: Color.White
    val contentColor = contentBaseColor.copy(alpha = chrome.contentAlpha)
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(chrome.height)
            .focusable(enabled = chrome.focusEnabled),
        shape = RoundedCornerShape(chrome.cornerRadius),
        colors = ButtonDefaults.textButtonColors(
            containerColor = containerBaseColor.copy(alpha = chrome.containerAlpha),
            contentColor = contentColor,
            disabledContainerColor = disabledContainerBaseColor.copy(alpha = chrome.disabledContainerAlpha),
            disabledContentColor = disabledContentBaseColor.copy(alpha = chrome.disabledButtonContentAlpha),
        ),
        contentPadding = PaddingValues(horizontal = chrome.horizontalPadding, vertical = chrome.verticalPadding),
    ) {
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(chrome.contentSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(chrome.iconSize))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            value?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentBaseColor.copy(alpha = chrome.valueAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
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
    val state = remember(positionMs, durationMs) {
        buildPlayerEdgeProgressUiState(
            positionMs = positionMs,
            durationMs = durationMs,
        )
    }
    val trackColor = state.trackTone
        ?.let(::sourceLibraryToneColor)
        ?: Color.Transparent
    LinearProgressIndicator(
        progress = { state.progressFraction },
        modifier = modifier.height(state.height),
        color = sourceLibraryToneColor(state.progressTone),
        trackColor = trackColor.copy(alpha = state.trackAlpha),
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
    val chrome = remember(selected, prominent, enabled) {
        buildPlayerCircleButtonChromeUiState(
            selected = selected,
            prominent = prominent,
            enabled = enabled,
        )
    }
    val containerBaseColor = chrome.containerTone?.let(::sourceLibraryToneColor)
        ?: playerChromeBaseColor(chrome.containerBaseColor)
    val iconBaseColor = chrome.iconTone?.let(::sourceLibraryToneColor)
        ?: playerChromeBaseColor(chrome.iconBaseColor)
    val backgroundColor = containerBaseColor.copy(alpha = chrome.containerAlpha)
    val iconColor = iconBaseColor.copy(alpha = chrome.iconAlpha)

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(chrome.size)
            .clip(CircleShape)
            .background(backgroundColor)
            .focusable(enabled = chrome.focusEnabled),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(chrome.iconSize),
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
    val state = remember(playbackState, videoSize, protocol) {
        buildPlayerStartupOverlayUiState(
            playbackState = playbackState,
            videoSize = videoSize,
            protocol = protocol,
        )
    }
    Surface(
        modifier = modifier.width(state.width),
        shape = RoundedCornerShape(state.cornerRadius),
        color = AnimePanel.copy(alpha = state.containerAlpha),
        border = BorderStroke(state.borderWidth, Color.White.copy(alpha = state.borderAlpha)),
    ) {
        Column(
            modifier = Modifier.padding(state.contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(state.contentSpacing),
        ) {
            CircularProgressIndicator(
                color = sourceLibraryToneColor(state.progressTone),
                strokeWidth = state.progressStrokeWidth,
                modifier = Modifier.size(state.progressSize),
            )
            Text(
                state.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = state.titleAlpha),
                fontWeight = FontWeight.Bold,
                maxLines = state.titleMaxLines,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                state.metadataLine,
                style = MaterialTheme.typography.bodySmall,
                color = AnimeMuted.copy(alpha = state.metadataAlpha),
                maxLines = state.metadataMaxLines,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TorrentPlaceholderSurface(
    state: TorrentEngineState,
    modifier: Modifier = Modifier,
) {
    val preparationState = remember(state) {
        buildTorrentPlaybackPreparationUiState(state)
    }
    Box(
        modifier = modifier.background(AnimeBackground),
        contentAlignment = Alignment.Center,
    ) {
        val progressTrackColor = preparationState.progressTrackTone
            ?.let(::sourceLibraryToneColor)
            ?: Color.White
        Column(
            modifier = Modifier.width(preparationState.contentWidth).padding(preparationState.contentPadding),
            verticalArrangement = Arrangement.spacedBy(preparationState.contentSpacing),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                preparationState.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White.copy(alpha = preparationState.titleAlpha),
                fontWeight = FontWeight.Bold,
            )
            Text(
                preparationState.description,
                style = MaterialTheme.typography.bodyLarge,
                color = AnimeMuted.copy(alpha = preparationState.descriptionAlpha),
            )
            LinearProgressIndicator(
                progress = { preparationState.bufferingProgress },
                modifier = Modifier.fillMaxWidth(),
                color = sourceLibraryToneColor(preparationState.progressTone),
                trackColor = progressTrackColor.copy(alpha = preparationState.progressTrackAlpha),
            )
            Text(preparationState.statusLine, color = Color.White.copy(alpha = preparationState.primaryLineAlpha))
            Text(preparationState.readinessLine, color = Color.White.copy(alpha = preparationState.primaryLineAlpha))
            Text(preparationState.progressLine, color = Color.White.copy(alpha = preparationState.primaryLineAlpha))
            preparationState.bufferingLine?.let { line ->
                Text(line, color = Color.White.copy(alpha = preparationState.primaryLineAlpha))
            }
            Text(preparationState.connectionLine, color = Color.White.copy(alpha = preparationState.primaryLineAlpha))
            preparationState.fileLine?.let { line ->
                Text(
                    line,
                    style = MaterialTheme.typography.bodySmall,
                    color = AnimeMuted.copy(alpha = preparationState.secondaryLineAlpha),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            preparationState.sizeLine?.let { line ->
                Text(line, style = MaterialTheme.typography.bodySmall, color = AnimeMuted.copy(alpha = preparationState.secondaryLineAlpha))
            }
            preparationState.errorMessage?.let { Text(it, color = sourceLibraryToneColor(preparationState.errorTone)) }
        }
    }
}

private fun normalizePlaybackDurationMs(durationMs: Long): Long {
    val maxReasonableDurationMs = 24L * 60L * 60L * 1000L
    return durationMs.takeIf { it in 1L..maxReasonableDurationMs } ?: 0L
}

private fun formatPlaybackStateLabel(label: String): String {
    return when (label.uppercase()) {
        "IDLE" -> "等待播放"
        "BUFFERING" -> "缓冲中"
        "READY" -> "播放就绪"
        "ENDED" -> "已播完"
        else -> label.ifBlank { "播放中" }
    }
}

private fun nextDanmakuDensity(density: Float): Float {
    return when {
        density < 0.45f -> 0.62f
        density < 0.82f -> 1f
        else -> 0.3f
    }
}

private fun formatDanmakuDensity(density: Float): String {
    return formatDanmakuDensityForUi(density)
}

private fun formatPlaybackSpeed(speed: Float): String {
    return formatPlaybackSpeedForUi(speed)
}

private fun formatPercentLabel(value: Float): String {
    return formatPercentForUi(value)
}

private fun formatScaleLabel(value: Float): String {
    return formatScaleForUi(value)
}

private const val HOME_SCHEDULE_LIMIT = 12
private const val HOME_RECOMMENDATION_LIMIT = 10
