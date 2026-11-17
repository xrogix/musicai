package com.music.myapplication.domain.usecase

import com.music.myapplication.domain.model.Song
import com.music.myapplication.domain.repository.MusicRepository

class MarkSongAsPlayedUseCase(
    private val musicRepository: MusicRepository,
) {
    suspend operator fun invoke(song: Song) {
        musicRepository.markSongAsPlayed(song)
    }
}
