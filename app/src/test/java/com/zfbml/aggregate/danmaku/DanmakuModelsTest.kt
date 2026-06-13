package com.zfbml.aggregate.danmaku

import org.junit.Assert.assertEquals
import org.junit.Test

class DanmakuModelsTest {
    @Test
    fun danmakuEffectStylePreferenceRoundTripsKnownStyles() {
        DanmakuEffectStyle.entries.forEach { style ->
            assertEquals(style, danmakuEffectStyleFromPreference(style.toPreferenceValue()))
        }
    }

    @Test
    fun danmakuEffectStylePreferenceFallsBackToPlatformAdaptiveForMissingOrStaleValues() {
        assertEquals(DanmakuEffectStyle.PlatformAdaptive, danmakuEffectStyleFromPreference(null))
        assertEquals(DanmakuEffectStyle.PlatformAdaptive, danmakuEffectStyleFromPreference(""))
        assertEquals(DanmakuEffectStyle.PlatformAdaptive, danmakuEffectStyleFromPreference("LegacyGlow"))
    }

    @Test
    fun playerDanmakuDisplayPreferencesClampPersistedSliderValues() {
        assertEquals(PLAYER_DANMAKU_DENSITY_DEFAULT, normalizePlayerDanmakuDensityPreference(Float.NaN))
        assertEquals(0.3f, normalizePlayerDanmakuDensityPreference(0.1f))
        assertEquals(1f, normalizePlayerDanmakuDensityPreference(1.6f))
        assertEquals(0.62f, normalizePlayerDanmakuDensityPreference(0.62f))

        assertEquals(PLAYER_DANMAKU_ALPHA_DEFAULT, normalizePlayerDanmakuAlphaPreference(Float.POSITIVE_INFINITY))
        assertEquals(0.35f, normalizePlayerDanmakuAlphaPreference(0.1f))
        assertEquals(1f, normalizePlayerDanmakuAlphaPreference(1.2f))
        assertEquals(0.76f, normalizePlayerDanmakuAlphaPreference(0.76f))

        assertEquals(PLAYER_DANMAKU_FONT_SCALE_DEFAULT, normalizePlayerDanmakuFontScalePreference(Float.NEGATIVE_INFINITY))
        assertEquals(0.62f, normalizePlayerDanmakuFontScalePreference(0.2f))
        assertEquals(1.08f, normalizePlayerDanmakuFontScalePreference(1.5f))
        assertEquals(0.72f, normalizePlayerDanmakuFontScalePreference(0.72f))
    }
}
