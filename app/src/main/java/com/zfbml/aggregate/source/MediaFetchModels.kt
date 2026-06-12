package com.zfbml.aggregate.source

private val mediaAliasStrongDelimiterRegex = Regex("""[\r\n|;\uFF1B\u3001\uFF0C]+""")
private val mediaAliasSoftDelimiterRegex = Regex("""(?:\s+/\s+|\s*,\s+)""")

internal fun splitMediaAliasText(value: String?): List<String> {
    if (value.isNullOrBlank()) return emptyList()
    return value
        .split(mediaAliasStrongDelimiterRegex)
        .flatMap { segment -> segment.split(mediaAliasSoftDelimiterRegex) }
        .map { it.trim() }
        .filter { it.length >= 2 }
}

internal fun String.containsMediaCjkText(): Boolean {
    return any { char ->
        char in '\u4e00'..'\u9fff' ||
            char in '\u3040'..'\u30ff' ||
            char in '\u3400'..'\u4dbf'
    }
}

data class MediaFetchRequest(
    val subjectId: String?,
    val subjectNames: List<String>,
    val subjectNameCn: String?,
    val episodeId: String?,
    val episodeIndex: Int?,
    val episodeTitle: String?,
    val sourceEpisode: Episode,
) {
    companion object {
        fun fromEpisode(episode: Episode): MediaFetchRequest {
            val distinctAliases = buildList {
                add(episode.raw["subjectNameCn"])
                add(episode.raw["subjectTitle"])
                addAll(splitMediaAliasText(episode.raw["subjectAliases"]))
                add(episode.raw["subjectName"])
            }
                .filterNotNull()
                .map { it.trim() }
                .filter { it.length >= 2 }
                .distinctBy { it.normalizedAliasKey() }
            val cjkAliases = distinctAliases.filter { it.containsMediaCjkText() }
            val nonCjkAliases = distinctAliases.filterNot { it.containsMediaCjkText() }
            val aliases = (cjkAliases + nonCjkAliases).ifEmpty { distinctAliases }

            return MediaFetchRequest(
                subjectId = episode.raw["subjectId"],
                subjectNames = aliases,
                subjectNameCn = episode.raw["subjectNameCn"],
                episodeId = episode.raw["episodeId"],
                episodeIndex = episode.index,
                episodeTitle = episode.raw["episodeTitle"] ?: episode.title,
                sourceEpisode = episode,
            )
        }

        private fun String.normalizedAliasKey(): String {
            return lowercase()
                .replace(Regex("""[\[\]【】()（）:：!！?？.,，。~～_\-\s]+"""), "")
        }
    }
}

data class RouteCandidate(
    val stream: MediaStream,
    val sourceId: String,
    val sourceName: String,
    val title: String,
    val routeName: String?,
    val episodeTitle: String?,
    val episodeIndex: Int?,
    val quality: String?,
    val subgroup: String?,
    val protocol: StreamProtocol,
    val score: Int,
    val publishedAt: String?,
    val sizeBytes: Long?,
)
