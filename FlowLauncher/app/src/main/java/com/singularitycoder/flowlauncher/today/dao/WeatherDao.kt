package com.singularitycoder.flowlauncher.today.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.flowlauncher.helper.constants.Table
import com.singularitycoder.flowlauncher.today.model.Weather

@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weather: Weather)

    @Transaction
    @Query("SELECT * FROM ${Table.WEATHER} WHERE location LIKE :location LIMIT 1")
    suspend fun getWeatherByLocation(location: String): Weather?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(weather: Weather)

    @Delete
    suspend fun delete(weather: Weather)

    @Query("SELECT * FROM ${Table.WEATHER}")
    fun getLatestWeatherLiveData(): LiveData<Weather>

    @Query("SELECT * FROM ${Table.WEATHER}")
    suspend fun getAll(): List<Weather>

    @Query("DELETE FROM ${Table.WEATHER}")
    suspend fun deleteAll()
}
