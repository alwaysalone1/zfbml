package com.zfbml.aggregate.source.catalog

import com.zfbml.aggregate.source.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class BangumiCategoryRepository(
    private val client: OkHttpClient,
) {
    private val specs = listOf(
        BangumiCategorySpec(
            category = BangumiCategory(
                id = "recommend",
                title = "推荐",
                subtitle = "高排名、高评分、讨论稳定的番剧",
                badge = "BEST",
            ),
            sort = "rank",
            filter = BangumiCategoryFilter(
                type = listOf(2),
                rank = listOf("<=1000"),
                ratingCount = listOf(">=500"),
            ),
            localSort = CategoryLocalSort.Rank,
        ),
        BangumiCategorySpec(
            category = BangumiCategory(
                id = "japanese",
                title = "日漫",
                subtitle = "日本动画里热度最高的一批",
                badge = "JP",
            ),
            sort = "heat",
            filter = BangumiCategoryFilter(
                type = listOf(2),
                tag = listOf("日本"),
                ratingCount = listOf(">=500"),
            ),
        ),
        BangumiCategorySpec(
            category = BangumiCategory(
                id = "chinese",
                title = "国漫",
                subtitle = "国产/中国动画热门条目",
                badge = "CN",
            ),
            sort = "heat",
            filter = BangumiCategoryFilter(
                type = listOf(2),
                tag = listOf("中国"),
                ratingCount = listOf(">=20"),
            ),
        ),
        BangumiCategorySpec(
            category = BangumiCategory(
                id = "american",
                title = "美漫",
                subtitle = "美国动画和欧美向作品",
                badge = "US",
            ),
            sort = "heat",
            filter = BangumiCategoryFilter(
                type = listOf(2),
                tag = listOf("美国"),
                ratingCount = listOf(">=20"),
            ),
        ),
        BangumiCategorySpec(
            category = BangumiCategory(
                id = "movie",
                title = "剧场版",
                subtitle = "动画电影、剧场版和特别篇",
                badge = "MOV",
            ),
            sort = "rank",
            filter = BangumiCategoryFilter(
                type = listOf(2),
                tag = listOf("剧场版"),
                ratingCount = listOf(">=100"),
            ),
            localSort = CategoryLocalSort.Rank,
        ),
        BangumiCategorySpec(
            category = BangumiCategory(
                id = "hot",
                title = "热门",
                subtitle = "按 Bangumi 收藏热度排序",
                badge = "HOT",
            ),
            sort = "heat",
            filter = BangumiCategoryFilter(
                type = listOf(2),
                ratingCount = listOf(">=100"),
            ),
        ),
        BangumiCategorySpec(
            category = BangumiCategory(
                id = "high-score",
                title = "高分",
                subtitle = "评分 8.0+ 且评价人数充足",
                badge = "9+",
            ),
            sort = "score",
            filter = BangumiCategoryFilter(
                type = listOf(2),
                rating = listOf(">=8"),
                ratingCount = listOf(">=500"),
            ),
            localSort = CategoryLocalSort.Score,
        ),
        BangumiCategorySpec(
            category = BangumiCategory(
                id = "most-followed",
                title = "最多追番",
                subtitle = "按正在看的用户数排序",
                badge = "追",
            ),
            sort = "heat",
            filter = BangumiCategoryFilter(
                type = listOf(2),
                ratingCount = listOf(">=100"),
            ),
            localSort = CategoryLocalSort.Doing,
        ),
        BangumiCategorySpec(
            category = BangumiCategory(
                id = "most-watched",
                title = "最多观看",
                subtitle = "Bangumi 无播放量，按看过收藏近似",
                badge = "看",
            ),
            sort = "heat",
            filter = BangumiCategoryFilter(
                type = listOf(2),
                ratingCount = listOf(">=100"),
            ),
            localSort = CategoryLocalSort.Collect,
        ),
    )

    val categories: List<BangumiCategory> = specs.map { it.category }

    suspend fun loadCategory(categoryId: String, limit: Int = DEFAULT_LIMIT): BangumiCategoryResult {
        val spec = specs.firstOrNull { it.category.id == categoryId } ?: specs.first()
        val response = postSearch(
            BangumiCategorySearchRequest(
                keyword = spec.keyword,
                sort = spec.sort,
                filter = spec.filter,
            ),
            limit = limit.coerceIn(1, MAX_LIMIT),
        )
        val items = response.data
            .filter { it.type == null || it.type == 2 }
            .map { it.toSearchResult(spec.category) }
            .let { spec.localSort.apply(it) }

        return BangumiCategoryResult(
            category = spec.category,
            items = items,
        )
    }

    suspend fun loadHomeRecommendations(limit: Int = HOME_LIMIT): List<SearchResult> = coroutineScope {
        listOf("recommend", "high-score", "hot", "movie")
            .map { id ->
                async {
                    runCatching { loadCategory(id, limit = 8).items }
                        .getOrDefault(emptyList())
                }
            }
            .awaitAll()
            .flatten()
            .distinctBy { result -> result.raw["subjectId"] ?: result.url }
            .take(limit.coerceIn(1, MAX_LIMIT))
    }

    private suspend fun postSearch(
        body: BangumiCategorySearchRequest,
        limit: Int,
    ): BangumiCategorySearchResponse = withContext(Dispatchers.IO) {
        val requestBody = json.encodeToString(body).toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$BASE_URL/search/subjects?limit=$limit&offset=0")
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            require(response.isSuccessful) { "HTTP ${response.code} for Bangumi category" }
            json.decodeFromString(response.body?.string().orEmpty())
        }
    }

    private fun BangumiCategorySubjectDto.toSearchResult(category: BangumiCategory): SearchResult {
        val title = displayTitle()
        val subtitle = buildList {
            add(category.title)
            date?.takeIf(String::isNotBlank)?.let { add(it) }
            platform?.takeIf(String::isNotBlank)?.let { add(it) }
            rating?.score?.takeIf { it > 0.0 }?.let { add("%.1f".format(it)) }
            rank?.takeIf { it > 0 }?.let { add("Rank $it") }
        }.joinToString(" / ")

        return SearchResult(
            providerId = "bangumi-catalog",
            title = title,
            url = "bangumi://subject/$id",
            posterUrl = posterUrl(),
            subtitle = subtitle,
            raw = buildMap {
                put("subjectId", id.toString())
                put("subjectTitle", title)
                put("subjectName", name)
                put("subjectNameCn", nameCn.ifBlank { title })
                put("categoryId", category.id)
                put("categoryTitle", category.title)
                summary?.takeIf(String::isNotBlank)?.let { put("summary", it) }
                date?.takeIf(String::isNotBlank)?.let { put("date", it) }
                platform?.takeIf(String::isNotBlank)?.let { put("platform", it) }
                episodeCount()?.let { put("episodeCount", it.toString()) }
                rating?.score?.takeIf { it > 0.0 }?.let { put("rating", "%.1f".format(it)) }
                rating?.total?.takeIf { it > 0 }?.let { put("ratingCount", it.toString()) }
                rank?.takeIf { it > 0 }?.let { put("rank", it.toString()) }
                collection?.doing?.takeIf { it > 0 }?.let { put("doing", it.toString()) }
                collection?.collect?.takeIf { it > 0 }?.let { put("collect", it.toString()) }
                collection?.wish?.takeIf { it > 0 }?.let { put("wish", it.toString()) }
                posterUrl()?.let { put("posterUrl", it) }
            },
        )
    }

    private fun BangumiCategorySubjectDto.displayTitle(): String = nameCn.ifBlank { name }

    private fun BangumiCategorySubjectDto.posterUrl(): String? {
        return (images?.large ?: images?.common ?: images?.medium ?: image ?: "$BASE_URL/subjects/$id/image?type=large")
            .replace("http://", "https://")
    }

    private fun BangumiCategorySubjectDto.episodeCount(): Int? {
        return totalEpisodes?.takeIf { it > 0 } ?: eps?.takeIf { it > 0 }
    }

    private fun CategoryLocalSort.apply(items: List<SearchResult>): List<SearchResult> {
        return when (this) {
            CategoryLocalSort.Api -> items
            CategoryLocalSort.Rank -> items.sortedBy { it.raw["rank"]?.toIntOrNull() ?: Int.MAX_VALUE }
            CategoryLocalSort.Score -> items.sortedWith(compareByDescending<SearchResult> {
                it.raw["rating"]?.toDoubleOrNull() ?: 0.0
            }.thenBy { it.raw["rank"]?.toIntOrNull() ?: Int.MAX_VALUE })
            CategoryLocalSort.Doing -> items.sortedByDescending { it.raw["doing"]?.toIntOrNull() ?: 0 }
            CategoryLocalSort.Collect -> items.sortedByDescending { it.raw["collect"]?.toIntOrNull() ?: 0 }
        }
    }

    private companion object {
        const val BASE_URL = "https://api.bgm.tv/v0"
        const val USER_AGENT = "ZFBML/0.2.21 (https://github.com/alwaysalone1/zfbml)"
        const val DEFAULT_LIMIT = 24
        const val HOME_LIMIT = 12
        const val MAX_LIMIT = 50
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        @OptIn(ExperimentalSerializationApi::class)
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
            encodeDefaults = true
        }
    }
}

