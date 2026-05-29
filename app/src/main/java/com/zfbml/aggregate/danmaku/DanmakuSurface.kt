package com.zfbml.aggregate.danmaku

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp

@Composable
fun DanmakuSurface(
    items: List<DanmakuItem>,
    playbackMsProvider: () -> Long,
    profile: DanmakuProfile,
    settings: DanmakuSettings,
    modifier: Modifier = Modifier,
) {
    val layoutEngine = remember { DanmakuLayoutEngine() }
    val fillPaint = rememberTextPaint()
    val measurePaint = rememberTextPaint()
    val strokePaint = rememberTextPaint().apply {
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
    }
    val density = LocalDensity.current
    var frameTimeMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(settings.enabled, items) {
        while (settings.enabled) {
            frameTimeMs = withFrameMillis { it }
        }
    }

    Canvas(modifier = modifier) {
        frameTimeMs
        val playbackMs = playbackMsProvider()
        val metricsCache = HashMap<DanmakuItem, DanmakuTextMetrics>()
        fun metricsFor(item: DanmakuItem): DanmakuTextMetrics {
            return metricsCache.getOrPut(item) {
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
        val rendered = layoutEngine.layout(
            items = items,
            playbackMs = playbackMs,
            widthPx = size.width,
            heightPx = size.height,
            profile = profile,
            settings = settings,
            measureText = ::metricsFor,
        )
        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas
            rendered.forEach { entry ->
                val metrics = metricsFor(entry.item)
                val color = entry.item.color.withAlpha(entry.alpha)
                strokePaint.textSize = metrics.textSizePx
                strokePaint.strokeWidth = profile.strokeWidthPx
                strokePaint.color = 0xCC000000.toInt()
                fillPaint.textSize = metrics.textSizePx
                fillPaint.color = color
                fillPaint.setShadowLayer(profile.shadowRadiusPx, 1f, 1f, 0x88000000.toInt())
                native.drawText(entry.item.text, entry.x, entry.y, strokePaint)
                native.drawText(entry.item.text, entry.x, entry.y, fillPaint)
            }
        }
    }
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
