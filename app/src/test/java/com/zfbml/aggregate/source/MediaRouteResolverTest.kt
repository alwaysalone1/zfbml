package com.zfbml.aggregate.source

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaRouteResolverTest {
    @Test
    fun requestIncludesCatalogAliases() {
        val episode = Episode(
            providerId = "bangumi-catalog",
            id = "ep-2",
            title = "Episode 2",
            url = "bangumi://subject/1/episode/2",
            index = 2,
            raw = mapOf(
                "subjectId" to "1",
                "subjectNameCn" to "Test CN",
                "subjectName" to "Test JP",
                "subjectAliases" to "Alias One|Alias Two",
                "episodeId" to "20",
            ),
        )

        val request = MediaFetchRequest.fromEpisode(episode)

        assertEquals("1", request.subjectId)
        assertEquals(2, request.episodeIndex)
        assertEquals(listOf("Test CN", "Alias One", "Alias Two", "Test JP"), request.subjectNames)
    }

    @Test
    fun requestKeepsChineseAliasesAheadOfForeignOriginalName() {
        val episode = Episode(
            providerId = "bangumi-catalog",
            id = "ep-1",
            title = "Episode 1",
            url = "bangumi://subject/1/episode/1",
            index = 1,
            raw = mapOf(
                "subjectId" to "1",
                "subjectNameCn" to "大闹天宫",
                "subjectName" to "Sun Ukun: Uproar in Heaven",
                "subjectAliases" to "大闹天宫 1961|Uproar in Heaven",
            ),
        )

        val request = MediaFetchRequest.fromEpisode(episode)

        assertEquals(
            listOf("大闹天宫", "大闹天宫 1961"),
            request.subjectNames,
        )
    }

    @Test
    fun resolverPromotesEpisodeMatchedRoute() = runTest {
        val provider = FakeRouteProvider()
        val request = MediaFetchRequest.fromEpisode(
            Episode(
                providerId = "bangumi-catalog",
                id = "catalog-2",
                title = "Episode 2",
                url = "bangumi://subject/1/episode/2",
                index = 2,
                raw = mapOf(
                    "subjectId" to "1",
                    "subjectNameCn" to "Test Anime",
                    "subjectName" to "Test JP",
                ),
            ),
        )

        val routes = MediaRouteResolver(listOf(provider), providerTimeoutMs = 1_000L).resolve(request)

        assertTrue(routes.isNotEmpty())
        assertEquals("Fake Source", routes.first().sourceName)
        assertEquals("2", routes.first().stream.metadata["routeEpisodeIndex"])
        assertEquals("1080p", routes.first().quality)
    }

    @Test
    fun resolverPrefersHlsWhenEpisodeMatchIsEquivalent() = runTest {
        val provider = FakeMixedProtocolProvider()
        val request = MediaFetchRequest.fromEpisode(
            Episode(
                providerId = "bangumi-catalog",
                id = "catalog-1",
                title = "Episode 1",
                url = "bangumi://subject/1/episode/1",
                index = 1,
                raw = mapOf(
                    "subjectNameCn" to "Test Anime",
                    "subjectName" to "Test JP",
                ),
            ),
        )

        val routes = MediaRouteResolver(listOf(provider), providerTimeoutMs = 1_000L).resolve(request)

        assertTrue(routes.size >= 2)
        assertEquals(StreamProtocol.HLS, routes.first().protocol)
    }

    @Test
    fun resolverRanksExactChineseTitleAboveContainingVariants() = runTest {
        val provider = FakeChineseTitleProvider()
        val request = MediaFetchRequest.fromEpisode(
            Episode(
                providerId = "bangumi-catalog",
                id = "catalog-1",
                title = "第 1 集",
                url = "bangumi://subject/1/episode/1",
                index = 1,
                raw = mapOf(
                    "subjectNameCn" to "大闹天宫",
                    "subjectName" to "Sun Ukun: Uproar in Heaven",
                    "subjectAliases" to "大闹天宫 1961",
                ),
            ),
        )

        val routes = MediaRouteResolver(listOf(provider), providerTimeoutMs = 1_000L).resolve(request)

        assertTrue(routes.isNotEmpty())
        assertEquals("大闹天宫", routes.first().title)
    }

    private class FakeRouteProvider : SourceProvider {
        override val manifest = SourceManifest(
            id = "fake",
            name = "Fake Source",
            version = "1",
            author = "test",
            capabilities = setOf(SourceCapability.SEARCH, SourceCapability.DETAIL, SourceCapability.STREAM),
        )

        override suspend fun search(query: String): List<SearchResult> {
            return listOf(
                SearchResult(
                    providerId = manifest.id,
                    title = "$query - 01",
                    url = "fake://result/1",
                ),
                SearchResult(
                    providerId = manifest.id,
                    title = "$query - 02 1080p",
                    url = "fake://result/2",
                ),
            )
        }

        override suspend fun loadDetail(result: SearchResult): MediaDetail {
            val index = result.url.substringAfterLast('/').toInt()
            return MediaDetail(
                providerId = manifest.id,
                title = result.title,
                url = result.url,
                episodes = listOf(
                    Episode(
                        providerId = manifest.id,
                        id = "fake-ep-$index",
                        title = result.title,
                        url = "${result.url}/stream",
                        index = index,
                    ),
                ),
            )
        }

        override suspend fun resolveStreams(episode: Episode): List<MediaStream> {
            return listOf(
                MediaStream(
                    id = episode.id,
                    providerId = manifest.id,
                    url = episode.url,
                    protocol = StreamProtocol.PROGRESSIVE,
                    quality = if (episode.index == 2) "1080p" else "720p",
                    sourceScore = 60,
                ),
            )
        }
    }

    private class FakeMixedProtocolProvider : SourceProvider {
        override val manifest = SourceManifest(
            id = "fake-mixed",
            name = "Fake Mixed Source",
            version = "1",
            author = "test",
            capabilities = setOf(SourceCapability.SEARCH, SourceCapability.DETAIL, SourceCapability.STREAM),
        )

        override suspend fun search(query: String): List<SearchResult> {
            return listOf(
                SearchResult(
                    providerId = manifest.id,
                    title = "$query - 01 1080p",
                    url = "fake://mixed/1",
                ),
            )
        }

        override suspend fun loadDetail(result: SearchResult): MediaDetail {
            return MediaDetail(
                providerId = manifest.id,
                title = result.title,
                url = result.url,
                episodes = listOf(
                    Episode(
                        providerId = manifest.id,
                        id = "mixed-ep-1",
                        title = result.title,
                        url = "${result.url}/stream",
                        index = 1,
                    ),
                ),
            )
        }

        override suspend fun resolveStreams(episode: Episode): List<MediaStream> {
            return listOf(
                MediaStream(
                    id = "${episode.id}-mp4",
                    providerId = manifest.id,
                    url = "${episode.url}.mp4",
                    protocol = StreamProtocol.PROGRESSIVE,
                    quality = "1080p",
                    sourceScore = 60,
                ),
                MediaStream(
                    id = "${episode.id}-hls",
                    providerId = manifest.id,
                    url = "${episode.url}.m3u8",
                    protocol = StreamProtocol.HLS,
                    quality = "1080p",
                    sourceScore = 60,
                ),
            )
        }
    }

    private class FakeChineseTitleProvider : SourceProvider {
        override val manifest = SourceManifest(
            id = "fake-cn",
            name = "Fake Chinese Source",
            version = "1",
            author = "test",
            capabilities = setOf(SourceCapability.SEARCH, SourceCapability.DETAIL, SourceCapability.STREAM),
        )

        override suspend fun search(query: String): List<SearchResult> {
            return listOf(
                SearchResult(
                    providerId = manifest.id,
                    title = "哪吒大闹天宫",
                    url = "fake://cn/variant",
                    raw = mapOf("mediaKind" to "online"),
                ),
                SearchResult(
                    providerId = manifest.id,
                    title = "大闹天宫",
                    url = "fake://cn/exact",
                    raw = mapOf("mediaKind" to "online"),
                ),
                SearchResult(
                    providerId = manifest.id,
                    title = "大闹天宫第一季[电影解说]",
                    url = "fake://cn/commentary",
                    raw = mapOf("mediaKind" to "online"),
                ),
            )
        }

        override suspend fun loadDetail(result: SearchResult): MediaDetail {
            return MediaDetail(
                providerId = manifest.id,
                title = result.title,
                url = result.url,
                episodes = listOf(
                    Episode(
                        providerId = manifest.id,
                        id = result.url,
                        title = "第 1 集",
                        url = "${result.url}/stream",
                        index = 1,
                    ),
                ),
            )
        }

        override suspend fun resolveStreams(episode: Episode): List<MediaStream> {
            return listOf(
                MediaStream(
                    id = episode.id,
                    providerId = manifest.id,
                    url = "${episode.url}.m3u8",
                    protocol = StreamProtocol.HLS,
                    quality = "1080p",
                    sourceScore = 60,
                ),
            )
        }
    }
}
