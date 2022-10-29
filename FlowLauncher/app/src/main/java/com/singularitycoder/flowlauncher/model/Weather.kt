package com.singularitycoder.flowlauncher.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.flowlauncher.helper.Table
import kotlinx.parcelize.Parcelize

@Entity(tableName = Table.WEATHER)
@Parcelize
data class Weather(
    val temperature: String,
    val condition: String,
    val imageUrl: String,
    @PrimaryKey val location: String,
    val dateTime: String
) : Parcelable
