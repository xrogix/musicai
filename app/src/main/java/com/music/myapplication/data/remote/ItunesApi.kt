package com.music.myapplication.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {
    @GET("search")
    suspend fun searchSongs(
        @Query("term") term: String,
        @Query("country") country: String = "br",
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int,
    ): ItunesResponseSchema

    @GET("lookup")
    suspend fun lookupAlbum(
        @Query("id") collectionId: Long,
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 200,
    ): ItunesResponseSchema
}
