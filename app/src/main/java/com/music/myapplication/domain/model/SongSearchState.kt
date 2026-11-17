package com.music.myapplication.domain.model

sealed interface SongSearchState {
    data object Idle : SongSearchState

    data class Loading(val query: String) : SongSearchState

    data class Results(
        val result: SongSearchResult,
        val visibleCount: Int = minOf(SONG_SEARCH_PAGE_SIZE, result.songs.size),
    ) : SongSearchState {
        init {
            require(visibleCount in 0..result.songs.size) {
                "Visible count must be within the search result size."
            }
        }

        val visibleSongs: List<Song>
            get() = result.songs.take(visibleCount)

        val canLoadMore: Boolean
            get() = visibleCount < result.songs.size

        fun loadNextPage(pageSize: Int = SONG_SEARCH_PAGE_SIZE): Results {
            require(pageSize > 0) { "Page size must be positive." }
            return copy(visibleCount = (visibleCount + pageSize).coerceAtMost(result.songs.size))
        }
    }

    data class Error(
        val query: String,
        val message: String,
    ) : SongSearchState
}
