package com.zfbml.aggregate.danmaku

import com.zfbml.aggregate.auth.AuthSessionStore
import com.zfbml.aggregate.source.Episode
import com.zfbml.aggregate.source.MediaDetail
import kotlinx.coroutines.delay

abstract class CookieScopedDanmakuProvider(
    final override val id: String,
    final override val platform: DanmakuPlatform,
    final override val authDomain: String,
    final override val profile: DanmakuProfile,
    private val authSessionStore: AuthSessionStore,
) : DanmakuProvider {
    protected fun cookieHeader(): String? = authSessionStore.cookieHeader(authDomain)

    override suspend fun match(detail: MediaDetail, episode: Episode): List<DanmakuMatch> {
        delay(40)
        val hasCookie = cookieHeader().isNullOrBlank().not()
        val score = if (hasCookie) 80 else 45
        return listOf(
            DanmakuMatch(
                providerId = id,
                platform = platform,
                title = detail.title,
                episodeTitle = episode.title,
                score = score,
                token = "${detail.title}|${episode.title}|$platform",
            ),
        )
    }

    override suspend fun fetchTimeline(match: DanmakuMatch): List<DanmakuItem> {
        return sampleTimeline(match.platform)
    }

    override fun normalize(raw: String): List<DanmakuItem> = emptyList()

    private fun sampleTimeline(platform: DanmakuPlatform): List<DanmakuItem> {
        return listOf(
            DanmakuItem(1_200, "Opening line from $platform", DanmakuMode.Scroll, 0xFFFFFFFF, platform = platform),
            DanmakuItem(2_800, "Top pinned style", DanmakuMode.Top, 0xFFFFD54F, platform = platform),
            DanmakuItem(4_000, "Bottom subtitle style", DanmakuMode.Bottom, 0xFF80CBC4, platform = platform),
            DanmakuItem(6_200, "High density renderer check", DanmakuMode.Scroll, 0xFFFFFFFF, platform = platform),
        )
    }
}

class BilibiliDanmakuProvider(authSessionStore: AuthSessionStore) : CookieScopedDanmakuProvider(
    id = "danmaku-bilibili",
    platform = DanmakuPlatform.Bilibili,
    authDomain = "bilibili.com",
    profile = DanmakuProfile(
        platform = DanmakuPlatform.Bilibili,
        supportsAdvanced = true,
        strokeWidthPx = 5f,
        maxTracks = 14,
    ),
    authSessionStore = authSessionStore,
) {
    override fun normalize(raw: String): List<DanmakuItem> = BilibiliDanmakuParser.parseXml(raw)
}

class TencentDanmakuProvider(authSessionStore: AuthSessionStore) : CookieScopedDanmakuProvider(
    id = "danmaku-tencent",
    platform = DanmakuPlatform.Tencent,
    authDomain = "v.qq.com",
    profile = DanmakuProfile(platform = DanmakuPlatform.Tencent, strokeWidthPx = 4f),
    authSessionStore = authSessionStore,
)

class IqiyiDanmakuProvider(authSessionStore: AuthSessionStore) : CookieScopedDanmakuProvider(
    id = "danmaku-iqiyi",
    platform = DanmakuPlatform.Iqiyi,
    authDomain = "iqiyi.com",
    profile = DanmakuProfile(platform = DanmakuPlatform.Iqiyi, shadowRadiusPx = 5f),
    authSessionStore = authSessionStore,
)

class YoukuDanmakuProvider(authSessionStore: AuthSessionStore) : CookieScopedDanmakuProvider(
    id = "danmaku-youku",
    platform = DanmakuPlatform.Youku,
    authDomain = "youku.com",
    profile = DanmakuProfile(platform = DanmakuPlatform.Youku, fontScale = 0.96f),
    authSessionStore = authSessionStore,
)

class DanmakuRegistry(
    providers: List<DanmakuProvider>,
) {
    private val byId = providers.associateBy { it.id }
    val profiles: List<DanmakuProfile> = providers.map { it.profile }

    fun provider(id: String): DanmakuProvider? = byId[id]

    suspend fun matchAll(detail: MediaDetail, episode: Episode): List<DanmakuMatch> {
        return byId.values.flatMap { provider ->
            runCatching { provider.match(detail, episode) }.getOrDefault(emptyList())
        }.sortedByDescending { it.score }
    }
}
