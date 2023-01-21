package com.singularitycoder.flowlauncher.home.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
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
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.*
import coil.ImageLoader
import coil.request.ImageRequest
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.addEditAppFlow.model.AppFlow
import com.singularitycoder.flowlauncher.addEditAppFlow.view.AddEditFlowFragment
import com.singularitycoder.flowlauncher.addEditAppFlow.viewModel.AppFlowViewModel
import com.singularitycoder.flowlauncher.databinding.FragmentHomeBinding
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.blur.BlurStackOptimized
import com.singularitycoder.flowlauncher.helper.constants.*
import com.singularitycoder.flowlauncher.helper.quickactionview.Action
import com.singularitycoder.flowlauncher.helper.quickactionview.QuickActionView
import com.singularitycoder.flowlauncher.home.dao.ContactDao
import com.singularitycoder.flowlauncher.home.model.App
import com.singularitycoder.flowlauncher.home.model.Contact
import com.singularitycoder.flowlauncher.home.viewmodel.HomeViewModel
import com.singularitycoder.flowlauncher.home.worker.AppWorker
import com.singularitycoder.flowlauncher.quickSettings.QuickSettingsBottomSheetFragment
import com.singularitycoder.flowlauncher.toBitmapOf
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import javax.inject.Inject


// Maybe option to change color
// 12 hr, 24 hr clock
// Time listener
// FIXME align app grid to the edges
// Letter strip for app search
// Probably some kind of doc for quick access of commonly used apps.

// TODO Unable to detect package installed. Check further - https://stackoverflow.com/questions/11392183/how-to-check-programmatically-if-an-application-is-installed-or-not-in-android
// TODO store install uninstall package flags in db and listen to flag changes and then refresh screen

// SOS signal - swipe to decline in 5 sec - send message to pre selected contacts
// Notes n checklist widegt with quick add

// Universal search
// Compass in Glance
// Current Lat Long in Glance
// Steps count in Today

// Touch Effect similar to Ratio Launcher - https://developer.android.com/develop/ui/views/touch-and-input/gestures/movement
// https://guides.codepath.com/android/gestures-and-touch-events
// https://developer.android.com/develop/ui/views/touch-and-input/gestures
// On search fab touch - vertical list - show options -> voice search, change flow, notifications, quick settings, universal search
// On search fab touch - horizontal list - Phone app, sms app, camera app
// Voice commands - Brightness, alarm

// Split quick action view to a module

