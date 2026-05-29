package com.zfbml.aggregate.source.online

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.zfbml.aggregate.source.DownloadPolicy
import com.zfbml.aggregate.source.Episode
import com.zfbml.aggregate.source.MediaDetail
import com.zfbml.aggregate.source.MediaStream
import com.zfbml.aggregate.source.SearchResult
import com.zfbml.aggregate.source.SourceCapability
import com.zfbml.aggregate.source.SourceManifest
import com.zfbml.aggregate.source.SourceProvider
import com.zfbml.aggregate.source.StreamProtocol
import com.zfbml.aggregate.torrent.TorrentTitleScorer
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class AnimekoOnlineSourceProvider(
    context: Context,
    private val client: OkHttpClient,
) : SourceProvider {
    private val appContext = context.applicationContext
    private val onlineClient = client.newBuilder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .callTimeout(7, TimeUnit.SECONDS)
        .build()
    private val sniffer = AndroidWebVideoSniffer(appContext)
    private val configMutex = Mutex()
    private var cachedConfigs: List<OnlineSelectorConfig>? = null

    override val manifest = SourceManifest(
        id = ID,
        name = "Animeko Online Sources",
        version = "0.1.0",
        author = "zfbml",
        capabilities = setOf(
            SourceCapability.SEARCH,
            SourceCapability.DETAIL,
            SourceCapability.EPISODES,
            SourceCapability.STREAM,
            SourceCapability.WEBVIEW_SNIFF,
            SourceCapability.DOWNLOAD,
        ),
        domains = setOf("sub.creamycake.org"),
        requiresWebView = true,
        supportsDownload = true,
    )

    override suspend fun search(query: String): List<SearchResult> {
        val keyword = query.trim()
        if (
            keyword.isBlank() ||
            keyword.startsWith("magnet:", ignoreCase = true) ||
            keyword.startsWith("http://", ignoreCase = true) ||
            keyword.startsWith("https://", ignoreCase = true)
        ) {
            return emptyList()
        }

        val activeConfigs = configs().take(MAX_ACTIVE_SOURCES)
        Log.i(TAG, "search keyword=$keyword onlineSources=${activeConfigs.size}")
        return coroutineScope {
            activeConfigs.map { config ->
                async {
                    withTimeoutOrNull(SOURCE_SEARCH_TIMEOUT_MS) {
                        runCatching { searchSource(config, keyword) }
                            .onSuccess { Log.i(TAG, "search source=${config.name} hits=${it.size}") }
                            .onFailure { Log.w(TAG, "search failed source=${config.name}", it) }
                            .getOrDefault(emptyList())
                    }.orEmpty()
                }
            }.awaitAll()
        }
            .flatten()
            .distinctBy { "${it.raw[RAW_SOURCE_ID]}|${it.url}" }
            .sortedWith(compareByDescending<SearchResult> { it.onlineScore(keyword) }.thenBy { it.title })
            .take(MAX_SEARCH_RESULTS)
    }

    override suspend fun loadDetail(result: SearchResult): MediaDetail {
        val config = configFor(result.raw[RAW_SOURCE_ID])
        Log.i(TAG, "detail start source=${config.name} title=${result.title} url=${result.url}")
        val document = fetchDocument(config, result.url, referer = config.baseUrl)
        val episodes = extractEpisodes(config, result, document)
        Log.i(TAG, "detail source=${config.name} title=${result.title} episodes=${episodes.size}")
        return MediaDetail(
            providerId = manifest.id,
            title = document.selectFirst("h1")?.text()?.trim().orEmpty().ifBlank { result.title },
            url = result.url,
            summary = document.selectFirst("meta[name=description]")?.attr("content")?.trim(),
            posterUrl = result.posterUrl ?: document.selectFirst("img")?.absUrl("src")?.takeIf(String::isNotBlank),
            episodes = episodes.ifEmpty {
                listOf(
                    Episode(
                        providerId = manifest.id,
                        id = "${result.url}:single".hashCode().toString(),
                        title = result.title,
                        url = result.url,
                        index = 1,
                        raw = result.raw,
                    ),
                )
            },
        )
    }

    override suspend fun resolveStreams(episode: Episode): List<MediaStream> {
        val config = configFor(episode.raw[RAW_SOURCE_ID])
        Log.i(TAG, "resolve start source=${config.name} episode=${episode.title} url=${episode.url}")
        val pageText = fetchText(config, episode.url, referer = config.baseUrl)
        val directUrl = resolveVideoUrl(config, pageText, episode.url, depth = 0)
            ?: sniffer.sniff(
                playUrl = episode.url,
                matchVideoPattern = config.matchVideoUrl,
                matchNestedPattern = config.matchNestedUrl.takeIf { config.enableNestedUrl },
                headers = requestHeaders(config, referer = episode.url, videoHeaders = false),
                timeoutMs = WEBVIEW_SNIFF_TIMEOUT_MS,
            )

        val videoUrl = directUrl?.normalizeUrl(episode.url)
        if (videoUrl == null) {
            Log.w(TAG, "resolve empty source=${config.name} episode=${episode.title} page=${episode.url}")
            return emptyList()
        }
        val protocol = inferProtocol(videoUrl)
        if (!isPlayableStreamUrl(config, episode.url, videoUrl, protocol)) {
            Log.w(TAG, "resolved but unavailable source=${config.name} episode=${episode.title} url=${videoUrl.take(160)}")
            return emptyList()
        }
        Log.i(TAG, "resolved source=${config.name} episode=${episode.title} protocol=$protocol url=${videoUrl.take(160)}")
        return listOf(
            MediaStream(
                id = "${episode.id}:${videoUrl.hashCode()}",
                providerId = config.id,
                url = videoUrl,
                protocol = protocol,
                quality = config.defaultResolution.ifBlank { "auto" },
                codec = null,
                headers = requestHeaders(config, referer = episode.url, videoHeaders = true),
                downloadPolicy = if (protocol == StreamProtocol.WEBVIEW_ONLY) {
                    DownloadPolicy.BlockedWebViewOnly
                } else {
                    DownloadPolicy.Allowed
                },
                sourceScore = (ONLINE_STREAM_BASE_SCORE - config.tier * 12).coerceAtLeast(120),
                metadata = episode.raw + mapOf(
                    "mediaKind" to "online",
                    "routeProviderId" to config.id,
                    "routeProviderName" to config.name,
                    "routeSubtitle" to listOfNotNull(
                        episode.raw[RAW_CHANNEL_NAME]?.takeIf(String::isNotBlank),
                        config.defaultResolution.takeIf(String::isNotBlank),
                    ).joinToString(" / "),
                    "sourceName" to config.name,
                    "sourceTier" to config.tier.toString(),
                ),
            ),
        )
    }

    private suspend fun isPlayableStreamUrl(
        config: OnlineSelectorConfig,
        referer: String,
        videoUrl: String,
        protocol: StreamProtocol,
    ): Boolean = withContext(Dispatchers.IO) {
        val requestBuilder = Request.Builder().url(videoUrl)
        requestHeaders(config, referer, videoHeaders = true).forEach { (key, value) ->
            requestBuilder.header(key, value)
        }
        if (protocol == StreamProtocol.PROGRESSIVE) {
            requestBuilder.header("Range", "bytes=0-0")
        }
        runCatching {
            onlineClient.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "stream check failed status=${response.code} url=${videoUrl.take(160)}")
                    return@use false
                }
                if (protocol == StreamProtocol.HLS) {
                    val body = response.body?.string().orEmpty()
                    body.contains("#EXTM3U") ||
                        body.contains("#EXT-X") ||
                        body.contains(".ts", ignoreCase = true)
                } else {
                    true
                }
            }
        }.getOrElse { error ->
            Log.w(TAG, "stream check failed url=${videoUrl.take(160)}", error)
            false
        }
    }

    private suspend fun configs(): List<OnlineSelectorConfig> {
        cachedConfigs?.let { return it }
        return configMutex.withLock {
            cachedConfigs?.let { return@withLock it }
            val loaded = runCatching { loadSubscriptionConfigs() }
                .getOrDefault(emptyList())
                .ifEmpty { fallbackConfigs() }
            cachedConfigs = loaded
            loaded
        }
    }

    private suspend fun configFor(sourceId: String?): OnlineSelectorConfig {
        val all = configs()
        return all.firstOrNull { it.id == sourceId }
            ?: all.firstOrNull()
            ?: error("No online source config is available")
    }

    private suspend fun loadSubscriptionConfigs(): List<OnlineSelectorConfig> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(DEFAULT_SUBSCRIPTION_URL)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .build()
        onlineClient.newCall(request).execute().use { response ->
            require(response.isSuccessful) { "HTTP ${response.code} for Animeko online subscription" }
            val body = response.body?.string().orEmpty()
            val subscription = json.decodeFromString<AnimekoSubscription>(body)
            subscription.sources()
                .filter { it.factoryId == "web-selector" }
                .mapIndexedNotNull { index, item -> item.arguments?.toConfig(index) }
                .sortedWith(compareBy<OnlineSelectorConfig> { it.tier }.thenBy { it.name })
        }
    }

    private suspend fun searchSource(config: OnlineSelectorConfig, rawKeyword: String): List<SearchResult> {
        val keyword = config.searchKeyword(rawKeyword)
        if (keyword.isBlank()) return emptyList()
        val searchUrl = config.searchUrl.replace("{keyword}", keyword.urlEncode())
        val document = fetchDocument(config, searchUrl, referer = config.baseUrl)
        return extractSubjects(config, document)
            .filter { result -> titleMatchesQuery(result.title, keyword) }
    }

    private fun extractSubjects(config: OnlineSelectorConfig, document: Document): List<SearchResult> {
        val subjects = when (config.subjectFormatId) {
            "indexed" -> {
                val names = document.select(config.selectNames)
                val links = document.select(config.selectLinks)
                links.mapIndexedNotNull { index, link ->
                    val nameElement = names.getOrNull(index)
                    val title = nameElement?.readableTitle().orEmpty().ifBlank { link.readableTitle() }
                    val url = link.readableHref()
                    if (title.isBlank() || url.isBlank()) null else subjectResult(config, title, url, link)
                }
            }
            else -> document.select(config.selectLists).mapNotNull { element ->
                val link = element.asLinkElement()
                val title = element.readableTitle().ifBlank { link?.readableTitle().orEmpty() }
                val url = link?.readableHref().orEmpty()
                if (title.isBlank() || url.isBlank()) null else subjectResult(config, title, url, element)
            }
        }
        return subjects.distinctBy { it.url }
    }

    private fun subjectResult(
        config: OnlineSelectorConfig,
        title: String,
        url: String,
        element: Element,
    ): SearchResult {
        return SearchResult(
            providerId = manifest.id,
            title = title,
            url = url.normalizeUrl(config.baseUrl),
            posterUrl = element.selectFirst("img")?.bestImageUrl()?.normalizeUrl(config.baseUrl),
            subtitle = "${config.name} / \u5728\u7ebf\u6e90",
            raw = mapOf(
                RAW_SOURCE_ID to config.id,
                RAW_SOURCE_NAME to config.name,
                "mediaKind" to "online",
                "sourceTier" to config.tier.toString(),
            ),
        )
    }

    private fun extractEpisodes(
        config: OnlineSelectorConfig,
        result: SearchResult,
        document: Document,
    ): List<Episode> {
        val grouped = extractGroupedEpisodes(config, result, document)
        if (grouped.isNotEmpty()) return grouped
        return extractFlatEpisodes(config, result, document)
    }

    private fun extractGroupedEpisodes(
        config: OnlineSelectorConfig,
        result: SearchResult,
        document: Document,
    ): List<Episode> {
        if (config.selectEpisodeLists.isBlank() || config.selectEpisodesFromList.isBlank()) return emptyList()
        val channelNames = document.select(config.selectChannelNames)
        val episodeLists = document.select(config.selectEpisodeLists)
        val episodes = mutableListOf<Episode>()
        episodeLists.forEachIndexed { listIndex, listElement ->
            val channelName = channelNames.getOrNull(listIndex)?.text()?.trim().orEmpty()
            if (!config.channelMatches(channelName)) return@forEachIndexed
            val episodeElements = listElement.select(config.selectEpisodesFromList)
            episodeElements.forEach { episodeElement ->
                val link = episodeElement.episodeLink(config.selectEpisodeLinksFromList)
                val href = link?.readableHref().orEmpty()
                val title = episodeElement.readableTitle().ifBlank { link?.readableTitle().orEmpty() }
                if (href.isBlank() || title.isBlank()) return@forEach
                episodes += episodeFrom(config, result, href, title, channelName, episodes.size + 1)
            }
        }
        return episodes.distinctBy { "${it.raw[RAW_CHANNEL_NAME]}|${it.url}" }
    }

    private fun extractFlatEpisodes(
        config: OnlineSelectorConfig,
        result: SearchResult,
        document: Document,
    ): List<Episode> {
        if (config.selectEpisodes.isBlank()) return emptyList()
        return document.select(config.selectEpisodes).mapIndexedNotNull { index, episodeElement ->
            val link = episodeElement.episodeLink(config.selectEpisodeLinks)
            val href = link?.readableHref().orEmpty()
            val title = episodeElement.readableTitle().ifBlank { link?.readableTitle().orEmpty() }
            if (href.isBlank() || title.isBlank()) {
                null
            } else {
                episodeFrom(config, result, href, title, "", index + 1)
            }
        }.distinctBy { it.url }
    }

    private fun episodeFrom(
        config: OnlineSelectorConfig,
        result: SearchResult,
        href: String,
        title: String,
        channelName: String,
        fallbackIndex: Int,
    ): Episode {
        val episodeIndex = parseEpisodeIndex(title, config.matchEpisodeSortFromName)
            ?: TorrentTitleScorer.extractEpisode(title)
            ?: fallbackIndex
        return Episode(
            providerId = manifest.id,
            id = "${config.id}:${href.hashCode()}:$episodeIndex",
            title = title,
            url = href.normalizeUrl(result.url),
            index = episodeIndex,
            raw = result.raw + mapOf(
                RAW_CHANNEL_NAME to channelName,
                "episodeTitle" to title,
                "episodeIndex" to episodeIndex.toString(),
            ),
        )
    }

    private suspend fun resolveVideoUrl(
        config: OnlineSelectorConfig,
        pageText: String,
        baseUrl: String,
        depth: Int,
    ): String? {
        val decoded = pageText.decodeHtmlPayload()
        AnimekoOnlineExtractors.extractMacCmsPlayerUrl(decoded)?.let { playerUrl ->
            if (playerUrl.looksLikePlayableVideo()) return playerUrl.normalizeUrl(baseUrl)
        }
        AnimekoOnlineExtractors.findVideoByConfiguredRegex(config.matchVideoUrl, decoded)
            ?.let { return it.normalizeUrl(baseUrl) }
        AnimekoOnlineExtractors.findGenericVideoUrl(decoded)?.let { return it.normalizeUrl(baseUrl) }

        if (depth >= MAX_NESTED_DEPTH || !config.enableNestedUrl) return null
        val nestedUrls = Jsoup.parse(decoded, baseUrl)
            .select("iframe[src], script[src], a[href]")
            .mapNotNull { element ->
                element.absUrl("src").ifBlank { element.absUrl("href") }.takeIf(String::isNotBlank)
            }
            .filter { url -> config.nestedMatches(url) && !url.contains(".ts", ignoreCase = true) }
            .distinct()
            .take(MAX_NESTED_CANDIDATES)

        for (nestedUrl in nestedUrls) {
            val nestedText = runCatching { fetchText(config, nestedUrl, referer = baseUrl) }.getOrNull() ?: continue
            resolveVideoUrl(config, nestedText, nestedUrl, depth + 1)?.let { return it }
        }
        return null
    }

    private suspend fun fetchDocument(config: OnlineSelectorConfig, url: String, referer: String?): Document {
        val text = fetchText(config, url, referer)
        return Jsoup.parse(text.decodeHtmlPayload(), url)
    }

    private suspend fun fetchText(config: OnlineSelectorConfig, url: String, referer: String?): String = withContext(Dispatchers.IO) {
        val requestBuilder = Request.Builder().url(url)
        requestHeaders(config, referer, videoHeaders = false).forEach { (key, value) ->
            requestBuilder.header(key, value)
        }
        onlineClient.newCall(requestBuilder.build()).execute().use { response ->
            require(response.isSuccessful) { "HTTP ${response.code} for $url" }
            response.body?.string().orEmpty()
        }
    }

    private fun requestHeaders(
        config: OnlineSelectorConfig,
        referer: String?,
        videoHeaders: Boolean,
    ): Map<String, String> {
        return buildMap {
            put("User-Agent", config.userAgent.ifBlank { DEFAULT_BROWSER_UA })
            val refererHeader = if (videoHeaders) {
                config.videoReferer.ifBlank { referer.orEmpty() }
            } else {
                referer.orEmpty()
            }
            if (refererHeader.isNotBlank()) put("Referer", refererHeader)
            val cookie = config.cookies.trim()
            if (cookie.isNotBlank()) put("Cookie", cookie)
            if (videoHeaders && config.videoUserAgent.isNotBlank()) put("User-Agent", config.videoUserAgent)
        }
    }

    private fun SearchResult.onlineScore(query: String): Int {
        var score = 220
        val lower = title.lowercase()
        if (lower.contains(query.lowercase())) score += 80
        raw["sourceTier"]?.toIntOrNull()?.let { score -= it * 8 }
        return score
    }

    private fun fallbackConfigs(): List<OnlineSelectorConfig> {
        return listOf(
            OnlineSelectorConfig(
                id = "online-omofun111",
                name = "omofun111",
                searchUrl = "https://enlienli.link/vod/search.html?wd={keyword}",
                baseUrl = "https://enlienli.link/",
                searchUseOnlyFirstWord = true,
                subjectFormatId = "a",
                selectLists = ".module-card-item>.module-card-item-info>.module-card-item-title>a",
                channelFormatId = "index-grouped",
                selectChannelNames = "div>div>div>div>div>div.module-tab-items-box>.module-tab-item>span",
                matchChannelName = "^(?!.*\u9ad8\u6e05\u7ebf\u8def3).*$",
                selectEpisodeLists = ".module-play-list-content",
                selectEpisodesFromList = "a",
                matchEpisodeSortFromName = "\\u7b2c\\s*(?<ep>.+)\\s*[\\u8bdd\\u96c6]",
                defaultResolution = "1080P",
                matchVideoUrl = DEFAULT_VIDEO_REGEX,
                tier = 0,
            ),
            OnlineSelectorConfig(
                id = "online-girigiri",
                name = "girigiri",
                searchUrl = "https://bgm.girigirilove.com/search/-------------/?wd={keyword}",
                baseUrl = "https://bgm.girigirilove.com/",
                searchUseOnlyFirstWord = true,
                searchRemoveSpecial = true,
                subjectFormatId = "indexed",
                selectNames = "body > .box-width .vod-detail .detail-info .slide-info-title",
                selectLinks = "body > .box-width .vod-detail .detail-info > a",
                channelFormatId = "index-grouped",
                selectChannelNames = ".anthology-tab > .swiper-wrapper a",
                matchChannelName = "(?<ch>.+?)(\\d+?)",
                selectEpisodeLists = ".anthology-list-box",
                selectEpisodesFromList = "a",
                matchEpisodeSortFromName = "\\u7b2c\\s*(?<ep>.+)\\s*[\\u8bdd\\u96c6]",
                defaultResolution = "1080P",
                enableNestedUrl = true,
                matchNestedUrl = "^.+(vip|index\\.php).+\\?",
                matchVideoUrl = "(^http(s)?://(?!.*http(s)?://).+((m3u8)).*(\\?.+)?)|(akamaized)|(bilivideo.com)|(url=(?<v>.+playlist.m3u8))",
                cookies = "quality=1080P",
                videoUserAgent = DEFAULT_BROWSER_UA,
                tier = 0,
            ),
        )
    }

    private companion object {
        const val ID = "animeko-online"
        const val TAG = "ZfbmlOnlineSource"
        const val DEFAULT_SUBSCRIPTION_URL = "https://sub.creamycake.org/v1/css1.json"
        const val USER_AGENT = "ZFBML/0.2.15 (https://github.com/alwaysalone1/zfbml)"
        const val DEFAULT_BROWSER_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36"
        const val RAW_SOURCE_ID = "onlineSourceId"
        const val RAW_SOURCE_NAME = "onlineSourceName"
        const val RAW_CHANNEL_NAME = "onlineChannelName"
        const val SOURCE_SEARCH_TIMEOUT_MS = 4_500L
        const val WEBVIEW_SNIFF_TIMEOUT_MS = 12_000L
        const val MAX_ACTIVE_SOURCES = 8
        const val MAX_SEARCH_RESULTS = 30
        const val MAX_NESTED_DEPTH = 2
        const val MAX_NESTED_CANDIDATES = 4
        const val ONLINE_STREAM_BASE_SCORE = 260
        const val DEFAULT_VIDEO_REGEX =
            "(^http(s)?://(?!.*http).+\\.(mp4|m3u8|flv|mkv))|(url=(?<v>http(s)?://.+\\.(mp4|m3u8|flv|mkv)))|((bilivideo|akamaized|szbdyd)\\.com(?!.*\\.ts))"
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }
    }
}

