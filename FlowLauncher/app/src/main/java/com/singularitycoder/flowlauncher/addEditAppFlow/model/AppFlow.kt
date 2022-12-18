package com.singularitycoder.flowlauncher.addEditAppFlow.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.flowlauncher.helper.constants.Table
import com.singularitycoder.flowlauncher.home.model.App

@Entity(tableName = Table.APP_FLOW)
data class AppFlow(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appFlowName: String = "",
    var isSelected: Boolean = false,
    val appList: List<App> = emptyList()
)