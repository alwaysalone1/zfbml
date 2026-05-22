package com.zfbml.aggregate.danmaku

import kotlin.math.max

data class RenderedDanmaku(
    val item: DanmakuItem,
    val x: Float,
    val y: Float,
    val alpha: Float,
)

class DanmakuLayoutEngine {
    private val trackEnds = LongArray(64)

    fun layout(
        items: List<DanmakuItem>,
        playbackMs: Long,
        widthPx: Float,
        heightPx: Float,
        profile: DanmakuProfile,
        settings: DanmakuSettings,
    ): List<RenderedDanmaku> {
        if (!settings.enabled || widthPx <= 0f || heightPx <= 0f) return emptyList()
        val filtered = items
            .asSequence()
            .filter { item -> settings.blockedWords.none { item.text.contains(it, ignoreCase = true) } }
            .filter { item -> item.timeMs <= playbackMs && playbackMs - item.timeMs <= maxWindow(item, profile) }
            .sortedBy { it.timeMs }
            .take(max(1, (profile.maxItemsPerMinute * settings.density).toInt()))
            .toList()
        trackEnds.fill(0)
        val lineHeight = 32f * profile.fontScale * settings.fontScale
        return filtered.mapNotNull { item ->
            val duration = durationFor(item, profile)
            val elapsed = playbackMs - item.timeMs
            val textWidthEstimate = item.text.length * item.fontSizeSp * 0.72f * settings.fontScale
            val trackCount = max(1, (heightPx / lineHeight).toInt()).coerceAtMost(profile.maxTracks)
            val track = allocateTrack(item, item.timeMs, trackCount)
            val y = (track + 1) * lineHeight
            val x = when (item.mode) {
                DanmakuMode.Top,
                DanmakuMode.Bottom -> (widthPx - textWidthEstimate) / 2f
                DanmakuMode.Reverse -> -textWidthEstimate + (widthPx + textWidthEstimate) * elapsed / duration.toFloat()
                DanmakuMode.Advanced -> item.position?.let { it.x * widthPx } ?: ((widthPx - textWidthEstimate) / 2f)
                DanmakuMode.Script -> return@mapNotNull null
                DanmakuMode.Scroll -> widthPx - (widthPx + textWidthEstimate) * elapsed / duration.toFloat()
            }
            val adjustedY = when (item.mode) {
                DanmakuMode.Bottom -> heightPx - y
                DanmakuMode.Advanced -> item.position?.let { it.y * heightPx } ?: y
                else -> y
            }
            if (x + textWidthEstimate < 0f || x > widthPx) null else RenderedDanmaku(item, x, adjustedY, settings.alpha)
        }
    }

    private fun allocateTrack(item: DanmakuItem, startMs: Long, trackCount: Int): Int {
        if (item.mode == DanmakuMode.Top) return 0
        if (item.mode == DanmakuMode.Bottom) return trackCount - 1
        val track = trackEnds
            .take(trackCount)
            .withIndex()
            .minBy { it.value }
            .index
        trackEnds[track] = startMs + 700
        return track
    }

    private fun maxWindow(item: DanmakuItem, profile: DanmakuProfile): Long = durationFor(item, profile) + 500

    private fun durationFor(item: DanmakuItem, profile: DanmakuProfile): Long = when (item.mode) {
        DanmakuMode.Top -> profile.topDurationMs
        DanmakuMode.Bottom -> profile.bottomDurationMs
        DanmakuMode.Advanced -> item.position?.durationMs ?: profile.topDurationMs
        DanmakuMode.Script -> 0
        else -> profile.scrollDurationMs
    }
}
