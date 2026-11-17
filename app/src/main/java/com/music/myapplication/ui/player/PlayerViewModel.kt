package com.music.myapplication.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.myapplication.domain.model.Song
import com.music.myapplication.domain.usecase.MarkSongAsPlayedUseCase
import com.music.myapplication.player.PreviewPlaybackState
import com.music.myapplication.player.PreviewPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val player: PreviewPlayer,
    private val markSongAsPlayed: MarkSongAsPlayedUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(PreviewPlaybackState())
    val state = mutableState.asStateFlow()
    private var loadedSongId: Long? = null

    init {
        viewModelScope.launch { player.state.collect { mutableState.value = it } }
    }

    fun open(song: Song) {
        if (loadedSongId == song.id) return
        loadedSongId = song.id
        val preview = song.previewUrl
        if (preview.isNullOrBlank()) {
            mutableState.value = PreviewPlaybackState()
            return
        }
        player.loadPreview(preview)
        player.play()
        viewModelScope.launch { markSongAsPlayed(song) }
    }

    fun togglePlayback(song: Song) {
        if (song.previewUrl.isNullOrBlank()) return
        if (state.value.isPlaying) {
            player.pause()
        } else {
            if (loadedSongId != song.id) open(song) else {
                player.play()
                viewModelScope.launch { markSongAsPlayed(song) }
            }
        }
    }

    fun pause() = player.pause()

    fun seekTo(positionMillis: Long) = player.seekTo(positionMillis)

    fun toggleRepeating() = player.setRepeating(!state.value.isRepeating)

    override fun onCleared() {
        player.release()
        super.onCleared()
    }
}
