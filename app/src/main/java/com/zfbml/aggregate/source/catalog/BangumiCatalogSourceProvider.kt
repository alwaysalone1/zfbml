package com.zfbml.aggregate.source.catalog

import com.zfbml.aggregate.source.DownloadPolicy
import com.zfbml.aggregate.source.Episode
import com.zfbml.aggregate.source.MediaDetail
import com.zfbml.aggregate.source.MediaFetchRequest
import com.zfbml.aggregate.source.MediaRouteResolver
import com.zfbml.aggregate.source.MediaStream
import com.zfbml.aggregate.source.SearchResult
import com.zfbml.aggregate.source.SourceCapability
import com.zfbml.aggregate.source.SourceManifest
import com.zfbml.aggregate.source.SourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class BangumiCatalogSourceProvider(
    private val client: OkHttpClient,
    private val resourceProviders: List<SourceProvider>,
) : SourceProvider {
    private val routeResolver = MediaRouteResolver(resourceProviders, RESOURCE_SEARCH_TIMEOUT_MS)

    override val manifest = SourceManifest(
        id = ID,
        name = "Bangumi Catalog",
        version = "0.1.0",
        author = "zfbml",
        capabilities = setOf(
            SourceCapability.SEARCH,
            SourceCapability.DETAIL,
            SourceCapability.EPISODES,
            SourceCapability.STREAM,
            SourceCapability.BITTORRENT,
        ),
        domains = setOf("api.bgm.tv", "bgm.tv"),
        supportsDownload = true,
    )

    override suspend fun search(query: String): List<SearchResult> {
        val trimmed = query.trim()
        if (
            trimmed.isBlank() ||
            trimmed.startsWith("magnet:", ignoreCase = true) ||
            trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            return emptyList()
        }

        val response = postJson<BangumiSearchResponse>(
            url = "$BASE_URL/search/subjects?limit=24&offset=0",
            body = BangumiSearchRequest(
                keyword = trimmed,
                sort = "match",
                filter = BangumiSearchFilter(type = listOf(2)),
            ),
        )
        return response.data
            .filter { it.type == null || it.type == 2 }
            .map { subject -> subject.toSearchResult() }
    }

    override suspend fun loadDetail(result: SearchResult): MediaDetail {
        val subjectId = result.subjectId()
        val subject = runCatching {
            getJson<BangumiSubjectDto>("$BASE_URL/subjects/$subjectId")
        }.getOrNull() ?: return fallbackDetail(result, subjectId)
        val episodes = fetchEpisodes(subject)
        return MediaDetail(
            providerId = manifest.id,
            title = subject.displayTitle().ifBlank { result.title },
            url = "bangumi://subject/$subjectId",
            summary = subject.summary?.trim().takeUnless { it.isNullOrBlank() } ?: result.raw["summary"],
            posterUrl = subject.posterUrl(),
            episodes = episodes.ifEmpty { fallbackEpisodes(subject, result) },
        )
    }

    override suspend fun resolveStreams(episode: Episode): List<MediaStream> {
        val request = MediaFetchRequest.fromEpisode(episode)
        if (request.subjectNames.isEmpty()) return emptyList()

        return routeResolver.resolve(request)
            .map { route ->
                route.stream.copy(
                    metadata = route.stream.metadata + mapOf(
                        "catalogProvider" to manifest.id,
                        "catalogEpisodeId" to episode.id,
                        "catalogTitle" to (episode.raw["subjectTitle"] ?: episode.title),
                        "catalogSubjectId" to episode.raw["subjectId"].orEmpty(),
                    ),
                )
            }
            .sortedByDescending { it.sourceScore }
    }

    private suspend fun fetchEpisodes(subject: BangumiSubjectDto): List<Episode> {
        val subjectId = subject.id
        val response = runCatching {
            getJson<BangumiEpisodeResponse>("$BASE_URL/episodes?subject_id=$subjectId&type=0&limit=100&offset=0")
        }.getOrNull() ?: return emptyList()

        return response.data.mapIndexed { index, item ->
            val episodeIndex = item.episodeIndex(index + 1)
            Episode(
                providerId = manifest.id,
                id = "bangumi-$subjectId-${item.id}",
                title = item.displayTitle(episodeIndex),
                url = "bangumi://subject/$subjectId/episode/${item.id}",
                index = episodeIndex,
                raw = mapOf(
                    "subjectId" to subjectId.toString(),
                    "subjectTitle" to subject.displayTitle(),
                    "subjectName" to subject.name,
                    "subjectNameCn" to subject.nameCn,
                    "subjectAliases" to subject.aliases().joinToString("|"),
                    "episodeTitle" to item.displayName(),
                    "episodeId" to item.id.toString(),
                    "expectedCount" to subject.episodeCount().orZeroString(),
                ),
            )
        }
    }

    private fun fallbackEpisodes(subject: BangumiSubjectDto, result: SearchResult): List<Episode> {
        val count = subject.episodeCount()?.coerceIn(1, 120) ?: 1
        val subjectId = subject.id
        return (1..count).map { index ->
            Episode(
                providerId = manifest.id,
                id = "bangumi-$subjectId-fallback-$index",
                title = "\u7b2c $index \u96c6",
                url = "bangumi://subject/$subjectId/episode/$index",
                index = index,
                raw = mapOf(
                    "subjectId" to subjectId.toString(),
                    "subjectTitle" to subject.displayTitle().ifBlank { result.title },
                    "subjectName" to subject.name,
                    "subjectNameCn" to subject.nameCn,
                    "subjectAliases" to subject.aliases().joinToString("|"),
                ),
            )
        }
    }

    private fun fallbackDetail(result: SearchResult, subjectId: Int): MediaDetail {
        return MediaDetail(
            providerId = manifest.id,
            title = result.title,
            url = result.url,
            summary = result.raw["summary"] ?: "\u6682\u65f6\u65e0\u6cd5\u8fde\u63a5 Bangumi \u83b7\u53d6\u5b8c\u6574\u7b80\u4ecb\uff0c\u5df2\u5148\u4f7f\u7528\u672c\u5730\u6761\u76ee\u4fe1\u606f\u4e3a\u4f60\u5339\u914d\u64ad\u653e\u7ebf\u8def\u3002",
            posterUrl = result.posterUrl ?: result.raw["posterUrl"],
            episodes = fallbackEpisodes(result, subjectId),
        )
    }

    private fun fallbackEpisodes(result: SearchResult, subjectId: Int): List<Episode> {
        val count = result.raw["episodeCount"]?.toIntOrNull()?.coerceIn(1, 120) ?: 12
        return (1..count).map { index ->
            Episode(
                providerId = manifest.id,
                id = "bangumi-$subjectId-offline-$index",
                title = "\u7b2c $index \u96c6",
                url = "bangumi://subject/$subjectId/episode/$index",
                index = index,
                raw = mapOf(
                    "subjectId" to subjectId.toString(),
                    "subjectTitle" to result.title,
                    "subjectName" to result.raw["subjectName"].orEmpty(),
                    "subjectNameCn" to (result.raw["subjectNameCn"] ?: result.title),
                    "subjectAliases" to result.raw["subjectAliases"].orEmpty(),
                ),
            )
        }
    }

    private suspend inline fun <reified T> getJson(url: String): T = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .build()
        client.newCall(request).execute().use { response ->
            require(response.isSuccessful) { "HTTP ${response.code} for $url" }
            json.decodeFromString<T>(response.body?.string().orEmpty())
        }
    }

    private suspend inline fun <reified T> postJson(url: String, body: BangumiSearchRequest): T = withContext(Dispatchers.IO) {
        val requestBody = json.encodeToString(body).toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .post(requestBody)
            .build()
        client.newCall(request).execute().use { response ->
            require(response.isSuccessful) { "HTTP ${response.code} for $url" }
            json.decodeFromString<T>(response.body?.string().orEmpty())
        }
    }

    private fun BangumiSubjectDto.toSearchResult(): SearchResult {
        val title = displayTitle()
        val subtitle = buildList {
            add("Bangumi")
            date?.takeIf(String::isNotBlank)?.let { add(it) }
            platform?.takeIf(String::isNotBlank)?.let { add(it) }
            rating?.score?.takeIf { it > 0.0 }?.let { add("%.1f".format(it)) }
        }.joinToString(" / ")
        return SearchResult(
            providerId = manifest.id,
            title = title,
            url = "bangumi://subject/$id",
            posterUrl = posterUrl(),
            subtitle = subtitle,
            raw = buildMap {
                put("subjectId", id.toString())
                put("subjectName", name)
                put("subjectNameCn", nameCn)
                aliases().takeIf { it.isNotEmpty() }?.let { put("subjectAliases", it.joinToString("|")) }
                summary?.takeIf(String::isNotBlank)?.let { put("summary", it) }
                date?.takeIf(String::isNotBlank)?.let { put("date", it) }
                posterUrl()?.let { put("posterUrl", it) }
                episodeCount()?.let { put("episodeCount", it.toString()) }
            },
        )
    }

    private fun SearchResult.subjectId(): Int {
        return raw["subjectId"]?.toIntOrNull()
            ?: url.substringAfterLast('/').toIntOrNull()
            ?: error("Bangumi subject id missing for $title")
    }

    private fun BangumiSubjectDto.displayTitle(): String = nameCn.ifBlank { name }

    private fun BangumiSubjectDto.aliases(): List<String> {
        return infobox
            .filter { item ->
                item.key.contains("\u522b\u540d", ignoreCase = true) ||
                    item.key.contains("alias", ignoreCase = true) ||
                    item.key.contains("\u53c8\u540d", ignoreCase = true) ||
                    item.key.contains("\u82f1\u6587\u540d", ignoreCase = true)
            }
            .flatMap { item -> item.value?.flattenStrings().orEmpty() }
            .map { it.trim() }
            .filter { it.length >= 2 }
            .filterNot { it == name || it == nameCn }
            .distinct()
    }

    private fun JsonElement.flattenStrings(): List<String> {
        return when (this) {
            is JsonPrimitive -> listOfNotNull(contentOrNull)
            is JsonArray -> flatMap { it.flattenStrings() }
            is JsonObject -> values.flatMap { it.flattenStrings() }
        }
    }

    private fun BangumiSubjectDto.posterUrl(): String? {
        return images?.large ?: images?.medium ?: images?.common ?: image ?: "$BASE_URL/subjects/$id/image?type=large"
    }

    private fun BangumiSubjectDto.episodeCount(): Int? {
        return totalEpisodes?.takeIf { it > 0 } ?: eps?.takeIf { it > 0 }
    }

    private fun BangumiEpisodeDto.episodeIndex(fallback: Int): Int {
        return (ep ?: sort)?.toInt()?.takeIf { it > 0 } ?: fallback
    }

    private fun BangumiEpisodeDto.displayName(): String = nameCn.ifBlank { name }

    private fun BangumiEpisodeDto.displayTitle(index: Int): String {
        val title = displayName()
        return if (title.isBlank()) {
            "\u7b2c $index \u96c6"
        } else {
            "\u7b2c $index \u96c6 $title"
        }
    }

    private fun Int?.orZeroString(): String = this?.toString() ?: "0"

    private companion object {
        const val ID = "bangumi-catalog"
        const val BASE_URL = "https://api.bgm.tv/v0"
        const val USER_AGENT = "ZFBML/0.2.10 (https://github.com/alwaysalone1/zfbml)"
        const val RESOURCE_SEARCH_TIMEOUT_MS = 5_000L
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
}

