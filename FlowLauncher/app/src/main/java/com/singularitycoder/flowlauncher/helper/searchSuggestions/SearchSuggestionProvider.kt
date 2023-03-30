/*
 * Copyright (C) 2020 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.singularitycoder.flowlauncher.helper.searchSuggestions

import android.text.TextUtils
import android.util.Log
import com.singularitycoder.flowlauncher.helper.readStringFromStream
import org.json.JSONArray
import java.io.BufferedInputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// https://github.com/LineageOS/android_packages_apps_Jelly

internal abstract class SearchSuggestionProvider(private val encoding: String) {

    companion object {
        private const val TAG = "SuggestionProvider"
        private const val DEFAULT_LANGUAGE = "en"
        private val INTERVAL_DAY = TimeUnit.DAYS.toSeconds(1)
        private val language: String
            get() {
                var language = Locale.getDefault().language
                if (TextUtils.isEmpty(language)) {
                    language = DEFAULT_LANGUAGE
                }
                return language
            }
    }

    private val deviceLanguage: String = language

    suspend fun fetchSearchSuggestionResultsList(rawQuery: String): List<String> {
        val resultsList: MutableList<String> = ArrayList(/* initialCapacity = */ 5)
        val query: String = try {
            URLEncoder.encode(rawQuery, encoding)
        } catch (e: UnsupportedEncodingException) {
            Log.e(TAG, "Unable to encode the URL", e)
            return resultsList
        }
        val content = downloadSearchSuggestionsForQuery(query, deviceLanguage) ?: return resultsList
        try {
            parseResults(content) { suggestion ->
                resultsList.add(suggestion)
                resultsList.size < 5
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to parse results", e)
        }
        return resultsList
    }

    /**
     * Create a URL for the given query in the given language.
     *
     * @param query    the query that was made.
     * @param language the locale of the user.
     * @return should return a URL that can be fetched using a GET.
     */
    protected abstract fun createQueryUrl(
        query: String,
        language: String
    ): String

    /**
     * Parse the results of an input stream into a list of [String].
     *
     * @param content  the raw input to parse.
     * @param callback the callback to invoke for each received suggestion
     * @throws Exception throw an exception if anything goes wrong.
     */
    open suspend fun parseResults(
        content: String,
        callback: suspend (suggestion: String) -> Unit
    ) {
        val respArray = JSONArray(content)
        val jsonArray = respArray.getJSONArray(1)
        repeat(jsonArray.length()) { position: Int ->
            val suggestion = jsonArray.getString(position)
            callback.invoke(suggestion)
        }
    }

    /**
     * This method downloads the search suggestions for the specific query.
     * NOTE: This is a blocking operation, do not fetchResults on the UI thread.
     *
     * @param query the query to get suggestions for
     * @return the cache file containing the suggestions
     */
    private suspend fun downloadSearchSuggestionsForQuery(
        query: String,
        language: String
    ): String? = suspendCoroutine<String?> { continuation: Continuation<String?> ->
        try {
            val url = URL(createQueryUrl(query, language))
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.addRequestProperty(
                "Cache-Control",
                "max-age=$INTERVAL_DAY, max-stale=$INTERVAL_DAY"
            )
            urlConnection.addRequestProperty("Accept-Charset", encoding)
            try {
                BufferedInputStream(urlConnection.inputStream).use {
                    continuation.resume(readStringFromStream(inputStream = it, encoding = getEncoding(urlConnection)))
                }
            } finally {
                urlConnection.disconnect()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Problem getting search suggestions", e)
        }
    }

    private fun getEncoding(connection: HttpURLConnection): String {
        val contentEncoding = connection.contentEncoding
        if (contentEncoding != null) {
            return contentEncoding
        }
        val contentType = connection.contentType
        for (value in contentType.split(";").toTypedArray().map { str -> str.trim { it <= ' ' } }) {
            if (value.lowercase(Locale.US).startsWith("charset=")) {
                return value.substring(8)
            }
        }
        return encoding
    }

//    internal fun interface ResultCallback {
//        fun addResult(suggestion: String): Boolean
//    }
}