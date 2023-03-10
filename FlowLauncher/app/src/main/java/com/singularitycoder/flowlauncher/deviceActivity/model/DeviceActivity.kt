package com.singularitycoder.flowlauncher.deviceActivity.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.singularitycoder.flowlauncher.helper.constants.Table
import com.singularitycoder.flowlauncher.helper.timeNow

@Entity(tableName = Table.DEVICE_ACTIVITY)
data class DeviceActivity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var title: String = "",
    var date: Long = timeNow,
    @Ignore var isDateShown: Boolean = false,
)