package com.zfbml.aggregate

import android.content.Context
import com.zfbml.aggregate.danmaku.BilibiliDanmakuProvider
import com.zfbml.aggregate.danmaku.DanmakuRegistry
import com.zfbml.aggregate.danmaku.IqiyiDanmakuProvider
import com.zfbml.aggregate.danmaku.TencentDanmakuProvider
import com.zfbml.aggregate.danmaku.YoukuDanmakuProvider
import com.zfbml.aggregate.download.Media3DownloadCoordinator
import com.zfbml.aggregate.download.YtDlpAdvancedDownloadProvider
import com.zfbml.aggregate.network.Ipv4FirstDns
import com.zfbml.aggregate.source.DirectUrlSourceProvider
import com.zfbml.aggregate.source.SourceProvider
import com.zfbml.aggregate.source.SourceRegistry
import com.zfbml.aggregate.source.catalog.BangumiCatalogSourceProvider
import com.zfbml.aggregate.source.catalog.BangumiCategoryRepository
import com.zfbml.aggregate.source.catalog.BangumiCalendarRepository
import com.zfbml.aggregate.source.online.AnimekoOnlineSourceProvider
import com.zfbml.aggregate.source.rule.RuleSourceParser
import com.zfbml.aggregate.source.rule.RuleSourceProvider
import com.zfbml.aggregate.torrent.LibtorrentEngine
import com.zfbml.aggregate.torrent.RssTorrentSourceProvider
import com.zfbml.aggregate.torrent.TorrentSourceProvider
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

class AppGraph(
    private val context: Context,
) {
    private val httpClient = OkHttpClient.Builder()
        .dns(Ipv4FirstDns)
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .callTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val resourceProviders: List<SourceProvider> by lazy {
        buildList {
            add(AnimekoOnlineSourceProvider(context.applicationContext, httpClient))
            add(TorrentSourceProvider())
            add(RssTorrentSourceProvider.mikan(httpClient))
            add(RssTorrentSourceProvider.dmhy(httpClient))
            add(RssTorrentSourceProvider.acgRip(httpClient))
            add(RssTorrentSourceProvider.nyaa(httpClient))
            add(RssTorrentSourceProvider.bangumiMoe(httpClient))
        }
    }

    val sourceRegistry: SourceRegistry by lazy {
        SourceRegistry(
            buildList {
                add(BangumiCatalogSourceProvider(httpClient, resourceProviders))
                addAll(resourceProviders)
                add(DirectUrlSourceProvider())
                addAll(loadBundledRuleProviders())
            },
        )
    }

    val bangumiCalendarRepository: BangumiCalendarRepository by lazy {
        BangumiCalendarRepository(httpClient)
    }

    val bangumiCategoryRepository: BangumiCategoryRepository by lazy {
        BangumiCategoryRepository(httpClient)
    }

    val danmakuRegistry: DanmakuRegistry by lazy {
        DanmakuRegistry(
            listOf(
                BilibiliDanmakuProvider(httpClient),
                TencentDanmakuProvider(httpClient),
                IqiyiDanmakuProvider(httpClient),
                YoukuDanmakuProvider(httpClient),
            ),
        )
    }

    val media3DownloadCoordinator: Media3DownloadCoordinator by lazy {
        Media3DownloadCoordinator(context.applicationContext)
    }

    val advancedDownloadProvider = YtDlpAdvancedDownloadProvider()

    val torrentEngine: LibtorrentEngine by lazy {
        LibtorrentEngine(context.applicationContext)
    }

    private fun loadBundledRuleProviders(): List<SourceProvider> {
        val parser = RuleSourceParser()
        val files = context.assets.list("rules").orEmpty()
        return files.mapNotNull { file ->
            runCatching {
                context.assets.open("rules/$file").bufferedReader().use { reader ->
                    RuleSourceProvider(parser.decode(reader.readText()), httpClient)
                }
            }.getOrNull()
        }
    }
}
