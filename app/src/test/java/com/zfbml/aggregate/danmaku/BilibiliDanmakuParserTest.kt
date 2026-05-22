package com.zfbml.aggregate.danmaku

import org.junit.Assert.assertEquals
import org.junit.Test

class BilibiliDanmakuParserTest {
    @Test
    fun parsesXmlDanmaku() {
        val xml = """<i><d p="1.5,5,25,16777215,0,0,userhash,42">hello</d></i>"""

        val items = BilibiliDanmakuParser.parseXml(xml)

        assertEquals(1, items.size)
        assertEquals(1_500, items.first().timeMs)
        assertEquals(DanmakuMode.Top, items.first().mode)
        assertEquals("hello", items.first().text)
    }
}
