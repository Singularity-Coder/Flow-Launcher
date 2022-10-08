package com.singularitycoder.flowlauncher

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.singularitycoder.flowlauncher.model.App
import java.io.IOException


/**
 * https://developer.android.com/training/package-visibility
 * Add query activities in manifest for getting full access to all apps
 * queryIntentActivities(), getPackageInfo(), and getInstalledApplications() need this
 * The limited visibility also affects explicit interactions with other apps, such as starting another app's service.
 * To allow your app to see all other installed apps, Android 11 introduces the QUERY_ALL_PACKAGES permission.
 * */
fun Context.appList(): List<App> {
    val appList = mutableListOf<App>()
    val intent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val allApps = if (Build.VERSION.SDK_INT > 33) {
        packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
    } else {
        packageManager.queryIntentActivities(intent, 0)
    }
    for (item in allApps) {
        val app = App().apply {
            title = item.loadLabel(packageManager).toString()
            packageName = item.activityInfo.packageName
            icon = item.activityInfo.loadIcon(packageManager)
            println("packageName: $packageName")
        }
        appList.add(app)
    }
    return appList
}

fun Activity.launchApp(packageName: String) {
    val intent = packageManager.getLaunchIntentForPackage(packageName)
    startActivity(intent)
}

fun Activity.installApp(packageName: String) {

}

// https://stackoverflow.com/questions/6813322/install-uninstall-apks-programmatically-packagemanager-vs-intents
fun Activity.uninstallApp(packageName: String) {
    val intent = Intent(Intent.ACTION_DELETE).apply {
        data = Uri.parse("package:$packageName")
    }
    startActivity(intent)
}

// https://www.codegrepper.com/code-examples/shell/how+to+uninstall+app+from+android+phone+programmatically
fun Activity.uninstallApp2(packageName: String) {
    val packageInstaller = packageManager.packageInstaller
    val params = SessionParams(SessionParams.MODE_FULL_INSTALL).apply {
        setAppPackageName(packageName)
    }
    val sessionId = try {
        packageInstaller.createSession(params)
    } catch (e: IOException) {
        println(e.message)
        0
    }
    packageInstaller.uninstall(
        packageName,
        PendingIntent.getBroadcast(
            /* context = */ this,
            /* requestCode = */ sessionId,
            /* intent = */ Intent("android.intent.action.MAIN"),
            /* flags = */ 0
        ).intentSender
    )
}
