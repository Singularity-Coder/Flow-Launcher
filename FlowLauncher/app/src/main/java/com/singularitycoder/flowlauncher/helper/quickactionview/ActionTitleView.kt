package com.singularitycoder.flowlauncher.helper.quickactionview

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.widget.AppCompatTextView

/**
 * Shows the title of the Action
 */
@SuppressLint("ViewConstructor")
internal class ActionTitleView(context: Context?, private val mAction: Action, private val mConfigHelper: ConfigHelper) : AppCompatTextView(context!!) {
    init {
        init()
    }

    private fun init() {
        setPadding(mConfigHelper.textPaddingLeft, mConfigHelper.textPaddingTop, mConfigHelper.textPaddingRight, mConfigHelper.textPaddingBottom)
        setTextColor(mConfigHelper.textColor)
        textSize = mConfigHelper.textSize.toFloat()
        setBackgroundDrawable(mConfigHelper.getTextBackgroundDrawable(context))
        text = mAction.title
    }
}