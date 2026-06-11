package com.zfbml.aggregate.danmaku

import com.zfbml.aggregate.source.Episode
import com.zfbml.aggregate.source.MediaDetail
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

private const val MAX_AUTOMATIC_TITLE_SEARCH_COUNT = 4

abstract class WebDanmakuProvider(
    final override val id: String,
    final override val platform: DanmakuPlatform,
    final override val profile: DanmakuProfile,
    private val service: WebDanmakuService,
) : DanmakuProvider {
    final override val authDomain: String? = null

    override suspend fun match(detail: MediaDetail, episode: Episode): List<DanmakuMatch> {
        return service.match(platform, detail.title, episode.title, episode.index)
    }

    override suspend fun fetchTimeline(match: DanmakuMatch): List<DanmakuItem> {
        return service.fetch(match)
    }

    override fun normalize(raw: String): List<DanmakuItem> = emptyList()
}

class BilibiliDanmakuProvider(httpClient: OkHttpClient) : WebDanmakuProvider(
    id = "danmaku-bilibili",
    platform = DanmakuPlatform.Bilibili,
    profile = DanmakuProfile(
        platform = DanmakuPlatform.Bilibili,
        supportsAdvanced = true,
        strokeWidthPx = 5f,
        maxTracks = 14,
    ),
    service = WebDanmakuService(httpClient),
) {
    override fun normalize(raw: String): List<DanmakuItem> = BilibiliDanmakuParser.parseXml(raw)
}

class TencentDanmakuProvider(httpClient: OkHttpClient) : WebDanmakuProvider(
    id = "danmaku-tencent",
    platform = DanmakuPlatform.Tencent,
    profile = DanmakuProfile(platform = DanmakuPlatform.Tencent, strokeWidthPx = 4f),
    service = WebDanmakuService(httpClient),
)

class IqiyiDanmakuProvider(httpClient: OkHttpClient) : WebDanmakuProvider(
    id = "danmaku-iqiyi",
    platform = DanmakuPlatform.Iqiyi,
    profile = DanmakuProfile(platform = DanmakuPlatform.Iqiyi, shadowRadiusPx = 5f),
    service = WebDanmakuService(httpClient),
)

class YoukuDanmakuProvider(httpClient: OkHttpClient) : WebDanmakuProvider(
    id = "danmaku-youku",
    platform = DanmakuPlatform.Youku,
    profile = DanmakuProfile(platform = DanmakuPlatform.Youku, fontScale = 0.96f),
    service = WebDanmakuService(httpClient),
)

