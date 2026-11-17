package com.music.myapplication.domain.model

const val MAX_SONG_SEARCH_RESULTS = 200

const val SONG_SEARCH_PAGE_SIZE = 20

enum class SongSearchSource {
    REMOTE,
    CACHE,
}

data class SongSearchResult(
    val query: String,
    val songs: List<Song>,
    val source: SongSearchSource,
)
