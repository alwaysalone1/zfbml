package com.zfbml.aggregate.source.rule

import com.zfbml.aggregate.source.SourceCapability
import com.zfbml.aggregate.source.StreamProtocol
import kotlinx.serialization.Serializable

@Serializable
data class RuleSource(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val baseUrl: String,
    val capabilities: Set<SourceCapability>,
    val domains: Set<String> = emptySet(),
    val requiresWebView: Boolean = false,
    val supportsDownload: Boolean = false,
    val defaults: RuleRequestDefaults = RuleRequestDefaults(),
    val search: RuleSearch? = null,
    val detail: RuleDetail? = null,
    val episodes: RuleEpisodes? = null,
    val stream: RuleStream? = null,
)

@Serializable
data class RuleRequestDefaults(
    val userAgent: String? = null,
    val referer: String? = null,
    val headers: Map<String, String> = emptyMap(),
)

@Serializable
enum class RuleHttpMethod {
    GET,
    POST,
}

@Serializable
data class RuleSearch(
    val path: String,
    val method: RuleHttpMethod = RuleHttpMethod.GET,
    val bodyTemplate: String? = null,
    val itemSelector: String,
    val titleSelector: String,
    val urlSelector: String,
    val urlAttribute: String = "href",
    val posterSelector: String? = null,
    val posterAttribute: String = "src",
    val subtitleSelector: String? = null,
)

@Serializable
data class RuleDetail(
    val titleSelector: String,
    val summarySelector: String? = null,
    val posterSelector: String? = null,
    val posterAttribute: String = "src",
)

@Serializable
data class RuleEpisodes(
    val itemSelector: String,
    val titleSelector: String,
    val urlSelector: String,
    val urlAttribute: String = "href",
)

@Serializable
data class RuleStream(
    val urlSelector: String,
    val urlAttribute: String = "src",
    val qualitySelector: String? = null,
    val codecSelector: String? = null,
    val protocol: StreamProtocol = StreamProtocol.UNKNOWN,
)
