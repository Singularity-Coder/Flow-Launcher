package com.singularitycoder.flowlauncher.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.flowlauncher.helper.constants.Table
import com.singularitycoder.flowlauncher.model.FlowImage

@Dao
interface FlowImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flowImage: FlowImage)

    @Transaction
    @Query("SELECT * FROM ${Table.FLOW_IMAGE} WHERE title LIKE :title LIMIT 1")
    suspend fun getItemByTitle(title: String): FlowImage?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(flowImage: FlowImage)

    @Delete
    suspend fun delete(flowImage: FlowImage)

    @Query("SELECT * FROM ${Table.FLOW_IMAGE}")
    fun getLatestLiveData(): LiveData<FlowImage>

    @Query("SELECT * FROM ${Table.FLOW_IMAGE}")
    fun getAllLiveData(): LiveData<List<FlowImage>>

    @Query("SELECT * FROM ${Table.FLOW_IMAGE}")
    suspend fun getAll(): List<FlowImage>

    @Query("DELETE FROM ${Table.FLOW_IMAGE}")
    suspend fun deleteAll()
}
