package com.zfbml.aggregate.source.rule

import com.zfbml.aggregate.source.SourceCapability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleSourceParserTest {
    @Test
    fun decodesRuleSource() {
        val json = """
            {
              "id": "rule-a",
              "name": "Rule A",
              "version": "1",
              "author": "test",
              "baseUrl": "https://example.invalid",
              "capabilities": ["SEARCH", "STREAM"],
              "search": {
                "path": "/s?q={query}",
                "itemSelector": ".item",
                "titleSelector": ".title",
                "urlSelector": "a"
              },
              "stream": {
                "urlSelector": "video source"
              }
            }
        """.trimIndent()

        val source = RuleSourceParser().decode(json)

        assertEquals("rule-a", source.id)
        assertTrue(SourceCapability.SEARCH in source.capabilities)
        assertEquals("/s?q={query}", source.search?.path)
    }
}
