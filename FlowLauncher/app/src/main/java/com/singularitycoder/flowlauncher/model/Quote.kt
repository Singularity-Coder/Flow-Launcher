package com.singularitycoder.flowlauncher.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Quote(
    val title: String,
    val author: String
) : Parcelable
