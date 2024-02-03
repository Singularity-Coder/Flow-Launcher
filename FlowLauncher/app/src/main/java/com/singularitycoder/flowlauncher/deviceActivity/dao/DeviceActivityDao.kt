package com.singularitycoder.flowlauncher.deviceActivity.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.flowlauncher.addEditAppFlow.model.AppFlow
import com.singularitycoder.flowlauncher.deviceActivity.model.DeviceActivity
import com.singularitycoder.flowlauncher.helper.constants.Table
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(deviceActivity: DeviceActivity)

    @Transaction
    @Query("SELECT * FROM ${Table.DEVICE_ACTIVITY} WHERE id LIKE :id LIMIT 1")
    suspend fun getItemByTitle(id: String): DeviceActivity

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(deviceActivity: DeviceActivity)

    @Delete
    suspend fun delete(deviceActivity: DeviceActivity)

    @Query("SELECT * FROM ${Table.DEVICE_ACTIVITY}")
    fun getAllStateFlow(): Flow<List<DeviceActivity>>

    @Query("SELECT * FROM ${Table.DEVICE_ACTIVITY}")
    fun getAllLiveData(): LiveData<List<DeviceActivity>>

    @Transaction
    @Query("SELECT * FROM ${Table.DEVICE_ACTIVITY} WHERE id = :id")
    suspend fun getDeviceActivityFlowById(id: Long): DeviceActivity

    @Query("SELECT * FROM ${Table.DEVICE_ACTIVITY}")
    suspend fun getAll(): List<DeviceActivity>

    @Query("DELETE FROM ${Table.DEVICE_ACTIVITY}")
    suspend fun deleteAll()

    @Query("DELETE FROM ${Table.DEVICE_ACTIVITY} WHERE :elapsedTime > date")
    suspend fun deleteAllActivityOlderThan7Days(elapsedTime: Long)

    @Query("DELETE FROM ${Table.DEVICE_ACTIVITY} WHERE title LIKE :link")
    suspend fun deleteByTitle(link: String): Int
}
