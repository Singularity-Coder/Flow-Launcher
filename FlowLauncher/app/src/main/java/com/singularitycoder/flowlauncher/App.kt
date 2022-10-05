package com.singularitycoder.flowlauncher

import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

data class App(
    var title: String = "",
    var packageName: String = "",
    var icon: Drawable? = null
)