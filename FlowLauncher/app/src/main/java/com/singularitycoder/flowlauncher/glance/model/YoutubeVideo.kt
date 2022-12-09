package com.singularitycoder.flowlauncher.glance.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.flowlauncher.helper.constants.Table
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = Table.YOUTUBE_VIDEO)
data class YoutubeVideo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoId: String,
    val title: String
) : Parcelable
