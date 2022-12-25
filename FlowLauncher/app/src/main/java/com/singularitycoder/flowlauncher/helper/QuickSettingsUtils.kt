package com.singularitycoder.flowlauncher.helper

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import java.io.DataOutputStream
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.Method


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

fun Context.isPhoneStatePermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
}

fun Context.isAirplaneModeEnabled(): Boolean {
    return Settings.Global.getInt(
        contentResolver,
        Settings.Global.AIRPLANE_MODE_ON, 0
    ) != 0
}

// https://stackoverflow.com/questions/5533881/toggle-airplane-mode-in-android
// https://dustinbreese.blogspot.com/2009/04/andoid-controlling-airplane-mode.html
fun Context.setAirplaneMode(isEnabled: Boolean) {
    try {
        Settings.Global.putInt(
            contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, if (isEnabled) 1 else 0
        )
        // Post an intent to reload
        val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
            putExtra("state", !isEnabled)
        }
        sendBroadcast(intent)
    } catch (_: Exception) {
    }
}

// https://stackoverflow.com/questions/26539445/the-setmobiledataenabled-method-is-no-longer-callable-as-of-android-l-and-later
fun Context.setMobileNetwork() {
    try {
        // Get the current state of the mobile network.
        val state = if (isMobileDataEnabledFromLollipop()) 0 else 1
        // Get the value of the "TRANSACTION_setDataEnabled" field.
        val transactionCode = getTransactionCode()
        when {
            Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP -> {
                val mSubscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                // Loop through the subscription list i.e. SIM list.
                for (i in 0 until mSubscriptionManager.activeSubscriptionInfoCountMax) {
                    if (transactionCode != null && transactionCode.isNotBlank()) {
                        // Get the active subscription ID for a given SIM card.
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                            return
                        }
                        val subscriptionId = mSubscriptionManager.activeSubscriptionInfoList[i].subscriptionId
                        // Execute the command via `su` to turn off
                        // mobile network for a subscription service.
                        val command = "service call phone $transactionCode i32 $subscriptionId i32 $state"
                        executeCommandViaSu(option = "-c", command = command)
                    }
                }
            }
            Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP -> {
                if (transactionCode != null && transactionCode.isNotBlank()) {
                    // Execute the command via `su` to turn off mobile network.
                    val command = "service call phone $transactionCode i32 $state"
                    executeCommandViaSu(option = "-c", command = command)
                }
            }
        }
    } catch (_: Exception) {
    }
}

// https://stackoverflow.com/questions/26539445/the-setmobiledataenabled-method-is-no-longer-callable-as-of-android-l-and-later
private fun Context.isMobileDataEnabledFromLollipop(): Boolean {
    return Settings.Global.getInt(contentResolver, "mobile_data", 0) == 1
}

// https://stackoverflow.com/questions/26539445/the-setmobiledataenabled-method-is-no-longer-callable-as-of-android-l-and-later
private fun Context.getTransactionCode(): String? = try {
    val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val telephonyClass = Class.forName(telephonyManager.javaClass.name)
    val telephonyMethod: Method = telephonyClass.getDeclaredMethod("getITelephony").apply {
        isAccessible = true
    }
    val telephonyStub: Any? = telephonyMethod.invoke(telephonyManager)
    val telephonyStubClass = Class.forName(telephonyStub?.javaClass?.name ?: "")
    val declaringClass = telephonyStubClass.declaringClass
    val field: Field = declaringClass.getDeclaredField("TRANSACTION_setDataEnabled").apply {
        isAccessible = true
    }
    java.lang.String.valueOf(field.getInt(null))
} catch (_: Exception) {
    // The "TRANSACTION_setDataEnabled" field is not available,
    // or named differently in the current API level, so we throw
    // an exception and inform users that the method is not available.
    null
}

// https://stackoverflow.com/questions/26539445/the-setmobiledataenabled-method-is-no-longer-callable-as-of-android-l-and-later
private fun executeCommandViaSu(option: String, command: String) {
    var success = false
    var su = "su"
    for (i in 0..2) {
        // Default "su" command executed successfully, then quit.
        if (success) break
        // Else, execute other "su" commands.
        if (i == 1) {
            su = "/system/xbin/su"
        } else if (i == 2) {
            su = "/system/bin/su"
        }
        try {
            // Execute command as "su".
            Runtime.getRuntime().exec(arrayOf(su, option, command))
        } catch (e: IOException) {
            success = false
            // Oops! Cannot execute `su` for some reason. Log error here.
        } finally {
            success = true
        }
    }
}

// https://stackoverflow.com/questions/26539445/the-setmobiledataenabled-method-is-no-longer-callable-as-of-android-l-and-later
fun Context.setMobileDataStateTo(isEnabled: Boolean) = try {
    val telephonyService = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val setMobileDataEnabledMethod = telephonyService.javaClass.getDeclaredMethod("setDataEnabled", Boolean::class.javaPrimitiveType).apply {
        invoke(telephonyService, isEnabled)
    }
} catch (_: Exception) {
}

// https://stackoverflow.com/questions/26539445/the-setmobiledataenabled-method-is-no-longer-callable-as-of-android-l-and-later
fun Context.getMobileDataState(): Boolean = try {
    val telephonyService = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val getMobileDataEnabledMethod = telephonyService.javaClass.getDeclaredMethod("getDataEnabled")
    getMobileDataEnabledMethod.invoke(telephonyService) as Boolean
} catch (_: Exception) {
    false
}

// https://stackoverflow.com/questions/31120082/latest-update-on-enabling-and-disabling-mobile-data-programmatically
fun Context.showNetworkSettings() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val intent = Intent(Settings.ACTION_DATA_USAGE_SETTINGS)
        startActivity(intent)
    } else {
        val intent = Intent().apply {
            component = ComponentName(
                "com.android.settings",
                "com.android.settings.Settings\$DataUsageSummaryActivity"
            )
        }
        startActivity(intent)
    }
}

// https://stackoverflow.com/questions/31120082/latest-update-on-enabling-and-disabling-mobile-data-programmatically
private fun enableMobileData(isEnabled: Boolean) = try {
    val cmds = if (isEnabled) {
        arrayOf("svc data enable")
    } else {
        arrayOf("svc data disable")
    }
    val p = Runtime.getRuntime().exec("su")
    val os = DataOutputStream(p.outputStream)
    for (tmpCmd in cmds) {
        os.writeBytes("$tmpCmd\n")
    }
    os.writeBytes("exit\n")
    os.flush()
} catch (e: java.lang.Exception) {
    e.printStackTrace()
}

// https://stackoverflow.com/questions/31120082/latest-update-on-enabling-and-disabling-mobile-data-programmatically
fun Context.setMobileDataState(isEnabled: Boolean) {
    try {
        val dataManager: ConnectivityManager? = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val dataMtd = ConnectivityManager::class.java.getDeclaredMethod("setMobileDataEnabled", Boolean::class.javaPrimitiveType).apply {
            isAccessible = true
            invoke(dataManager, isEnabled)
        }
    } catch (_: Exception) {
    }
}
