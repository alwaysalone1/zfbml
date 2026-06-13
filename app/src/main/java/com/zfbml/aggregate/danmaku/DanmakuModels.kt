package com.zfbml.aggregate.danmaku

import kotlinx.serialization.Serializable

internal const val DANMAKU_AUTOMATIC_TIMELINE_MIN_SCORE = 60
internal const val PLAYER_DANMAKU_ENABLED_DEFAULT = true
internal const val PLAYER_DANMAKU_DENSITY_DEFAULT = 0.32f
internal const val PLAYER_DANMAKU_ALPHA_DEFAULT = 0.76f
internal const val PLAYER_DANMAKU_FONT_SCALE_DEFAULT = 0.72f

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

@Serializable
enum class DanmakuPlatform {
    Bilibili,
    Tencent,
    Iqiyi,
    Youku,
    Local,
}

enum class DanmakuEffectStyle {
    PlatformAdaptive,
    ClassicStroke,
    CinemaGlow,
    HighContrast,
    Lightweight,
}

internal fun DanmakuEffectStyle.toPreferenceValue(): String = name

internal fun danmakuEffectStyleFromPreference(value: String?): DanmakuEffectStyle {
    return value
        ?.let { stored -> DanmakuEffectStyle.entries.firstOrNull { it.name == stored } }
        ?: DanmakuEffectStyle.PlatformAdaptive
}

internal fun normalizePlayerDanmakuDensityPreference(value: Float): Float {
    return value.finiteOr(PLAYER_DANMAKU_DENSITY_DEFAULT).coerceIn(0.3f, 1f)
}

internal fun normalizePlayerDanmakuAlphaPreference(value: Float): Float {
    return value.finiteOr(PLAYER_DANMAKU_ALPHA_DEFAULT).coerceIn(0.35f, 1f)
}

internal fun normalizePlayerDanmakuFontScalePreference(value: Float): Float {
    return value.finiteOr(PLAYER_DANMAKU_FONT_SCALE_DEFAULT).coerceIn(0.62f, 1.08f)
}

private fun Float.finiteOr(defaultValue: Float): Float {
    return if (isFinite()) this else defaultValue
}

data class DanmakuProfile(
    val platform: DanmakuPlatform,
    val fontScale: Float = 1f,
    val strokeWidthPx: Float = 4f,
    val shadowRadiusPx: Float = 4f,
    val scrollDurationMs: Long = 8_500,
    val topDurationMs: Long = 4_500,
    val bottomDurationMs: Long = 4_500,
    val maxTracks: Int = 18,
    val maxItemsPerMinute: Int = 1_000,
    val supportsAdvanced: Boolean = false,
)

data class DanmakuSettings(
    val enabled: Boolean = true,
    val alpha: Float = 0.9f,
    val density: Float = 1f,
    val fontScale: Float = 1f,
    val effectStyle: DanmakuEffectStyle = DanmakuEffectStyle.PlatformAdaptive,
    val blockedWords: Set<String> = emptySet(),
)

data class DanmakuSafeArea(
    val topInsetPx: Float = 0f,
    val bottomInsetPx: Float = 0f,
    val startInsetPx: Float = 0f,
    val endInsetPx: Float = 0f,
    val centerExcludedHeightPx: Float = 0f,
)

@Serializable
data class DanmakuMatch(
    val providerId: String,
    val platform: DanmakuPlatform,
    val title: String,
    val episodeTitle: String? = null,
    val score: Int,
    val token: String,
    val source: DanmakuMatchSource = DanmakuMatchSource.Automatic,
)

@Serializable
data class DanmakuManualMapping(
    val detailTitle: String,
    val detailProviderId: String? = null,
    val detailUrl: String? = null,
    val episodeId: String? = null,
    val episodeIndex: Int? = null,
    val match: DanmakuMatch,
)

@Serializable
enum class DanmakuMatchSource {
    Automatic,
    Manual,
}
