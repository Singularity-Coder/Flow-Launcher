package com.singularitycoder.flowlauncher.universalSearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.flowlauncher.addEditAppFlow.model.SelectedFlowArgs
import com.singularitycoder.flowlauncher.glance.model.GlanceImage
import com.singularitycoder.flowlauncher.glance.model.YoutubeVideo
import com.singularitycoder.flowlauncher.today.model.Quote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UniversalSearchViewModel @Inject constructor() : ViewModel() {

    private val _appsStateFlow = MutableStateFlow<String>("")
    val appsStateFlow = _appsStateFlow.asStateFlow()

    private val _contactsStateFlow = MutableStateFlow<String>("")
    val contactsStateFlow = _contactsStateFlow.asStateFlow()

    private val _smsStateFlow = MutableStateFlow<String>("")
    val smsStateFlow = _smsStateFlow.asStateFlow()

    private val _androidSettingsStateFlow = MutableStateFlow<String>("")
    val androidSettingsStateFlow = _androidSettingsStateFlow.asStateFlow()

    private val _sanskritWordsStateFlow = MutableStateFlow<String>("")
    val sanskritWordsStateFlow = _sanskritWordsStateFlow.asStateFlow()

    private val _englishWordsStateFlow = MutableStateFlow<String>("")
    val englishWordsStateFlow = _englishWordsStateFlow.asStateFlow()

    //--------------------------------------------------------------------------------

    fun setQueryValue(text: String) {
        _appsStateFlow.value = text
        _contactsStateFlow.value = text
        _smsStateFlow.value = text
        _androidSettingsStateFlow.value = text
        _sanskritWordsStateFlow.value = text
        _englishWordsStateFlow.value = text
    }
}