internal object AnimekoOnlineExtractors {
    fun extractMacCmsPlayerUrl(text: String): String? {
        extractMacCmsUrlField(text)?.let { return it }
        val match = Regex("""player_[a-zA-Z0-9_]+\s*=\s*(\{.*?})\s*(?:</script>|;)""", RegexOption.DOT_MATCHES_ALL)
            .find(text)
            ?: return null
        return runCatching {
            val obj = json.parseToJsonElement(match.groupValues[1]).jsonObject
            val encrypt = obj["encrypt"]?.jsonPrimitive?.intOrNull ?: 0
            val rawUrl = obj["url"]?.jsonPrimitive?.contentOrNull.orEmpty()
            when (encrypt) {
                1 -> rawUrl.urlDecode()
                2 -> String(android.util.Base64.decode(rawUrl, android.util.Base64.DEFAULT), Charsets.UTF_8).urlDecode()
                else -> rawUrl
            }
        }.getOrNull()?.decodeWebEscapes()
    }

    fun findVideoByConfiguredRegex(matchVideoUrl: String, text: String): String? {
        if (matchVideoUrl.isBlank()) return null
        val regex = runCatching { Regex(matchVideoUrl, RegexOption.IGNORE_CASE) }.getOrNull() ?: return null
        return regex.findAll(text)
            .mapNotNull { match ->
                match.groups["v"]?.value
                    ?: match.value.substringAfter("url=", missingDelimiterValue = match.value)
            }
            .firstOrNull { it.looksLikePlayableVideo() }
    }

