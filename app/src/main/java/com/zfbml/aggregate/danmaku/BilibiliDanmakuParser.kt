package com.zfbml.aggregate.danmaku

object BilibiliDanmakuParser {
    private val danmakuRegex = Regex(
        pattern = """<d\s+[^>]*p="([^"]*)"[^>]*>(.*?)</d>""",
        options = setOf(RegexOption.DOT_MATCHES_ALL),
    )

    fun parseXml(xml: String): List<DanmakuItem> {
        return danmakuRegex.findAll(xml).mapNotNull { match ->
            val p = match.groupValues[1].split(',')
            if (p.size < 8) return@mapNotNull null
            val text = match.groupValues[2].xmlUnescape().trim()
            if (text.isBlank()) return@mapNotNull null
            DanmakuItem(
                timeMs = (p[0].toDoubleOrNull() ?: 0.0).times(1000).toLong(),
                mode = mapMode(p[1].toIntOrNull() ?: 1),
                fontSizeSp = p[2].toFloatOrNull() ?: 24f,
                color = 0xFF000000 or (p[3].toLongOrNull() ?: 0xFFFFFF),
                userHash = p[6].ifBlank { null },
                platform = DanmakuPlatform.Bilibili,
                text = text,
                rawStyle = mapOf(
                    "mode" to p[1],
                    "pool" to p[5],
                    "rowId" to p[7],
                ),
            )
        }.sortedBy { it.timeMs }.toList()
    }

    private fun mapMode(mode: Int): DanmakuMode = when (mode) {
        1, 2, 3 -> DanmakuMode.Scroll
        4 -> DanmakuMode.Bottom
        5 -> DanmakuMode.Top
        6 -> DanmakuMode.Reverse
        7 -> DanmakuMode.Advanced
        8 -> DanmakuMode.Script
        else -> DanmakuMode.Scroll
    }
}

private fun String.xmlUnescape(): String {
    return replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&amp;", "&")
}
