package com.singularitycoder.flowlauncher.helper

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.location.GnssStatus
import android.location.LocationManager
import android.media.AudioManager
import android.net.*
import android.net.ConnectivityManager.NetworkCallback
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.singularitycoder.flowlauncher.BuildConfig
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.helper.constants.IntentAction
import com.singularitycoder.flowlauncher.helper.constants.IntentExtra
import com.singularitycoder.flowlauncher.helper.services.NotificationFetchService
import com.singularitycoder.flowlauncher.helper.services.PowerMenuService
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
fun Context.openSettings(screen: String?) = try {
    startActivity(Intent(screen))
} catch (_: Exception) {
    showToast(message = "Couldn't find it!", duration = Toast.LENGTH_SHORT)
}

fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Context.isBluetoothPermissionGranted(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
    }
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

// https://stackoverflow.com/questions/10311834/how-to-check-if-location-services-are-enabled#:~:text=%40lenik%2C%20some%20devices%20provide%20a,if%20specific%20providers%20are%20enabled.
// https://developer.android.com/reference/android/provider/Settings.Secure#LOCATION_PROVIDERS_ALLOWED
fun Context.isLocationToggleEnabled(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val isProvidersListEmpty = locationManager.getProviders(true).isEmpty()
        locationManager.isLocationEnabled
    } else {
        val locationProviders: String = Settings.Secure.getString(contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
        locationProviders.isNotBlank()
    }
}

fun Context.setLocationToggleListener(
    onStarted: () -> Unit,
    onStopped: () -> Unit,
) {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        locationManager.registerGnssStatusCallback(object : GnssStatus.Callback() {
            override fun onStopped() {
                super.onStopped()
                println("location toggle: stopped")
                onStopped.invoke()
            }

            override fun onStarted() {
                super.onStarted()
                println("location toggle: started")
                onStarted.invoke()
            }

            override fun onSatelliteStatusChanged(status: GnssStatus) {
                super.onSatelliteStatusChanged(status)
                println("location toggle: ${status.satelliteCount}")
            }
        }, null)
    }
}

fun Context.enableAirplaneMode() {
    if (isWriteSettingsPermissionGranted()) {
        setAirplaneMode(isAirplaneModeEnabled().not())
    }
}

// https://www.tabnine.com/code/java/classes/android.net.wifi.WifiManager
// https://stackoverflow.com/questions/6394599/android-turn-on-off-wifi-hotspot-programmatically
fun AppCompatActivity.enableWifiHotspot(
    wifiConfig: WifiConfiguration?,
    isEnabled: Boolean
): Boolean = try {
    val wifiManager: WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val wifiInfo = wifiManager.connectionInfo
    val ssid: String? = wifiInfo.ssid
    val networkInfo: NetworkInfo? = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)
    if (isEnabled) {
        // disables wifi hotspot if it's already enabled
        wifiManager.isWifiEnabled = false
    }
    val method = wifiManager.javaClass.getMethod(
        /* name = */ "setWifiApEnabled",
        /* ...parameterTypes = */ WifiConfiguration::class.java, Boolean::class.javaPrimitiveType
    )
    method.invoke(wifiManager, wifiConfig, isEnabled) as Boolean
} catch (_: Exception) {
    false
}

// https://stackoverflow.com/questions/6394599/android-turn-on-off-wifi-hotspot-programmatically
// https://stackoverflow.com/questions/34355580/android-6-0-1-couldnt-enable-wifi-hotspot-programmatically/35504709#35504709
// https://stackoverflow.com/questions/12401108/how-to-check-programmatically-if-hotspot-is-enabled-or-disabled
fun Context.isWifiHotspotEnabled(): Boolean {
    val AP_STATE_DISABLING = 10
    val AP_STATE_DISABLED = 11
    val AP_STATE_ENABLING = 12
    val AP_STATE_ENABLED = 13
    val AP_STATE_FAILED = 14

    val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val method: Method = wifiManager.javaClass.getMethod("getWifiApState").also {
        it.isAccessible = true
    }
    val invoke = method.invoke(wifiManager)
    println("hotspot state: $invoke")
    return when (invoke.toString().toIntOrNull()) {
        AP_STATE_DISABLING, AP_STATE_DISABLED, AP_STATE_FAILED -> false
        AP_STATE_ENABLING, AP_STATE_ENABLED -> true
        else -> false
    }
}

