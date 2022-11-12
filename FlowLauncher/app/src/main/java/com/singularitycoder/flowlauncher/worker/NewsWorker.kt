package com.singularitycoder.flowlauncher.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.singularitycoder.flowlauncher.db.FlowDatabase
import com.singularitycoder.flowlauncher.db.NewsDao
import com.singularitycoder.flowlauncher.helper.KEY_IS_WORK_COMPLETE
import com.singularitycoder.flowlauncher.model.News
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

// https://www.youtube.com/watch?v=SWEqYNbURCg
class NewsWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DbEntryPoint {
        fun db(): FlowDatabase
    }

    override suspend fun doWork(): Result {
        return withContext(IO) {
            val appContext = context.applicationContext ?: throw IllegalStateException()
            val dbEntryPoint = EntryPointAccessors.fromApplication(appContext, DbEntryPoint::class.java)
            val dao = dbEntryPoint.db().newsDao()

            try {
                scrapeGoogleForLocalNews(dao)
                Result.success(sendResult(isWorkComplete = true))
            } catch (e: Exception) {
                if (e is HttpStatusException) println("Error status: ${e.statusCode}")
                println("Exception: $e")
                Result.failure()
            }
        }
    }

    // https://jsoup.org/cookbook/extracting-data/selector-syntax
    private suspend fun scrapeGoogleForLocalNews(dao: NewsDao) {
        Jsoup.connect("https://www.google.com/search?q=news").timeout(10_000).get().apply {
            dao.deleteAll()
            val elementList = getElementsByClass("JJZKK Wui1sd")/*.toggleClass("rCXe4d")*/ // This is the list item class and not the entire list class
            println("sizeeee: " + elementList.size)
            for (i in 0..elementList.size) {
                val imageUrl = getElementsByClass("uhHOwf BYbUcd").select("img").eq(i).attr("src")
                val headline = getElementsByClass("mCBkyc tNxQIb ynAwRc nDgy9d").eq(i).text()
                val source = getElementsByClass("CEMjEf NUnG9d").eq(i).text()
                val time = getElementsByClass("OSrXXb ZE0LJd YsWzw").eq(i).text()
                val link = getElementsByClass("WlydOe").eq(i).attr("href")

                println(
                    """
                       imageUrl: $imageUrl 
                       headline: $headline
                       source: $source
                       time: $time
                       link: $link
                    """.trimIndent()
                )

                if (headline.isNullOrBlank()) continue

                dao.insert(
                    News(
                        imageUrl = imageUrl,
                        title = headline,
                        source = source ?: "",
                        time = time ?: "",
                        link = link
                    )
                )
            }
        }
    }

    private suspend fun scrapeGoogleNews(dao: NewsDao) {
        // SCRAPE GOOGLE NEWS - This works once every 5 or 6 trials
        Jsoup.connect("https://news.google.com/topics/CAAqHAgKIhZDQklTQ2pvSWJHOWpZV3hmZGpJb0FBUAE").timeout(10_000).get().apply {
            val elementList = getElementsByClass("IFHyqb DeXSAc").toggleClass("IFHyqb DeXSAc") // This is the list item class and not the entire list class
            println("sizeeee: " + elementList.size)
            for (i in 0..elementList.size) {
                val imageUrl = getElementsByClass("Quavad").attr("src")
                val headline = getElementsByClass("JtKRv").text()
                val source = getElementsByClass("vr1PYe").text()
                val time = getElementsByClass("hvbAAd").text()
                val link = getElementsByClass("WwrzSb").attr("href")

                println(
                    """
                       imageUrl: $imageUrl 
                       headline: $headline
                       source: $source
                       time: $time
                       link: $link
                    """.trimIndent()
                )

                dao.insert(
                    News(
                        imageUrl = imageUrl,
                        title = headline,
                        source = source,
                        time = time,
                        link = try {
                            link.replaceFirst(".", "https://news.google.com") ?: ""
                        } catch (e: Exception) {
                            link
                        }
                    )
                )
            }
        }
    }

    private fun sendResult(isWorkComplete: Boolean): Data = Data.Builder()
        .putBoolean(KEY_IS_WORK_COMPLETE, isWorkComplete)
        .build()
}