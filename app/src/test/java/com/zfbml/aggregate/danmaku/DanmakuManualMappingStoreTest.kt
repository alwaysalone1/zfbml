package com.zfbml.aggregate.danmaku

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DanmakuManualMappingStoreTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun persistsManualMappingsAcrossStoreInstances() = runTest {
        val file = temporaryFolder.newFile("danmaku_manual_mappings.json")
        val mapping = manualMapping(token = "bilibili-episode-1")

        DanmakuManualMappingStore(file).save(listOf(mapping))
        val restored = DanmakuManualMappingStore(file).load()

        assertEquals(listOf(mapping), restored)
    }

    @Test
    fun addOrReplaceKeepsLatestMappingForSameEpisode() = runTest {
        val file = temporaryFolder.newFile("danmaku_manual_mappings.json")
        val store = DanmakuManualMappingStore(file)

        store.addOrReplace(manualMapping(token = "old-token"))
        val updated = store.addOrReplace(manualMapping(token = "new-token"))

        assertEquals(1, updated.size)
        assertEquals("new-token", updated.single().match.token)
        assertEquals(updated, DanmakuManualMappingStore(file).load())
    }

    @Test
    fun loadReturnsEmptyListForMissingOrCorruptFile() = runTest {
        val missing = DanmakuManualMappingStore(temporaryFolder.newFile("missing.json").also { it.delete() })
        val corruptFile = temporaryFolder.newFile("corrupt.json")
        corruptFile.writeText("{", Charsets.UTF_8)

        assertEquals(emptyList<DanmakuManualMapping>(), missing.load())
        assertEquals(emptyList<DanmakuManualMapping>(), DanmakuManualMappingStore(corruptFile).load())
    }

    private fun manualMapping(token: String): DanmakuManualMapping {
        return DanmakuManualMapping(
            detailTitle = "Test Anime",
            detailProviderId = "bangumi-catalog",
            detailUrl = "bangumi://subject/1",
            episodeId = "ep-1",
            episodeIndex = 1,
            match = DanmakuMatch(
                providerId = "danmaku-bilibili",
                platform = DanmakuPlatform.Bilibili,
                title = "Test Anime",
                episodeTitle = "Episode 1",
                score = 90,
                token = token,
            ),
        )
    }
}
