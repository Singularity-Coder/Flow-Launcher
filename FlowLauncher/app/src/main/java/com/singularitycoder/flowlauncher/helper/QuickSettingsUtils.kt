package com.singularitycoder.flowlauncher.helper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat


// https://stackoverflow.com/questions/18312609/change-the-system-brightness-programmatically#:~:text=LayoutParams%20lp%20%3D%20window.,setAttributes(lp)%3B
// https://stackoverflow.com/questions/32083410/cant-get-write-settings-permission
fun Context.isWriteSettingsPermissionGranted(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (Settings.System.canWrite(this).not()) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
            false
        } else true
    } else true
}

// https://stackoverflow.com/questions/18312609/change-the-system-brightness-programmatically#:~:text=LayoutParams%20lp%20%3D%20window.,setAttributes(lp)%3B
fun normalizedBrightness(
    brightness: Float,
    inMin: Float,
    inMax: Float,
    outMin: Float,
    outMax: Float
): Float {
    val outRange = outMax - outMin
    val inRange = inMax - inMin
    return (brightness - inMin) * outRange / inRange + outMin
}

// https://stackoverflow.com/questions/18312609/change-the-system-brightness-programmatically#:~:text=LayoutParams%20lp%20%3D%20window.,setAttributes(lp)%3B
fun Activity.setScreenBrightnessTo(value: Int) {
    Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, value)
    val layoutParams = window.attributes
    layoutParams.screenBrightness = value / 255f
    window.attributes = layoutParams
}

// https://stackoverflow.com/questions/18312609/change-the-system-brightness-programmatically#:~:text=LayoutParams%20lp%20%3D%20window.,setAttributes(lp)%3B
fun Context.getScreenBrightness(): Int {
    return Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
}

// https://stackoverflow.com/questions/18312609/change-the-system-brightness-programmatically#:~:text=LayoutParams%20lp%20%3D%20window.,setAttributes(lp)%3B
fun Context.setScreenBrightnessToAuto() {
    Settings.System.putInt(
        contentResolver,
        Settings.System.SCREEN_BRIGHTNESS_MODE,
        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
    )
}

// https://stackoverflow.com/questions/40925722/how-to-increase-and-decrease-the-volume-programmatically-in-android
fun AudioManager.raiseVolume() {
    adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND)
}

// https://stackoverflow.com/questions/40925722/how-to-increase-and-decrease-the-volume-programmatically-in-android
fun AudioManager.lowerVolume() {
    adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND)
}

// https://stackoverflow.com/questions/19517417/opening-android-settings-programmatically
fun Context.openSettings() {
    startActivity(Intent(Settings.ACTION_SETTINGS))
}

fun Context.isCameraPermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}

fun Context.isAirplaneModeEnabled(): Boolean {
    return Settings.Global.getInt(
        contentResolver,
        Settings.Global.AIRPLANE_MODE_ON, 0) != 0
}

// https://stackoverflow.com/questions/5533881/toggle-airplane-mode-in-android
// https://dustinbreese.blogspot.com/2009/04/andoid-controlling-airplane-mode.html
fun Context.setAirplaneMode(isEnabled: Boolean) {
    try {
        Settings.Global.putInt(
            contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, if (isEnabled) 1 else 0)
        // Post an intent to reload
        val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
            putExtra("state", !isEnabled)
        }
        sendBroadcast(intent)
    } catch (_: Exception) {
    }
}