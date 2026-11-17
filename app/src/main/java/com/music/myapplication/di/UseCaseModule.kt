package com.music.myapplication.di

import com.music.myapplication.domain.repository.MusicRepository
import com.music.myapplication.domain.usecase.LoadAlbumUseCase
import com.music.myapplication.domain.usecase.MarkSongAsPlayedUseCase
import com.music.myapplication.domain.usecase.ObserveRecentSongsUseCase
import com.music.myapplication.domain.usecase.SearchSongsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    fun provideSearchSongsUseCase(repository: MusicRepository) = SearchSongsUseCase(repository)

    @Provides
    fun provideObserveRecentSongsUseCase(repository: MusicRepository) = ObserveRecentSongsUseCase(repository)

    @Provides
    fun provideMarkSongAsPlayedUseCase(repository: MusicRepository) = MarkSongAsPlayedUseCase(repository)

    @Provides
    fun provideLoadAlbumUseCase(repository: MusicRepository) = LoadAlbumUseCase(repository)
}
