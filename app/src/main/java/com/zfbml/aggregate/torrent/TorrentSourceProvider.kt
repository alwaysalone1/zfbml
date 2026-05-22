package com.zfbml.aggregate.torrent

import com.zfbml.aggregate.source.DownloadPolicy
import com.zfbml.aggregate.source.Episode
import com.zfbml.aggregate.source.MediaDetail
import com.zfbml.aggregate.source.MediaStream
import com.zfbml.aggregate.source.SearchResult
import com.zfbml.aggregate.source.SourceCapability
import com.zfbml.aggregate.source.SourceManifest
import com.zfbml.aggregate.source.SourceProvider
import com.zfbml.aggregate.source.StreamProtocol

class TorrentSourceProvider : SourceProvider {
    override val manifest: SourceManifest = SourceManifest(
        id = "bt",
        name = "BitTorrent",
        version = "0.1.0",
        author = "zfbml",
        capabilities = setOf(
            SourceCapability.SEARCH,
            SourceCapability.DETAIL,
            SourceCapability.EPISODES,
            SourceCapability.STREAM,
            SourceCapability.DOWNLOAD,
            SourceCapability.BITTORRENT,
        ),
        domains = setOf("magnet", "torrent"),
        supportsDownload = true,
    )

    override suspend fun search(query: String): List<SearchResult> {
        val trimmed = query.trim()
        val magnet = MagnetLink.parse(trimmed)
        if (magnet != null) {
            val title = magnet.displayName ?: magnet.infoHash?.let { "BT $it" } ?: "Magnet link"
            return listOf(
                SearchResult(
                    providerId = manifest.id,
                    title = title,
                    url = magnet.uri,
                    subtitle = "Magnet · ${magnet.trackers.size} trackers",
                    raw = buildMap {
                        magnet.infoHash?.let { put("infoHash", it) }
                        put("trackers", magnet.trackers.joinToString("|"))
                    },
                ),
            )
        }
        if (trimmed.endsWith(".torrent", ignoreCase = true) && trimmed.startsWith("http", ignoreCase = true)) {
            return listOf(
                SearchResult(
                    providerId = manifest.id,
                    title = trimmed.substringAfterLast('/').ifBlank { "Torrent file" },
                    url = trimmed,
                    subtitle = "Torrent URL",
                ),
            )
        }
        return emptyList()
    }

    override suspend fun loadDetail(result: SearchResult): MediaDetail {
        return MediaDetail(
            providerId = manifest.id,
            title = result.title,
            url = result.url,
            summary = "BitTorrent resource. The torrent engine resolves metadata, selects the video file, then serves it to the player through local cache or a local range proxy.",
            episodes = listOf(
                Episode(
                    providerId = manifest.id,
                    id = result.raw["infoHash"] ?: result.url.hashCode().toString(),
                    title = result.title,
                    url = result.url,
                    index = TorrentTitleScorer.extractEpisode(result.title),
                    raw = result.raw,
                ),
            ),
        )
    }

    override suspend fun resolveStreams(episode: Episode): List<MediaStream> {
        val title = episode.title
        val candidate = TorrentCandidate(
            title = title,
            magnetUri = episode.url,
            infoHash = episode.raw["infoHash"],
            quality = TorrentTitleScorer.extractQuality(title),
            episodeIndex = episode.index,
            sourceName = manifest.name,
        )
        return listOf(
            MediaStream(
                id = candidate.infoHash ?: episode.id,
                providerId = manifest.id,
                url = episode.url,
                protocol = StreamProtocol.BITTORRENT,
                quality = candidate.quality ?: "BT",
                codec = "auto",
                downloadPolicy = DownloadPolicy.CacheOnly,
                sourceScore = TorrentTitleScorer.score(title, candidate),
                metadata = candidate.metadata(),
            ),
        )
    }
}
