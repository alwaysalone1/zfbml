package com.zfbml.aggregate.danmaku

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

internal data class DanmakuEffectPaintStyle(
    val strokeWidthPx: Float,
    val shadowRadiusPx: Float,
    val shadowOffsetXPx: Float,
    val shadowOffsetYPx: Float,
    val shadowColor: Int,
    val strokeAlpha: Float,
)

@Composable
fun DanmakuSurface(
    items: List<DanmakuItem>,
    playbackMsProvider: () -> Long,
    profile: DanmakuProfile,
    settings: DanmakuSettings,
    isPlaying: Boolean,
    playbackSpeed: Float,
    modifier: Modifier = Modifier,
    safeArea: DanmakuSafeArea = DanmakuSafeArea(),
) {
    val layoutEngine = remember { DanmakuLayoutEngine() }
    val layoutCache = remember { DanmakuSurfaceLayoutCache() }
    val playbackClock = remember { DanmakuPlaybackClock() }
    val fillPaint = rememberTextPaint()
    val measurePaint = rememberTextPaint()
    val paintCache = remember { DanmakuPaintCache() }
    val strokePaint = rememberTextPaint().apply {
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
    }
    val currentPlaybackMsProvider by rememberUpdatedState(playbackMsProvider)
    val density = LocalDensity.current
    val frameSnapshot = remember { DanmakuFrameSnapshot(playbackMsProvider().coerceAtLeast(0L)) }
    var frameTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(settings.enabled, items.isNotEmpty(), isPlaying) {
        val frameDelayMs = danmakuFrameDelayMs(
            enabled = settings.enabled,
            hasItems = items.isNotEmpty(),
            isPlaying = isPlaying,
        ) ?: return@LaunchedEffect
        while (true) {
            val nextFrameTimeNs = withFrameNanos { it }
            frameTick = frameSnapshot.capture(
                frameTimeNs = nextFrameTimeNs,
                sampledPlaybackMs = currentPlaybackMsProvider(),
            )
            if (frameDelayMs > 0L) {
                delay(frameDelayMs)
            }
        }
    }
    LaunchedEffect(items, profile, settings.enabled, settings.density, settings.fontScale, settings.blockedWords) {
        frameTick = frameSnapshot.reset(currentPlaybackMsProvider())
        playbackClock.reset()
    }

    Canvas(modifier = modifier) {
        val currentFrameTick = frameTick
        val currentFrameTimeNs = frameSnapshot.frameTimeNs
        if (currentFrameTick == 0L || !danmakuFrameTimeReady(currentFrameTimeNs)) return@Canvas
        val playbackMs = playbackClock.positionMs(
            sampledPlaybackMs = frameSnapshot.sampledPlaybackMs,
            frameTimeNs = currentFrameTimeNs,
            isPlaying = isPlaying,
            playbackSpeed = playbackSpeed,
        )
        val preparedLayout = layoutCache.layoutFor(
            items = items,
            widthPx = size.width,
            heightPx = size.height,
            profile = profile,
            settings = settings,
            densityKey = density.density,
            safeArea = safeArea,
        ) {
            layoutEngine.prepare(
                items = items,
                widthPx = size.width,
                heightPx = size.height,
                profile = profile,
                settings = settings,
                safeArea = safeArea,
            ) { item ->
                val textSizePx = with(density) {
                    (item.fontSizeSp * profile.fontScale * settings.fontScale).sp.toPx()
                }
                measurePaint.textSize = textSizePx
                val fontMetrics = measurePaint.fontMetrics
                val rawLineHeight = fontMetrics.descent - fontMetrics.ascent
                DanmakuTextMetrics(
                    textSizePx = textSizePx,
                    widthPx = measurePaint.measureText(item.text).coerceAtLeast(1f),
                    lineHeightPx = (rawLineHeight * 1.18f).coerceAtLeast(textSizePx * 1.25f),
                    baselineOffsetPx = (-fontMetrics.ascent + rawLineHeight * 0.08f).coerceAtLeast(1f),
                )
            }
        }
        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas
            preparedLayout.forEachVisible(playbackMs, settings.alpha) { entry, x, entryAlpha ->
                val effectPaintStyle = danmakuEffectPaintStyle(
                    effectStyle = settings.effectStyle,
                    platform = entry.item.platform,
                    profile = profile,
                )
                val metrics = entry.metrics
                paintCache.applyFrameStyle(
                    fillPaint = fillPaint,
                    strokePaint = strokePaint,
                    effectPaintStyle = effectPaintStyle,
                )
                paintCache.applyEntryStyle(
                    fillPaint = fillPaint,
                    strokePaint = strokePaint,
                    textSizePx = metrics.textSizePx,
                    fillColor = danmakuFillColor(entry.item.color, entryAlpha),
                    strokeColor = danmakuStrokeColor(entryAlpha, effectPaintStyle.strokeAlpha),
                )
                if (effectPaintStyle.strokeWidthPx > 0f && effectPaintStyle.strokeAlpha > 0f) {
                    native.drawText(entry.item.text, x, entry.y, strokePaint)
                }
                native.drawText(entry.item.text, x, entry.y, fillPaint)
            }
        }
    }
}

