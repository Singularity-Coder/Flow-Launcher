package com.singularitycoder.flowlauncher.di

import android.content.Context
import androidx.room.Room
import com.singularitycoder.flowlauncher.db.ContactDao
import com.singularitycoder.flowlauncher.db.ContactDatabase
import com.singularitycoder.flowlauncher.helper.Db
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ContactModule {

    @Singleton
    @Provides
    fun injectContactRoomDatabase(@ApplicationContext context: Context): ContactDatabase {
        return Room.databaseBuilder(context, ContactDatabase::class.java, Db.CONTACT).build()
    }

    @Singleton
    @Provides
    fun injectContactDao(db: ContactDatabase): ContactDao = db.contactDao()
}
