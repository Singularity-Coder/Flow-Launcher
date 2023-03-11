package com.singularitycoder.flowlauncher.helper.constants

import android.Manifest
import android.os.Build
import android.os.Parcelable
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.singularitycoder.flowlauncher.BuildConfig
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.addEditMedia.view.AddFragment
import com.singularitycoder.flowlauncher.helper.deviceWidth
import com.singularitycoder.flowlauncher.helper.dpToPx
import kotlinx.parcelize.Parcelize
import java.util.concurrent.TimeUnit

const val DIRECTORY_DEFAULT_MEDIA = "DEFAULT_MEDIA"
const val FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider"
const val REQUEST_CODE_VIDEO = 1001

const val KEY_IS_WORK_COMPLETE = "KEY_IS_WORK_COMPLETE"
const val FIRST_URL = "FIRST_URL"

const val HOME_LAYOUT_BLURRED_IMAGE = "home_layout_blurred_image.jpg"

val THIRTY_DAYS_IN_MILLIS = TimeUnit.DAYS.toMillis(30L)
val TWENTY_FOUR_HOURS_IN_MILLIS = TimeUnit.HOURS.toMillis(24L)

val quickSettingsPermissions = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.POST_NOTIFICATIONS
    } else {
        Manifest.permission.CAMERA
    },
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Manifest.permission.BLUETOOTH_CONNECT
    } else {
        Manifest.permission.BLUETOOTH
    }
)

object IntentKey {
    const val YOUTUBE_VIDEO_LIST = "YOUTUBE_VIDEO_LIST"
    const val YOUTUBE_VIDEO_ID = "YOUTUBE_VIDEO_ID"
    const val NOTIF_SCREENSHOT_COUNTDOWN = "NOTIF_SCREENSHOT_COUNTDOWN"
    const val PACKAGE_NAME = "PACKAGE_NAME"
    const val DOWNLOAD_STATUS = "DOWNLOAD_STATUS"
    const val NOTIFICATION_COUNT = "NOTIFICATION_COUNT"
}

object IntentAction {
    const val ACTION_ACCESSIBILITY_ACTION = "com.singularitycoder.flowlauncher.ACCESSIBILITY_ACTION"
    const val ACTION_NOTIFICATION_LIST = "com.singularitycoder.flowlauncher.NOTIFICATION_LIST"
}

object IntentExtra {
    const val EXTRA_ACTION = "action"
}

@Parcelize
enum class Notif(
    val channelName: String,
    val channelId: String
) : Parcelable {
    SCREENSHOT_COUNTDOWN(
        channelName = "SCREENSHOT_COUNTDOWN",
        channelId = "${BuildConfig.APPLICATION_ID}.SCREENSHOT_COUNTDOWN"
    )
}

enum class VideoFormat(val extension: String) {
    MPEG_4(extension = "mp4"),
    THREE_GPP(extension = "3gp"),
    MATROSKA(extension = "mkv"),
    MPEG_TS(extension = "ts"),
    WEB_M(extension = "webm");
}

enum class HomeScreen {
    GLANCE, APPS, TODAY
}

enum class QuickActionHome(val value: String) {
    NONE(value = "None"),
    VOICE_SEARCH(value = "Voice Search"),
    QUICK_SETTINGS(value = "Quick Settings"),
    SELECT_FLOW(value = "Select Flow"),
    GLANCE(value = "Glance"),
    TODAY(value = "Today"),
    NOTIFICATIONS(value = "Notifications"),
    UNIVERSAL_SEARCH(value = "Universal Search"),
    PHONE(value = "Phone"),
    SMS(value = "SMS"),
    CAMERA(value = "Camera"),
}

enum class QuickActionAddMedia(val value: String) {
    NONE(value = "None"),
    SELECT_FROM_GALLERY(value = "Select from gallery"),
    TAKE_PHOTO(value = "Take Photo"),
    TAKE_VIDEO(value = "Take Video"),
}

object AppGrid {
    /** Spacing calc: App Icon width is 56dp * 4 = 224dp. So (device width - icon width * 4)
     * This is the available space for spacing the 4 app columns evenly. Its used 8 times.
     * Since spacing is present on both the left and right side of the app multiplied by 4 */
    const val COLUMNS = 4
    private val APP_ICON_WIDTH = 56.dpToPx()
    private val TOTAL_APP_ICON_WIDTH = APP_ICON_WIDTH * COLUMNS
    private const val TOTAL_APP_SIDES = COLUMNS * 2 // 2 because we add spacing on both sides of the app icon. Left and right
    private val AVAILABLE_WIDTH_FOR_SPACING = (deviceWidth() - TOTAL_APP_ICON_WIDTH) // The space available for spacing the apps evenly after removing the total 4 apps width
    val ONE_APP_SIDE_SPACING = AVAILABLE_WIDTH_FOR_SPACING / TOTAL_APP_SIDES
}

object BottomSheetTag {
    const val QUICK_SETTINGS = "QUICK_SETTINGS_BOTTOM_SHEET"
    const val APP_SELECTOR = "QUICK_SETTINGS_BOTTOM_SHEET"
    const val VOICE_SEARCH = "VOICE_SEARCH_BOTTOM_SHEET"
    const val DEVICE_ACTIVITY = "DEVICE_ACTIVITY_BOTTOM_SHEET"
}

object FragmentsTag {
    val ADD_ITEM: String = AddFragment::class.java.simpleName
}