    fun findGenericVideoUrl(text: String): String? {
        return GENERIC_VIDEO_URL_REGEX.findAll(text)
            .map { it.value.decodeWebEscapes() }
            .firstOrNull { it.looksLikePlayableVideo() }
    }

    private fun extractMacCmsUrlField(text: String): String? {
        val match = Regex(
            """player_[a-zA-Z0-9_]+\s*=\s*\{.*?"encrypt"\s*:\s*(\d+).*?"url"\s*:\s*"([^"]*)"""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE),
        ).find(text) ?: return null
        val encrypt = match.groupValues.getOrNull(1)?.toIntOrNull() ?: 0
        val encodedUrl = match.groupValues.getOrNull(2).orEmpty().decodeJsonStringLiteral()
        return when (encrypt) {
            1 -> encodedUrl.urlDecode()
            2 -> runCatching {
                String(android.util.Base64.decode(encodedUrl, android.util.Base64.DEFAULT), Charsets.UTF_8).urlDecode()
            }.getOrDefault(encodedUrl)
            else -> encodedUrl
        }.decodeWebEscapes()
    }

    private val GENERIC_VIDEO_URL_REGEX = Regex(
        """https?:\\?/\\?/[^"'\s<>]+?(?:\.m3u8|\.mp4|\.mkv|\.flv)(?:\?[^"'\s<>]*)?""",
        setOf(RegexOption.IGNORE_CASE),
    )

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }
}