// https://developer.android.com/reference/android/net/wifi/WifiManager
@RequiresApi(Build.VERSION_CODES.M)
fun Context.networkState() {
    val request = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()
    val networkCallback: NetworkCallback = object : NetworkCallback() {
        override fun onAvailable(network: Network) = Unit

        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            val wifiInfo = networkCapabilities.transportInfo as WifiInfo?
        }
    }
    val connectivityManager = getSystemService(ConnectivityManager::class.java).apply {
        requestNetwork(request, networkCallback)
        registerNetworkCallback(request, networkCallback)
    }
}

// https://stackoverflow.com/questions/30040014/android-bluetooth-get-connected-devices
fun isBluetoothEnabled(): Boolean = BluetoothAdapter.getDefaultAdapter().isEnabled

// https://stackoverflow.com/questions/24693682/turn-off-device-programmatically
fun Context.showPowerButtonOptions() {
    val intent = Intent(Intent.ACTION_SHUTDOWN).apply {
        putExtra("android.intent.extra.KEY_CONFIRM", true)
    }
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    }
}

/**
 * Super User
 * <uses-permission android:name="android.permission.SHUTDOWN" />  */
// https://stackoverflow.com/questions/24693682/turn-off-device-programmatically
fun Context.shutDownDevice() {
    val intent = Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN").apply {
        putExtra("android.intent.extra.KEY_CONFIRM", false)
        flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(intent)
}

// https://stackoverflow.com/questions/10411650/how-to-shutdown-an-android-mobile-programmatically
fun shutDownOnRootedDevice() {
    try {
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot -p"))
        process.waitFor()
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

// Primary source: https://github.com/farmerbb/Taskbar
// https://stackoverflow.com/questions/14352648/how-to-lock-unlock-screen-programmatically
// https://rdcworld-android.blogspot.com/2012/03/lock-phone-screen-programmtically.html
fun Context.lockDevice() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        sendAccessibilityAction(action = AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN, onComplete = {})
    }
}

fun Context.showNotificationDrawer() {
    sendAccessibilityAction(action = AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS, onComplete = {})
}

fun Context.showPowerButtonActions() {
    sendAccessibilityAction(action = AccessibilityService.GLOBAL_ACTION_POWER_DIALOG, onComplete = {})
}

fun Context.takeScreenshot() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        sendAccessibilityAction(action = AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT, onComplete = {})
    }
}

fun Context.toggleSplitScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        sendAccessibilityAction(action = AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN, onComplete = {})
    }
}

fun Context.showAndroidNavAppsSwitcher() {
    sendAccessibilityAction(action = AccessibilityService.GLOBAL_ACTION_RECENTS, onComplete = {})
}

fun Context.showAndroidNavHome() {
    sendAccessibilityAction(action = AccessibilityService.GLOBAL_ACTION_HOME, onComplete = {})
}

fun Context.showAndroidNavBack() {
    sendAccessibilityAction(action = AccessibilityService.GLOBAL_ACTION_BACK, onComplete = {})
}

fun Context.showQuickSettings() {
    sendAccessibilityAction(action = AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS, onComplete = {})
}

// https://github.com/farmerbb/Taskbar
fun Context.sendAccessibilityAction(action: Int, onComplete: () -> Unit) {
    setComponentEnabled(context = this, clazz = PowerMenuService::class.java, enabled = true)
    setComponentEnabled(context = this, clazz = NotificationFetchService::class.java, enabled = true)
    val isAccessibilityServiceEnabled = isAccessibilityServiceEnabled(this)
    when {
        isAccessibilityServiceEnabled.not() && isWriteSecureSettingsPermissionGranted() -> {
            val services = Settings.Secure.getString(
                /* resolver = */ contentResolver,
                /* name = */ Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            val finalServices = services ?: ""
            val powerMenuService: String = ComponentName(this, PowerMenuService::class.java).flattenToString()
            if (finalServices.contains(powerMenuService).not()) {
                try {
                    Settings.Secure.putString(
                        /* resolver = */ contentResolver,
                        /* name = */ Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                        /* value = */ if (finalServices.isEmpty()) powerMenuService else "$finalServices:$powerMenuService"
                    )
                } catch (_: Exception) {
                }
            }
            doAfter(100) {
                val intent = Intent(IntentAction.ACTION_ACCESSIBILITY_ACTION).apply {
                    putExtra(IntentExtra.EXTRA_ACTION, action)
                }
                sendCustomBroadcast(intent)
                try {
                    Settings.Secure.putString(
                        /* resolver = */ contentResolver,
                        /* name = */ Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                        /* value = */ finalServices
                    )
                } catch (ignored: java.lang.Exception) {
                }
                onComplete.invoke()
            }
        }
        isAccessibilityServiceEnabled -> {
            val intent = Intent(IntentAction.ACTION_ACCESSIBILITY_ACTION).apply {
                putExtra(IntentExtra.EXTRA_ACTION, action)
            }
            sendCustomBroadcast(intent)
            onComplete.invoke()
        }
        else -> {
            showAlertDialog(
                title = getString(R.string.tb_permission_dialog_title),
                message = getString(R.string.tb_enable_accessibility),
                positiveBtnText = getString(R.string.tb_action_activate),
                negativeBtnText = getString(R.string.tb_action_cancel),
                positiveAction = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        showToast(getString(R.string.tb_lock_device_not_supported))
                    }
                }
            )
        }
    }
}

