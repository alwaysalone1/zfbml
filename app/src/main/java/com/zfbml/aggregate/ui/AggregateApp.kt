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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
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
import com.zfbml.aggregate.source.SourceCapability
import com.zfbml.aggregate.source.SourceManifest
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
    LaunchedEffect(Unit) {
        delay(1_100)
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
            progress = railProgress,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 78.dp)
                .alpha(contentAlpha),
        )
        SplashPosterRibbon(
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
                    progress = railProgress,
                    modifier = Modifier
                        .size(190.dp)
                        .scale(glowScale),
                )
                Box(
                    modifier = Modifier
                        .size(152.dp)
                        .scale(glowScale)
                        .clip(CircleShape)
                        .background(AnimeAccentCyan.copy(alpha = 0.1f)),
                )
                BrandMark(
                    modifier = Modifier
                        .size(112.dp)
                        .scale(logoScale),
                )
            }
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
                    text = "\u4ECA\u665A\u7EE7\u7EED\u8FFD",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AnimeMuted,
                )
            }
            SplashProgressRail(progress = railProgress, modifier = Modifier.width(164.dp))
        }
    }
}

@Composable
private fun SplashDanmakuOrbit(progress: Float, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        SplashDanmakuStreak(
            width = 78.dp,
            color = AnimeAccentCyan,
            alpha = 0.44f,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (22 + progress * 14).dp, y = 25.dp),
        )
        SplashDanmakuStreak(
            width = 58.dp,
            color = AnimeAccentPink,
            alpha = 0.4f,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = ((1f - progress) * -16).dp, y = 56.dp),
        )
        SplashDanmakuStreak(
            width = 92.dp,
            color = AnimeAccentAmber,
            alpha = 0.34f,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (10 + progress * 20).dp, y = (-48).dp),
        )
        SplashDanmakuStreak(
            width = 68.dp,
            color = AnimeAccentViolet,
            alpha = 0.32f,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = ((1f - progress) * -18).dp, y = (-22).dp),
        )
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
private fun SplashPosterRibbon(modifier: Modifier = Modifier) {
    val tiles = listOf(
        AnimeAccentPink,
        AnimeAccentCyan,
        AnimeAccentAmber,
        AnimeAccentViolet,
        AnimeAccentGreen,
    )
    Row(
        modifier = modifier.height(58.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tiles.forEachIndexed { index, color ->
            Box(
                modifier = Modifier
                    .width(if (index == 2) 42.dp else 34.dp)
                    .height(if (index == 2) 58.dp else 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = if (index == 2) 0.56f else 0.28f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.BottomStart,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (index == 2) 0.72f else 0.58f)
                        .height(3.dp)
                        .offset(x = 5.dp, y = (-8).dp)
                        .background(Color.White.copy(alpha = 0.62f), RoundedCornerShape(999.dp)),
                )
                Box(
                    modifier = Modifier
                        .size(if (index == 2) 14.dp else 10.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.24f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(if (index == 2) 6.dp else 4.dp)
                            .height(if (index == 2) 8.dp else 6.dp)
                            .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(2.dp)),
                    )
                }
            }
        }
    }
}

