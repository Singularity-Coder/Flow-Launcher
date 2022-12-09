package com.singularitycoder.flowlauncher.home.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.singularitycoder.flowlauncher.helper.db.FlowDatabase
import com.singularitycoder.flowlauncher.helper.constants.KEY_IS_WORK_COMPLETE
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
            val dao = dbEntryPoint.db().weatherDao()

            try {
                // Convert drawable to bitmap
                // Apply grayscale and blue filters
                // Save the bitmap in internal storage new folder
                // All apps r constantly synced in background - all in one place internal storage
                // Flows
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