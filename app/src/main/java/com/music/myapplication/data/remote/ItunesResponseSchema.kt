package com.music.myapplication.data.remote

import com.google.gson.annotations.SerializedName

data class ItunesResponseSchema(
    @SerializedName("resultCount") val resultCount: Int? = null,
    @SerializedName("results") val results: List<ItunesResultSchema> = emptyList(),
)

data class ItunesResultSchema(
    @SerializedName("wrapperType") val wrapperType: String? = null,
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("trackId") val trackId: Long? = null,
    @SerializedName("trackName") val trackName: String? = null,
    @SerializedName("artistName") val artistName: String? = null,
    @SerializedName("collectionId") val collectionId: Long? = null,
    @SerializedName("collectionName") val collectionName: String? = null,
    @SerializedName("artworkUrl100") val artworkUrl: String? = null,
    @SerializedName("previewUrl") val previewUrl: String? = null,
    @SerializedName("trackTimeMillis") val trackDurationMillis: Long? = null,
    @SerializedName("trackViewUrl") val trackViewUrl: String? = null,
    @SerializedName("collectionViewUrl") val collectionViewUrl: String? = null,
    @SerializedName("releaseDate") val releaseDate: String? = null,
    @SerializedName("trackCount") val trackCount: Int? = null,
)
