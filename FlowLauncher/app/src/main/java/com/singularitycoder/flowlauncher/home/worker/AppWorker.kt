package com.singularitycoder.flowlauncher.home.worker

import android.content.Context
import android.content.pm.ResolveInfo
import android.graphics.drawable.BitmapDrawable
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.singularitycoder.flowlauncher.helper.appInfoList
import com.singularitycoder.flowlauncher.helper.appList
import com.singularitycoder.flowlauncher.helper.constants.KEY_IS_WORK_COMPLETE
import com.singularitycoder.flowlauncher.helper.db.FlowDatabase
import com.singularitycoder.flowlauncher.helper.saveToInternalStorage
import com.singularitycoder.flowlauncher.home.model.App
import com.singularitycoder.flowlauncher.toBitmap
import com.singularitycoder.flowlauncher.toBlueFilter
import com.singularitycoder.flowlauncher.toGrayAndBlueFilteredBitmap
import com.singularitycoder.flowlauncher.toGrayscaleFilter
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException

class AppWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DbEntryPoint {
        fun db(): FlowDatabase
    }

    override suspend fun doWork(): Result {
        return withContext(IO) {
            val appContext = context.applicationContext ?: throw IllegalStateException()
            val dbEntryPoint = EntryPointAccessors.fromApplication(appContext, DbEntryPoint::class.java)
            val dao = dbEntryPoint.db().appDao()

            try {
                context.appInfoList().map { item: ResolveInfo? ->
                    val appIconName = "app_icon_${item?.activityInfo?.packageName}".replace(oldValue = ".", newValue = "_")
                    val appIconDir = "${context.filesDir.absolutePath}/app_icons"
                    val bitmap = item?.activityInfo?.loadIcon(context.packageManager)?.toBitmap()?.toGrayscaleFilter()?.toBlueFilter().apply {
                        this?.saveToInternalStorage(appIconName, appIconDir)
                    }
                    App().apply {
                        title = item?.loadLabel(context.packageManager).toString()
                        packageName = item?.activityInfo?.packageName ?: ""
                        iconPath = "$appIconDir/$appIconName"
                        println("packageName: $packageName")
                    }
                }.also {
                    dao.deleteAll()
                    dao.insertAll(it.sortedBy { it.title })
                }
                Result.success()
            } catch (e: Exception) {
                println("Exception: $e")
                Result.failure()
            }
        }
    }

    private fun sendResult(isWorkComplete: Boolean): Data = Data.Builder()
        .putBoolean(KEY_IS_WORK_COMPLETE, isWorkComplete)
        .build()
}