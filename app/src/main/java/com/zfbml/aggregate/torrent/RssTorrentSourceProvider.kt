package com.zfbml.aggregate.torrent

import com.zfbml.aggregate.source.DownloadPolicy
import com.zfbml.aggregate.source.Episode
import com.zfbml.aggregate.source.MediaDetail
import com.zfbml.aggregate.source.MediaStream
import com.zfbml.aggregate.source.SearchResult
import com.zfbml.aggregate.source.SourceCapability
import com.zfbml.aggregate.source.SourceManifest
import com.zfbml.aggregate.source.SourceProvider
import com.zfbml.aggregate.source.StreamProtocol
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser

class RssTorrentSourceProvider(
    private val config: RssTorrentSourceConfig,
    private val client: OkHttpClient,
) : SourceProvider {
    override val manifest: SourceManifest = SourceManifest(
        id = config.id,
        name = config.name,
        version = "0.1.0",
        author = "zfbml",
        capabilities = setOf(
            SourceCapability.SEARCH,
            SourceCapability.DETAIL,
            SourceCapability.EPISODES,
            SourceCapability.STREAM,
            SourceCapability.DOWNLOAD,
            SourceCapability.BITTORRENT,
        ),
        domains = config.domains,
        supportsDownload = true,
    )

    override suspend fun search(query: String): List<SearchResult> {
        val trimmed = query.trim()
        if (trimmed.isBlank() || trimmed.startsWith("magnet:", ignoreCase = true)) {
            return emptyList()
        }
        val xml = fetch(config.searchUrl(trimmed))
        return RssTorrentParser.parse(xml, config).take(config.maxResults).map { item ->
            val subtitle = buildList {
                add(config.name)
                item.quality?.let { add(it) }
                item.sizeBytes?.let { add(formatBytes(it)) }
                item.seeders?.let { add("${it} seeders") }
            }.joinToString(" / ")
            SearchResult(
                providerId = manifest.id,
                title = item.title,
                url = item.pageUrl ?: item.torrentUrl,
                subtitle = subtitle,
                raw = item.raw(),
            )
        }
    }

    override suspend fun loadDetail(result: SearchResult): MediaDetail {
        return MediaDetail(
            providerId = manifest.id,
            title = result.title,
            url = result.url,
            summary = result.raw["description"] ?: result.subtitle,
            episodes = listOf(
                Episode(
                    providerId = manifest.id,
                    id = result.raw["infoHash"] ?: result.raw["torrentUrl"] ?: result.url.hashCode().toString(),
                    title = result.title,
                    url = result.raw["torrentUrl"] ?: result.url,
                    index = TorrentTitleScorer.extractEpisode(result.title),
                    raw = result.raw,
                ),
            ),
        )
    }

    override suspend fun resolveStreams(episode: Episode): List<MediaStream> {
        val torrentUrl = episode.raw["torrentUrl"] ?: episode.url
        val candidate = TorrentCandidate(
            title = episode.title,
            magnetUri = torrentUrl,
            infoHash = episode.raw["infoHash"] ?: MagnetLink.parse(torrentUrl)?.infoHash,
            sizeBytes = episode.raw["sizeBytes"]?.toLongOrNull(),
            seeders = episode.raw["seeders"]?.toIntOrNull(),
            subgroup = episode.raw["subgroup"],
            quality = episode.raw["quality"] ?: TorrentTitleScorer.extractQuality(episode.title),
            episodeIndex = episode.index,
            sourceName = manifest.name,
        )
        return listOf(
            MediaStream(
                id = candidate.infoHash ?: torrentUrl.hashCode().toString(),
                providerId = manifest.id,
                url = torrentUrl,
                protocol = StreamProtocol.BITTORRENT,
                quality = candidate.quality ?: "BT",
                codec = "auto",
                downloadPolicy = DownloadPolicy.CacheOnly,
                sourceScore = (TorrentTitleScorer.score(episode.title, candidate) + config.scoreBoost).coerceIn(0, 220),
                metadata = candidate.metadata() + mapOf(
                    "pageUrl" to (episode.raw["pageUrl"] ?: episode.url),
                    "torrentUrl" to torrentUrl,
                ),
            ),
        )
    }

    private suspend fun fetch(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/rss+xml, application/xml, text/xml, */*")
            .build()
        client.newCall(request).execute().use { response ->
            require(response.isSuccessful) { "HTTP ${response.code} for $url" }
            response.body?.string().orEmpty()
        }
    }

    companion object {
        fun dmhy(client: OkHttpClient) = RssTorrentSourceProvider(
            RssTorrentSourceConfig(
                id = "dmhy",
                name = "DMHY",
                domains = setOf("share.dmhy.org"),
                searchUrlTemplate = "https://share.dmhy.org/topics/rss/rss.xml?keyword={query}",
                scoreBoost = 18,
            ),
            client,
        )

        fun acgRip(client: OkHttpClient) = RssTorrentSourceProvider(
            RssTorrentSourceConfig(
                id = "acg-rip",
                name = "ACG.RIP",
                domains = setOf("acg.rip"),
                searchUrlTemplate = "https://acg.rip/.xml?term={query}",
                scoreBoost = 16,
            ),
            client,
        )

        fun nyaa(client: OkHttpClient) = RssTorrentSourceProvider(
            RssTorrentSourceConfig(
                id = "nyaa",
                name = "Nyaa",
                domains = setOf("nyaa.si"),
                searchUrlTemplate = "https://nyaa.si/?page=rss&q={query}&c=1_2&f=0",
                scoreBoost = 10,
            ),
            client,
        )

        fun mikan(client: OkHttpClient) = RssTorrentSourceProvider(
            RssTorrentSourceConfig(
                id = "mikan",
                name = "Mikan",
                domains = setOf("mikanani.me"),
                searchUrlTemplate = "https://mikanani.me/RSS/Search?searchstr={query}",
                scoreBoost = 20,
            ),
            client,
        )

        fun bangumiMoe(client: OkHttpClient) = RssTorrentSourceProvider(
            RssTorrentSourceConfig(
                id = "bangumi-moe",
                name = "Bangumi Moe",
                domains = setOf("bangumi.moe"),
                searchUrlTemplate = "https://bangumi.moe/rss/search/{query}",
                scoreBoost = 12,
            ),
            client,
        )

        private const val USER_AGENT =
            "Mozilla/5.0 (Android; ZFBML) AppleWebKit/537.36 (KHTML, like Gecko) ZfbmlAggregate/0.2"
    }
}

data class RssTorrentSourceConfig(
    val id: String,
    val name: String,
    val domains: Set<String>,
    val searchUrlTemplate: String,
    val scoreBoost: Int = 0,
    val maxResults: Int = 50,
) {
    fun searchUrl(query: String): String {
        return searchUrlTemplate.replace("{query}", query.urlEncode())
    }
}

data class RssTorrentItem(
    val title: String,
    val torrentUrl: String,
    val pageUrl: String?,
    val description: String?,
    val publishedAt: String?,
    val infoHash: String?,
    val sizeBytes: Long?,
    val seeders: Int?,
    val quality: String?,
    val subgroup: String?,
    val sourceName: String,
) {
    fun raw(): Map<String, String> = buildMap {
        put("torrentUrl", torrentUrl)
        pageUrl?.let { put("pageUrl", it) }
        description?.let { put("description", it) }
        publishedAt?.let { put("publishedAt", it) }
        infoHash?.let { put("infoHash", it) }
        sizeBytes?.let { put("sizeBytes", it.toString()) }
        seeders?.let { put("seeders", it.toString()) }
        quality?.let { put("quality", it) }
        subgroup?.let { put("subgroup", it) }
        put("sourceName", sourceName)
    }
}

object RssTorrentParser {
    fun parse(xml: String, config: RssTorrentSourceConfig): List<RssTorrentItem> {
        val document = Jsoup.parse(xml, "", Parser.xmlParser())
        return document.getElementsByTag("item").mapNotNull { item ->
            val title = item.directChildText("title").ifBlank { return@mapNotNull null }
            val enclosure = item.directChild("enclosure")
            val enclosureUrl = enclosure?.attr("url").orEmpty().ifBlank { null }
            val link = item.directChildText("link").ifBlank { null }
            val torrentUrl = when {
                enclosureUrl != null -> enclosureUrl
                link != null && link.isTorrentLike() -> link
                else -> null
            }?.normalizeTorrentUrl() ?: return@mapNotNull null
            val description = item.directChildText("description")
                .takeIf(String::isNotBlank)
                ?.let { Jsoup.parse(it).text().trim() }
            val infoHash = item.directChildText("nyaa:infoHash")
                .ifBlank { MagnetLink.parse(torrentUrl)?.infoHash.orEmpty() }
                .ifBlank { null }
            val sizeBytes = item.directChildText("nyaa:size").parseSizeBytes()
                ?: enclosure?.attr("length")?.toLongOrNull()?.takeIf { it > 1024L }
                ?: description?.parseSizeBytes()
            RssTorrentItem(
                title = title,
                torrentUrl = torrentUrl,
                pageUrl = link,
                description = description,
                publishedAt = item.directChildText("pubDate").ifBlank { null },
                infoHash = infoHash,
                sizeBytes = sizeBytes,
                seeders = item.directChildText("nyaa:seeders").toIntOrNull(),
                quality = TorrentTitleScorer.extractQuality(title),
                subgroup = title.extractSubgroup(),
                sourceName = config.name,
            )
        }
    }

    private fun Element.directChild(tagName: String): Element? {
        return children().firstOrNull { child ->
            child.tagName().equals(tagName, ignoreCase = true) ||
                child.tagName().substringAfter(':').equals(tagName, ignoreCase = true)
        }
    }

    private fun Element.directChildText(tagName: String): String {
        return directChild(tagName)?.text().orEmpty().trim()
    }
}

private fun String.isTorrentLike(): Boolean {
    return startsWith("magnet:", ignoreCase = true) || endsWith(".torrent", ignoreCase = true)
}

private fun String.normalizeTorrentUrl(): String {
    if (startsWith("magnet:", ignoreCase = true)) return this
    return toHttpUrlOrNull()?.toString() ?: this
}

private fun String.extractSubgroup(): String? {
    return Regex("""^[\[【]([^\]】]{1,32})[\]】]""").find(this)?.groupValues?.getOrNull(1)
}

private fun String?.parseSizeBytes(): Long? {
    if (this.isNullOrBlank()) return null
    val match = Regex("""(?i)(\d+(?:\.\d+)?)\s*(tib|tb|gib|gb|mib|mb|kib|kb|b)""")
        .find(this)
        ?: return null
    val value = match.groupValues[1].toDoubleOrNull() ?: return null
    val multiplier = when (match.groupValues[2].lowercase()) {
        "tb", "tib" -> 1024L * 1024L * 1024L * 1024L
        "gb", "gib" -> 1024L * 1024L * 1024L
        "mb", "mib" -> 1024L * 1024L
        "kb", "kib" -> 1024L
        else -> 1L
    }
    return (value * multiplier).toLong()
}

private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name()).replace("+", "%20")

private fun formatBytes(bytes: Long): String {
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
