package com.music.myapplication.ui.navigation

import androidx.lifecycle.ViewModel
import com.music.myapplication.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class MusicSessionState(
    val queue: List<Song> = emptyList(),
    val selectedSong: Song? = null,
    val selectedAlbumId: Long? = null,
)

@HiltViewModel
class MusicSessionViewModel @Inject constructor() : ViewModel() {
    private val mutableState = MutableStateFlow(MusicSessionState())
    val state = mutableState.asStateFlow()

    fun selectSong(song: Song, songs: List<Song>) {
        val queue = (songs + song).distinctBy(Song::id)
        mutableState.update {
            it.copy(
                queue = queue,
                selectedSong = song,
                selectedAlbumId = song.albumId,
            )
        }
    }

    fun selectAlbum(albumId: Long?) {
        mutableState.update { it.copy(selectedAlbumId = albumId) }
    }

    fun move(delta: Int): Song? {
        var selected: Song? = null
        mutableState.update { current ->
            if (current.queue.isEmpty()) return@update current
            val currentIndex = current.queue.indexOfFirst { it.id == current.selectedSong?.id }
            val safeIndex = currentIndex.coerceAtLeast(0)
            val nextIndex = (safeIndex + delta).coerceIn(0, current.queue.lastIndex)
            val nextSong = current.queue[nextIndex]
            selected = nextSong
            current.copy(selectedSong = nextSong, selectedAlbumId = nextSong.albumId)
        }
        return selected
    }
}
