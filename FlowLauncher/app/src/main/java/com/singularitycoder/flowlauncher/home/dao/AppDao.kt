package com.singularitycoder.flowlauncher.home.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.flowlauncher.helper.constants.Table
import com.singularitycoder.flowlauncher.home.model.App
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: App)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(appList: List<App>)


    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(app: App?)


    @Transaction
    @Query("SELECT * FROM ${Table.APP} WHERE packageName LIKE :packageName LIMIT 1")
    suspend fun getAppByPackage(packageName: String): App?

    @Query("SELECT * FROM ${Table.APP}")
    fun getAllLiveData(): LiveData<List<App>>

    @Query("SELECT * FROM ${Table.APP}")
    fun getAllStateFlow(): Flow<List<App>>

    @Query("SELECT * FROM ${Table.APP}")
    suspend fun getAll(): List<App>


    @Delete
    suspend fun delete(app: App?)

    @Query("DELETE FROM ${Table.APP} WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String?)

    @Query("DELETE FROM ${Table.APP}")
    suspend fun deleteAll()
}
