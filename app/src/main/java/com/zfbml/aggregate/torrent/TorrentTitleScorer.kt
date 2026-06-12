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
            Regex("""(?i)\bS\d{1,2}\s*E\s*(\d{1,3})\b"""),
            Regex("""(?i)\bSeason\s*\d{1,2}\s*(?:Episode|Ep\.?)\s*(\d{1,3})\b"""),
            Regex("""第\s*[零〇一二三四五六七八九十百两\d]{1,8}\s*[季部期]\s*第?\s*([零〇一二三四五六七八九十百两\d]{1,8})\s*[话話集]"""),
            Regex("""\[(\d{1,3})]"""),
            Regex("""(?i)\b(?:Episode|Ep\.?|E)\s*(\d{1,3})\b"""),
            Regex("""[-#]\s*(\d{1,3})\b"""),
            Regex("""第\s*([零〇一二三四五六七八九十百两\d]{1,8})\s*[话話集]"""),
        )
        return patterns.firstNotNullOfOrNull { regex ->
            regex.find(title)?.groupValues?.getOrNull(1)?.let(::parseEpisodeToken)
        }
    }

    private fun parseEpisodeToken(value: String): Int? {
        val clean = value.trim().trimStart('0')
        if (clean.isBlank()) return 0
        clean.toIntOrNull()?.let { return it }
        return parseChineseNumber(clean)
    }

    private fun parseChineseNumber(value: String): Int? {
        if (value.none { it == '十' || it == '百' }) {
            val digits = value.map { chineseDigitValue(it) ?: return null }
            return digits.joinToString("").toIntOrNull()
        }
        var total = 0
        var current = 0
        var hasValue = false
        value.forEach { char ->
            when (char) {
                '百' -> {
                    total += (current.takeIf { it > 0 } ?: 1) * 100
                    current = 0
                    hasValue = true
                }
                '十' -> {
                    total += (current.takeIf { it > 0 } ?: 1) * 10
                    current = 0
                    hasValue = true
                }
                else -> {
                    val digit = chineseDigitValue(char) ?: return null
                    current = current * 10 + digit
                    hasValue = true
                }
            }
        }
        return if (hasValue) total + current else null
    }

    private fun chineseDigitValue(char: Char): Int? {
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

    private fun String.normalizeForScore(): String {
        return lowercase()
            .replace(Regex("""[\[\]【】()（）._\-]+"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }
}
