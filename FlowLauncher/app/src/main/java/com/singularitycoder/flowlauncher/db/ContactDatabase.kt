package com.singularitycoder.flowlauncher.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.singularitycoder.flowlauncher.model.Contact

@Database(
    entities = [
        Contact::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class ContactDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}

