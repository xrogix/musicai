package com.music.myapplication.domain.model

data class Album(
    val id: Long,
    val title: String,
    val artistName: String,
    val artworkUrl: String? = null,
    val releaseDate: String? = null,
    val trackCount: Int? = null,
    val storeUrl: String? = null,
    val tracks: List<Song> = emptyList(),
)
