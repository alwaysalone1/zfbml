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
    val metrics: DanmakuTextMetrics,
)

data class DanmakuTextMetrics(
    val textSizePx: Float,
    val widthPx: Float,
    val lineHeightPx: Float,
    val baselineOffsetPx: Float,
)

internal class PreparedDanmakuLayout internal constructor(
    private val entries: List<ScheduledDanmakuEntry>,
    private val maxActiveItems: Int,
    private val maxActiveWindowMs: Long,
) {
    fun render(playbackMs: Long, alpha: Float): List<RenderedDanmaku> {
        return render(playbackMs.toDouble(), alpha)
    }

    fun render(playbackMs: Double, alpha: Float): List<RenderedDanmaku> {
        if (entries.isEmpty()) return emptyList()
        val rendered = mutableListOf<RenderedDanmaku>()
        forEachVisible(playbackMs, alpha) { entry, x, entryAlpha ->
            rendered += RenderedDanmaku(
                item = entry.item,
                x = x,
                y = entry.y,
                alpha = entryAlpha,
                lane = entry.lane,
                widthPx = entry.metrics.widthPx,
                metrics = entry.metrics,
            )
        }
        return rendered
    }

    internal fun forEachVisible(
        playbackMs: Double,
        alpha: Float,
        block: (entry: ScheduledDanmakuEntry, x: Float, alpha: Float) -> Unit,
    ) {
        if (entries.isEmpty()) return
        val firstIndex = lowerBound(playbackMs - maxActiveWindowMs)
        val endIndex = upperBound(playbackMs)
        if (firstIndex >= endIndex) return

        var visibleCount = 0
        var fullAlphaStartIndex = endIndex
        var fadeStartIndex = endIndex
        val maxVisibleWithFade = maxActiveItems + OverflowFadeItemCount
        for (index in endIndex - 1 downTo firstIndex) {
            val entry = entries[index]
            if (entry.isVisibleAt(playbackMs)) {
                visibleCount += 1
                if (visibleCount <= maxActiveItems) {
                    fullAlphaStartIndex = index
                }
                fadeStartIndex = index
                if (visibleCount >= maxVisibleWithFade) break
            }
        }

        val fadeCount = (visibleCount - maxActiveItems).coerceIn(0, OverflowFadeItemCount)
        var visibleOrdinal = 0
        for (index in fadeStartIndex until endIndex) {
            val entry = entries[index]
            if (!entry.isVisibleAt(playbackMs)) continue
            visibleOrdinal += 1
            val overflowAlpha = if (fadeCount > 0 && index < fullAlphaStartIndex) {
                val rankFromNewest = visibleCount - visibleOrdinal
                val overflowFromNewest = rankFromNewest - maxActiveItems
                alpha * ((fadeCount - overflowFromNewest).toFloat() / (fadeCount + 1).toFloat())
            } else {
                alpha
            }
            val entryAlpha = entry.alphaAt(playbackMs, overflowAlpha)
            if (entryAlpha <= 0f) continue
            block(entry, entry.xAt(playbackMs), entryAlpha)
        }
    }

    private fun lowerBound(targetMs: Double): Int {
        var low = 0
        var high = entries.size
        while (low < high) {
            val mid = (low + high) ushr 1
            if (entries[mid].startMs < targetMs) low = mid + 1 else high = mid
        }
        return low
    }

    private fun upperBound(targetMs: Double): Int {
        var low = 0
        var high = entries.size
        while (low < high) {
            val mid = (low + high) ushr 1
            if (entries[mid].startMs <= targetMs) low = mid + 1 else high = mid
        }
        return low
    }

    companion object {
        val Empty = PreparedDanmakuLayout(emptyList(), 1, 0L)
    }
}

private const val OverflowFadeItemCount = 4
private const val FixedDanmakuFadeMs = 180L

internal data class ScheduledDanmakuEntry(
    val item: DanmakuItem,
    val startMs: Long,
    val durationMs: Long,
    val activeWindowMs: Long,
    val lane: Int,
    val startX: Float,
    val endX: Float,
    val y: Float,
    val viewportStartX: Float,
    val viewportEndX: Float,
    val metrics: DanmakuTextMetrics,
)

