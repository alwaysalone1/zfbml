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

@Composable
fun DanmakuSurface(
    items: List<DanmakuItem>,
    playbackMsProvider: () -> Long,
    profile: DanmakuProfile,
    settings: DanmakuSettings,
    isPlaying: Boolean,
    playbackSpeed: Float,
    modifier: Modifier = Modifier,
) {
    val layoutEngine = remember { DanmakuLayoutEngine() }
    val layoutCache = remember { DanmakuSurfaceLayoutCache() }
    val playbackClock = remember { DanmakuPlaybackClock() }
    val fillPaint = rememberTextPaint()
    val measurePaint = rememberTextPaint()
    val strokePaint = rememberTextPaint().apply {
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
    }
    val currentPlaybackMsProvider by rememberUpdatedState(playbackMsProvider)
    val density = LocalDensity.current
    var frameTimeNs by remember { mutableLongStateOf(0L) }
    var sampledPlaybackMs by remember { mutableLongStateOf(playbackMsProvider().coerceAtLeast(0L)) }

    LaunchedEffect(settings.enabled, items.isNotEmpty(), isPlaying) {
        val frameDelayMs = danmakuFrameDelayMs(
            enabled = settings.enabled,
            hasItems = items.isNotEmpty(),
            isPlaying = isPlaying,
        ) ?: return@LaunchedEffect
        while (true) {
            frameTimeNs = withFrameNanos { it }
            if (frameDelayMs > 0L) {
                delay(frameDelayMs)
            }
        }
    }
    LaunchedEffect(settings.enabled, items.isNotEmpty(), isPlaying) {
        val sampleDelayMs = danmakuPlaybackSampleDelayMs(
            enabled = settings.enabled,
            hasItems = items.isNotEmpty(),
            isPlaying = isPlaying,
        ) ?: return@LaunchedEffect
        while (true) {
            sampledPlaybackMs = currentPlaybackMsProvider().coerceAtLeast(0L)
            delay(sampleDelayMs)
        }
    }
    LaunchedEffect(items, profile, settings.enabled, settings.density, settings.fontScale, settings.blockedWords) {
        sampledPlaybackMs = currentPlaybackMsProvider().coerceAtLeast(0L)
        playbackClock.reset()
    }

    Canvas(modifier = modifier) {
        frameTimeNs
        val playbackMs = playbackClock.positionMs(
            sampledPlaybackMs = sampledPlaybackMs,
            frameTimeNs = frameTimeNs,
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
        ) {
            layoutEngine.prepare(
                items = items,
                widthPx = size.width,
                heightPx = size.height,
                profile = profile,
                settings = settings,
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
            strokePaint.strokeWidth = profile.strokeWidthPx
            strokePaint.color = 0xCC000000.toInt()
            fillPaint.setShadowLayer(profile.shadowRadiusPx, 1f, 1f, 0x88000000.toInt())
            preparedLayout.forEachVisible(playbackMs, settings.alpha) { entry, x, entryAlpha ->
                val metrics = entry.metrics
                val color = entry.item.color.withAlpha(entryAlpha)
                strokePaint.textSize = metrics.textSizePx
                fillPaint.textSize = metrics.textSizePx
                fillPaint.color = color
                native.drawText(entry.item.text, x, entry.y, strokePaint)
                native.drawText(entry.item.text, x, entry.y, fillPaint)
            }
        }
    }
}

internal class DanmakuSurfaceLayoutCache {
    private var items: List<DanmakuItem>? = null
    private var widthPx: Float = -1f
    private var heightPx: Float = -1f
    private var profile: DanmakuProfile? = null
    private var settings: DanmakuLayoutSettingsKey? = null
    private var densityKey: Float = -1f
    private var layout: PreparedDanmakuLayout = PreparedDanmakuLayout.Empty

    fun layoutFor(
        items: List<DanmakuItem>,
        widthPx: Float,
        heightPx: Float,
        profile: DanmakuProfile,
        settings: DanmakuSettings,
        densityKey: Float,
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
            this.densityKey != densityKey
        ) {
            this.items = items
            this.widthPx = widthPx
            this.heightPx = heightPx
            this.profile = profile
            this.settings = settingsKey
            this.densityKey = densityKey
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

private fun Long.withAlpha(alpha: Float): Int {
    val a = (alpha * 255).toInt().coerceIn(0, 255)
    return (a shl 24) or (toInt() and 0x00FFFFFF)
}

internal fun danmakuFrameDelayMs(
    enabled: Boolean,
    hasItems: Boolean,
    isPlaying: Boolean,
): Long? {
    if (!enabled || !hasItems) return null
    return if (isPlaying) 0L else PausedFrameDelayMs
}

internal fun danmakuPlaybackSampleDelayMs(
    enabled: Boolean,
    hasItems: Boolean,
    isPlaying: Boolean,
): Long? {
    if (!enabled || !hasItems) return null
    return if (isPlaying) PlayingPlaybackSampleDelayMs else PausedFrameDelayMs
}

private const val PlayingPlaybackSampleDelayMs = 96L
private const val PausedFrameDelayMs = 250L
