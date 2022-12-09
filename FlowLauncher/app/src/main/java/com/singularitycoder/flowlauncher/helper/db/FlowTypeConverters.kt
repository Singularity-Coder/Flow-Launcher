package com.singularitycoder.flowlauncher.helper.db

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.singularitycoder.flowlauncher.helper.FlowUtils
import com.singularitycoder.flowlauncher.today.model.Remainder
import java.lang.reflect.Type

// Type converters must not contain any arguments in the constructor
// Classes that are used as TypeConverters must have no-argument public constructors.
// Use a ProvidedTypeConverter annotation if you need to take control over creating an instance of a TypeConverter.

// https://stackoverflow.com/questions/46585075/android-how-to-make-type-converters-for-room-generic-for-all-list-of-objects
abstract class FlowTypeConverter<T> {
    private val type: Type = object : TypeToken<List<T>>() {}.type

    @TypeConverter
    fun listToString(value: List<T>): String = FlowUtils.gson.toJson(value, type)

    @TypeConverter
    fun stringToList(value: String): List<T> = FlowUtils.gson.fromJson(value, type)
}

class StringListConverter : FlowTypeConverter<String>()
class IntListConverter : FlowTypeConverter<Int>()
class RemainderListConverter : FlowTypeConverter<Remainder>()