private fun ScheduledDanmakuEntry.isVisibleAt(playbackMs: Double): Boolean {
    val elapsed = playbackMs - startMs
    if (elapsed < 0.0 || elapsed > activeWindowMs) return false
    val x = xAt(playbackMs)
    return x + metrics.widthPx >= viewportStartX && x <= viewportEndX
}

private fun ScheduledDanmakuEntry.xAt(playbackMs: Double): Float {
    val elapsed = playbackMs - startMs
    val progress = (elapsed / durationMs.toDouble()).coerceIn(0.0, 1.0)
    return (startX + (endX - startX) * progress).toFloat()
}

private fun ScheduledDanmakuEntry.alphaAt(playbackMs: Double, baseAlpha: Float): Float {
    if (!item.mode.hasFixedFade()) return baseAlpha
    if (durationMs <= 0L) return 0f
    val elapsed = playbackMs - startMs
    val fadeMs = min(FixedDanmakuFadeMs.toDouble(), durationMs.toDouble() / 2.0)
    if (fadeMs <= 0.0) return baseAlpha
    val fadeIn = elapsed / fadeMs
    val fadeOut = (durationMs - elapsed) / fadeMs
    val fade = min(fadeIn, fadeOut).coerceIn(0.0, 1.0)
    return baseAlpha * fade.toFloat()
}

private fun DanmakuMode.hasFixedFade(): Boolean {
    return this == DanmakuMode.Top || this == DanmakuMode.Bottom || this == DanmakuMode.Advanced
}

private fun DanmakuSafeArea.coerceWithin(widthPx: Float, heightPx: Float): DanmakuSafeArea {
    val safeWidth = widthPx.coerceAtLeast(1f)
    val safeHeight = heightPx.coerceAtLeast(1f)
    val top = topInsetPx.coerceFiniteAtLeast(0f).coerceAtMost(safeHeight - 1f)
    val bottom = bottomInsetPx.coerceFiniteAtLeast(0f).coerceAtMost(safeHeight - top - 1f)
    val start = startInsetPx.coerceFiniteAtLeast(0f).coerceAtMost(safeWidth - 1f)
    val end = endInsetPx.coerceFiniteAtLeast(0f).coerceAtMost(safeWidth - start - 1f)
    val remainingHeight = safeHeight - top - bottom
    val centerExcludedHeight = centerExcludedHeightPx
        .coerceFiniteAtLeast(0f)
        .coerceAtMost((remainingHeight - 1f).coerceAtLeast(0f))
    return DanmakuSafeArea(
        topInsetPx = top,
        bottomInsetPx = bottom,
        startInsetPx = start,
        endInsetPx = end,
        centerExcludedHeightPx = centerExcludedHeight,
    )
}

private fun Float.coerceFiniteAtLeast(minimumValue: Float): Float {
    return if (isFinite()) coerceAtLeast(minimumValue) else minimumValue
}

class DanmakuLayoutEngine {
    fun layout(
        items: List<DanmakuItem>,
        playbackMs: Long,
        widthPx: Float,
        heightPx: Float,
        profile: DanmakuProfile,
        settings: DanmakuSettings,
        safeArea: DanmakuSafeArea = DanmakuSafeArea(),
        measureText: ((DanmakuItem) -> DanmakuTextMetrics)? = null,
    ): List<RenderedDanmaku> {
        return prepare(
            items = items,
            widthPx = widthPx,
            heightPx = heightPx,
            profile = profile,
            settings = settings,
            safeArea = safeArea,
            measureText = measureText,
        ).render(playbackMs, settings.alpha)
    }

