package com.singularitycoder.flowlauncher.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.singularitycoder.flowlauncher.model.Contact
import com.singularitycoder.flowlauncher.model.Glance

@Database(
    entities = [
        Contact::class,
        Glance::class,
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
    abstract fun glanceDao(): GlanceDao
}

