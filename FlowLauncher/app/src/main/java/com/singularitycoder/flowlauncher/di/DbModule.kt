package com.singularitycoder.flowlauncher.di

import android.content.Context
import androidx.room.Room
import com.singularitycoder.flowlauncher.db.ContactDao
import com.singularitycoder.flowlauncher.db.FlowDatabase
import com.singularitycoder.flowlauncher.db.WeatherDao
import com.singularitycoder.flowlauncher.helper.Db
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Singleton
    @Provides
    fun injectFlowRoomDatabase(@ApplicationContext context: Context): FlowDatabase {
        return Room.databaseBuilder(context, FlowDatabase::class.java, Db.FLOW).build()
    }

    @Singleton
    @Provides
    fun injectContactDao(db: FlowDatabase): ContactDao = db.contactDao()

    @Singleton
    @Provides
    fun injectWeatherDao(db: FlowDatabase): WeatherDao = db.weatherDao()
}
