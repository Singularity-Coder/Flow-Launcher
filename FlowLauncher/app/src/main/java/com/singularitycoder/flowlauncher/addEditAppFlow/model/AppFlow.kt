package com.singularitycoder.flowlauncher.addEditAppFlow.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.flowlauncher.helper.constants.Table
import com.singularitycoder.flowlauncher.home.model.App

@Entity(tableName = Table.APP_FLOW)
data class AppFlow(
    @PrimaryKey val appFlowName: String = "",
    val isSelected: Boolean = false,
    val appList: List<App> = emptyList()
)