internal class DanmakuFrameSnapshot(initialPlaybackMs: Long) {
    var sampledPlaybackMs: Long = initialPlaybackMs.coerceAtLeast(0L)
        private set
    var frameTimeNs: Long = DanmakuFrameTimeUnsetNs
        private set
    private var tick: Long = 0L

    fun capture(frameTimeNs: Long, sampledPlaybackMs: Long): Long {
        this.frameTimeNs = frameTimeNs
        this.sampledPlaybackMs = sampledPlaybackMs.coerceAtLeast(0L)
        return nextTick()
    }

    fun reset(sampledPlaybackMs: Long): Long {
        this.frameTimeNs = DanmakuFrameTimeUnsetNs
        this.sampledPlaybackMs = sampledPlaybackMs.coerceAtLeast(0L)
        return nextTick()
    }

    private fun nextTick(): Long {
        tick = if (tick == Long.MAX_VALUE) 1L else tick + 1L
        return tick
    }
}

private class DanmakuPaintCache {
    private var strokeWidthPx = Float.NaN
    private var shadowRadiusPx = Float.NaN
    private var shadowOffsetXPx = Float.NaN
    private var shadowOffsetYPx = Float.NaN
    private var shadowColor: Int? = null
    private var textSizePx = Float.NaN
    private var fillColor: Int? = null
    private var strokeColor: Int? = null

    fun applyFrameStyle(
        fillPaint: Paint,
        strokePaint: Paint,
        effectPaintStyle: DanmakuEffectPaintStyle,
    ) {
        val safeStrokeWidth = effectPaintStyle.strokeWidthPx.coerceFiniteAtLeast(0f)
        if (this.strokeWidthPx != safeStrokeWidth) {
            strokePaint.strokeWidth = safeStrokeWidth
            this.strokeWidthPx = safeStrokeWidth
        }
        val safeShadowRadius = effectPaintStyle.shadowRadiusPx.coerceFiniteAtLeast(0f)
        val safeShadowOffsetX = effectPaintStyle.shadowOffsetXPx.coerceFiniteAtLeast(0f)
        val safeShadowOffsetY = effectPaintStyle.shadowOffsetYPx.coerceFiniteAtLeast(0f)
        if (
            this.shadowRadiusPx != safeShadowRadius ||
            this.shadowOffsetXPx != safeShadowOffsetX ||
            this.shadowOffsetYPx != safeShadowOffsetY ||
            this.shadowColor != effectPaintStyle.shadowColor
        ) {
            fillPaint.setShadowLayer(
                safeShadowRadius,
                safeShadowOffsetX,
                safeShadowOffsetY,
                effectPaintStyle.shadowColor,
            )
            this.shadowRadiusPx = safeShadowRadius
            this.shadowOffsetXPx = safeShadowOffsetX
            this.shadowOffsetYPx = safeShadowOffsetY
            this.shadowColor = effectPaintStyle.shadowColor
        }
    }

    fun applyEntryStyle(
        fillPaint: Paint,
        strokePaint: Paint,
        textSizePx: Float,
        fillColor: Int,
        strokeColor: Int,
    ) {
        val safeTextSizePx = textSizePx.coerceFiniteAtLeast(1f)
        if (this.textSizePx != safeTextSizePx) {
            fillPaint.textSize = safeTextSizePx
            strokePaint.textSize = safeTextSizePx
            this.textSizePx = safeTextSizePx
        }
        if (this.fillColor != fillColor) {
            fillPaint.color = fillColor
            this.fillColor = fillColor
        }
        if (this.strokeColor != strokeColor) {
            strokePaint.color = strokeColor
            this.strokeColor = strokeColor
        }
    }
}

private fun Float.coerceFiniteAtLeast(minimumValue: Float): Float {
    return if (isFinite()) coerceAtLeast(minimumValue) else minimumValue
}

internal class DanmakuSurfaceLayoutCache {
    private var items: List<DanmakuItem>? = null
    private var widthPx: Float = -1f
    private var heightPx: Float = -1f
    private var profile: DanmakuProfile? = null
    private var settings: DanmakuLayoutSettingsKey? = null
    private var densityKey: Float = -1f
    private var safeArea: DanmakuSafeArea = DanmakuSafeArea()
    private var layout: PreparedDanmakuLayout = PreparedDanmakuLayout.Empty

    fun layoutFor(
        items: List<DanmakuItem>,
        widthPx: Float,
        heightPx: Float,
        profile: DanmakuProfile,
        settings: DanmakuSettings,
        densityKey: Float,
        safeArea: DanmakuSafeArea = DanmakuSafeArea(),
        build: () -> PreparedDanmakuLayout,
    ): PreparedDanmakuLayout {
        val settingsKey = DanmakuLayoutSettingsKey(settings)
        val previousItems = this.items
        val hasSameItems = previousItems != null && (previousItems === items || previousItems == items)
        if (
            !hasSameItems ||
            this.widthPx != widthPx ||
            this.heightPx != heightPx ||
            this.profile != profile ||
            this.settings != settingsKey ||
            this.densityKey != densityKey ||
            this.safeArea != safeArea
        ) {
            this.items = items
            this.widthPx = widthPx
            this.heightPx = heightPx
            this.profile = profile
            this.settings = settingsKey
            this.densityKey = densityKey
            this.safeArea = safeArea
            layout = build()
        } else if (previousItems !== items) {
            this.items = items
        }
        return layout
    }
}

