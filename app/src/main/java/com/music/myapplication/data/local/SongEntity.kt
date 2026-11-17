package com.music.myapplication.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.music.myapplication.domain.model.Song

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artistName: String,
    val albumId: Long?,
    val albumTitle: String?,
    val artworkUrl: String?,
    val previewUrl: String?,
    val trackDurationMillis: Long?,
    val storeUrl: String?,
    val lastPlayedAt: Long? = null,
)

@Entity(tableName = "search_caches", primaryKeys = ["query", "requestedLimit"])
data class SearchCacheEntity(
    val query: String,
    val requestedLimit: Int,
    val cachedAt: Long,
)

@Entity(
    tableName = "search_cache_songs",
    primaryKeys = ["query", "requestedLimit", "songId"],
    indices = [Index(value = ["query", "requestedLimit", "position"])],
)
data class SearchCacheSongEntity(
    val query: String,
    val requestedLimit: Int,
    val songId: Long,
    val position: Int,
)

fun Song.toEntity(lastPlayedAt: Long? = null) = SongEntity(
    id = id,
    title = title,
    artistName = artistName,
    albumId = albumId,
    albumTitle = albumTitle,
    artworkUrl = artworkUrl,
    previewUrl = previewUrl,
    trackDurationMillis = trackDurationMillis,
    storeUrl = storeUrl,
    lastPlayedAt = lastPlayedAt,
)

fun SongEntity.toDomain() = Song(
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
