package com.singularitycoder.flowlauncher.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.singularitycoder.flowlauncher.db.FlowDatabase
import com.singularitycoder.flowlauncher.db.HolidayDao
import com.singularitycoder.flowlauncher.helper.KEY_IS_WORK_COMPLETE
import com.singularitycoder.flowlauncher.helper.Preferences
import com.singularitycoder.flowlauncher.helper.timeNow
import com.singularitycoder.flowlauncher.model.Holiday
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup

// Public Holidays are very very very imp. Its an overview of a country or a region's core interests
class PublicHolidaysWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DbEntryPoint {
        fun db(): FlowDatabase
    }

    override suspend fun doWork(): Result {
        return withContext(IO) {
            val appContext = context.applicationContext ?: throw IllegalStateException()
            val dbEntryPoint = EntryPointAccessors.fromApplication(appContext, DbEntryPoint::class.java)
            val dao = dbEntryPoint.db().holidayDao()

            try {
                scrapeGoogleForPublicHolidays(dao, context)
                Result.success(sendResult(isWorkComplete = true))
            } catch (e: Exception) {
                if (e is HttpStatusException) println("Error status: ${e.statusCode}")
                println("Exception: $e")
                Result.failure()
            }
        }
    }

    // https://jsoup.org/cookbook/extracting-data/selector-syntax
    private suspend fun scrapeGoogleForPublicHolidays(dao: HolidayDao, context: Context) {
        Jsoup.connect("https://www.google.com/search?q=public+holidays").timeout(10_000).get().apply {
            dao.deleteAll()
            val elementList = getElementsByClass("ct5Ked klitem-tr PZPZlf") // This is the list item class and not the entire list class
            println("sizeeee: " + elementList.size)
            for (i in 0..elementList.size) {
                val imageUrl = getElementsByClass("xflDWd OP21Sc").select("img").eq(i).attr("src")
                val title = getElementsByClass("bVj5Zb FozYP").eq(i).text()
                val date = getElementsByClass("TCYkdd FozYP").eq(i).text()
                val link = getElementsByClass("ct5Ked klitem-tr PZPZlf").eq(i).attr("href")
                val header = getElementsByClass("Wkr6U z4P7Tc").eq(i).text()
                val location = getElementsByClass("Wkr6U z4P7Tc").eq(i).text()

                println(
                    """
                       imageUrl: $imageUrl 
                       title: $title
                       date: $date
                       link: $link
                       header: $header
                       location: $location
                    """.trimIndent()
                )

                if (title.isNullOrBlank()) continue

                dao.insert(
                    Holiday(
                        imageUrl = imageUrl,
                        title = title,
                        date = date,
                        link = link,
                        location = location,
                        header = header,
                    )
                )

                Preferences.write(context).putBoolean(Preferences.KEY_IS_HOLIDAYS_AVAILABLE, true).apply()
                Preferences.write(context).putLong(Preferences.KEY_LAST_HOLIDAYS_FETCH_TIME, timeNow).apply()
            }
        }
    }

    private fun sendResult(isWorkComplete: Boolean): Data = Data.Builder()
        .putBoolean(KEY_IS_WORK_COMPLETE, isWorkComplete)
        .build()
}