    internal fun prepare(
        items: List<DanmakuItem>,
        widthPx: Float,
        heightPx: Float,
        profile: DanmakuProfile,
        settings: DanmakuSettings,
        safeArea: DanmakuSafeArea = DanmakuSafeArea(),
        measureText: ((DanmakuItem) -> DanmakuTextMetrics)? = null,
    ): PreparedDanmakuLayout {
        if (!settings.enabled || widthPx <= 0f || heightPx <= 0f) return PreparedDanmakuLayout.Empty
        val boundedSafeArea = safeArea.coerceWithin(widthPx, heightPx)
        val safeWidthPx = (widthPx - boundedSafeArea.startInsetPx - boundedSafeArea.endInsetPx).coerceAtLeast(1f)
        val safeHeightPx = (heightPx - boundedSafeArea.topInsetPx - boundedSafeArea.bottomInsetPx).coerceAtLeast(1f)
        val safeStartX = boundedSafeArea.startInsetPx
        val safeEndX = boundedSafeArea.startInsetPx + safeWidthPx
        val measured = items
            .asSequence()
            .filter { item -> settings.blockedWords.none { item.text.contains(it, ignoreCase = true) } }
            .sortedBy { it.timeMs }
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
            .toList()
        if (measured.isEmpty()) return PreparedDanmakuLayout.Empty

        val lineHeight = measured.maxOf { it.metrics.lineHeightPx }
            .coerceAtLeast(18f)
            .coerceAtMost(safeHeightPx)
        val physicalTracks = max(1, (safeHeightPx / lineHeight).toInt())
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
        val scheduledEntries = mutableListOf<ScheduledDanmakuEntry>()
        fun laneAvoidsCenter(globalLane: Int, metrics: DanmakuTextMetrics): Boolean {
            return baselineAvoidsCenter(
                baselineY = boundedSafeArea.topInsetPx + baselineForLane(globalLane, lineHeight, metrics),
                metrics = metrics,
                safeTopPx = boundedSafeArea.topInsetPx,
                safeHeightPx = safeHeightPx,
                centerExcludedHeightPx = boundedSafeArea.centerExcludedHeightPx,
            )
        }
        fun bottomLaneAvoidsCenter(lane: Int, metrics: DanmakuTextMetrics): Boolean {
            return baselineAvoidsCenter(
                baselineY = boundedSafeArea.topInsetPx + bottomBaselineForLane(lane, safeHeightPx, lineHeight, metrics),
                metrics = metrics,
                safeTopPx = boundedSafeArea.topInsetPx,
                safeHeightPx = safeHeightPx,
                centerExcludedHeightPx = boundedSafeArea.centerExcludedHeightPx,
            )
        }

        measured.forEach { entry ->
            val item = entry.item
            val metrics = entry.metrics
            val scheduled = when (item.mode) {
                DanmakuMode.Scroll -> {
                    allocateMovingLane(
                        slots = movingSlots,
                        direction = MovingDirection.RightToLeft,
                        startMs = item.timeMs,
                        screenWidthPx = safeWidthPx,
                        textWidthPx = metrics.widthPx,
                        durationMs = entry.durationMs,
                        gapPx = gapPx,
                        laneAllowed = { lane -> laneAvoidsCenter(movingStartLane + lane, metrics) },
                    )?.let { lane ->
                        val globalLane = movingStartLane + lane
                        ScheduledDanmaku(
                            lane = globalLane,
                            startX = safeEndX,
                            endX = safeStartX - metrics.widthPx,
                            y = boundedSafeArea.topInsetPx + baselineForLane(globalLane, lineHeight, metrics),
                        )
                    }
                }
                DanmakuMode.Reverse -> {
                    allocateMovingLane(
                        slots = movingSlots,
                        direction = MovingDirection.LeftToRight,
                        startMs = item.timeMs,
                        screenWidthPx = safeWidthPx,
                        textWidthPx = metrics.widthPx,
                        durationMs = entry.durationMs,
                        gapPx = gapPx,
                        laneAllowed = { lane -> laneAvoidsCenter(movingStartLane + lane, metrics) },
                    )?.let { lane ->
                        val globalLane = movingStartLane + lane
                        ScheduledDanmaku(
                            lane = globalLane,
                            startX = safeStartX - metrics.widthPx,
                            endX = safeEndX,
                            y = boundedSafeArea.topInsetPx + baselineForLane(globalLane, lineHeight, metrics),
                        )
                    }
                }
                DanmakuMode.Top -> allocateFixedLane(
                    slots = topSlots,
                    startMs = item.timeMs,
                    durationMs = entry.durationMs,
                    laneAllowed = { lane -> laneAvoidsCenter(lane, metrics) },
                )?.let { lane ->
                    val x = safeStartX + (safeWidthPx - metrics.widthPx) / 2f
                    ScheduledDanmaku(
                        lane = lane,
                        startX = x,
                        endX = x,
                        y = boundedSafeArea.topInsetPx + baselineForLane(lane, lineHeight, metrics),
                    )
                }
                DanmakuMode.Bottom -> allocateFixedLane(
                    slots = bottomSlots,
                    startMs = item.timeMs,
                    durationMs = entry.durationMs,
                    laneAllowed = { lane -> bottomLaneAvoidsCenter(lane, metrics) },
                )?.let { lane ->
                    val globalLane = totalTracks - 1 - lane
                    val x = safeStartX + (safeWidthPx - metrics.widthPx) / 2f
                    ScheduledDanmaku(
                        lane = globalLane,
                        startX = x,
                        endX = x,
                        y = boundedSafeArea.topInsetPx + bottomBaselineForLane(lane, safeHeightPx, lineHeight, metrics),
                    )
                }
                DanmakuMode.Advanced -> {
                    val position = item.position
                    val x = position?.let { safeStartX + it.x * safeWidthPx } ?: (safeStartX + (safeWidthPx - metrics.widthPx) / 2f)
                    val y = position?.let { boundedSafeArea.topInsetPx + it.y * safeHeightPx }
                        ?: (boundedSafeArea.topInsetPx + baselineForLane(0, lineHeight, metrics))
                    if (!baselineAvoidsCenter(
                            baselineY = y,
                            metrics = metrics,
                            safeTopPx = boundedSafeArea.topInsetPx,
                            safeHeightPx = safeHeightPx,
                            centerExcludedHeightPx = boundedSafeArea.centerExcludedHeightPx,
                        )
                    ) {
                        null
                    } else {
                        ScheduledDanmaku(
                            lane = 0,
                            startX = x,
                            endX = x,
                            y = y,
                        )
                    }
                }
                DanmakuMode.Script -> null
            }
            if (scheduled != null) {
                scheduledEntries += ScheduledDanmakuEntry(
                    item = item,
                    startMs = item.timeMs,
                    durationMs = entry.durationMs,
                    activeWindowMs = maxWindow(item, profile),
                    y = scheduled.y,
                    lane = scheduled.lane,
                    startX = scheduled.startX,
                    endX = scheduled.endX,
                    viewportStartX = safeStartX,
                    viewportEndX = safeEndX,
                    metrics = metrics,
                )
            }
        }
        val maxActiveWindowMs = scheduledEntries.maxOfOrNull { it.activeWindowMs } ?: 0L
        return PreparedDanmakuLayout(
            entries = scheduledEntries,
            maxActiveItems = danmakuActiveItemLimit(
                maxItemsPerMinute = profile.maxItemsPerMinute,
                density = settings.density,
                activeWindowMs = maxActiveWindowMs,
            ),
            maxActiveWindowMs = maxActiveWindowMs,
        )
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
        laneAllowed: (Int) -> Boolean = { true },
    ): Int? {
        slots.indices.forEach { index ->
            if (!laneAllowed(index)) return@forEach
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

    private fun allocateFixedLane(
        slots: LongArray,
        startMs: Long,
        durationMs: Long,
        laneAllowed: (Int) -> Boolean = { true },
    ): Int? {
        slots.indices.forEach { index ->
            if (!laneAllowed(index)) return@forEach
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

    private fun baselineAvoidsCenter(
        baselineY: Float,
        metrics: DanmakuTextMetrics,
        safeTopPx: Float,
        safeHeightPx: Float,
        centerExcludedHeightPx: Float,
    ): Boolean {
        if (centerExcludedHeightPx <= 0f) return true
        val centerTop = safeTopPx + ((safeHeightPx - centerExcludedHeightPx) / 2f).coerceAtLeast(0f)
        val centerBottom = centerTop + centerExcludedHeightPx
        val lineTop = baselineY - metrics.baselineOffsetPx
        val lineBottom = lineTop + metrics.lineHeightPx
        return lineBottom <= centerTop || lineTop >= centerBottom
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
        val startX: Float,
        val endX: Float,
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

internal fun danmakuActiveItemLimit(
    maxItemsPerMinute: Int,
    density: Float,
    activeWindowMs: Long,
): Int {
    val boundedDensity = density.coerceAtLeast(0.25f)
    val boundedWindowMinutes = activeWindowMs.coerceAtLeast(1L).toDouble() / MILLIS_PER_MINUTE
    return max(1, (maxItemsPerMinute.coerceAtLeast(1) * boundedDensity * boundedWindowMinutes).roundToInt())
}

private const val MILLIS_PER_MINUTE = 60_000.0
