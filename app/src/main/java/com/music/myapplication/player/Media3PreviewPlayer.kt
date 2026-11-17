package com.music.myapplication.player

import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class Media3PreviewPlayer @Inject constructor(
    private val player: ExoPlayer,
) : PreviewPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutableState = MutableStateFlow(PreviewPlaybackState())
    private var progressJob: Job? = null
    private var lastError: String? = null
    private var released = false

    override val state: StateFlow<PreviewPlaybackState> = mutableState.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            publishState()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) startPositionUpdates() else stopPositionUpdates()
            publishState()
        }

        override fun onPlayerError(error: PlaybackException) {
            lastError = error.message ?: "Preview playback failed."
            publishState()
        }
    }

    init {
        player.addListener(listener)
    }

    override fun loadPreview(url: String) {
        checkNotReleased()
        require(url.isNotBlank()) { "Preview URL must not be blank." }
        lastError = null
        stopPositionUpdates()
        player.stop()
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        publishState()
    }

    override fun play() {
        checkNotReleased()
        if (player.currentMediaItem != null) player.play()
    }

    override fun pause() {
        if (!released) player.pause()
    }

    override fun seekTo(positionMillis: Long) {
        checkNotReleased()
        val position = positionMillis.coerceAtLeast(0)
        val duration = player.duration
        player.seekTo(if (duration != C.TIME_UNSET && duration > 0) position.coerceAtMost(duration) else position)
    }

    override fun setRepeating(enabled: Boolean) {
        checkNotReleased()
        player.repeatMode = if (enabled) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        publishState()
    }

    override fun release() {
        if (released) return
        released = true
        stopPositionUpdates()
        player.removeListener(listener)
        player.release()
        scope.cancel()
        mutableState.value = PreviewPlaybackState(isReleased = true)
    }

    private fun startPositionUpdates() {
        if (progressJob?.isActive == true) return
        progressJob = scope.launch {
            while (isActive && player.isPlaying) {
                publishState()
                delay(PROGRESS_INTERVAL_MILLIS)
            }
            publishState()
            progressJob = null
        }
    }

    private fun stopPositionUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun publishState() {
        if (released) return
        val duration = player.duration
        mutableState.value = PreviewPlaybackState(
            isReady = player.playbackState == Player.STATE_READY,
            isBuffering = player.playbackState == Player.STATE_BUFFERING,
            isPlaying = player.isPlaying,
            isRepeating = player.repeatMode == Player.REPEAT_MODE_ONE,
            isEnded = player.playbackState == Player.STATE_ENDED,
            positionMillis = player.currentPosition.coerceAtLeast(0),
            durationMillis = duration.takeUnless { it == C.TIME_UNSET || it < 0 } ?: 0,
            errorMessage = lastError,
        )
    }

    private fun checkNotReleased() {
        check(!released) { "Preview player has been released." }
    }

    private companion object {
        const val PROGRESS_INTERVAL_MILLIS = 250L
    }
}
