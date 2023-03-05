package com.singularitycoder.flowlauncher.helper.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import com.singularitycoder.flowlauncher.helper.constants.Broadcast

class CustomBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_TIME_CHANGED -> {}
            Intent.ACTION_DATE_CHANGED -> {}
            Intent.ACTION_TIMEZONE_CHANGED -> {}
            Intent.ACTION_TIME_TICK -> {
                context.sendBroadcast(action = Broadcast.TIME_CHANGED)
            }
            Intent.ACTION_BOOT_COMPLETED -> {}
            Intent.ACTION_PACKAGE_FULLY_REMOVED -> {
                context.sendBroadcast(action = Broadcast.PACKAGE_REMOVED)
            }
            Intent.ACTION_PACKAGE_ADDED -> {}
            Intent.ACTION_PACKAGE_CHANGED -> {}
            Intent.ACTION_PACKAGE_REMOVED -> {}
            Intent.ACTION_PACKAGE_RESTARTED -> {}
            Intent.ACTION_PACKAGE_DATA_CLEARED -> {}
            Intent.ACTION_PACKAGES_SUSPENDED -> {}
            Intent.ACTION_PACKAGES_UNSUSPENDED -> {}
            Intent.ACTION_PACKAGE_INSTALL -> {
                context.sendBroadcast(action = Broadcast.PACKAGE_INSTALLED)
            }
            Intent.ACTION_UID_REMOVED -> Unit
            Intent.ACTION_BATTERY_CHANGED -> Unit
            Intent.ACTION_POWER_CONNECTED -> Unit
            Intent.ACTION_POWER_DISCONNECTED -> Unit
            Intent.ACTION_SCREEN_OFF -> Unit
            Intent.ACTION_SCREEN_ON -> Unit
            Intent.ACTION_SHUTDOWN -> {
                // Requires <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
            }
        }
    }

    private fun Context.sendBroadcast(
        action: String,
        bundle: Bundle = bundleOf()
    ) {
        val intent = Intent(action).apply {
            putExtras(bundle)
        }
        sendBroadcast(intent)
    }
}