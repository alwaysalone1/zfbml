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

    @Test
    fun playbackSamplingUsesLowerRateThanFrameRendering() {
        assertEquals(null, danmakuPlaybackSampleDelayMs(enabled = false, hasItems = true, isPlaying = true))
        assertEquals(null, danmakuPlaybackSampleDelayMs(enabled = true, hasItems = false, isPlaying = true))
        assertEquals(96L, danmakuPlaybackSampleDelayMs(enabled = true, hasItems = true, isPlaying = true))
        assertEquals(250L, danmakuPlaybackSampleDelayMs(enabled = true, hasItems = true, isPlaying = false))
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
