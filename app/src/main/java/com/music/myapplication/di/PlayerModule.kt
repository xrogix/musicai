package com.music.myapplication.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.Provides
import dagger.Binds
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.android.components.ViewModelComponent
import com.music.myapplication.player.Media3PreviewPlayer
import com.music.myapplication.player.PreviewPlayer

@Module
@InstallIn(ViewModelComponent::class)
abstract class PlayerModule {
    @Binds
    @ViewModelScoped
    abstract fun bindPreviewPlayer(implementation: Media3PreviewPlayer): PreviewPlayer
}

@Module
@InstallIn(ViewModelComponent::class)
object PlayerDependenciesModule {
    @Provides
    @ViewModelScoped
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer =
        ExoPlayer.Builder(context).build()
}
