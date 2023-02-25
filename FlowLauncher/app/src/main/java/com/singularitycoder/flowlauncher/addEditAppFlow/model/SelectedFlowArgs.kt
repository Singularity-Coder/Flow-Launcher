package com.singularitycoder.flowlauncher.addEditAppFlow.model

data class SelectedFlowArgs(
    val isAddFlow: Boolean = false,
    val position: Int = 0,
    val appFlowId: Long = 0L
)