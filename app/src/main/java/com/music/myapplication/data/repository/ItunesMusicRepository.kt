package com.music.myapplication.data.repository

import com.music.myapplication.data.local.MusicDao
import com.music.myapplication.data.local.toDomain
import com.music.myapplication.data.local.toEntity
import com.music.myapplication.data.mapper.toAlbumOrNull
import com.music.myapplication.data.mapper.toSongs
import com.music.myapplication.data.remote.ItunesApi
import com.music.myapplication.domain.model.Album
import com.music.myapplication.domain.model.MAX_SONG_SEARCH_RESULTS
import com.music.myapplication.domain.model.Song
import com.music.myapplication.domain.model.SongSearchResult
import com.music.myapplication.domain.model.SongSearchSource
import com.music.myapplication.domain.repository.MusicRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject

class ItunesMusicRepository @Inject constructor(
    private val api: ItunesApi,
    private val musicDao: MusicDao,
) : MusicRepository {
    override suspend fun searchSongs(query: String, limit: Int): SongSearchResult {
        val normalizedQuery = query.trim()
        require(normalizedQuery.isNotEmpty()) { "Search query must not be blank." }
        val safeLimit = limit.coerceIn(1, MAX_SONG_SEARCH_RESULTS)
        val cacheKey = normalizedQuery.lowercase(Locale.ROOT)

        val response = try {
            api.searchSongs(term = normalizedQuery, limit = safeLimit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (remoteFailure: Exception) {
            musicDao.getSearchCache(cacheKey, safeLimit) ?: throw remoteFailure
            val songs = musicDao.getCachedSearchSongs(cacheKey, safeLimit).map { it.toDomain() }
            return SongSearchResult(normalizedQuery, songs, SongSearchSource.CACHE)
        }

        val songs = response.results.toSongs().take(safeLimit)
        musicDao.cacheSearch(
            query = cacheKey,
            requestedLimit = safeLimit,
            cachedAt = System.currentTimeMillis(),
            songs = songs.map { it.toEntity() },
        )
        return SongSearchResult(normalizedQuery, songs, SongSearchSource.REMOTE)
    }

    override fun observeRecentSongs(): Flow<List<Song>> =
        musicDao.observeRecentSongs().map { songs -> songs.map { it.toDomain() } }

    override suspend fun markSongAsPlayed(song: Song) {
        musicDao.markPlayed(song.toEntity(lastPlayedAt = System.currentTimeMillis()))
    }

    override suspend fun getAlbum(albumId: Long): Album? {
        require(albumId > 0) { "Album ID must be positive." }
        return api.lookupAlbum(collectionId = albumId)
            .results
            .toAlbumOrNull(albumId)
    }
}
