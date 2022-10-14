package com.singularitycoder.flowlauncher.db

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.singularitycoder.flowlauncher.helper.FlowUtils
import com.singularitycoder.flowlauncher.model.Remainder
import java.lang.reflect.Type

// Type converters must not contain any arguments in the constructor
// Classes that are used as TypeConverters must have no-argument public constructors.
// Use a ProvidedTypeConverter annotation if you need to take control over creating an instance of a TypeConverter.

class ImagePathListConverter {
    private val type: Type = object : TypeToken<List<String>?>() {}.type

    @TypeConverter
    fun listToString(list: List<String>?): String? {
        list ?: return null
        return FlowUtils.gson.toJson(list, type)
    }

    @TypeConverter
    fun stringToList(string: String?): List<String>? {
        string ?: return null
        return FlowUtils.gson.fromJson(string, type)
    }
}

class YoutubeVideoIdListConverter {
    private val type: Type = object : TypeToken<List<Int>?>() {}.type

    @TypeConverter
    fun listToString(list: List<Int>?): String? {
        list ?: return null
        return FlowUtils.gson.toJson(list, type)
    }

    @TypeConverter
    fun stringToList(string: String?): List<Int>? {
        string ?: return null
        return FlowUtils.gson.fromJson(string, type)
    }
}

class RemainderListConverter {
    private val type: Type = object : TypeToken<List<Remainder>?>() {}.type

    @TypeConverter
    fun listToString(list: List<Remainder>?): String? {
        list ?: return null
        return FlowUtils.gson.toJson(list, type)
    }

    @TypeConverter
    fun stringToList(string: String?): List<Remainder>? {
        string ?: return null
        return FlowUtils.gson.fromJson(string, type)
    }
}
