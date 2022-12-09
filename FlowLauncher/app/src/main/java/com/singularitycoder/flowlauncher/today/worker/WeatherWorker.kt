package com.singularitycoder.flowlauncher.today.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.singularitycoder.flowlauncher.helper.db.FlowDatabase
import com.singularitycoder.flowlauncher.helper.constants.KEY_IS_WORK_COMPLETE
import com.singularitycoder.flowlauncher.today.model.Weather
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup

// https://www.youtube.com/watch?v=SWEqYNbURCg
class WeatherWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DbEntryPoint {
        fun db(): FlowDatabase
    }

    override suspend fun doWork(): Result {
        return withContext(IO) {
            val appContext = context.applicationContext ?: throw IllegalStateException()
            val dbEntryPoint = EntryPointAccessors.fromApplication(appContext, DbEntryPoint::class.java)
            val dao = dbEntryPoint.db().weatherDao()

            try {
                // SCRAPE GOOGLE FOR WEATHER
                Jsoup.connect("https://www.google.com/search?q=weather").timeout(10_000).get().apply {
                    val temperature = getElementsByClass("wob_t q8U8x").text()
                    val condition = getElementsByClass("wob_dcp").text()
                    val imageUrl = getElementsByClass("wob_tci").attr("src")
                    val location = getElementsByClass("wob_loc q8U8x").text()
                    val dateTime = getElementsByClass("wob_dts").text()

                    println(
                        """
                            temperature: $temperature 
                            condition: $condition
                            imageUrl: $imageUrl
                            location: $location
                            dateTime: $dateTime
                         """.trimIndent()
                    )

                    dao.insert(
                        Weather(
                            temperature = temperature,
                            condition = condition,
                            imageUrl = "https:$imageUrl",
                            location = location,
                            dateTime = dateTime,
                        )
                    )
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