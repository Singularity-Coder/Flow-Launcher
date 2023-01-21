package com.singularitycoder.flowlauncher.helper.quickactionview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

/**
 * Determines which config to get values from, based on the state of each config.
 */
class ConfigHelper(actionConfig: Action.Config?, quickActionViewConfig: QuickActionView.Config) {
    private val mActionConfig: Action.Config?
    private val mQuickActionViewConfig: QuickActionView.Config

    init {
        mActionConfig = actionConfig
        mQuickActionViewConfig = quickActionViewConfig
    }

    fun getTextBackgroundDrawable(context: Context?): Drawable? {
        return if (mActionConfig?.getTextBackgroundDrawable(context) != null) {
            mActionConfig.getTextBackgroundDrawable(context)
        } else mQuickActionViewConfig.getTextBackgroundDrawable(context)
    }

    @get:ColorInt
    val textColor: Int
        get() = if (mActionConfig != null && mActionConfig.textColor != 0) {
            mActionConfig.textColor
        } else mQuickActionViewConfig.textColor
    val backgroundColorStateList: ColorStateList?
        get() = if (mActionConfig != null && mActionConfig.backgroundColorStateList != null) {
            mActionConfig.backgroundColorStateList
        } else mQuickActionViewConfig.backgroundColorStateList
    val typeface: Typeface
        get() = mQuickActionViewConfig.typeface
    val textSize: Int
        get() = mQuickActionViewConfig.textSize
    val textPaddingTop: Int
        get() = mQuickActionViewConfig.textPaddingTop
    val textPaddingBottom: Int
        get() = mQuickActionViewConfig.textPaddingBottom
    val textPaddingLeft: Int
        get() = mQuickActionViewConfig.textPaddingLeft
    val textPaddingRight: Int
        get() = mQuickActionViewConfig.textPaddingRight
}