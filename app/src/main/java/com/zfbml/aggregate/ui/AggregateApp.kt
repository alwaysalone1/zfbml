package com.zfbml.aggregate.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.zfbml.aggregate.source.SearchResult
import com.zfbml.aggregate.source.StreamProtocol
import com.zfbml.aggregate.torrent.TorrentEngineState
import kotlinx.coroutines.launch

@Composable
fun AggregateApp(graph: AppGraph) {
    var screen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
    Surface(modifier = Modifier.fillMaxSize(), color = AnimeBackground) {
        when (val current = screen) {
            AppScreen.Home -> HomeScreen(
                graph = graph,
                onOpenDetail = { screen = AppScreen.Detail(it) },
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
        }
    }
}

private sealed interface AppScreen {
    data object Home : AppScreen
    data class Detail(val result: SearchResult) : AppScreen
    data class Player(val detail: MediaDetail, val episode: Episode, val stream: MediaStream) : AppScreen
}

private val AnimeBackground = Color(0xFF10131A)
private val AnimePanel = Color(0xFF1A1F2B)
private val AnimeBorder = Color(0xFF2A3142)
private val AnimeMuted = Color(0xFFAAB3C5)
private val AnimeAccentPink = Color(0xFFE85D8E)
private val AnimeAccentCyan = Color(0xFF41BFD6)
private val AnimeAccentAmber = Color(0xFFE7B84A)
private val AnimeAccentViolet = Color(0xFF8A7CF6)

@Composable
private fun HomeScreen(
    graph: AppGraph,
    onOpenDetail: (SearchResult) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("\u846C\u9001\u7684\u8299\u8389\u83B2") }
    var results by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    val providers = graph.sourceRegistry.manifests

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(AnimeBackground)
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(
            modifier = Modifier
                .width(340.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "\u005A\u0046\u0042\u004D\u004C \u756A\u5267\u805A\u5408",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = "\u0042\u0054 \u4E3B\u7EBF / \u5728\u7EBF\u6E90\u8865\u5145 / \u5F39\u5E55\u76F4\u6293",
                style = MaterialTheme.typography.bodyMedium,
                color = AnimeMuted,
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("\u641C\u7D22\u756A\u540D\u6216\u7C98\u8D34 magnet / \u76F4\u94FE") },
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusable(),
                colors = ButtonDefaults.buttonColors(containerColor = AnimeAccentPink, contentColor = Color.White),
                onClick = {
                    scope.launch {
                        loading = true
                        results = graph.sourceRegistry.searchAll(query)
                        loading = false
                    }
                },
            ) {
                Text("\u5168\u6E90\u641C\u7D22")
            }
            Text("\u64AD\u653E\u7EBF\u8DEF", style = MaterialTheme.typography.titleMedium, color = Color.White)
            providers.forEach { manifest ->
                SourceRailItem(name = manifest.name, version = manifest.version)
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimeHeroStrip()
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("\u756A\u5267\u5217\u8868", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Text("\u641C\u7D22\u7ED3\u679C\u4F1A\u51FA\u73B0\u5728\u8FD9\u91CC", style = MaterialTheme.typography.bodyMedium, color = AnimeMuted)
            }
            if (loading) {
                CircularProgressIndicator(color = AnimeAccentCyan)
            }
            if (results.isEmpty() && !loading) {
                AnimeFeatureShelf()
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
        HeroTile("BT", "\u78C1\u529B\u4E3B\u7EBF", AnimeAccentPink, Modifier.weight(1f))
        HeroTile("\u5F39\u5E55", "\u65E0\u767B\u5F55\u76F4\u6293", AnimeAccentCyan, Modifier.weight(1f))
        HeroTile("TV", "\u9065\u63A7\u4F53\u9A8C", AnimeAccentAmber, Modifier.weight(1f))
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
        items(
            listOf(
                SearchResult("bt", "\u7C98\u8D34 magnet \u5F00\u59CB\u6D4B\u8BD5", "magnet:", subtitle = "BT \u4E3B\u7EBF\u8DEF"),
                SearchResult("direct", "\u7C98\u8D34\u89C6\u9891\u76F4\u94FE", "https://", subtitle = "\u5728\u7EBF\u64AD\u653E"),
                SearchResult("demo", "Demo \u756A\u5267\u6837\u4F8B", "demo://sample", subtitle = "\u5185\u7F6E\u6E90"),
            ),
        ) { item ->
            ResultCard(result = item, onClick = {})
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
        colors = CardDefaults.cardColors(containerColor = AnimePanel),
        border = BorderStroke(1.dp, AnimeBorder),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(58.dp)
                    .height(78.dp)
                    .background(providerAccent(result.providerId)),
                contentAlignment = Alignment.Center,
            ) {
                Text(result.providerId.take(2).uppercase(), style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(result.title, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                result.subtitle?.takeIf(String::isNotBlank)?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = AnimeMuted, maxLines = 1)
                }
                Text(result.url, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

private fun providerAccent(providerId: String): Color {
    return when (providerId.lowercase()) {
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

    LaunchedEffect(result) {
        loading = true
        error = null
        runCatching { graph.sourceRegistry.loadDetail(result) }
            .onSuccess { detail = it }
            .onFailure { error = it.message }
        loading = false
    }

    Column(Modifier.fillMaxSize().background(AnimeBackground).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack, modifier = Modifier.focusable()) { Text("\u8FD4\u56DE") }
            Text(result.title, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
        }
        if (loading) {
            CircularProgressIndicator(color = AnimeAccentCyan)
        }
        error?.let { Text("\u9519\u8BEF: $it", color = MaterialTheme.colorScheme.error) }
        detail?.let { media ->
            Text(media.summary.orEmpty(), style = MaterialTheme.typography.bodyLarge, color = AnimeMuted)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(media.episodes) { episode ->
                    Card(
                        modifier = Modifier.fillMaxWidth().focusable(),
                        colors = CardDefaults.cardColors(containerColor = AnimePanel),
                        border = BorderStroke(1.dp, AnimeBorder),
                        onClick = {
                            scope.launch {
                                val streams = graph.sourceRegistry.resolveStreams(episode)
                                streams.firstOrNull()?.let { stream -> onPlay(media, episode, stream) }
                            }
                        },
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(episode.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(episode.url, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onBack, modifier = Modifier.focusable()) { Text("\u8FD4\u56DE") }
                Button(onClick = { danmakuEnabled = !danmakuEnabled }, modifier = Modifier.focusable()) {
                    Text(if (danmakuEnabled) "\u5F39\u5E55\u5F00" else "\u5F39\u5E55\u5173")
                }
                Button(
                    onClick = {
                        if (stream.protocol != StreamProtocol.BITTORRENT) {
                            graph.media3DownloadCoordinator.enqueue(stream, "${detail.title} ${episode.title}")
                        }
                    },
                    modifier = Modifier.focusable(),
                ) {
                    Text(if (stream.protocol == StreamProtocol.BITTORRENT) "BT \u7F13\u5B58" else "\u7F13\u5B58")
                }
                Text(stream.quality.orEmpty(), color = Color.White, maxLines = 1)
                val errorMessage = if (stream.protocol == StreamProtocol.BITTORRENT) {
                    torrentState.errorMessage
                } else {
                    state.errorMessage
                }
                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, maxLines = 1) }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("\u5F39\u5E55\u5BC6\u5EA6", color = Color.White, modifier = Modifier.width(80.dp))
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
        modifier = modifier.background(AnimeBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.width(560.dp).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text("BT \u64AD\u653E\u7EBF\u8DEF", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Text(
                "\u6B63\u5728\u89E3\u6790\u79CD\u5B50\u5143\u6570\u636E\u3001\u9009\u62E9\u89C6\u9891\u6587\u4EF6\uFF0C\u5E76\u4E3A\u64AD\u653E\u5668\u9884\u7F13\u51B2\u5F00\u5934\u5206\u7247\u3002",
                style = MaterialTheme.typography.bodyLarge,
                color = AnimeMuted,
            )
            LinearProgressIndicator(
                progress = { (state.plan?.bufferingPercent ?: 0f) / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            Text("\u72B6\u6001: ${state.status ?: "\u7B49\u5F85\u4E2D"}", color = Color.White)
            Text(
                "\u5143\u6570\u636E: ${if (state.hasMetadata) "\u5DF2\u83B7\u53D6" else "\u7B49\u5F85"}  \u64AD\u653E\u4EE3\u7406: ${if (state.plan?.localPlaybackUrl != null) "\u5DF2\u5EFA\u7ACB" else "\u7B49\u5F85"}",
                color = Color.White,
            )
            Text("\u79CD\u5B50: ${formatPercent(state.progressPercent)}  \u6587\u4EF6: ${formatPercent(state.selectedFileProgressPercent)}  \u7F13\u51B2: ${formatPercent(state.plan?.bufferingPercent ?: 0f)}", color = Color.White)
            Text("\u8FDE\u63A5: ${state.connectedPeers}  \u505A\u79CD: ${state.connectedSeeds}  \u4E0B\u8F7D: ${formatBytesPerSecond(state.downloadRateBytesPerSecond)}", color = Color.White)
            state.plan?.selectedFileName?.let { name ->
                Text("\u6587\u4EF6: $name", style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            state.plan?.selectedFileSizeBytes?.let { size ->
                Text("\u5927\u5C0F: ${formatBytes(size)}", style = MaterialTheme.typography.bodySmall, color = AnimeMuted)
            }
            Text(stream.url, style = MaterialTheme.typography.bodySmall, color = AnimeMuted, maxLines = 3, overflow = TextOverflow.Ellipsis)
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
