package com.zfbml.aggregate.danmaku

import org.junit.Assert.assertEquals
import org.junit.Test

class DanmakuSurfaceTest {
    @Test
    fun frameDelayStopsWhenDisabledOrEmpty() {
        assertEquals(null, danmakuFrameDelayMs(enabled = false, hasItems = true, isPlaying = true))
        assertEquals(null, danmakuFrameDelayMs(enabled = true, hasItems = false, isPlaying = true))
    }

    @Test
    fun frameDelayUsesFullRateOnlyWhilePlaying() {
        assertEquals(0L, danmakuFrameDelayMs(enabled = true, hasItems = true, isPlaying = true))
        assertEquals(250L, danmakuFrameDelayMs(enabled = true, hasItems = true, isPlaying = false))
    }
}
