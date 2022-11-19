package com.singularitycoder.flowlauncher.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.flowlauncher.helper.constants.Table
import com.singularitycoder.flowlauncher.model.Contact

@Dao
interface ContactDao {

    // Single Item CRUD ops ------------------------------------------------------------------------------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact: Contact)

    @Transaction
    @Query("SELECT * FROM ${Table.CONTACT} WHERE mobileNumber LIKE :mobileNumber LIMIT 1")
    suspend fun getContactByPhone(mobileNumber: String): Contact?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)

    // ---------------------------------------------------------------------------------------------------------------------------------------------

    // All of the parameters of the Insert method must either be classes annotated with Entity or collections/array of it.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(contactList: List<Contact>)

    @Query("SELECT * FROM ${Table.CONTACT}")
    fun getAllContactsListLiveData(): LiveData<List<Contact>>

    @Query("SELECT * FROM ${Table.CONTACT}")
    fun getLatestContactLiveData(): LiveData<Contact>

    @Query("SELECT * FROM ${Table.CONTACT}")
    suspend fun getAll(): List<Contact>

    @Query("DELETE FROM ${Table.CONTACT}")
    suspend fun deleteAll()
}
