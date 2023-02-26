package com.singularitycoder.flowlauncher.addEditAppFlow.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.flowlauncher.addEditAppFlow.dao.AppFlowDao
import com.singularitycoder.flowlauncher.addEditAppFlow.model.AppFlow
import com.singularitycoder.flowlauncher.home.model.App
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

    suspend fun getAllFlowIds(): List<Long> = appFlowDao.getAllIds()

    suspend fun deleteAllAppFlows() = appFlowDao.deleteAll()

    suspend fun getAppFlowById(id: Long) = appFlowDao.getAppFlowById(id)

    suspend fun updateAppFlow(appFlow: AppFlow?) = appFlowDao.update(appFlow)

    suspend fun selectFlow(
        isSelected: Boolean,
        appFlowList: List<Long>,
        appFlow: AppFlow?
    ) = appFlowDao.updateAllFlowsToNotSelectedAndThenSetSelectedFlow(
        isSelected = isSelected,
        appFlowIdList = appFlowList,
        appFlow = appFlow
    )

    suspend fun updateAppFlowById(
        id: Long,
        appList: List<App>
    ) = appFlowDao.updateById(id, appList)

    suspend fun updateAllAppFlowsSelection(
        isSelected: Boolean,
        appFlowList: List<Long>
    ) = appFlowDao.updateAll(isSelected, appFlowList)

    fun deleteAppFlow(appFlow: AppFlow) = viewModelScope.launch {
        appFlowDao.delete(appFlow)
    }
}
