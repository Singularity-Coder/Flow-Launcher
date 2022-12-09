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

// Just because the code is open source doesnt mean these are. These are just for show. Purchase them from original source for commercial use.
val tempImageUrlList = listOf(
    // Pexels
    "https://images.pexels.com/photos/1382731/pexels-photo-1382731.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/1382730/pexels-photo-1382730.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/1251247/pexels-photo-1251247.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/1382734/pexels-photo-1382734.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/2422915/pexels-photo-2422915.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/2850287/pexels-photo-2850287.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/1083822/pexels-photo-1083822.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/2150/sky-space-dark-galaxy.jpg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/1323550/pexels-photo-1323550.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/219932/pexels-photo-219932.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/7925859/pexels-photo-7925859.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/921703/pexels-photo-921703.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/1564655/pexels-photo-1564655.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/2832074/pexels-photo-2832074.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/2179422/pexels-photo-2179422.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/6781720/pexels-photo-6781720.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/11623695/pexels-photo-11623695.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/2061057/pexels-photo-2061057.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/2694561/pexels-photo-2694561.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",

    // https://www.artstation.com/wlop - I am speechless
    "https://cdna.artstation.com/p/assets/images/images/037/291/422/large/wlop-33se.jpg?1620016916",
    "https://cdnb.artstation.com/p/assets/images/images/031/648/147/large/wlop-3se.jpg?1604222403",
    "https://cdnb.artstation.com/p/assets/images/images/054/497/061/large/wlop-1se.jpg?1664691512",
    "https://cdnb.artstation.com/p/assets/images/images/054/088/453/large/wlop-2se.jpg?1663735786",
    "https://cdna.artstation.com/p/assets/images/images/053/461/876/large/wlop-66se.jpg?1662278693",
    "https://cdna.artstation.com/p/assets/images/images/053/058/242/large/wlop-65se.jpg?1661324815",
    "https://cdnb.artstation.com/p/assets/images/images/051/594/225/large/wlop-34se.jpg?1657692924",
    "https://cdnb.artstation.com/p/assets/images/images/045/923/541/large/wlop-23se.jpg?1643862531",
    "https://cdnb.artstation.com/p/assets/images/images/045/259/505/large/wlop-10se.jpg?1642308657",
    "https://cdna.artstation.com/p/assets/images/images/021/083/124/large/wl-op-3s.jpg?1570338646",

    // https://www.artstation.com/dadachyo - Pure eye candy
    "https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/96f51c3e-3aba-4f7c-98cb-cdd9e2acdaf9/dcm5wxy-01c67715-302b-4e8b-b872-40ef1fbd5124.jpg?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7InBhdGgiOiJcL2ZcLzk2ZjUxYzNlLTNhYmEtNGY3Yy05OGNiLWNkZDllMmFjZGFmOVwvZGNtNXd4eS0wMWM2NzcxNS0zMDJiLTRlOGItYjg3Mi00MGVmMWZiZDUxMjQuanBnIn1dXSwiYXVkIjpbInVybjpzZXJ2aWNlOmZpbGUuZG93bmxvYWQiXX0.08E9WxlJdLbOr8jX1urteeyl9rqeRq6nbqwzvbbRcuE",
    "https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/96f51c3e-3aba-4f7c-98cb-cdd9e2acdaf9/dcg8sz8-5a0af99e-f9b7-464f-ba73-8a224058dcf0.jpg?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7InBhdGgiOiJcL2ZcLzk2ZjUxYzNlLTNhYmEtNGY3Yy05OGNiLWNkZDllMmFjZGFmOVwvZGNnOHN6OC01YTBhZjk5ZS1mOWI3LTQ2NGYtYmE3My04YTIyNDA1OGRjZjAuanBnIn1dXSwiYXVkIjpbInVybjpzZXJ2aWNlOmZpbGUuZG93bmxvYWQiXX0.GpGFRW4jswQNqPQPtxlg8ed64LMJc5mi3LvWlXdLP6c",
    "https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/96f51c3e-3aba-4f7c-98cb-cdd9e2acdaf9/dcddx6a-6a5545a1-34f3-412b-9cb9-72da94e9d147.jpg?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7InBhdGgiOiJcL2ZcLzk2ZjUxYzNlLTNhYmEtNGY3Yy05OGNiLWNkZDllMmFjZGFmOVwvZGNkZHg2YS02YTU1NDVhMS0zNGYzLTQxMmItOWNiOS03MmRhOTRlOWQxNDcuanBnIn1dXSwiYXVkIjpbInVybjpzZXJ2aWNlOmZpbGUuZG93bmxvYWQiXX0.mgggg51YkJKkNI27dlWseejSkFPBd386HrBH_7i7uCw",
    "https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/96f51c3e-3aba-4f7c-98cb-cdd9e2acdaf9/dbrcn12-08622bce-b04c-4c84-9f96-2d6c868bb242.jpg?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7InBhdGgiOiJcL2ZcLzk2ZjUxYzNlLTNhYmEtNGY3Yy05OGNiLWNkZDllMmFjZGFmOVwvZGJyY24xMi0wODYyMmJjZS1iMDRjLTRjODQtOWY5Ni0yZDZjODY4YmIyNDIuanBnIn1dXSwiYXVkIjpbInVybjpzZXJ2aWNlOmZpbGUuZG93bmxvYWQiXX0.ehZt-O964201iQyAo-sE6rqyV8VqEynDxrf40jH3h8Y",
    "https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/96f51c3e-3aba-4f7c-98cb-cdd9e2acdaf9/dbqj7er-cc1a8948-c577-4ca6-99ca-e5b58f69b996.jpg/v1/fill/w_800,h_943,q_75,strp/dadachyo_1014_by_dadachyo_dbqj7er-fullview.jpg?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7ImhlaWdodCI6Ijw9OTQzIiwicGF0aCI6IlwvZlwvOTZmNTFjM2UtM2FiYS00ZjdjLTk4Y2ItY2RkOWUyYWNkYWY5XC9kYnFqN2VyLWNjMWE4OTQ4LWM1NzctNGNhNi05OWNhLWU1YjU4ZjY5Yjk5Ni5qcGciLCJ3aWR0aCI6Ijw9ODAwIn1dXSwiYXVkIjpbInVybjpzZXJ2aWNlOmltYWdlLm9wZXJhdGlvbnMiXX0.1Ik7EiQpkcQWCz2oaIQwMbq9EkSXgFFT3odriUSmS2o",
    "https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/96f51c3e-3aba-4f7c-98cb-cdd9e2acdaf9/dc2y0sc-5110e5ae-8706-4a0b-b391-3b41421a8d73.jpg/v1/fill/w_600,h_740,q_75,strp/20180212_by_dadachyo_dc2y0sc-fullview.jpg?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7ImhlaWdodCI6Ijw9NzQwIiwicGF0aCI6IlwvZlwvOTZmNTFjM2UtM2FiYS00ZjdjLTk4Y2ItY2RkOWUyYWNkYWY5XC9kYzJ5MHNjLTUxMTBlNWFlLTg3MDYtNGEwYi1iMzkxLTNiNDE0MjFhOGQ3My5qcGciLCJ3aWR0aCI6Ijw9NjAwIn1dXSwiYXVkIjpbInVybjpzZXJ2aWNlOmltYWdlLm9wZXJhdGlvbnMiXX0.XDaqNsaAkMgpWtMiIfS7u5fq-WgQE9YXFz7-evro1fU",
    "https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/96f51c3e-3aba-4f7c-98cb-cdd9e2acdaf9/dcjzk9k-ca9621d8-8554-45ab-8578-a02adf06a7d3.jpg/v1/fill/w_1066,h_750,q_70,strp/romantic_mermaid_by_dadachyo_dcjzk9k-pre.jpg?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7ImhlaWdodCI6Ijw9OTAwIiwicGF0aCI6IlwvZlwvOTZmNTFjM2UtM2FiYS00ZjdjLTk4Y2ItY2RkOWUyYWNkYWY5XC9kY2p6azlrLWNhOTYyMWQ4LTg1NTQtNDVhYi04NTc4LWEwMmFkZjA2YTdkMy5qcGciLCJ3aWR0aCI6Ijw9MTI4MCJ9XV0sImF1ZCI6WyJ1cm46c2VydmljZTppbWFnZS5vcGVyYXRpb25zIl19.xhLUoJtyV-_0BSPWDN4hvCweFO7aFYq3zcpzPDWrOUA",
    "https://cdna.artstation.com/p/assets/images/images/031/928/910/large/dadachyo-20170616-1000.jpg?1605006164",
    "https://cdna.artstation.com/p/assets/images/images/007/049/694/large/dadachyo-ddc-20170425-900jpg.jpg?1503318185",

    // https://www.artstation.com/guweiz
    "https://cdna.artstation.com/p/assets/images/images/023/848/644/large/z-w-gu-5b.jpg?1580520356",

    // https://www.artstation.com/razaras
    "https://cdna.artstation.com/p/assets/images/images/054/361/876/large/kittichai-rueangchaichan-razaras-my-room-23-p.jpg?1664365930",
    "https://cdnb.artstation.com/p/assets/images/images/052/530/045/large/kittichai-rueangchaichan-razaras-my-room-21-p.jpg?1660045409",
    "https://cdnb.artstation.com/p/assets/images/images/056/163/393/large/kittichai-rueangchaichan-razaras-my-room-27-e1p.jpg?1668601359",
    "https://cdnb.artstation.com/p/assets/images/images/038/461/001/large/kittichai-rueangchaichan-razaras-oracle-my-room-p.jpg?1623162296",
    "https://cdna.artstation.com/p/assets/images/images/050/367/312/large/kittichai-rueangchaichan-razaras-my-room-18-p1.jpg?1654686559",

    // https://www.artstation.com/203
    "https://cdna.artstation.com/p/assets/images/images/002/794/298/large/zhang-weicheng-lx17.jpg?1465805961",

    // https://www.pinterest.com/kaatcreative/
    "https://i.pinimg.com/originals/27/1d/58/271d58601cabeabd44d73c99b8c8a113.jpg",

    // https://www.artstation.com/ksenia_o
    "https://cdnb.artstation.com/p/assets/images/images/014/397/469/large/ksenia-ovchinnikova-07.jpg?1543824755",

    // https://www.artstation.com/danny008
    "https://cdnb.artstation.com/p/assets/images/images/046/145/245/large/dannylailai-3.jpg?1644407304",
    "https://cdna.artstation.com/p/assets/images/images/055/287/746/large/dannylailai-.jpg?1666603911",
    "https://cdna.artstation.com/p/assets/images/images/043/142/984/large/dannylailai-.jpg?1636438625",

    // https://www.artstation.com/syiyiyiyiyiyi
    "https://cdnb.artstation.com/p/assets/images/images/037/068/003/large/yyyan-.jpg?1619406568",
    "https://cdnb.artstation.com/p/assets/images/images/028/614/947/large/-9c430cc5ly1g986qdan7fj21ww2pgkjr.jpg?1594976637",

    // https://www.artstation.com/7200ss
    "https://cdnb.artstation.com/p/assets/images/images/005/840/803/large/sangsoo-jeong-android18.jpg?1494165857",
    "https://cdna.artstation.com/p/assets/images/images/046/618/948/large/sangsoo-jeong-traditionalarmorgirl-4crop.jpg?1645557954",
    "https://cdna.artstation.com/p/assets/images/images/056/705/036/large/sangsoo-jeong-traditionalarmorgirl-8taracrop.jpg?1669895743",

    // Pexels
    "https://images.pexels.com/photos/1496373/pexels-photo-1496373.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/45848/kumamoto-japan-aso-cloud-45848.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/1222561/pexels-photo-1222561.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/531321/pexels-photo-531321.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/797947/pexels-photo-797947.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/426894/pexels-photo-426894.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/2559484/pexels-photo-2559484.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/2397651/pexels-photo-2397651.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/1114897/pexels-photo-1114897.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/4666754/pexels-photo-4666754.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/247421/pexels-photo-247421.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/1042828/pexels-photo-1042828.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/13556748/pexels-photo-13556748.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/3227984/pexels-photo-3227984.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/391522/pexels-photo-391522.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/1403550/pexels-photo-1403550.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/2765872/pexels-photo-2765872.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/1336924/pexels-photo-1336924.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/6611628/pexels-photo-6611628.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/5319953/pexels-photo-5319953.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/12735759/pexels-photo-12735759.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/5326990/pexels-photo-5326990.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/5277693/pexels-photo-5277693.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/663317/pexels-photo-663317.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/2175952/pexels-photo-2175952.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/247599/pexels-photo-247599.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"
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