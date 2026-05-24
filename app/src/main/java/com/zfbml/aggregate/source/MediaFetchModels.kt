package com.zfbml.aggregate.source

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
            val aliases = buildList {
                add(episode.raw["subjectNameCn"])
                add(episode.raw["subjectName"])
                add(episode.raw["subjectTitle"])
                episode.raw["subjectAliases"]
                    ?.split("|")
                    ?.forEach { add(it) }
            }
                .filterNotNull()
                .map { it.trim() }
                .filter { it.length >= 2 }
                .distinct()

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
