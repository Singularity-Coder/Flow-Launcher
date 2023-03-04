package com.singularitycoder.flowlauncher.universalSearch

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class UniversalSearchViewModel @Inject constructor() : ViewModel() {

    private val _textChangeFlow = MutableStateFlow<String>("")
    val textChangeFlow = _textChangeFlow.asStateFlow()

    //--------------------------------------------------------------------------------

    fun setQueryValue(text: String) {
        _textChangeFlow.value = text
    }
}