internal data class OnlineSelectorConfig(
    val id: String,
    val name: String,
    val searchUrl: String,
    val baseUrl: String,
    val searchUseOnlyFirstWord: Boolean = false,
    val searchRemoveSpecial: Boolean = false,
    val subjectFormatId: String = "a",
    val selectLists: String = "",
    val selectNames: String = "",
    val selectLinks: String = "",
    val channelFormatId: String = "index-grouped",
    val selectChannelNames: String = "",
    val matchChannelName: String = "",
    val selectEpisodeLists: String = "",
    val selectEpisodesFromList: String = "",
    val selectEpisodeLinksFromList: String = "",
    val selectEpisodes: String = "",
    val selectEpisodeLinks: String = "",
    val matchEpisodeSortFromName: String = "",
    val defaultResolution: String = "auto",
    val enableNestedUrl: Boolean = true,
    val matchNestedUrl: String = "",
    val matchVideoUrl: String = "",
    val cookies: String = "",
    val videoReferer: String = "",
    val videoUserAgent: String = "",
    val userAgent: String = "",
    val tier: Int = 4,
) {
    fun searchKeyword(raw: String): String {
        return normalizeOnlineSearchKeyword(raw, searchRemoveSpecial, searchUseOnlyFirstWord)
    }

    fun channelMatches(channelName: String): Boolean {
        if (matchChannelName.isBlank()) return true
        return runCatching { Regex(matchChannelName).containsMatchIn(channelName.ifBlank { "default" }) }
            .getOrDefault(true)
    }

    fun nestedMatches(url: String): Boolean {
        if (matchNestedUrl.isBlank()) return false
        return runCatching { Regex(matchNestedUrl, RegexOption.IGNORE_CASE).containsMatchIn(url) }
            .getOrDefault(false)
    }

    fun videoRegex(): Regex? {
        if (matchVideoUrl.isBlank()) return null
        return runCatching { Regex(matchVideoUrl, RegexOption.IGNORE_CASE) }.getOrNull()
    }
}

