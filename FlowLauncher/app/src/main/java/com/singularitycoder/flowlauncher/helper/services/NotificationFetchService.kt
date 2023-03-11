package com.singularitycoder.flowlauncher.helper.services

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.singularitycoder.flowlauncher.helper.constants.IntentAction
import com.singularitycoder.flowlauncher.helper.constants.IntentExtra
import com.singularitycoder.flowlauncher.helper.constants.IntentKey
import com.singularitycoder.flowlauncher.helper.registerCustomReceiver
import com.singularitycoder.flowlauncher.helper.sendCustomBroadcast
import com.singularitycoder.flowlauncher.helper.showToast
import com.singularitycoder.flowlauncher.helper.unregisterCustomReceiver
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.*


// https://stackoverflow.com/questions/28712568/get-notification-message-using-accessibilityservice-android
// https://issuetracker.google.com/issues/36984668
// https://stackoverflow.com/questions/32570505/why-is-my-notificationlistenerservice-not-working
// https://stackoverflow.com/questions/40503081/onaccessibilityevent-not-called-at-all
// https://stackoverflow.com/questions/37577737/getting-notifications-with-accessibilty-service-if-device-is-muted
class NotificationFetchService : AccessibilityService() {
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!performGlobalAction(intent.getIntExtra(IntentExtra.EXTRA_ACTION, -1))) {
                this@NotificationFetchService.showToast("Not supported on this device")
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        var count = 0
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
        val notificationsList: Array<StatusBarNotification?> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notificationManager.activeNotifications
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }
        for (notification in notificationsList) {
            if (notification != null && notification.notification.flags and NotificationCompat.FLAG_GROUP_SUMMARY == 0 && notification.isClearable) {
                count++
            }
        }
//        val notificationsList = getNotificationData2(event)
        broadcastNotificationCount(count)
    }

    override fun onInterrupt() {}
    override fun onCreate() {
        super.onCreate()
        registerCustomReceiver(receiver, IntentAction.ACTION_ACCESSIBILITY_ACTION)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterCustomReceiver(receiver)
    }

    /** Data sent to [HomeFragment] */
    private fun broadcastNotificationCount(count: Int) {
        val bundle = bundleOf(IntentKey.NOTIFICATION_COUNT to count)
        sendCustomBroadcast(action = IntentAction.ACTION_NOTIFICATION_LIST, bundle = bundle)
    }

    // https://stackoverflow.com/questions/9292032/extract-notification-text-from-parcelable-contentview-or-contentintent
    private fun getNotificationData(event: AccessibilityEvent) {
        val notification: Notification = event.parcelableData as? Notification ?: return
        val views: RemoteViews? = notification.contentView
        val secretClass: Class<*>? = views?.javaClass

        try {
            val text: MutableMap<Int?, String> = HashMap()
            val outerFields: Array<Field> = secretClass?.declaredFields ?: emptyArray()
            for (i in outerFields.indices) {
                if (!outerFields[i].name.equals("mActions")) continue
                outerFields[i].isAccessible = true
//                val list = outerFields[i].get(views)
//                for (action: Any in list) {
//                    val innerFields: Array<Field> = action.javaClass.declaredFields
//                    var value: Any? = null
//                    var type: Int? = null
//                    var viewId: Int? = null
//                    innerFields.forEach { field ->
//                        field.isAccessible = true
//                        if (field.name.equals("value")) {
//                            value = field.get(action)
//                        } else if (field.name.equals("type")) {
//                            type = field.getInt(action)
//                        } else if (field.name.equals("viewId")) {
//                            viewId = field.getInt(action)
//                        }
//                    }
//                    if (type == 9 || type == 10) {
//                        text[viewId] = value.toString()
//                    }
//                }
                println("title is: " + text[16908310])
                println("info is: " + text[16909082])
                println("text is: " + text[16908358])
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // https://stackoverflow.com/questions/9292032/extract-notification-text-from-parcelable-contentview-or-contentintent
    private fun getNotificationData2(event: AccessibilityEvent): List<String>? {
        val notification: Notification = event.parcelableData as? Notification ?: return emptyList()
        // We have to extract the information from the view
        var views = notification.bigContentView
        if (views == null) views = notification.contentView
        if (views == null) return null

        // Use reflection to examine the m_actions member of the given RemoteViews object.
        // It's not pretty, but it works.
        val text: MutableList<String> = ArrayList()
        try {
            val field = views.javaClass.getDeclaredField("mActions")
            field.isAccessible = true
            val actions = field[views] as ArrayList<Parcelable>

            // Find the setText() and setTime() reflection actions
            for (p in actions) {
                val parcel = Parcel.obtain()
                p.writeToParcel(parcel, 0)
                parcel.setDataPosition(0)

                // The tag tells which type of action it is (2 is ReflectionAction, from the source)
                val tag = parcel.readInt()
                if (tag != 2) continue

                // View ID
                parcel.readInt()
                val methodName = parcel.readString()
                if (methodName == null) continue else if (methodName == "setText") {
                    // Parameter type (10 = Character Sequence)
                    parcel.readInt()

                    // Store the actual string
                    val t: String = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel).toString().trim()
                    text.add(t)
                } else if (methodName == "setTime") {
                    // Parameter type (5 = Long)
                    parcel.readInt()
                    val t: String = SimpleDateFormat("h:mm a").format(Date(parcel.readLong()))
                    text.add(t)
                }
                parcel.recycle()
            }
        } // It's not usually good style to do this, but then again, neither is the use of reflection...
        catch (e: java.lang.Exception) {
            println("NotificationClassifier $e")
        }
        return text
    }

    private fun getNotificationData3(event: AccessibilityEvent): List<String> {
        val notification: Notification = event.parcelableData as? Notification ?: return emptyList()
        println("ticker: " + notification.tickerText)
        println("icon: " + notification.icon)
        println("notification: " + event.text)
        return emptyList()
    }
}