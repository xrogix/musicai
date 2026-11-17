package com.music.myapplication.domain.usecase

import com.music.myapplication.domain.model.Album
import com.music.myapplication.domain.repository.MusicRepository

class LoadAlbumUseCase(
    private val musicRepository: MusicRepository,
) {
    suspend operator fun invoke(albumId: Long): Album? {
        require(albumId > 0) { "Album ID must be positive." }
        return musicRepository.getAlbum(albumId)
    }
}
