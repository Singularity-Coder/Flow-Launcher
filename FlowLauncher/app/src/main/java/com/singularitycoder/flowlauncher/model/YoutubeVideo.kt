package com.singularitycoder.flowlauncher.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.flowlauncher.helper.constants.Table
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = Table.YOUTUBE_VIDEO)
data class YoutubeVideo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val videoId: String
) : Parcelable