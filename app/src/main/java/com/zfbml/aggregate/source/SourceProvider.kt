package com.zfbml.aggregate.source

interface SourceProvider {
    val manifest: SourceManifest

    suspend fun search(query: String): List<SearchResult>

    suspend fun loadDetail(result: SearchResult): MediaDetail

    suspend fun listEpisodes(detail: MediaDetail): List<Episode> = detail.episodes

    suspend fun resolveStreams(episode: Episode): List<MediaStream>
}
