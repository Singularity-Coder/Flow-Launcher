package com.singularitycoder.flowlauncher.helper.constants

import com.singularitycoder.flowlauncher.BuildConfig
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.addEditMedia.view.AddFragment
import com.singularitycoder.flowlauncher.today.model.QuoteColor
import java.util.concurrent.TimeUnit

const val FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider"
const val REQUEST_CODE_VIDEO = 1001

const val KEY_IS_WORK_COMPLETE = "KEY_IS_WORK_COMPLETE"
const val FIRST_URL = "FIRST_URL"

val THIRTY_DAYS_IN_MILLIS = TimeUnit.DAYS.toMillis(30L)
val TWENTY_FOUR_HOURS_IN_MILLIS = TimeUnit.HOURS.toMillis(24L)

object BottomSheetTag {
    const val QUICK_SETTINGS_BOTTOM_SHEET = "QUICK_SETTINGS_BOTTOM_SHEET"
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
}

object WorkerTag {
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

val typefaceList = listOf(
    // fontsquirrel.com
    R.font.didonesque_roman_regular,
    R.font.blackjack,
    R.font.arizonia_regular,
    R.font.abril_fatface_regular,
    R.font.comfortaa_thin,
    R.font.sofia_regular,
    R.font.quicksand_regular,
    R.font.playfair_displaysc_regular,
    R.font.pacifico,
    R.font.lobster,
    R.font.kaushan_script_regular,
    R.font.dancing_script_regular,
    R.font.caviar_dreams_bold,
    R.font.milkshake,
    R.font.yeseva_one_regular,

    // fontspring.com
    R.font.cabrito_didone_regular,
    R.font.sangli_regular,
    R.font.oblik_classic_bold_italic,
    R.font.alethia_next_light_italic,
    R.font.cushy_regular,
    R.font.alma_mono_regular,
    R.font.fiorina_title_light,
    R.font.fiorina_title_light_italic,
    R.font.sovba_regular,
    R.font.jt_leonor_light,
    R.font.pons_rounded_slab_regular,
    R.font.haboro_regular,
    R.font.lagu_sans_light,
    R.font.cardillac_light,
    R.font.contax_sans_55_regular,
    R.font.fertigo_pro_regular,
    R.font.wreath_halftone_regular,
    R.font.civita_light,
    R.font.zekton_regular,
    R.font.carrara_light,
    R.font.merge_light,
    R.font.corda_light,
    R.font.queulat_regular,
    R.font.questa_grande_regular,
    R.font.neuro_political_regular,
    R.font.verona_serial_regular,
    R.font.stratford_serial_regular,
    R.font.mufan_pfs,
    R.font.worcester_serial_regular,
    R.font.beround_semibold,
    R.font.mono_fonto_regular,
    R.font.vanberg,
    R.font.larabiefont_regular,
    R.font.queulat_soft_regular,
    R.font.primer_print_regular,
    R.font.primer_print_bold,
    R.font.cyntho_next_light,
    R.font.jt_marnie_light,
    R.font.solitas_serif_normal_regular,
    R.font.goudy_serial_regular,
    R.font.sangli_normal_regular,
    R.font.radiata_medium,
    R.font.belda_normal_regular,
    R.font.recharge_bold,

    // Google Fonts
    R.font.courgette_regular,
    R.font.montserrat_alternates_regular,
    R.font.cookie_regular,
    R.font.oleo_script_regular,
    R.font.playball_regular,
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
)