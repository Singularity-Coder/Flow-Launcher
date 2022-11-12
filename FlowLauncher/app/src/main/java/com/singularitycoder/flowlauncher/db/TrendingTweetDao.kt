package com.singularitycoder.flowlauncher.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.flowlauncher.helper.Table
import com.singularitycoder.flowlauncher.model.TrendingTweet

@Dao
interface TrendingTweetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trendingTweet: TrendingTweet)

    @Transaction
    @Query("SELECT * FROM ${Table.TWITTER_TRENDING} WHERE hashTag LIKE :hashTag LIMIT 1")
    suspend fun getTrendingTweetsByTitle(hashTag: String): TrendingTweet?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(trendingTweet: TrendingTweet)

    @Delete
    suspend fun delete(trendingTweet: TrendingTweet)

    @Query("SELECT * FROM ${Table.TWITTER_TRENDING}")
    fun getLatestTrendingTweetsLiveData(): LiveData<TrendingTweet>

    @Query("SELECT * FROM ${Table.TWITTER_TRENDING}")
    fun getAllTrendingTweetsLiveData(): LiveData<List<TrendingTweet>>

    @Query("SELECT * FROM ${Table.TWITTER_TRENDING}")
    suspend fun getAll(): List<TrendingTweet>

    @Query("DELETE FROM ${Table.TWITTER_TRENDING}")
    suspend fun deleteAll()
}
