package com.singularitycoder.flowlauncher.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Headline(
    val imageUrl: String,
    val title: String,
    val source: String
) : Parcelable
