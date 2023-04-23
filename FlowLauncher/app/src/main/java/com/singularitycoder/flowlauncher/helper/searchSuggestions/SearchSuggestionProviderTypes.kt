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

import org.json.JSONArray

// https://github.com/LineageOS/android_packages_apps_Jelly

internal class GoogleSearchSuggestionProvider : SearchSuggestionProvider("UTF-8") {
    override fun createQueryUrl(
        query: String,
        language: String
    ): String = "https://www.google.com/complete/search?client=android&oe=utf8&ie=utf8&hl=$language&q=$query"
}

internal class BingSearchSuggestionProvider : SearchSuggestionProvider("UTF-8") {
    override fun createQueryUrl(
        query: String,
        language: String
    ): String = "https://api.bing.com/osjson.aspx?query=$query&language=$language"
}

internal class DuckSearchSuggestionProvider : SearchSuggestionProvider("UTF-8") {
    override fun createQueryUrl(
        query: String,
        language: String
    ): String = "https://duckduckgo.com/ac/?q=$query"

    override suspend fun parseResults(
        content: String,
        callback: suspend (suggestion: String) -> Unit
    ) {
        val jsonArray = JSONArray(content)
        repeat(jsonArray.length()) { position: Int ->
            val obj = jsonArray.getJSONObject(position)
            val suggestion = obj.getString("phrase")
            callback.invoke(suggestion)
        }
    }
}

internal class YahooSearchSuggestionProvider : SearchSuggestionProvider("UTF-8") {
    override fun createQueryUrl(
        query: String,
        language: String
    ): String = "https://search.yahoo.com/sugg/chrome?output=fxjson&command=$query"
}