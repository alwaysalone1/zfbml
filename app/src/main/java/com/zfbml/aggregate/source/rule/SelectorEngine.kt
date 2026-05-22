package com.zfbml.aggregate.source.rule

import org.jsoup.nodes.Element
import org.jsoup.select.Elements

object SelectorEngine {
    fun select(root: Element, selector: String?): Elements {
        if (selector.isNullOrBlank()) return Elements()
        return root.select(normalize(selector))
    }

    fun text(root: Element, selector: String?): String {
        val element = select(root, selector).firstOrNull() ?: return ""
        return element.text().trim()
    }

    fun attr(root: Element, selector: String?, attr: String): String {
        val element = select(root, selector).firstOrNull() ?: return ""
        return element.attr(attr).trim()
    }

    private fun normalize(selector: String): String {
        return when {
            selector.startsWith("css:", ignoreCase = true) -> selector.substringAfter(':')
            selector.startsWith("xpath:", ignoreCase = true) -> error(
                "XPath selectors are reserved in the rule schema but not enabled in this build. Use css: selectors.",
            )
            else -> selector
        }
    }
}
