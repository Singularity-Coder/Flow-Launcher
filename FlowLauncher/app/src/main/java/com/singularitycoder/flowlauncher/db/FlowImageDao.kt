package com.singularitycoder.flowlauncher.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.flowlauncher.helper.constants.Table
import com.singularitycoder.flowlauncher.model.GlanceImage

@Dao
interface FlowImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(glanceImage: GlanceImage)

    @Transaction
    @Query("SELECT * FROM ${Table.FLOW_IMAGE} WHERE title LIKE :title LIMIT 1")
    suspend fun getItemByTitle(title: String): GlanceImage?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(glanceImage: GlanceImage)

    @Delete
    suspend fun delete(glanceImage: GlanceImage)

    @Query("SELECT * FROM ${Table.FLOW_IMAGE}")
    fun getLatestLiveData(): LiveData<GlanceImage>

    @Query("SELECT * FROM ${Table.FLOW_IMAGE}")
    fun getAllLiveData(): LiveData<List<GlanceImage>>

    @Query("SELECT * FROM ${Table.FLOW_IMAGE}")
    suspend fun getAll(): List<GlanceImage>

    @Query("DELETE FROM ${Table.FLOW_IMAGE}")
    suspend fun deleteAll()
}
