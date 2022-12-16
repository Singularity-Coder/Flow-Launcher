package com.singularitycoder.flowlauncher.home.model

import android.graphics.drawable.Drawable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.singularitycoder.flowlauncher.helper.constants.Table

@Entity(tableName = Table.APP)
data class App(
    var title: String = "",
    @PrimaryKey var packageName: String = "",
    var iconPath: String = "",
    @Ignore var icon: Drawable? = null,
)