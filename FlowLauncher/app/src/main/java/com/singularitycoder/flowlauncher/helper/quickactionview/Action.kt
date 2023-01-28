package com.singularitycoder.flowlauncher.helper.quickactionview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.helper.color
import com.singularitycoder.flowlauncher.helper.getThemeAttrColor

/**
 * Action that can be added to the [QuickActionView]
 */
class Action(id: Int, icon: Drawable, title: CharSequence) {
    val id: Int
    val icon: Drawable
    val title: CharSequence
    private var mConfig: Config? = null

    /**
     * Create a new Action, which you add to the QuickActionView with [QuickActionView.addAction]
     *
     * @param id    the action's unique id
     * @param icon  the drawable icon to display
     * @param title the title that appears above the Action button
     */
    init {
        require(id != 0) { "Actions must have a non-zero id" }
        this.id = id
        this.icon = icon
        this.title = title
    }

    var config: Config?
        get() = mConfig
        set(config) {
            mConfig = config
        }

    /**
     * Configuration for the [Action] which controls the visuals.
     */
    class Config {
        var backgroundColorStateList: ColorStateList? = null
            private set
        var textColor = 0
            private set
        var iconCustomColor = 0
            private set

        @DrawableRes
        var textBackgroundDrawable = 0

        constructor(context: Context) {
            val colorAccent = context.getThemeAttrColor(androidx.appcompat.R.attr.colorPrimary)
            backgroundColorStateList = ColorStateList.valueOf(colorAccent)
            textBackgroundDrawable = R.drawable.qav_text_background
            textColor = Color.WHITE
            iconCustomColor = R.color.purple_500
        }

        constructor() {}

        fun setBackgroundColorStateList(backgroundColorStateList: ColorStateList?): Config {
            this.backgroundColorStateList = backgroundColorStateList
            return this
        }

        fun setBackgroundColor(@ColorInt backgroundColor: Int): Config {
            backgroundColorStateList = ColorStateList.valueOf(backgroundColor)
            return this
        }

        fun setIconColor(@ColorRes iconColor: Int): Config {
            this.iconCustomColor = iconColor
            return this
        }

        fun getTextBackgroundDrawable(context: Context?): Drawable? {
            return if (textBackgroundDrawable != 0) {
                ContextCompat.getDrawable(context!!, textBackgroundDrawable)
            } else null
        }

        fun setTextBackgroundDrawable(@DrawableRes textBackgroundDrawable: Int): Config {
            this.textBackgroundDrawable = textBackgroundDrawable
            return this
        }

        fun setTextColor(textColor: Int): Config {
            this.textColor = textColor
            return this
        }
    }
}