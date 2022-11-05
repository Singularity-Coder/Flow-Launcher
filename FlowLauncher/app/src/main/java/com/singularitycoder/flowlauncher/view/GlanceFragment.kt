package com.singularitycoder.flowlauncher.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.work.*
import coil.load
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.SharedViewModel
import com.singularitycoder.flowlauncher.databinding.FragmentGlanceBinding
import com.singularitycoder.flowlauncher.databinding.FragmentTodayBinding
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.model.Holiday
import com.singularitycoder.flowlauncher.model.News
import com.singularitycoder.flowlauncher.model.Weather
import com.singularitycoder.flowlauncher.worker.NewsWorker
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

@AndroidEntryPoint
class GlanceFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = GlanceFragment()
    }

    private val sharedViewModel: SharedViewModel by viewModels()
    private lateinit var binding: FragmentGlanceBinding

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
        parsePublicHolidaysWithWorker()
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
    }

    private fun FragmentGlanceBinding.observeForData() {
        sharedViewModel.holidayListLiveData.observe(viewLifecycleOwner) { it: List<Holiday>? ->
            tvHolidaysPlaceholder.text = "${it?.getOrNull(1)?.header} | ${it?.firstOrNull()?.location}"

            // TODO set holidays based on current date. 3 holidays from current date
            holiday1.apply {
                tvKey.text = it?.getOrNull(0)?.title
                tvValue.text = it?.getOrNull(0)?.date?.substringBefore(",")
                root.setOnClickListener { v: View? ->
                    val link = it?.getOrNull(0)?.link?.replace("/search?q=", "").toString()
                    requireActivity().searchWithChrome(query = link)
                }
            }
            holiday2.apply {
                tvKey.text = it?.getOrNull(1)?.title
                tvValue.text = it?.getOrNull(1)?.date?.substringBefore(",")
                root.setOnClickListener { v: View? ->
                    val link = it?.getOrNull(1)?.link?.replace("/search?q=", "").toString()
                    requireActivity().searchWithChrome(query = link)
                }
            }
            holiday3.apply {
                tvKey.text = it?.getOrNull(2)?.title
                tvValue.text = it?.getOrNull(2)?.date?.substringBefore(",")
                root.setOnClickListener { v: View? ->
                    val link = it?.getOrNull(0)?.link?.replace("/search?q=", "").toString()
                    requireActivity().searchWithChrome(query = link)
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