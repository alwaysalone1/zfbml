package com.zfbml.aggregate.download

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import java.io.File
import java.util.concurrent.Executors

@OptIn(UnstableApi::class)
object MediaDownloadModule {
    private const val DOWNLOAD_FOLDER = "media3_downloads"

    @Volatile
    private var databaseProvider: StandaloneDatabaseProvider? = null

    @Volatile
    private var cache: SimpleCache? = null

    @Volatile
    private var manager: DownloadManager? = null

    fun downloadManager(context: Context): DownloadManager {
        return manager ?: synchronized(this) {
            manager ?: DownloadManager(
                context.applicationContext,
                database(context),
                downloadCache(context),
                DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true),
                Executors.newFixedThreadPool(4),
            ).also { manager = it }
        }
    }

    fun downloadCache(context: Context): Cache {
        return cache ?: synchronized(this) {
            cache ?: SimpleCache(
                File(context.filesDir, DOWNLOAD_FOLDER),
                NoOpCacheEvictor(),
                database(context),
            ).also { cache = it }
        }
    }

    private fun database(context: Context): StandaloneDatabaseProvider {
        return databaseProvider ?: synchronized(this) {
            databaseProvider ?: StandaloneDatabaseProvider(context.applicationContext).also {
                databaseProvider = it
            }
        }
    }
}
