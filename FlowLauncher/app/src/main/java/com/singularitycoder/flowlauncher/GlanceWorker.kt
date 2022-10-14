package com.singularitycoder.flowlauncher

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.singularitycoder.flowlauncher.db.FlowDatabase
import com.singularitycoder.flowlauncher.helper.FIRST_URL
import com.singularitycoder.flowlauncher.helper.KEY_IS_WORK_COMPLETE
import com.singularitycoder.flowlauncher.model.Glance
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class GlanceWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DbEntryPoint {
        fun db(): FlowDatabase
    }

    override suspend fun doWork(): Result {
        return withContext(IO) {
            val appContext = context.applicationContext ?: throw IllegalStateException()
            val dbEntryPoint = EntryPointAccessors.fromApplication(appContext, DbEntryPoint::class.java)
            val dao = dbEntryPoint.db().glanceDao()
            val firstUrl = inputData.getString(FIRST_URL)

            println("WebPageParsingWorker -> Work Started with firstUrl: $firstUrl")

            if (firstUrl.isNullOrBlank()) return@withContext Result.failure()

            try {
                val urlSet1 = ArrayList<String>()
                val urlSet2 = ArrayList<String>()

                // SCRAPE GOOGLE
                Jsoup.connect(firstUrl).timeout(10_000).get().apply {
                    val linkElementsList = select("a[href]")
                    val imageElementsList = select("src")
                    val titleElementsList = select("h1")
                    val descElementsList = select("p")
                    linkElementsList.forEach { it: Element? ->
//                    println("All Links: " + it?.attr("abs:href").toString())
                        if (it?.text()?.contains("https://") == true) {
                            val sanitizedUrl = it.text().toString().substringAfterLast("https://").substringBefore(" ")
                            println("links https: $sanitizedUrl")
                            urlSet1.add("https://$sanitizedUrl")
                        }
                        if (it?.text()?.contains("http://") == true) {
                            val sanitizedUrl = it.text().toString().substringAfterLast("http://").substringBefore(" ")
                            println("links http: $sanitizedUrl")
                            urlSet1.add("http://$sanitizedUrl")
                        }
                    }
                }

                // SCRAPE HOME PAGE OF SITE, IF DETAIL PAGE THEN SCRAPE TEXT
                urlSet1.forEach urlSet1@{ pageUrl: String ->
                    Jsoup.connect(pageUrl).timeout(10_000).get().apply {
                        val linkElementsList2 = select("a[href]")
                        val imageElementsList2 = select("src")
                        val titleElementsList2 = select("h1")
                        val descElementsList2 = select("p")

                        var title = ""
                        var description = ""

                        titleElementsList2.forEach titles@{ it: Element? ->
                            title += it?.text().toString()
                        }

                        if (title.isBlank()) return@urlSet1

                        descElementsList2.forEach { it: Element? ->
                            description += ". " + it?.text().toString()
                        }

                        if (description.replace(".", "").isBlank()) return@urlSet1

                        dao.insert(Glance())
                    }
                }

                // SCRAPE DETAIL PAGE IF NECESSARY

                Result.success(sendResult(isWorkComplete = true))
            } catch (e: Exception) {
                if (e is HttpStatusException) println("Error status: ${e.statusCode}")
                println("Exception: $e")
                Result.failure()
            }
        }
    }

    // java.lang.IllegalStateException: Data cannot occupy more than 10240 bytes when serialized
    // You can't send data with size more than 10240 bytes.
    // Store results in Room DB. Use flow to get each addition realtime
    private fun sendResult(isWorkComplete: Boolean): Data = Data.Builder()
        .putBoolean(KEY_IS_WORK_COMPLETE, isWorkComplete)
        .build()
}