object AddItemType {
    const val QUOTE = "QUOTE"
    const val GLANCE_IMAGE = "FLOW_IMAGE"
    const val YOUTUBE_VIDEO = "YOUTUBE_VIDEO"
}

// https://stackoverflow.com/questions/6000452/launching-mobile-network-settings-screen-programmatically
object SettingsScreen {
    const val HOME = Settings.ACTION_SETTINGS
    const val NETWORK = Settings.ACTION_WIRELESS_SETTINGS
    const val QUICK_NETWORK_TOGGLE_POPUP = "android.settings.panel.action.INTERNET_CONNECTIVITY"
    const val AIRPLANE_MODE = Settings.ACTION_AIRPLANE_MODE_SETTINGS
    const val WIFI_HOTSPOT = "android.settings.TETHER_SETTINGS"
    const val WIFI = Settings.ACTION_WIFI_SETTINGS
    const val BLUETOOTH = Settings.ACTION_BLUETOOTH_SETTINGS
    const val NFC_POPUP = "android.settings.panel.action.NFC"

    const val LOCATION = Settings.ACTION_LOCATION_SOURCE_SETTINGS
    const val SOUND = Settings.ACTION_SOUND_SETTINGS
    const val DISPLAY = Settings.ACTION_DISPLAY_SETTINGS
    const val DATE = Settings.ACTION_DATE_SETTINGS
    const val SECURITY = Settings.ACTION_SECURITY_SETTINGS
    const val APN = Settings.ACTION_APN_SETTINGS
    const val APPLICATION = Settings.ACTION_APPLICATION_SETTINGS
    const val NFC = Settings.ACTION_NFC_SETTINGS
    const val INTERNAL_STORAGE = Settings.ACTION_INTERNAL_STORAGE_SETTINGS
    const val USER_DICTIONARY = Settings.ACTION_USER_DICTIONARY_SETTINGS
    const val MANAGE_APPLICATION = Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS
    const val MANAGE_ALL_APPLICATION = Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS
    const val MEMORY_CARD = Settings.ACTION_MEMORY_CARD_SETTINGS

    @RequiresApi(Build.VERSION_CODES.O)
    const val NOTIFICATION = Settings.ACTION_APP_NOTIFICATION_SETTINGS

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    const val BATTERY_SAVER = Settings.ACTION_BATTERY_SAVER_SETTINGS
}

object Db {
    const val FLOW = "db_flow"
}

object Table {
    const val APP = "table_app"
    const val APP_FLOW = "table_app_flow"
    const val CONTACT = "table_contact"
    const val NEWS = "table_news"
    const val WEATHER = "table_weather"
    const val HOLIDAY = "table_holiday"
    const val TWITTER_TRENDING = "table_twitter_trending"
    const val QUOTE = "table_quote"
    const val YOUTUBE_VIDEO = "table_youtube_video"
    const val GLANCE_IMAGE = "table_glance_image"
    const val DEVICE_ACTIVITY = "table_device_activity"
}

object Broadcast {
    const val VOLUME_RAISED = "BROADCAST_VOLUME_RAISED"
    const val VOLUME_LOWERED = "BROADCAST_VOLUME_LOWERED"
    const val DOWNLOAD_COMPLETE = "BROADCAST_DOWNLOAD_COMPLETE"
    const val NOTIFICATION_LIST = "BROADCAST_NOTIFICATION_LIST"
}

object WorkerTag {
    const val APPS_PARSER = "WORKER_TAG_APPS_PARSER"
    const val NEWS_PARSER = "WORKER_TAG_NEWS_PARSER"
    const val WEATHER_PARSER = "WORKER_TAG_WEATHER_PARSER"
    const val PUBLIC_HOLIDAYS_PARSER = "WORKER_TAG_PUBLIC_HOLIDAYS_PARSER"
    const val TRENDING_TWEETS_PARSER = "WORKER_TAG_TRENDING_TWEETS_PARSER"
    const val TIME_ANNOUNCER = "WORKER_TAG_TIME_ANNOUNCER"
    const val UNIVERSAL_SEARCH = "WORKER_TAG_UNIVERSAL_SEARCH"
    const val WEB_LINKS_FETCH = "WORKER_TAG_WEB_LINKS_FETCH"
}

object WorkerData {
    const val URL = "WORKER_DATA_URL"
    const val URL_LINKS_LIST = "WORKER_DATA_URL_LINKS_LIST"
}

enum class SpeechAction(val value: String) {
    NONE("none"),
    OPEN("open"),
    LAUNCH("launch"),
    CALL("call"),
    MESSAGE("message"),
    SEARCH("search"),
    FIND("find"),
}

val daysMap = mapOf(
    "mon" to "Monday",
    "tue" to "Tuesday",
    "wed" to "Wednesday",
    "thu" to "Thursday",
    "fri" to "Friday",
    "sat" to "Saturday",
    "sun" to "Sunday",
)

val tempImageDrawableList = listOf(
    R.drawable.p1,
    R.drawable.p2,
    R.drawable.p3,
    R.drawable.p4,
    R.drawable.p5,
    R.drawable.p6,
    R.drawable.p7,
    R.drawable.p8,
    R.drawable.p9,
    R.drawable.p10,
    R.drawable.p11,
    R.drawable.p12,
    R.drawable.p13,
    R.drawable.p14,
    R.drawable.p15,
    R.drawable.p16,
    R.drawable.p17,
    R.drawable.p18,
    R.drawable.p19,
    R.drawable.p20,
)