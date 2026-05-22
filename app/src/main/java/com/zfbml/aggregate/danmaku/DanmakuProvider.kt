package com.zfbml.aggregate.danmaku

import com.zfbml.aggregate.source.Episode
import com.zfbml.aggregate.source.MediaDetail

interface DanmakuProvider {
    val id: String
    val platform: DanmakuPlatform
    val profile: DanmakuProfile
    val authDomain: String?

    suspend fun match(detail: MediaDetail, episode: Episode): List<DanmakuMatch>

    suspend fun fetchTimeline(match: DanmakuMatch): List<DanmakuItem>

    fun normalize(raw: String): List<DanmakuItem>
}
