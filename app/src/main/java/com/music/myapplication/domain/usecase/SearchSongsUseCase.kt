package com.music.myapplication.domain.usecase

import com.music.myapplication.domain.model.MAX_SONG_SEARCH_RESULTS
import com.music.myapplication.domain.model.SongSearchResult
import com.music.myapplication.domain.repository.MusicRepository

class SearchSongsUseCase(
    private val musicRepository: MusicRepository,
) {
    suspend operator fun invoke(query: String): SongSearchResult {
        val normalizedQuery = query.trim()
        require(normalizedQuery.isNotEmpty()) { "Search query must not be blank." }

        return musicRepository.searchSongs(
            query = normalizedQuery,
            limit = MAX_SONG_SEARCH_RESULTS,
        )
    }
}
