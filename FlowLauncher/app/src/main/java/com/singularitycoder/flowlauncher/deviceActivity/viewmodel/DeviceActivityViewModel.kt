package com.singularitycoder.flowlauncher.deviceActivity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.flowlauncher.deviceActivity.dao.DeviceActivityDao
import com.singularitycoder.flowlauncher.deviceActivity.model.DeviceActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceActivityViewModel @Inject constructor(
    private val deviceActivityDao: DeviceActivityDao
) : ViewModel() {

    private val _deviceActivityListStateFlow = MutableStateFlow<List<DeviceActivity>>(emptyList())
    val deviceActivityListStateFlow = _deviceActivityListStateFlow.asStateFlow()

    init {
        getAllDeviceActivitiesStateFlow()
    }

    private fun getAllDeviceActivitiesStateFlow() = viewModelScope.launch {
        deviceActivityDao
            .getAllStateFlow().catch { it: Throwable ->
                println(it.message)
            }.collect {
                _deviceActivityListStateFlow.value = it
            }
    }

    fun addDeviceActivity(deviceActivity: DeviceActivity?) = viewModelScope.launch {
        deviceActivityDao.insert(deviceActivity ?: return@launch)
    }

    fun deleteDeviceActivity(deviceActivity: DeviceActivity?) = viewModelScope.launch {
        deviceActivityDao.delete(deviceActivity ?: return@launch)
    }

    fun deleteAllDeviceActivity() = viewModelScope.launch {
        deviceActivityDao.deleteAll()
    }

    fun deleteAllDeviceActivityOlderThan7Days(elapsedTime: Long) = viewModelScope.launch {
        deviceActivityDao.deleteAllActivityOlderThan7Days(elapsedTime)
    }
}
