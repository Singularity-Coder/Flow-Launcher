package com.singularitycoder.flowlauncher.today.view

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.work.*
import coil.load
import com.google.android.material.chip.Chip
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.SharedViewModel
import com.singularitycoder.flowlauncher.addEditMedia.view.AddFragment
import com.singularitycoder.flowlauncher.databinding.FragmentTodayBinding
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.constants.*
import com.singularitycoder.flowlauncher.today.model.News
import com.singularitycoder.flowlauncher.today.model.Quote
import com.singularitycoder.flowlauncher.today.model.TrendingTweet
import com.singularitycoder.flowlauncher.today.model.Weather
import com.singularitycoder.flowlauncher.today.worker.NewsWorker
import com.singularitycoder.flowlauncher.today.worker.TrendingTweetsWorker
import com.singularitycoder.flowlauncher.today.worker.WeatherWorker
import dagger.hilt.android.AndroidEntryPoint

// Refresh on every swipe
// Rearrangable cards

// Headlines today - Location, Category while scraping data
// Remainders today - Remind Me remainders
// My Habits - Todos today
// Perfect Me routines
// Todos - checklist, notes

// Analyze Me - daily analysis
// Trip Me - most used visual meditation

// Ask for location permission and get weathr from location

// TODO create a res.txt file for stroign references -
// https://www.htmlsymbols.xyz/alchemical-symbols
// https://jsoup.org/cookbook/extracting-data/selector-syntax

@AndroidEntryPoint
class TodayFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = TodayFragment()
    }

    private lateinit var binding: FragmentTodayBinding
    private val sharedViewModel: SharedViewModel by viewModels()
    private var newsList = listOf<News>()
    private var quoteList = listOf<Quote>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTodayBinding.inflate(inflater, container, false)
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
        parseNewsWithWorker()
        parseWeatherWithWorker()
