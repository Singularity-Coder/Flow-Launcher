package com.singularitycoder.flowlauncher.helper

object Db {
    const val CONTACT = "db_contact"
}

object Table {
    const val CONTACT = "table_contact"
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