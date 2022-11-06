package com.singularitycoder.flowlauncher.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.work.*
import com.singularitycoder.flowlauncher.SharedViewModel
import com.singularitycoder.flowlauncher.databinding.FragmentGlanceBinding
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.model.Holiday
import com.singularitycoder.flowlauncher.worker.PublicHolidaysWorker
import dagger.hilt.android.AndroidEntryPoint

// Refresh on every swipe
// Rearrangable cards

// Image or video glances - ability to add own images
// Unread message count
// Missed calls
// next 3 Remainders/Events

// Fav Youtube videos links
// 10k Hours - top 3 skills
// Perfect Me - routines
// My Goals - Top 3 goals

// My Mind - pinned notes
// My Loans
// My Expenses
// My Bills

// TODO ideal to have viewpagers for naviagting through images and other widget stuff but too much work. Do it when u r bored
// TODO top 4 or 5 contacts u will message and call - problem is another recyclerview ...the pain

@AndroidEntryPoint
class GlanceFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = GlanceFragment()
    }

    private val sharedViewModel: SharedViewModel by viewModels()
    private lateinit var binding: FragmentGlanceBinding

    private val callSmsPermissionsResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions: Map<String, @JvmSuppressWildcards Boolean>? ->
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
        if (requireContext().isCallContactSmsPermissionGranted()) {
            binding.layoutUnreadSms.tvValue.text = requireContext().unreadSmsCount().toString()
            binding.layoutMissedCalls.tvValue.text = requireContext().missedCallCount().toString()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGlanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        binding.observeForData()
    }

    override fun onResume() {
        super.onResume()
        callSmsPermissionsResult.launch(callContactSmsPermissionList)
        val lastHolidayFetchTime = Preferences.read(requireContext()).getLong(Preferences.KEY_LAST_HOLIDAYS_FETCH_TIME, timeNow - TWENTY_FOUR_HOURS_IN_MILLIS.toLong() - /* grace 3k mills */ 3000)
        if (timeNow > lastHolidayFetchTime + TWENTY_FOUR_HOURS_IN_MILLIS) {
            parsePublicHolidaysWithWorker()
        }
    }

    private fun FragmentGlanceBinding.setupUI() {
        ivGlanceImage.layoutParams.height = deviceWidth() - 32.dpToPx()
        tvImageCount.text = "${1}/${tempImageDrawableList.size}"
        setupRemaindersCard()
        setupTakeActionCard()
    }

    private fun FragmentGlanceBinding.setupUserActionListeners() {
        var currentImagePosition = 0
        ivGlanceImage.setOnClickListener {
            doAfter(3.seconds()) {
                cardImageCount.isVisible = false
            }
            cardImageCount.isVisible = true
            tvImageCount.text = "${currentImagePosition + 1}/${tempImageDrawableList.size}"
            ivGlanceImage.setImageDrawable(requireContext().drawable(tempImageDrawableList[currentImagePosition]))
            if (currentImagePosition == tempImageDrawableList.lastIndex) {
                currentImagePosition = 0
            } else {
                currentImagePosition++
            }
        }
        ivGlanceImage.setOnLongClickListener {
            // Add or remove images. new bottom sheet
            false
        }
        cardYoutubeVideos.setOnClickListener {
            // Open youtube video in new screen. Auto oriented horizontally
        }
        cardYoutubeVideos.setOnLongClickListener {
            // Add or remove youtube video. new bottom sheet
            false
        }
        btnMenu.setOnClickListener { view: View? ->
            view ?: return@setOnClickListener
            val glanceOptions = listOf("Add Media", "Add Remainders", "Add Youtube Videos")
            requireContext().showPopup(
                view = view,
                menuList = glanceOptions
            ) { position: Int ->
                when (glanceOptions[position]) {
                    glanceOptions[0] -> {
                        root.showSnackBar(glanceOptions[0])
                    }
                    glanceOptions[1] -> {
                        root.showSnackBar(glanceOptions[1])
                    }
                    glanceOptions[2] -> {
                        root.showSnackBar(glanceOptions[2])
                    }
                }
            }
        }
        binding.layoutUnreadSms.root.setOnClickListener {
            // TODO fix this
            requireContext().sendSms("", "")
        }
        binding.layoutMissedCalls.root.setOnClickListener {
            // TODO fix this
            requireContext().openDialer("")
        }
    }

    private fun FragmentGlanceBinding.observeForData() {
        sharedViewModel.holidayListLiveData.observe(viewLifecycleOwner) { it: List<Holiday>? ->
            if (it.isNullOrEmpty()) return@observe
            updateHolidaysView(it)
        }
    }

    private fun FragmentGlanceBinding.updateHolidaysView(it: List<Holiday>) {
        tvHolidaysPlaceholder.apply {
            text = "${it.getOrNull(1)?.header} | ${it.firstOrNull()?.location}"
            setOnClickListener {
                requireActivity().searchWithChrome(query = "public+holidays")
            }
        }

        val holidaysInUnixDatesList = try {
            it.filter { holiday: Holiday ->
                val year = it.getOrNull(1)?.header?.substringAfter("(")?.replace(")", "")?.trim()
                val monthDay = holiday.date?.toFormattedHolidayDate()
                val newDate = "$monthDay, $year"
                val unixDate = try {
                    convertDateToLong(newDate, DateType.MMM_d_yyyy.value)
                } catch (e: Exception) {
                    0L
                }
                unixDate > timeNow
            }.sortedBy { holiday: Holiday -> holiday.date }
        } catch (e: Exception) {
            println("excep: ${e.message}")
            emptyList()
        }
        println("date list: $holidaysInUnixDatesList")

        when {
            holidaysInUnixDatesList.isEmpty() -> {
                cardPublicHolidays.isVisible = false
            }
            holidaysInUnixDatesList.size < 2 -> {
                cardPublicHolidays.isVisible = true
                holiday1.apply {
                    root.isVisible = true
                    divider.isVisible = false
                }
                holiday2.root.isVisible = false
                holiday3.root.isVisible = false
                holiday1.apply {
                    tvKey.text = holidaysInUnixDatesList.getOrNull(0)?.title
                    tvValue.text = holidaysInUnixDatesList.getOrNull(0)?.date?.toFormattedHolidayDate()
                    root.setOnClickListener { v: View? ->
                        val link = holidaysInUnixDatesList.getOrNull(0)?.link?.replace("/search?q=", "").toString()
                        requireActivity().searchWithChrome(query = link)
                    }
                }
            }
            holidaysInUnixDatesList.size < 3 -> {
                cardPublicHolidays.isVisible = true
                holiday1.apply {
                    root.isVisible = true
                    divider.isVisible = true
                }
                holiday2.apply {
                    root.isVisible = true
                    divider.isVisible = false
                }
                holiday3.root.isVisible = false
                holiday2.apply {
                    tvKey.text = holidaysInUnixDatesList.getOrNull(1)?.title
                    tvValue.text = holidaysInUnixDatesList.getOrNull(1)?.date?.toFormattedHolidayDate()
                    root.setOnClickListener { v: View? ->
                        val link = holidaysInUnixDatesList.getOrNull(1)?.link?.replace("/search?q=", "").toString()
                        requireActivity().searchWithChrome(query = link)
                    }
                }
            }
            holidaysInUnixDatesList.size < 4 -> {
                cardPublicHolidays.isVisible = true
                holiday1.apply {
                    root.isVisible = true
                    divider.isVisible = true
                }
                holiday2.apply {
                    root.isVisible = true
                    divider.isVisible = true
                }
                holiday3.root.isVisible = true
                holiday3.apply {
                    tvKey.text = holidaysInUnixDatesList.getOrNull(2)?.title
                    tvValue.text = holidaysInUnixDatesList.getOrNull(2)?.date?.toFormattedHolidayDate()
                    divider.isVisible = false
                    root.setOnClickListener { v: View? ->
                        val link = holidaysInUnixDatesList.getOrNull(2)?.link?.replace("/search?q=", "").toString()
                        requireActivity().searchWithChrome(query = link)
                    }
                }
            }
        }
    }

    private fun FragmentGlanceBinding.setupTakeActionCard() {
        layoutMissedCalls.apply {
            tvKey.text = "Missed Calls"
            tvValue.text = "14"
        }
        layoutUnreadSms.apply {
            tvKey.text = "Unread SMS"
            tvValue.text = "293"
            divider.isVisible = false
        }
    }

    private fun FragmentGlanceBinding.setupRemaindersCard() {
        remainder1.apply {
            tvKey.text = "Sell mangoes to mango guy to get money for buying mangoes."
            tvValue.text = "12 SPT"
        }
        remainder2.apply {
            tvKey.text = "Climb mount everest."
            tvValue.text = "6 NOV"
        }
        remainder3.apply {
            tvKey.text = "Call Chacha Chaudhary."
            tvValue.text = "1 JAN"
            divider.isVisible = false
        }
    }

    private fun showProgress(show: Boolean) {
        binding.progressCircular.isVisible = show
        binding.btnMenu.isVisible = show.not()
    }

    private fun parsePublicHolidaysWithWorker() {
        val workManager = WorkManager.getInstance(requireContext())
        val workConstraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest = OneTimeWorkRequestBuilder<PublicHolidaysWorker>()
            .setConstraints(workConstraints)
            .build()
        workManager.enqueueUniqueWork(WorkerTag.PUBLIC_HOLIDAYS_PARSER, ExistingWorkPolicy.REPLACE, workRequest)
        workManager.getWorkInfoByIdLiveData(workRequest.id).observe(this) { workInfo: WorkInfo? ->
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
}