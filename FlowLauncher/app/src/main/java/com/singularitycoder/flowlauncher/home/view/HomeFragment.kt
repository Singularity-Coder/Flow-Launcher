package com.singularitycoder.flowlauncher.home.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.*
import coil.ImageLoader
import coil.request.ImageRequest
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.SharedViewModel
import com.singularitycoder.flowlauncher.addEditAppFlow.model.AppFlow
import com.singularitycoder.flowlauncher.addEditAppFlow.view.AddEditFlowFragment
import com.singularitycoder.flowlauncher.addEditAppFlow.viewModel.AppFlowViewModel
import com.singularitycoder.flowlauncher.databinding.FragmentHomeBinding
import com.singularitycoder.flowlauncher.deviceActivity.model.DeviceActivity
import com.singularitycoder.flowlauncher.deviceActivity.viewmodel.DeviceActivityViewModel
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.blur.BlurStackOptimized
import com.singularitycoder.flowlauncher.helper.constants.*
import com.singularitycoder.flowlauncher.helper.quickActionView.Action
import com.singularitycoder.flowlauncher.helper.quickActionView.QuickActionView
import com.singularitycoder.flowlauncher.home.dao.ContactDao
import com.singularitycoder.flowlauncher.home.model.App
import com.singularitycoder.flowlauncher.home.model.Contact
import com.singularitycoder.flowlauncher.home.viewmodel.HomeViewModel
import com.singularitycoder.flowlauncher.home.worker.AppWorker
import com.singularitycoder.flowlauncher.home.worker.EnableDisableAppsWorker
import com.singularitycoder.flowlauncher.quickSettings.view.QuickSettingsBottomSheetFragment
import com.singularitycoder.flowlauncher.toBitmapOf
import com.singularitycoder.flowlauncher.universalSearch.view.UniversalSearchFragment
import com.singularitycoder.flowlauncher.universalSearch.worker.UniversalSearchWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }

    @Inject
    lateinit var contactDao: ContactDao

    private lateinit var binding: FragmentHomeBinding
    private lateinit var dateTimeTimer: Timer
    private lateinit var timeAnnouncementTimer: Timer
    private lateinit var textToSpeech: TextToSpeech

    private val homeViewModel: HomeViewModel by viewModels()
    private val appFlowViewModel: AppFlowViewModel by viewModels()
    private val deviceActivityViewModel by viewModels<DeviceActivityViewModel>()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val homeAppsAdapter by lazy { HomeAppsAdapter() }

    private var speechAction = SpeechAction.NONE
    private var contactName: String = ""
    private var messageBody: String = ""
    private var removedAppPosition: Int = 0
    private var removedApp: App? = null
    private var flowName: String? = ""
    private var isTimeAnnouncementStopped: Boolean = false

    // TODO set proper event messages
    /**
     * Data received from [CustomBroadcastReceiver]
     * https://developer.android.com/reference/android/content/Intent
     * https://stackoverflow.com/questions/24072489/java-lang-securityexception-permission-denial-not-allowed-to-send-broadcast-an
     * */
    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_TIME_TICK -> {
                    setTimeDateAndFlow()
                }
                Intent.ACTION_PACKAGE_FULLY_REMOVED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "${removedApp?.title} was uninstalled."))
                    homeViewModel.removeAppFromDb(removedApp)
                    val appIconName = "app_icon_${removedApp?.packageName}".replace(oldValue = ".", newValue = "_")
                    val appIconDir = "${requireContext().filesDir?.absolutePath}/app_icons"
                    deleteBitmapFromInternalStorage(appIconName, appIconDir)
                    homeAppsAdapter.notifyItemRemoved(removedAppPosition)
                }
                Intent.ACTION_PACKAGE_INSTALL -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Certain app was installed."))
                    refreshAppList()
                }
                Intent.ACTION_TIME_CHANGED -> Unit
                Intent.ACTION_DATE_CHANGED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Date changed."))
                }
                Intent.ACTION_TIMEZONE_CHANGED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Time zone changed."))
                }
                Intent.ACTION_BOOT_COMPLETED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Device started."))
                }
                Intent.ACTION_PACKAGE_ADDED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "An App was installed."))
                }
                Intent.ACTION_PACKAGE_CHANGED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Package changed."))
                }
                Intent.ACTION_PACKAGE_REMOVED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "An App was removed."))
                }
                Intent.ACTION_PACKAGE_RESTARTED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "App restarted."))
                }
                Intent.ACTION_PACKAGE_DATA_CLEARED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "This App's data was cleared."))
                }
                Intent.ACTION_PACKAGES_SUSPENDED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "An App was suspended."))
                }
                Intent.ACTION_PACKAGES_UNSUSPENDED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "An App was unsuspended."))
                }
                Intent.ACTION_UID_REMOVED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "UID was removed."))
                }
                Intent.ACTION_BATTERY_CHANGED -> {
                    deviceActivityViewModel.addDeviceActivity(
                        DeviceActivity(
                            title = when (requireContext().batteryPercent()) {
                                5 -> "Battery is at 5 percent."
                                30 -> "Battery is at 30 percent."
                                else -> return
                            }
                        )
                    )
                }
                Intent.ACTION_BATTERY_LOW -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Battery is low."))
                }
                Intent.ACTION_BATTERY_OKAY -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Battery is okay."))
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Connected to power supply."))
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Disconnected from power supply."))
                }
                Intent.ACTION_SCREEN_OFF -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Screen turned off."))
                }
                Intent.ACTION_SCREEN_ON -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Screen turned on."))
                }
                Intent.ACTION_SHUTDOWN -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Device was shutdown."))
                    // Requires <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
                }
                Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Airplane mode changed"))
                }
                Intent.ACTION_APPLICATION_LOCALE_CHANGED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Application locale changed."))
                }
                Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Application restrictins changed."))
                }
                Intent.ACTION_CAMERA_BUTTON -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Camera button pressed."))
                }
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "System popups closed."))
                }
                Intent.ACTION_CONFIGURATION_CHANGED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Config changed."))
                }
                Intent.ACTION_DEVICE_STORAGE_LOW -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Device storage is slow."))
                }
                Intent.ACTION_DEVICE_STORAGE_OK -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Device storage is okay."))
                }
                Intent.ACTION_DOCK_EVENT -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Docked."))
                }
                Intent.ACTION_DREAMING_STARTED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Dreaming started."))
                }
                Intent.ACTION_DREAMING_STOPPED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Dreaming stopped."))
                }
                Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "External apps available."))
                }
                Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "External apps unavailable."))
                }
                Intent.ACTION_GTALK_SERVICE_CONNECTED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Gtalk service connected."))
                }
                Intent.ACTION_GTALK_SERVICE_DISCONNECTED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Gtalk service disconnected."))
                }
                Intent.ACTION_HEADSET_PLUG -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Connected to headset."))
                }
                Intent.ACTION_INPUT_METHOD_CHANGED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Input method changed."))
                }
                Intent.ACTION_LOCALE_CHANGED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Locale changed."))
                }
                Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Boot completed."))
                }
                Intent.ACTION_MANAGE_PACKAGE_STORAGE -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Manage package storage."))
                }
                Intent.ACTION_MEDIA_BAD_REMOVAL -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Media was removed poorly."))
                }
                Intent.ACTION_MEDIA_BUTTON -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Media button click detected."))
                }
                Intent.ACTION_MEDIA_CHECKING -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Checking media."))
                }
                Intent.ACTION_MEDIA_EJECT -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Media ejected."))
                }
                Intent.ACTION_MEDIA_MOUNTED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Media mounted."))
                }
                Intent.ACTION_MEDIA_NOFS -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Media NOFS."))
                }
                Intent.ACTION_MEDIA_REMOVED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Media was removed."))
                }
                Intent.ACTION_MEDIA_SCANNER_FINISHED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Media scan complete."))
                }
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Media scan file."))
                }
                Intent.ACTION_MEDIA_SCANNER_STARTED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Media scan started."))
                }
                Intent.ACTION_MEDIA_SHARED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Media shared."))
                }
                Intent.ACTION_MEDIA_UNMOUNTABLE -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Media unmountable."))
                }
                Intent.ACTION_MEDIA_UNMOUNTED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Media unmounted."))
                }
                Intent.ACTION_MY_PACKAGE_REPLACED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "My package replaced."))
                }
                Intent.ACTION_MY_PACKAGE_SUSPENDED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "My package suspended."))
                }
                Intent.ACTION_MY_PACKAGE_UNSUSPENDED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "My package unsuspended."))
                }
                Intent.ACTION_NEW_OUTGOING_CALL -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "New outgoing call."))
                }
                Intent.ACTION_PACKAGE_FIRST_LAUNCH -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Package first launch."))
                }
                Intent.ACTION_PACKAGE_NEEDS_VERIFICATION -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Package needs verification."))
                }
                Intent.ACTION_PACKAGE_REPLACED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Package replaced."))
                }
                Intent.ACTION_PACKAGE_VERIFIED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Package verified."))
                }
                Intent.ACTION_PROVIDER_CHANGED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Provider changed."))
                }
                Intent.ACTION_REBOOT -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Device rebooted."))
                }
                Intent.ACTION_UMS_CONNECTED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "UMS connected."))
                }
                Intent.ACTION_UMS_DISCONNECTED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "UMS disconnected."))
                }
                Intent.ACTION_USER_PRESENT -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "User exists."))
                }
                Intent.ACTION_USER_UNLOCKED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "User unlocked."))
                }
                Intent.ACTION_WALLPAPER_CHANGED -> {
                    deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Wallpaper changed."))
                }
                IntentAction.ACTION_NOTIFICATION_LIST -> {
                    // Handle notification count and list
                    val notificationCount = intent.getBundleExtra(IntentKey.NOTIFICATION_COUNT)
                    requireContext().showToast("Num of notifs: $notificationCount")
                }
            }
        }
    }

    private val permissionsResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions: Map<String, @JvmSuppressWildcards Boolean>? ->
        permissions ?: return@registerForActivityResult
        permissions.entries.forEach { it: Map.Entry<String, @JvmSuppressWildcards Boolean> ->
            println("Permission status: ${it.key} = ${it.value}")
            val permission = it.key
            val isGranted = it.value
            when {
                isGranted -> {
                    // disable blocking layout and proceed
                }
                ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission) -> {
                    // permission permanently denied. Show settings dialog
                    // enable blocking layout and show popup to go to settings
                    requireContext().showPermissionSettings()
                }
                else -> {
                    // Permission denied but not permanently, tell user why you need it. Ideally provide a button to request it again and another to dismiss
                    // enable blocking layout
                }
            }
        }
        if (permissions.values.any { it.not() }) {
            requireContext().showPermissionSettings()
        } else {
            parseUniversalSearchDataWithWorker()
        }
    }

    private val contactsPermissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isPermissionGranted: Boolean? ->
        isPermissionGranted ?: return@registerForActivityResult
        if (isPermissionGranted.not()) {
            requireContext().showPermissionSettings()
            return@registerForActivityResult
        }
        lifecycleScope.launch {
            val isContactsSynced = Preferences.read(requireContext()).getBoolean(Preferences.KEY_IS_CONTACTS_SYNCED, false)
            if (isContactsSynced.not()) {
                requireContext().getContactsList().sortedBy { it.name }.forEach { it: Contact ->
                    contactDao.insert(it)
                }
            }
            Preferences.write(requireContext()).putBoolean(Preferences.KEY_IS_CONTACTS_SYNCED, true).apply()
            val contact = contactDao.getAll().firstOrNull { it.name.contains(contactName) }
            when (speechAction) {
                SpeechAction.CALL -> {
                    requireContext().openDialer(phoneNumber = contact?.mobileNumber ?: "")
                }
                SpeechAction.MESSAGE -> {
                    requireContext().sendSms(phoneNumber = contact?.mobileNumber ?: "", body = messageBody)
                }
                else -> Unit
            }
            speechAction = SpeechAction.NONE
        }
    }

    private val speechToTextResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
        result ?: return@registerForActivityResult
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val data: Intent? = result.data
        lifecycleScope.launch(IO) {
            val text = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.trim()
            println("speech result: $text")
            when (text?.substringBefore(" ")?.toLowCase()) {
                SpeechAction.OPEN.value, SpeechAction.LAUNCH.value -> {
                    // TODO get app list from DB
                    val appName = text.substringAfter(" ")
                    val app = context?.appList()?.firstOrNull { it.title.contains(appName) } ?: kotlin.run {
                        // show list of apps with the starting letter
                        return@launch
                    }
                    withContext(Main) {
                        requireActivity().launchApp(app.packageName)
                    }
                }
                SpeechAction.CALL.value -> {
                    speechAction = SpeechAction.CALL
                    contactName = text.substringAfter(" ")
                    grantContactsPermission()
                }
                SpeechAction.MESSAGE.value -> {
                    speechAction = SpeechAction.MESSAGE
                    contactName = text.substringAfter(" ").substringBefore("saying")
                    messageBody = text.substringAfter("saying ")
                    grantContactsPermission()
                }
                SpeechAction.SEARCH.value, SpeechAction.FIND.value -> {
                    val query = text.substringAfter(" ")
                    requireActivity().searchWithChrome(query = query)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        webViewTest()
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    override fun onResume() {
        super.onResume()
        // Check if app count modified. if so then trigger apps
//        if (homeAppsAdapter.homeAppList.size != requireContext().appInfoList().size) {
//            refreshAppList()
//        }
        println("This triggers everytime we switch the screen in viewpager")
        permissionsResult.launch(callContactSmsPermissionList)
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_PACKAGE_FULLY_REMOVED))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_PACKAGE_INSTALL))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_TIME_CHANGED))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_DATE_CHANGED))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_TIMEZONE_CHANGED))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_BOOT_COMPLETED))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_PACKAGE_ADDED))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_PACKAGE_CHANGED))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_PACKAGE_REMOVED))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_PACKAGE_RESTARTED))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_PACKAGE_DATA_CLEARED))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_UID_REMOVED))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_BATTERY_LOW))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_BATTERY_OKAY))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_POWER_CONNECTED))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_POWER_DISCONNECTED))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_SCREEN_ON))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_SHUTDOWN))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_PACKAGES_SUSPENDED))
            activity?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_PACKAGES_UNSUSPENDED))
        }
        activity?.registerReceiver(broadcastReceiver, IntentFilter(IntentAction.ACTION_NOTIFICATION_LIST))
    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(broadcastReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        dateTimeTimer.purge()
        timeAnnouncementTimer.purge()
    }

    private fun FragmentHomeBinding.setupUI() {
        refreshAppList()
        refreshDateTime()
        initTextToSpeech()
        startTimeAnnouncementWorker()
        parseUniversalSearchDataWithWorker()
        rvApps.apply {
            layoutManager = GridLayoutManager(context, AppGrid.COLUMNS)
            adapter = homeAppsAdapter
            addItemDecoration(
                GridSpacingItemDecoration(
                    spanCount = AppGrid.COLUMNS,
                    spacing = AppGrid.ONE_APP_SIDE_SPACING, /* px */
                )
            )
        }
    }

    private fun FragmentHomeBinding.setupUserActionListeners() {
        homeAppsAdapter.setItemClickListener { app, position ->
            deviceActivityViewModel.addDeviceActivity(DeviceActivity(title = "Launched ${app.title} App."))
            requireActivity().launchApp(app.packageName)
        }

        homeAppsAdapter.setItemLongClickListener { view, app, position ->
            showPopupMenu(
                view = view,
                menuRes = R.menu.app_popup_menu,
                app = app,
                position = position
            )
        }

        setHomeFabTouchOptions()

        tvTime.onSafeClick {
            Intent(AlarmClock.ACTION_SET_ALARM).launchAppIfExists(requireActivity())
        }

        tvTime.setOnLongClickListener {
            if (isTimeAnnouncementStopped) {
                startTimeAnnouncementWorker()
                isTimeAnnouncementStopped = false
            } else {
                timeAnnouncementTimer.cancel()
                isTimeAnnouncementStopped = true
            }
            true
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = homeViewModel.appListStateFlow) { appList: List<App> ->
            if (appList.isEmpty()) return@collectLatestLifecycleFlow
            val allAppFlows = appFlowViewModel.getAllAppFlows()
            if (allAppFlows.firstOrNull()?.appList.isNullOrEmpty().not()) {
                return@collectLatestLifecycleFlow
            }
            appFlowViewModel.addAppFlow(
                AppFlow(
                    id = 1,
                    appFlowName = "Default Flow",
                    isSelected = true,
                    appList = appList
                )
            )
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = appFlowViewModel.appFlowListStateFlow) { it: List<AppFlow> ->
            if (it.isEmpty()) return@collectLatestLifecycleFlow
            val selectedFlow = it.firstOrNull { it.isSelected }
            val isFlowNameHasFlow = selectedFlow?.appFlowName?.contains(other = "flow", ignoreCase = true) == true
            flowName = if (isFlowNameHasFlow) selectedFlow?.appFlowName else "${selectedFlow?.appFlowName} Flow"
            homeAppsAdapter.homeAppList = selectedFlow?.appList ?: emptyList()
            withContext(Main) {
                // https://stackoverflow.com/questions/43221847/cannot-call-this-method-while-recyclerview-is-computing-a-layout-or-scrolling-wh
                homeAppsAdapter.notifyDataSetChanged()
                blurAndSaveBitmapForFlowSelectionScreenBackground()
                setEnableDisableAppsWorker(selectedFlow)
            }
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = sharedViewModel.voiceToTextStateFlow) { text: String ->
            if (text.isEmpty()) return@collectLatestLifecycleFlow
            lifecycleScope.launch(IO) {
                println("speech result: $text")
                when (text.substringBefore(" ").toLowCase()) {
                    SpeechAction.OPEN.value, SpeechAction.LAUNCH.value -> {
                        // TODO get app list from DB
                        val appName = text.substringAfter(" ")
                        val app = context?.appList()?.firstOrNull { it.title.contains(appName) } ?: kotlin.run {
                            // show list of apps with the starting letter
                            return@launch
                        }
                        withContext(Main) {
                            requireActivity().launchApp(app.packageName)
                        }
                    }
                    SpeechAction.CALL.value -> {
                        speechAction = SpeechAction.CALL
                        contactName = text.substringAfter(" ")
                        grantContactsPermission()
                    }
                    SpeechAction.MESSAGE.value -> {
                        speechAction = SpeechAction.MESSAGE
                        contactName = text.substringAfter(" ").substringBefore("saying")
                        messageBody = text.substringAfter("saying ")
                        grantContactsPermission()
                    }
                    SpeechAction.SEARCH.value, SpeechAction.FIND.value -> {
                        val query = text.substringAfter(" ")
                        requireActivity().searchWithChrome(query = query)
                    }
                }
                sharedViewModel.setVoiceToTextValue(text = "")
            }
        }

//        homeViewModel.appListLiveData.observe(viewLifecycleOwner) { it: List<App>? ->
//            homeAppsAdapter.homeAppList = it ?: emptyList()
//            homeAppsAdapter.notifyDataSetChanged()
//        }
    }

    private fun setEnableDisableAppsWorker(selectedFlow: AppFlow?) = lifecycleScope.launch {
        FlowUtils.selectedFlow = selectedFlow
        val workRequest = OneTimeWorkRequestBuilder<EnableDisableAppsWorker>().build()
        WorkManager.getInstance(requireContext()).enqueueUniqueWork(WorkerTag.ENABLE_DISABLE_APPS, ExistingWorkPolicy.REPLACE, workRequest)
    }

    private fun blurAndSaveBitmapForFlowSelectionScreenBackground() = lifecycleScope.launch {
        try {
            val blurredBitmapFile = File(
                /* parent = */ requireContext().getHomeLayoutBlurredImageFileDir(),
                /* child = */ HOME_LAYOUT_BLURRED_IMAGE
            )
            if (blurredBitmapFile.exists()) return@launch
            val homeLayoutBitmap = prepareHomeLayoutBitmap()
            val imageRequest = ImageRequest.Builder(requireContext()).data(homeLayoutBitmap).listener(
                onStart = {
                    // set your progressbar visible here
                },
                onSuccess = { request, metadata ->
                    // set your progressbar invisible here
                }
            ).build()
            val drawable = ImageLoader(requireContext()).execute(imageRequest).drawable
            val bitmapToBlur = (drawable as BitmapDrawable).bitmap
            val blurredBitmap = BlurStackOptimized().blur(image = bitmapToBlur, radius = 50)
            blurredBitmap.saveToInternalStorage(fileName = HOME_LAYOUT_BLURRED_IMAGE, fileDir = requireContext().getHomeLayoutBlurredImageFileDir())
        } catch (_: Exception) {
        }
    }

    private fun prepareHomeLayoutBitmap(): BitmapDrawable? {
        val imageLayout = binding.root
        val bitmapDrawableOfLayout = imageLayout.toBitmapOf(width = deviceWidth(), height = deviceHeight())?.toDrawable(requireContext().resources)
        return bitmapDrawableOfLayout
    }

    private fun setTimeDateAndFlow() {
        lifecycleScope.launch(IO) {
            // https://stackoverflow.com/questions/5369682/how-to-get-current-time-and-date-in-android#:~:text=Date%3B%20Date%20currentTime%20%3D%20Calendar.,getTime()%3B
            // https://stackoverflow.com/questions/7672597/how-to-get-timezone-from-android-mobile
            println("Time now: ${Calendar.getInstance().time}") // Sat Oct 08 00:58:23 GMT+05:30 2022
            println("Time zone: ${TimeZone.getDefault()}") // libcore.util.ZoneInfo[mDstSavings=0,mUseDst=false,mDelegate=[id="Asia/Kolkata",mRawOffset=19800000,mEarliestRawOffset=21208000,transitions=7]]

            val time = timeNow toTimeOfType DateType.h_mm_a
            val hours = time.substringBefore(":")
            val minutes = time.substringAfter(":").substringBefore(" ")
            val dayPeriod = time.substringAfter(" ").toUpCase()
            val html = "${if (hours.length == 1) "0$hours" else hours} : $minutes <small><small><small>$dayPeriod</small></small></small>"
            val day = Calendar.getInstance().time.toString().substringBefore(" ")

            withContext(Main) {
                binding.tvTime.text = getHtmlFormattedTime(html)
                binding.tvFlowType.text = "$day, ${timeNow toTimeOfType DateType.dd_MMM_yyyy}  |  $flowName"
            }
        }
    }

    private fun refreshDateTime() {
        dateTimeTimer = Timer()
        dateTimeTimer.doEvery(
            duration = 1.seconds(),
            withInitialDelay = 0.seconds(),
        ) {
            setTimeDateAndFlow()
        }
    }

    private fun grantContactsPermission() {
        contactsPermissionResult.launch(Manifest.permission.READ_CONTACTS)
    }

    private fun showPopupMenu(
        view: View,
        @MenuRes menuRes: Int,
        app: App,
        position: Int
    ) {
        PopupMenu(requireContext(), view).apply {
            this.menu.invokeSetMenuIconMethod()
            menuInflater.inflate(menuRes, this.menu)
            setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.menu_item_app_info -> {
                        requireContext().showInfoOf(app)
                        false
                    }
                    R.id.menu_item_remove_app -> {
                        removeApp(app, position)
                        false
                    }
                    R.id.menu_item_share_app -> {
                        requireContext().shareApkOf(app)
                        false
                    }
                    else -> false
                }
            }
            setOnDismissListener { it: PopupMenu? ->
            }
            setMarginBtwMenuIconAndText(
                context = requireContext(),
                menu = this.menu,
                iconMarginDp = 10
            )
            this.menu.forEach { it: MenuItem ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.iconTintList = ContextCompat.getColorStateList(requireContext(), R.color.purple_500)
                }
            }
            show()
        }
    }

    private fun removeApp(app: App, position: Int) {
        removedAppPosition = position
        removedApp = app
        requireActivity().uninstallApp(app.packageName)
    }

    private fun refreshAppList() {
        parseAppsWithWorker()
    }

    private fun parseAppsWithWorker() {
        val workManager = WorkManager.getInstance(requireContext())
        val workRequest = OneTimeWorkRequestBuilder<AppWorker>().build()
        workManager.enqueueUniqueWork(WorkerTag.APPS_PARSER, ExistingWorkPolicy.REPLACE, workRequest)
        workManager.getWorkInfoByIdLiveData(workRequest.id).observe(viewLifecycleOwner) { workInfo: WorkInfo? ->
            when (workInfo?.state) {
                WorkInfo.State.RUNNING -> {
                    println("RUNNING: show Progress")
                    showProgress(true)
                }
                WorkInfo.State.ENQUEUED -> println("ENQUEUED: show Progress")
                WorkInfo.State.SUCCEEDED -> {
                    println("SUCCEEDED: showing Progress")
                    showProgress(false)
                }
                WorkInfo.State.FAILED -> {
                    println("FAILED: stop showing Progress")
                    binding.root.showSnackBar("Something went wrong!")
                    showProgress(false)
                }
                WorkInfo.State.BLOCKED -> println("BLOCKED: show Progress")
                WorkInfo.State.CANCELLED -> {
                    println("CANCELLED: stop showing Progress")
                    showProgress(false)
                }
                else -> Unit
            }
        }
    }

    private fun startTimeAnnouncementWorker() {
        timeAnnouncementTimer = Timer()
        timeAnnouncementTimer.doEvery(duration = 35.seconds()) {
            withContext(Main) {
                val time = timeNow toTimeOfType DateType.h_mm_a
                val minutes = time.substringAfter(":").substringBefore(" ")
                val hours = time.substringBefore(":")
                val dayPeriod = time.substringAfter(" ")
                val timeToAnnounce = "It's $hours $minutes $dayPeriod"
                if (minutes == "00") startTextToSpeech(textToSpeak = timeToAnnounce)
            }
        }
    }

    private fun parseUniversalSearchDataWithWorker() {
        val workManager = WorkManager.getInstance(requireContext())
        val workRequest = PeriodicWorkRequestBuilder<UniversalSearchWorker>(PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MINUTES).build()
        workManager.enqueueUniquePeriodicWork(WorkerTag.UNIVERSAL_SEARCH, ExistingPeriodicWorkPolicy.KEEP, workRequest)
    }

    private fun setHomeFabTouchOptions() {
        val icon1 = requireContext().drawable(R.drawable.ic_round_keyboard_voice_24)?.changeColor(requireContext(), R.color.purple_500)
        val action1 = Action(id = QuickActionHome.VOICE_SEARCH.ordinal, icon = icon1!!, title = QuickActionHome.VOICE_SEARCH.value)

        val icon2 = requireContext().drawable(R.drawable.ic_round_tune_24)?.changeColor(requireContext(), R.color.purple_500)
        val action2 = Action(id = QuickActionHome.QUICK_SETTINGS.ordinal, icon = icon2!!, title = QuickActionHome.QUICK_SETTINGS.value)

        val icon3 = requireContext().drawable(R.drawable.ic_round_apps_24)?.changeColor(requireContext(), R.color.purple_500)
        val action3 = Action(id = QuickActionHome.SELECT_FLOW.ordinal, icon = icon3!!, title = QuickActionHome.SELECT_FLOW.value)

        val icon4 = requireContext().drawable(R.drawable.round_eye_24)?.changeColor(requireContext(), R.color.purple_500)
        val action4 = Action(id = QuickActionHome.GLANCE.ordinal, icon = icon4!!, title = QuickActionHome.GLANCE.value)

        val icon5 = requireContext().drawable(R.drawable.round_today_24)?.changeColor(requireContext(), R.color.purple_500)
        val action5 = Action(id = QuickActionHome.TODAY.ordinal, icon = icon5!!, title = QuickActionHome.TODAY.value)

        val icon6 = requireContext().drawable(R.drawable.ic_round_notifications_24)?.changeColor(requireContext(), R.color.purple_500)
        val action6 = Action(id = QuickActionHome.NOTIFICATIONS.ordinal, icon = icon6!!, title = QuickActionHome.NOTIFICATIONS.value)

        val icon7 = requireContext().drawable(R.drawable.ic_round_search_24)?.changeColor(requireContext(), R.color.purple_500)
        val action7 = Action(id = QuickActionHome.UNIVERSAL_SEARCH.ordinal, icon = icon7!!, title = QuickActionHome.UNIVERSAL_SEARCH.value)

        val icon8 = requireContext().drawable(R.drawable.round_phone_24)?.changeColor(requireContext(), R.color.purple_500)
        val action8 = Action(id = QuickActionHome.PHONE.ordinal, icon = icon8!!, title = QuickActionHome.PHONE.value)

        val icon9 = requireContext().drawable(R.drawable.round_sms_24)?.changeColor(requireContext(), R.color.purple_500)
        val action9 = Action(id = QuickActionHome.SMS.ordinal, icon = icon9!!, title = QuickActionHome.SMS.value)

        val icon10 = requireContext().drawable(R.drawable.round_photo_camera_24)?.changeColor(requireContext(), R.color.purple_500)
        val action10 = Action(id = QuickActionHome.CAMERA.ordinal, icon = icon10!!, title = QuickActionHome.PHONE.value)

        val homeFabQuickActionView = QuickActionView.make(requireContext()).apply {
            addAction(action1) // more configuring
            addAction(action2)
            addAction(action3)
            addAction(action4)
            addAction(action5)
            addAction(action6)
            addAction(action7)
            addAction(action8)
            addAction(action9)
            addAction(action10)
            register(binding.fabVoiceSearch)
            setBackgroundColor(requireContext().color(R.color.purple_50))
            setIndicatorDrawable(null)
        }
        homeFabQuickActionView.setOnActionHoverChangedListener { action: Action?, quickActionView: QuickActionView?, isHovering: Boolean ->
            // FIXME deselection issue
//            if (isHovering) {
//                quickActionView?.setBackgroundColor(requireContext().color(R.color.purple_500))
//                quickActionView?.setIconColor(R.color.purple_50)
//            } else {
//                quickActionView?.setBackgroundColor(requireContext().color(R.color.purple_50))
//                quickActionView?.setIconColor(R.color.purple_500)
//            }
        }
        homeFabQuickActionView.setOnActionSelectedListener { action: Action?, quickActionView: QuickActionView? ->
            when (action?.id) {
                QuickActionHome.VOICE_SEARCH.ordinal -> {
                    // Start Speech to Text
//                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
//                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
//                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Start Speaking Now!")
//                    }
//                    speechToTextResult.launch(intent)
                    VoiceSearchBottomSheetFragment.newInstance().show(
                        /* manager = */ requireActivity().supportFragmentManager,
                        /* tag = */ BottomSheetTag.VOICE_SEARCH
                    )
                }
                QuickActionHome.QUICK_SETTINGS.ordinal -> {
                    if (requireContext().isWriteSettingsPermissionGranted()) {
                        QuickSettingsBottomSheetFragment.newInstance().show(
                            /* manager = */ requireActivity().supportFragmentManager,
                            /* tag = */ BottomSheetTag.QUICK_SETTINGS
                        )
                    }
                }
                QuickActionHome.SELECT_FLOW.ordinal -> {
                    blurAndSaveBitmapForFlowSelectionScreenBackground()
                    (requireActivity() as AppCompatActivity).showScreen(
                        fragment = AddEditFlowFragment.newInstance(),
                        tag = AddEditFlowFragment::class.java.simpleName
                    )
                }
                QuickActionHome.GLANCE.ordinal -> {
                    (requireActivity() as MainActivity).showHomeScreen(HomeScreen.GLANCE.ordinal)
                }
                QuickActionHome.TODAY.ordinal -> {
                    (requireActivity() as MainActivity).showHomeScreen(HomeScreen.TODAY.ordinal)
                }
                QuickActionHome.NOTIFICATIONS.ordinal -> {
                    requireContext().showNotificationDrawer()
                }
                QuickActionHome.UNIVERSAL_SEARCH.ordinal -> {
                    (requireActivity() as AppCompatActivity).showScreen(
                        fragment = UniversalSearchFragment.newInstance(),
                        tag = UniversalSearchFragment::class.java.simpleName
                    )
                }
                QuickActionHome.PHONE.ordinal -> {
                    Intent(Intent.ACTION_DIAL).launchAppIfExists(requireActivity())
                }
                QuickActionHome.SMS.ordinal -> {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("sms:")
                    }
                    intent.launchAppIfExists(requireActivity())
                }
                QuickActionHome.CAMERA.ordinal -> {
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).launchAppIfExists(requireActivity())
                }
            }
        }
    }

    private fun showProgress(isShow: Boolean) {
//        if (homeAppsAdapter.homeAppList.isNotEmpty()) return
        binding.layoutShimmerAppLoader.root.isVisible = isShow
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status: Int ->
            if (status == TextToSpeech.SUCCESS) {
                val result: Int = textToSpeech.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    println("Language not supported for Text-to-Speech!")
                }
            }
        }
        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                println("Started reading $utteranceId")
            }

            override fun onDone(utteranceId: String) {
                println("Finished reading $utteranceId")
            }

            override fun onError(utteranceId: String) {
                println("Error reading $utteranceId")
            }
        })
    }

    private fun startTextToSpeech(textToSpeak: String) {
        val params = Bundle().apply { putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, textToSpeak /* utteranceId = */) }
        textToSpeech.apply {
            speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, textToSpeak)
            playSilentUtterance(1000, TextToSpeech.QUEUE_ADD, textToSpeak) // Stay silent for 1000 ms
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun webViewTest() {
        binding.webview.apply {
            isVisible = false
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            addJavascriptInterface(
                TwitterJavaScriptInterface(),
                "HTMLOUT"
            ) // Register a new JavaScript interface called HTMLOUT
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    // This call inject JavaScript into the page which just finished loading.
                    // loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');")
                    loadUrl("javascript:window.HTMLOUT.processHTML('HTMLOUT'+document.getElementsByTagName('html')[0].innerHTML);")
                    view.saveWebArchive(/*fileName*/ requireContext().internalFilesDir(fileName = "trendingTweets").absolutePath)
                }
            }
            loadUrl("https://twitter.com/explore/tabs/trending")
        }
    }

    var twitterTrendingHtml: String? = null

    inner class TwitterJavaScriptInterface {
        @JavascriptInterface
        fun processHTML(html: String?) {
            twitterTrendingHtml = html
            println("twitterTrendingHtml: $twitterTrendingHtml")
        }
    }
}