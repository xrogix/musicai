package com.music.myapplication.domain.usecase

import com.music.myapplication.domain.model.Song
import com.music.myapplication.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow

class ObserveRecentSongsUseCase(
    private val musicRepository: MusicRepository,
) {
    operator fun invoke(): Flow<List<Song>> = musicRepository.observeRecentSongs()
}
