package com.singularitycoder.flowlauncher.helper.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.singularitycoder.flowlauncher.helper.constants.Broadcast

class UninstallAppBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PACKAGE_FULLY_REMOVED -> {
                val packageName = intent.data?.encodedSchemeSpecificPart
                sendPackageRemovedBroadcast(context)
            }
        }
    }

    private fun sendPackageRemovedBroadcast(context: Context) {
        val intent = Intent(Broadcast.PACKAGE_REMOVED)
        context.sendBroadcast(intent)
    }
}