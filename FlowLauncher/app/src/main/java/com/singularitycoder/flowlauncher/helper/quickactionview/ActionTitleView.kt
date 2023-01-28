package com.singularitycoder.flowlauncher.helper.quickactionview

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.widget.AppCompatTextView

/**
 * Shows the title of the Action
 */
@SuppressLint("ViewConstructor")
internal class ActionTitleView(context: Context?, private val action: Action, private val configHelper: ConfigHelper) : AppCompatTextView(context!!) {
    init {
        init()
    }

    private fun init() {
        setPadding(configHelper.textPaddingLeft, configHelper.textPaddingTop, configHelper.textPaddingRight, configHelper.textPaddingBottom)
        setTextColor(configHelper.textColor)
        textSize = configHelper.textSize.toFloat()
        setBackgroundDrawable(configHelper.getTextBackgroundDrawable(context))
        text = action.title
    }
}