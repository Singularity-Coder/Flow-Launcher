package com.singularitycoder.flowlauncher.helper

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

object Preferences {
    const val KEY_IS_CONTACTS_SYNCED = "KEY_IS_CONTACTS_SYNCED"

    private const val PREFERENCE_STORAGE_NAME = "flow_launcher_preferences"
    private fun Context.preferences(): SharedPreferences = getSharedPreferences(PREFERENCE_STORAGE_NAME, MODE_PRIVATE)
    fun write(context: Context): SharedPreferences.Editor = context.preferences().edit()
    fun read(context: Context): SharedPreferences = context.preferences()
}