package com.music.myapplication.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.myapplication.domain.model.Song
import com.music.myapplication.domain.model.SongSearchState
import com.music.myapplication.domain.usecase.ObserveRecentSongsUseCase
import com.music.myapplication.domain.usecase.SearchSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val query: String = "",
    val search: SongSearchState = SongSearchState.Idle,
    val recentSongs: List<Song> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchSongs: SearchSongsUseCase,
    observeRecentSongs: ObserveRecentSongsUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(HomeUiState())
    val state = mutableState.asStateFlow()
    private var searchJob: Job? = null
    private var searchSequence = 0L

    init {
        viewModelScope.launch {
            observeRecentSongs().collect { songs ->
                mutableState.update { it.copy(recentSongs = songs.distinctBy(Song::id)) }
            }
        }
    }

    fun updateQuery(query: String) {
        mutableState.update { it.copy(query = query) }
    }

    fun search(query: String = state.value.query) {
        val normalized = query.trim()
        searchJob?.cancel()
        val requestId = ++searchSequence
        mutableState.update {
            it.copy(
                query = query,
                search = if (normalized.isBlank()) SongSearchState.Idle else SongSearchState.Loading(normalized),
            )
        }
        if (normalized.isBlank()) return

        searchJob = viewModelScope.launch {
            try {
                val result = searchSongs(normalized)
                if (requestId == searchSequence) {
                    val uniqueResult = result.copy(songs = result.songs.distinctBy(Song::id))
                    mutableState.update { it.copy(search = SongSearchState.Results(uniqueResult)) }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                if (requestId == searchSequence) {
                    mutableState.update {
                        it.copy(
                            search = SongSearchState.Error(
                                query = normalized,
                                message = failure.message.orEmpty(),
                            ),
                        )
                    }
                }
            }
        }
    }

    fun loadMore() {
        mutableState.update { current ->
            val result = current.search as? SongSearchState.Results ?: return@update current
            if (!result.canLoadMore) current else current.copy(search = result.loadNextPage())
        }
    }
}