class DanmakuRegistry(
    providers: List<DanmakuProvider>,
    initialManualMappings: List<DanmakuManualMapping> = emptyList(),
    private val timelineCacheSize: Int = DEFAULT_TIMELINE_CACHE_SIZE,
) {
    private val byId = providers.associateBy { it.id }
    private val timelineCacheLock = Mutex()
    private val manualMappingLock = Mutex()
    private var manualMappings = initialManualMappings
    private val timelineCache = LinkedHashMap<DanmakuTimelineCacheKey, List<DanmakuItem>>(16, 0.75f, true)
    private val inFlightTimelineRequests = mutableMapOf<DanmakuTimelineCacheKey, Deferred<List<DanmakuItem>>>()
    val profiles: List<DanmakuProfile> = providers.map { it.profile }

    fun provider(id: String): DanmakuProvider? = byId[id]

    suspend fun matchAll(detail: MediaDetail, episode: Episode): List<DanmakuMatch> = coroutineScope {
        val manualMatches = manualMatchesFor(detail, episode)
        val automaticMatches = automaticMatches(detail, episode)
        (manualMatches + automaticMatches)
            .distinctBy { it.providerId to it.token }
            .sortedByDescending { it.score }
    }

    suspend fun searchCandidates(detail: MediaDetail, episode: Episode, query: String): List<DanmakuMatch> {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return emptyList()
        return automaticMatches(detail.copy(title = cleanQuery), episode, includeAliases = false)
            .distinctBy { it.providerId to it.token }
            .sortedByDescending { it.score }
    }

    suspend fun replaceManualMappings(mappings: List<DanmakuManualMapping>) {
        manualMappingLock.withLock {
            manualMappings = mappings
        }
        clearTimelineCache()
    }

    suspend fun addOrReplaceManualMapping(mapping: DanmakuManualMapping) {
        manualMappingLock.withLock {
            manualMappings = manualMappings
                .filterNot { it.sameManualTarget(mapping) }
                .plus(mapping)
        }
        clearTimelineCache()
    }

    private suspend fun automaticMatches(
        detail: MediaDetail,
        episode: Episode,
        includeAliases: Boolean = true,
    ): List<DanmakuMatch> = coroutineScope {
        val titles = if (includeAliases) {
            danmakuAutomaticSearchTitles(detail, episode)
        } else {
            listOf(detail.title.trim()).filter { it.isNotBlank() }
        }
        titles.flatMap { title ->
            byId.values.map { provider ->
                async {
                    runCatching { provider.match(detail.copy(title = title), episode) }.getOrDefault(emptyList())
                }
            }
        }.awaitAll()
            .flatten()
            .distinctBy { it.providerId to it.token }
            .sortedByDescending { it.score }
    }

    private suspend fun manualMatchesFor(detail: MediaDetail, episode: Episode): List<DanmakuMatch> {
        val mappings = manualMappingLock.withLock { manualMappings.toList() }
        return mappings
            .filter { it.matches(detail, episode) }
            .mapNotNull { mapping ->
                if (mapping.match.providerId !in byId) return@mapNotNull null
                mapping.match.copy(
                    score = maxOf(mapping.match.score, MANUAL_MAPPING_SCORE),
                    source = DanmakuMatchSource.Manual,
                )
            }
            .sortedByDescending { it.score }
    }

    suspend fun fetchBestTimeline(detail: MediaDetail, episode: Episode): List<DanmakuItem> {
        val cacheKey = DanmakuTimelineCacheKey.from(detail, episode)
        return coroutineScope {
            var createdRequest = false
            val request = timelineCacheLock.withLock {
                timelineCache[cacheKey]?.let { return@coroutineScope it }
                inFlightTimelineRequests[cacheKey] ?: async {
                    fetchBestTimelineUncached(detail, episode)
                }.also { deferred ->
                    inFlightTimelineRequests[cacheKey] = deferred
                    createdRequest = true
                }
            }

            try {
                val timeline = request.await()
                if (createdRequest) {
                    timelineCacheLock.withLock {
                        if (inFlightTimelineRequests[cacheKey] === request) {
                            inFlightTimelineRequests.remove(cacheKey)
                        }
                        if (timeline.isNotEmpty()) {
                            cacheTimeline(cacheKey, timeline)
                        }
                    }
                }
                timeline
            } catch (error: Throwable) {
                if (createdRequest) {
                    timelineCacheLock.withLock {
                        if (inFlightTimelineRequests[cacheKey] === request) {
                            inFlightTimelineRequests.remove(cacheKey)
                        }
                    }
                }
                throw error
            }
        }
    }

    private suspend fun fetchBestTimelineUncached(detail: MediaDetail, episode: Episode): List<DanmakuItem> {
        for (match in manualMatchesFor(detail, episode)) {
            val timeline = runCatching {
                provider(match.providerId)?.fetchTimeline(match).orEmpty()
            }.getOrDefault(emptyList())
            if (timeline.isNotEmpty()) return timeline
        }
        for (match in automaticMatches(detail, episode)) {
            val timeline = runCatching {
                provider(match.providerId)?.fetchTimeline(match).orEmpty()
            }.getOrDefault(emptyList())
            if (timeline.isNotEmpty()) return timeline
        }
        return emptyList()
    }

    private suspend fun clearTimelineCache() {
        timelineCacheLock.withLock {
            timelineCache.clear()
            inFlightTimelineRequests.clear()
        }
    }

    private fun cacheTimeline(key: DanmakuTimelineCacheKey, timeline: List<DanmakuItem>) {
        if (timelineCacheSize <= 0) return
        timelineCache[key] = timeline
        while (timelineCache.size > timelineCacheSize) {
            val iterator = timelineCache.entries.iterator()
            if (!iterator.hasNext()) break
            iterator.next()
            iterator.remove()
        }
    }

    private companion object {
        const val DEFAULT_TIMELINE_CACHE_SIZE = 48
        const val MANUAL_MAPPING_SCORE = 100_000
    }
}

