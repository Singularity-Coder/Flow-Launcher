package com.singularitycoder.flowlauncher.helper.quickactionview

import android.content.Context
import android.util.TypedValue

/**
 * Gets the colors
 */
internal object ColorUtils {
    private val sTypedValue = TypedValue()
    fun getThemeAttrColor(context: Context, attributeColor: Int): Int {
        context.theme.resolveAttribute(attributeColor, sTypedValue, true)
        return sTypedValue.data
    }
}