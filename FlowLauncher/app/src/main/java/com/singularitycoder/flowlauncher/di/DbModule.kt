package com.singularitycoder.flowlauncher.di

import android.content.Context
import androidx.room.Room
import com.singularitycoder.flowlauncher.db.*
import com.singularitycoder.flowlauncher.helper.constants.Db
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

    @Singleton
    @Provides
    fun injectNewsDao(db: FlowDatabase): NewsDao = db.newsDao()

    @Singleton
    @Provides
    fun injectHolidayDao(db: FlowDatabase): HolidayDao = db.holidayDao()

    @Singleton
    @Provides
    fun injectTrendingTweetsDao(db: FlowDatabase): TrendingTweetDao = db.trendingTweetsDao()

    @Singleton
    @Provides
    fun injectQuoteDao(db: FlowDatabase): QuoteDao = db.quoteDao()

    @Singleton
    @Provides
    fun injectYoutubeVideoDao(db: FlowDatabase): YoutubeVideoDao = db.youtubeVideoDao()

    @Singleton
    @Provides
    fun injectFlowImageDao(db: FlowDatabase): FlowImageDao = db.flowImageDao()
}