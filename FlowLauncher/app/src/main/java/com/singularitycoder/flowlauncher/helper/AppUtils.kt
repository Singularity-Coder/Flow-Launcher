package com.singularitycoder.flowlauncher.helper

import android.app.Activity
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.singularitycoder.flowlauncher.home.model.App
import java.io.*


// https://stackoverflow.com/questions/10297149/listen-for-app-installed-upgraded-broadcast-message-in-android

/**
 * https://developer.android.com/training/package-visibility
 * Add query activities in manifest for getting full access to all apps
 * queryIntentActivities(), getPackageInfo(), and getInstalledApplications() need this
 * The limited visibility also affects explicit interactions with other apps, such as starting another app's service.
 * To allow your app to see all other installed apps, Android 11 introduces the QUERY_ALL_PACKAGES permission.
 * */
fun Context.appList(): List<App> {
    val intent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val allApps = if (Build.VERSION.SDK_INT > 33) {
        packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
    } else {
        packageManager.queryIntentActivities(intent, 0)
    }
    return allApps.map { item: ResolveInfo? ->
        App().apply {
            title = item?.loadLabel(packageManager).toString()
            packageName = item?.activityInfo?.packageName ?: ""
            icon = item?.activityInfo?.loadIcon(packageManager)
            println("packageName: $packageName")
        }
    }
}

fun Context.appInfoList(): List<ResolveInfo?> {
    val intent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    return if (Build.VERSION.SDK_INT > 33) {
        packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
    } else {
        packageManager.queryIntentActivities(intent, 0)
    }
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

// https://stackoverflow.com/questions/4421527/how-can-i-start-android-application-info-screen-programmatically
fun Context.showInstalledAppDetails(app: App) {
    val SCHEME = "package"
    val intent = Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts(SCHEME, app.packageName, null)
    }
    startActivity(intent)
}

// https://stackoverflow.com/questions/4421527/how-can-i-start-android-application-info-screen-programmatically
fun Context.showAppInfo(app: App) {
    val apiLevel = Build.VERSION.SDK_INT
    try {
        // Open the specific App Info page:
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${app.packageName}")
        }
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Open the generic Apps page:
        val intent = Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
        startActivity(intent)
    }
}

// https://stackoverflow.com/questions/38315805/enable-disable-packages-programmatically
// https://developer.android.com/reference/android/content/pm/PackageManager#setApplicationEnabledSetting(java.lang.String,%20int,%20int)
// https://android.stackexchange.com/questions/143560/how-to-disable-third-party-apps-without-uninstall
fun App.enable(context: Context) = try {
    context.packageManager.setApplicationEnabledSetting(this.packageName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0)
//    val runtime = Runtime.getRuntime() // Execute shell commands
//    runtime.exec("pm disable ${app.packageName}")
} catch (_: Exception) {
}

fun App.disable(context: Context) = try {
    context.packageManager.setApplicationEnabledSetting(this.packageName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0)
} catch (_: Exception) {
}

fun Context.isPackageInstalled(packageName: String): Boolean {
    return try {
        packageManager.getApplicationInfo(packageName, 0).enabled
    } catch (e: NameNotFoundException) {
        false
    }
}

fun Intent.launchAppIfExists(activity: Activity) {
    if (this.resolveActivity(activity.packageManager) != null) {
        val appPackage = this.resolveActivity(activity.packageManager).packageName
        activity.launchApp(appPackage)
    }
}

// https://stackoverflow.com/questions/37984988/how-to-send-apk-using-share-intent-programatically-in-android
fun Context.getApkFileUri(packageName: String): Uri? = try {
    val appInfo = appInfoList().find { it?.activityInfo?.packageName == packageName }
    val fileName = appInfo?.loadLabel(packageManager).toString()
    val file1 = File(appInfo?.activityInfo?.applicationInfo?.publicSourceDir ?: "")
    var file2 = File(Environment.getExternalStorageDirectory().toString() + "/Folder")

    println("file1 name: ${file1.name} ---- ${appInfo?.loadLabel(packageManager)}")
    println("fileName: $fileName")

    file2.mkdirs()
    file2 = File(file2.path + "/" + fileName + ".apk")
    file2.createNewFile()

    val inputStream: InputStream = FileInputStream(file1)
    val out: OutputStream = FileOutputStream(file2)
    // byte[] buf = new byte[1024];
    val buf = ByteArray(4096)
    var len: Int

    while (inputStream.read(buf).also { len = it } > 0) {
        out.write(buf, 0, len)
    }

    inputStream.close()
    out.close()
    println("File copied.")

    Uri.fromFile(File(file2.absolutePath))
} catch (ex: FileNotFoundException) {
    println(ex.message + " in the specified directory.")
    null
} catch (e: IOException) {
    println(e.message)
    null
}

fun Context.shareApk(packageName: String) {
    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "application/vnd.android.package-archive"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(getApkFileUri(packageName) ?: return))
    }
    ContextCompat.startActivity(
        /* context = */ this,
        /* intent = */ Intent.createChooser(intent, "Share files via"),
        /* options = */ null
    )
}

// Problem: android.os.FileUriExposedException: file:///product/app/Drive/Drive.apk exposed beyond app through ClipData.Item.getUri()
// https://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed
fun Context.shareApkOf(app: App) = try {
    val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getApplicationInfo(app.packageName, PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
    } else {
        packageManager.getApplicationInfo(app.packageName, PackageManager.GET_META_DATA)
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "*/*"
        putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + appInfo.publicSourceDir))
        putExtra(Intent.EXTRA_TEXT, "${app.title} APK")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    startActivity(Intent.createChooser(intent, "Share it using"))
} catch (e: Exception) {
    e.printStackTrace()
    showToast("Cannot share!")
}
