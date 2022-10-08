package com.singularitycoder.flowlauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.singularitycoder.flowlauncher.helper.BROADCAST_TIME_CHANGED

class CustomBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_TICK -> {
                sendTimeChangedBroadcast(context)
            }
        }
    }

    private fun sendTimeChangedBroadcast(context: Context) {
        val intent = Intent(BROADCAST_TIME_CHANGED)
        context.sendBroadcast(intent)
    }
}