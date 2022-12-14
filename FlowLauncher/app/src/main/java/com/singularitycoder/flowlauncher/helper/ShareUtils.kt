package com.singularitycoder.flowlauncher.helper

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri

// https://stackoverflow.com/questions/33222918/sharing-bitmap-via-android-intent
fun Context.shareImageAndTextViaApps(
    uri: Uri,
    title: String,
    subtitle: String,
    intentTitle: String? = null
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, subtitle)
    }
    startActivity(Intent.createChooser(intent, intentTitle ?: "Share to..."))
}

fun Context.openDialer(phoneNum: String) {
    val callIntent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNum, null))
    callIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
    startActivity(callIntent)
}

// Needs CALL_PHONE permission
fun Context.makeCall(phoneNum: String) {
    val callIntent = Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNum, null))
    callIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
    startActivity(callIntent)
}

fun Context.sendSms(phoneNum: String, body: String) = try {
    val smsIntent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("sms:$phoneNum")
        putExtra("sms_body", body)
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
    }
    startActivity(smsIntent)
} catch (_: Exception) {
}

fun Context.sendWhatsAppMessage(whatsAppPhoneNum: String) {
    try {
        // checks if such an app exists or not
        packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
        val uri = Uri.parse("smsto:$whatsAppPhoneNum")
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply { setPackage("com.whatsapp") }
        startActivity(Intent.createChooser(intent, "Dummy Title"))
    } catch (e: PackageManager.NameNotFoundException) {
        Toast.makeText(this, "WhatsApp not found. Install from PlayStore.", Toast.LENGTH_SHORT).show()
        try {
            val uri = Uri.parse("market://details?id=com.whatsapp")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET) }
            startActivity(intent)
        } catch (_: Exception) {
        }
    }
}

// https://stackoverflow.com/questions/12013416/is-there-any-way-in-android-to-force-open-a-link-to-open-in-chrome
fun Activity.searchWithChrome(query: String) {
    val sanitizedQuery = query.replaceFirst("for", "").trim().replace(" ", "+")
    val url = "https://www.google.com/search?q=$sanitizedQuery"
    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        setPackage("com.android.chrome")
    }
    try {
        startActivity(intent)
    } catch (ex: ActivityNotFoundException) {
        // If Chrome not installed
        intent.setPackage(null)
        startActivity(intent)
    }
}

fun Activity.openWithChrome(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        setPackage("com.android.chrome")
    }
    try {
        startActivity(intent)
    } catch (ex: ActivityNotFoundException) {
        // If Chrome not installed
        intent.setPackage(null)
        startActivity(intent)
    }
}