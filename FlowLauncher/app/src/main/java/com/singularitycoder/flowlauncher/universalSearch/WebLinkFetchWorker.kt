package com.singularitycoder.flowlauncher.universalSearch

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.singularitycoder.flowlauncher.helper.constants.WorkerData
import com.singularitycoder.flowlauncher.helper.seconds
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class WebLinkFetchWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(IO) {
        val firstUrl = inputData.getString(WorkerData.URL)
        if (firstUrl.isNullOrBlank()) return@withContext Result.failure()
        try {
            val linksList = mutableListOf<String>()
            Jsoup.connect(firstUrl).timeout(10.seconds().toInt()).get().apply {
                val linkElementsList = select("a[href]")
                linkElementsList.forEach { element: Element? ->
                    if (element?.text()?.contains("https://") == true || element?.text()?.contains("http://") == true) {
                        val sanitizedUrl = element.text().toString()
                            .substringAfterLast("https://")
                            .substringAfterLast("http://")
//                            .substringBefore(" ")
                            .replace("www.", "")
                            .trim()
                        println("links https: $sanitizedUrl")
                        if (linksList.size < 4) linksList.add(sanitizedUrl)
                    }
                }
            }

            Result.success(sendResult(linksList.toTypedArray()))
        } catch (e: Exception) {
            Result.failure()
        }
    }

    // java.lang.IllegalStateException: Data cannot occupy more than 10240 bytes when serialized
    // You can't send data with size more than 10240 bytes.
    // Store results in Room DB. Use flow to get each addition realtime
    private fun sendResult(urlLinksList: Array<String>): Data = Data.Builder()
        .putStringArray(WorkerData.URL_LINKS_LIST, urlLinksList)
        .build()
}