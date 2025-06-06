package com.singularitycoder.flowlauncher.helper.constants

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi

// Regex to select everything after equals [=](.*)
// This regex selects 5 chars or n chars if n dots after equals [=].....

private val androidPermissions1 = listOf(
    Manifest.permission.ACCESS_CHECKIN_PROPERTIES,
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
    Manifest.permission.ACCESS_NETWORK_STATE,
    Manifest.permission.ACCESS_WIFI_STATE,
    Manifest.permission.ACCOUNT_MANAGER,
    Manifest.permission.ADD_VOICEMAIL,
    Manifest.permission.BATTERY_STATS,
    Manifest.permission.BIND_ACCESSIBILITY_SERVICE,
    Manifest.permission.BIND_APPWIDGET,
    Manifest.permission.BIND_DEVICE_ADMIN,
    Manifest.permission.BIND_DREAM_SERVICE,
    Manifest.permission.BIND_INPUT_METHOD,
    Manifest.permission.BIND_NFC_SERVICE,
    Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
    Manifest.permission.BIND_PRINT_SERVICE,
    Manifest.permission.BIND_REMOTEVIEWS,
    Manifest.permission.BIND_TEXT_SERVICE,
    Manifest.permission.BIND_TV_INPUT,
    Manifest.permission.BIND_VOICE_INTERACTION,
    Manifest.permission.BIND_VPN_SERVICE,
    Manifest.permission.BIND_WALLPAPER,
    Manifest.permission.BLUETOOTH,
    Manifest.permission.BLUETOOTH_ADMIN,
    Manifest.permission.BLUETOOTH_PRIVILEGED,
    Manifest.permission.BODY_SENSORS,
    Manifest.permission.BROADCAST_PACKAGE_REMOVED,
    Manifest.permission.BROADCAST_SMS,
    Manifest.permission.BROADCAST_STICKY,
    Manifest.permission.BROADCAST_WAP_PUSH,
    Manifest.permission.CALL_PHONE,
    Manifest.permission.CALL_PRIVILEGED,
    Manifest.permission.CAMERA,
    Manifest.permission.CAPTURE_AUDIO_OUTPUT,
    Manifest.permission.CHANGE_COMPONENT_ENABLED_STATE,
    Manifest.permission.CHANGE_CONFIGURATION,
    Manifest.permission.CHANGE_NETWORK_STATE,
    Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
    Manifest.permission.CHANGE_WIFI_STATE,
    Manifest.permission.CLEAR_APP_CACHE,
    Manifest.permission.CONTROL_LOCATION_UPDATES,
    Manifest.permission.DELETE_CACHE_FILES,
    Manifest.permission.DELETE_PACKAGES,
    Manifest.permission.DIAGNOSTIC,
    Manifest.permission.DISABLE_KEYGUARD,
    Manifest.permission.DUMP,
    Manifest.permission.EXPAND_STATUS_BAR,
    Manifest.permission.FACTORY_TEST,
    Manifest.permission.GET_ACCOUNTS,
    Manifest.permission.GET_PACKAGE_SIZE,
    Manifest.permission.GET_TASKS,
    Manifest.permission.GLOBAL_SEARCH,
    Manifest.permission.INSTALL_LOCATION_PROVIDER,
    Manifest.permission.INSTALL_PACKAGES,
    Manifest.permission.INSTALL_SHORTCUT,
    Manifest.permission.INTERNET,
    Manifest.permission.KILL_BACKGROUND_PROCESSES,
    Manifest.permission.LOCATION_HARDWARE,
    Manifest.permission.MANAGE_DOCUMENTS,
    Manifest.permission.MASTER_CLEAR,
    Manifest.permission.MEDIA_CONTENT_CONTROL,
    Manifest.permission.MODIFY_AUDIO_SETTINGS,
    Manifest.permission.MODIFY_PHONE_STATE,
    Manifest.permission.MOUNT_FORMAT_FILESYSTEMS,
    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
    Manifest.permission.NFC,
    Manifest.permission.PERSISTENT_ACTIVITY,
    Manifest.permission.PROCESS_OUTGOING_CALLS,
    Manifest.permission.READ_CALENDAR,
    Manifest.permission.READ_CALL_LOG,
    Manifest.permission.READ_CONTACTS,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.READ_INPUT_STATE,
    Manifest.permission.READ_LOGS,
    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.READ_SMS,
    Manifest.permission.READ_SYNC_SETTINGS,
    Manifest.permission.READ_SYNC_STATS,
    Manifest.permission.READ_VOICEMAIL,
    Manifest.permission.REBOOT,
    Manifest.permission.RECEIVE_BOOT_COMPLETED,
    Manifest.permission.RECEIVE_MMS,
    Manifest.permission.RECEIVE_SMS,
    Manifest.permission.RECEIVE_WAP_PUSH,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.REORDER_TASKS,
    Manifest.permission.RESTART_PACKAGES,
    Manifest.permission.SEND_RESPOND_VIA_MESSAGE,
    Manifest.permission.SEND_SMS,
    Manifest.permission.SET_ALARM,
    Manifest.permission.SET_ALWAYS_FINISH,
    Manifest.permission.SET_ANIMATION_SCALE,
    Manifest.permission.SET_DEBUG_APP,
    Manifest.permission.SET_PREFERRED_APPLICATIONS,
    Manifest.permission.SET_PROCESS_LIMIT,
    Manifest.permission.SET_TIME,
    Manifest.permission.SET_TIME_ZONE,
    Manifest.permission.SET_WALLPAPER,
    Manifest.permission.SET_WALLPAPER_HINTS,
    Manifest.permission.SIGNAL_PERSISTENT_PROCESSES,
    Manifest.permission.STATUS_BAR,
    Manifest.permission.SYSTEM_ALERT_WINDOW,
    Manifest.permission.TRANSMIT_IR,
    Manifest.permission.UNINSTALL_SHORTCUT,
    Manifest.permission.UPDATE_DEVICE_STATS,
    Manifest.permission.USE_SIP,
    Manifest.permission.VIBRATE,
    Manifest.permission.WAKE_LOCK,
    Manifest.permission.WRITE_APN_SETTINGS,
    Manifest.permission.WRITE_CALENDAR,
    Manifest.permission.WRITE_CALL_LOG,
    Manifest.permission.WRITE_CONTACTS,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_GSERVICES,
    Manifest.permission.WRITE_SECURE_SETTINGS,
    Manifest.permission.WRITE_SETTINGS,
    Manifest.permission.WRITE_SYNC_SETTINGS,
    Manifest.permission.WRITE_VOICEMAIL,
)

