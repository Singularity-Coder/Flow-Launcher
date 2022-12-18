package com.singularitycoder.flowlauncher.addEditAppFlow.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.flowlauncher.addEditAppFlow.dao.AppFlowDao
import com.singularitycoder.flowlauncher.addEditAppFlow.model.AppFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppFlowViewModel @Inject constructor(
    private val appFlowDao: AppFlowDao
) : ViewModel() {

    private val _appFlowListStateFlow = MutableStateFlow<List<AppFlow>>(emptyList())
    val appFlowListStateFlow = _appFlowListStateFlow.asStateFlow()

    init {
        getAllAppsStateFlow()
    }

    private fun getAllAppsStateFlow() = viewModelScope.launch {
        appFlowDao
            .getAllStateFlow().catch { it: Throwable ->
                println(it.message)
            }.collect {
                _appFlowListStateFlow.value = it
            }
    }

    suspend fun addAppFlow(appFlow: AppFlow) = appFlowDao.insert(appFlow)

    suspend fun addAllAppFlows(appFlowList: List<AppFlow>) = appFlowDao.insertAll(appFlowList)

    suspend fun getAllAppFlows(): List<AppFlow> = appFlowDao.getAll()

    suspend fun deleteAllAppFlows() = appFlowDao.deleteAll()

    fun deleteAppFlow(appFlow: AppFlow) = viewModelScope.launch {
        appFlowDao.delete(appFlow)
    }
}
