package com.singularitycoder.flowlauncher.helper.quickActionView

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes

/**
 * Determines which config to get values from, based on the state of each config.
 */
class ConfigHelper(actionConfig: Action.Config?, quickActionViewConfig: QuickActionView.Config) {
    private val actionConfig: Action.Config?
    private val quickActionViewConfig: QuickActionView.Config

    init {
        this.actionConfig = actionConfig
        this.quickActionViewConfig = quickActionViewConfig
    }

    fun getTextBackgroundDrawable(context: Context?): Drawable? {
        return if (actionConfig?.getTextBackgroundDrawable(context) != null) {
            actionConfig.getTextBackgroundDrawable(context)
        } else quickActionViewConfig.getTextBackgroundDrawable(context)
    }

    @get:ColorInt
    val textColor: Int
        get() = if (actionConfig != null && actionConfig.textColor != 0) {
            actionConfig.textColor
        } else quickActionViewConfig.textColor
    @get:ColorRes
    val iconCustomColor: Int
        get() = if (actionConfig != null && actionConfig.iconCustomColor != 0) {
            actionConfig.iconCustomColor
        } else quickActionViewConfig.iconCustomColor
    val backgroundColorStateList: ColorStateList?
        get() = if (actionConfig?.backgroundColorStateList != null) {
            actionConfig.backgroundColorStateList
        } else quickActionViewConfig.backgroundColorStateList
    val typeface: Typeface
        get() = quickActionViewConfig.typeface
    val textSize: Int
        get() = quickActionViewConfig.textSize
    val textPaddingTop: Int
        get() = quickActionViewConfig.textPaddingTop
    val textPaddingBottom: Int
        get() = quickActionViewConfig.textPaddingBottom
    val textPaddingLeft: Int
        get() = quickActionViewConfig.textPaddingLeft
    val textPaddingRight: Int
        get() = quickActionViewConfig.textPaddingRight
}