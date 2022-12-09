package com.singularitycoder.flowlauncher.addEditAppFlow.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.flowlauncher.helper.constants.Table
import com.singularitycoder.flowlauncher.addEditAppFlow.model.AppFlow

@Dao
interface AppFlowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appFlow: AppFlow)

    @Transaction
    @Query("SELECT * FROM ${Table.APP_FLOW} WHERE title LIKE :title LIMIT 1")
    suspend fun getAppFlowByTitle(title: String): AppFlow?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(appFlow: AppFlow)

    @Delete
    suspend fun delete(appFlow: AppFlow)

    @Query("SELECT * FROM ${Table.APP_FLOW}")
    fun getAllLiveData(): LiveData<AppFlow>

    @Query("SELECT * FROM ${Table.APP_FLOW}")
    suspend fun getAll(): List<AppFlow>

    @Query("DELETE FROM ${Table.APP_FLOW}")
    suspend fun deleteAll()
}
