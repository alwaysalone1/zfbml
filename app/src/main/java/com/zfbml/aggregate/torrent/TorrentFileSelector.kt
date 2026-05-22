package com.zfbml.aggregate.torrent

object TorrentFileSelector {
    private val videoExtensions = setOf(
        "3gp",
        "avi",
        "flv",
        "m2ts",
        "m4v",
        "mkv",
        "mov",
        "mp4",
        "mpeg",
        "mpg",
        "ts",
        "webm",
        "wmv",
    )

    fun choose(files: List<TorrentFileCandidate>): TorrentFileCandidate? {
        return files
            .filter { it.sizeBytes > 0L }
            .sortedWith(
                compareByDescending<TorrentFileCandidate> { it.extension() in videoExtensions }
                    .thenByDescending { it.sizeBytes },
            )
            .firstOrNull()
    }

    private fun TorrentFileCandidate.extension(): String {
        return name.substringAfterLast('.', missingDelimiterValue = "").lowercase()
    }
}
