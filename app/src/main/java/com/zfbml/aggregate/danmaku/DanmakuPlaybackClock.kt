package com.zfbml.aggregate.danmaku

import kotlin.math.abs

internal class DanmakuPlaybackClock(
    private val hardSyncToleranceMs: Double = 90.0,
    private val softSyncToleranceMs: Double = 24.0,
    private val seekToleranceMs: Double = 260.0,
) {
    private var anchorPlaybackMs = 0.0
    private var anchorFrameNs = Long.MIN_VALUE
    private var lastSampledPlaybackMs: Double? = null
    private var lastPlaying = false
    private var lastSpeed = 1.0

    fun reset() {
        anchorPlaybackMs = 0.0
        anchorFrameNs = Long.MIN_VALUE
        lastSampledPlaybackMs = null
        lastPlaying = false
        lastSpeed = 1.0
    }

    fun positionMs(
        sampledPlaybackMs: Long,
        frameTimeNs: Long,
        isPlaying: Boolean,
        playbackSpeed: Float,
    ): Double {
        val sampled = sampledPlaybackMs.coerceAtLeast(0L).toDouble()
        val speed = playbackSpeed.takeIf { it.isFinite() && it > 0f }?.toDouble() ?: 1.0
        val previousSample = lastSampledPlaybackMs
        val sampleChanged = previousSample == null || sampled != previousSample
        val seekedBack = previousSample != null && sampled < previousSample - seekToleranceMs
        val clockChanged = isPlaying != lastPlaying || abs(speed - lastSpeed) > 0.001

        var predicted = when {
            anchorFrameNs == Long.MIN_VALUE || frameTimeNs < anchorFrameNs || clockChanged || seekedBack -> {
                syncTo(sampled, frameTimeNs, isPlaying, speed)
                sampled
            }
            !isPlaying -> {
                if (sampleChanged) syncTo(sampled, frameTimeNs, isPlaying, speed)
                sampled
            }
            else -> {
                val elapsedMs = (frameTimeNs - anchorFrameNs).coerceAtLeast(0L) / NANOS_PER_MS
                anchorPlaybackMs + elapsedMs * speed
            }
        }

        if (isPlaying && sampleChanged && !seekedBack) {
            val driftMs = sampled - predicted
            when {
                abs(driftMs) > hardSyncToleranceMs -> {
                    syncTo(sampled, frameTimeNs, isPlaying, speed)
                    predicted = sampled
                }
                abs(driftMs) > softSyncToleranceMs -> {
                    val correctionMs = driftMs * SOFT_CORRECTION_RATIO
                    anchorPlaybackMs += correctionMs
                    predicted += correctionMs
                }
            }
        }

        lastSampledPlaybackMs = sampled
        lastPlaying = isPlaying
        lastSpeed = speed
        return predicted.coerceAtLeast(0.0)
    }

    private fun syncTo(
        playbackMs: Double,
        frameTimeNs: Long,
        isPlaying: Boolean,
        speed: Double,
    ) {
        anchorPlaybackMs = playbackMs
        anchorFrameNs = frameTimeNs
        lastPlaying = isPlaying
        lastSpeed = speed
    }

    private companion object {
        const val NANOS_PER_MS = 1_000_000.0
        const val SOFT_CORRECTION_RATIO = 0.12
    }
}
