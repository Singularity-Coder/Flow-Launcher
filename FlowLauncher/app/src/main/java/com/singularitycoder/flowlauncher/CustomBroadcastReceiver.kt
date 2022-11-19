package com.singularitycoder.flowlauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.singularitycoder.flowlauncher.helper.constants.Broadcast

class CustomBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_TICK -> {
                sendTimeChangedBroadcast(context)
            }
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_FULLY_REMOVED -> {
                sendPackageRemovedBroadcast(context)
            }
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_INSTALL -> {
                sendPackageAddedBroadcast(context)
            }
        }
    }

    private fun sendTimeChangedBroadcast(context: Context) {
        val intent = Intent(Broadcast.TIME_CHANGED)
        context.sendBroadcast(intent)
    }

    private fun sendPackageRemovedBroadcast(context: Context) {
        val intent = Intent(Broadcast.PACKAGE_REMOVED)
        context.sendBroadcast(intent)
    }

    private fun sendPackageAddedBroadcast(context: Context) {
        val intent = Intent(Broadcast.PACKAGE_INSTALLED)
        context.sendBroadcast(intent)
    }
}