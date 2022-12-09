package com.singularitycoder.flowlauncher.home.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.flowlauncher.helper.constants.Table
import com.singularitycoder.flowlauncher.home.model.App

@Dao
interface AppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: App)

    @Transaction
    @Query("SELECT * FROM ${Table.APP} WHERE packageName LIKE :packageName LIMIT 1")
    suspend fun getAppByPackage(packageName: String): App?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(app: App)

    @Delete
    suspend fun delete(app: App)

    @Query("SELECT * FROM ${Table.APP}")
    fun getAllLiveData(): LiveData<App>

    @Query("SELECT * FROM ${Table.APP}")
    suspend fun getAll(): List<App>

    @Query("DELETE FROM ${Table.APP}")
    suspend fun deleteAll()
}
