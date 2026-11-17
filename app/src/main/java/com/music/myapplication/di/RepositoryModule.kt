package com.music.myapplication.di

import com.music.myapplication.data.repository.ItunesMusicRepository
import com.music.myapplication.domain.repository.MusicRepository
import dagger.Module
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMusicRepository(implementation: ItunesMusicRepository): MusicRepository
}
