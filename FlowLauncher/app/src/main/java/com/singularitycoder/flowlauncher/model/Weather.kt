package com.singularitycoder.flowlauncher.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Weather(
    val temperature: String,
    val condition: String,
    val imageUrl: String,
    val location: String,
    val dateTime: String
) : Parcelable
