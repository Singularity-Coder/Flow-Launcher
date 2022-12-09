package com.singularitycoder.flowlauncher.today.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Today(
    val quoteList: List<Quote>,
    val remaindersList: List<Remainder>,
    val weather: Weather,
    val newsList: List<News>
) : Parcelable
