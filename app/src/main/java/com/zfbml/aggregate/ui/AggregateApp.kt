package com.zfbml.aggregate.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zfbml.aggregate.AppGraph
import com.zfbml.aggregate.auth.AuthWebView
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
import com.zfbml.aggregate.source.SearchResult
import com.zfbml.aggregate.source.StreamProtocol
import com.zfbml.aggregate.torrent.TorrentEngineState
import kotlinx.coroutines.launch

@Composable
fun AggregateApp(graph: AppGraph) {
    var screen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (val current = screen) {
            AppScreen.Home -> HomeScreen(
                graph = graph,
                onOpenDetail = { screen = AppScreen.Detail(it) },
                onOpenAuth = { domain -> screen = AppScreen.Auth(domain) },
            )
            is AppScreen.Detail -> DetailScreen(
                graph = graph,
                result = current.result,
                onBack = { screen = AppScreen.Home },
                onPlay = { detail, episode, stream -> screen = AppScreen.Player(detail, episode, stream) },
            )
            is AppScreen.Player -> PlayerScreen(
                graph = graph,
                detail = current.detail,
                episode = current.episode,
                stream = current.stream,
                onBack = { screen = AppScreen.Detail(SearchResult(current.detail.providerId, current.detail.title, current.detail.url)) },
            )
            is AppScreen.Auth -> AuthScreen(
                domain = current.domain,
                onBack = { screen = AppScreen.Home },
            )
        }
    }
}

private sealed interface AppScreen {
    data object Home : AppScreen
    data class Detail(val result: SearchResult) : AppScreen
    data class Player(val detail: MediaDetail, val episode: Episode, val stream: MediaStream) : AppScreen
    data class Auth(val domain: String) : AppScreen
}

@Composable
private fun HomeScreen(
    graph: AppGraph,
    onOpenDetail: (SearchResult) -> Unit,
    onOpenAuth: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("demo") }
    var results by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    val providers = graph.sourceRegistry.manifests
    val authProfiles = graph.danmakuRegistry.profiles

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(
            modifier = Modifier
                .width(360.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Zfbml Aggregate", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search or paste URL") },
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusable(),
                onClick = {
                    scope.launch {
                        loading = true
                        results = graph.sourceRegistry.searchAll(query)
                        loading = false
                    }
                },
            ) {
                Text("Search all sources")
            }
            Text("Sources", style = MaterialTheme.typography.titleMedium)
            providers.forEach { manifest ->
                Text("${manifest.name}  ${manifest.version}", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(8.dp))
            Text("Danmaku auth", style = MaterialTheme.typography.titleMedium)
            authProfiles.forEach { profile ->
                val domain = when (profile.platform) {
                    DanmakuPlatform.Bilibili -> "bilibili.com"
                    DanmakuPlatform.Tencent -> "v.qq.com"
                    DanmakuPlatform.Iqiyi -> "iqiyi.com"
                    DanmakuPlatform.Youku -> "youku.com"
                    DanmakuPlatform.Local -> ""
                }
                if (domain.isNotBlank()) {
                    Button(onClick = { onOpenAuth(domain) }, modifier = Modifier.fillMaxWidth().focusable()) {
                        Text("Login ${profile.platform}")
                    }
                }
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Results", style = MaterialTheme.typography.titleLarge)
            if (loading) {
                CircularProgressIndicator()
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(results) { result ->
                    ResultCard(result = result, onClick = { onOpenDetail(result) })
                }
            }
        }
    }
}

@Composable
private fun ResultCard(result: SearchResult, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .focusable(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(result.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            result.subtitle?.takeIf(String::isNotBlank)?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }
            Text(result.providerId, style = MaterialTheme.typography.labelMedium)
        }
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

    LaunchedEffect(result) {
        loading = true
        error = null
        runCatching { graph.sourceRegistry.loadDetail(result) }
            .onSuccess { detail = it }
            .onFailure { error = it.message }
        loading = false
    }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack, modifier = Modifier.focusable()) { Text("Back") }
            Text(result.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        if (loading) {
            CircularProgressIndicator()
        }
        error?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error) }
        detail?.let { media ->
            Text(media.summary.orEmpty(), style = MaterialTheme.typography.bodyLarge)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(media.episodes) { episode ->
                    Card(
                        modifier = Modifier.fillMaxWidth().focusable(),
                        onClick = {
                            scope.launch {
                                val streams = graph.sourceRegistry.resolveStreams(episode)
                                streams.firstOrNull()?.let { stream -> onPlay(media, episode, stream) }
                            }
                        },
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(episode.title, style = MaterialTheme.typography.titleMedium)
                            Text(episode.url, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
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

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
                .padding(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onBack, modifier = Modifier.focusable()) { Text("Back") }
                Button(onClick = { danmakuEnabled = !danmakuEnabled }, modifier = Modifier.focusable()) {
                    Text(if (danmakuEnabled) "Danmaku on" else "Danmaku off")
                }
                Button(
                    onClick = {
                        if (stream.protocol != StreamProtocol.BITTORRENT) {
                            graph.media3DownloadCoordinator.enqueue(stream, "${detail.title} ${episode.title}")
                        }
                    },
                    modifier = Modifier.focusable(),
                ) {
                    Text(if (stream.protocol == StreamProtocol.BITTORRENT) "BT cache" else "Cache")
                }
                Text(stream.quality.orEmpty(), maxLines = 1)
                val errorMessage = if (stream.protocol == StreamProtocol.BITTORRENT) {
                    torrentState.errorMessage
                } else {
                    state.errorMessage
                }
                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, maxLines = 1) }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Density", modifier = Modifier.width(80.dp))
                Slider(
                    value = density,
                    onValueChange = { density = it },
                    valueRange = 0.2f..1.5f,
                    modifier = Modifier.width(220.dp).focusable(),
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
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.width(560.dp).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text("BitTorrent stream", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Resolving torrent metadata, selecting the best video file, and buffering the opening pieces for Media3 playback.",
                style = MaterialTheme.typography.bodyLarge,
            )
            LinearProgressIndicator(
                progress = { (state.plan?.bufferingPercent ?: 0f) / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Status: ${state.status ?: "waiting"}")
            Text("Torrent: ${formatPercent(state.progressPercent)}  File: ${formatPercent(state.selectedFileProgressPercent)}  Buffer: ${formatPercent(state.plan?.bufferingPercent ?: 0f)}")
            Text("Peers: ${state.connectedPeers}  Seeds: ${state.connectedSeeds}  Down: ${formatBytesPerSecond(state.downloadRateBytesPerSecond)}")
            state.plan?.selectedFileName?.let { name ->
                Text("Selected: $name", style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            state.plan?.selectedFileSizeBytes?.let { size ->
                Text("Size: ${formatBytes(size)}", style = MaterialTheme.typography.bodySmall)
            }
            Text(stream.url, style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
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

@Composable
private fun AuthScreen(domain: String, onBack: () -> Unit) {
    val configuration = LocalConfiguration.current
    val loginUrl = remember(domain) { "https://$domain" }
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onBack, modifier = Modifier.focusable()) { Text("Back") }
            Text("Auth session: $domain", style = MaterialTheme.typography.titleMedium)
        }
        val webModifier = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Modifier.fillMaxSize()
        } else {
            Modifier.fillMaxSize()
        }
        AuthWebView(url = loginUrl, modifier = webModifier)
    }
}
