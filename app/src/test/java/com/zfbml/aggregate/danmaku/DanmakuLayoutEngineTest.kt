package com.zfbml.aggregate.danmaku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DanmakuLayoutEngineTest {
    @Test
    fun layoutsActiveDanmakuOnly() {
        val items = listOf(
            DanmakuItem(1_000, "active", DanmakuMode.Scroll, platform = DanmakuPlatform.Local),
            DanmakuItem(20_000, "future", DanmakuMode.Scroll, platform = DanmakuPlatform.Local),
        )

        val rendered = DanmakuLayoutEngine().layout(
            items = items,
            playbackMs = 2_000,
            widthPx = 1_920f,
            heightPx = 1_080f,
            profile = DanmakuProfile(DanmakuPlatform.Local),
            settings = DanmakuSettings(),
        )

        assertEquals(1, rendered.size)
        assertEquals("active", rendered.first().item.text)
        assertTrue(rendered.first().x in -500f..1_920f)
    }
}
