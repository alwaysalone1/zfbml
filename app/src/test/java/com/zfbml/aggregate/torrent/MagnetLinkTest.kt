package com.zfbml.aggregate.torrent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MagnetLinkTest {
    @Test
    fun parsesMagnetLink() {
        val link = MagnetLink.parse(
            "magnet:?xt=urn:btih:ABCDEF123456&dn=Demo%20Episode&tr=https%3A%2F%2Ftracker.example%2Fannounce",
        )

        assertNotNull(link)
        assertEquals("ABCDEF123456", link?.infoHash)
        assertEquals("Demo Episode", link?.displayName)
        assertEquals(listOf("https://tracker.example/announce"), link?.trackers)
    }

    @Test
    fun buildsMagnetLink() {
        val uri = MagnetLink.build(
            infoHash = "ABCDEF123456",
            displayName = "Demo Episode",
            trackers = listOf("https://tracker.example/announce"),
        )

        val link = MagnetLink.parse(uri)

        assertEquals("ABCDEF123456", link?.infoHash)
        assertEquals("Demo Episode", link?.displayName)
    }
}
