package com.music.myapplication.ui.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.myapplication.domain.model.Album
import com.music.myapplication.domain.usecase.LoadAlbumUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AlbumUiState {
    data object Loading : AlbumUiState
    data class Content(val album: Album) : AlbumUiState
    data object Missing : AlbumUiState
    data class Error(val message: String) : AlbumUiState
}

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val loadAlbum: LoadAlbumUseCase,
) : ViewModel() {

    private val mutableState = MutableStateFlow<AlbumUiState>(AlbumUiState.Loading)
    val state = mutableState.asStateFlow()

    fun load(albumId: Long?) = viewModelScope.launch {
        if (albumId == null || albumId <= 0) {
            mutableState.value = AlbumUiState.Missing
            return@launch
        }
        mutableState.value = AlbumUiState.Loading
        try {
            mutableState.value = loadAlbum(albumId)?.let(AlbumUiState::Content) ?: AlbumUiState.Missing
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            mutableState.value = AlbumUiState.Error(
                failure.message.orEmpty(),
            )
        }
    }
}
