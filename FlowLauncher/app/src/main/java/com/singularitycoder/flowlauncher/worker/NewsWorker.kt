package com.singularitycoder.flowlauncher.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.singularitycoder.flowlauncher.db.FlowDatabase
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
                // SCRAPE GOOGLE NEWS
                Jsoup.connect("https://news.google.com/topics/CAAqHAgKIhZDQklTQ2pvSWJHOWpZV3hmZGpJb0FBUAE").timeout(10_000).get().apply {
                    val elementList = getElementsByClass("WwrzSb")
                    elementList.forEach { it: Element? ->
                        println(it?.allElements)
                        val imageUrl = it?.getElementsByClass("msvBD zC7z7b")?.text()
                        val headline = it?.getElementsByClass("JtKRv")?.text()
                        val source = it?.getElementsByClass("vr1PYe")?.text()
                        val time = it?.getElementsByClass("hvbAAd")?.text()
                        val link = it?.getElementsByClass("hvbAAd")?.text()

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
                                imageUrl = imageUrl ?: "",
                                title = headline ?: "",
                                source = source ?: "",
                                time = time ?: "",
                                link = link ?: ""
                            )
                        )
                    }
                }

                Result.success(sendResult(isWorkComplete = true))
            } catch (e: Exception) {
                if (e is HttpStatusException) println("Error status: ${e.statusCode}")
                println("Exception: $e")
                Result.failure()
            }
        }
    }

    private fun sendResult(isWorkComplete: Boolean): Data = Data.Builder()
        .putBoolean(KEY_IS_WORK_COMPLETE, isWorkComplete)
        .build()
}