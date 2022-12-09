package com.singularitycoder.flowlauncher.today.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.flowlauncher.helper.constants.Table
import com.singularitycoder.flowlauncher.today.model.News

@Dao
interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(news: News)

    @Transaction
    @Query("SELECT * FROM ${Table.NEWS} WHERE title LIKE :title LIMIT 1")
    suspend fun getNewsByTitle(title: String): News?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(news: News)

    @Delete
    suspend fun delete(news: News)

    @Query("SELECT * FROM ${Table.NEWS}")
    fun getLatestNewsLiveData(): LiveData<News>

    @Query("SELECT * FROM ${Table.NEWS}")
    fun getAllNewsLiveData(): LiveData<List<News>>

    @Query("SELECT * FROM ${Table.NEWS}")
    suspend fun getAll(): List<News>

    @Query("DELETE FROM ${Table.NEWS}")
    suspend fun deleteAll()
}
