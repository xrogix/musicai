package com.music.myapplication.domain.model

data class Song(
    val id: Long,
    val title: String,
    val artistName: String,
    val albumId: Long? = null,
    val albumTitle: String? = null,
    val artworkUrl: String? = null,
    val previewUrl: String? = null,
    val trackDurationMillis: Long? = null,
    val storeUrl: String? = null,
)