private data class DanmakuLayoutSettingsKey(
    val enabled: Boolean,
    val density: Float,
    val fontScale: Float,
    val blockedWords: Set<String>,
) {
    constructor(settings: DanmakuSettings) : this(
        enabled = settings.enabled,
        density = settings.density,
        fontScale = settings.fontScale,
        blockedWords = settings.blockedWords,
    )
}

@Composable
private fun rememberTextPaint(): Paint {
    return remember {
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            style = Paint.Style.FILL
        }
    }
}

internal fun danmakuFillColor(color: Long, alpha: Float): Int {
    val a = (alpha * 255).toInt().coerceIn(0, 255)
    return (a shl 24) or (color.toInt() and 0x00FFFFFF)
}

internal fun danmakuStrokeColor(alpha: Float): Int {
    return danmakuStrokeColor(alpha, DanmakuStrokeAlpha)
}

internal fun danmakuStrokeColor(alpha: Float, strokeAlpha: Float): Int {
    val a = (alpha * strokeAlpha.coerceIn(0f, 1f) * 255).toInt().coerceIn(0, 255)
    return a shl 24
}

internal fun danmakuEffectPaintStyle(
    effectStyle: DanmakuEffectStyle,
    platform: DanmakuPlatform,
    profile: DanmakuProfile,
): DanmakuEffectPaintStyle {
    val resolvedStyle = resolveDanmakuEffectStyle(effectStyle, platform)
    val strokeWidth = profile.strokeWidthPx.coerceFiniteAtLeast(0f)
    val shadowRadius = profile.shadowRadiusPx.coerceFiniteAtLeast(0f)
    return when (resolvedStyle) {
        DanmakuEffectStyle.PlatformAdaptive,
        DanmakuEffectStyle.ClassicStroke -> DanmakuEffectPaintStyle(
            strokeWidthPx = strokeWidth,
            shadowRadiusPx = shadowRadius,
            shadowOffsetXPx = 1f,
            shadowOffsetYPx = 1f,
            shadowColor = DanmakuShadowColor,
            strokeAlpha = 0.8f,
        )
        DanmakuEffectStyle.CinemaGlow -> DanmakuEffectPaintStyle(
            strokeWidthPx = strokeWidth * 0.72f,
            shadowRadiusPx = (shadowRadius * 1.8f).coerceAtLeast(6f),
            shadowOffsetXPx = 0f,
            shadowOffsetYPx = 0f,
            shadowColor = 0xAA40E8FF.toInt(),
            strokeAlpha = 0.58f,
        )
        DanmakuEffectStyle.HighContrast -> DanmakuEffectPaintStyle(
            strokeWidthPx = (strokeWidth * 1.35f).coerceAtLeast(4f),
            shadowRadiusPx = (shadowRadius * 0.8f).coerceAtLeast(2f),
            shadowOffsetXPx = 1f,
            shadowOffsetYPx = 1f,
            shadowColor = 0xCC000000.toInt(),
            strokeAlpha = 0.94f,
        )
        DanmakuEffectStyle.Lightweight -> DanmakuEffectPaintStyle(
            strokeWidthPx = strokeWidth * 0.45f,
            shadowRadiusPx = 0f,
            shadowOffsetXPx = 0f,
            shadowOffsetYPx = 0f,
            shadowColor = 0x00000000,
            strokeAlpha = 0.5f,
        )
    }
}

internal fun resolveDanmakuEffectStyle(
    effectStyle: DanmakuEffectStyle,
    platform: DanmakuPlatform,
): DanmakuEffectStyle {
    if (effectStyle != DanmakuEffectStyle.PlatformAdaptive) return effectStyle
    return when (platform) {
        DanmakuPlatform.Bilibili -> DanmakuEffectStyle.ClassicStroke
        DanmakuPlatform.Tencent -> DanmakuEffectStyle.CinemaGlow
        DanmakuPlatform.Iqiyi -> DanmakuEffectStyle.HighContrast
        DanmakuPlatform.Youku -> DanmakuEffectStyle.CinemaGlow
        DanmakuPlatform.Local -> DanmakuEffectStyle.ClassicStroke
    }
}

internal fun danmakuFrameDelayMs(
    enabled: Boolean,
    hasItems: Boolean,
    isPlaying: Boolean,
): Long? {
    if (!enabled || !hasItems) return null
    return if (isPlaying) 0L else PausedFrameDelayMs
}

internal fun danmakuFrameTimeReady(frameTimeNs: Long): Boolean = frameTimeNs != DanmakuFrameTimeUnsetNs

internal const val DanmakuFrameTimeUnsetNs = Long.MIN_VALUE
private const val PausedFrameDelayMs = 250L
private const val DanmakuStrokeAlpha = 0.8f
private const val DanmakuShadowColor = 0x88000000.toInt()
