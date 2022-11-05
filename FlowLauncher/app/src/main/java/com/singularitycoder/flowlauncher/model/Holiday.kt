package com.singularitycoder.flowlauncher.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.flowlauncher.helper.Table
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = Table.HOLIDAY)
data class Holiday(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imageUrl: String? = "",
    val title: String = "",
    val date: String? = "",
    val link: String? = "",
    val location: String? = "",
    val header: String? = ""
) : Parcelable {
    constructor() : this(0, "", "", "", "", "", "")
}
