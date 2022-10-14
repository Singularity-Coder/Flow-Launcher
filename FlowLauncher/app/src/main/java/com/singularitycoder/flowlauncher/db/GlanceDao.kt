package com.singularitycoder.flowlauncher.db

import androidx.room.*
import com.singularitycoder.flowlauncher.helper.Table
import com.singularitycoder.flowlauncher.model.Glance

@Dao
interface GlanceDao {

    // Single Item CRUD ops ------------------------------------------------------------------------------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(glance: Glance)

//    @Transaction
//    @Query("SELECT * FROM ${Table.GLANCE} WHERE mobileNumber LIKE :mobileNumber LIMIT 1")
//    suspend fun getContactByPhone(mobileNumber: String): Contact?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(glance: Glance)

    @Delete
    suspend fun delete(glance: Glance)

    // ---------------------------------------------------------------------------------------------------------------------------------------------

    @Query("DELETE FROM ${Table.GLANCE}")
    suspend fun deleteAll()
}
