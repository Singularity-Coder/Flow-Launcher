package com.singularitycoder.flowlauncher.model

import android.os.Parcelable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.flowlauncher.helper.constants.Table
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = Table.QUOTE)
data class Quote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String,
) : Parcelable

data class QuoteColor(
    @ColorRes val textColor: Int = 0,
    @ColorRes val iconColor: Int = 0,
    @DrawableRes val gradientColor: Int = 0
)
