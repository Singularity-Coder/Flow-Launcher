package com.singularitycoder.flowlauncher.glance.model

import android.os.Parcelable
import com.singularitycoder.flowlauncher.today.model.Remainder
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