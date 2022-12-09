package com.singularitycoder.flowlauncher.today.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.flowlauncher.helper.constants.Table
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = Table.TWITTER_TRENDING)
data class TrendingTweet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hashTag: String = "",
    val tweetCount: String? = "",
    val link: String? = ""
) : Parcelable {
    constructor() : this(0, "", "", "")
}