@Serializable
private data class AnimekoSubscription(
    @SerialName("exportedMediaSourceDataList") val exported: ExportedMediaSourceDataList? = null,
    val mediaSources: List<ExportedMediaSourceData> = emptyList(),
) {
    fun sources(): List<ExportedMediaSourceData> = exported?.mediaSources ?: mediaSources
}

@Serializable
private data class ExportedMediaSourceDataList(
    val mediaSources: List<ExportedMediaSourceData> = emptyList(),
)

@Serializable
private data class ExportedMediaSourceData(
    val factoryId: String = "",
    val version: Int = 0,
    val arguments: AnimekoSourceArguments? = null,
)

@Serializable
private data class AnimekoSourceArguments(
    val name: String = "",
    val searchConfig: AnimekoSearchConfig = AnimekoSearchConfig(),
    val tier: Int = 4,
) {
    fun toConfig(index: Int): OnlineSelectorConfig? {
        if (name.isBlank() || searchConfig.searchUrl.isBlank()) return null
        val base = searchConfig.rawBaseUrl.takeIf(String::isNotBlank)
            ?: searchConfig.searchUrl.origin()
        val sourceId = "online-" + name.slug().ifBlank { index.toString() }
        val grouped = searchConfig.selectorChannelFormatFlattened
        val flat = searchConfig.selectorChannelFormatNoChannel
        val subjectA = searchConfig.selectorSubjectFormatA
        val subjectIndexed = searchConfig.selectorSubjectFormatIndexed
        val videoHeaders = searchConfig.matchVideo.addHeadersToVideo
        return OnlineSelectorConfig(
            id = sourceId,
            name = name,
            searchUrl = searchConfig.searchUrl,
            baseUrl = base,
            searchUseOnlyFirstWord = searchConfig.searchUseOnlyFirstWord,
            searchRemoveSpecial = searchConfig.searchRemoveSpecial,
            subjectFormatId = searchConfig.subjectFormatId,
            selectLists = subjectA.selectLists,
            selectNames = subjectIndexed.selectNames,
            selectLinks = subjectIndexed.selectLinks,
            channelFormatId = searchConfig.channelFormatId,
            selectChannelNames = grouped.selectChannelNames,
            matchChannelName = grouped.matchChannelName,
            selectEpisodeLists = grouped.selectEpisodeLists,
            selectEpisodesFromList = grouped.selectEpisodesFromList,
            selectEpisodeLinksFromList = grouped.selectEpisodeLinksFromList,
            selectEpisodes = flat.selectEpisodes,
            selectEpisodeLinks = flat.selectEpisodeLinks,
            matchEpisodeSortFromName = grouped.matchEpisodeSortFromName.ifBlank { flat.matchEpisodeSortFromName },
            defaultResolution = searchConfig.defaultResolution,
            enableNestedUrl = searchConfig.matchVideo.enableNestedUrl,
            matchNestedUrl = searchConfig.matchVideo.matchNestedUrl,
            matchVideoUrl = searchConfig.matchVideo.matchVideoUrl,
            cookies = searchConfig.matchVideo.cookies,
            videoReferer = videoHeaders["referer"].orEmpty(),
            videoUserAgent = videoHeaders["userAgent"].orEmpty(),
            tier = tier,
        )
    }
}

