package com.zfbml.aggregate.danmaku

import android.content.Context
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DanmakuManualMappingStore(
    private val file: File,
    private val json: Json = DefaultJson,
) {
    suspend fun load(): List<DanmakuManualMapping> = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext emptyList()
        runCatching {
            json.decodeFromString<List<DanmakuManualMapping>>(file.readText(Charsets.UTF_8))
        }.getOrDefault(emptyList())
    }

    suspend fun save(mappings: List<DanmakuManualMapping>) {
        withContext(Dispatchers.IO) {
            val target = file.absoluteFile
            target.parentFile?.mkdirs()
            val temp = File(target.parentFile, "${target.name}.tmp")
            try {
                temp.writeText(json.encodeToString(mappings), Charsets.UTF_8)
                temp.copyTo(target, overwrite = true)
            } finally {
                temp.delete()
            }
        }
    }

    suspend fun addOrReplace(mapping: DanmakuManualMapping): List<DanmakuManualMapping> {
        val next = load()
            .filterNot { it.sameManualTarget(mapping) }
            .plus(mapping)
        save(next)
        return next
    }

    companion object {
        private const val FILE_NAME = "danmaku_manual_mappings.json"
        private val DefaultJson = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        fun fromContext(context: Context): DanmakuManualMappingStore {
            return DanmakuManualMappingStore(File(context.filesDir, FILE_NAME))
        }
    }
}

private fun DanmakuManualMapping.sameManualTarget(other: DanmakuManualMapping): Boolean {
    return normalizedManualTitle(detailTitle) == normalizedManualTitle(other.detailTitle) &&
        detailProviderId == other.detailProviderId &&
        detailUrl == other.detailUrl &&
        episodeId == other.episodeId &&
        episodeIndex == other.episodeIndex
}

private fun normalizedManualTitle(value: String): String {
    return value
        .lowercase()
        .replace(Regex("""[\s\p{P}\p{S}]"""), "")
}