fun Context.registerCustomReceiver(receiver: BroadcastReceiver?, vararg actions: String?) {
    unregisterCustomReceiver(receiver)
    val intentFilter = IntentFilter()
    for (action in actions) {
        intentFilter.addAction(action)
    }
    LocalBroadcastManager.getInstance(this).registerReceiver(receiver!!, intentFilter)
}

fun Context.unregisterCustomReceiver(receiver: BroadcastReceiver?) {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver!!)
}

fun Context.sendCustomBroadcast(intent: Intent?) {
    intent ?: return
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
}

fun Context.isWriteSecureSettingsPermissionGranted(): Boolean {
    return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED)
}

// https://github.com/farmerbb/Taskbar
fun setComponentEnabled(context: Context, clazz: Class<*>?, enabled: Boolean) = try {
    val component = ComponentName(context, clazz!!)
    context.packageManager.setComponentEnabledSetting(
        /* p0 = */ component,
        /* p1 = */ if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        /* p2 = */ PackageManager.DONT_KILL_APP
    )
} catch (_: Exception) {
}

// https://github.com/farmerbb/Taskbar
fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val accessibilityServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
    val component = ComponentName(context, PowerMenuService::class.java)
    return (accessibilityServices != null &&
            (accessibilityServices.contains(component.flattenToString()) || accessibilityServices.contains(component.flattenToShortString())))
}

fun isLibrary(context: Context): Boolean = context.packageName != BuildConfig.APPLICATION_ID

fun Context.showVolumeQuickSettings() {
    val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_SAME, AudioManager.USE_DEFAULT_STREAM_TYPE, AudioManager.FLAG_SHOW_UI)
}

// https://github.com/farmerbb/Taskbar
fun Context.showFileManager() {
    val fileManagerIntent: Intent

    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) fileManagerIntent = Intent(Intent.ACTION_VIEW) else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) fileManagerIntent = Intent("android.provider.action.BROWSE") else {
        fileManagerIntent = Intent("android.provider.action.BROWSE_DOCUMENT_ROOT")
        fileManagerIntent.component = ComponentName.unflattenFromString("com.android.documentsui/.DocumentsActivity")
    }

    fileManagerIntent.addCategory(Intent.CATEGORY_DEFAULT)
    fileManagerIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    fileManagerIntent.data = Uri.parse("content://com.android.externalstorage.documents/root/primary")

    try {
        startActivity(fileManagerIntent)
    } catch (e: ActivityNotFoundException) {
        showToast(getString(R.string.tb_lock_device_not_supported))
    } catch (ignored: IllegalArgumentException) {
    }
}

fun Context.isFlashAvailable(): Boolean = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

fun Context.isFrontCameraAvailable(): Boolean = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)

fun flashLight(isOn: Boolean, cameraManager: CameraManager) {
    try {
        val cameraId = cameraManager.cameraIdList.firstOrNull() ?: ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager.setTorchMode(cameraId, isOn)
        }
    } catch (_: Exception) {
    }
}

// https://github.com/farmerbb/Taskbar
// U can get notifications count form this
fun Context.modifyNotifications() {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    } catch (e: ActivityNotFoundException) {
        showToast(getString(R.string.tb_lock_device_not_supported))
    }
}

// https://github.com/farmerbb/Taskbar
// U can get recently used/closed apps
fun Context.requestUsageAccess() {
    try {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    } catch (e: ActivityNotFoundException) {
        showToast(getString(R.string.tb_lock_device_not_supported))
    }
}