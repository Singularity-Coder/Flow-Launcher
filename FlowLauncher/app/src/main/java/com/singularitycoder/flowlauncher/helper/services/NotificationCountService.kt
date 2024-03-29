/* Copyright 2020 Braden Farmer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.singularitycoder.flowlauncher.helper.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.singularitycoder.flowlauncher.helper.constants.Broadcast
import com.singularitycoder.flowlauncher.helper.constants.IntentAction
import com.singularitycoder.flowlauncher.helper.constants.IntentKey
import com.singularitycoder.flowlauncher.helper.sendCustomBroadcast

// https://github.com/farmerbb/Taskbar
// TODO Not working
class NotificationCountService : NotificationListenerService() {
    private val requestCountReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            broadcastNotificationCount()
        }
    }

    override fun onListenerConnected() {
        registerReceiver(requestCountReceiver, IntentFilter(Broadcast.NOTIFICATION_LIST))
        broadcastNotificationCount()
    }

    override fun onListenerDisconnected() {
        unregisterReceiver(requestCountReceiver)
        broadcastNotificationCount(0)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        broadcastNotificationCount()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        broadcastNotificationCount()
    }

    private fun broadcastNotificationCount() {
        var count = 0
        val notifications: Array<StatusBarNotification?> = try {
            activeNotifications
        } catch (e: Exception) {
            arrayOfNulls(0)
        }
        for (notification in notifications) {
            if (notification != null && notification.notification.flags and NotificationCompat.FLAG_GROUP_SUMMARY == 0 && notification.isClearable) {
                count++
            }
        }
        broadcastNotificationCount(count)
    }

    /** Data sent to [HomeFragment] */
    private fun broadcastNotificationCount(count: Int) {
        val bundle = bundleOf(IntentKey.NOTIFICATION_COUNT to getValidCount(count))
        sendCustomBroadcast(action = IntentAction.ACTION_NOTIFICATION_LIST, bundle = bundle)
    }

    private fun getValidCount(count: Int): Int = Math.min(count, 99)
}
