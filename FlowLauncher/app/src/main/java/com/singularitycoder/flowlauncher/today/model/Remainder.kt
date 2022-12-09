package com.singularitycoder.flowlauncher.today.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Remainder(
    val title: String,
    val date: String,
    val time: String
) : Parcelable {
    constructor() : this("", "", "")
}