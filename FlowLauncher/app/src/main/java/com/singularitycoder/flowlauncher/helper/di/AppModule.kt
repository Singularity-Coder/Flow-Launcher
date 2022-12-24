package com.singularitycoder.flowlauncher.helper.di

import android.content.Context
import android.media.AudioManager
import com.singularitycoder.flowlauncher.helper.db.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun injectAudioManager(
        @ApplicationContext appContext: Context
    ): AudioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
}
