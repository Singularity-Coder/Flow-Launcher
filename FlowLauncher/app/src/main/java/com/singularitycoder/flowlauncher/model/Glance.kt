package com.singularitycoder.flowlauncher.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.flowlauncher.helper.Table
import kotlinx.parcelize.Parcelize

@Parcelize
data class Glance(
    var id: Int,
    val unreadSmsCount: Int,
    val missedCallCount: Int,
    val imagePathList: List<String>,
    val youtubeVideoIdList: List<String>,
    val remainderList: List<Remainder>
) : Parcelable {
    constructor() : this(0,0, 0, emptyList(), emptyList(), emptyList())
}