package com.singularitycoder.flowlauncher.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.flowlauncher.helper.Table
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = Table.NEWS)
data class News(
    val imageUrl: String? = "",
    @PrimaryKey val title: String = "",
    val source: String? = "",
    val time: String? = "",
    val link: String? = ""
) : Parcelable {
    constructor() : this("", "", "", "", "")
}
