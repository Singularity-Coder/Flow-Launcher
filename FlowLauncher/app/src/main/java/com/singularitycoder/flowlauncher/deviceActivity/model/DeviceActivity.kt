package com.singularitycoder.flowlauncher.deviceActivity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.flowlauncher.helper.constants.Table

@Entity(tableName = Table.DEVICE_ACTIVITY)
data class DeviceActivity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var title: String = "",
    var date: Long = 0,
)