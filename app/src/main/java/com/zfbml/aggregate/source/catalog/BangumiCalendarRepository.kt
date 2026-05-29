package com.zfbml.aggregate.source.catalog

import com.zfbml.aggregate.source.SearchResult
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class BangumiCalendarRepository(
    private val client: OkHttpClient,
) {
    suspend fun loadWeeklySchedule(): List<BangumiScheduleDay> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(CALENDAR_URL)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .build()
        client.newCall(request).execute().use { response ->
            require(response.isSuccessful) { "HTTP ${response.code} for $CALENDAR_URL" }
            json.decodeFromString<List<BangumiCalendarDayDto>>(response.body?.string().orEmpty())
                .map { it.toScheduleDay() }
                .sortedBy { it.weekdayId }
        }
    }

    companion object {
        fun currentBangumiWeekdayId(): Int {
            return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                else -> 7
            }
        }

        private const val CALENDAR_URL = "https://api.bgm.tv/calendar"
        private const val USER_AGENT = "ZFBML/0.2.19 (https://github.com/alwaysalone1/zfbml)"
        private val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
}

data class BangumiScheduleDay(
    val weekdayId: Int,
    val weekdayCn: String,
    val weekdayEn: String,
    val items: List<SearchResult>,
)

@Serializable
private data class BangumiCalendarDayDto(
    val weekday: BangumiCalendarWeekdayDto = BangumiCalendarWeekdayDto(),
    val items: List<BangumiCalendarItemDto> = emptyList(),
) {
    fun toScheduleDay(): BangumiScheduleDay {
        return BangumiScheduleDay(
            weekdayId = weekday.id,
            weekdayCn = weekday.cn.ifBlank { "星期${weekday.id}" },
            weekdayEn = weekday.en,
            items = items
                .filter { it.type == null || it.type == 2 }
                .sortedWith(compareByDescending<BangumiCalendarItemDto> { it.collection?.doing ?: 0 }.thenBy { it.displayTitle() })
                .map { it.toSearchResult(weekday) },
        )
    }
}

@Serializable
private data class BangumiCalendarWeekdayDto(
    val en: String = "",
    val cn: String = "",
    val ja: String = "",
    val id: Int = 1,
)

@Serializable
private data class BangumiCalendarItemDto(
    val id: Int,
    val url: String? = null,
    val type: Int? = null,
    val name: String = "",
    @SerialName("name_cn") val nameCn: String = "",
    val summary: String? = null,
    @SerialName("air_date") val airDate: String? = null,
    @SerialName("air_weekday") val airWeekday: Int? = null,
    val images: BangumiCalendarImagesDto? = null,
    val rating: BangumiCalendarRatingDto? = null,
    val rank: Int? = null,
    val collection: BangumiCalendarCollectionDto? = null,
) {
    fun toSearchResult(weekday: BangumiCalendarWeekdayDto): SearchResult {
        val title = displayTitle()
        val subtitle = buildList {
            add(weekday.cn.ifBlank { "每日放送" })
            airDate?.takeIf(String::isNotBlank)?.let { add(it) }
            rating?.score?.takeIf { it > 0.0 }?.let { add("%.1f".format(it)) }
            collection?.doing?.takeIf { it > 0 }?.let { add("${it} 在看") }
        }.joinToString(" / ")
        return SearchResult(
            providerId = "bangumi-catalog",
            title = title,
            url = "bangumi://subject/$id",
            posterUrl = images?.posterUrl(),
            subtitle = subtitle,
            raw = buildMap {
                put("subjectId", id.toString())
                put("subjectTitle", title)
                put("subjectName", name)
                put("subjectNameCn", nameCn.ifBlank { title })
                summary?.takeIf(String::isNotBlank)?.let { put("summary", it) }
                airDate?.takeIf(String::isNotBlank)?.let { put("date", it) }
                put("airWeekday", (airWeekday ?: weekday.id).toString())
                rank?.let { put("rank", it.toString()) }
                rating?.score?.takeIf { it > 0.0 }?.let { put("rating", "%.1f".format(it)) }
                collection?.doing?.takeIf { it > 0 }?.let { put("doing", it.toString()) }
                images?.posterUrl()?.let { put("posterUrl", it) }
            },
        )
    }

    fun displayTitle(): String = nameCn.ifBlank { name }
}

@Serializable
private data class BangumiCalendarImagesDto(
    val large: String? = null,
    val common: String? = null,
    val medium: String? = null,
    val small: String? = null,
    val grid: String? = null,
) {
    fun posterUrl(): String? {
        return (large ?: common ?: medium ?: grid ?: small)?.replace("http://", "https://")
    }
}

@Serializable
private data class BangumiCalendarRatingDto(
    val score: Double? = null,
)

@Serializable
private data class BangumiCalendarCollectionDto(
    val doing: Int? = null,
)
