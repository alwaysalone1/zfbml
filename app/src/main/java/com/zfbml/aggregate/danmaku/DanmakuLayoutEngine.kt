package com.zfbml.aggregate.danmaku

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class RenderedDanmaku(
    val item: DanmakuItem,
    val x: Float,
    val y: Float,
    val alpha: Float,
    val lane: Int,
    val widthPx: Float,
)

data class DanmakuTextMetrics(
    val textSizePx: Float,
    val widthPx: Float,
    val lineHeightPx: Float,
    val baselineOffsetPx: Float,
)

class DanmakuLayoutEngine {
    fun layout(
        items: List<DanmakuItem>,
        playbackMs: Long,
        widthPx: Float,
        heightPx: Float,
        profile: DanmakuProfile,
        settings: DanmakuSettings,
        measureText: ((DanmakuItem) -> DanmakuTextMetrics)? = null,
    ): List<RenderedDanmaku> {
        if (!settings.enabled || widthPx <= 0f || heightPx <= 0f) return emptyList()
        val active = items
            .asSequence()
            .filter { item -> settings.blockedWords.none { item.text.contains(it, ignoreCase = true) } }
            .filter { item -> item.timeMs <= playbackMs && playbackMs - item.timeMs <= maxWindow(item, profile) }
            .sortedBy { it.timeMs }
            .toList()
        if (active.isEmpty()) return emptyList()

        val maxActiveItems = max(1, (profile.maxItemsPerMinute * settings.density.coerceAtLeast(0.25f)).roundToInt())
        val measured = active
            .takeLast(maxActiveItems)
            .mapNotNull { item ->
                val duration = durationFor(item, profile)
                if (duration <= 0L) {
                    null
                } else {
                    MeasuredDanmaku(
                        item = item,
                        durationMs = duration,
                        metrics = sanitizeMetrics(measureText?.invoke(item) ?: estimateMetrics(item, profile, settings)),
                    )
                }
            }
        if (measured.isEmpty()) return emptyList()

        val lineHeight = measured.maxOf { it.metrics.lineHeightPx }
            .coerceAtLeast(18f)
            .coerceAtMost(heightPx)
        val physicalTracks = max(1, (heightPx / lineHeight).toInt())
        val densityLimit = max(1, (profile.maxTracks * settings.density.coerceIn(0.5f, 1.5f)).roundToInt())
        val totalTracks = physicalTracks.coerceAtMost(densityLimit)
        val hasTop = measured.any { it.item.mode == DanmakuMode.Top }
        val hasBottom = measured.any { it.item.mode == DanmakuMode.Bottom }
        val topReserve = fixedReserve(hasTop, totalTracks)
        val bottomReserve = fixedReserve(hasBottom, totalTracks - topReserve)
        val movingStartLane = topReserve
        val movingLaneCount = max(1, totalTracks - topReserve - bottomReserve)
        val movingSlots = Array(movingLaneCount) { MovingSlot.Empty }
        val topSlots = LongArray(max(1, topReserve)) { Long.MIN_VALUE }
        val bottomSlots = LongArray(max(1, bottomReserve)) { Long.MIN_VALUE }
        val gapPx = (lineHeight * 0.72f).coerceAtLeast(24f)
        val rendered = mutableListOf<RenderedDanmaku>()

        measured.forEach { entry ->
            val item = entry.item
            val elapsed = playbackMs - item.timeMs
            val metrics = entry.metrics
            val progress = (elapsed / entry.durationMs.toFloat()).coerceIn(0f, 1f)
            val scheduled = when (item.mode) {
                DanmakuMode.Scroll -> {
                    allocateMovingLane(
                        slots = movingSlots,
                        direction = MovingDirection.RightToLeft,
                        startMs = item.timeMs,
                        screenWidthPx = widthPx,
                        textWidthPx = metrics.widthPx,
                        durationMs = entry.durationMs,
                        gapPx = gapPx,
                    )?.let { lane ->
                        val globalLane = movingStartLane + lane
                        ScheduledDanmaku(
                            lane = globalLane,
                            x = widthPx - (widthPx + metrics.widthPx) * progress,
                            y = baselineForLane(globalLane, lineHeight, metrics),
                        )
                    }
                }
                DanmakuMode.Reverse -> {
                    allocateMovingLane(
                        slots = movingSlots,
                        direction = MovingDirection.LeftToRight,
                        startMs = item.timeMs,
                        screenWidthPx = widthPx,
                        textWidthPx = metrics.widthPx,
                        durationMs = entry.durationMs,
                        gapPx = gapPx,
                    )?.let { lane ->
                        val globalLane = movingStartLane + lane
                        ScheduledDanmaku(
                            lane = globalLane,
                            x = -metrics.widthPx + (widthPx + metrics.widthPx) * progress,
                            y = baselineForLane(globalLane, lineHeight, metrics),
                        )
                    }
                }
                DanmakuMode.Top -> allocateFixedLane(topSlots, item.timeMs, entry.durationMs)?.let { lane ->
                    ScheduledDanmaku(
                        lane = lane,
                        x = (widthPx - metrics.widthPx) / 2f,
                        y = baselineForLane(lane, lineHeight, metrics),
                    )
                }
                DanmakuMode.Bottom -> allocateFixedLane(bottomSlots, item.timeMs, entry.durationMs)?.let { lane ->
                    val globalLane = totalTracks - 1 - lane
                    ScheduledDanmaku(
                        lane = globalLane,
                        x = (widthPx - metrics.widthPx) / 2f,
                        y = bottomBaselineForLane(lane, heightPx, lineHeight, metrics),
                    )
                }
                DanmakuMode.Advanced -> {
                    val position = item.position
                    ScheduledDanmaku(
                        lane = 0,
                        x = position?.let { it.x * widthPx } ?: ((widthPx - metrics.widthPx) / 2f),
                        y = position?.let { it.y * heightPx } ?: baselineForLane(0, lineHeight, metrics),
                    )
                }
                DanmakuMode.Script -> null
            }
            if (scheduled != null && scheduled.x + metrics.widthPx >= 0f && scheduled.x <= widthPx) {
                rendered += RenderedDanmaku(
                    item = item,
                    x = scheduled.x,
                    y = scheduled.y,
                    alpha = settings.alpha,
                    lane = scheduled.lane,
                    widthPx = metrics.widthPx,
                )
            }
        }
        return rendered
    }

