package com.music.myapplication.domain.usecase

import com.music.myapplication.domain.model.Album
import com.music.myapplication.domain.model.MAX_SONG_SEARCH_RESULTS
import com.music.myapplication.domain.model.Song
import com.music.myapplication.domain.model.SongSearchResult
import com.music.myapplication.domain.model.SongSearchSource
import com.music.myapplication.domain.repository.MusicRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class MusicUseCasesTest {
    private val repository = mockk<MusicRepository>()

    @Test
    fun `search trims query uses maximum limit and returns repository result`() = runTest {
        val expected = SongSearchResult("jazz", listOf(song), SongSearchSource.REMOTE)
        coEvery { repository.searchSongs("jazz", MAX_SONG_SEARCH_RESULTS) } returns expected

        assertEquals(expected, SearchSongsUseCase(repository)("  jazz  "))
    }

    @Test
    fun `search rejects blank query without calling repository`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            runTest { SearchSongsUseCase(repository)("  ") }
        }

        assertEquals("Search query must not be blank.", exception.message)
    }

    @Test
    fun `observe recent songs returns repository flow`() {
        val recentSongs = flowOf(listOf(song))
        every { repository.observeRecentSongs() } returns recentSongs

        assertSame(recentSongs, ObserveRecentSongsUseCase(repository)())
    }

    @Test
    fun `load album returns repository result`() = runTest {
        val album = Album(id = 17, title = "Blue", artistName = "Miles Davis", tracks = listOf(song))
        coEvery { repository.getAlbum(17) } returns album

        assertEquals(album, LoadAlbumUseCase(repository)(17))
    }

    @Test
    fun `load album rejects non positive ID without calling repository`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            runTest { LoadAlbumUseCase(repository)(0) }
        }

        assertEquals("Album ID must be positive.", exception.message)
    }

    private companion object {
        val song = Song(
            id = 42,
            title = "So What",
            artistName = "Miles Davis",
            albumId = 17,
            albumTitle = "Kind of Blue",
        )
    }
}
