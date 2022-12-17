package com.singularitycoder.flowlauncher.helper.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.singularitycoder.flowlauncher.addEditAppFlow.dao.AppFlowDao
import com.singularitycoder.flowlauncher.addEditAppFlow.model.AppFlow
import com.singularitycoder.flowlauncher.glance.dao.GlanceImageDao
import com.singularitycoder.flowlauncher.glance.dao.HolidayDao
import com.singularitycoder.flowlauncher.glance.dao.YoutubeVideoDao
import com.singularitycoder.flowlauncher.glance.model.GlanceImage
import com.singularitycoder.flowlauncher.glance.model.Holiday
import com.singularitycoder.flowlauncher.glance.model.YoutubeVideo
import com.singularitycoder.flowlauncher.home.dao.AppDao
import com.singularitycoder.flowlauncher.home.dao.ContactDao
import com.singularitycoder.flowlauncher.home.model.App
import com.singularitycoder.flowlauncher.home.model.Contact
import com.singularitycoder.flowlauncher.today.dao.NewsDao
import com.singularitycoder.flowlauncher.today.dao.QuoteDao
import com.singularitycoder.flowlauncher.today.dao.TrendingTweetDao
import com.singularitycoder.flowlauncher.today.dao.WeatherDao
import com.singularitycoder.flowlauncher.today.model.News
import com.singularitycoder.flowlauncher.today.model.Quote
import com.singularitycoder.flowlauncher.today.model.TrendingTweet
import com.singularitycoder.flowlauncher.today.model.Weather

@Database(
    entities = [
        App::class,
        AppFlow::class,
        Contact::class,
        News::class,
        Weather::class,
        Holiday::class,
        TrendingTweet::class,
        GlanceImage::class,
        YoutubeVideo::class,
        Quote::class,
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(
    AppListConverter::class,
    RemainderListConverter::class,
    StringListConverter::class,
    IntListConverter::class
)
abstract class FlowDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun appFlowDao(): AppFlowDao
    abstract fun contactDao(): ContactDao
    abstract fun newsDao(): NewsDao
    abstract fun weatherDao(): WeatherDao
    abstract fun holidayDao(): HolidayDao
    abstract fun trendingTweetsDao(): TrendingTweetDao
    abstract fun flowImageDao(): GlanceImageDao
    abstract fun quoteDao(): QuoteDao
    abstract fun youtubeVideoDao(): YoutubeVideoDao
}

