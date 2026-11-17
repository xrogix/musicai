package com.music.myapplication.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.music.myapplication.data.local.MusicDatabase
import com.music.myapplication.data.local.toEntity
import com.music.myapplication.data.remote.ItunesApi
import com.music.myapplication.data.remote.ItunesResponseSchema
import com.music.myapplication.data.remote.ItunesResultSchema
import com.music.myapplication.data.repository.ItunesMusicRepository
import com.music.myapplication.domain.model.Song
import com.music.myapplication.domain.model.SongSearchSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MusicDataIntegrationTest {
    private lateinit var database: MusicDatabase

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MusicDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun `search is served from Room when Apple request fails`() = runBlocking {
        val api = FakeItunesApi().apply {
            searchResponse = ItunesResponseSchema(
                results = listOf(
                    ItunesResultSchema(
                        trackId = 42,
                        trackName = "So What",
                        artistName = "Miles Davis",
                        collectionId = 17,
                        collectionName = "Kind of Blue",
                    ),
                ),
            )
        }
        val repository = ItunesMusicRepository(api, database.musicDao())

        val firstSearch = repository.searchSongs(" jazz ", 200)
        assertEquals("jazz", api.lastSearchTerm)
        api.searchFailure = IOException("offline")
        val cachedSearch = repository.searchSongs("JAZZ", 200)

        assertEquals(SongSearchSource.REMOTE, firstSearch.source)
        assertEquals(SongSearchSource.CACHE, cachedSearch.source)
        assertEquals(firstSearch.songs, cachedSearch.songs)
    }

    @Test
    fun `recent songs are ordered by last played time`() = runBlocking {
        val dao = database.musicDao()
        val earlier = song(10, "Earlier")
        val later = song(11, "Later")

        dao.markPlayed(earlier.toEntity(lastPlayedAt = 100L))
        dao.markPlayed(later.toEntity(lastPlayedAt = 200L))

        assertEquals(listOf(later, earlier), dao.observeRecentSongs().first().map { it.toSongForTest() })
    }

    private fun song(id: Long, title: String) = Song(id = id, title = title, artistName = "Artist")

    private fun com.music.myapplication.data.local.SongEntity.toSongForTest() = Song(
        id = id,
        title = title,
        artistName = artistName,
        albumId = albumId,
        albumTitle = albumTitle,
        artworkUrl = artworkUrl,
        previewUrl = previewUrl,
        trackDurationMillis = trackDurationMillis,
        storeUrl = storeUrl,
    )

    private class FakeItunesApi : ItunesApi {
        var searchResponse = ItunesResponseSchema()
        var searchFailure: Exception? = null
        var lastSearchTerm: String? = null

        override suspend fun searchSongs(
            term: String,
            country: String,
            media: String,
            entity: String,
            limit: Int,
        ): ItunesResponseSchema {
            lastSearchTerm = term
            searchFailure?.let { throw it }
            return searchResponse
        }

        override suspend fun lookupAlbum(
            collectionId: Long,
            entity: String,
            limit: Int,
        ): ItunesResponseSchema = ItunesResponseSchema()
    }
}
