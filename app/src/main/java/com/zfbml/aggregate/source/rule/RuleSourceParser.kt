package com.zfbml.aggregate.source.rule

import kotlinx.serialization.json.Json

class RuleSourceParser(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    },
) {
    fun decode(text: String): RuleSource = json.decodeFromString(RuleSource.serializer(), text)

    fun encode(source: RuleSource): String = json.encodeToString(RuleSource.serializer(), source)
}