private fun danmakuAutomaticSearchTitles(detail: MediaDetail, episode: Episode): List<String> {
    val candidates = buildList {
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
        .distinctBy { it.normalizedDanmakuTitleKey() }
    val cjkTitles = candidates.filter { it.containsDanmakuCjkText() }
    val nonCjkTitles = candidates.filterNot { candidate ->
        cjkTitles.any { it.normalizedDanmakuTitleKey() == candidate.normalizedDanmakuTitleKey() }
    }
    return (cjkTitles + nonCjkTitles).take(MAX_AUTOMATIC_TITLE_SEARCH_COUNT)
}

private fun String.normalizedDanmakuTitleKey(): String {
    return lowercase().filter { char ->
        char.isLetterOrDigit() ||
            char in '\u4e00'..'\u9fff' ||
            char in '\u3040'..'\u30ff' ||
            char in '\u3400'..'\u4dbf'
    }
}

private fun String.containsDanmakuCjkText(): Boolean {
    return any { char ->
        char in '\u4e00'..'\u9fff' ||
            char in '\u3040'..'\u30ff' ||
            char in '\u3400'..'\u4dbf'
    }
}

internal fun danmakuCandidateMatchScore(
    baseScore: Int,
    queryTitle: String,
    candidateTitle: String,
    candidateEpisodeTitle: String?,
    candidateEpisodeOrder: Int?,
    requestedEpisodeTitle: String,
    requestedEpisodeNumber: Int,
): Int {
    return baseScore +
        danmakuTitleMatchScore(queryTitle, candidateTitle) +
        danmakuEpisodeMatchScore(
            candidateEpisodeTitle = candidateEpisodeTitle,
            candidateEpisodeOrder = candidateEpisodeOrder,
            requestedEpisodeTitle = requestedEpisodeTitle,
            requestedEpisodeNumber = requestedEpisodeNumber,
        )
}

internal fun danmakuTitleMatchScore(queryTitle: String, candidateTitle: String): Int {
    val query = normalizeDanmakuMatchText(queryTitle)
    val candidate = normalizeDanmakuMatchText(candidateTitle)
    if (query.isBlank() || candidate.isBlank()) return 0
    return when {
        candidate == query -> 36
        candidate.contains(query) -> 28
        candidate.length >= maxOf(4, (query.length * 0.65f).toInt()) && query.contains(candidate) -> 20
        else -> 0
    }
}

internal fun danmakuEpisodeMatchScore(
    candidateEpisodeTitle: String?,
    candidateEpisodeOrder: Int?,
    requestedEpisodeTitle: String,
    requestedEpisodeNumber: Int,
): Int {
    val requestedTitle = normalizeDanmakuMatchText(requestedEpisodeTitle)
    val candidateTitle = normalizeDanmakuMatchText(candidateEpisodeTitle.orEmpty())
    val requestedTitleNumber = danmakuEpisodeNumberFromText(requestedEpisodeTitle)
    val candidateTitleNumber = danmakuEpisodeNumberFromText(candidateEpisodeTitle.orEmpty())
    val titleNumbersAgree = requestedTitleNumber == null || candidateTitleNumber == null || requestedTitleNumber == candidateTitleNumber
    var score = 0
    if (requestedTitle.isNotBlank() && candidateTitle.isNotBlank()) {
        score += when {
            candidateTitle == requestedTitle -> 24
            titleNumbersAgree && candidateTitle.length >= 4 && requestedTitle.contains(candidateTitle) -> 16
            titleNumbersAgree && requestedTitle.length >= 4 && candidateTitle.contains(requestedTitle) -> 16
            else -> 0
        }
    }
    if (requestedEpisodeNumber > 0 && candidateEpisodeOrder == requestedEpisodeNumber) {
        score += 18
    }
    if (requestedEpisodeNumber > 0 && candidateTitleNumber == requestedEpisodeNumber) {
        score += 12
    }
    return score
}

private fun normalizeDanmakuMatchText(value: String): String {
    return htmlDecodeDanmakuText(value.replace(Regex("""<[^>]+>"""), ""))
        .lowercase()
        .replace("\u6d77\u8d3c\u738b", "\u822a\u6d77\u738b")
        .replace(Regex("""[\s\p{P}\p{S}]"""), "")
}

internal fun danmakuEpisodeNumberFromText(value: String): Int? {
    val clean = htmlDecodeDanmakuText(value.replace(Regex("""<[^>]+>"""), "")).trim()
    Regex("""第\s*([零〇一二三四五六七八九十百两\d]+)\s*[集话話期]""")
        .find(clean)
        ?.groupValues
        ?.getOrNull(1)
        ?.let(::parseDanmakuEpisodeNumberToken)
        ?.let { return it }
    Regex("""(?i)\b(?:ep|episode)\.?\s*0*(\d{1,4})\b""")
        .find(clean)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
        ?.let { return it }
    Regex("""\b0*(\d{1,4})\b""")
        .find(clean)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
        ?.let { return it }
    return Regex("""([零〇一二三四五六七八九十百两]{1,8})\s*[集话話期]""")
        .find(clean)
        ?.groupValues
        ?.getOrNull(1)
        ?.let(::parseDanmakuEpisodeNumberToken)
}

private fun parseDanmakuEpisodeNumberToken(value: String): Int? {
    val clean = value.trim().trimStart('0')
    if (clean.isBlank()) return 0
    clean.toIntOrNull()?.let { return it }
    return parseDanmakuChineseNumber(clean)
}

private fun parseDanmakuChineseNumber(value: String): Int? {
    if (value.none { it == '十' || it == '百' }) {
        val digits = value.map { danmakuChineseDigitValue(it) ?: return null }
        return digits.joinToString("").toIntOrNull()
    }
    var total = 0
    var current = 0
    var hasValue = false
    for (char in value) {
        when (char) {
            '零', '〇' -> Unit
            '十' -> {
                total += (if (current == 0) 1 else current) * 10
                current = 0
                hasValue = true
            }
            '百' -> {
                total += (if (current == 0) 1 else current) * 100
                current = 0
                hasValue = true
            }
            else -> {
                current = danmakuChineseDigitValue(char) ?: return null
                hasValue = true
            }
        }
    }
    return (total + current).takeIf { hasValue && it > 0 }
}

private fun danmakuChineseDigitValue(char: Char): Int? {
    return when (char) {
        '零', '〇' -> 0
        '一' -> 1
        '二', '两' -> 2
        '三' -> 3
        '四' -> 4
        '五' -> 5
        '六' -> 6
        '七' -> 7
        '八' -> 8
        '九' -> 9
        else -> null
    }
}

private fun htmlDecodeDanmakuText(value: String): String {
    return value
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
}

private fun DanmakuManualMapping.matches(detail: MediaDetail, episode: Episode): Boolean {
    if (episodeId.isNullOrBlank() && episodeIndex == null) return false
    if (!detailProviderId.isNullOrBlank() && detailProviderId != detail.providerId) return false
    if (!detailUrl.isNullOrBlank() && detailUrl != detail.url) return false
    if (!episodeId.isNullOrBlank() && episodeId != episode.id) return false
    if (episodeIndex != null && episodeIndex != episode.index) return false
    val expectedTitle = normalizedManualTitle(detailTitle)
    val actualTitle = normalizedManualTitle(detail.title)
    if (expectedTitle.isBlank() || actualTitle.isBlank()) return false
    return expectedTitle == actualTitle || expectedTitle.contains(actualTitle) || actualTitle.contains(expectedTitle)
}

private fun DanmakuManualMapping.sameManualTarget(other: DanmakuManualMapping): Boolean {
    return normalizedManualTitle(detailTitle) == normalizedManualTitle(other.detailTitle) &&
        detailProviderId == other.detailProviderId &&
        detailUrl == other.detailUrl &&
        episodeId == other.episodeId &&
        episodeIndex == other.episodeIndex
}

private fun normalizedManualTitle(value: String): String {
    return value
        .lowercase()
        .replace(Regex("""[\s\p{P}\p{S}]"""), "")
}

private data class DanmakuTimelineCacheKey(
    val detailProviderId: String,
    val detailTitle: String,
    val detailUrl: String,
    val episodeProviderId: String,
    val episodeId: String,
    val episodeUrl: String,
    val episodeIndex: Int?,
    val subjectId: String?,
    val episodeRemoteId: String?,
) {
    companion object {
        fun from(detail: MediaDetail, episode: Episode): DanmakuTimelineCacheKey {
            return DanmakuTimelineCacheKey(
                detailProviderId = detail.providerId,
                detailTitle = detail.title,
                detailUrl = detail.url,
                episodeProviderId = episode.providerId,
                episodeId = episode.id,
                episodeUrl = episode.url,
                episodeIndex = episode.index,
                subjectId = episode.raw["subjectId"],
                episodeRemoteId = episode.raw["episodeId"],
            )
        }
    }
}

class WebDanmakuService(
    private val httpClient: OkHttpClient,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun match(
        platform: DanmakuPlatform,
        title: String,
        episodeTitle: String,
        episodeIndex: Int?,
    ): List<DanmakuMatch> {
        val cleanTitle = cleanTitle(title)
        if (cleanTitle.isBlank()) return emptyList()
        val targetEpisode = episodeIndex ?: episodeNumberFromText(episodeTitle) ?: 1
        return runCatching {
            when (platform) {
                DanmakuPlatform.Bilibili -> matchBilibili(cleanTitle, episodeTitle, targetEpisode)
                DanmakuPlatform.Tencent -> matchTencent(cleanTitle, episodeTitle, targetEpisode)
                DanmakuPlatform.Iqiyi -> matchIqiyi(cleanTitle, episodeTitle, targetEpisode)
                DanmakuPlatform.Youku -> matchYouku(cleanTitle, episodeTitle, targetEpisode)
                DanmakuPlatform.Local -> emptyList()
            }
        }.getOrElse { emptyList() }
    }

    suspend fun fetch(match: DanmakuMatch): List<DanmakuItem> {
        return runCatching {
            val parts = match.token.split("|")
            when (parts.firstOrNull()) {
                "bilibili" -> fetchBilibili(parts.getOrNull(1).orEmpty())
                "tencent" -> fetchTencent(parts.getOrNull(1).orEmpty())
                "iqiyi" -> fetchIqiyi(parts.getOrNull(1).orEmpty())
                "youku" -> fetchYouku(parts.getOrNull(1).orEmpty())
                else -> emptyList()
            }
        }.getOrElse { emptyList() }
    }

    private suspend fun matchBilibili(title: String, episodeTitle: String, episodeNumber: Int): List<DanmakuMatch> {
        val normalized = normalizeForMatch(title)
        val candidates = buildList {
            for (type in listOf("media_bangumi", "media_ft")) {
                val url = "https://api.bilibili.com/x/web-interface/search/type?keyword=${enc(title)}&search_type=$type"
                val root = getJson(url, "https://search.bilibili.com/")
                val items = root.obj("data")?.array("result").orEmpty()
                for (item in items.mapNotNull { it.asObject() }) {
                    val seasonId = item.string("season_id").toIntOrNull() ?: continue
                    val itemTitle = stripHtml(item.string("title"))
                    if (itemTitle.isBlank() || !looksLikeSameTitle(itemTitle, normalized)) continue
                    add(SearchCandidate(seasonId.toString(), itemTitle, item.string("type_name"), 90))
                }
            }
        }.distinctBy { it.id }.take(4)

        return candidates.mapNotNull { candidate ->
            val episode = pickEpisode(fetchBilibiliEpisodes(candidate.id), episodeTitle, episodeNumber) ?: return@mapNotNull null
            DanmakuMatch(
                providerId = "danmaku-bilibili",
                platform = DanmakuPlatform.Bilibili,
                title = candidate.title,
                episodeTitle = episode.title,
                score = danmakuCandidateMatchScore(
                    baseScore = candidate.score,
                    queryTitle = title,
                    candidateTitle = candidate.title,
                    candidateEpisodeTitle = episode.title,
                    candidateEpisodeOrder = episode.order,
                    requestedEpisodeTitle = episodeTitle,
                    requestedEpisodeNumber = episodeNumber,
                ),
                token = "bilibili|${episode.id}",
            )
        }
    }

    private suspend fun fetchBilibili(cid: String): List<DanmakuItem> {
        if (cid.isBlank()) return emptyList()
        val xml = getText("https://api.bilibili.com/x/v1/dm/list.so?oid=$cid", "https://www.bilibili.com/")
        return BilibiliDanmakuParser.parseXml(xml).map { it.copy(platform = DanmakuPlatform.Bilibili) }
    }

    private suspend fun fetchBilibiliEpisodes(seasonId: String): List<PlatformEpisode> {
        val root = getJson("https://api.bilibili.com/pgc/view/web/season?season_id=$seasonId", "https://www.bilibili.com/")
        val result = root.obj("result") ?: return emptyList()
        val raw = mutableListOf<JsonElement>()
        raw.addAll(result.array("episodes").orEmpty())
        result.array("section").orEmpty().mapNotNull { it.asObject() }.forEach { section ->
            raw.addAll(section.array("episodes").orEmpty())
        }
        return raw.mapIndexedNotNull { index, element ->
            val item = element.asObject() ?: return@mapIndexedNotNull null
            val cid = item.string("cid").takeIf { it.isNotBlank() } ?: return@mapIndexedNotNull null
            val title = listOf(item.string("title"), item.string("long_title")).filter { it.isNotBlank() }.joinToString(" ")
            PlatformEpisode(
                id = cid,
                order = item.string("title").toIntOrNull() ?: index + 1,
                title = title,
            )
        }
    }

    private suspend fun matchTencent(title: String, episodeTitle: String, episodeNumber: Int): List<DanmakuMatch> {
        val body = """
            {"version":"25071701","clientType":1,"filterValue":"","uuid":"0379274D-05A0-4EB6-A89C-878C9A460426","query":"${title.escapeJson()}","retry":0,"pagenum":0,"isPrefetch":true,"pagesize":30,"queryFrom":0,"searchDatakey":"","transInfo":"","isneedQc":true,"preQid":"","adClientInfo":"","extraInfo":{"multi_terminal_pc":"1","themeType":"1","sugRelatedIds":"{}","appVersion":""}}
        """.trimIndent()
        val root = postJson(
            "https://pbaccess.video.qq.com/trpc.videosearch.mobile_search.MultiTerminalSearch/MbSearch?vplatform=2",
            body,
            "https://v.qq.com/x/search/?q=${enc(title)}",
        )
        val normalized = normalizeForMatch(title)
        val items = mutableListOf<JsonElement>()
        root.obj("data")?.array("areaBoxList").orEmpty().mapNotNull { it.asObject() }.forEach { box ->
            items.addAll(box.array("itemList").orEmpty())
        }
        root.obj("data")?.obj("normalList")?.array("itemList").orEmpty().let(items::addAll)
        val candidates = items.mapNotNull { element ->
            val item = element.asObject() ?: return@mapNotNull null
            val videoInfo = item.obj("videoInfo") ?: return@mapNotNull null
            val titleText = stripHtml(videoInfo.string("title"))
            if (titleText.isBlank() || !looksLikeSameTitle(titleText, normalized)) return@mapNotNull null
            val episodes = tencentEpisodes(videoInfo)
            if (episodes.isEmpty()) return@mapNotNull null
            SearchEpisodesCandidate(titleText, videoInfo.string("typeName"), episodes, 82)
        }.take(5)

        return candidates.mapNotNull { candidate ->
            val episode = pickEpisode(candidate.episodes, episodeTitle, episodeNumber) ?: return@mapNotNull null
            DanmakuMatch(
                providerId = "danmaku-tencent",
                platform = DanmakuPlatform.Tencent,
                title = candidate.title,
                episodeTitle = episode.title,
                score = danmakuCandidateMatchScore(
                    baseScore = candidate.score,
                    queryTitle = title,
                    candidateTitle = candidate.title,
                    candidateEpisodeTitle = episode.title,
                    candidateEpisodeOrder = episode.order,
                    requestedEpisodeTitle = episodeTitle,
                    requestedEpisodeNumber = episodeNumber,
                ),
                token = "tencent|${episode.id}",
            )
        }
    }

    private fun tencentEpisodes(videoInfo: JsonObject): List<PlatformEpisode> {
        val result = mutableListOf<PlatformEpisode>()
        videoInfo.array("episodeSites").orEmpty().mapNotNull { it.asObject() }.forEach { site ->
            site.array("episodeInfoList").orEmpty().mapNotNull { it.asObject() }.forEachIndexed { index, episode ->
                val vid = episode.string("id")
                if (vid.isNotBlank()) {
                    val title = stripHtml(episode.string("title"))
                    result.add(PlatformEpisode(vid, episodeNumberFromText(title) ?: index + 1, title, episode.string("url")))
                }
            }
        }
        return result.distinctBy { it.id }
    }

    private suspend fun fetchTencent(vid: String): List<DanmakuItem> {
        if (vid.isBlank()) return emptyList()
        val base = getJson("https://dm.video.qq.com/barrage/base/$vid", "https://v.qq.com/")
        val segments = base.obj("segment_index")?.values
            ?.mapNotNull { it.asObject()?.string("segment_name") }
            ?.filter { it.isNotBlank() }
            ?.take(120)
            .orEmpty()
        return segments.flatMap { fetchTencentSegment(vid, it) }.sortedBy { it.timeMs }
    }

    private suspend fun fetchTencentSegment(vid: String, segment: String): List<DanmakuItem> {
        val root = getJson("https://dm.video.qq.com/barrage/segment/$vid/$segment", "https://v.qq.com/")
        return root.array("barrage_list").orEmpty().mapNotNull { element ->
            val item = element.asObject() ?: return@mapNotNull null
            val text = item.string("content").trim()
            if (text.isBlank()) return@mapNotNull null
            DanmakuItem(
                timeMs = item.string("time_offset").toLongOrNull() ?: 0L,
                text = text,
                mode = modeFromStyle(item.objFromString("content_style")),
                color = colorFromStyle(item.objFromString("content_style")),
                fontSizeSp = item.objFromString("content_style")?.string("font_size")?.toFloatOrNull() ?: 24f,
                platform = DanmakuPlatform.Tencent,
                rawStyle = mapOf("source" to "tencent"),
            )
        }
    }

    private suspend fun matchIqiyi(title: String, episodeTitle: String, episodeNumber: Int): List<DanmakuMatch> {
        val root = getJson(
            "https://mesh.if.iqiyi.com/portal/lw/search/homePageV3?key=${enc(title)}&current_page=1&mode=1&source=input&pageNum=1&pageSize=25&pcv=13.074.22699&version=13.074.22699",
            "https://www.iqiyi.com/",
        )
        val normalized = normalizeForMatch(title)
        val albums = mutableListOf<JsonObject>()
        collectIqiyiAlbums(root.obj("data")?.get("templates"), albums)
        val matches = albums.mapNotNull { album ->
            val cleanTitle = stripHtml(album.string("title"))
            val pageUrl = album.string("pageUrl")
            val qipuId = album.string("qipuId")
            if (cleanTitle.isBlank() || !looksLikeSameTitle(cleanTitle, normalized)) return@mapNotNull null
            val id = qipuId.ifBlank { iqiyiMediaIdFromUrl(pageUrl) }
            if (id.isBlank()) return@mapNotNull null
            val episodes = fetchIqiyiEpisodes(id).ifEmpty {
                listOf(PlatformEpisode(id, 1, cleanTitle, pageUrl.ifBlank { id }))
            }
            val episode = pickEpisode(episodes, episodeTitle, episodeNumber) ?: return@mapNotNull null
            DanmakuMatch(
                providerId = "danmaku-iqiyi",
                platform = DanmakuPlatform.Iqiyi,
                title = cleanTitle,
                episodeTitle = episode.title,
                score = danmakuCandidateMatchScore(
                    baseScore = 74,
                    queryTitle = title,
                    candidateTitle = cleanTitle,
                    candidateEpisodeTitle = episode.title,
                    candidateEpisodeOrder = episode.order,
                    requestedEpisodeTitle = episodeTitle,
                    requestedEpisodeNumber = episodeNumber,
                ),
                token = "iqiyi|${episode.url ?: episode.id}",
            )
        }
        return matches.take(5)
    }

    private suspend fun fetchIqiyi(token: String): List<DanmakuItem> {
        if (token.isBlank()) return emptyList()
        val tvid = if (token.all(Char::isDigit)) token else iqiyiMediaIdFromUrl(token)
        if (tvid.isBlank()) return emptyList()
        val info = getJson("https://pcw-api.iqiyi.com/video/video/baseinfo/$tvid", "https://www.iqiyi.com/")
        val data = info.obj("data") ?: return emptyList()
        val duration = data.string("durationSec").toDoubleOrNull()?.toInt() ?: return emptyList()
        val albumId = data.string("albumId").ifBlank { tvid }
        val categoryId = data.string("channelId").ifBlank { data.string("categoryId").ifBlank { "4" } }
        val count = minOf((duration / 300) + 1, 24)
        return (1..count).flatMap { fetchIqiyiSegment(tvid, albumId, categoryId, it) }.sortedBy { it.timeMs }
    }

    private suspend fun fetchIqiyiEpisodes(mediaId: String): List<PlatformEpisode> {
        if (!mediaId.all(Char::isDigit)) return emptyList()
        val root = getJson("https://pcw-api.iqiyi.com/albums/album/avlistinfo?aid=$mediaId&page=1&size=100", "https://www.iqiyi.com/")
        return root.obj("data")?.array("epsodelist").orEmpty().mapIndexedNotNull { index, element ->
            val item = element.asObject() ?: return@mapIndexedNotNull null
            val tvId = item.string("tvId")
            if (tvId.isBlank()) return@mapIndexedNotNull null
            val title = listOf(item.string("name"), item.string("subtitle")).filter { it.isNotBlank() }.joinToString(" ")
            PlatformEpisode(tvId, item.string("order").toIntOrNull() ?: index + 1, stripHtml(title), item.string("playUrl").ifBlank { tvId })
        }
    }

    private suspend fun fetchIqiyiSegment(tvid: String, albumId: String, categoryId: String, segment: Int): List<DanmakuItem> {
        if (tvid.length < 4) return emptyList()
        val path = "/bullet/${tvid.substring(tvid.length - 4, tvid.length - 2)}/${tvid.substring(tvid.length - 2)}/${tvid}_300_$segment.z"
        val bytes = getBytes("https://cmts.iqiyi.com$path?rn=0.0123456789123456&business=danmu&is_iqiyi=true&is_video_page=true&tvid=$tvid&albumid=$albumId&categoryid=$categoryId&qypid=010102101000000000", "https://www.iqiyi.com/")
        val xml = decodeCompressedText(bytes)
        val contents = extractXml(xml, "content")
        val times = extractXml(xml, "showTime")
        val colors = extractXml(xml, "color")
        val positions = extractXml(xml, "position")
        return contents.mapIndexedNotNull { index, raw ->
            val text = htmlDecode(raw).trim()
            if (text.isBlank()) return@mapIndexedNotNull null
            DanmakuItem(
                timeMs = ((times.getOrNull(index)?.toDoubleOrNull() ?: 0.0) * 1000).toLong(),
                text = text,
                mode = modeFromPosition(positions.getOrNull(index).orEmpty()),
                color = colorFromHex(colors.getOrNull(index).orEmpty()),
                platform = DanmakuPlatform.Iqiyi,
            )
        }
    }

    private suspend fun matchYouku(title: String, episodeTitle: String, episodeNumber: Int): List<DanmakuMatch> {
        val root = getJson("https://search.youku.com/api/search?keyword=${enc(title)}&userAgent=${enc(UA)}&site=1&categories=0&ftype=0&ob=0&pg=1", "https://www.youku.com/")
        val normalized = normalizeForMatch(title)
        return root.array("pageComponentList").orEmpty().mapNotNull { element ->
            val common = element.asObject()?.obj("commonData") ?: return@mapNotNull null
            val displayName = stripHtml(common.obj("titleDTO")?.string("displayName").orEmpty())
            val showId = common.string("showId")
            if (displayName.isBlank() || showId.isBlank() || !looksLikeSameTitle(displayName, normalized)) return@mapNotNull null
            val episodes = fetchYoukuEpisodes(showId)
            val episode = pickEpisode(episodes, episodeTitle, episodeNumber) ?: return@mapNotNull null
            DanmakuMatch(
                providerId = "danmaku-youku",
                platform = DanmakuPlatform.Youku,
                title = displayName,
                episodeTitle = episode.title,
                score = danmakuCandidateMatchScore(
                    baseScore = 68,
                    queryTitle = title,
                    candidateTitle = displayName,
                    candidateEpisodeTitle = episode.title,
                    candidateEpisodeOrder = episode.order,
                    requestedEpisodeTitle = episodeTitle,
                    requestedEpisodeNumber = episodeNumber,
                ),
                token = "youku|${episode.id}",
            )
        }.take(5)
    }

    private suspend fun fetchYoukuEpisodes(showId: String): List<PlatformEpisode> {
        val root = getJson("https://openapi.youku.com/v2/shows/videos.json?client_id=53e6cc67237fc59a&package=com.huawei.hwvplayer.youku&ext=show&show_id=$showId&page=1&count=100", "https://www.youku.com/")
        return root.array("videos").orEmpty().mapIndexedNotNull { index, element ->
            val item = element.asObject() ?: return@mapIndexedNotNull null
            val id = item.string("id")
            if (id.isBlank()) return@mapIndexedNotNull null
            PlatformEpisode(id, item.string("stage").toIntOrNull() ?: item.string("seq").toIntOrNull() ?: index + 1, item.string("displayName").ifBlank { item.string("title") }, item.string("link"))
        }
    }

    private suspend fun fetchYouku(videoId: String): List<DanmakuItem> {
        // Youku's current danmaku API requires transient H5 tokens. The no-login search and episode
        // resolution are wired now; tokenized segment fetching is the next port from Ling1.
        return emptyList()
    }

    private suspend fun getText(url: String, referer: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", UA)
            .header("Accept", "*/*")
            .header("Referer", referer)
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) "" else response.body.string()
        }
    }

    private suspend fun getBytes(url: String, referer: String): ByteArray = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", UA)
            .header("Accept", "*/*")
            .header("Referer", referer)
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) ByteArray(0) else response.body.bytes()
        }
    }

    private suspend fun getJson(url: String, referer: String): JsonObject {
        return parseJson(getText(url, referer))
    }

    private suspend fun postJson(url: String, body: String, referer: String): JsonObject = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody("application/json".toMediaType()))
            .header("User-Agent", UA)
            .header("Accept", "application/json,text/plain,*/*")
            .header("Origin", originFrom(referer))
            .header("Referer", referer)
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) JsonObject(emptyMap()) else parseJson(response.body.string())
        }
    }

    private fun parseJson(text: String): JsonObject {
        val clean = text.trim().removePrefix("callback(").removeSuffix(")")
        return runCatching { json.parseToJsonElement(clean).jsonObject }.getOrDefault(JsonObject(emptyMap()))
    }

    private fun pickEpisode(episodes: List<PlatformEpisode>, episodeTitle: String, episodeNumber: Int): PlatformEpisode? {
        if (episodes.isEmpty()) return null
        var bestEpisode: PlatformEpisode? = null
        var bestScore = 0
        episodes.forEach { candidate ->
            val score = danmakuEpisodeMatchScore(
                candidateEpisodeTitle = candidate.title,
                candidateEpisodeOrder = candidate.order,
                requestedEpisodeTitle = episodeTitle,
                requestedEpisodeNumber = episodeNumber,
            )
            if (score > bestScore) {
                bestScore = score
                bestEpisode = candidate
            }
        }
        bestEpisode?.let { return it }
        return episodes.getOrNull(episodeNumber - 1) ?: episodes.firstOrNull()
    }

    private fun cleanTitle(value: String): String = stripHtml(value)
        .replace(Regex("""第\s*[一二三四五六七八九十百零〇两\d]+\s*[季部期章]"""), "")
        .replace(Regex("""\s+"""), " ")
        .trim()

    private fun looksLikeSameTitle(title: String, normalizedQuery: String): Boolean {
        val normalizedTitle = normalizeForMatch(title)
        if (normalizedTitle.isBlank() || normalizedQuery.isBlank()) return false
        if (normalizedTitle.contains(normalizedQuery)) return true
        return normalizedTitle.length >= maxOf(4, (normalizedQuery.length * 0.65).toInt()) && normalizedQuery.contains(normalizedTitle)
    }

    private fun normalizeForMatch(value: String): String = stripHtml(value)
        .let(::normalizeDanmakuMatchText)

    private fun episodeNumberFromText(value: String): Int? = danmakuEpisodeNumberFromText(value)

    private fun stripHtml(value: String): String = htmlDecode(value.replace(Regex("""<[^>]+>"""), "")).trim()

    private fun htmlDecode(value: String): String = value
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")

    private fun enc(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8.name())

    private fun String.escapeJson(): String = replace("\\", "\\\\").replace("\"", "\\\"")

    private fun originFrom(referer: String): String {
        val match = Regex("""^(https?://[^/]+)""").find(referer)
        return match?.groupValues?.getOrNull(1) ?: "https://v.qq.com"
    }

    private fun JsonElement.asObject(): JsonObject? = this as? JsonObject
    private fun JsonObject.obj(name: String): JsonObject? = get(name) as? JsonObject
    private fun JsonObject.array(name: String): JsonArray? = get(name) as? JsonArray
    private fun JsonObject.string(name: String): String = (get(name) as? JsonPrimitive)?.contentOrNull.orEmpty()
    private fun JsonObject.objFromString(name: String): JsonObject? = parseJson(string(name))

    private fun modeFromStyle(style: JsonObject?): DanmakuMode = modeFromPosition(style?.string("position").orEmpty().ifBlank { style?.string("mode").orEmpty() })

    private fun modeFromPosition(value: String): DanmakuMode {
        val text = value.trim().lowercase()
        return when {
            text.contains("top") || text == "1" || text == "5" -> DanmakuMode.Top
            text.contains("bottom") || text == "2" || text == "4" -> DanmakuMode.Bottom
            else -> DanmakuMode.Scroll
        }
    }

    private fun colorFromStyle(style: JsonObject?): Long {
        val gradients = style?.get("gradient_colors") as? JsonArray
        val value = gradients?.firstOrNull()?.asText().orEmpty().ifBlank { style?.string("color").orEmpty() }
        return colorFromHex(value)
    }

    private fun colorFromHex(value: String): Long {
        val normalized = value.trim().removePrefix("#")
        return 0xFF000000 or ((normalized.toLongOrNull(16) ?: 0xFFFFFF) and 0xFFFFFF)
    }

    private fun JsonElement.asText(): String = (this as? JsonPrimitive)?.contentOrNull.orEmpty()

    private fun collectIqiyiAlbums(node: JsonElement?, target: MutableList<JsonObject>) {
        when (node) {
            is JsonArray -> node.forEach { collectIqiyiAlbums(it, target) }
            is JsonObject -> {
                val title = node.string("title")
                val pageUrl = node.string("pageUrl")
                if (title.isNotBlank() && pageUrl.contains("iqiyi.com")) target.add(node)
                node.values.forEach { collectIqiyiAlbums(it, target) }
            }
            else -> Unit
        }
    }

    private fun iqiyiMediaIdFromUrl(url: String): String = Regex("""v_([A-Za-z0-9]+)\.html""").find(url)?.groupValues?.getOrNull(1).orEmpty()

    private fun decodeCompressedText(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""
        return runCatching { java.util.zip.GZIPInputStream(bytes.inputStream()).reader().readText() }
            .getOrElse {
                runCatching { java.util.zip.InflaterInputStream(bytes.inputStream()).reader().readText() }
                    .getOrDefault(bytes.toString(StandardCharsets.UTF_8))
            }
    }

    private fun extractXml(xml: String, tag: String): List<String> {
        return Regex("<$tag>(.*?)</$tag>", RegexOption.DOT_MATCHES_ALL)
            .findAll(xml)
            .map { it.groupValues.getOrNull(1).orEmpty() }
            .toList()
    }

    private data class PlatformEpisode(val id: String, val order: Int, val title: String, val url: String? = null)
    private data class SearchCandidate(val id: String, val title: String, val subtitle: String, val score: Int)
    private data class SearchEpisodesCandidate(val title: String, val subtitle: String, val episodes: List<PlatformEpisode>, val score: Int)

    private companion object {
        const val UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }
}
