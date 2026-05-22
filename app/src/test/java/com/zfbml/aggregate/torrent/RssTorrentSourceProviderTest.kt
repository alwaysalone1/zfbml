package com.zfbml.aggregate.torrent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RssTorrentSourceProviderTest {
    private val config = RssTorrentSourceConfig(
        id = "test",
        name = "Test RSS",
        domains = setOf("example.test"),
        searchUrlTemplate = "https://example.test/rss?q={query}",
    )

    @Test
    fun parsesDmhyMagnetEnclosure() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <rss version="2.0">
              <channel>
                <item>
                  <title><![CDATA[[Group] Demo Anime - 03 [1080p]]]></title>
                  <link>https://share.dmhy.org/topics/view/1.html</link>
                  <pubDate>Fri, 22 May 2026 14:35:35 +0800</pubDate>
                  <description><![CDATA[Demo description]]></description>
                  <enclosure url="magnet:?xt=urn:btih:ABCDEF123456&amp;dn=Demo&amp;tr=https%3A%2F%2Ftracker.example%2Fannounce" length="1" type="application/x-bittorrent" />
                </item>
              </channel>
            </rss>
        """.trimIndent()

        val items = RssTorrentParser.parse(xml, config)

        assertEquals(1, items.size)
        assertEquals("magnet:?xt=urn:btih:ABCDEF123456&dn=Demo&tr=https%3A%2F%2Ftracker.example%2Fannounce", items.first().torrentUrl)
        assertEquals("ABCDEF123456", items.first().infoHash)
        assertEquals("1080p", items.first().quality)
        assertEquals("Group", items.first().subgroup)
    }

    @Test
    fun parsesNyaaNamespacedMetadata() {
        val xml = """
            <rss xmlns:nyaa="https://nyaa.si/xmlns/nyaa" version="2.0">
              <channel>
                <item>
                  <title>[SubsPlease] Demo Anime - 04 (720p)</title>
                  <link>https://nyaa.si/download/100.torrent</link>
                  <guid>https://nyaa.si/view/100</guid>
                  <nyaa:seeders>42</nyaa:seeders>
                  <nyaa:infoHash>2fd2133f28fbd29021b775c17c8e1cf5aceb9a15</nyaa:infoHash>
                  <nyaa:size>714.6 MiB</nyaa:size>
                </item>
              </channel>
            </rss>
        """.trimIndent()

        val item = RssTorrentParser.parse(xml, config).single()

        assertEquals("https://nyaa.si/download/100.torrent", item.torrentUrl)
        assertEquals(42, item.seeders)
        assertEquals("2fd2133f28fbd29021b775c17c8e1cf5aceb9a15", item.infoHash)
        assertTrue(item.sizeBytes in 749_300_000L..749_400_000L)
    }

    @Test
    fun parsesMikanTorrentEnclosureLength() {
        val xml = """
            <rss version="2.0">
              <channel>
                <item>
                  <title>[ANi] Demo Anime - 05 [1080P][Baha][WEB-DL]</title>
                  <link>https://mikanani.me/Home/Episode/hash</link>
                  <description>[ANi] Demo Anime - 05 [1080P][Baha][WEB-DL][679.9 MB]</description>
                  <enclosure type="application/x-bittorrent" length="712926848" url="https://mikanani.me/Download/20260522/hash.torrent" />
                </item>
              </channel>
            </rss>
        """.trimIndent()

        val item = RssTorrentParser.parse(xml, config).single()

        assertEquals("https://mikanani.me/Download/20260522/hash.torrent", item.torrentUrl)
        assertEquals(712926848L, item.sizeBytes)
        assertEquals("1080p", item.quality)
    }
}
