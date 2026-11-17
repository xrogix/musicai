package com.music.myapplication.domain.repository

import com.music.myapplication.domain.model.Album
import com.music.myapplication.domain.model.MAX_SONG_SEARCH_RESULTS
import com.music.myapplication.domain.model.Song
import com.music.myapplication.domain.model.SongSearchResult
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun searchSongs(
        query: String,
        limit: Int = MAX_SONG_SEARCH_RESULTS,
    ): SongSearchResult

    fun observeRecentSongs(): Flow<List<Song>>

    suspend fun markSongAsPlayed(song: Song)

    suspend fun getAlbum(albumId: Long): Album?
}
