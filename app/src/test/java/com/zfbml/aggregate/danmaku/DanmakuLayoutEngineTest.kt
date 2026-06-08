package com.zfbml.aggregate.danmaku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DanmakuLayoutEngineTest {
    @Test
    fun activeItemLimitScalesWithVisibleWindow() {
        assertEquals(
            100,
            danmakuActiveItemLimit(maxItemsPerMinute = 600, density = 1f, activeWindowMs = 10_000L),
        )
        assertEquals(
            50,
            danmakuActiveItemLimit(maxItemsPerMinute = 600, density = 0.5f, activeWindowMs = 10_000L),
        )
        assertEquals(
            1,
            danmakuActiveItemLimit(maxItemsPerMinute = 2, density = 1f, activeWindowMs = 8_500L),
        )
    }

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
    fun safeAreaKeepsDanmakuAwayFromPlayerOverlays() {
        val items = listOf(
            DanmakuItem(1_000L, "top", DanmakuMode.Top, platform = DanmakuPlatform.Local),
            DanmakuItem(1_000L, "bottom", DanmakuMode.Bottom, platform = DanmakuPlatform.Local),
            DanmakuItem(1_000L, "advanced", DanmakuMode.Advanced, position = DanmakuPosition(0.5f, 0.5f, 4_500L), platform = DanmakuPlatform.Local),
        )

        val rendered = DanmakuLayoutEngine().layout(
            items = items,
            playbackMs = 1_500,
            widthPx = 1_920f,
            heightPx = 1_080f,
            profile = DanmakuProfile(DanmakuPlatform.Local, supportsAdvanced = true),
            settings = DanmakuSettings(),
            safeArea = DanmakuSafeArea(
                topInsetPx = 90f,
                bottomInsetPx = 180f,
                startInsetPx = 120f,
                endInsetPx = 360f,
            ),
            measureText = { DanmakuTextMetrics(textSizePx = 64f, widthPx = 220f, lineHeightPx = 84f, baselineOffsetPx = 66f) },
        )

        val safeTop = 90f
        val safeBottom = 1_080f - 180f
        val safeStart = 120f
        val safeEnd = 1_920f - 360f

        assertEquals(3, rendered.size)
        assertTrue(rendered.all { it.y in safeTop..safeBottom })
        assertTrue(rendered.all { it.x >= safeStart && it.x + it.widthPx <= safeEnd })
        assertTrue(rendered.first { it.item.text == "top" }.y >= safeTop)
        assertTrue(rendered.first { it.item.text == "bottom" }.y <= safeBottom)
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

    @Test
    fun preparedLayoutSupportsFractionalFrameProgress() {
        val items = listOf(
            DanmakuItem(
                timeMs = 1_000L,
                text = "fractional-scroll",
                mode = DanmakuMode.Scroll,
                platform = DanmakuPlatform.Local,
            ),
        )

        val prepared = DanmakuLayoutEngine().prepare(
            items = items,
            widthPx = 1_920f,
            heightPx = 1_080f,
            profile = DanmakuProfile(DanmakuPlatform.Local),
            settings = DanmakuSettings(),
            measureText = { DanmakuTextMetrics(textSizePx = 64f, widthPx = 260f, lineHeightPx = 84f, baselineOffsetPx = 66f) },
        )

        val wholeMs = prepared.render(playbackMs = 1_100.0, alpha = 0.9f).single()
        val halfMs = prepared.render(playbackMs = 1_100.5, alpha = 0.9f).single()

        assertTrue(halfMs.x < wholeMs.x)
        assertTrue(wholeMs.x - halfMs.x in 0.05f..0.25f)
    }

    @Test
    fun visibleTraversalMatchesRenderedList() {
        val items = (0 until 4).map { index ->
            DanmakuItem(
                timeMs = 1_000L + index * 80L,
                text = "visible-$index",
                mode = DanmakuMode.Scroll,
                platform = DanmakuPlatform.Local,
            )
        }

        val prepared = DanmakuLayoutEngine().prepare(
            items = items,
            widthPx = 1_920f,
            heightPx = 1_080f,
            profile = DanmakuProfile(DanmakuPlatform.Local),
            settings = DanmakuSettings(),
            measureText = { DanmakuTextMetrics(textSizePx = 64f, widthPx = 260f, lineHeightPx = 84f, baselineOffsetPx = 66f) },
        )
        val rendered = prepared.render(playbackMs = 1_400.25, alpha = 0.76f)
        val traversed = mutableListOf<Pair<String, Float>>()

        prepared.forEachVisible(playbackMs = 1_400.25, alpha = 0.76f) { entry, x, _ ->
            traversed += entry.item.text to x
        }

        assertEquals(rendered.map { it.item.text to it.x }, traversed)
    }

    @Test
    fun densePreparedLayoutCapsRenderedItemsByVisibleWindow() {
        val items = (0 until 300).map { index ->
            DanmakuItem(
                timeMs = 1_000L + index,
                text = "dense-$index",
                mode = DanmakuMode.Advanced,
                position = DanmakuPosition(x = 0.5f, y = 0.2f, durationMs = 10_000L),
                platform = DanmakuPlatform.Local,
            )
        }

        val prepared = DanmakuLayoutEngine().prepare(
            items = items,
            widthPx = 1_920f,
            heightPx = 1_080f,
            profile = DanmakuProfile(
                platform = DanmakuPlatform.Local,
                maxItemsPerMinute = 600,
            ),
            settings = DanmakuSettings(),
            measureText = {
                DanmakuTextMetrics(
                    textSizePx = 64f,
                    widthPx = 220f,
                    lineHeightPx = 84f,
                    baselineOffsetPx = 66f,
                )
            },
        )

        val rendered = prepared.render(playbackMs = 1_500.0, alpha = 0.8f)
        val expectedFullAlphaLimit = danmakuActiveItemLimit(
            maxItemsPerMinute = 600,
            density = 1f,
            activeWindowMs = 10_500L,
        )

        assertEquals(expectedFullAlphaLimit + 4, rendered.size)
        assertEquals("dense-${300 - rendered.size}", rendered.first().item.text)
        assertEquals("dense-299", rendered.last().item.text)
    }

    @Test
    fun overflowVisibleDanmakuFadesInsteadOfHardDropping() {
        val items = (0 until 5).map { index ->
            DanmakuItem(
                timeMs = 1_000L,
                text = "overflow-$index",
                mode = DanmakuMode.Scroll,
                platform = DanmakuPlatform.Local,
            )
        }

        val prepared = DanmakuLayoutEngine().prepare(
            items = items,
            widthPx = 1_920f,
            heightPx = 1_080f,
            profile = DanmakuProfile(
                platform = DanmakuPlatform.Local,
                maxTracks = 8,
                maxItemsPerMinute = 2,
            ),
            settings = DanmakuSettings(),
            measureText = { DanmakuTextMetrics(textSizePx = 64f, widthPx = 220f, lineHeightPx = 84f, baselineOffsetPx = 66f) },
        )

        val rendered = prepared.render(playbackMs = 1_100.0, alpha = 0.8f)

        assertEquals(5, rendered.size)
        assertTrue(rendered.take(4).all { it.alpha < 0.8f })
        assertEquals(0.8f, rendered.last().alpha, 0.001f)
    }

    @Test
    fun fixedDanmakuFadesInAndOutWithoutAffectingScrollAlpha() {
        val items = listOf(
            DanmakuItem(
                timeMs = 1_000L,
                text = "fixed",
                mode = DanmakuMode.Top,
                platform = DanmakuPlatform.Local,
            ),
            DanmakuItem(
                timeMs = 1_000L,
                text = "scroll",
                mode = DanmakuMode.Scroll,
                platform = DanmakuPlatform.Local,
            ),
        )

        val prepared = DanmakuLayoutEngine().prepare(
            items = items,
            widthPx = 1_920f,
            heightPx = 1_080f,
            profile = DanmakuProfile(
                platform = DanmakuPlatform.Local,
                topDurationMs = 1_000L,
            ),
            settings = DanmakuSettings(),
            measureText = { DanmakuTextMetrics(textSizePx = 64f, widthPx = 220f, lineHeightPx = 84f, baselineOffsetPx = 66f) },
        )

        val fadeInFrame = prepared.render(playbackMs = 1_050.0, alpha = 0.8f)
        val fullFrame = prepared.render(playbackMs = 1_500.0, alpha = 0.8f)
        val fadeOutFrame = prepared.render(playbackMs = 1_950.0, alpha = 0.8f)

        assertTrue(fadeInFrame.first { it.item.text == "fixed" }.alpha < 0.4f)
        assertEquals(0.8f, fullFrame.first { it.item.text == "fixed" }.alpha, 0.001f)
        assertTrue(fadeOutFrame.first { it.item.text == "fixed" }.alpha < 0.4f)
        assertEquals(0.8f, fadeInFrame.first { it.item.text == "scroll" }.alpha, 0.001f)
    }
}
