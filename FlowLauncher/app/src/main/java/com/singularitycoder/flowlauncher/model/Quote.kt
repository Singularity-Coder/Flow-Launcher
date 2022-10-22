package com.singularitycoder.flowlauncher.model

import android.os.Parcelable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class Quote(
    val quote: String,
    val author: String,
) : Parcelable

data class QuoteColor(
    @ColorRes val textColor: Int = 0,
    @ColorRes val iconColor: Int = 0,
    @DrawableRes val gradientColor: Int = 0
)
