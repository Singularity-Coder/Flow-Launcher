package com.singularitycoder.flowlauncher.today.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.flowlauncher.helper.constants.Table
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = Table.NEWS)
data class News(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imageUrl: String? = "",
    val title: String = "",
    val source: String? = "",
    val time: String? = "",
    val link: String? = ""
) : Parcelable {
    constructor() : this(0, "", "", "", "", "")
}
