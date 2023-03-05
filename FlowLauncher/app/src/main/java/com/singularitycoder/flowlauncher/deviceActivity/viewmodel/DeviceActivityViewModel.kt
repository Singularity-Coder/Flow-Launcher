package com.singularitycoder.flowlauncher.deviceActivity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.flowlauncher.addEditAppFlow.model.AppFlow
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

    private val _appFlowListStateFlow = MutableStateFlow<List<DeviceActivity>>(emptyList())
    val appFlowListStateFlow = _appFlowListStateFlow.asStateFlow()

    init {
        getAllAppsStateFlow()
    }

    private fun getAllAppsStateFlow() = viewModelScope.launch {
        deviceActivityDao
            .getAllStateFlow().catch { it: Throwable ->
                println(it.message)
            }.collect {
                _appFlowListStateFlow.value = it
            }
    }

    suspend fun getAppFlowById(id: Long) = deviceActivityDao.getAppFlowById(id)
}
