package com.singularitycoder.flowlauncher.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.flowlauncher.helper.constants.Table
import com.singularitycoder.flowlauncher.model.Quote

@Dao
interface QuoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quote: Quote)

    @Transaction
    @Query("SELECT * FROM ${Table.QUOTE} WHERE author LIKE :author LIMIT 1")
    suspend fun getItemByTitle(author: String): Quote?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(quote: Quote)

    @Delete
    suspend fun delete(quote: Quote)

    @Query("SELECT * FROM ${Table.QUOTE}")
    fun getLatestLiveData(): LiveData<Quote>

    @Query("SELECT * FROM ${Table.QUOTE}")
    fun getAllLiveData(): LiveData<List<Quote>>

    @Query("SELECT * FROM ${Table.QUOTE}")
    suspend fun getAll(): List<Quote>

    @Query("DELETE FROM ${Table.QUOTE}")
    suspend fun deleteAll()

    @Query("DELETE FROM ${Table.QUOTE} WHERE title LIKE :title")
    fun deleteByQuote(title: String): Int
}
