package com.singularitycoder.flowlauncher.addEditAppFlow.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.flowlauncher.addEditAppFlow.model.AppFlow
import com.singularitycoder.flowlauncher.helper.constants.Table
import com.singularitycoder.flowlauncher.home.model.App
import kotlinx.coroutines.flow.Flow

@Dao
interface AppFlowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appFlow: AppFlow)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appFlowList: List<AppFlow>)

    @Transaction
    @Query("SELECT * FROM ${Table.APP_FLOW} WHERE appFlowName LIKE :name LIMIT 1")
    suspend fun getAppFlowByName(name: String): AppFlow

    @Transaction
    @Query("SELECT * FROM ${Table.APP_FLOW} WHERE id = :id")
    suspend fun getAppFlowById(id: Long): AppFlow

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(appFlow: AppFlow)

    @Query("UPDATE ${Table.APP_FLOW} SET appList = :appList WHERE id = :id")
    suspend fun updateById(id: Long, appList: List<App>)

    @Query("UPDATE ${Table.APP_FLOW} SET isSelected = :isSelected WHERE id IN (:appFlowIdList)")
    suspend fun updateAll(isSelected: Boolean, appFlowIdList: List<Long>)

    // https://stackoverflow.com/questions/44711911/android-room-database-transactions
    @Transaction
    suspend fun updateAllFlowsToNotSelectedAndThenSetSelectedFlow(
        isSelected: Boolean,
        appFlowIdList: List<Long>,
        appFlow: AppFlow
    ) {
        updateAll(isSelected, appFlowIdList)
        update(appFlow)
    }

    @Delete
    suspend fun delete(appFlow: AppFlow)

    @Query("SELECT * FROM ${Table.APP_FLOW}")
    fun getAllStateFlow(): Flow<List<AppFlow>>

    @Query("SELECT * FROM ${Table.APP_FLOW}")
    fun getAllLiveData(): LiveData<AppFlow>

    @Query("SELECT * FROM ${Table.APP_FLOW}")
    suspend fun getAll(): List<AppFlow>

    // https://www.sqlitetutorial.net/sqlite-select/
    @Query("SELECT id FROM ${Table.APP_FLOW} ")
    suspend fun getAllIds(): List<Long>

    @Query("DELETE FROM ${Table.APP_FLOW}")
    suspend fun deleteAll()
}
