package com.singularitycoder.flowlauncher.glance.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.flowlauncher.helper.constants.Table
import com.singularitycoder.flowlauncher.glance.model.YoutubeVideo

@Dao
interface YoutubeVideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(youtubeVideo: YoutubeVideo)

    @Transaction
    @Query("SELECT * FROM ${Table.YOUTUBE_VIDEO} WHERE title LIKE :title LIMIT 1")
    suspend fun getItemByTitle(title: String): YoutubeVideo?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(youtubeVideo: YoutubeVideo)

    @Delete
    suspend fun delete(youtubeVideo: YoutubeVideo)

    @Query("SELECT * FROM ${Table.YOUTUBE_VIDEO}")
    fun getLatestLiveData(): LiveData<YoutubeVideo>

    @Query("SELECT * FROM ${Table.YOUTUBE_VIDEO}")
    fun getAllLiveData(): LiveData<List<YoutubeVideo>>

    @Query("SELECT * FROM ${Table.YOUTUBE_VIDEO}")
    suspend fun getAll(): List<YoutubeVideo>

    @Query("SELECT * FROM ${Table.YOUTUBE_VIDEO}")
    fun getAllVideos(): List<YoutubeVideo>

    @Query("DELETE FROM ${Table.YOUTUBE_VIDEO}")
    suspend fun deleteAll()

    @Query("DELETE FROM ${Table.YOUTUBE_VIDEO} WHERE videoId LIKE :videoId")
    suspend fun deleteByVideoId(videoId: String): Int
}
