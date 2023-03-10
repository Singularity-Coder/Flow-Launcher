package com.singularitycoder.flowlauncher.helper

import android.util.TypedValue
import com.google.gson.Gson
import com.singularitycoder.flowlauncher.home.model.App
import com.singularitycoder.flowlauncher.home.model.Contact
import com.singularitycoder.flowlauncher.home.model.Sms

object FlowUtils {
    var appList = emptyList<App>()
    var contactsList = emptyList<Contact>()
    var smsList = emptyList<Sms>()
    var androidSettingsMap: Map<String, String> = HashMap()
    var sanskritVocabMap: Map<String, String> = HashMap()
    var englishVocabMap: Map<String, String> = HashMap()

    val gson = Gson()
    val typedValue = TypedValue()
}