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

    @Test
    fun usesMeasuredTextHeightToSpreadScrollingTracks() {
        val items = (0 until 12).map { index ->
            DanmakuItem(
                timeMs = 1_000L,
                text = "scroll-$index",
                mode = DanmakuMode.Scroll,
                platform = DanmakuPlatform.Local,
            )
        }

        val rendered = DanmakuLayoutEngine().layout(
            items = items,
            playbackMs = 1_100,
            widthPx = 1_920f,
            heightPx = 1_080f,
            profile = DanmakuProfile(DanmakuPlatform.Local),
            settings = DanmakuSettings(),
            measureText = { DanmakuTextMetrics(textSizePx = 64f, widthPx = 220f, lineHeightPx = 84f, baselineOffsetPx = 66f) },
        )

        assertEquals(12, rendered.size)
        assertTrue(rendered.maxOf { it.y } - rendered.minOf { it.y } > 650f)
    }

    @Test
    fun topDanmakuUsesSeparateFixedLanes() {
        val items = (0 until 3).map { index ->
            DanmakuItem(
                timeMs = 1_000L,
                text = "top-$index",
                mode = DanmakuMode.Top,
                platform = DanmakuPlatform.Local,
            )
        }

        val rendered = DanmakuLayoutEngine().layout(
            items = items,
            playbackMs = 1_500,
            widthPx = 1_920f,
            heightPx = 1_080f,
            profile = DanmakuProfile(DanmakuPlatform.Local),
            settings = DanmakuSettings(),
            measureText = { DanmakuTextMetrics(textSizePx = 64f, widthPx = 220f, lineHeightPx = 84f, baselineOffsetPx = 66f) },
        )

        assertEquals(3, rendered.size)
        assertEquals(3, rendered.map { it.lane }.distinct().size)
    }

    @Test
    fun dropsScrollingDanmakuWhenNoLaneCanAvoidCollision() {
        val items = (0 until 5).map { index ->
            DanmakuItem(
                timeMs = 1_000L,
                text = "dense-$index",
                mode = DanmakuMode.Scroll,
                platform = DanmakuPlatform.Local,
            )
        }

        val rendered = DanmakuLayoutEngine().layout(
            items = items,
            playbackMs = 1_100,
            widthPx = 720f,
            heightPx = 180f,
            profile = DanmakuProfile(DanmakuPlatform.Local),
            settings = DanmakuSettings(),
            measureText = { DanmakuTextMetrics(textSizePx = 64f, widthPx = 240f, lineHeightPx = 90f, baselineOffsetPx = 70f) },
        )

        assertEquals(2, rendered.size)
    }

    @Test
    fun preparedLayoutRendersFramesWithoutRemeasuring() {
        val items = listOf(
            DanmakuItem(
                timeMs = 1_000L,
                text = "smooth-scroll",
                mode = DanmakuMode.Scroll,
                platform = DanmakuPlatform.Local,
            ),
        )
        var measureCalls = 0

        val prepared = DanmakuLayoutEngine().prepare(
            items = items,
            widthPx = 1_920f,
            heightPx = 1_080f,
            profile = DanmakuProfile(DanmakuPlatform.Local),
            settings = DanmakuSettings(),
            measureText = {
                measureCalls += 1
                DanmakuTextMetrics(textSizePx = 64f, widthPx = 260f, lineHeightPx = 84f, baselineOffsetPx = 66f)
            },
        )

        val first = prepared.render(playbackMs = 1_100, alpha = 0.9f).single()
        val second = prepared.render(playbackMs = 1_350, alpha = 0.9f).single()

        assertEquals(1, measureCalls)
        assertTrue(second.x < first.x)
    }
}
