package com.singularitycoder.flowlauncher.universalSearch.worker

import android.app.usage.UsageStats
import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.singularitycoder.flowlauncher.*
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.db.FlowDatabase
import com.singularitycoder.flowlauncher.home.model.App
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject

class UniversalSearchWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DbEntryPoint {
        fun db(): FlowDatabase
    }

    override suspend fun doWork(): Result = withContext(IO) {
        val appContext = context.applicationContext ?: throw IllegalStateException()
        val dbEntryPoint = EntryPointAccessors.fromApplication(appContext, DbEntryPoint::class.java)
        val dao = dbEntryPoint.db().appDao()

        coroutineScope {
            val recentAppsList = mutableListOf<App>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                dao.getAll().forEach { app: App ->
                    context.getRecentAppsWithUsageStats().forEach { usageStats: UsageStats ->
                        if (app.packageName == usageStats.packageName) {
                            recentAppsList.add(app)
                        }
                    }
                }
            }
            FlowUtils.recentAppList = recentAppsList
        }

        coroutineScope {
            FlowUtils.appList = dao.getAll()
        }

        coroutineScope {
            val sanskritWordsJsonString = context.loadJsonStringFrom(rawResource = R.raw.sanskrit_dictionary)
            FlowUtils.sanskritVocabMap = (JSONObject(sanskritWordsJsonString ?: "").toMap() as? Map<String, String>) ?: emptyMap()
        }

        coroutineScope {
            val englishWordsJsonString = context.loadJsonStringFrom(rawResource = R.raw.websters_english_dictionary)
            FlowUtils.englishVocabMap = (JSONObject(englishWordsJsonString ?: "").toMap() as? Map<String, String>) ?: emptyMap()
        }

        coroutineScope {
            val androidSettingsJsonString = context.loadJsonStringFrom(rawResource = R.raw.android_settings)
            FlowUtils.androidSettingsMap = (JSONObject(androidSettingsJsonString ?: "").toMap() as? Map<String, String>) ?: emptyMap()
        }

        coroutineScope {
            FlowUtils.contactsList = context.getContactsList()
        }

        coroutineScope {
            FlowUtils.smsList = context.getSmsList()
        }

        Result.success()
    }
}