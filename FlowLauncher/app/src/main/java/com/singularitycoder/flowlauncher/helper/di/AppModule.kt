package com.singularitycoder.flowlauncher.helper.di

import android.bluetooth.BluetoothManager
import android.content.Context
import android.media.AudioManager
import android.net.wifi.WifiManager
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
    ): AudioManager = appContext.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    @Singleton
    @Provides
    fun injectWifiManager(
        @ApplicationContext appContext: Context
    ): WifiManager = appContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    @Singleton
    @Provides
    fun injectBluetoothManager(
        @ApplicationContext appContext: Context
    ): BluetoothManager = appContext.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
}
