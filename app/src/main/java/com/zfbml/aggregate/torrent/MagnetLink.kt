package com.zfbml.aggregate.torrent

import java.net.URLDecoder
import java.net.URLEncoder

data class MagnetLink(
    val uri: String,
    val infoHash: String?,
    val displayName: String?,
    val trackers: List<String>,
) {
    companion object {
        fun parse(value: String): MagnetLink? {
            val trimmed = value.trim()
            if (!trimmed.startsWith("magnet:?", ignoreCase = true)) return null
            val params = trimmed.substringAfter('?')
                .split('&')
                .mapNotNull { part ->
                    val key = part.substringBefore('=', missingDelimiterValue = "").takeIf(String::isNotBlank)
                    val rawValue = part.substringAfter('=', missingDelimiterValue = "")
                    key?.let { it to rawValue.urlDecode() }
                }
            val xtValues = params.filter { it.first == "xt" }.map { it.second }
            val infoHash = xtValues.firstNotNullOfOrNull { xt ->
                when {
                    xt.startsWith("urn:btih:", ignoreCase = true) -> xt.substringAfterLast(':')
                    else -> null
                }
            }
            return MagnetLink(
                uri = trimmed,
                infoHash = infoHash,
                displayName = params.firstOrNull { it.first == "dn" }?.second,
                trackers = params.filter { it.first == "tr" }.map { it.second }.distinct(),
            )
        }

        fun build(infoHash: String, displayName: String? = null, trackers: List<String> = emptyList()): String {
            val params = buildList {
                add("xt=urn:btih:${infoHash.trim()}")
                displayName?.takeIf(String::isNotBlank)?.let { add("dn=${it.urlEncode()}") }
                trackers.filter { it.isNotBlank() }.forEach { add("tr=${it.urlEncode()}") }
            }
            return "magnet:?${params.joinToString("&")}"
        }
    }
}

private fun String.urlDecode(): String = URLDecoder.decode(this, Charsets.UTF_8.name())

private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())