@Serializable
private data class AnimekoSearchConfig(
    val searchUrl: String = "",
    val searchUseOnlyFirstWord: Boolean = false,
    val searchRemoveSpecial: Boolean = false,
    val rawBaseUrl: String = "",
    val subjectFormatId: String = "a",
    val selectorSubjectFormatA: SelectorSubjectFormatA = SelectorSubjectFormatA(),
    val selectorSubjectFormatIndexed: SelectorSubjectFormatIndexed = SelectorSubjectFormatIndexed(),
    val channelFormatId: String = "index-grouped",
    val selectorChannelFormatFlattened: SelectorChannelFormatFlattened = SelectorChannelFormatFlattened(),
    val selectorChannelFormatNoChannel: SelectorChannelFormatNoChannel = SelectorChannelFormatNoChannel(),
    val defaultResolution: String = "auto",
    val matchVideo: MatchVideoConfig = MatchVideoConfig(),
)

@Serializable
private data class SelectorSubjectFormatA(
    val selectLists: String = "",
)

@Serializable
private data class SelectorSubjectFormatIndexed(
    val selectNames: String = "",
    val selectLinks: String = "",
)

@Serializable
private data class SelectorChannelFormatFlattened(
    val selectChannelNames: String = "",
    val matchChannelName: String = "",
    val selectEpisodeLists: String = "",
    val selectEpisodesFromList: String = "",
    val selectEpisodeLinksFromList: String = "",
    val matchEpisodeSortFromName: String = "",
)

