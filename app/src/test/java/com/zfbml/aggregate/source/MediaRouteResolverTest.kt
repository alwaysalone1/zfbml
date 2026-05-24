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
        assertEquals(listOf("Test CN", "Test JP", "Alias One", "Alias Two"), request.subjectNames)
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
}