@RequiresApi(Build.VERSION_CODES.S)
private val androidPermissions2 = listOf(
    Manifest.permission.ACCEPT_HANDOVER,
    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
    Manifest.permission.ACCESS_BLOBS_ACROSS_USERS,
    Manifest.permission.ACCESS_MEDIA_LOCATION,
    Manifest.permission.ACCESS_NOTIFICATION_POLICY,
    Manifest.permission.ACTIVITY_RECOGNITION,
    Manifest.permission.ANSWER_PHONE_CALLS,
    Manifest.permission.BIND_CALL_REDIRECTION_SERVICE,
    Manifest.permission.BIND_CARRIER_MESSAGING_CLIENT_SERVICE,
    Manifest.permission.BIND_AUTOFILL_SERVICE,
    Manifest.permission.BIND_CARRIER_MESSAGING_SERVICE,
    Manifest.permission.BIND_CARRIER_SERVICES,
    Manifest.permission.BIND_CHOOSER_TARGET_SERVICE,
    Manifest.permission.BIND_COMPANION_DEVICE_SERVICE,
    Manifest.permission.BIND_CONDITION_PROVIDER_SERVICE,
    Manifest.permission.BIND_CONTROLS,
    Manifest.permission.BIND_INCALL_SERVICE,
    Manifest.permission.BIND_MIDI_DEVICE_SERVICE,
    Manifest.permission.BIND_QUICK_ACCESS_WALLET_SERVICE,
    Manifest.permission.BIND_QUICK_SETTINGS_TILE,
    Manifest.permission.BIND_SCREENING_SERVICE,
    Manifest.permission.BIND_TELECOM_CONNECTION_SERVICE,
    Manifest.permission.BIND_VISUAL_VOICEMAIL_SERVICE,
    Manifest.permission.BIND_VR_LISTENER_SERVICE,
    Manifest.permission.BLUETOOTH_ADVERTISE,
    Manifest.permission.BLUETOOTH_CONNECT,
    Manifest.permission.BLUETOOTH_SCAN,
    Manifest.permission.CALL_COMPANION_APP,
    Manifest.permission.FOREGROUND_SERVICE,
    Manifest.permission.GET_ACCOUNTS_PRIVILEGED,
    Manifest.permission.HIDE_OVERLAY_WINDOWS,
    Manifest.permission.HIGH_SAMPLING_RATE_SENSORS,
    Manifest.permission.INSTANT_APP_FOREGROUND_SERVICE,
    Manifest.permission.INTERACT_ACROSS_PROFILES,
    Manifest.permission.LOADER_USAGE_STATS,
    Manifest.permission.MANAGE_EXTERNAL_STORAGE,
    Manifest.permission.MANAGE_MEDIA,
    Manifest.permission.MANAGE_ONGOING_CALLS,
    Manifest.permission.MANAGE_OWN_CALLS,
    Manifest.permission.NFC_PREFERRED_PAYMENT_INFO,
    Manifest.permission.NFC_TRANSACTION_EVENT,
    Manifest.permission.PACKAGE_USAGE_STATS,
    Manifest.permission.QUERY_ALL_PACKAGES,
    Manifest.permission.READ_PHONE_NUMBERS,
    Manifest.permission.READ_PRECISE_PHONE_STATE,
    Manifest.permission.REQUEST_COMPANION_PROFILE_WATCH,
    Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND,
    Manifest.permission.REQUEST_COMPANION_START_FOREGROUND_SERVICES_FROM_BACKGROUND,
    Manifest.permission.REQUEST_COMPANION_USE_DATA_IN_BACKGROUND,
    Manifest.permission.REQUEST_DELETE_PACKAGES,
    Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
    Manifest.permission.REQUEST_INSTALL_PACKAGES,
    Manifest.permission.REQUEST_OBSERVE_COMPANION_DEVICE_PRESENCE,
    Manifest.permission.REQUEST_PASSWORD_COMPLEXITY,
    Manifest.permission.SCHEDULE_EXACT_ALARM,
    Manifest.permission.SMS_FINANCIAL_TRANSACTIONS,
    Manifest.permission.START_FOREGROUND_SERVICES_FROM_BACKGROUND,
    Manifest.permission.START_VIEW_PERMISSION_USAGE,
    Manifest.permission.UPDATE_PACKAGES_WITHOUT_USER_ACTION,
    Manifest.permission.USE_BIOMETRIC,
    Manifest.permission.USE_FINGERPRINT,
    Manifest.permission.USE_FULL_SCREEN_INTENT,
    Manifest.permission.USE_ICC_AUTH_WITH_DEVICE_IDENTIFIER,
    Manifest.permission.UWB_RANGING,
)

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private val androidPermissions3 = listOf(
    Manifest.permission.POST_NOTIFICATIONS,
    Manifest.permission.OVERRIDE_WIFI_CONFIG,
    Manifest.permission.BIND_TV_INTERACTIVE_APP,
    Manifest.permission.BODY_SENSORS_BACKGROUND,
    Manifest.permission.DELIVER_COMPANION_MESSAGES,
    Manifest.permission.LAUNCH_MULTI_PANE_SETTINGS_DEEP_LINK,
    Manifest.permission.MANAGE_WIFI_INTERFACES,
    Manifest.permission.MANAGE_WIFI_NETWORK_SELECTION,
    Manifest.permission.NEARBY_WIFI_DEVICES,
    Manifest.permission.READ_ASSISTANT_APP_SEARCH_DATA,
    Manifest.permission.READ_BASIC_PHONE_STATE,
    Manifest.permission.READ_HOME_APP_SEARCH_DATA,
    Manifest.permission.READ_MEDIA_AUDIO,
    Manifest.permission.READ_MEDIA_IMAGES,
    Manifest.permission.READ_MEDIA_VIDEO,
    Manifest.permission.READ_NEARBY_STREAMING_POLICY,
    Manifest.permission.REQUEST_COMPANION_PROFILE_APP_STREAMING,
    Manifest.permission.REQUEST_COMPANION_PROFILE_AUTOMOTIVE_PROJECTION,
    Manifest.permission.REQUEST_COMPANION_PROFILE_COMPUTER,
    Manifest.permission.REQUEST_COMPANION_SELF_MANAGED,
    Manifest.permission.START_VIEW_APP_FEATURES,
    Manifest.permission.SUBSCRIBE_TO_KEYGUARD_LOCKED_STATE,
    Manifest.permission.USE_EXACT_ALARM,
)

val allAndroidPermissions = listOf(
    androidPermissions1,
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        androidPermissions2
    } else emptyList(),
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        androidPermissions3
    } else emptyList(),
).flatten().toTypedArray()