package com.singularitycoder.flowlauncher.addEditAppFlow.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.flowlauncher.helper.constants.Table

@Entity(tableName = Table.APP_FLOW)
data class AppFlow(
    @PrimaryKey val title: String = "",
    val packageList: List<String> = emptyList()
)