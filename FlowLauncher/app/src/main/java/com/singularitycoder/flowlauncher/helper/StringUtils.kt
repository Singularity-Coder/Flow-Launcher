package com.singularitycoder.flowlauncher.helper

import java.util.*

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