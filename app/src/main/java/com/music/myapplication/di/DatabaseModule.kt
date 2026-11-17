package com.music.myapplication.di

import android.content.Context
import androidx.room.Room
import com.music.myapplication.data.local.MusicDao
import com.music.myapplication.data.local.MusicDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideMusicDatabase(@ApplicationContext context: Context): MusicDatabase =
        Room.databaseBuilder(context, MusicDatabase::class.java, DATABASE_NAME).build()

    @Provides
    fun provideMusicDao(database: MusicDatabase): MusicDao = database.musicDao()

    private const val DATABASE_NAME = "music-cache.db"
}
