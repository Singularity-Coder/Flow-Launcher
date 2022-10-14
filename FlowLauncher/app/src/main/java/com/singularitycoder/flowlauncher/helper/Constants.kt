package com.singularitycoder.flowlauncher.helper

import com.singularitycoder.flowlauncher.BuildConfig

const val FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider"
const val REQUEST_CODE_VIDEO = 1001
const val TAG_ADD_CONTACT_MODAL_BOTTOM_SHEET = "TAG_ADD_CONTACT_MODAL_BOTTOM_SHEET"

const val KEY_IS_WORK_COMPLETE = "KEY_IS_WORK_COMPLETE"
const val FIRST_URL = "FIRST_URL"

object Db {
    const val CONTACT = "db_contact"
}

object Table {
    const val CONTACT = "table_contact"
    const val GLANCE = "table_glance"
}

object Broadcast {
    const val TIME_CHANGED = "BROADCAST_TIME_CHANGED"
    const val PACKAGE_REMOVED = "BROADCAST_PACKAGE_REMOVED"
    const val PACKAGE_INSTALLED = "BROADCAST_PACKAGE_ADDED"
}

enum class SpeechAction(val value: String) {
    NONE("none"),
    OPEN("open"),
    LAUNCH("launch"),
    CALL("call"),
    MESSAGE("message"),
    SEARCH("search"),
    FIND("find"),
}