    private fun fixedReserve(enabled: Boolean, availableTracks: Int): Int {
        if (!enabled || availableTracks <= 1) return 0
        return max(1, availableTracks / 4).coerceAtMost(3)
    }

    private fun allocateMovingLane(
        slots: Array<MovingSlot>,
        direction: MovingDirection,
        startMs: Long,
        screenWidthPx: Float,
        textWidthPx: Float,
        durationMs: Long,
        gapPx: Float,
    ): Int? {
        slots.indices.forEach { index ->
            if (canUseMovingLane(slots[index], direction, startMs, screenWidthPx, textWidthPx, durationMs, gapPx)) {
                slots[index] = MovingSlot(
                    direction = direction,
                    startMs = startMs,
                    widthPx = textWidthPx,
                    durationMs = durationMs,
                )
                return index
            }
        }
        return null
    }

    private fun canUseMovingLane(
        slot: MovingSlot,
        direction: MovingDirection,
        startMs: Long,
        screenWidthPx: Float,
        textWidthPx: Float,
        durationMs: Long,
        gapPx: Float,
    ): Boolean {
        if (slot === MovingSlot.Empty) return true
        val elapsedMs = startMs - slot.startMs
        if (elapsedMs < 0L) return false
        if (elapsedMs >= slot.durationMs) return true
        if (slot.direction != direction) return false

        val previousSpeed = (screenWidthPx + slot.widthPx) / slot.durationMs.toFloat()
        val newSpeed = (screenWidthPx + textWidthPx) / durationMs.toFloat()
        val gap = when (direction) {
            MovingDirection.RightToLeft -> {
                val previousX = screenWidthPx - previousSpeed * elapsedMs
                screenWidthPx - (previousX + slot.widthPx)
            }
            MovingDirection.LeftToRight -> {
                val previousX = -slot.widthPx + previousSpeed * elapsedMs
                previousX
            }
        }
        if (gap < gapPx) return false
        if (newSpeed <= previousSpeed) return true
        val catchUpMs = gap / (newSpeed - previousSpeed)
        return catchUpMs >= min((slot.durationMs - elapsedMs).toFloat(), durationMs.toFloat())
    }

