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
}
