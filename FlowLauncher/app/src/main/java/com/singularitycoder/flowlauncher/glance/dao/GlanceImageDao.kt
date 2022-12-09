package com.singularitycoder.flowlauncher.glance.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.flowlauncher.helper.constants.Table
import com.singularitycoder.flowlauncher.glance.model.GlanceImage

@Dao
interface GlanceImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(glanceImage: GlanceImage)

    @Transaction
    @Query("SELECT * FROM ${Table.GLANCE_IMAGE} WHERE title LIKE :title LIMIT 1")
    suspend fun getItemByTitle(title: String): GlanceImage?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(glanceImage: GlanceImage)

    @Delete
    suspend fun delete(glanceImage: GlanceImage)

    @Query("SELECT * FROM ${Table.GLANCE_IMAGE}")
    fun getLatestLiveData(): LiveData<GlanceImage>

    @Query("SELECT * FROM ${Table.GLANCE_IMAGE}")
    fun getAllLiveData(): LiveData<List<GlanceImage>>

    @Query("SELECT * FROM ${Table.GLANCE_IMAGE}")
    suspend fun getAll(): List<GlanceImage>

    @Query("DELETE FROM ${Table.GLANCE_IMAGE}")
    suspend fun deleteAll()

    @Query("DELETE FROM ${Table.GLANCE_IMAGE} WHERE link LIKE :link")
    suspend fun deleteByLink(link: String): Int
}
