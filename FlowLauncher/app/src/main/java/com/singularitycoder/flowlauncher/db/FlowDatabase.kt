package com.singularitycoder.flowlauncher.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.singularitycoder.flowlauncher.model.Contact
import com.singularitycoder.flowlauncher.model.News
import com.singularitycoder.flowlauncher.model.Weather

@Database(
    entities = [
        Contact::class,
        News::class,
        Weather::class,
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
}