//        parseTrendingTweetsWithWorker()
        if (quoteList.isNotEmpty()) binding.cardQuotes.performClick()
    }

    private fun FragmentTodayBinding.setupUI() {
        val html = "21°&#x1D9C;"
        tvTemperature.text = getHtmlFormattedTime(html)
        cardNews.performClick()
        setRemainders()
        listOf(
            "1. Mooodi",
            "2. Melon Musk",
            "3. Abhijit Chavda",
            "4. TRS Clips",
            "5. Praveen Mohan",
            "6. Anime Mania",
            "7. Bikaari war",
            "8. Chaka Tak Chak",
            "9. Icecream Fry",
            "10. Chocolate Noodles"
        ).forEach { it: String ->
            val chip = Chip(requireContext()).apply {
                text = it
                isCheckable = false
                isClickable = false
                chipBackgroundColor = ColorStateList.valueOf(requireContext().color(R.color.black_50))
//                setTextColor(nnContext.color(R.color.purple_500))
                elevation = 0f
                onSafeClick {
                }
            }
            chipGroupTrendingTweets.addView(chip)
        }
    }

    private fun FragmentTodayBinding.setupUserActionListeners() {
        btnMenu.onSafeClick { it: Pair<View?, Boolean> ->
            it.first ?: return@onSafeClick
            val todayOptions = listOf("Add Remainders", "Add Quotes")
            requireContext().showPopup(
                view = it.first!!,
                menuList = todayOptions
            ) { position: Int ->
                when (todayOptions[position]) {
                    todayOptions[0] -> {
                        root.showSnackBar(todayOptions[0])
                    }
                    todayOptions[1] -> {
                        (requireActivity() as? MainActivity)?.showScreen(AddFragment.newInstance(AddItemType.QUOTE), FragmentsTag.ADD_ITEM, isAdd = true)
                        root.showSnackBar(todayOptions[1])
                    }
                }
            }
        }

        setOnQuoteClickListener()

        var newsPosition = 0
        var newsImagePosition = 0
        cardNews.onSafeClick {
            val calculatedNewsPosition = if (newsPosition == newsList.size) {
                newsPosition = 0
                newsPosition
            } else newsPosition
            val calculatedNewsImagePosition = if (newsImagePosition == typefaceList.size) {
                newsImagePosition = 0
                newsImagePosition
            } else newsImagePosition
            // TODO get actual news image.
            ivNewsImage.load(tempImageDrawableList[calculatedNewsImagePosition]) {
                placeholder(R.color.black)
            }
            val source = if (newsList[calculatedNewsPosition].source.isNullOrBlank()) {
                newsList[calculatedNewsPosition].link?.substringAfter("//")?.substringBefore("/")?.replace("www.", "")
            } else {
                newsList[calculatedNewsPosition].source
            }
            tvSource.text = "$source  \u2022  ${newsList[calculatedNewsPosition].time}"
            tvTitle.text = newsList[calculatedNewsPosition].title
            btnFullStory.onSafeClick {
                requireActivity().openWithChrome(url = newsList[calculatedNewsPosition].link ?: "")
            }
            newsPosition++
            newsImagePosition++
        }

        cardWeather.onSafeClick {
            requireActivity().searchWithChrome(query = "weather")
        }
    }

    private fun FragmentTodayBinding.observeForData() {
        sharedViewModel.weatherLiveData.observe(viewLifecycleOwner) { it: Weather? ->
            it ?: kotlin.run {
                cardWeather.isVisible = false
                return@observe
            }
            ivWeather.load(it.imageUrl) {
                placeholder(com.singularitycoder.flowlauncher.R.drawable.ic_baseline_cloud_24)
            }
            tvLocation.text = it.location
            tvWeatherCondition.text = "${it.condition} @ ${it.dateTime?.substringAfter(",")?.trim()}"
            tvTemperature.text = getHtmlFormattedTime(html = "${it.temperature}°&#x1D9C;")
        }
        sharedViewModel.newsListLiveData.observe(viewLifecycleOwner) { it: List<News>? ->
            newsList = it ?: kotlin.run {
                cardNews.isVisible = false
                emptyList()
            }
        }
        sharedViewModel.trendingTweetListLiveData.observe(viewLifecycleOwner) { it: List<TrendingTweet>? ->
            it ?: kotlin.run {
                cardTwitterTrending.isVisible = false
                return@observe
            }
        }
        sharedViewModel.quoteListLiveData.observe(viewLifecycleOwner) { it: List<Quote>? ->
            quoteList = it?.ifEmpty {
                allQuotesList
            } ?: emptyList()
            quotePosition = 0
            gradientPosition = 0
            newsTypefacePosition = 0
            cardQuotes.performClick()
        }
    }

    private var quotePosition = 0
    private var gradientPosition = 0
    private var newsTypefacePosition = 0
    private fun FragmentTodayBinding.setOnQuoteClickListener() {
        cardQuotes.onSafeClick {
            val calculatedQuotePosition = if (quotePosition == quoteList.size) {
                quotePosition = 0
                quotePosition
            } else quotePosition
            val calculatedGradientPosition = if (gradientPosition == gradientList.size) {
                gradientPosition = 0
                gradientPosition
            } else gradientPosition
            val calculatedTypefacePosition = if (newsTypefacePosition == typefaceList.size) {
                newsTypefacePosition = 0
                newsTypefacePosition
            } else newsTypefacePosition
            // TODO replace this with db list
            tvQuote.text = "${quoteList[calculatedQuotePosition].title}\n\n- ${quoteList[calculatedQuotePosition].author}"
            tvQuote.setTextColor(requireContext().color(quoteColorList[calculatedGradientPosition].textColor))
            tvQuote.setTypeface(requireContext(), typefaceList[calculatedTypefacePosition])
            clQuotes.background = requireContext().drawable(quoteColorList[calculatedGradientPosition].gradientColor)
            ivQuoteBackground.imageTintList = ColorStateList.valueOf(requireContext().color(quoteColorList[calculatedGradientPosition].iconColor))
            quotePosition++
            gradientPosition++
            newsTypefacePosition++
        }

        cardQuotes.setOnLongClickListener {
            val quoteContextPosition = quotePosition - 1
            if (quoteList[quoteContextPosition].context.isBlank()) return@setOnLongClickListener false
            requireContext().showAlertDialog(
                title = "Context",
                message = quoteList[quoteContextPosition].context,
                positiveBtnText = "Ok"
            )
            true
        }
    }

    private fun FragmentTodayBinding.setRemainders() {
        remainder1.apply {
            tvKey.text = "Sell mangoes to mango guy to get money for buying mangoes."
            tvValue.text = "8:45 AM"
        }
        remainder2.apply {
            tvKey.text = "Climb mount everest."
            tvValue.text = "11:00 AM"
        }
        remainder3.apply {
            tvKey.text = "Call Chacha Chaudhary."
            tvValue.text = "3:45 PM"
            divider.isVisible = false
        }
    }

    private fun showProgress(show: Boolean) {
        binding.progressCircular.isVisible = show
        binding.btnMenu.isVisible = show.not()
    }

    private fun parseWeatherWithWorker() {
        val workManager = WorkManager.getInstance(requireContext())
        val workConstraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest = OneTimeWorkRequestBuilder<WeatherWorker>()
            .setConstraints(workConstraints)
            .build()
        workManager.enqueueUniqueWork(WorkerTag.WEATHER_PARSER, ExistingWorkPolicy.REPLACE, workRequest)
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

    private fun parseNewsWithWorker() {
        val workManager = WorkManager.getInstance(requireContext())
        val workConstraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest = OneTimeWorkRequestBuilder<NewsWorker>()
            .setConstraints(workConstraints)
            .build()
        workManager.enqueueUniqueWork(WorkerTag.NEWS_PARSER, ExistingWorkPolicy.REPLACE, workRequest)
        workManager.getWorkInfoByIdLiveData(workRequest.id).observe(this) { workInfo: WorkInfo? ->
            when (workInfo?.state) {
                WorkInfo.State.RUNNING -> {
                    println("RUNNING: show Progress")
                    showProgress(true)
                }
                WorkInfo.State.ENQUEUED -> println("ENQUEUED: show Progress")
                WorkInfo.State.SUCCEEDED -> {
                    println("SUCCEEDED: showing Progress")
                    val isWorkComplete = workInfo.outputData.getBoolean(KEY_IS_WORK_COMPLETE, false)
                    if (isWorkComplete) {
//                        WorkManager.getInstance(requireContext()).cancelAllWorkByTag(WorkerTag.NEWS_PARSER)
                    }
                    binding.cardNews.performClick()
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

    private fun parseTrendingTweetsWithWorker() {
        val workManager = WorkManager.getInstance(requireContext())
        val workConstraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest = OneTimeWorkRequestBuilder<TrendingTweetsWorker>()
            .setConstraints(workConstraints)
            .build()
        workManager.enqueueUniqueWork(WorkerTag.TRENDING_TWEETS_PARSER, ExistingWorkPolicy.REPLACE, workRequest)
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