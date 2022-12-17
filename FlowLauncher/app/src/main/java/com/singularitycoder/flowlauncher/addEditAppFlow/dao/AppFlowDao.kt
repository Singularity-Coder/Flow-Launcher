package com.singularitycoder.flowlauncher.addEditAppFlow.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.flowlauncher.addEditAppFlow.model.AppFlow
import com.singularitycoder.flowlauncher.helper.constants.Table
import kotlinx.coroutines.flow.Flow

@Dao
interface AppFlowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appFlow: AppFlow)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appFlowList: List<AppFlow>)

    @Transaction
    @Query("SELECT * FROM ${Table.APP_FLOW} WHERE appFlowName LIKE :name LIMIT 1")
    suspend fun getAppFlowByName(name: String): AppFlow?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(appFlow: AppFlow)

    @Delete
    suspend fun delete(appFlow: AppFlow)

    @Query("SELECT * FROM ${Table.APP_FLOW}")
    fun getAllStateFlow(): Flow<List<AppFlow>>

    @Query("SELECT * FROM ${Table.APP_FLOW}")
    fun getAllLiveData(): LiveData<AppFlow>

    @Query("SELECT * FROM ${Table.APP_FLOW}")
    suspend fun getAll(): List<AppFlow>

    @Query("DELETE FROM ${Table.APP_FLOW}")
    suspend fun deleteAll()
}