@AndroidEntryPoint
class HomeFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }

    @Inject
    lateinit var contactDao: ContactDao

    private lateinit var binding: FragmentHomeBinding
    private lateinit var timer: Timer

    private val homeAppsAdapter by lazy { HomeAppsAdapter() }
    private val homeViewModel: HomeViewModel by viewModels()
    private val appFlowViewModel: AppFlowViewModel by viewModels()

    private var speechAction = SpeechAction.NONE
    private var contactName = ""
    private var messageBody = ""
    private var removedAppPosition = 0
    private var removedApp: App? = null
    private var flowName: String? = ""

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                // FIXME not working
                Broadcast.TIME_CHANGED -> {
                    setTimeDateAndFlow()
                }
                Broadcast.PACKAGE_REMOVED -> {
                    homeViewModel.removeAppFromDb(removedApp)
                    // TODO remove icon as well
                    val appIconName = "app_icon_${removedApp?.packageName}".replace(oldValue = ".", newValue = "_")
                    val appIconDir = "${requireContext().filesDir?.absolutePath}/app_icons"
                    deleteBitmapFromInternalStorage(appIconName, appIconDir)
//                    homeAppsAdapter.notifyItemRemoved(removedAppPosition)
                }
                // FIXME not working
                Broadcast.PACKAGE_INSTALLED -> {
                    refreshAppList()
                }
            }
        }
    }

    private val contactsPermissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isPermissionGranted: Boolean? ->
        isPermissionGranted ?: return@registerForActivityResult
        if (isPermissionGranted.not()) {
            requireContext().showPermissionSettings()
            return@registerForActivityResult
        }
        lifecycleScope.launch {
            val isContactsSynced = Preferences.read(requireContext())
                .getBoolean(Preferences.KEY_IS_CONTACTS_SYNCED, false)
            if (isContactsSynced.not()) {
                requireContext().getContacts().sortedBy { it.name }.forEach { it: Contact ->
                    contactDao.insert(it)
                }
            }
            Preferences.write(requireContext())
                .putBoolean(Preferences.KEY_IS_CONTACTS_SYNCED, true).apply()
            val contact = contactDao.getAll().firstOrNull { it.name.contains(contactName) }
            when (speechAction) {
                SpeechAction.CALL -> {
                    requireContext().openDialer(contact?.mobileNumber ?: "")
                }
                SpeechAction.MESSAGE -> {
                    requireContext().sendSms(contact?.mobileNumber ?: "", messageBody)
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
            val text =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
                    ?.trim()
            println("speech result: $text")
            when (text?.substringBefore(" ")?.toLowCase()) {
                SpeechAction.OPEN.value, SpeechAction.LAUNCH.value -> {
                    // TODO get app list from DB
                    val appName = text.substringAfter(" ")
                    val app = context?.appList()?.firstOrNull { it.title.contains(appName) }
                        ?: kotlin.run {
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
        // Check if app count modified. if so then trigger apsp
//        if (homeAppsAdapter.homeAppList.size != requireContext().appInfoList().size) {
//            refreshAppList()
//        }
        println("This triggers everytime we switch the screen in viewpager")
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Broadcast.TIME_CHANGED))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Broadcast.PACKAGE_REMOVED))
        activity?.registerReceiver(broadcastReceiver, IntentFilter(Broadcast.PACKAGE_INSTALLED))
    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(broadcastReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.purge()
    }

    private fun FragmentHomeBinding.setupUI() {
        setTimeDateAndFlow()
        refreshAppList()
        refreshDateTime()
        rvApps.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = homeAppsAdapter
            addItemDecoration(
                GridSpacingItemDecoration(
                    spanCount = 4 /* columns */,
                    spacing = 24.dpToPx() /* px */,
                    includeEdge = false
                )
            )
        }
    }

    private fun FragmentHomeBinding.setupUserActionListeners() {
        homeAppsAdapter.setItemClickListener { app, position ->
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
        setTouchOptions()
        rvApps.setOnLongClickListener {
            root.performLongClick()
            false
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
            val selectedFlow = it.firstOrNull { it.isSelected }
            val isFlowNameHasFlow = selectedFlow?.appFlowName?.toLowCase()?.contains("flow") == true
            flowName = if (isFlowNameHasFlow) selectedFlow?.appFlowName else "${selectedFlow?.appFlowName} Flow"
            homeAppsAdapter.homeAppList = selectedFlow?.appList ?: emptyList()
            enableDisableApps(selectedFlow)
            withContext(Main) {
                // https://stackoverflow.com/questions/43221847/cannot-call-this-method-while-recyclerview-is-computing-a-layout-or-scrolling-wh
                homeAppsAdapter.notifyDataSetChanged()
                blurAndSaveBitmapForImageBackground()
            }
        }

//        homeViewModel.appListLiveData.observe(viewLifecycleOwner) { it: List<App>? ->
//            homeAppsAdapter.homeAppList = it ?: emptyList()
//            homeAppsAdapter.notifyDataSetChanged()
//        }
    }

    private fun enableDisableApps(selectedFlow: AppFlow?) = lifecycleScope.launch {
        val defaultFlowApps = appFlowViewModel.getAppFlowById(id = 1L)?.appList
        selectedFlow?.appList?.forEach { selectedApp: App ->
            selectedApp.enable(requireContext())
        }
        defaultFlowApps?.forEach { defaultApp: App ->
            val isDefaultAppNotPresentInSelectedApp =
                selectedFlow?.appList?.map { it.packageName }?.contains(defaultApp.packageName)
                    ?.not() == true
            if (isDefaultAppNotPresentInSelectedApp) {
                defaultApp.disable(requireContext())
            }
        }
    }

    private fun blurAndSaveBitmapForImageBackground() = lifecycleScope.launch {
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

            val time = convertLongToTime(timeNow, DateType.h_mm_a)
            val hours = time.substringBefore(":")
            val minutes = time.substringAfter(":").substringBefore(" ")
            val dayPeriod = time.substringAfter(" ")
            val html = "$hours : $minutes <small><small><small>$dayPeriod</small></small></small>"
            val day = Calendar.getInstance().time.toString().substringBefore(" ")

            withContext(Main) {
                binding.tvTime.text = getHtmlFormattedTime(html)
                binding.tvFlowType.text =
                    "$day, ${convertLongToTime(timeNow, DateType.dd_MMM_yyyy)}  |  $flowName"
            }
        }
    }

    private fun refreshDateTime() {
        timer = Timer()
        timer.doEvery(
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
                        requireContext().showAppInfo(app)
                        false
                    }
                    R.id.menu_item_remove_app -> {
                        removeApp(app, position)
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
                    it.iconTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.purple_500)
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
        workManager.getWorkInfoByIdLiveData(workRequest.id)
            .observe(viewLifecycleOwner) { workInfo: WorkInfo? ->
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

    private fun setTouchOptions() {
        // voice search, change flow, notifications, quick settings, universal search
        QuickActionView.make(requireContext()).apply {
            val icon1 = requireContext().drawable(R.drawable.ic_round_keyboard_voice_24)?.changeColor(requireContext(), R.color.purple_500)
            val action1 = Action(/* id = */ 1, /* icon = */ icon1!!, /* title = */ "Voice Search")
            val icon2 = requireContext().drawable(R.drawable.ic_round_tune_24)?.changeColor(requireContext(), R.color.purple_500)
            val action2 = Action(/* id = */ 2, /* icon = */ icon2!!, /* title = */ "Quick Settings")
            val icon3 = requireContext().drawable(R.drawable.ic_round_apps_24)?.changeColor(requireContext(), R.color.purple_500)
            val action3 = Action(/* id = */ 3, /* icon = */ icon3!!, /* title = */ "Select Flow")
            addAction(action1) // more configuring
            addAction(action2)
            addAction(action3)
            register(binding.fabVoiceSearch)
            setBackgroundColor(requireContext().color(R.color.purple_50))
            setOnActionSelectedListener { action: Action?, quickActionView: QuickActionView? ->
                when (action?.id) {
                    1 -> {
                        // Start Speech to Text
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Start Speaking Now!")
                        }
                        speechToTextResult.launch(intent)
                    }
                    2 -> {
                        if (requireContext().isWriteSettingsPermissionGranted()) {
                            QuickSettingsBottomSheetFragment.newInstance().show(
                                requireActivity().supportFragmentManager,
                                BottomSheetTag.QUICK_SETTINGS
                            )
                        }
                    }
                    3 -> {
                        blurAndSaveBitmapForImageBackground()
                        (requireActivity() as AppCompatActivity).showScreen(
                            AddEditFlowFragment.newInstance(),
                            AddEditFlowFragment::class.java.simpleName
                        )
                    }
                }
            }
        }
    }

    private fun showProgress(isShow: Boolean) {
//        if (homeAppsAdapter.homeAppList.isNotEmpty()) return
        binding.layoutShimmerAppLoader.root.isVisible = isShow
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