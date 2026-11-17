package com.music.myapplication.player

import kotlinx.coroutines.flow.StateFlow

interface PreviewPlayer {
    val state: StateFlow<PreviewPlaybackState>

    fun loadPreview(url: String)
    fun play()
    fun pause()
    fun seekTo(positionMillis: Long)
    fun setRepeating(enabled: Boolean)
    fun release()
}

data class PreviewPlaybackState(
    val isReady: Boolean = false,
    val isBuffering: Boolean = false,
    val isPlaying: Boolean = false,
    val isRepeating: Boolean = false,
    val isEnded: Boolean = false,
    val positionMillis: Long = 0,
    val durationMillis: Long = 0,
    val errorMessage: String? = null,
    val isReleased: Boolean = false,
)