data class BangumiCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String,
)

data class BangumiCategoryResult(
    val category: BangumiCategory,
    val items: List<SearchResult>,
)

private data class BangumiCategorySpec(
    val category: BangumiCategory,
    val keyword: String = "",
    val sort: String,
    val filter: BangumiCategoryFilter,
    val localSort: CategoryLocalSort = CategoryLocalSort.Api,
)

private enum class CategoryLocalSort {
    Api,
    Rank,
    Score,
    Doing,
    Collect,
}

@Serializable
private data class BangumiCategorySearchRequest(
    val keyword: String,
    val sort: String,
    val filter: BangumiCategoryFilter,
)

@Serializable
private data class BangumiCategoryFilter(
    val type: List<Int>,
    @SerialName("meta_tags") val metaTags: List<String>? = null,
    val tag: List<String>? = null,
    @SerialName("air_date") val airDate: List<String>? = null,
    val rating: List<String>? = null,
    @SerialName("rating_count") val ratingCount: List<String>? = null,
    val rank: List<String>? = null,
    val nsfw: Boolean = false,
)

@Serializable
private data class BangumiCategorySearchResponse(
    val data: List<BangumiCategorySubjectDto> = emptyList(),
)

@Serializable
private data class BangumiCategorySubjectDto(
    val id: Int,
    val type: Int? = null,
    val name: String = "",
    @SerialName("name_cn") val nameCn: String = "",
    val summary: String? = null,
    val date: String? = null,
    val platform: String? = null,
    val images: BangumiCategoryImagesDto? = null,
    val image: String? = null,
    val rating: BangumiCategoryRatingDto? = null,
    val rank: Int? = null,
    val collection: BangumiCategoryCollectionDto? = null,
    val eps: Int? = null,
    @SerialName("total_episodes") val totalEpisodes: Int? = null,
)

@Serializable
private data class BangumiCategoryImagesDto(
    val large: String? = null,
    val common: String? = null,
    val medium: String? = null,
    val small: String? = null,
    val grid: String? = null,
)

@Serializable
private data class BangumiCategoryRatingDto(
    val score: Double? = null,
    val total: Int? = null,
)

@Serializable
private data class BangumiCategoryCollectionDto(
    val wish: Int? = null,
    val collect: Int? = null,
    val doing: Int? = null,
    @SerialName("on_hold") val onHold: Int? = null,
    val dropped: Int? = null,
)
