package com.music.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MusicDao {
    @Query("SELECT * FROM search_caches WHERE query = :query AND requestedLimit = :requestedLimit LIMIT 1")
    abstract suspend fun getSearchCache(query: String, requestedLimit: Int): SearchCacheEntity?

    @Query(
        """SELECT songs.* FROM songs
            INNER JOIN search_cache_songs ON songs.id = search_cache_songs.songId
            WHERE search_cache_songs.query = :query
              AND search_cache_songs.requestedLimit = :requestedLimit
            ORDER BY search_cache_songs.position ASC""",
    )
    abstract suspend fun getCachedSearchSongs(query: String, requestedLimit: Int): List<SongEntity>

    @Query("SELECT * FROM songs WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC, id DESC")
    abstract fun observeRecentSongs(): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertSongIfAbsent(song: SongEntity)

    @Query(
        """UPDATE songs SET
            title = :title,
            artistName = :artistName,
            albumId = :albumId,
            albumTitle = :albumTitle,
            artworkUrl = :artworkUrl,
            previewUrl = :previewUrl,
            trackDurationMillis = :trackDurationMillis,
            storeUrl = :storeUrl
            WHERE id = :id""",
    )
    abstract suspend fun updateCatalogSong(
        id: Long,
        title: String,
        artistName: String,
        albumId: Long?,
        albumTitle: String?,
        artworkUrl: String?,
        previewUrl: String?,
        trackDurationMillis: Long?,
        storeUrl: String?,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPlayedSong(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSearchCache(cache: SearchCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertCacheSongs(songs: List<SearchCacheSongEntity>)

    @Query("DELETE FROM search_cache_songs WHERE query = :query AND requestedLimit = :requestedLimit")
    abstract suspend fun deleteCachedSearchSongs(query: String, requestedLimit: Int)

    @Transaction
    open suspend fun upsertCatalogSong(song: SongEntity) {
        insertSongIfAbsent(song)
        updateCatalogSong(
            id = song.id,
            title = song.title,
            artistName = song.artistName,
            albumId = song.albumId,
            albumTitle = song.albumTitle,
            artworkUrl = song.artworkUrl,
            previewUrl = song.previewUrl,
            trackDurationMillis = song.trackDurationMillis,
            storeUrl = song.storeUrl,
        )
    }

    @Transaction
    open suspend fun cacheSearch(
        query: String,
        requestedLimit: Int,
        cachedAt: Long,
        songs: List<SongEntity>,
    ) {
        songs.forEach { upsertCatalogSong(it) }
        deleteCachedSearchSongs(query, requestedLimit)
        insertSearchCache(SearchCacheEntity(query, requestedLimit, cachedAt))
        if (songs.isNotEmpty()) {
            insertCacheSongs(
                songs.mapIndexed { index, song ->
                    SearchCacheSongEntity(query, requestedLimit, song.id, index)
                },
            )
        }
    }

    @Transaction
    open suspend fun markPlayed(song: SongEntity) {
        requireNotNull(song.lastPlayedAt) { "A played song must have a playback timestamp." }
        insertPlayedSong(song)
    }
}
