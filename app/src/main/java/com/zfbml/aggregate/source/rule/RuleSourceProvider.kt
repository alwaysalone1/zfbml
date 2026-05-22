package com.zfbml.aggregate.source.rule

import com.zfbml.aggregate.source.DownloadPolicy
import com.zfbml.aggregate.source.Episode
import com.zfbml.aggregate.source.MediaDetail
import com.zfbml.aggregate.source.MediaStream
import com.zfbml.aggregate.source.SearchResult
import com.zfbml.aggregate.source.SourceManifest
import com.zfbml.aggregate.source.SourceProvider
import com.zfbml.aggregate.source.StreamProtocol
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup

class RuleSourceProvider(
    private val rule: RuleSource,
    private val client: OkHttpClient,
) : SourceProvider {
    override val manifest: SourceManifest = SourceManifest(
        id = rule.id,
        name = rule.name,
        version = rule.version,
        author = rule.author,
        capabilities = rule.capabilities,
        domains = rule.domains.ifEmpty { setOfNotNull(runCatching { URI(rule.baseUrl).host }.getOrNull()) },
        requiresWebView = rule.requiresWebView,
        supportsDownload = rule.supportsDownload,
    )

    override suspend fun search(query: String): List<SearchResult> {
        val search = requireNotNull(rule.search) { "Rule ${rule.id} has no search block" }
        val document = fetchDocument(
            path = search.path.replace("{query}", query.urlEncode()),
            method = search.method,
            bodyTemplate = search.bodyTemplate?.replace("{query}", query),
        )
        return SelectorEngine.select(document, search.itemSelector).mapNotNull { item ->
            val title = SelectorEngine.text(item, search.titleSelector)
            val url = SelectorEngine.attr(item, search.urlSelector, search.urlAttribute)
            if (title.isBlank() || url.isBlank()) {
                null
            } else {
                SearchResult(
                    providerId = manifest.id,
                    title = title,
                    url = absolutize(url),
                    posterUrl = search.posterSelector?.let {
                        SelectorEngine.attr(item, it, search.posterAttribute).takeIf(String::isNotBlank)?.let(::absolutize)
                    },
                    subtitle = search.subtitleSelector?.let { SelectorEngine.text(item, it) }.orEmpty(),
                )
            }
        }
    }

    override suspend fun loadDetail(result: SearchResult): MediaDetail {
        val document = fetchDocument(result.url)
        val detail = rule.detail
        val episodes = rule.episodes?.let { episodeRule ->
            SelectorEngine.select(document, episodeRule.itemSelector).mapIndexedNotNull { index, item ->
                val title = SelectorEngine.text(item, episodeRule.titleSelector)
                val url = SelectorEngine.attr(item, episodeRule.urlSelector, episodeRule.urlAttribute)
                if (title.isBlank() || url.isBlank()) {
                    null
                } else {
                    Episode(
                        providerId = manifest.id,
                        id = "${result.url}#$index".hashCode().toString(),
                        title = title,
                        url = absolutize(url),
                        index = index + 1,
                    )
                }
            }
        }.orEmpty()
        return MediaDetail(
            providerId = manifest.id,
            title = detail?.let { SelectorEngine.text(document, it.titleSelector) }.orEmpty().ifBlank { result.title },
            url = result.url,
            summary = detail?.summarySelector?.let { SelectorEngine.text(document, it) },
            posterUrl = detail?.posterSelector?.let {
                SelectorEngine.attr(document, it, detail.posterAttribute).takeIf(String::isNotBlank)?.let(::absolutize)
            } ?: result.posterUrl,
            episodes = episodes.ifEmpty {
                listOf(
                    Episode(
                        providerId = manifest.id,
                        id = result.url.hashCode().toString(),
                        title = result.title,
                        url = result.url,
                        index = 1,
                    ),
                )
            },
        )
    }

    override suspend fun resolveStreams(episode: Episode): List<MediaStream> {
        val stream = requireNotNull(rule.stream) { "Rule ${rule.id} has no stream block" }
        val document = fetchDocument(episode.url)
        val url = SelectorEngine.attr(document, stream.urlSelector, stream.urlAttribute)
        if (url.isBlank()) return emptyList()
        val protocol = if (stream.protocol == StreamProtocol.UNKNOWN) inferProtocol(url) else stream.protocol
        val policy = when (protocol) {
            StreamProtocol.WEBVIEW_ONLY -> DownloadPolicy.BlockedWebViewOnly
            else -> DownloadPolicy.Allowed
        }
        return listOf(
            MediaStream(
                id = "${episode.id}:0",
                providerId = manifest.id,
                url = absolutize(url),
                protocol = protocol,
                quality = stream.qualitySelector?.let { SelectorEngine.text(document, it) },
                codec = stream.codecSelector?.let { SelectorEngine.text(document, it) },
                headers = requestHeaders(referer = episode.url),
                downloadPolicy = policy,
                sourceScore = 40,
            ),
        )
    }

    private suspend fun fetchDocument(
        path: String,
        method: RuleHttpMethod = RuleHttpMethod.GET,
        bodyTemplate: String? = null,
    ) = withContext(Dispatchers.IO) {
        val url = if (path.startsWith("http://") || path.startsWith("https://")) path else absolutize(path)
        val requestBuilder = Request.Builder().url(url)
        requestHeaders(referer = rule.defaults.referer).forEach { (key, value) -> requestBuilder.header(key, value) }
        if (method == RuleHttpMethod.POST) {
            requestBuilder.post((bodyTemplate ?: "").toRequestBody("application/x-www-form-urlencoded".toMediaType()))
        }
        client.newCall(requestBuilder.build()).execute().use { response ->
            require(response.isSuccessful) { "HTTP ${response.code} for $url" }
            Jsoup.parse(response.body?.string().orEmpty(), url)
        }
    }

    private fun requestHeaders(referer: String?): Map<String, String> {
        return buildMap {
            rule.defaults.userAgent?.let { put("User-Agent", it) }
            referer?.let { put("Referer", it) }
            putAll(rule.defaults.headers)
        }
    }

    private fun absolutize(value: String): String {
        return runCatching { URI(rule.baseUrl).resolve(value).toString() }.getOrDefault(value)
    }

    private fun inferProtocol(url: String): StreamProtocol = when {
        url.contains(".m3u8", ignoreCase = true) -> StreamProtocol.HLS
        url.contains(".mpd", ignoreCase = true) -> StreamProtocol.DASH
        url.startsWith("rtsp://", ignoreCase = true) -> StreamProtocol.RTSP
        url.endsWith(".mp4", ignoreCase = true) || url.endsWith(".mkv", ignoreCase = true) -> StreamProtocol.PROGRESSIVE
        else -> StreamProtocol.UNKNOWN
    }
}

private fun String.urlEncode(): String = java.net.URLEncoder.encode(this, Charsets.UTF_8.name())
