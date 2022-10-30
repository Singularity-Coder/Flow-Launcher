package com.singularitycoder.flowlauncher.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.flowlauncher.helper.Table
import kotlinx.parcelize.Parcelize

@Entity(tableName = Table.WEATHER)
@Parcelize
data class Weather(
    @PrimaryKey(autoGenerate = false) val id: Int = 0,
    val temperature: String,
    val condition: String,
    val imageUrl: String,
    val location: String,
    val dateTime: String
) : Parcelable
