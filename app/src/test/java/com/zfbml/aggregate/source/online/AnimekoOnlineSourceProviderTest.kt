package com.zfbml.aggregate.source.online

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimekoOnlineSourceProviderTest {
    @Test
    fun searchKeywordCleansSpecialTitleAndUsesFirstAlias() {
        val keyword = normalizeOnlineSearchKeyword(
            raw = "败犬女主太多了！ / Makeine",
            removeSpecial = true,
            useOnlyFirstWord = true,
        )

        assertEquals("败犬女主太多了", keyword)
    }

    @Test
    fun titleMatchingAcceptsAliasesButRejectsUnrelatedShows() {
        assertTrue(titleMatchesQuery("前辈是男孩子", "前辈是男孩子"))
        assertTrue(titleMatchesQuery("Makeine 败犬女主太多了", "败犬女主太多了"))
        assertFalse(titleMatchesQuery("葬送的芙莉莲", "前辈是男孩子"))
    }

    @Test
    fun episodeParserHandlesArabicAndChineseNumbers() {
        val pattern = """第\s*(?<ep>.+)\s*[话集]"""

        assertEquals(3, parseEpisodeIndex("第 03 话", pattern))
        assertEquals(12, parseEpisodeIndex("第十二集", pattern))
        assertEquals(21, parseEpisodeIndex("第二十一话", pattern))
    }

    @Test
    fun macCmsUrlExtractorDecodesEncodedPlayerUrl() {
        val html = """
            <script>
            var player_aaaa={"encrypt":1,"url":"https%3A%2F%2Fcdn.example.test%2Fanime%2F01%2Findex.m3u8%3Ftoken%3Da%2526b"};
            </script>
        """.trimIndent()

        assertEquals(
            "https://cdn.example.test/anime/01/index.m3u8?token=a&b",
            AnimekoOnlineExtractors.extractMacCmsPlayerUrl(html),
        )
    }

    @Test
    fun configuredAndGenericVideoExtractorsFindPlayableUrls() {
        val pattern = """url=(?<v>https?://[^"']+?\.m3u8)"""
        val text = "window.player=\"url=https://cdn.example.test/path/playlist.m3u8\""

        assertEquals(
            "https://cdn.example.test/path/playlist.m3u8",
            AnimekoOnlineExtractors.findVideoByConfiguredRegex(pattern, text),
        )
        assertEquals(
            "https://cdn.example.test/video/01.mp4?auth=1",
            AnimekoOnlineExtractors.findGenericVideoUrl(
                """var src="https:\/\/cdn.example.test\/video\/01.mp4?auth=1";""",
            ),
        )
    }

    @Test
    fun fallbackConfigChannelRegexFiltersKnownBrokenLine() {
        val config = OnlineSelectorConfig(
            id = "online-omofun111",
            name = "omofun111",
            searchUrl = "https://example.test/search?wd={keyword}",
            baseUrl = "https://example.test/",
            matchChannelName = "^(?!.*\u9ad8\u6e05\u7ebf\u8def3).*$",
        )

        assertTrue(config.channelMatches("高清线路1"))
        assertFalse(config.channelMatches("高清线路3"))
    }
}
