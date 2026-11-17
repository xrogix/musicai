package com.music.myapplication.data.mapper

import com.music.myapplication.data.remote.ItunesResultSchema
import com.music.myapplication.domain.model.Album
import com.music.myapplication.domain.model.Song

fun ItunesResultSchema.toSongOrNull(): Song? {
    val id = trackId ?: return null
    val title = trackName?.takeIf(String::isNotBlank) ?: return null
    val artist = artistName?.takeIf(String::isNotBlank) ?: return null

    return Song(
        id = id,
        title = title,
        artistName = artist,
        albumId = collectionId,
        albumTitle = collectionName,
        artworkUrl = artworkUrl,
        previewUrl = previewUrl,
        trackDurationMillis = trackDurationMillis,
        storeUrl = trackViewUrl ?: collectionViewUrl,
    )
}

fun List<ItunesResultSchema>.toSongs(): List<Song> =
    mapNotNull(ItunesResultSchema::toSongOrNull).distinctBy(Song::id)

fun List<ItunesResultSchema>.toAlbumOrNull(requestedId: Long): Album? {
    val collection = firstOrNull { it.collectionId == requestedId && it.collectionName != null }
    val tracks = toSongs().filter { it.albumId == requestedId }
    val firstTrack = tracks.firstOrNull()
    val title = collection?.collectionName ?: firstTrack?.albumTitle ?: return null
    val artist = collection?.artistName ?: firstTrack?.artistName ?: return null

    return Album(
        id = requestedId,
        title = title,
        artistName = artist,
        artworkUrl = collection?.artworkUrl ?: firstTrack?.artworkUrl,
        releaseDate = collection?.releaseDate,
        trackCount = collection?.trackCount ?: tracks.size.takeIf { it > 0 },
        storeUrl = collection?.collectionViewUrl ?: firstTrack?.storeUrl,
        tracks = tracks,
    )
}
