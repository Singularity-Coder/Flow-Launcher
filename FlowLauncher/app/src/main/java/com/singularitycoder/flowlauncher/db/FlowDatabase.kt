package com.singularitycoder.flowlauncher.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.singularitycoder.flowlauncher.model.*

@Database(
    entities = [
        Contact::class,
        News::class,
        Weather::class,
        Holiday::class,
        TrendingTweet::class,
        GlanceImage::class,
        YoutubeVideo::class,
        Quote::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    RemainderListConverter::class,
    ImagePathListConverter::class,
    YoutubeVideoIdListConverter::class
)
abstract class FlowDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun newsDao(): NewsDao
    abstract fun weatherDao(): WeatherDao
    abstract fun holidayDao(): HolidayDao
    abstract fun trendingTweetsDao(): TrendingTweetDao
    abstract fun flowImageDao(): GlanceImageDao
    abstract fun quoteDao(): QuoteDao
    abstract fun youtubeVideoDao(): YoutubeVideoDao
}

