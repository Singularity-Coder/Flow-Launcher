package com.singularitycoder.flowlauncher.helper.di

import android.content.Context
import androidx.room.Room
import com.singularitycoder.flowlauncher.addEditAppFlow.dao.AppFlowDao
import com.singularitycoder.flowlauncher.deviceActivity.dao.DeviceActivityDao
import com.singularitycoder.flowlauncher.glance.dao.GlanceImageDao
import com.singularitycoder.flowlauncher.glance.dao.HolidayDao
import com.singularitycoder.flowlauncher.glance.dao.YoutubeVideoDao
import com.singularitycoder.flowlauncher.helper.constants.Db
import com.singularitycoder.flowlauncher.helper.db.*
import com.singularitycoder.flowlauncher.home.dao.AppDao
import com.singularitycoder.flowlauncher.home.dao.ContactDao
import com.singularitycoder.flowlauncher.today.dao.NewsDao
import com.singularitycoder.flowlauncher.today.dao.QuoteDao
import com.singularitycoder.flowlauncher.today.dao.TrendingTweetDao
import com.singularitycoder.flowlauncher.today.dao.WeatherDao
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
        return Room.databaseBuilder(context, FlowDatabase::class.java, Db.FLOW)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun injectAppDao(db: FlowDatabase): AppDao = db.appDao()

    @Singleton
    @Provides
    fun injectAppFlowDao(db: FlowDatabase): AppFlowDao = db.appFlowDao()

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
    fun injectFlowImageDao(db: FlowDatabase): GlanceImageDao = db.flowImageDao()

    @Singleton
    @Provides
    fun injectDeviceActivityDao(db: FlowDatabase): DeviceActivityDao = db.deviceActivityDao()
}