@Composable
private fun SplashSignalRails(progress: Float, modifier: Modifier = Modifier) {
    val rails = listOf(
        Triple(132.dp, AnimeAccentCyan, 0.18f),
        Triple(92.dp, AnimeAccentPink, 0.28f),
        Triple(118.dp, AnimeAccentAmber, 0.12f),
    )
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        rails.forEachIndexed { index, (width, color, startAlpha) ->
            Box(
                modifier = Modifier
                    .width(width)
                    .height(3.dp)
                    .offset(x = ((progress - 0.5f) * (index + 1) * 18f).dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                color.copy(alpha = startAlpha + progress.coerceIn(0f, 1f) * 0.28f),
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
private fun SplashProgressRail(progress: Float, modifier: Modifier = Modifier) {
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
            text = "\u7247\u5355\u5DF2\u5C31\u7EEA",
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

private enum class PlayerPanel {
    More,
    Danmaku,
    Quality,
    Speed,
    Route,
    Episode,
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AnimePanel,
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppTab.entries.forEach { tab ->
                AppBottomNavItem(
                    tab = tab,
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AppNavigationRail(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
) {
    Surface(
        modifier = Modifier.width(92.dp).fillMaxHeight(),
        color = AnimePanel,
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BrandMark(Modifier.size(48.dp))
            Spacer(Modifier.height(6.dp))
            AppTab.entries.forEach { tab ->
                AppRailNavItem(
                    tab = tab,
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                "ZFBML",
                style = MaterialTheme.typography.labelSmall,
                color = AnimeAccentCyan,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun AppBottomNavItem(
    tab: AppTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = if (selected) AnimeAccentPink else Color.White.copy(alpha = 0.62f)
    TextButton(
        onClick = onClick,
        modifier = modifier.height(56.dp).focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) AnimeAccentPink.copy(alpha = 0.16f) else Color.Transparent,
            contentColor = accent,
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(tab.icon, contentDescription = tab.label, modifier = Modifier.size(if (selected) 23.dp else 21.dp))
            Text(
                tab.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun AppRailNavItem(
    tab: AppTab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accent = if (selected) AnimeAccentPink else Color.White.copy(alpha = 0.62f)
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(62.dp).focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) AnimeAccentPink.copy(alpha = 0.16f) else Color.Transparent,
            contentColor = accent,
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(tab.icon, contentDescription = tab.label, modifier = Modifier.size(if (selected) 24.dp else 22.dp))
            Text(
                tab.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
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
    onToggleCalendar: () -> Unit,
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
        item {
            HomeWatchHub(
                continueItem = continueItem,
                todayCount = selectedItems.size,
                recommendationCount = remainder.size.coerceAtLeast(feedSelection.spotlight.size),
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
private fun HomeWatchHub(
    continueItem: SearchResult?,
    todayCount: Int,
    recommendationCount: Int,
    onContinue: () -> Unit,
    onCalendar: () -> Unit,
    onRecommendation: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HomeWatchHubCard(
            title = "\u7ee7\u7eed\u770b",
            subtitle = continueItem?.title ?: "\u6682\u65e0\u8fdb\u5ea6",
            icon = Icons.Filled.PlayArrow,
            accent = AnimeAccentPink,
            enabled = continueItem != null,
            onClick = onContinue,
            modifier = Modifier.weight(1.18f),
        )
        HomeWatchHubCard(
            title = "\u4eca\u65e5\u66f4\u65b0",
            subtitle = if (todayCount > 0) "${todayCount}\u90e8\u653e\u9001" else "\u67e5\u770b\u65e5\u5386",
            icon = Icons.Filled.Bookmarks,
            accent = AnimeAccentCyan,
            onClick = onCalendar,
            modifier = Modifier.weight(1f),
        )
        HomeWatchHubCard(
            title = "\u70ed\u95e8\u63a8\u8350",
            subtitle = if (recommendationCount > 0) "${recommendationCount}\u90e8\u53ef\u9009" else "\u5148\u53bb\u641c\u7d22",
            icon = Icons.Filled.VideoLibrary,
            accent = AnimeAccentGreen,
            enabled = recommendationCount > 0,
            onClick = onRecommendation,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HomeWatchHubCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(78.dp).focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) AnimePanel else Color.White.copy(alpha = 0.035f),
            disabledContainerColor = Color.White.copy(alpha = 0.035f),
        ),
        border = BorderStroke(1.dp, if (enabled) accent.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.06f)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (enabled) Color.White else Color.White.copy(alpha = 0.42f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (enabled) accent else Color.White.copy(alpha = 0.28f),
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) AnimeMuted else Color.White.copy(alpha = 0.32f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
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
private fun SectionHeader(
    title: String,
    action: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
                setRequestProperty("User-Agent", "ZFBML/0.5.34")
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

    fun runSearch(searchTerm: String = query) {
        val normalizedQuery = searchTerm.trim()
        if (normalizedQuery.isBlank()) return
        query = normalizedQuery
        scope.launch {
            searched = true
            loading = true
            searchMessage = null
            runCatching { graph.sourceRegistry.searchAllWithReport(normalizedQuery) }
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
                Text("\u627e\u756a", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text("\u8f93\u5165\u756a\u540d\u6216\u7c98\u8d34\u64ad\u653e\u94fe\u63a5\uff0c\u5148\u8fdb\u8be6\u60c5\u9875\uff0c\u518d\u7531\u5e94\u7528\u81ea\u52a8\u5339\u914d\u6700\u5408\u9002\u7684\u64ad\u653e\u6e90\u3002", style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
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
        if (!searched && results.isEmpty()) {
            item {
                SearchSuggestionStrip(
                    suggestions = listOf("\u5b64\u72ec\u6447\u6eda", "\u51e1\u4eba\u4fee\u4ed9\u4f20", "\u9b3c\u706d\u4e4b\u5203", "\u9b54\u6cd5\u5c11\u5973\u5c0f\u5706", "\u590f\u76ee\u53cb\u4eba\u5e10"),
                    onSelected = { runSearch(it) },
                )
            }
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
    val onlineCount = providers.count { SourceCapability.STREAM in it.capabilities && SourceCapability.BITTORRENT !in it.capabilities }
    val btCount = providers.count { SourceCapability.BITTORRENT in it.capabilities }
    val downloadableCount = providers.count { it.supportsDownload || SourceCapability.DOWNLOAD in it.capabilities }
    val webViewCount = providers.count { it.requiresWebView || SourceCapability.WEBVIEW_SNIFF in it.capabilities }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SourceLibraryHero(
                providerCount = providers.size,
                onlineCount = onlineCount,
                btCount = btCount,
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    SourceStrategyCard("先播在线", "${onlineCount.coerceAtLeast(0)} 源", "HLS/MP4 优先开播", AnimeAccentCyan)
                }
                item {
                    SourceStrategyCard("备用补源", "${btCount.coerceAtLeast(0)} 源", "资源站作为补充", AnimeAccentAmber)
                }
                item {
                    SourceStrategyCard("离线缓存", "${downloadableCount.coerceAtLeast(0)} 源", "可播线路可缓存", AnimeAccentGreen)
                }
                item {
                    SourceStrategyCard("网页兜底", "${webViewCount.coerceAtLeast(0)} 源", "复杂页面再嗅探", AnimeAccentViolet)
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("已接入线路", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Text("默认由详情页自动选择最佳线路，手动切换只在卡顿、失效或想换清晰度时进入。", style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
            }
        }
        items(providers) { manifest ->
            SourceCard(
                manifest = manifest,
                accent = providerAccent(manifest.id),
            )
        }
    }
}

@Composable
private fun SourceLibraryHero(
    providerCount: Int,
    onlineCount: Int,
    btCount: Int,
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
                Text("片库频道", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Text("像视频 App 一样点开就看：在线源先播，资源站和嗅探只做备用。", style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    item {
                        RouteStatusBadge("${providerCount} 个来源", AnimeAccentPink)
                    }
                    item {
                        RouteStatusBadge("${onlineCount} 在线", AnimeAccentCyan)
                    }
                    if (btCount > 0) {
                        item {
                            RouteStatusBadge("${btCount} 备用", AnimeAccentAmber)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceStrategyCard(
    title: String,
    value: String,
    subtitle: String,
    accent: Color,
) {
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
            Text(title, style = MaterialTheme.typography.labelMedium, color = AnimeMuted, maxLines = 1)
            Text(value, style = MaterialTheme.typography.titleLarge, color = accent, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.72f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SourceCard(
    manifest: SourceManifest,
    accent: Color,
) {
    val isBt = SourceCapability.BITTORRENT in manifest.capabilities
    val isStream = SourceCapability.STREAM in manifest.capabilities
    val statusLabel = when {
        isStream && !isBt -> "在线源"
        isBt -> "资源站"
        SourceCapability.SEARCH in manifest.capabilities -> "索引源"
        else -> "辅助源"
    }
    val statusColor = when {
        isStream && !isBt -> AnimeAccentCyan
        isBt -> AnimeAccentAmber
        else -> AnimeAccentViolet
    }
    val featureText = buildList {
        if (SourceCapability.SEARCH in manifest.capabilities) add("搜索")
        if (SourceCapability.DETAIL in manifest.capabilities) add("详情")
        if (SourceCapability.EPISODES in manifest.capabilities) add("选集")
        if (isStream) add("播放")
        if (manifest.supportsDownload || SourceCapability.DOWNLOAD in manifest.capabilities) add("缓存")
        if (manifest.requiresWebView || SourceCapability.WEBVIEW_SNIFF in manifest.capabilities) add("嗅探")
    }.joinToString(" · ").ifBlank { "基础来源" }
    val domainText = manifest.domains.joinToString(" · ").ifBlank { "本地内置" }
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
                Icon(if (isBt) Icons.Filled.Subscriptions else Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(manifest.name, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    RouteStatusBadge(statusLabel, statusColor)
                }
                Text(featureText, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.78f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(domainText, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(manifest.version, style = MaterialTheme.typography.labelMedium, color = accent, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(manifest.author, style = MaterialTheme.typography.labelSmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
    val sourceCount = graph.sourceRegistry.manifests.size
    val danmakuCount = graph.danmakuRegistry.profiles.size
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ProfileHeroCard(
                version = "0.5.34",
                sourceCount = sourceCount,
                danmakuCount = danmakuCount,
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    ProfileQuickCard("追番记录", "继续看入口", Icons.Filled.PlayArrow, AnimeAccentPink)
                }
                item {
                    ProfileQuickCard("离线缓存", "可播线路缓存", Icons.Filled.CloudDownload, AnimeAccentCyan)
                }
                item {
                    ProfileQuickCard("弹幕设置", "${danmakuCount} 平台样式", Icons.Filled.ClosedCaption, AnimeAccentViolet)
                }
                item {
                    ProfileQuickCard("线路管理", "${sourceCount} 个来源", Icons.Filled.VideoLibrary, AnimeAccentAmber)
                }
            }
        }
        item {
            Text("播放体验", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        }
        item {
            ProfileSettingRow(
                title = "播放内核",
                subtitle = "在线播放默认走 Media3，疑难格式后续再接入兜底内核",
                value = "Media3",
                icon = Icons.Filled.PlayArrow,
                accent = AnimeAccentCyan,
            )
        }
        item {
            ProfileSettingRow(
                title = "弹幕样式",
                subtitle = "B站 / 腾讯 / 爱奇艺 / 优酷样式持续补齐",
                value = "${danmakuCount} 平台",
                icon = Icons.Filled.ClosedCaption,
                accent = AnimeAccentViolet,
            )
        }
        item {
            ProfileSettingRow(
                title = "播放源策略",
                subtitle = "自动最佳优先，手动换源保留给卡顿和失效场景",
                value = "${sourceCount} 来源",
                icon = Icons.Filled.VideoLibrary,
                accent = AnimeAccentPink,
            )
        }
    }
}

@Composable
private fun ProfileHeroCard(
    version: String,
    sourceCount: Int,
    danmakuCount: Int,
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
                Text("我的追番中心", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text("继续看、缓存、弹幕和线路都收在这里，普通用户不用面对调试入口。", style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    item { RouteStatusBadge("v$version", AnimeAccentPink) }
                    item { RouteStatusBadge("${sourceCount} 来源", AnimeAccentCyan) }
                    item { RouteStatusBadge("${danmakuCount} 弹幕平台", AnimeAccentViolet) }
                }
            }
        }
    }
}

@Composable
private fun ProfileQuickCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
) {
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
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ProfileSettingRow(
    title: String,
    subtitle: String,
    value: String,
    icon: ImageVector,
    accent: Color,
) {
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
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(value, style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.Bold, maxLines = 1)
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
                    Text("\u5168\u7ad9\u627e\u756a", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("\u652f\u6301\u756a\u540d\u3001\u5267\u540d\u548c\u76f4\u94fe", style = MaterialTheme.typography.bodySmall, color = AnimeMuted)
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("\u8f93\u5165\u756a\u540d\u6216\u7c98\u8d34\u64ad\u653e\u94fe\u63a5") },
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
    suggestions: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("\u5927\u5bb6\u5728\u627e", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(suggestions) { suggestion ->
                TextButton(
                    onClick = { onSelected(suggestion) },
                    modifier = Modifier.height(36.dp).focusable(),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        contentColor = Color.White,
                    ),
                    contentPadding = PaddingValues(horizontal = 13.dp, vertical = 0.dp),
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = AnimeAccentCyan, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(suggestion, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                }
            }
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
            Text("\u627e\u5230\u540e\u5148\u8fdb\u756a\u5267\u8be6\u60c5", style = MaterialTheme.typography.titleSmall, color = Color.White)
            Text("\u8be6\u60c5\u9875\u4f1a\u5c55\u793a\u7b80\u4ecb\u3001\u9009\u96c6\u548c\u81ea\u52a8\u63a8\u8350\u7684\u64ad\u653e\u6e90\uff0c\u70b9\u64ad\u653e\u5c31\u80fd\u7ee7\u7eed\u770b\u3002", style = MaterialTheme.typography.bodySmall, color = AnimeMuted)
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
            text = "\u6ca1\u627e\u5230\u5408\u9002\u7ed3\u679c\uff0c\u53ef\u4ee5\u6362\u4e00\u4e2a\u756a\u540d\u3001\u522b\u540d\u6216\u5173\u952e\u8bcd\u518d\u8bd5\u3002",
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
    var routesFromCache by remember(result) { mutableStateOf(false) }
    var routesExpanded by remember(result) { mutableStateOf(false) }

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
    LaunchedEffect(detail?.url, selectedEpisode?.id, routesLoading, routesError, routes) {
        val media = detail ?: return@LaunchedEffect
        val episode = selectedEpisode ?: return@LaunchedEffect
        if (routesLoading || routesError != null || routes.isEmpty()) return@LaunchedEffect
        routePrefetchWindow(media.episodes, episode).forEach { prefetchEpisode ->
            launch {
                graph.sourceRegistry.prefetchRouteCandidates(prefetchEpisode)
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
            item {
                DetailHero(
                    media = media,
                    selectedEpisode = selectedEpisode,
                    routeUiState = routeUiState,
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
                    episodeCount = media.episodes.size,
                    selectedEpisode = selectedEpisode,
                    routeUiState = routeUiState,
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
                    state = routeUiState,
                    expanded = routesExpanded,
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
                        RouteLoadingPanel(selectedEpisode = selectedEpisode, modifier = Modifier.padding(horizontal = 18.dp))
                    }
                }
                routesError?.let { message ->
                    item {
                        Text(
                            "\u7ebf\u8def\u52a0\u8f7d\u5931\u8d25: $message",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 18.dp),
                        )
                    }
                }
                if (!routesLoading && selectedEpisode != null && routes.isEmpty() && routesError == null) {
                    item {
                        EmptyRoutePanel(modifier = Modifier.padding(horizontal = 18.dp))
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
    onPlay: () -> Unit,
    onToggleRoutes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playLabel = selectedEpisode?.index?.let { "播放第 $it 集" } ?: "立即观看"
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 360.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AnimePanel),
    ) {
        PosterArtwork(
            posterUrl = media.posterUrl,
            accent = providerAccent(media.providerId),
            modifier = Modifier.matchParentSize().alpha(0.34f),
            shape = RoundedCornerShape(8.dp),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.12f),
                            AnimeBackground.copy(alpha = 0.78f),
                            AnimeBackground.copy(alpha = 0.96f),
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                PosterArtwork(
                    posterUrl = media.posterUrl,
                    accent = providerAccent(media.providerId),
                    modifier = Modifier.size(width = 116.dp, height = 164.dp),
                    shape = RoundedCornerShape(8.dp),
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(
                        media.title,
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
                state = routeUiState,
                selectedEpisode = selectedEpisode,
            )
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
                    Text(if (routeUiState.canPlay) playLabel else "匹配播放源")
                }
                DetailRouteEntryButton(
                    state = routeUiState,
                    onClick = onToggleRoutes,
                    modifier = Modifier.widthIn(min = 126.dp, max = 156.dp).height(48.dp),
                )
            }
        }
    }
}

@Composable
private fun DetailRouteEntryButton(
    state: RouteUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = when (state.status) {
        RouteLoadStatus.Ready -> AnimeAccentCyan
        RouteLoadStatus.Loading -> AnimeAccentAmber
        RouteLoadStatus.Failed -> MaterialTheme.colorScheme.error
        RouteLoadStatus.Empty -> AnimeAccentAmber
        RouteLoadStatus.Idle -> AnimeMuted
    }
    val title = when (state.status) {
        RouteLoadStatus.Ready -> "自动最佳"
        RouteLoadStatus.Loading -> "匹配中"
        RouteLoadStatus.Failed -> "播放源异常"
        RouteLoadStatus.Empty -> "暂无播放源"
        RouteLoadStatus.Idle -> "手动换源"
    }
    val value = when {
        state.sourceCount > 1 -> "${state.sourceCount} 个来源"
        state.routeCount > 1 -> "${state.routeCount} 线可切"
        state.canPlay -> state.recommendationTitle
        state.status == RouteLoadStatus.Loading -> "优先在线"
        state.status == RouteLoadStatus.Failed -> "查看原因"
        else -> "选择剧集"
    }
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
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    value,
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
    state: RouteUiState,
    selectedEpisode: Episode?,
) {
    val accent = when (state.status) {
        RouteLoadStatus.Ready -> AnimeAccentGreen
        RouteLoadStatus.Loading -> AnimeAccentCyan
        RouteLoadStatus.Failed -> MaterialTheme.colorScheme.error
        RouteLoadStatus.Empty -> AnimeAccentAmber
        RouteLoadStatus.Idle -> AnimeMuted
    }
    val title = when (state.status) {
        RouteLoadStatus.Ready -> "即将播放"
        RouteLoadStatus.Loading -> "匹配播放源"
        RouteLoadStatus.Failed -> "播放源异常"
        RouteLoadStatus.Empty -> "等待可用播放源"
        RouteLoadStatus.Idle -> "等待选集"
    }
    val episodeLabel = selectedEpisode?.index?.let { "第 $it 集" } ?: state.selectedEpisodeTitle
    val decision = when (state.status) {
        RouteLoadStatus.Ready -> "$episodeLabel · 推荐 ${state.recommendationTitle} · ${state.recommendationDetail}"
        RouteLoadStatus.Loading -> "$episodeLabel · 正在优先匹配在线播放"
        else -> state.detail
    }
    val bestRoute = state.bestRoute
    val qualityLabel = bestRoute?.let { playerQualityLabel(it) } ?: when (state.status) {
        RouteLoadStatus.Loading -> "匹配中"
        RouteLoadStatus.Failed -> "待重试"
        RouteLoadStatus.Empty -> "待补源"
        RouteLoadStatus.Idle -> "自动"
        RouteLoadStatus.Ready -> "自动"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.32f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                if (state.status == RouteLoadStatus.Loading) {
                    CircularProgressIndicator(color = accent, modifier = Modifier.size(18.dp))
                } else {
                    Icon(
                        imageVector = if (state.status == RouteLoadStatus.Ready) Icons.Filled.Check else Icons.Filled.VideoLibrary,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    decision,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                if (state.canPlay) "推荐播放" else "自动匹配",
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
            )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
            item { DetailDecisionChip("当前集", episodeLabel, AnimeAccentPink) }
            item { DetailDecisionChip("推荐源", state.recommendationTitle, AnimeAccentCyan) }
            item { DetailDecisionChip("清晰度", qualityLabel, AnimeAccentAmber) }
            if (state.status != RouteLoadStatus.Idle) {
                item { DetailDecisionChip("加载", state.loadOriginLabel, AnimeAccentGreen) }
            }
            if (state.routeCount > 1) {
                item { DetailDecisionChip("可切换", state.sourceCoverageLabel, AnimeAccentViolet) }
            }
        }
    }
}

@Composable
private fun DetailDecisionChip(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, color.copy(alpha = 0.22f), RoundedCornerShape(999.dp))
            .padding(horizontal = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = AnimeMuted, maxLines = 1)
        Text(value.ifBlank { "自动" }, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun DetailRouteStatusCard(
    state: RouteUiState,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onPlayBest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = when (state.status) {
        RouteLoadStatus.Ready -> AnimeAccentGreen
        RouteLoadStatus.Loading -> AnimeAccentCyan
        RouteLoadStatus.Failed -> MaterialTheme.colorScheme.error
        RouteLoadStatus.Empty -> AnimeAccentAmber
        RouteLoadStatus.Idle -> AnimeMuted
    }
    val onlineValue = when {
        state.onlineCount > 0 -> "${state.onlineCount} 条"
        state.status == RouteLoadStatus.Loading -> "匹配中"
        else -> "待补充"
    }
    val btValue = when {
        state.btCount > 0 -> "${state.btCount} 条"
        state.status == RouteLoadStatus.Loading -> "兜底中"
        else -> "备用"
    }
    val compactReady = state.status == RouteLoadStatus.Ready && !expanded
    val showDiagnostics = expanded ||
        state.status == RouteLoadStatus.Loading ||
        state.status == RouteLoadStatus.Failed
    val headerSubtitle = if (compactReady) {
        state.bestRoute?.let { route ->
            listOfNotNull(
                state.selectedEpisodeTitle,
                "自动最佳",
                playerQualityLabel(route),
                if (state.routeCount > 1) state.sourceCoverageLabel else null,
            ).filter { it.isNotBlank() }.distinct().joinToString(" · ")
        } ?: state.selectedEpisodeTitle
    } else {
        state.selectedEpisodeTitle
    }
    val actionText = when {
        expanded -> "收起"
        state.status == RouteLoadStatus.Ready -> "切换"
        else -> "详情"
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = AnimePanel,
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(if (compactReady) 8.dp else 14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(if (compactReady) 38.dp else 44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.status == RouteLoadStatus.Loading) {
                        CircularProgressIndicator(color = accent, modifier = Modifier.size(if (compactReady) 18.dp else 22.dp))
                    } else {
                        Icon(
                            imageVector = if (state.status == RouteLoadStatus.Ready) Icons.Filled.Check else Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(if (compactReady) 21.dp else 24.dp),
                        )
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(state.message, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(headerSubtitle, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                TextButton(onClick = onToggleExpanded, modifier = Modifier.height(38.dp).focusable()) {
                    Text(actionText, color = AnimeAccentCyan, style = MaterialTheme.typography.labelLarge)
                }
            }
            if (state.status == RouteLoadStatus.Loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(8.dp)),
                    color = accent,
                    trackColor = Color.White.copy(alpha = 0.08f),
                )
            }
            if (!compactReady) {
                RouteRecommendationBand(
                    state = state,
                    accent = accent,
                    onPlayBest = onPlayBest,
                )
            }
            if (showDiagnostics) {
                RouteSourceFocusRow(state = state, accent = accent)
                RouteLoadingStepRow(state = state, accent = accent)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RouteMetricChip("可播", state.routeCount.toString(), AnimeAccentCyan, Modifier.weight(1f))
                    RouteMetricChip("来源", state.sourceCount.toString(), AnimeAccentViolet, Modifier.weight(1f))
                    RouteMetricChip("异常", state.failedCount.toString(), if (state.failedCount > 0) MaterialTheme.colorScheme.error else AnimeMuted, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RouteRecommendationBand(
    state: RouteUiState,
    accent: Color,
    onPlayBest: () -> Unit,
) {
    val route = state.bestRoute
    val actionLabel = if (route?.protocol == StreamProtocol.BITTORRENT) "边下边播" else "播放推荐"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.12f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.width(4.dp).height(48.dp).clip(RoundedCornerShape(8.dp)).background(accent),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(if (route == null) "播放源" else "推荐源", style = MaterialTheme.typography.labelMedium, color = accent, maxLines = 1)
            Text(state.recommendationTitle, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(state.recommendationDetail, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.72f), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (state.canPlay) {
            Button(
                onClick = onPlayBest,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AnimeAccentPink, contentColor = Color.White),
                modifier = Modifier.height(40.dp).focusable(),
                contentPadding = PaddingValues(horizontal = 12.dp),
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(actionLabel, maxLines = 1)
            }
        }
    }
}

@Composable
private fun RouteSourceFocusRow(state: RouteUiState, accent: Color) {
    val route = state.bestRoute
    val sourceValue = route?.sourceName ?: when (state.status) {
        RouteLoadStatus.Loading -> "匹配中"
        RouteLoadStatus.Failed -> "失败"
        RouteLoadStatus.Empty -> "暂无"
        RouteLoadStatus.Idle -> "待选择"
        RouteLoadStatus.Ready -> "自动"
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RouteSourceFocusChip("推荐源", sourceValue, accent, Modifier.weight(1f))
        RouteSourceFocusChip("来源覆盖", state.sourceCoverageLabel, AnimeAccentCyan, Modifier.weight(1f))
        RouteSourceFocusChip("加载方式", state.loadOriginLabel, AnimeAccentAmber, Modifier.weight(1f))
    }
}

@Composable
private fun RouteSourceFocusChip(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .heightIn(min = 54.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.11f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun RouteLoadingStepRow(
    state: RouteUiState,
    accent: Color,
) {
    val colors = listOf(accent, AnimeAccentCyan, AnimeAccentAmber)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        state.loadingSteps.take(3).forEachIndexed { index, step ->
            RouteDiagnosticStep(
                title = step.title,
                value = step.value,
                active = step.active,
                accent = colors.getOrElse(index) { accent },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RouteDiagnosticStep(
    title: String,
    value: String,
    active: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .heightIn(min = 58.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) accent.copy(alpha = 0.13f) else Color.White.copy(alpha = 0.05f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = if (active) accent else AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun RouteMetricChip(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun DetailEpisodeSectionHeader(
    episodeCount: Int,
    selectedEpisode: Episode?,
    routeUiState: RouteUiState,
    modifier: Modifier = Modifier,
) {
    val accent = when (routeUiState.status) {
        RouteLoadStatus.Ready -> AnimeAccentGreen
        RouteLoadStatus.Loading -> AnimeAccentCyan
        RouteLoadStatus.Failed -> MaterialTheme.colorScheme.error
        RouteLoadStatus.Empty -> AnimeAccentAmber
        RouteLoadStatus.Idle -> AnimeMuted
    }
    val currentLabel = selectedEpisode?.index?.let { "当前第 $it 集" }
        ?: selectedEpisode?.title?.takeIf { it.isNotBlank() }
        ?: "默认从第 1 集开始"
    val statusLabel = when (routeUiState.status) {
        RouteLoadStatus.Ready -> "已匹配"
        RouteLoadStatus.Loading -> "匹配中"
        RouteLoadStatus.Failed -> "异常"
        RouteLoadStatus.Empty -> "待补源"
        RouteLoadStatus.Idle -> "待选集"
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("选集", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    RouteStatusBadge(statusLabel, accent)
                }
                Text(
                    "$currentLabel · 共 ${episodeCount.coerceAtLeast(1)} 集 · 切换后自动匹配最佳播放源",
                    style = MaterialTheme.typography.bodySmall,
                    color = AnimeMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                "全部 ${episodeCount.coerceAtLeast(1)}",
                style = MaterialTheme.typography.labelLarge,
                color = AnimeAccentCyan,
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AnimeAccentCyan.copy(alpha = 0.1f))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
            )
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
    LazyRow(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(episodes) { episode ->
            val selected = episode.id == selectedEpisodeId
            Card(
                onClick = { onEpisodeSelected(episode) },
                modifier = Modifier.width(118.dp).height(72.dp).focusable(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = if (selected) AnimePanelSoft else AnimePanel),
                border = BorderStroke(1.dp, if (selected) AnimeAccentCyan else AnimeBorder),
            ) {
                Column(
                    Modifier.fillMaxSize().padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = episode.index?.let { "%02d".format(it) } ?: "SP",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selected) AnimeAccentCyan else Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Text(
                        text = episode.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selected) Color.White else AnimeMuted,
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
private fun RouteLoadingPanel(selectedEpisode: Episode?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
private fun EmptyRoutePanel(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
    recommendedSourceId: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val groups = remember(routes, selectedSourceId, recommendedSourceId) {
        buildRouteSourceGroups(
            routes = routes,
            selectedSourceId = selectedSourceId,
            recommendedSourceId = recommendedSourceId,
        )
    }
    val allGroup = remember(routes, selectedSourceId, recommendedSourceId) {
        buildRouteSourceGroups(
            routes = routes,
            selectedSourceId = selectedSourceId,
            recommendedSourceId = recommendedSourceId,
            includeAll = true,
        ).first()
    }
    val recommendedGroup = groups.firstOrNull { it.hasRecommended }
    val selectedGroup = groups.firstOrNull { it.isFilterSelected }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        RouteSourceSelectorHeader(
            recommendedName = recommendedGroup?.name ?: "自动推荐",
            selectedName = selectedGroup?.name ?: "自动最佳",
            routeCount = allGroup.totalCount,
            sourceCount = groups.size,
            playableCount = allGroup.playableCount,
        )
        RouteSourceAutoChoiceCard(
            recommendedName = recommendedGroup?.name ?: "自动推荐",
            selected = selectedSourceId == null,
            routeCount = allGroup.totalCount,
            playableCount = allGroup.playableCount,
            onlineCount = allGroup.onlineCount,
            btCount = allGroup.btCount,
            onClick = { onSelected(null) },
        )
        LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(groups) { group ->
                RouteSourceFilterPill(
                    title = group.name,
                    subtitle = group.sourceSummary,
                    badge = "${group.totalCount}线",
                    selected = group.isFilterSelected,
                    recommended = group.hasRecommended,
                    onClick = { onSelected(group.id) },
                )
            }
        }
    }
}

@Composable
private fun RouteSourceSelectorHeader(
    recommendedName: String,
    selectedName: String,
    routeCount: Int,
    sourceCount: Int,
    playableCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "播放方案",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "$sourceCount 组来源 · $playableCount/$routeCount 可播 · 默认自动最佳",
                style = MaterialTheme.typography.bodySmall,
                color = AnimeMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        RouteSourceStatusPill("推荐", recommendedName, AnimeAccentPink)
        RouteSourceStatusPill("当前", selectedName, AnimeAccentCyan)
    }
}

@Composable
private fun RouteSourceAutoChoiceCard(
    recommendedName: String,
    selected: Boolean,
    routeCount: Int,
    playableCount: Int,
    onlineCount: Int,
    btCount: Int,
    onClick: () -> Unit,
) {
    val accent = if (selected) AnimeAccentCyan else AnimeAccentPink
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) AnimePanelSoft else AnimePanel),
        border = BorderStroke(1.dp, if (selected) AnimeAccentCyan else AnimeAccentPink.copy(alpha = 0.42f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(8.dp)).background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("自动最佳", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                    RouteStatusBadge("推荐入口", AnimeAccentPink)
                    if (selected) RouteStatusBadge("当前", AnimeAccentCyan)
                }
                Text(
                    "优先 $recommendedName · $playableCount/$routeCount 可播",
                    style = MaterialTheme.typography.bodySmall,
                    color = AnimeMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (onlineCount > 0) RouteStatusBadge("${onlineCount} 在线", AnimeAccentGreen)
                    if (btCount > 0) RouteStatusBadge("${btCount} 备用", AnimeAccentAmber)
                }
            }
            Text(
                if (selected) "使用中" else "使用",
                style = MaterialTheme.typography.labelLarge,
                color = accent,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun RouteSourceStatusPill(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.13f))
            .padding(horizontal = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
        Text(value, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.86f), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun RouteSourceFilterPill(
    title: String,
    subtitle: String,
    badge: String,
    selected: Boolean,
    recommended: Boolean,
    onClick: () -> Unit,
) {
    val accent = when {
        recommended -> AnimeAccentPink
        selected -> AnimeAccentCyan
        else -> AnimeBorder
    }
    val contentColor = when {
        selected -> Color.White
        recommended -> Color.White.copy(alpha = 0.94f)
        else -> Color.White.copy(alpha = 0.78f)
    }
    TextButton(
        onClick = onClick,
        modifier = Modifier.width(174.dp).height(76.dp).focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = when {
                selected -> accent.copy(alpha = 0.18f)
                recommended -> AnimeAccentPink.copy(alpha = 0.12f)
                else -> Color.White.copy(alpha = 0.06f)
            },
            contentColor = contentColor,
        ),
        border = BorderStroke(1.dp, if (selected || recommended) accent else AnimeBorder),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    fontWeight = if (selected || recommended) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected || recommended) accent else AnimeMuted,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected || recommended) accent else AnimeMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (recommended) {
                    Text(
                        text = "自动推荐",
                        style = MaterialTheme.typography.labelSmall,
                        color = AnimeAccentPink,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
                if (selected) {
                    Text(
                        text = "当前方案",
                        style = MaterialTheme.typography.labelSmall,
                        color = AnimeAccentCyan,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                } else if (!recommended) {
                    Text(
                        text = "点按切换",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.58f),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteStatusBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .height(24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
    }
}

@Composable
private fun RoutePlayActionLabel(route: RouteCandidate, recommended: Boolean) {
    val (label, color) = when {
        route.protocol == StreamProtocol.BITTORRENT -> "边下边播" to AnimeAccentAmber
        route.protocol == StreamProtocol.WEBVIEW_ONLY -> "网页兜底" to AnimeMuted
        recommended -> "推荐播放" to AnimeAccentPink
        else -> "播放" to AnimeAccentCyan
    }
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.13f))
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
    }
}

private fun RouteCandidate.primaryRouteLabel(): String {
    return listOfNotNull(
        routeName?.takeIf { it.isNotBlank() },
        quality?.takeIf { it.isNotBlank() },
        subgroup?.takeIf { it.isNotBlank() }?.take(12),
    ).distinct().joinToString(" · ").ifBlank { protocol.displayName() }
}

private fun RouteCandidate.routeStatusLabel(): Pair<String, Color> {
    return when (protocol) {
        StreamProtocol.BITTORRENT -> "备用源" to AnimeAccentAmber
        StreamProtocol.WEBVIEW_ONLY -> "仅网页" to AnimeMuted
        StreamProtocol.HLS, StreamProtocol.DASH, StreamProtocol.PROGRESSIVE, StreamProtocol.SMOOTH_STREAMING -> "在线可播" to AnimeAccentGreen
        else -> protocol.displayName() to AnimeAccentCyan
    }
}

@Composable
private fun RouteCandidateRow(
    route: RouteCandidate,
    recommended: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = when {
        recommended -> AnimeAccentPink
        route.protocol == StreamProtocol.BITTORRENT -> AnimeAccentAmber
        route.protocol == StreamProtocol.WEBVIEW_ONLY -> AnimeMuted
        else -> AnimeAccentCyan
    }
    val (statusLabel, statusColor) = route.routeStatusLabel()
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (recommended) AnimePanelSoft else AnimePanel),
        border = BorderStroke(1.dp, if (recommended) AnimeAccentPink else AnimeBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.width(4.dp).height(64.dp).clip(RoundedCornerShape(8.dp)).background(accent),
            )
            Box(
                modifier = Modifier.size(42.dp).background(providerAccent(route.sourceId), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(route.sourceName.take(1), style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        route.sourceName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (recommended) RouteStatusBadge("推荐", AnimeAccentPink)
                    RouteStatusBadge(statusLabel, statusColor)
                }
                Text(
                    route.primaryRouteLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AnimeAccentCyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(route.title, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(route.protocol.displayName(), style = MaterialTheme.typography.labelLarge, color = accent)
                route.sizeBytes?.let { Text(formatBytes(it), style = MaterialTheme.typography.bodySmall, color = AnimeMuted) }
                RoutePlayActionLabel(route, recommended)
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
        seekFeedbackText = "$direction ${kotlin.math.abs(deltaMs) / 1000L} 秒 · ${formatPlaybackTime(target)}"
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
        danmakuItems = graph.danmakuRegistry.fetchBestTimeline(detail, currentEpisode)
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
    fun VideoStage(modifier: Modifier, compact: Boolean) {
        val danmakuTopPadding = if (controlsVisible) {
            if (compact) 36.dp else 52.dp
        } else {
            6.dp
        }
        val danmakuBottomPadding = when {
            !controlsVisible -> 6.dp
            activePanel != null -> if (compact) 18.dp else 210.dp
            compact -> 76.dp
            else -> 118.dp
        }
        Box(modifier.background(Color.Black)) {
            if (currentStream.protocol == StreamProtocol.BITTORRENT && torrentPlaybackUrl == null) {
                TorrentPlaceholderSurface(
                    stream = currentStream,
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
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .padding(top = danmakuTopPadding, bottom = danmakuBottomPadding),
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
                    episodeValue = currentEpisode.index?.let { index ->
                        if (detail.episodes.size > 1) "$index/${detail.episodes.size}" else "第 $index 集"
                    } ?: currentEpisode.title.ifBlank { "当前集" },
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
            AnimatedVisibility(
                visible = seekFeedbackText != null && activePanel == null && !controlsLocked,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(seekFeedbackPlacement.feedbackAlignment())
                    .padding(horizontal = if (seekFeedbackPlacement == PlayerSeekFeedbackPlacement.Center) 0.dp else 28.dp)
                    .offset(y = if (compact) 74.dp else 96.dp)
                    .zIndex(4.6f),
            ) {
                PlayerSeekFeedbackPill(
                    text = seekFeedbackText.orEmpty(),
                    placement = seekFeedbackPlacement,
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
                    onOffline = {
                        revealControls()
                        if (currentStream.protocol != StreamProtocol.BITTORRENT) {
                            graph.media3DownloadCoordinator.enqueue(currentStream, "${detail.title} ${currentEpisode.title}")
                        }
                    },
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
                    danmakuEnabled = danmakuEnabled,
                    density = density,
                    danmakuAlpha = danmakuAlpha,
                    danmakuFontScale = danmakuFontScale,
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
                    onSpeedSelected = {
                        revealControls()
                        playbackSpeed = it
                        activePanel = null
                    },
                    onShowPanel = { nextPanel ->
                        revealControls()
                        activePanel = nextPanel
                    },
                    onOffline = {
                        revealControls()
                        if (currentStream.protocol != StreamProtocol.BITTORRENT) {
                            graph.media3DownloadCoordinator.enqueue(currentStream, "${detail.title} ${currentEpisode.title}")
                        }
                    },
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
    val quality = stream.quality.orEmpty().ifBlank { "自动" }
    val message = routeNotice ?: errorMessage
    val currentEpisodeText = episode.index?.let { "第 $it 集" } ?: "当前集"
    val playbackStateText = formatPlaybackStateLabel(playbackState)
    val sourceName = routes.firstOrNull { it.stream.id == stream.id || it.stream.url == stream.url }?.sourceName
        ?: stream.metadata["routeProviderName"]
        ?: stream.providerId
    val playbackBrief = listOf(
        quality.takeIf { it != "自动" } ?: "自动清晰度",
        sourceName.takeIf { it.isNotBlank() },
        playbackStateText.takeIf { it.isNotBlank() },
        if (routes.size > 1) "自动推荐 · 可换源" else "自动推荐",
    )
        .filterNotNull()
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(" · ")
    val episodeActionValue = if (detail.episodes.size > 1) {
        episode.index?.let { "$it/${detail.episodes.size}" } ?: "${detail.episodes.size}集"
    } else {
        "当前"
    }
    val routeActionValue = if (routes.size > 1) "${routes.size}源" else "自动"
    val showRouteDiagnostics = message != null || hasPlaybackIssue
    val visibleEpisodes = remember(detail.episodes, episode.id) {
        portraitEpisodeWindow(detail.episodes, episode, maxCount = 18)
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(AnimeBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 15.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = detail.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    VideoMetaChip(currentEpisodeText)
                    VideoMetaChip(quality)
                    VideoMetaChip(playbackStateText)
                }
                Text(
                    text = episode.title,
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
                                    text = currentEpisodeText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.78f),
                                    maxLines = 1,
                                )
                            }
                            Text(
                                text = episode.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = playbackBrief,
                                style = MaterialTheme.typography.bodySmall,
                                color = AnimeMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    if (detail.episodes.size > 1 || routes.size > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (detail.episodes.size > 1) {
                                PortraitPlaybackAction(
                                    icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                                    title = "选集",
                                    subtitle = episodeActionValue,
                                    accent = AnimeAccentPink,
                                    onClick = { onShowPanel(PlayerPanel.Episode) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (routes.size > 1) {
                                PortraitPlaybackAction(
                                    icon = Icons.Filled.VideoLibrary,
                                    title = "换源",
                                    subtitle = routeActionValue,
                                    accent = AnimeAccentCyan,
                                    onClick = { onShowPanel(PlayerPanel.Route) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                    if (showRouteDiagnostics) {
                        PortraitRouteInsightRow(
                            routes = routes,
                            stream = stream,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = message ?: "当前播放源需要处理",
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                errorMessage != null -> MaterialTheme.colorScheme.error
                                routeNotice != null -> AnimeAccentAmber
                                else -> AnimeMuted
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (hasPlaybackIssue) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            TextButton(
                                onClick = onRetryRoute,
                                modifier = Modifier.weight(1f).height(38.dp).background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp)),
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, tint = AnimeAccentPink, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("重试当前", color = AnimeAccentPink)
                            }
                            TextButton(
                                onClick = onNextRoute,
                                enabled = canSelectNextRoute,
                                modifier = Modifier.weight(1f).height(38.dp).background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp)),
                            ) {
                                Icon(Icons.Filled.VideoLibrary, contentDescription = null, tint = if (canSelectNextRoute) AnimeAccentCyan else AnimeMuted, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("换个源", color = if (canSelectNextRoute) AnimeAccentCyan else AnimeMuted)
                            }
                        }
                    }
                }
            }
        }
        if (detail.episodes.size > 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("选集", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { onShowPanel(PlayerPanel.Episode) }) {
                        Text("全部 ${detail.episodes.size} 集", color = AnimeAccentCyan)
                    }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(visibleEpisodes, key = { it.id }) { item ->
                        val selected = item.id == episode.id
                        val loading = episodeLoadingId == item.id
                        Card(
                            onClick = { onEpisodeSelected(item) },
                            enabled = episodeLoadingId == null || loading,
                            modifier = Modifier.width(82.dp).height(48.dp).focusable(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = if (selected) AnimePanelSoft else AnimePanel),
                            border = BorderStroke(1.dp, if (selected) AnimeAccentCyan else AnimeBorder),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 9.dp, vertical = 7.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = item.index?.let { "%02d".format(it) } ?: "SP",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) AnimeAccentCyan else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                )
                                Text(
                                    text = if (loading) "加载中" else item.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) Color.White else AnimeMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    if (detail.episodes.size > visibleEpisodes.size) {
                        item {
                            PortraitEpisodeMoreCard(
                                count = detail.episodes.size,
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

private fun portraitEpisodeWindow(
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

@Composable
private fun PortraitPlaybackAction(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(34.dp).focusable(),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = accent.copy(alpha = 0.12f),
            contentColor = accent,
        ),
        contentPadding = PaddingValues(horizontal = 11.dp, vertical = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                text = subtitle,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PortraitEpisodeMoreCard(count: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(72.dp).height(48.dp).focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AnimePanelSoft),
        border = BorderStroke(1.dp, AnimeAccentCyan.copy(alpha = 0.42f)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 9.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("全部", style = MaterialTheme.typography.labelLarge, color = AnimeAccentCyan, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("${count}集", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.78f), maxLines = 1)
        }
    }
}

@Composable
private fun PortraitRouteInsightRow(
    routes: List<RouteCandidate>,
    stream: MediaStream,
    modifier: Modifier = Modifier,
) {
    val routeCoverageLabel = if (routes.isNotEmpty()) playerRouteCoverageLabel(routes) else "单线"
    val onlineRoutes = routes.filter { it.protocol != StreamProtocol.BITTORRENT && it.protocol != StreamProtocol.WEBVIEW_ONLY }
    val btRoutes = routes.filter { it.protocol == StreamProtocol.BITTORRENT }
    val onlineValue = routeInsightCountLabel(onlineRoutes)
    val btValue = routeInsightCountLabel(btRoutes)
    val currentLabel = stream.quality?.takeIf { it.isNotBlank() } ?: stream.protocol.displayName()
    val chips = listOf(
        Triple("覆盖", routeCoverageLabel, AnimeAccentCyan),
        Triple("在线", onlineValue ?: "待匹配", AnimeAccentPink),
        Triple("备用", btValue ?: "自动", AnimeAccentAmber),
        Triple("当前", currentLabel, AnimeAccentGreen),
    )

    LazyRow(
        modifier = modifier.height(31.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        contentPadding = PaddingValues(end = 2.dp),
    ) {
        items(chips.size) { index ->
            val (label, value, color) = chips[index]
            PortraitRouteInsightChip(label = label, value = value, color = color)
        }
    }
}

private fun routeInsightCountLabel(routes: List<RouteCandidate>): String? {
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

@Composable
private fun PortraitRouteInsightChip(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
        Text(
            text = value,
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
    episodeValue: String,
    routeCount: Int,
    routeCoverageLabel: String,
    playbackSpeed: Float,
    modifier: Modifier = Modifier,
) {
    val compactStatus = overlayState.error ?: overlayState.notice
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
                compactStatus?.let {
                    PlayerCompactStatusPill(
                        text = it,
                        error = overlayState.error != null,
                    )
                }
            } else {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = overlayState.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = listOf(overlayState.episodeTitle, overlayState.playbackState).filter { it.isNotBlank() }.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                PlayerTopRouteStatus(
                    routeLabel = overlayState.routeLabel,
                    statusLabel = overlayState.statusLabel,
                    accent = when {
                        overlayState.error != null -> MaterialTheme.colorScheme.error
                        overlayState.notice != null -> AnimeAccentAmber
                        else -> AnimeAccentCyan
                    },
                    onClick = onOpenRoutePanel,
                    modifier = Modifier.widthIn(min = 142.dp, max = 210.dp),
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
                overlayState = overlayState,
                episodeValue = episodeValue,
                routeCount = routeCount,
                routeCoverageLabel = routeCoverageLabel,
                playbackSpeed = playbackSpeed,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PlayerTopStatusStrip(
    overlayState: PlayerOverlayState,
    episodeValue: String,
    routeCount: Int,
    routeCoverageLabel: String,
    playbackSpeed: Float,
    modifier: Modifier = Modifier,
) {
    val sourceValue = if (routeCount > 1) {
        "${overlayState.sourceLabel} · $routeCoverageLabel"
    } else {
        overlayState.sourceLabel
    }
    val chips = listOf(
        PlayerTopStatusSpec("本集", episodeValue, AnimeAccentPink),
        PlayerTopStatusSpec("来源", sourceValue, AnimeAccentCyan),
        PlayerTopStatusSpec("清晰度", overlayState.qualityLabel, AnimeAccentAmber),
        PlayerTopStatusSpec("倍速", formatPlaybackSpeed(playbackSpeed), AnimeAccentGreen),
    )

    LazyRow(
        modifier = modifier.height(32.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        contentPadding = PaddingValues(end = 2.dp),
    ) {
        items(chips, key = { it.label }) { chip ->
            PlayerTopStatusChip(
                label = chip.label,
                value = chip.value,
                color = chip.color,
            )
        }
    }
}

private data class PlayerTopStatusSpec(
    val label: String,
    val value: String,
    val color: Color,
)

@Composable
private fun PlayerTopStatusChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(30.dp)
            .widthIn(min = 88.dp, max = 210.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.Black.copy(alpha = 0.28f))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
        Text(
            text = value.ifBlank { "自动" },
            modifier = Modifier.weight(1f, fill = false),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.86f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PlayerCompactStatusPill(
    text: String,
    error: Boolean,
    modifier: Modifier = Modifier,
) {
    val accent = if (error) MaterialTheme.colorScheme.error else Color.White.copy(alpha = 0.82f)
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = accent,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .widthIn(max = 132.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.Black.copy(alpha = 0.32f))
            .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
    )
}

@Composable
private fun PlayerTopRouteStatus(
    routeLabel: String,
    statusLabel: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(36.dp).clickable(onClick = onClick).focusable(),
        shape = RoundedCornerShape(999.dp),
        color = Color.Black.copy(alpha = 0.34f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.26f)),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                text = routeLabel,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
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
    text: String,
    placement: PlayerSeekFeedbackPlacement,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.58f))
            .border(1.dp, AnimeAccentPink.copy(alpha = 0.32f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        placement.feedbackIcon()?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AnimeAccentPink,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
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
        verticalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 7.dp),
    ) {
        if (!compact) {
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

            val bottomNotice = routeNotice ?: errorMessage
            if (bottomNotice != null) {
                PlayerFullscreenNoticeStrip(
                    title = routeSummary,
                    message = bottomNotice,
                    isError = errorMessage != null && routeNotice == null,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

        } else {
            PlayerCompactInteractionRow(
                progressFraction = if (durationMs > 0L) displayPositionMs.toFloat() / durationMs.toFloat() else null,
                danmakuEnabled = danmakuEnabled,
                onToggleDanmaku = onToggleDanmaku,
                onOpenDanmakuSettings = { onShowPanel(PlayerPanel.Danmaku) },
                onEnterFullscreen = onEnterFullscreen,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (compact && hasPlaybackIssue) {
            PlayerCompactRecoveryRow(
                canSelectNextRoute = canSelectNextRoute,
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
                offlineEnabled = currentStream.protocol != StreamProtocol.BITTORRENT,
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
    title: String,
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    val accent = if (isError) MaterialTheme.colorScheme.error else AnimeAccentAmber
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.34f))
            .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(accent),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.9f),
        )
        Text(
            text = message,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.4f),
        )
    }
}

@Composable
private fun PlayerCompactInteractionRow(
    progressFraction: Float?,
    danmakuEnabled: Boolean,
    onToggleDanmaku: () -> Unit,
    onOpenDanmakuSettings: () -> Unit,
    onEnterFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        PlayerCompactProgressLine(progressFraction = progressFraction, modifier = Modifier.fillMaxWidth())
        Row(
            modifier = Modifier.fillMaxWidth().height(36.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            PlayerCompactDanmakuInputBar(
                danmakuEnabled = danmakuEnabled,
                onOpenDanmakuSettings = onOpenDanmakuSettings,
                onToggleDanmaku = onToggleDanmaku,
                modifier = Modifier.weight(1f),
            )
            PlayerTinyIconAction(
                icon = Icons.Filled.Fullscreen,
                contentDescription = "全屏播放",
                onClick = onEnterFullscreen,
                modifier = Modifier.width(38.dp),
            )
        }
    }
}

@Composable
private fun PlayerCompactDanmakuInputBar(
    danmakuEnabled: Boolean,
    onOpenDanmakuSettings: () -> Unit,
    onToggleDanmaku: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(999.dp))
            .background(Color.Black.copy(alpha = 0.32f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onOpenDanmakuSettings,
            )
            .padding(start = 11.dp, end = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.ClosedCaption,
            contentDescription = null,
            tint = if (danmakuEnabled) AnimeAccentPink else Color.White.copy(alpha = 0.42f),
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = if (danmakuEnabled) "发条弹幕" else "弹幕关闭",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.76f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        TextButton(
            onClick = onToggleDanmaku,
            modifier = Modifier.width(32.dp).height(26.dp).focusable(),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.textButtonColors(
                containerColor = if (danmakuEnabled) AnimeAccentPink.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
                contentColor = if (danmakuEnabled) AnimeAccentPink else Color.White.copy(alpha = 0.56f),
            ),
            contentPadding = PaddingValues(0.dp),
        ) {
            Text(
                text = if (danmakuEnabled) "开" else "关",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun PlayerCompactProgressLine(
    progressFraction: Float?,
    modifier: Modifier = Modifier,
) {
    if (progressFraction != null) {
        LinearProgressIndicator(
            progress = { progressFraction.coerceIn(0f, 1f) },
            modifier = modifier.height(3.dp).clip(RoundedCornerShape(999.dp)),
            color = AnimeAccentPink,
            trackColor = Color.White.copy(alpha = 0.18f),
        )
    } else {
        Box(
            modifier
                .height(3.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.18f)),
        )
    }
}

@Composable
private fun PlayerTinyIconAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.width(38.dp),
    selected: Boolean = false,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(34.dp).focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) AnimeAccentPink.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f),
            contentColor = if (selected) AnimeAccentPink else Color.White.copy(alpha = 0.72f),
            disabledContainerColor = Color.White.copy(alpha = 0.05f),
            disabledContentColor = Color.White.copy(alpha = 0.34f),
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(19.dp))
    }
}

@Composable
private fun PlayerTinyToggle(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.width(44.dp),
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(34.dp).focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) AnimeAccentPink.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f),
            contentColor = if (selected) AnimeAccentPink else Color.White.copy(alpha = 0.68f),
            disabledContainerColor = Color.White.copy(alpha = 0.05f),
            disabledContentColor = Color.White.copy(alpha = 0.34f),
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, maxLines = 1, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PlayerCompactRecoveryRow(
    canSelectNextRoute: Boolean,
    onRetryRoute: () -> Unit,
    onNextRoute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(34.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayerTinyToggle(
            text = "重试",
            selected = true,
            onClick = onRetryRoute,
            modifier = Modifier.weight(1f),
        )
        PlayerTinyToggle(
            text = "换个源",
            selected = false,
            onClick = onNextRoute,
            modifier = Modifier.weight(1f),
            enabled = canSelectNextRoute,
        )
    }
}

@Composable
private fun PlayerFullscreenLockButton(
    locked: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.width(44.dp).height(44.dp).focusable(),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (locked) AnimeAccentPink.copy(alpha = 0.24f) else Color.Black.copy(alpha = 0.42f),
            contentColor = if (locked) AnimeAccentPink else Color.White.copy(alpha = 0.88f),
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Icon(
            imageVector = if (locked) Icons.Filled.LockOpen else Icons.Filled.Lock,
            contentDescription = if (locked) "解锁控制" else "锁定控制",
            modifier = Modifier.size(21.dp),
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
    Column(
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.34f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PlayerFullscreenDockButton(
            icon = Icons.Filled.ClosedCaption,
            label = if (danmakuEnabled) "弹幕开" else "弹幕关",
            selected = danmakuEnabled,
            onClick = onToggleDanmaku,
        )
        PlayerFullscreenDockButton(
            icon = Icons.Filled.HighQuality,
            label = "清晰度",
            onClick = { onShowPanel(PlayerPanel.Quality) },
        )
        PlayerFullscreenDockButton(
            icon = Icons.Filled.Speed,
            label = "倍速",
            onClick = { onShowPanel(PlayerPanel.Speed) },
        )
        PlayerFullscreenDockButton(
            icon = Icons.AutoMirrored.Filled.PlaylistPlay,
            label = "选集",
            enabled = episodeCount > 1,
            onClick = { onShowPanel(PlayerPanel.Episode) },
        )
        PlayerFullscreenDockButton(
            icon = Icons.Filled.VideoLibrary,
            label = "换源",
            enabled = routeCount > 1,
            onClick = { onShowPanel(PlayerPanel.Route) },
        )
        PlayerFullscreenDockButton(
            icon = Icons.Filled.MoreVert,
            label = "更多",
            onClick = { onShowPanel(PlayerPanel.More) },
        )
    }
}

@Composable
private fun PlayerFullscreenDockButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    selected: Boolean = false,
    enabled: Boolean = true,
) {
    val accent = if (selected) AnimeAccentPink else Color.White.copy(alpha = 0.82f)
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.width(48.dp).height(48.dp).focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) AnimeAccentPink.copy(alpha = 0.16f) else Color.Transparent,
            contentColor = accent,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.White.copy(alpha = 0.32f),
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
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
    offlineEnabled: Boolean,
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
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayerDanmakuInputBar(
                danmakuEnabled = danmakuEnabled,
                onToggleDanmaku = onToggleDanmaku,
                onOpenDanmakuSettings = { onShowPanel(PlayerPanel.Danmaku) },
                modifier = Modifier.weight(0.78f),
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
                offlineEnabled = offlineEnabled,
                hasPlaybackIssue = hasPlaybackIssue,
                canSelectNextRoute = canSelectNextRoute,
                onShowPanel = onShowPanel,
                onNextEpisode = onNextEpisode,
                onOffline = onOffline,
                onRetryRoute = onRetryRoute,
                onNextRoute = onNextRoute,
                modifier = Modifier.weight(2.34f),
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
    Row(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.26f))
            .border(
                1.dp,
                if (hasPlaybackIssue) MaterialTheme.colorScheme.error.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RouteStatusBadge(if (hasPlaybackIssue) "播放异常" else "正在播放", if (hasPlaybackIssue) MaterialTheme.colorScheme.error else AnimeAccentGreen)
        Text(
            text = routeSummary,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.86f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        PlayerStatusTinyText(quality)
        PlayerStatusTinyText(formatPlaybackSpeed(playbackSpeed))
        if (routeCount > 1) {
            PlayerStatusTinyText(routeCoverageLabel)
        }
        if (episodeCount > 1) {
            PlayerStatusTinyText("${episodeCount}集")
        }
    }
}

@Composable
private fun PlayerStatusTinyText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = 0.62f),
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
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.Black.copy(alpha = 0.32f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onOpenDanmakuSettings,
            )
            .padding(start = 10.dp, end = 6.dp),
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
            text = if (danmakuEnabled) "点我发弹幕" else "弹幕已关闭",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        TextButton(
            onClick = onToggleDanmaku,
            modifier = Modifier.width(42.dp).height(26.dp).focusable(),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.textButtonColors(
                containerColor = if (danmakuEnabled) AnimeAccentPink.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f),
                contentColor = if (danmakuEnabled) AnimeAccentPink else Color.White.copy(alpha = 0.56f),
            ),
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
        ) {
            Text(
                text = if (danmakuEnabled) "开" else "关",
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
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.Black.copy(alpha = 0.30f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(999.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayerFullscreenSeekButton(
            icon = Icons.Filled.Replay10,
            contentDescription = "后退 10 秒",
            onClick = onSeekBackward,
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(18.dp)
                .background(Color.White.copy(alpha = 0.10f)),
        )
        PlayerFullscreenSeekButton(
            icon = Icons.Filled.Forward10,
            contentDescription = "快进 10 秒",
            onClick = onSeekForward,
        )
    }
}

@Composable
private fun PlayerFullscreenSeekButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(width = 42.dp, height = 36.dp).focusable(),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White.copy(alpha = 0.84f),
            modifier = Modifier.size(18.dp),
        )
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
    offlineEnabled: Boolean,
    hasPlaybackIssue: Boolean,
    canSelectNextRoute: Boolean,
    onShowPanel: (PlayerPanel) -> Unit,
    onNextEpisode: () -> Unit,
    onOffline: () -> Unit,
    onRetryRoute: () -> Unit,
    onNextRoute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actions = buildList {
        if (hasPlaybackIssue) {
            add(
                PlayerActionSpec(
                    icon = Icons.Filled.Refresh,
                    title = "重试",
                    value = "当前",
                    selected = true,
                    onClick = onRetryRoute,
                ),
            )
            add(
                PlayerActionSpec(
                    icon = Icons.Filled.VideoLibrary,
                    title = "换个源",
                    value = if (canSelectNextRoute) "可切" else "无",
                    enabled = canSelectNextRoute,
                    onClick = onNextRoute,
                ),
            )
        }
        add(
            PlayerActionSpec(
                icon = Icons.Filled.HighQuality,
                title = "清晰度",
                value = quality,
                selected = activePanel == PlayerPanel.Quality,
                enabled = routeCount > 0,
                onClick = { onShowPanel(PlayerPanel.Quality) },
            ),
        )
        add(
            PlayerActionSpec(
                icon = Icons.Filled.Speed,
                title = "倍速",
                value = formatPlaybackSpeed(playbackSpeed),
                selected = activePanel == PlayerPanel.Speed,
                onClick = { onShowPanel(PlayerPanel.Speed) },
            ),
        )
        add(
            PlayerActionSpec(
                icon = Icons.Filled.VideoLibrary,
                title = "换源",
                value = routeCoverageLabel,
                selected = activePanel == PlayerPanel.Route,
                enabled = routeCount > 1,
                onClick = { onShowPanel(PlayerPanel.Route) },
            ),
        )
        add(
            PlayerActionSpec(
                icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                title = "选集",
                value = if (episodeCount > 1) "${episodeCount}集" else "单集",
                selected = activePanel == PlayerPanel.Episode,
                enabled = episodeCount > 1,
                onClick = { onShowPanel(PlayerPanel.Episode) },
            ),
        )
        add(
            PlayerActionSpec(
                icon = Icons.Filled.SkipNext,
                title = "下一集",
                value = nextEpisode?.index?.let { "第${it}集" } ?: nextEpisode?.title ?: "无",
                enabled = nextEpisode != null,
                onClick = onNextEpisode,
            ),
        )
        add(
            PlayerActionSpec(
                icon = Icons.Filled.CloudDownload,
                title = "缓存",
                value = if (offlineEnabled) "离线" else "不可用",
                enabled = offlineEnabled,
                onClick = onOffline,
            ),
        )
        add(
            PlayerActionSpec(
                icon = Icons.Filled.MoreVert,
                title = "更多",
                value = "设置",
                selected = activePanel == PlayerPanel.More,
                onClick = { onShowPanel(PlayerPanel.More) },
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
                    title = action.title,
                    value = if (compactValues) null else action.value,
                    selected = action.selected,
                    enabled = action.enabled,
                    onClick = action.onClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private data class PlayerActionSpec(
    val icon: ImageVector?,
    val title: String,
    val value: String? = null,
    val selected: Boolean = false,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

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
    failedStreamIds: Set<String>,
    onDismiss: () -> Unit,
    onRouteSelected: (RouteCandidate) -> Unit,
    onEpisodeSelected: (Episode) -> Unit,
    onToggleDanmaku: () -> Unit,
    onDensityChange: (Float) -> Unit,
    onDanmakuAlphaChange: (Float) -> Unit,
    onDanmakuFontScaleChange: (Float) -> Unit,
    onSpeedSelected: (Float) -> Unit,
    onShowPanel: (PlayerPanel) -> Unit,
    onOffline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val routeCoverageLabel = remember(routeOptions) { playerRouteCoverageLabel(routeOptions) }
    BoxWithConstraints(modifier = modifier) {
        val landscape = maxWidth > maxHeight
        val panelWidth = when {
            !landscape -> maxWidth
            maxWidth < 680.dp -> maxWidth * 0.54f
            else -> 392.dp
        }
        val portraitPanelHeight = when {
            maxHeight < 620.dp -> maxHeight * 0.72f
            else -> maxHeight * 0.58f
        }
        val portraitPanelMinHeight = when {
            maxHeight < 420.dp -> maxHeight * 0.66f
            maxHeight < 520.dp -> 260.dp
            else -> 320.dp
        }
        val currentRoute = routeOptions.firstOrNull { route ->
            route.stream.id == currentStream.id || route.stream.url == currentStream.url
        }
        val scrimAlpha = if (landscape) 0.14f else 0.32f
        val panelInteractionSource = remember { MutableInteractionSource() }
        val panelModifier = if (landscape) {
            Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp, top = 14.dp, bottom = 14.dp)
                .fillMaxHeight()
                .width(panelWidth)
        } else {
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .heightIn(min = portraitPanelMinHeight, max = portraitPanelHeight)
        }
        val panelShape = if (landscape) {
            RoundedCornerShape(8.dp)
        } else {
            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
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
            color = Color(0xF217171C),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PlayerPanelHeader(
                    title = playerPanelTitle(panel),
                    subtitle = playerPanelSubtitle(panel),
                    onDismiss = onDismiss,
                )
                PlayerPanelContextBar(
                    title = detail.title,
                    episode = currentEpisode,
                    sourceLabel = currentRoute?.sourceName ?: currentStream.metadata["routeProviderName"] ?: currentStream.providerId,
                    quality = currentStream.quality.orEmpty().ifBlank { "自动" },
                    playbackSpeed = playbackSpeed,
                    modifier = Modifier.fillMaxWidth(),
                )
                PlayerPanelQuickTabs(
                    selectedPanel = panel,
                    routeCount = routeOptions.size,
                    routeCoverageLabel = routeCoverageLabel,
                    episodeCount = detail.episodes.size,
                    danmakuEnabled = danmakuEnabled,
                    onSelected = onShowPanel,
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    when (panel) {
                        PlayerPanel.More -> PlayerMorePanel(
                            routeCount = routeOptions.size,
                            routeCoverageLabel = routeCoverageLabel,
                            episodeCount = detail.episodes.size,
                            routeLabel = currentRoute?.primaryRouteLabel() ?: currentStream.protocol.displayName(),
                            quality = currentStream.quality.orEmpty().ifBlank { "自动" },
                            playbackSpeed = playbackSpeed,
                            danmakuEnabled = danmakuEnabled,
                            offlineEnabled = currentStream.protocol != StreamProtocol.BITTORRENT,
                            onShowPanel = onShowPanel,
                            onOffline = onOffline,
                        )
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

private data class PlayerPanelTabSpec(
    val panel: PlayerPanel,
    val label: String,
    val icon: ImageVector,
    val value: String?,
    val enabled: Boolean = true,
    val selected: Boolean = false,
)

@Composable
private fun PlayerPanelHeader(title: String, subtitle: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.56f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        TextButton(onClick = onDismiss, modifier = Modifier.height(34.dp)) {
            Text("收起", color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun PlayerPanelContextBar(
    title: String,
    episode: Episode,
    sourceLabel: String,
    quality: String,
    playbackSpeed: Float,
    modifier: Modifier = Modifier,
) {
    val episodeLabel = episode.index?.let { "第 $it 集" } ?: "当前集"
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AnimeAccentPink.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = AnimeAccentPink, modifier = Modifier.size(18.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    listOf(episodeLabel, sourceLabel.ifBlank { "自动源" }, quality, formatPlaybackSpeed(playbackSpeed)).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = AnimeMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            RouteStatusBadge("播放中", AnimeAccentGreen)
        }
    }
}

@Composable
private fun PlayerPanelQuickTabs(
    selectedPanel: PlayerPanel,
    routeCount: Int,
    routeCoverageLabel: String,
    episodeCount: Int,
    danmakuEnabled: Boolean,
    onSelected: (PlayerPanel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(
        PlayerPanelTabSpec(
            panel = PlayerPanel.Quality,
            label = "清晰度",
            icon = Icons.Filled.HighQuality,
            value = null,
            enabled = routeCount > 0,
        ),
        PlayerPanelTabSpec(
            panel = PlayerPanel.Speed,
            label = "倍速",
            icon = Icons.Filled.Speed,
            value = null,
        ),
        PlayerPanelTabSpec(
            panel = PlayerPanel.Route,
            label = "换源",
            icon = Icons.Filled.VideoLibrary,
            value = routeCoverageLabel,
            enabled = routeCount > 1,
        ),
        PlayerPanelTabSpec(
            panel = PlayerPanel.Episode,
            label = "选集",
            icon = Icons.AutoMirrored.Filled.PlaylistPlay,
            value = if (episodeCount > 1) "${episodeCount}集" else "单集",
            enabled = episodeCount > 1,
        ),
        PlayerPanelTabSpec(
            panel = PlayerPanel.Danmaku,
            label = "弹幕",
            icon = Icons.Filled.ClosedCaption,
            value = if (danmakuEnabled) "开" else "关",
            selected = danmakuEnabled,
        ),
        PlayerPanelTabSpec(
            panel = PlayerPanel.More,
            label = "设置",
            icon = Icons.Filled.MoreVert,
            value = null,
        ),
    )
    LazyRow(
        modifier = modifier.height(38.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        contentPadding = PaddingValues(horizontal = 1.dp),
    ) {
        items(tabs, key = { it.panel }) { tab ->
            val selected = tab.panel == selectedPanel
            PlayerPanelQuickTab(
                tab = tab,
                selected = selected,
                onClick = { onSelected(tab.panel) },
            )
        }
    }
}

@Composable
private fun PlayerPanelQuickTab(
    tab: PlayerPanelTabSpec,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accent = when {
        selected -> AnimeAccentPink
        tab.selected -> AnimeAccentCyan
        else -> Color.White.copy(alpha = 0.72f)
    }
    TextButton(
        onClick = onClick,
        enabled = tab.enabled || selected,
        modifier = Modifier.width(86.dp).height(36.dp).focusable(),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) AnimeAccentPink.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f),
            contentColor = accent,
            disabledContainerColor = Color.White.copy(alpha = 0.035f),
            disabledContentColor = Color.White.copy(alpha = 0.32f),
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(tab.icon, contentDescription = null, modifier = Modifier.size(15.dp))
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
                    color = accent.copy(alpha = if (tab.enabled || selected) 0.76f else 0.48f),
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
    offlineEnabled: Boolean,
    onShowPanel: (PlayerPanel) -> Unit,
    onOffline: () -> Unit,
) {
    val actions = listOf(
        PlayerMoreAction(
            title = "清晰度",
            subtitle = quality,
            icon = Icons.Filled.HighQuality,
            onClick = { onShowPanel(PlayerPanel.Quality) },
        ),
        PlayerMoreAction(
            title = "倍速",
            subtitle = formatPlaybackSpeed(playbackSpeed),
            icon = Icons.Filled.Speed,
            onClick = { onShowPanel(PlayerPanel.Speed) },
        ),
        PlayerMoreAction(
            title = "选集",
            subtitle = "共 $episodeCount 集",
            icon = Icons.AutoMirrored.Filled.PlaylistPlay,
            enabled = episodeCount > 1,
            onClick = { onShowPanel(PlayerPanel.Episode) },
        ),
        PlayerMoreAction(
            title = "换源",
            subtitle = if (routeCount > 1) routeCoverageLabel else "自动推荐",
            icon = Icons.Filled.VideoLibrary,
            enabled = routeCount > 1,
            onClick = { onShowPanel(PlayerPanel.Route) },
        ),
        PlayerMoreAction(
            title = "弹幕",
            subtitle = if (danmakuEnabled) "已开启" else "已关闭",
            icon = Icons.Filled.ClosedCaption,
            selected = danmakuEnabled,
            onClick = { onShowPanel(PlayerPanel.Danmaku) },
        ),
        PlayerMoreAction(
            title = "缓存",
            subtitle = if (offlineEnabled) "本集离线" else "暂不支持",
            icon = Icons.Filled.CloudDownload,
            enabled = offlineEnabled,
            onClick = onOffline,
        ),
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PlayerMoreSummaryCard(
            routeLabel = routeLabel,
            quality = quality,
            playbackSpeed = playbackSpeed,
            routeCoverageLabel = routeCoverageLabel,
            episodeCount = episodeCount,
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
    routeLabel: String,
    quality: String,
    playbackSpeed: Float,
    routeCoverageLabel: String,
    episodeCount: Int,
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
                    RouteStatusBadge("当前设置", AnimeAccentPink)
                    Text(
                        text = listOf("清晰度 $quality", "倍速 ${formatPlaybackSpeed(playbackSpeed)}").joinToString(" · "),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.82f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(
                    text = listOf(
                        "当前源 ${routeLabel.ifBlank { "自动推荐" }}",
                        routeCoverageLabel,
                        "${episodeCount.coerceAtLeast(1)} 集",
                    ).joinToString(" · "),
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
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val enabled: Boolean = true,
    val selected: Boolean = false,
    val onClick: () -> Unit,
)

@Composable
private fun PlayerMoreActionTile(action: PlayerMoreAction, modifier: Modifier = Modifier) {
    val border = if (action.selected) {
        BorderStroke(1.dp, AnimeAccentCyan.copy(alpha = 0.62f))
    } else {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    }
    val container = if (action.selected) {
        AnimeAccentCyan.copy(alpha = 0.16f)
    } else {
        Color.White.copy(alpha = 0.08f)
    }
    Surface(
        modifier = modifier
            .height(58.dp)
            .alpha(if (action.enabled) 1f else 0.42f)
            .clickable(enabled = action.enabled, onClick = action.onClick),
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
                    .background(if (action.selected) AnimeAccentCyan.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.title,
                    tint = if (action.selected) AnimeAccentCyan else Color.White.copy(alpha = 0.88f),
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = action.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.56f),
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
            value = density.coerceIn(0.3f, 1f),
            valueRange = 0.3f..1f,
            steps = 2,
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
            value = fontScale.coerceIn(0.62f, 1.08f),
            valueRange = 0.62f..1.08f,
            steps = 8,
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
        Text("当前播放源没有提供可切换清晰度", style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
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
    failedStreamIds: Set<String>,
    onRouteSelected: (RouteCandidate) -> Unit,
) {
    if (routes.isEmpty()) {
        Text("暂时没有可用播放源", style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
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
    val sourceCount = remember(routes) { routes.map { it.sourceId }.distinct().size }
    val showSourceStrip = detailedMode || sourceCount > 1
    val visibleRoutes = remember(routes, selectedSourceId, failedStreamIds) {
        routePanelVisibleRoutes(
            routes = routes,
            selectedSourceId = selectedSourceId,
            failedStreamIds = failedStreamIds,
        )
    }
    val selectedSourceName = selectedSourceId?.let { sourceId ->
        routes.firstOrNull { it.sourceId == sourceId }?.sourceName ?: sourceId
    } ?: "全部播放源"
    val sourceListTitle = when {
        detailedMode && selectedSourceId == null -> "全部播放源"
        detailedMode -> "已筛选来源"
        selectedSourceId == null -> "推荐源"
        else -> "筛选播放源"
    }
    val routeListTitle = when {
        selectedSourceId != null -> "$sourceListTitle · $selectedSourceName (${visibleRoutes.size})"
        else -> "$sourceListTitle (${visibleRoutes.size})"
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
        if (showSourceStrip) {
            item {
                PlayerRouteSourceStrip(
                    routes = routes,
                    selectedStreamId = selectedStreamId,
                    selectedSourceId = selectedSourceId,
                    recommendedStreamId = panelState.recommendedRoute?.stream?.id,
                    failedStreamIds = failedStreamIds,
                    detailedMode = detailedMode,
                    onSourceSelected = { selectedSourceId = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        item {
            Text(
                routeListTitle,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.72f),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        items(visibleRoutes, key = { it.stream.id }) { route ->
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
private fun PlayerRouteSourceStrip(
    routes: List<RouteCandidate>,
    selectedStreamId: String,
    selectedSourceId: String?,
    recommendedStreamId: String?,
    failedStreamIds: Set<String>,
    detailedMode: Boolean,
    onSourceSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val groups = remember(routes, selectedStreamId, selectedSourceId, recommendedStreamId, failedStreamIds) {
        buildRouteSourceGroups(
            routes = routes,
            selectedSourceId = selectedSourceId,
            selectedStreamId = selectedStreamId,
            recommendedStreamId = recommendedStreamId,
            failedStreamIds = failedStreamIds,
            includeAll = true,
        )
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            if (detailedMode) "按来源筛选" else "播放源分组",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.72f),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(groups, key = { it.id }) { group ->
                PlayerRouteSourceChip(
                    group = group,
                    detailedMode = detailedMode,
                    onClick = {
                        onSourceSelected(if (group.isAll) null else group.id)
                    },
                )
            }
        }
    }
}

@Composable
private fun PlayerRouteSourceChip(
    group: RouteSourceGroupUiState,
    detailedMode: Boolean,
    onClick: () -> Unit,
) {
    val accent = when {
        group.isFilterSelected -> AnimeAccentCyan
        group.hasSelected -> AnimeAccentCyan
        group.hasRecommended -> AnimeAccentPink
        group.onlineCount > 0 -> AnimeAccentGreen
        group.btCount > 0 -> AnimeAccentAmber
        else -> AnimeMuted
    }
    Card(
        onClick = onClick,
        modifier = Modifier.width(if (detailedMode) 152.dp else 132.dp).height(if (detailedMode) 74.dp else 46.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (group.isFilterSelected || group.hasSelected || group.hasRecommended) {
                Color.White.copy(alpha = 0.08f)
            } else {
                Color.White.copy(alpha = 0.045f)
            },
        ),
        border = BorderStroke(1.dp, accent.copy(alpha = if (group.isFilterSelected || group.hasSelected || group.hasRecommended) 0.85f else 0.34f)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = if (detailedMode) Arrangement.SpaceBetween else Arrangement.Center,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    group.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                when {
                    group.isFilterSelected && group.isAll -> RouteStatusBadge("全部", AnimeAccentCyan)
                    group.isFilterSelected -> RouteStatusBadge("已选", AnimeAccentCyan)
                    group.hasSelected -> RouteStatusBadge("当前", AnimeAccentCyan)
                    group.hasRecommended -> RouteStatusBadge("推荐", AnimeAccentPink)
                }
            }
            if (detailedMode) {
                Text(
                    "${group.playableCount}/${group.totalCount} 可播 · ${group.onlineCount} 在线 · ${group.btCount} BT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.66f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (group.failedCount > 0) {
                    Text(
                        "${group.failedCount} 条失败已降级",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    Text(
                        if (group.onlineCount > 0) "在线播放" else "备用来源",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        maxLines = 1,
                    )
                }
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
    val webOnly = route.protocol == StreamProtocol.WEBVIEW_ONLY
    val accent = when {
        failed -> MaterialTheme.colorScheme.error
        selected -> AnimeAccentCyan
        recommended -> AnimeAccentPink
        route.protocol == StreamProtocol.BITTORRENT -> AnimeAccentAmber
        webOnly -> AnimeMuted
        else -> AnimeAccentGreen
    }
    val (statusLabel, statusColor) = when {
        failed -> "已失败" to MaterialTheme.colorScheme.error
        selected -> "当前" to AnimeAccentCyan
        else -> route.routeStatusLabel()
    }
    val primaryTitle = if (detailedMode) route.sourceName else route.primaryRouteLabel()
    val secondaryTitle = if (detailedMode) route.primaryRouteLabel() else route.sourceName
    Card(
        onClick = onClick,
        enabled = !webOnly,
        modifier = Modifier.fillMaxWidth().focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected || recommended) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.045f),
            disabledContainerColor = Color.White.copy(alpha = 0.035f),
        ),
        border = BorderStroke(1.dp, if (selected || recommended || failed) accent.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.08f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.width(4.dp).height(if (detailedMode) 58.dp else 46.dp).clip(RoundedCornerShape(8.dp)).background(accent),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        primaryTitle,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (recommended) RouteStatusBadge("推荐", AnimeAccentPink)
                    RouteStatusBadge(statusLabel, statusColor)
                }
                Text(
                    secondaryTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (detailedMode) AnimeAccentCyan else AnimeMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (detailedMode) {
                    Text(
                        listOfNotNull(route.title, route.protocol.displayName()).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = AnimeMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                if (detailedMode) {
                    Text(route.protocol.displayName(), style = MaterialTheme.typography.labelMedium, color = accent, maxLines = 1)
                    route.sizeBytes?.let { Text(formatBytes(it), style = MaterialTheme.typography.labelSmall, color = AnimeMuted, maxLines = 1) }
                }
                PlayerRouteActionLabel(
                    selected = selected,
                    recommended = recommended,
                    failed = failed,
                    webOnly = webOnly,
                    bt = route.protocol == StreamProtocol.BITTORRENT,
                )
            }
        }
    }
}

@Composable
private fun PlayerRouteActionLabel(
    selected: Boolean,
    recommended: Boolean,
    failed: Boolean,
    webOnly: Boolean,
    bt: Boolean,
) {
    val (label, color) = when {
        failed -> "重试" to MaterialTheme.colorScheme.error
        selected -> "播放中" to AnimeAccentCyan
        webOnly -> "暂不可选" to AnimeMuted
        bt -> "边下边播" to AnimeAccentAmber
        recommended -> "使用推荐" to AnimeAccentPink
        else -> "切换" to AnimeAccentGreen
    }
    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.13f))
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.VideoLibrary,
                    contentDescription = null,
                    tint = AnimeAccentCyan,
                    modifier = Modifier.size(20.dp),
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        if (detailedMode) "自动推荐 · 共 ${state.totalCount} 源" else "推荐源 · 可播 ${state.availableCount} 源",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        if (detailedMode) {
                            state.recommendedRoute?.let { route ->
                                "推荐 ${route.sourceName} · ${route.routeName.orEmpty().ifBlank { route.protocol.displayName() }}"
                            } ?: "暂无推荐源"
                        } else {
                            state.recommendedRoute?.let { "已按清晰度和稳定性排序，可直接观看或换源" } ?: "暂时没有推荐源"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = AnimeMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (detailedMode) state.selectedRoute?.let { route ->
                        Text(
                            "当前 ${route.sourceName} · ${route.routeName.orEmpty().ifBlank { route.protocol.displayName() }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.62f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                TextButton(
                    onClick = onToggleDetailed,
                    modifier = Modifier.width(58.dp).height(32.dp).focusable(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (detailedMode) AnimeAccentCyan.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.08f),
                        contentColor = if (detailedMode) AnimeAccentCyan else Color.White.copy(alpha = 0.74f),
                    ),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(if (detailedMode) "简单" else "详细", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
            if (detailedMode || state.failedCount > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoutePanelMetricChip("可用", state.availableCount.toString(), AnimeAccentGreen)
                    if (detailedMode) {
                        RoutePanelMetricChip("在线", state.onlineCount.toString(), AnimeAccentCyan)
                        RoutePanelMetricChip("BT", state.btCount.toString(), AnimeAccentAmber)
                    }
                    if (state.failedCount > 0) {
                        RoutePanelMetricChip("失败", state.failedCount.toString(), MaterialTheme.colorScheme.error)
                    }
                }
            }
            notice?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = AnimeAccentAmber,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RoutePanelMetricChip(
    label: String,
    value: String,
    color: Color,
) {
    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.28f))
            .padding(horizontal = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f),
            maxLines = 1,
        )
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            color = color,
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
    if (detail.episodes.isEmpty()) {
        Text("当前条目没有可切换选集", style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item {
            PlayerEpisodeSummaryCard(
                title = detail.title,
                currentEpisode = currentEpisode,
                episodeCount = detail.episodes.size,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Text(
                "全部选集",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.72f),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
        items(detail.episodes, key = { it.id }) { item ->
            val loading = episodeLoadingId == item.id
            PlayerEpisodeOptionRow(
                episode = item,
                selected = item.id == currentEpisode.id,
                loading = loading,
                enabled = episodeLoadingId == null || loading,
                onClick = { onEpisodeSelected(item) },
            )
        }
    }
}

@Composable
private fun PlayerEpisodeSummaryCard(
    title: String,
    currentEpisode: Episode,
    episodeCount: Int,
    modifier: Modifier = Modifier,
) {
    val currentEpisodeLabel = currentEpisode.index?.let { "第 $it 集" } ?: currentEpisode.title
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(AnimeAccentPink.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null, tint = AnimeAccentPink, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "当前 $currentEpisodeLabel · 共 $episodeCount 集",
                    style = MaterialTheme.typography.bodySmall,
                    color = AnimeMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                    RouteStatusBadge("正在看", AnimeAccentPink)
                    RouteStatusBadge("自动匹配", AnimeAccentCyan)
                    Text(
                        "切换选集后自动选择最佳播放源",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.64f),
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
    episode: Episode,
    selected: Boolean,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val accent = when {
        loading -> AnimeAccentAmber
        selected -> AnimeAccentPink
        else -> AnimeAccentCyan
    }
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().focusable(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.045f),
            disabledContainerColor = Color.White.copy(alpha = 0.032f),
        ),
        border = BorderStroke(1.dp, if (selected || loading) accent.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.08f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.width(4.dp).height(52.dp).clip(RoundedCornerShape(8.dp)).background(accent.copy(alpha = if (enabled) 1f else 0.38f)),
            )
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(8.dp)).background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                if (loading) {
                    CircularProgressIndicator(color = accent, modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        episode.index?.let { "%02d".format(it) } ?: "SP",
                        style = MaterialTheme.typography.labelLarge,
                        color = accent,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        episode.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = if (enabled) 0.94f else 0.42f),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (selected) RouteStatusBadge("当前", AnimeAccentPink)
                }
                Text(
                    episode.index?.let { "第 $it 集" } ?: "特别篇",
                    style = MaterialTheme.typography.bodySmall,
                    color = AnimeMuted.copy(alpha = if (enabled) 0.86f else 0.38f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            EpisodeActionLabel(selected = selected, loading = loading, enabled = enabled)
        }
    }
}

@Composable
private fun EpisodeActionLabel(
    selected: Boolean,
    loading: Boolean,
    enabled: Boolean,
) {
    val (label, color) = when {
        loading -> "加载中" to AnimeAccentAmber
        selected -> "播放中" to AnimeAccentPink
        enabled -> "播放" to AnimeAccentCyan
        else -> "等待" to AnimeMuted
    }
    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = if (enabled || loading || selected) 0.13f else 0.06f))
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
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
    title: String,
    value: String? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.widthIn(min = 58.dp, max = 112.dp),
) {
    val contentColor = when {
        !enabled -> Color.White.copy(alpha = 0.34f)
        selected -> AnimeAccentPink
        else -> Color.White.copy(alpha = 0.9f)
    }
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(34.dp)
            .focusable(),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) AnimeAccentPink.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.16f),
            contentColor = contentColor,
            disabledContainerColor = Color.Black.copy(alpha = 0.1f),
            disabledContentColor = Color.White.copy(alpha = 0.36f),
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
    ) {
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
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
                    color = contentColor.copy(alpha = if (enabled) 0.68f else 0.5f),
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
        PlayerPanel.More -> "播放设置"
        PlayerPanel.Danmaku -> "弹幕设置"
        PlayerPanel.Quality -> "清晰度"
        PlayerPanel.Speed -> "播放速度"
        PlayerPanel.Route -> "播放源"
        PlayerPanel.Episode -> "选集"
    }
}

private fun playerPanelSubtitle(panel: PlayerPanel): String {
    return when (panel) {
        PlayerPanel.More -> "清晰度 · 倍速 · 选集 · 换源"
        PlayerPanel.Danmaku -> "密度 · 透明度 · 字号"
        PlayerPanel.Quality -> "当前可用质量"
        PlayerPanel.Speed -> "0.5x 至 2.0x"
        PlayerPanel.Route -> "推荐优先 · 手动换源"
        PlayerPanel.Episode -> "合集进度 · 自动匹配"
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

private fun formatPlaybackStateLabel(label: String): String {
    return when (label.uppercase()) {
        "IDLE" -> "等待播放"
        "BUFFERING" -> "缓冲中"
        "READY" -> "播放就绪"
        "ENDED" -> "已播完"
        else -> label.ifBlank { "播放中" }
    }
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
        density < 0.45f -> 0.62f
        density < 0.82f -> 1f
        else -> 0.3f
    }
}

private fun formatDanmakuDensity(density: Float): String {
    return when {
        density < 0.45f -> "30%"
        density < 0.82f -> "60%"
        else -> "100%"
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
