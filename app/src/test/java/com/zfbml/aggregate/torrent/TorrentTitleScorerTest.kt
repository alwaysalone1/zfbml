package com.zfbml.aggregate.torrent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TorrentTitleScorerTest {
    @Test
    fun extractsEpisodeAndQuality() {
        val title = "[SubsPlease] Demo Anime - 03 (1080p) [ABCDEF]"

        assertEquals(3, TorrentTitleScorer.extractEpisode(title))
        assertEquals("1080p", TorrentTitleScorer.extractQuality(title))
        assertEquals(5, TorrentTitleScorer.extractEpisode("[Group] Demo Anime \u7b2c 5 \u8bdd [1080p]"))
    }

    @Test
    fun scoresMatchingHighQualityTorrentHigher() {
        val low = TorrentCandidate(
            title = "Other Show 01 720p",
            magnetUri = "magnet:?xt=urn:btih:1",
            seeders = 1,
        )
        val high = TorrentCandidate(
            title = "[Group] Demo Anime 01 1080p",
            magnetUri = "magnet:?xt=urn:btih:2",
            seeders = 80,
        )

        assertTrue(TorrentTitleScorer.score("Demo Anime", high) > TorrentTitleScorer.score("Demo Anime", low))
    }
}
