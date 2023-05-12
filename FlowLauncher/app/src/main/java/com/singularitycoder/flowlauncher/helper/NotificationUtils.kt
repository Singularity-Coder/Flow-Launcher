package com.singularitycoder.flowlauncher.helper

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.app.*
import androidx.core.content.LocusIdCompat
import androidx.core.graphics.drawable.IconCompat
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.helper.constants.Notif
import com.singularitycoder.flowlauncher.quickSettings.view.QuickShortcutsBubbleActivity
import com.singularitycoder.flowlauncher.toBitmap
import kotlinx.parcelize.Parcelize

// https://www.youtube.com/watch?v=Ge4_4ZnAAX8
// >= API 26 which is Oreo, u need channels
// To have context inside this class, extend it from ContextWrapper
// Use NotificationManagerCompat is available in support lib. Used for posting notifications to support older versions of android
// https://stackoverflow.com/questions/18253482/vibrate-and-sound-defaults-on-notification
class NotificationUtils(context: Context?) : ContextWrapper(context) {

    private val vibrationPattern1 = longArrayOf(0, 1000, 0, 0, 0) // { delay, vibrate, sleep, vibrate, sleep } pattern

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val screenshotCountdownChannel = createChannel(
                channelId = Notif.SCREENSHOT_COUNTDOWN.channelId,
                channelName = Notif.SCREENSHOT_COUNTDOWN.channelName,
                channelDescription = "This notification is for showing screenshot countdown!"
            )

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
                createNotificationChannel(screenshotCountdownChannel)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(
        channelId: String,
        channelName: String,
        channelDescription: String
    ): NotificationChannel = NotificationChannel(
        channelId,
        channelName,
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        enableLights(true) // Shows notification indicator on device
        enableVibration(true) // Device vibrates
        val soundAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
        setSound(resourceUri(R.raw.shotgun), soundAttributes)
        vibrationPattern = vibrationPattern1
        description = channelDescription // Shows up in device notification settings
        lightColor = Color.WHITE // Notification indicator color on device
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC // Shows notification content even when the device is locked
    }

    // Title, text, smallIcon are mandatory
    fun showNotification(
        data: FlowNotification,
        @DrawableRes bigPicture: Int? = null,
        mainActivity: Class<out Any>,
        isFloatBubble: Boolean = false
    ) {
        val intent = Intent(this, mainActivity).apply {
            putParcelableArrayListExtra(data.intentKey, ArrayList<Parcelable?>().apply { add(data) })
        }
        val pendingIntent = PendingIntent.getActivity(
            /* context = */ this,
            /* requestCode = */ data.notificationType.ordinal,
            /* intent = */ intent,
            /* flags = */ flagUpdateCurrentData(isMutable = false)
        )
        val largeNotificationStyle = NotificationCompat.BigPictureStyle()
            .setBigContentTitle(data.title)
            .setSummaryText(data.description)
            .bigLargeIcon(drawable(R.mipmap.ic_launcher)?.toBitmap())

        if (null != bigPicture) largeNotificationStyle.bigPicture(drawable(bigPicture)?.toBitmap())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            largeNotificationStyle.showBigPictureWhenCollapsed(true)
        }

        val smallNotificationStyle = NotificationCompat.BigTextStyle()
            .setBigContentTitle(data.title)
            .setSummaryText(data.description)
            .bigText(data.description)
        val notificationStyle = if (data.isLargeNotification) largeNotificationStyle else smallNotificationStyle
        val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification = NotificationCompat.Builder(this, Notif.SCREENSHOT_COUNTDOWN.channelId)
            .setContentTitle(data.title)
            .setContentText(data.description)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // For < API 26 this is a must
            .setStyle(notificationStyle)
            .setSound(resourceUri(R.raw.shotgun))
            .setVibrate(vibrationPattern1)
            .setTicker("Screenshot countdown")
            .setLights(/* argb = */ Color.WHITE, /* onMs = */ 1000, /* offMs = */ 0)
            .setAutoCancel(true) // When u click on notif it removes itself from the notif drawer

        if (isFloatBubble) {
            val floatBubbleIntent = Intent(this, QuickShortcutsBubbleActivity::class.java).apply {
                action = Intent.ACTION_VIEW
            }
            val pendingIntentFloatBubble = PendingIntent.getActivity(
                /* context = */ this,
                /* requestCode = */ data.notificationType.ordinal,
                /* intent = */ floatBubbleIntent,
                /* flags = */ flagUpdateCurrentData(isMutable = true)
            )
            val bubbleIcon = IconCompat.createWithBitmap(this.drawable(R.drawable.baseline_flash_on_24)?.toBitmap() ?: getBitmapOf())
            notification.apply {
                bubbleMetadata = NotificationCompat.BubbleMetadata.Builder(pendingIntentFloatBubble, bubbleIcon)
                    .setDesiredHeight(resources.getDimensionPixelSize(R.dimen.bubble_height)) // The height of the expanded bubble.
                    .setAutoExpandBubble(true)
                    .build()
                setCategory(Notification.CATEGORY_MESSAGE)
                setShortcutId(data.intentKey)
                // This ID helps the intelligence services of the device to correlate this notification with the corresponding dynamic shortcut.
                setLocusId(LocusIdCompat(data.intentKey.toString()))
                setShowWhen(true)
                notification.setContentIntent(pendingIntent)
            }
        } else {
            notification.setContentIntent(pendingIntent)
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        NotificationManagerCompat.from(this).notify(data.notificationType.ordinal, notification.build())
    }

    private fun flagUpdateCurrentData(isMutable: Boolean): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        return if (isMutable) {
            if (Build.VERSION.SDK_INT >= 31) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        }
    }
}

@Parcelize
data class FlowNotification(
    val title: String?,
    val description: String? = null,
    val intentKey: String? = null,
    val notificationType: Notif = Notif.SCREENSHOT_COUNTDOWN,
    val isLargeNotification: Boolean = false
) : Parcelable