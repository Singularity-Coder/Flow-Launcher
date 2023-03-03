package com.singularitycoder.flowlauncher.helper

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

object Preferences {
    const val KEY_IS_CONTACTS_SYNCED = "KEY_IS_CONTACTS_SYNCED"
    const val KEY_IS_HOLIDAYS_AVAILABLE = "KEY_IS_HOLIDAYS_AVAILABLE"
    const val KEY_LAST_HOLIDAYS_FETCH_TIME = "KEY_LAST_HOLIDAYS_FETCH_TIME"
    const val KEY_SELECTED_FLOW_POSITION = "KEY_SELECTED_FLOW_POSITION"

    private const val PREFERENCE_STORAGE_NAME = "flow_launcher_preferences"
    private fun Context.preferences(): SharedPreferences = getSharedPreferences(PREFERENCE_STORAGE_NAME, MODE_PRIVATE)
    fun write(context: Context): SharedPreferences.Editor = context.preferences().edit()
    fun read(context: Context): SharedPreferences = context.preferences()
}