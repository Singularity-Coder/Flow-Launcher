package com.singularitycoder.flowlauncher.helper

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

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
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(Intent.createChooser(intent, intentTitle ?: "Share to..."))
    }
}

fun Context.openDialer(phoneNumber: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null))
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    }
}

// Needs CALL_PHONE permission
fun Context.makeCall(phoneNum: String) {
    if (isCallPhonePermissionGranted().not()) return
    val intent = Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNum, null))
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    }
}

fun Context.sendSms(phoneNumber: String, body: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("sms:$phoneNumber")
            putExtra("sms_body", body)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    } catch (_: Exception) {
    }
}

fun Context.sendWhatsAppMessage(whatsAppPhoneNum: String) {
    try {
        // checks if such an app exists or not
        packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
        val uri = Uri.parse("smsto:$whatsAppPhoneNum")
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply { setPackage("com.whatsapp") }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(intent, "Dummy Title"))
        }
    } catch (e: PackageManager.NameNotFoundException) {
        Toast.makeText(this, "WhatsApp not found. Install from PlayStore.", Toast.LENGTH_SHORT).show()
        try {
            val uri = Uri.parse("market://details?id=com.whatsapp")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET) }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        } catch (_: Exception) {
        }
    }
}