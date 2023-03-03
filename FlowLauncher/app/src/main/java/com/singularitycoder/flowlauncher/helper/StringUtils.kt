package com.singularitycoder.flowlauncher.helper

import android.content.Context
import androidx.annotation.RawRes
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


fun String.trimNewLines(): String = this.replace(oldValue = System.getProperty("line.separator") ?: "\n", newValue = " ")

// Works on Windows, Linux and Mac
// https://stackoverflow.com/questions/11048973/replace-new-line-return-with-space-using-regex
// https://javarevisited.blogspot.com/2014/04/how-to-replace-line-breaks-new-lines-windows-mac-linux.html
fun String.trimNewLinesUniversally(): String = this.replace(regex = Regex(pattern = "[\\t\\n\\r]+"), replacement = " ")

fun String.trimIndentsAndNewLines(): String = this.trimIndent().trimNewLinesUniversally()

fun String?.isNullOrBlankOrNaOrNullString(): Boolean {
    return this.isNullOrBlank() || "null" == this.toLowCase().trim() || "na" == this.toLowCase().trim()
}

fun String.toLowCase(): String = this.lowercase(Locale.getDefault())

fun String.toUpCase(): String = this.uppercase(Locale.getDefault())

fun String.capFirstChar(): String = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

fun String.substringBeforeLastIgnoreCase(delimiter: String, missingDelimiterValue: String = this): String {
    val index = toLowCase().lastIndexOf(delimiter.toLowCase())
    return if (index == -1) missingDelimiterValue else substring(0, index)
}

fun String.substringAfterLastIgnoreCase(delimiter: String, missingDelimiterValue: String? = null): String? {
    val index = toLowCase().lastIndexOf(delimiter.toLowCase())
    return if (index == -1) missingDelimiterValue else substring(index + delimiter.length, length)
}

fun String.toYoutubeThumbnailUrl(): String {
    val imageUrl = "https://img.youtube.com/vi/$this/0.jpg"
    println("Image url: $imageUrl")
    return imageUrl
}

// https://stackoverflow.com/questions/19945411/how-can-i-parse-a-local-json-file-from-assets-folder-into-a-listview
suspend fun Context.loadJsonStringFrom(@RawRes rawResource: Int): String? {
    return suspendCoroutine<String?> {
        try {
//        val inputStream: InputStream = assets.open("yourfilename.json")
            val inputStream: InputStream = resources.openRawResource(rawResource)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val jsonString = String(buffer, Charset.forName("UTF-8"))
            it.resume(jsonString)
        } catch (_: IOException) {
            it.resume(null)
        }
    }
}

// https://stackoverflow.com/questions/21544973/convert-jsonobject-to-map
fun JSONObject.toMap(): Map<String, Any> = try {
    val map: MutableMap<String, Any> = HashMap()
    val keys = this.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        var value = this[key]
        if (value is JSONArray) {
            value = value.toList()
        } else if (value is JSONObject) {
            value = value.toMap()
        }
        map[key] = value
    }
    map
} catch (_: Exception) {
    emptyMap<String, Any>()
}

// https://stackoverflow.com/questions/21544973/convert-jsonobject-to-map
fun JSONArray.toList(): List<Any> = try {
    val list: MutableList<Any> = ArrayList()
    for (i in 0 until this.length()) {
        var value = this[i]
        if (value is JSONArray) {
            value = value.toList()
        } else if (value is JSONObject) {
            value = value.toMap()
        }
        list.add(value)
    }
    list
} catch (_: Exception) {
    emptyList<Any>()
}