    private fun allocateFixedLane(slots: LongArray, startMs: Long, durationMs: Long): Int? {
        slots.indices.forEach { index ->
            if (startMs >= slots[index]) {
                slots[index] = startMs + durationMs + 220L
                return index
            }
        }
        return null
    }

    private fun baselineForLane(lane: Int, lineHeight: Float, metrics: DanmakuTextMetrics): Float {
        return lane * lineHeight + metrics.baselineOffsetPx
    }

    private fun bottomBaselineForLane(lane: Int, heightPx: Float, lineHeight: Float, metrics: DanmakuTextMetrics): Float {
        val lineTop = heightPx - (lane + 1) * lineHeight
        return lineTop + metrics.baselineOffsetPx
    }

    private fun maxWindow(item: DanmakuItem, profile: DanmakuProfile): Long = durationFor(item, profile) + 500

    private fun durationFor(item: DanmakuItem, profile: DanmakuProfile): Long = when (item.mode) {
        DanmakuMode.Top -> profile.topDurationMs
        DanmakuMode.Bottom -> profile.bottomDurationMs
        DanmakuMode.Advanced -> item.position?.durationMs ?: profile.topDurationMs
        DanmakuMode.Script -> 0
        else -> profile.scrollDurationMs
    }

    private fun estimateMetrics(
        item: DanmakuItem,
        profile: DanmakuProfile,
        settings: DanmakuSettings,
    ): DanmakuTextMetrics {
        val textSize = (item.fontSizeSp * profile.fontScale * settings.fontScale).coerceAtLeast(12f)
        val lineHeight = textSize * 1.35f
        return DanmakuTextMetrics(
            textSizePx = textSize,
            widthPx = (item.text.length * textSize * 0.68f).coerceAtLeast(textSize),
            lineHeightPx = lineHeight,
            baselineOffsetPx = lineHeight * 0.78f,
        )
    }

    private fun sanitizeMetrics(metrics: DanmakuTextMetrics): DanmakuTextMetrics {
        val lineHeight = metrics.lineHeightPx.coerceAtLeast(18f)
        return metrics.copy(
            textSizePx = metrics.textSizePx.coerceAtLeast(12f),
            widthPx = metrics.widthPx.coerceAtLeast(1f),
            lineHeightPx = lineHeight,
            baselineOffsetPx = metrics.baselineOffsetPx.coerceIn(1f, lineHeight),
        )
    }

    private data class MeasuredDanmaku(
        val item: DanmakuItem,
        val durationMs: Long,
        val metrics: DanmakuTextMetrics,
    )

    private data class ScheduledDanmaku(
        val lane: Int,
        val x: Float,
        val y: Float,
    )

    private enum class MovingDirection {
        RightToLeft,
        LeftToRight,
    }

    private data class MovingSlot(
        val direction: MovingDirection,
        val startMs: Long,
        val widthPx: Float,
        val durationMs: Long,
    ) {
        companion object {
            val Empty = MovingSlot(MovingDirection.RightToLeft, Long.MIN_VALUE, 0f, 0L)
        }
    }
}
