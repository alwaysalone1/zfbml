package com.zfbml.aggregate.torrent

object TorrentTitleScorer {
    private val qualityScores = mapOf(
        "2160" to 40,
        "4k" to 40,
        "1080" to 30,
        "720" to 20,
        "bdrip" to 15,
        "web-dl" to 12,
        "webrip" to 10,
    )

    fun score(query: String, candidate: TorrentCandidate): Int {
        val normalizedQuery = query.normalizeForScore()
        val normalizedTitle = candidate.title.normalizeForScore()
        var score = 50
        if (normalizedQuery.isNotBlank() && normalizedTitle.contains(normalizedQuery)) score += 40
        candidate.quality?.let { score += qualityScores.entries.firstOrNull { entry ->
            it.contains(entry.key, ignoreCase = true)
        }?.value ?: 0 }
        qualityScores.forEach { (token, value) ->
            if (normalizedTitle.contains(token)) score += value
        }
        candidate.seeders?.let { score += it.coerceAtMost(200) / 10 }
        candidate.sizeBytes?.let { bytes ->
            if (bytes > 200L * 1024 * 1024) score += 8
        }
        return score.coerceIn(0, 200)
    }

    fun extractQuality(title: String): String? {
        val lower = title.lowercase()
        return when {
            "2160" in lower || "4k" in lower -> "2160p"
            "1080" in lower -> "1080p"
            "720" in lower -> "720p"
            else -> null
        }
    }

    fun extractEpisode(title: String): Int? {
        val patterns = listOf(
            Regex("""\[(\d{1,3})]"""),
            Regex("""\b[Ee][Pp]?\.?\s*(\d{1,3})\b"""),
            Regex("""[-#]\s*(\d{1,3})\b"""),
            Regex("\u7b2c\\s*(\\d{1,3})\\s*[\u8bdd\u8a71\u96c6]"),
            Regex("""第\s*(\d{1,3})\s*[话話集]"""),
        )
        return patterns.firstNotNullOfOrNull { regex ->
            regex.find(title)?.groupValues?.getOrNull(1)?.toIntOrNull()
        }
    }

    private fun String.normalizeForScore(): String {
        return lowercase()
            .replace(Regex("""[\[\]【】()（）._\-]+"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }
}
