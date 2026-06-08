package com.zfbml.aggregate.danmaku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DanmakuPlaybackClockTest {
    @Test
    fun extrapolatesBetweenCoarsePlayerSamples() {
        val clock = DanmakuPlaybackClock()

        val first = clock.positionMs(
            sampledPlaybackMs = 2_000,
            frameTimeNs = 0,
            isPlaying = true,
            playbackSpeed = 1f,
        )
        val second = clock.positionMs(
            sampledPlaybackMs = 2_000,
            frameTimeNs = 16_666_667,
            isPlaying = true,
            playbackSpeed = 1f,
        )
        val third = clock.positionMs(
            sampledPlaybackMs = 2_000,
            frameTimeNs = 33_333_334,
            isPlaying = true,
            playbackSpeed = 1f,
        )

        assertEquals(2_000.0, first, 0.01)
        assertTrue(second in 2_016.0..2_017.5)
        assertTrue(third in 2_032.0..2_034.5)
    }

    @Test
    fun frameAlignedSamplesAdvanceWithoutMicroStalls() {
        val clock = DanmakuPlaybackClock()

        val first = clock.positionMs(
            sampledPlaybackMs = 2_000,
            frameTimeNs = 0,
            isPlaying = true,
            playbackSpeed = 1f,
        )
        val second = clock.positionMs(
            sampledPlaybackMs = 2_016,
            frameTimeNs = 16_666_667,
            isPlaying = true,
            playbackSpeed = 1f,
        )
        val third = clock.positionMs(
            sampledPlaybackMs = 2_033,
            frameTimeNs = 33_333_334,
            isPlaying = true,
            playbackSpeed = 1f,
        )

        assertTrue(second > first)
        assertTrue(third > second)
        assertTrue(second - first in 15.0..18.0)
        assertTrue(third - second in 15.0..18.0)
    }

    @Test
    fun resyncsAfterSeekBackward() {
        val clock = DanmakuPlaybackClock()

        clock.positionMs(sampledPlaybackMs = 5_000, frameTimeNs = 0, isPlaying = true, playbackSpeed = 1f)
        clock.positionMs(sampledPlaybackMs = 5_016, frameTimeNs = 16_000_000, isPlaying = true, playbackSpeed = 1f)

        val seeked = clock.positionMs(
            sampledPlaybackMs = 1_000,
            frameTimeNs = 32_000_000,
            isPlaying = true,
            playbackSpeed = 1f,
        )
        val afterSeek = clock.positionMs(
            sampledPlaybackMs = 1_000,
            frameTimeNs = 48_000_000,
            isPlaying = true,
            playbackSpeed = 1f,
        )

        assertEquals(1_000.0, seeked, 0.01)
        assertTrue(afterSeek in 1_015.0..1_017.5)
    }

    @Test
    fun pausedClockHoldsSampledPosition() {
        val clock = DanmakuPlaybackClock()

        clock.positionMs(sampledPlaybackMs = 1_000, frameTimeNs = 0, isPlaying = true, playbackSpeed = 1f)
        val paused = clock.positionMs(
            sampledPlaybackMs = 1_000,
            frameTimeNs = 250_000_000,
            isPlaying = false,
            playbackSpeed = 1f,
        )
        val stillPaused = clock.positionMs(
            sampledPlaybackMs = 1_000,
            frameTimeNs = 500_000_000,
            isPlaying = false,
            playbackSpeed = 1f,
        )

        assertEquals(1_000.0, paused, 0.01)
        assertEquals(1_000.0, stillPaused, 0.01)
    }

    @Test
    fun transientBackwardSampleDoesNotMoveClockBackwardWhilePlaying() {
        val clock = DanmakuPlaybackClock()

        clock.positionMs(sampledPlaybackMs = 5_000, frameTimeNs = 0, isPlaying = true, playbackSpeed = 1f)
        val beforeJitter = clock.positionMs(
            sampledPlaybackMs = 5_016,
            frameTimeNs = 16_000_000,
            isPlaying = true,
            playbackSpeed = 1f,
        )
        val jittered = clock.positionMs(
            sampledPlaybackMs = 4_920,
            frameTimeNs = 32_000_000,
            isPlaying = true,
            playbackSpeed = 1f,
        )

        assertTrue(jittered >= beforeJitter)
        assertTrue(jittered in 5_016.0..5_034.0)
    }

    @Test
    fun moderateForwardSampleDriftIsSoftCorrectedWhilePlaying() {
        val clock = DanmakuPlaybackClock()

        clock.positionMs(sampledPlaybackMs = 5_000, frameTimeNs = 0, isPlaying = true, playbackSpeed = 1f)
        val beforeDrift = clock.positionMs(
            sampledPlaybackMs = 5_000,
            frameTimeNs = 16_000_000,
            isPlaying = true,
            playbackSpeed = 1f,
        )
        val drifted = clock.positionMs(
            sampledPlaybackMs = 5_200,
            frameTimeNs = 32_000_000,
            isPlaying = true,
            playbackSpeed = 1f,
        )

        assertTrue(drifted > beforeDrift)
        assertTrue(drifted < 5_060.0)
    }

    @Test
    fun softCorrectionCapsSingleFrameJump() {
        val clock = DanmakuPlaybackClock()

        clock.positionMs(sampledPlaybackMs = 5_000, frameTimeNs = 0, isPlaying = true, playbackSpeed = 1f)
        val corrected = clock.positionMs(
            sampledPlaybackMs = 5_400,
            frameTimeNs = 16_000_000,
            isPlaying = true,
            playbackSpeed = 1f,
        )

        assertTrue(corrected in 5_021.0..5_023.0)
    }

    @Test
    fun forwardSampleSpikeIsSmoothedBeforeSeekThreshold() {
        val clock = DanmakuPlaybackClock()

        clock.positionMs(sampledPlaybackMs = 5_000, frameTimeNs = 0, isPlaying = true, playbackSpeed = 1f)
        val beforeSpike = clock.positionMs(
            sampledPlaybackMs = 5_016,
            frameTimeNs = 16_000_000,
            isPlaying = true,
            playbackSpeed = 1f,
        )
        val smoothed = clock.positionMs(
            sampledPlaybackMs = 6_200,
            frameTimeNs = 32_000_000,
            isPlaying = true,
            playbackSpeed = 1f,
        )

        assertTrue(smoothed > beforeSpike)
        assertTrue(smoothed in 5_050.0..5_070.0)
    }

    @Test
    fun obviousForwardSeekHardSyncsImmediately() {
        val clock = DanmakuPlaybackClock()

        clock.positionMs(sampledPlaybackMs = 2_000, frameTimeNs = 0, isPlaying = true, playbackSpeed = 1f)
        val seeked = clock.positionMs(
            sampledPlaybackMs = 5_000,
            frameTimeNs = 16_000_000,
            isPlaying = true,
            playbackSpeed = 1f,
        )

        assertEquals(5_000.0, seeked, 0.01)
    }

    @Test
    fun speedChangeKeepsClockContinuousAndAdvancing() {
        val clock = DanmakuPlaybackClock()

        clock.positionMs(sampledPlaybackMs = 2_000, frameTimeNs = 0, isPlaying = true, playbackSpeed = 1f)
        val beforeSpeedChange = clock.positionMs(
            sampledPlaybackMs = 2_000,
            frameTimeNs = 100_000_000,
            isPlaying = true,
            playbackSpeed = 1f,
        )
        val changedSpeed = clock.positionMs(
            sampledPlaybackMs = 2_000,
            frameTimeNs = 116_000_000,
            isPlaying = true,
            playbackSpeed = 2f,
        )
        val afterSpeedChange = clock.positionMs(
            sampledPlaybackMs = 2_000,
            frameTimeNs = 132_000_000,
            isPlaying = true,
            playbackSpeed = 2f,
        )

        assertTrue(changedSpeed >= beforeSpeedChange)
        assertTrue(afterSpeedChange > changedSpeed)
        assertTrue(afterSpeedChange in 2_130.0..2_134.5)
    }

    @Test
    fun pausingKeepsLastPredictedPositionWhenSampleLags() {
        val clock = DanmakuPlaybackClock()

        clock.positionMs(sampledPlaybackMs = 3_000, frameTimeNs = 0, isPlaying = true, playbackSpeed = 1f)
        val beforePause = clock.positionMs(
            sampledPlaybackMs = 3_000,
            frameTimeNs = 100_000_000,
            isPlaying = true,
            playbackSpeed = 1f,
        )
        val paused = clock.positionMs(
            sampledPlaybackMs = 3_000,
            frameTimeNs = 116_000_000,
            isPlaying = false,
            playbackSpeed = 1f,
        )
        val stillPaused = clock.positionMs(
            sampledPlaybackMs = 3_000,
            frameTimeNs = 350_000_000,
            isPlaying = false,
            playbackSpeed = 1f,
        )

        assertTrue(paused >= beforePause)
        assertEquals(paused, stillPaused, 0.01)
    }
}
