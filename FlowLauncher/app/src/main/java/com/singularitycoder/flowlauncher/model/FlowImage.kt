package com.singularitycoder.flowlauncher.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.flowlauncher.helper.constants.Table
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = Table.FLOW_IMAGE)
data class FlowImage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val link: String,
    val title: String
) : Parcelable
