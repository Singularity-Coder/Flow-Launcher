package com.singularitycoder.flowlauncher.view

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.singularitycoder.flowlauncher.appList
import com.singularitycoder.flowlauncher.databinding.FragmentHomeBinding
import com.singularitycoder.flowlauncher.db.ContactDao
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.launchApp
import com.singularitycoder.flowlauncher.model.App
import com.singularitycoder.flowlauncher.model.Contact
import com.singularitycoder.flowlauncher.uninstallApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject


// Maybe option to change color
// 12 hr, 24 hr clock
// Time listener
// Fav Walls - My Wall - set of 10 fav images - Glance
// FIXME the grid is still not center positioned
// Letter strip for app search
// Probably some kind of doc for quick access of commonly used apps.

// Pre built commands for voice search
// 1. OPEN app_name -> opens app, etc
// 2. CALL contact_name -> calls directly
// 3. MESSAGE contact_name SAYING message_body  -> Opens message
// 4. SEARCH FOR query -> Open chrome

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

    private val homeAppsList = mutableListOf<App>()
    private val homeAppsAdapter = HomeAppsAdapter()

    private var speechAction = SpeechAction.NONE
    private var contactName = ""
    private var messageBody = ""

    // FIXME not working
    private val timeChangedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action != BROADCAST_TIME_CHANGED) return
            setTimeDateAndFlow()
        }
    }

    private val contactsPermissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isPermissionGranted: Boolean? ->
        isPermissionGranted ?: return@registerForActivityResult
        if (isPermissionGranted.not()) {
            requireContext().showPermissionSettings()
            return@registerForActivityResult
        }
        CoroutineScope(IO).launch {
            val isContactsSynced = Preferences.read(requireContext()).getBoolean(Preferences.KEY_IS_CONTACTS_SYNCED, false)
            if (isContactsSynced.not()) {
                requireContext().getContacts().sortedBy { it.name }.forEach { it: Contact ->
                    contactDao.insert(it)
                }
            }
            Preferences.write(requireContext()).putBoolean(Preferences.KEY_IS_CONTACTS_SYNCED, true).apply()
            val contact = contactDao.getAll().firstOrNull { it.name.contains(contactName) }
            when (speechAction) {
                SpeechAction.CALL -> {
                    requireContext().makeCall(contact?.mobileNumber ?: "")
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
        CoroutineScope(IO).launch {
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
                    grantContactsPermissions()
                }
                SpeechAction.MESSAGE.value -> {
                    speechAction = SpeechAction.MESSAGE
                    contactName = text.substringAfter(" ").substringBefore("saying")
                    messageBody = text.substringAfter("saying ")
                    grantContactsPermissions()
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
        binding.setupUI()
        binding.setupUserActionListeners()
    }

    override fun onResume() {
        super.onResume()
        println("This triggers everytime we switch the screen")
        activity?.registerReceiver(timeChangedReceiver, IntentFilter(BROADCAST_TIME_CHANGED))
    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(timeChangedReceiver)
    }

    private fun FragmentHomeBinding.setupUI() {
        setTimeDateAndFlow()
        timer = Timer()
        timer.doEvery(
            duration = 1.seconds(),
            withInitialDelay = 0.seconds(),
        ) {
            setTimeDateAndFlow()
        }
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
        homeAppsList.addAll(requireContext().appList().sortedBy { it.title })
        homeAppsAdapter.apply {
            this.homeAppList = this@HomeFragment.homeAppsList
            notifyDataSetChanged()
        }
    }

    private fun FragmentHomeBinding.setupUserActionListeners() {
        homeAppsAdapter.setItemClickListener { app, position ->
            requireActivity().launchApp(app.packageName)
        }
        homeAppsAdapter.setItemLongClickListener { app, position ->
            // Show menu - Info, Delete
            requireContext().showAlertDialog(
                title = app.title,
                message = "Do you want to remove this app?",
                positiveAction = {
                    CoroutineScope(IO).launch {
                        // FIXME uninstall callback
                        requireActivity().uninstallApp(app.packageName)
                        withContext(Main) {
                            homeAppsAdapter.notifyItemRemoved(position)
                        }
                    }
                }
            )
        }
        root.setOnLongClickListener {
            // Open flow switcher
            false
        }
        fabProfile.setOnClickListener {
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
    }

    private fun setTimeDateAndFlow() {
        CoroutineScope(IO).launch {
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
                binding.tvFlowType.text = "$day, ${convertLongToTime(timeNow, DateType.dd_MMM_yyyy)}  |  Work Flow"
            }
        }
    }

    private fun grantContactsPermissions() {
        contactsPermissionResult.launch(Manifest.permission.READ_CONTACTS)
    }
}