@Serializable
private data class SelectorChannelFormatNoChannel(
    val selectEpisodes: String = "",
    val selectEpisodeLinks: String = "",
    val matchEpisodeSortFromName: String = "",
)

@Serializable
private data class MatchVideoConfig(
    val enableNestedUrl: Boolean = true,
    val matchNestedUrl: String = "",
    val matchVideoUrl: String = "",
    val cookies: String = "",
    val addHeadersToVideo: Map<String, String> = emptyMap(),
)

private class AndroidWebVideoSniffer(
    private val context: Context,
) {
    @SuppressLint("SetJavaScriptEnabled")
    suspend fun sniff(
        playUrl: String,
        matchVideoPattern: String,
        matchNestedPattern: String?,
        headers: Map<String, String>,
        timeoutMs: Long,
    ): String? = withContext(Dispatchers.Main.immediate) {
        val videoRegex = runCatching { Regex(matchVideoPattern, RegexOption.IGNORE_CASE) }.getOrNull()
        val nestedRegex = matchNestedPattern?.let {
            runCatching { Regex(it, RegexOption.IGNORE_CASE) }.getOrNull()
        }
        if (videoRegex == null) return@withContext null

        val result = CompletableDeferred<String?>()
        val webView = WebView(context)
        fun inspect(url: String) {
            if (result.isCompleted) return
            val decoded = url.decodeWebEscapes()
            val match = videoRegex.find(decoded)
            if (match != null && decoded.looksLikePlayableVideo()) {
                result.complete(match.groups["v"]?.value ?: decoded)
                return
            }
            if (nestedRegex != null && nestedRegex.containsMatchIn(decoded) && !decoded.contains(".ts", ignoreCase = true)) {
                runCatching { webView.loadUrl(decoded, headers) }
            }
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                inspect(request.url.toString())
                return super.shouldInterceptRequest(view, request)
            }

            override fun onLoadResource(view: WebView, url: String) {
                inspect(url)
                super.onLoadResource(view, url)
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                inspect(request.url.toString())
                return false
            }
        }

        webView.loadUrl(playUrl, headers)
        val found = withTimeoutOrNull(timeoutMs) { result.await() }
        webView.stopLoading()
        webView.destroy()
        found
    }
}

private fun Element.asLinkElement(): Element? {
    return if (tagName().equals("a", ignoreCase = true)) this else selectFirst("a[href]")
}

private fun Element.episodeLink(selector: String): Element? {
    return when {
        selector.isNotBlank() -> selectFirst(selector)?.asLinkElement()
        tagName().equals("a", ignoreCase = true) -> this
        else -> selectFirst("a[href]")
    }
}

private fun Element.readableTitle(): String {
    return attr("title").ifBlank { attr("alt") }.ifBlank { text() }.trim()
}

private fun Element.readableHref(): String {
    return attr("href").ifBlank { absUrl("href") }.trim()
}

private fun Element.bestImageUrl(): String? {
    val attrs = listOf("data-original", "data-src", "data-lazy-src", "src")
    return attrs.firstNotNullOfOrNull { attr -> attr(attr).takeIf(String::isNotBlank) }
}

internal fun parseEpisodeIndex(title: String, pattern: String): Int? {
    if (pattern.isBlank()) return null
    return runCatching {
        val match = Regex(pattern).find(title) ?: return null
        val token = match.groups["ep"]?.value ?: match.groupValues.getOrNull(1).orEmpty()
        token.trim().toIntOrNull() ?: parseChineseEpisodeNumber(token)
    }.getOrNull()
}

