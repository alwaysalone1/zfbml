package com.zfbml.aggregate.danmaku

import org.junit.Assert.assertEquals
import org.junit.Test

class DanmakuSurfaceTest {
    @Test
    fun danmakuFillColorAppliesEntryAlphaToTextColor() {
        val color = danmakuFillColor(0x00ABCDEF, alpha = 0.5f)

        assertEquals(127, color ushr 24)
        assertEquals(0x00ABCDEF, color and 0x00FFFFFF)
    }

    @Test
    fun danmakuStrokeColorScalesWithEntryAlpha() {
        val full = danmakuStrokeColor(alpha = 1f)
        val faded = danmakuStrokeColor(alpha = 0.25f)
        val hidden = danmakuStrokeColor(alpha = 0f)

        assertEquals(204, full ushr 24)
        assertEquals(51, faded ushr 24)
        assertEquals(0, hidden ushr 24)
        assertEquals(0x000000, faded and 0x00FFFFFF)
    }

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

    @Test
    fun playbackSamplingUsesLowerRateThanFrameRendering() {
        assertEquals(null, danmakuPlaybackSampleDelayMs(enabled = false, hasItems = true, isPlaying = true))
        assertEquals(null, danmakuPlaybackSampleDelayMs(enabled = true, hasItems = false, isPlaying = true))
        assertEquals(32L, danmakuPlaybackSampleDelayMs(enabled = true, hasItems = true, isPlaying = true))
        assertEquals(250L, danmakuPlaybackSampleDelayMs(enabled = true, hasItems = true, isPlaying = false))
    }

    @Test
    fun frameTimeIsNotReadyUntilVsyncProvidesRealTimestamp() {
        assertEquals(false, danmakuFrameTimeReady(DanmakuFrameTimeUnsetNs))
        assertEquals(true, danmakuFrameTimeReady(0L))
        assertEquals(true, danmakuFrameTimeReady(16_666_667L))
    }

    @Test
    fun layoutCacheReusesEqualItemListsAcrossRefreshes() {
        val cache = DanmakuSurfaceLayoutCache()
        val profile = DanmakuProfile(DanmakuPlatform.Local)
        val settings = DanmakuSettings()
        val items = listOf(DanmakuItem(1_000, "smooth", DanmakuMode.Scroll, platform = DanmakuPlatform.Local))
        val refreshedItems = items.map { it.copy() }
        val changedItems = listOf(DanmakuItem(1_000, "changed", DanmakuMode.Scroll, platform = DanmakuPlatform.Local))
        var buildCount = 0

        fun cachedLayoutFor(itemsForLayout: List<DanmakuItem>) {
            cache.layoutFor(
                items = itemsForLayout,
                widthPx = 1_920f,
                heightPx = 1_080f,
                profile = profile,
                settings = settings,
                densityKey = 1f,
            ) {
                buildCount += 1
                PreparedDanmakuLayout.Empty
            }
        }

        cachedLayoutFor(items)
        cachedLayoutFor(refreshedItems)
        cachedLayoutFor(changedItems)

        assertEquals(2, buildCount)
    }
}
