package com.singularitycoder.flowlauncher.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.flowlauncher.home.dao.AppDao
import com.singularitycoder.flowlauncher.home.model.App
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appDao: AppDao,
) : ViewModel() {

    private val _appListStateFlow = MutableStateFlow<List<App>>(emptyList())
    val appListStateFlow = _appListStateFlow.asStateFlow()

    val appListLiveData: LiveData<List<App>> by lazy {
        appDao.getAllLiveData()
    }

    init {
        getAllAppsStateFlow()
    }

    private fun getAllAppsStateFlow() = viewModelScope.launch {
        appDao
            .getAllStateFlow().catch { it: Throwable ->
                println(it.message)
            }.collect { it: List<App> ->
                _appListStateFlow.value = it
            }
    }

    fun removeAppFromDb(app: App?) = viewModelScope.launch {
        appDao.delete(app ?: return@launch)
    }
}