private fun parseChineseEpisodeNumber(value: String): Int? {
    val chars = mapOf(
        '\u96f6' to 0,
        '\u4e00' to 1,
        '\u4e8c' to 2,
        '\u4e24' to 2,
        '\u4e09' to 3,
        '\u56db' to 4,
        '\u4e94' to 5,
        '\u516d' to 6,
        '\u4e03' to 7,
        '\u516b' to 8,
        '\u4e5d' to 9,
    )
    val trimmed = value.trim()
    if (trimmed.isBlank()) return null
    if (trimmed == "\u5341") return 10
    val tenIndex = trimmed.indexOf('\u5341')
    if (tenIndex >= 0) {
        val high = trimmed.take(tenIndex).lastOrNull()?.let(chars::get) ?: 1
        val low = trimmed.drop(tenIndex + 1).firstOrNull()?.let(chars::get) ?: 0
        return high * 10 + low
    }
    return trimmed.mapNotNull(chars::get).joinToString("").toIntOrNull()
}

private fun String.origin(): String {
    Regex("""^(https?://[^/?#]+/)""", RegexOption.IGNORE_CASE)
        .find(this)
        ?.groupValues
        ?.getOrNull(1)
        ?.let { return it }
    return runCatching {
        val uri = URI(this.replace("{keyword}", "keyword"))
        "${uri.scheme}://${uri.host}/"
    }.getOrDefault(this)
}

private fun String.slug(): String {
    return lowercase()
        .replace(Regex("""[^a-z0-9]+"""), "-")
        .trim('-')
        .take(40)
}

private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())

private fun String.urlDecode(): String = runCatching {
    URLDecoder.decode(this, Charsets.UTF_8.name())
}.getOrDefault(this)

private fun String.decodeJsonStringLiteral(): String {
    return runCatching { Json.decodeFromString<String>("\"$this\"") }
        .getOrDefault(this)
}

private fun String.decodeWebEscapes(): String {
    return replace("\\/", "/")
        .replace("&amp;", "&")
        .replace("\\u0026", "&")
        .replace("%3A", ":")
        .replace("%2F", "/")
        .replace("%3F", "?")
        .replace("%3D", "=")
        .replace("%26", "&")
}

private fun String.decodeHtmlPayload(): String {
    val trimmed = trim()
    val decodedString = if (
        (trimmed.startsWith("\"<") || trimmed.startsWith("\"<!")) &&
        trimmed.endsWith("\"")
    ) {
        runCatching { Json.decodeFromString<String>(trimmed) }.getOrDefault(this)
    } else {
        this
    }
    return decodedString.decodeWebEscapes()
}

internal fun normalizeOnlineSearchKeyword(
    raw: String,
    removeSpecial: Boolean,
    useOnlyFirstWord: Boolean,
): String {
    var value = raw.trim()
    if (removeSpecial) {
        value = value.replace(Regex("""[\[\]【】()（）:：!！?？.,，。、_\-]+"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }
    if (useOnlyFirstWord) {
        value = value.split(Regex("""[\s/|+]+""")).firstOrNull().orEmpty().ifBlank { value }
    }
    return value.take(32)
}

internal fun titleMatchesQuery(title: String, query: String): Boolean {
    val normalizedTitle = title.normalizeTitleForMatch()
    val normalizedQuery = query.normalizeTitleForMatch()
    if (normalizedTitle.isBlank() || normalizedQuery.isBlank()) return false
    if (normalizedTitle.contains(normalizedQuery) || normalizedQuery.contains(normalizedTitle)) return true
    val queryTokens = normalizedQuery.split(Regex("""\s+""")).filter { it.length >= 2 }
    if (queryTokens.isEmpty()) return false
    return queryTokens.count { normalizedTitle.contains(it) } >= (queryTokens.size.coerceAtMost(2))
}

private fun String.normalizeTitleForMatch(): String {
    return lowercase()
        .replace(Regex("""[\[\]【】()（）:：!！?？.,，。~～_-]+"""), " ")
        .replace(Regex("""\s+"""), " ")
        .trim()
}

private fun String.normalizeUrl(baseUrl: String): String {
    val decoded = trim().trim('"', '\'', ' ')
        .decodeWebEscapes()
        .substringBefore("#")
    return when {
        decoded.startsWith("//") -> "https:$decoded"
        decoded.startsWith("http://", ignoreCase = true) ||
            decoded.startsWith("https://", ignoreCase = true) -> decoded
        else -> runCatching { URI(baseUrl).resolve(decoded).toString() }.getOrDefault(decoded)
    }
}

private fun String.looksLikePlayableVideo(): Boolean {
    val lower = lowercase()
    return (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("//")) &&
        !lower.contains(".ts?") &&
        (
            ".m3u8" in lower ||
                ".mp4" in lower ||
                ".mkv" in lower ||
                ".flv" in lower ||
                "bilivideo.com" in lower ||
                "akamaized" in lower ||
                "mime_type=video" in lower
            )
}

private fun inferProtocol(url: String): StreamProtocol {
    val lower = url.lowercase()
    return when {
        ".m3u8" in lower -> StreamProtocol.HLS
        ".mpd" in lower -> StreamProtocol.DASH
        lower.startsWith("rtsp://") -> StreamProtocol.RTSP
        lower.startsWith("http://") || lower.startsWith("https://") -> StreamProtocol.PROGRESSIVE
        else -> StreamProtocol.UNKNOWN
    }
}
