package com.singularitycoder.flowlauncher.home.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.singularitycoder.flowlauncher.helper.FlowUtils
import com.singularitycoder.flowlauncher.helper.db.FlowDatabase
import com.singularitycoder.flowlauncher.helper.disable
import com.singularitycoder.flowlauncher.helper.enable
import com.singularitycoder.flowlauncher.home.model.App
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class EnableDisableAppsWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DbEntryPoint {
        fun db(): FlowDatabase
    }

    override suspend fun doWork(): Result = withContext(IO) {
        val appContext = context.applicationContext ?: throw IllegalStateException()
        val dbEntryPoint = EntryPointAccessors.fromApplication(appContext, AppWorker.DbEntryPoint::class.java)
        val dao = dbEntryPoint.db().appFlowDao()
        try {
            val defaultFlowApps = dao.getAppFlowById(id = 1L)?.appList
            FlowUtils.selectedFlow?.appList?.forEach { selectedApp: App ->
                selectedApp.enable(context)
            }
            defaultFlowApps?.forEach { defaultApp: App ->
                val isDefaultAppNotPresentInSelectedApp = FlowUtils.selectedFlow?.appList?.map { it.packageName }?.contains(defaultApp.packageName)?.not() == true
                if (isDefaultAppNotPresentInSelectedApp) {
                    defaultApp.disable(context)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}