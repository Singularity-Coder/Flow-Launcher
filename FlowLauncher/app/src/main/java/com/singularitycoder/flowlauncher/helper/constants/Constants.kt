package com.singularitycoder.flowlauncher.helper.constants

import android.Manifest
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.singularitycoder.flowlauncher.BuildConfig
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.addEditMedia.view.AddFragment
import com.singularitycoder.flowlauncher.helper.deviceWidth
import com.singularitycoder.flowlauncher.helper.dpToPx
import com.singularitycoder.flowlauncher.today.model.QuoteColor
import java.util.concurrent.TimeUnit

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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Manifest.permission.BLUETOOTH_CONNECT
    } else {
        Manifest.permission.BLUETOOTH
    }
)

object AppGrid {
    /** Spacing calc: App Icon width is 56dp * 4 = 224dp. So (device width - icons width * 4)
     * This is the available space for spacing the 4 app columns evenly. Its used 8 times.
     * Since spacing is present to both the left and right side of the app multiplied by 4 */
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
}

object FragmentsTag {
    val ADD_ITEM = AddFragment::class.java.simpleName
}

object IntentKey {
    const val YOUTUBE_VIDEO_LIST = "YOUTUBE_VIDEO_LIST"
    const val YOUTUBE_VIDEO_ID = "YOUTUBE_VIDEO_ID"
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

enum class QuickActions(val value: String) {
    VOICE_SEARCH(value = "Voice Search"),
    QUICK_SETTINGS(value = "Quick Settings"),
    SELECT_FLOW(value = "Select Flow"),
    GLANCE(value = "Glance"),
    TODAY(value = "Today"),
    NOTIFICATIONS(value = "Notifications"),
    UNIVERSAL_SEARCH(value = "Universal Search")
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
}

object Broadcast {
    const val TIME_CHANGED = "BROADCAST_TIME_CHANGED"
    const val PACKAGE_REMOVED = "BROADCAST_PACKAGE_REMOVED"
    const val PACKAGE_INSTALLED = "BROADCAST_PACKAGE_ADDED"
    const val VOLUME_RAISED = "BROADCAST_VOLUME_RAISED"
    const val VOLUME_LOWERED = "BROADCAST_VOLUME_LOWERED"
}

object WorkerTag {
    const val APPS_PARSER = "WORKER_TAG_APPS_PARSER"
    const val NEWS_PARSER = "WORKER_TAG_NEWS_PARSER"
    const val WEATHER_PARSER = "WORKER_TAG_WEATHER_PARSER"
    const val PUBLIC_HOLIDAYS_PARSER = "WORKER_TAG_PUBLIC_HOLIDAYS_PARSER"
    const val TRENDING_TWEETS_PARSER = "WORKER_TAG_TRENDING_TWEETS_PARSER"
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

val gradientList = listOf(
    R.drawable.gradient_default,
    R.drawable.gradient_red,
    R.drawable.gradient_pink,
    R.drawable.gradient_purple,
    R.drawable.gradient_deep_purple,
    R.drawable.gradient_indigo,
    R.drawable.gradient_blue,
    R.drawable.gradient_light_blue,
    R.drawable.gradient_cyan,
    R.drawable.gradient_teal,
    R.drawable.gradient_green,
    R.drawable.gradient_light_green,
    R.drawable.gradient_lime,
    R.drawable.gradient_yellow,
    R.drawable.gradient_amber,
    R.drawable.gradient_orange,
    R.drawable.gradient_deep_orange,
    R.drawable.gradient_brown,
    R.drawable.gradient_grey,
    R.drawable.gradient_blue_grey,
    R.drawable.gradient_light_black,

    R.drawable.gradient_default_light,
    R.drawable.gradient_red_light,
    R.drawable.gradient_pink_light,
    R.drawable.gradient_purple_light,
    R.drawable.gradient_deep_purple_light,
    R.drawable.gradient_indigo_light,
    R.drawable.gradient_blue_light,
    R.drawable.gradient_light_blue_light,
    R.drawable.gradient_cyan_light,
    R.drawable.gradient_teal_light,
    R.drawable.gradient_green_light,
    R.drawable.gradient_light_green_light,
    R.drawable.gradient_lime_light,
    R.drawable.gradient_yellow_light,
    R.drawable.gradient_amber_light,
    R.drawable.gradient_orange_light,
    R.drawable.gradient_deep_orange_light,
    R.drawable.gradient_brown_light,
    R.drawable.gradient_grey_light,
    R.drawable.gradient_blue_grey_light,
)

val quoteColorList = listOf(
    QuoteColor(
        textColor = R.color.purple_900,
        iconColor = R.color.purple_900,
        gradientColor = R.drawable.gradient_default_light
    ),
    QuoteColor(
        textColor = R.color.md_red_900,
        iconColor = R.color.md_red_900,
        gradientColor = R.drawable.gradient_red_light
    ),
    QuoteColor(
        textColor = R.color.md_pink_900,
        iconColor = R.color.md_pink_900,
        gradientColor = R.drawable.gradient_pink_light
    ),
    QuoteColor(
        textColor = R.color.md_purple_900,
        iconColor = R.color.md_purple_900,
        gradientColor = R.drawable.gradient_purple_light
    ),
    QuoteColor(
        textColor = R.color.md_deep_purple_900,
        iconColor = R.color.md_deep_purple_900,
        gradientColor = R.drawable.gradient_deep_purple_light
    ),
    QuoteColor(
        textColor = R.color.md_indigo_900,
        iconColor = R.color.md_indigo_900,
        gradientColor = R.drawable.gradient_indigo_light
    ),
    QuoteColor(
        textColor = R.color.md_blue_900,
        iconColor = R.color.md_blue_900,
        gradientColor = R.drawable.gradient_blue_light
    ),
    QuoteColor(
        textColor = R.color.md_light_blue_900,
        iconColor = R.color.md_light_blue_900,
        gradientColor = R.drawable.gradient_light_blue_light
    ),
    QuoteColor(
        textColor = R.color.md_cyan_900,
        iconColor = R.color.md_cyan_900,
        gradientColor = R.drawable.gradient_cyan_light
    ),
    QuoteColor(
        textColor = R.color.md_teal_900,
        iconColor = R.color.md_teal_900,
        gradientColor = R.drawable.gradient_teal_light
    ),
    QuoteColor(
        textColor = R.color.md_green_900,
        iconColor = R.color.md_green_900,
        gradientColor = R.drawable.gradient_green_light
    ),
    QuoteColor(
        textColor = R.color.md_light_green_900,
        iconColor = R.color.md_light_green_900,
        gradientColor = R.drawable.gradient_light_green_light
    ),
    QuoteColor(
        textColor = R.color.md_lime_900,
        iconColor = R.color.md_lime_900,
        gradientColor = R.drawable.gradient_lime_light
    ),
    QuoteColor(
        textColor = R.color.md_yellow_900,
        iconColor = R.color.md_yellow_900,
        gradientColor = R.drawable.gradient_yellow_light
    ),
    QuoteColor(
        textColor = R.color.md_amber_900,
        iconColor = R.color.md_amber_900,
        gradientColor = R.drawable.gradient_amber_light
    ),
    QuoteColor(
        textColor = R.color.md_deep_orange_900,
        iconColor = R.color.md_deep_orange_900,
        gradientColor = R.drawable.gradient_orange_light
    ),
    QuoteColor(
        textColor = R.color.md_deep_orange_900,
        iconColor = R.color.md_deep_orange_900,
        gradientColor = R.drawable.gradient_deep_orange_light
    ),
    QuoteColor(
        textColor = R.color.md_brown_900,
        iconColor = R.color.md_brown_900,
        gradientColor = R.drawable.gradient_brown_light
    ),
    QuoteColor(
        textColor = R.color.md_grey_900,
        iconColor = R.color.md_grey_900,
        gradientColor = R.drawable.gradient_grey_light
    ),
    QuoteColor(
        textColor = R.color.md_blue_grey_900,
        iconColor = R.color.md_blue_grey_900,
        gradientColor = R.drawable.gradient_blue_grey_light
    ),


    QuoteColor(
        textColor = R.color.purple_50,
        iconColor = R.color.purple_300,
        gradientColor = R.drawable.gradient_default
    ),
    QuoteColor(
        textColor = R.color.md_red_50,
        iconColor = R.color.md_red_400,
        gradientColor = R.drawable.gradient_red
    ),
    QuoteColor(
        textColor = R.color.md_pink_50,
        iconColor = R.color.md_pink_400,
        gradientColor = R.drawable.gradient_pink
    ),
    QuoteColor(
        textColor = R.color.md_purple_50,
        iconColor = R.color.md_purple_400,
        gradientColor = R.drawable.gradient_purple
    ),
    QuoteColor(
        textColor = R.color.md_deep_purple_50,
        iconColor = R.color.md_deep_purple_400,
        gradientColor = R.drawable.gradient_deep_purple
    ),
    QuoteColor(
        textColor = R.color.md_indigo_50,
        iconColor = R.color.md_indigo_400,
        gradientColor = R.drawable.gradient_indigo
    ),
    QuoteColor(
        textColor = R.color.md_blue_50,
        iconColor = R.color.md_blue_400,
        gradientColor = R.drawable.gradient_blue
    ),
    QuoteColor(
        textColor = R.color.md_light_blue_50,
        iconColor = R.color.md_light_blue_400,
        gradientColor = R.drawable.gradient_light_blue
    ),
    QuoteColor(
        textColor = R.color.md_cyan_50,
        iconColor = R.color.md_cyan_400,
        gradientColor = R.drawable.gradient_cyan
    ),
    QuoteColor(
        textColor = R.color.md_teal_50,
        iconColor = R.color.md_teal_400,
        gradientColor = R.drawable.gradient_teal
    ),
    QuoteColor(
        textColor = R.color.md_green_50,
        iconColor = R.color.md_green_400,
        gradientColor = R.drawable.gradient_green
    ),
    QuoteColor(
        textColor = R.color.md_light_green_50,
        iconColor = R.color.md_light_green_400,
        gradientColor = R.drawable.gradient_light_green
    ),
    QuoteColor(
        textColor = R.color.md_lime_50,
        iconColor = R.color.md_lime_400,
        gradientColor = R.drawable.gradient_lime
    ),
    QuoteColor(
        textColor = R.color.md_yellow_50,
        iconColor = R.color.md_yellow_400,
        gradientColor = R.drawable.gradient_yellow
    ),
    QuoteColor(
        textColor = R.color.md_amber_50,
        iconColor = R.color.md_amber_400,
        gradientColor = R.drawable.gradient_amber
    ),
    QuoteColor(
        textColor = R.color.md_deep_orange_50,
        iconColor = R.color.md_deep_orange_400,
        gradientColor = R.drawable.gradient_orange
    ),
    QuoteColor(
        textColor = R.color.md_deep_orange_50,
        iconColor = R.color.md_deep_orange_400,
        gradientColor = R.drawable.gradient_deep_orange
    ),
    QuoteColor(
        textColor = R.color.md_brown_50,
        iconColor = R.color.md_brown_400,
        gradientColor = R.drawable.gradient_brown
    ),
    QuoteColor(
        textColor = R.color.md_grey_50,
        iconColor = R.color.md_grey_400,
        gradientColor = R.drawable.gradient_grey
    ),
    QuoteColor(
        textColor = R.color.md_blue_grey_50,
        iconColor = R.color.md_blue_grey_400,
        gradientColor = R.drawable.gradient_blue_grey
    ),
    QuoteColor(
        textColor = R.color.light_gray,
        iconColor = R.color.light_gray,
        gradientColor = R.drawable.gradient_light_black
    ),
)