package com.zfbml.aggregate.danmaku

import kotlin.math.abs
import kotlin.math.max

internal class DanmakuPlaybackClock(
    private val hardSyncToleranceMs: Double = 700.0,
    private val softSyncToleranceMs: Double = 24.0,
    private val seekToleranceMs: Double = 260.0,
    private val maxSoftCorrectionMs: Double = 6.0,
    private val maxCatchUpCorrectionMs: Double = 24.0,
    private val forwardSeekToleranceMs: Double = 2_000.0,
) {
    private var anchorPlaybackMs = 0.0
    private var anchorFrameNs = Long.MIN_VALUE
    private var lastSampledPlaybackMs: Double? = null
    private var lastOutputPlaybackMs: Double? = null
    private var lastPlaying = false
    private var lastSpeed = 1.0

    fun reset() {
        anchorPlaybackMs = 0.0
        anchorFrameNs = Long.MIN_VALUE
        lastSampledPlaybackMs = null
        lastOutputPlaybackMs = null
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
        val seekedForward = previousSample != null && sampled > previousSample + forwardSeekToleranceMs
        val clockChanged = isPlaying != lastPlaying || abs(speed - lastSpeed) > 0.001

        val previousOutput = lastOutputPlaybackMs
        var predicted = when {
            anchorFrameNs == Long.MIN_VALUE || frameTimeNs < anchorFrameNs || seekedBack || seekedForward -> {
                syncTo(sampled, frameTimeNs, isPlaying, speed)
                sampled
            }
            clockChanged -> {
                val continuousPlaybackMs = if (isPlaying || lastPlaying) {
                    previousOutput?.let { max(it, sampled) } ?: sampled
                } else {
                    sampled
                }
                syncTo(continuousPlaybackMs, frameTimeNs, isPlaying, speed)
                continuousPlaybackMs
            }
            !isPlaying -> {
                if (sampleChanged) {
                    syncTo(sampled, frameTimeNs, isPlaying, speed)
                    sampled
                } else {
                    anchorPlaybackMs
                }
            }
            else -> {
                val elapsedMs = (frameTimeNs - anchorFrameNs).coerceAtLeast(0L) / NANOS_PER_MS
                anchorPlaybackMs + elapsedMs * speed
            }
        }

        if (isPlaying && sampleChanged && !seekedBack && !seekedForward) {
            val driftMs = sampled - predicted
            when {
                driftMs > forwardSeekToleranceMs -> {
                    syncTo(sampled, frameTimeNs, isPlaying, speed)
                    predicted = sampled
                }
                driftMs > hardSyncToleranceMs -> {
                    val correctionMs = (driftMs * SOFT_CORRECTION_RATIO)
                        .coerceIn(maxSoftCorrectionMs, maxCatchUpCorrectionMs)
                    anchorPlaybackMs += correctionMs
                    predicted += correctionMs
                }
                abs(driftMs) > softSyncToleranceMs -> {
                    val correctionMs = (driftMs * SOFT_CORRECTION_RATIO)
                        .coerceIn(-maxSoftCorrectionMs, maxSoftCorrectionMs)
                    anchorPlaybackMs += correctionMs
                    predicted += correctionMs
                }
            }
        }

        if (isPlaying && !seekedBack) {
            previousOutput?.let { output ->
                predicted = max(predicted, output)
            }
        }

        lastSampledPlaybackMs = sampled
        lastOutputPlaybackMs = predicted
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