@Serializable
private data class BangumiSearchRequest(
    val keyword: String,
    val sort: String,
    val filter: BangumiSearchFilter,
)

@Serializable
private data class BangumiSearchFilter(
    val type: List<Int>,
)

@Serializable
private data class BangumiSearchResponse(
    val data: List<BangumiSubjectDto> = emptyList(),
)

@Serializable
private data class BangumiSubjectDto(
    val id: Int,
    val type: Int? = null,
    val name: String = "",
    @SerialName("name_cn") val nameCn: String = "",
    val summary: String? = null,
    val date: String? = null,
    val platform: String? = null,
    val images: BangumiImages? = null,
    val image: String? = null,
    val rating: BangumiRating? = null,
    val eps: Int? = null,
    @SerialName("total_episodes") val totalEpisodes: Int? = null,
    val infobox: List<BangumiInfoboxItem> = emptyList(),
)

@Serializable
private data class BangumiInfoboxItem(
    val key: String = "",
    val value: JsonElement? = null,
)

@Serializable
private data class BangumiImages(
    val large: String? = null,
    val medium: String? = null,
    val common: String? = null,
)

@Serializable
private data class BangumiRating(
    val score: Double? = null,
)

@Serializable
private data class BangumiEpisodeResponse(
    val data: List<BangumiEpisodeDto> = emptyList(),
) {
    val subjectTitle: String? = null
}

@Serializable
private data class BangumiEpisodeDto(
    val id: Int,
    val name: String = "",
    @SerialName("name_cn") val nameCn: String = "",
    val ep: Double? = null,
    val sort: Double? = null,
)
