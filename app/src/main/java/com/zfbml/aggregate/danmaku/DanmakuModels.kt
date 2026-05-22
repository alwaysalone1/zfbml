package com.zfbml.aggregate.danmaku

data class DanmakuItem(
    val timeMs: Long,
    val text: String,
    val mode: DanmakuMode,
    val color: Long = 0xFFFFFFFF,
    val fontSizeSp: Float = 24f,
    val position: DanmakuPosition? = null,
    val userHash: String? = null,
    val platform: DanmakuPlatform,
    val rawStyle: Map<String, String> = emptyMap(),
)

enum class DanmakuMode {
    Scroll,
    Top,
    Bottom,
    Reverse,
    Advanced,
    Script,
}

data class DanmakuPosition(
    val x: Float,
    val y: Float,
    val durationMs: Long,
)

enum class DanmakuPlatform {
    Bilibili,
    Tencent,
    Iqiyi,
    Youku,
    Local,
}

data class DanmakuProfile(
    val platform: DanmakuPlatform,
    val fontScale: Float = 1f,
    val strokeWidthPx: Float = 4f,
    val shadowRadiusPx: Float = 4f,
    val scrollDurationMs: Long = 8_500,
    val topDurationMs: Long = 4_500,
    val bottomDurationMs: Long = 4_500,
    val maxTracks: Int = 12,
    val maxItemsPerMinute: Int = 1_000,
    val supportsAdvanced: Boolean = false,
)

data class DanmakuSettings(
    val enabled: Boolean = true,
    val alpha: Float = 0.9f,
    val density: Float = 1f,
    val fontScale: Float = 1f,
    val blockedWords: Set<String> = emptySet(),
)

data class DanmakuMatch(
    val providerId: String,
    val platform: DanmakuPlatform,
    val title: String,
    val episodeTitle: String? = null,
    val score: Int,
    val token: String,
)
