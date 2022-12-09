package com.singularitycoder.flowlauncher.glance.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.flowlauncher.helper.constants.Table
import com.singularitycoder.flowlauncher.glance.model.Holiday

@Dao
interface HolidayDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(holiday: Holiday)

    @Transaction
    @Query("SELECT * FROM ${Table.HOLIDAY} WHERE title LIKE :title LIMIT 1")
    suspend fun getNewsByTitle(title: String): Holiday?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(holiday: Holiday)

    @Delete
    suspend fun delete(holiday: Holiday)

    @Query("SELECT * FROM ${Table.HOLIDAY}")
    fun getLatestHolidaysLiveData(): LiveData<Holiday>

    @Query("SELECT * FROM ${Table.HOLIDAY}")
    fun getAllHolidaysLiveData(): LiveData<List<Holiday>>

    @Query("SELECT * FROM ${Table.HOLIDAY}")
    suspend fun getAll(): List<Holiday>

    @Query("DELETE FROM ${Table.HOLIDAY}")
    suspend fun deleteAll()
}
