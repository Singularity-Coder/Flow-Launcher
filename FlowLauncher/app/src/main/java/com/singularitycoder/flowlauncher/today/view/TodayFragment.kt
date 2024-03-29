package com.singularitycoder.flowlauncher.today.view

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import coil.load
import com.google.android.material.chip.Chip
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.SharedViewModel
import com.singularitycoder.flowlauncher.addEditAppFlow.view.AppSelectorBottomSheetFragment
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        btnMenu.onSafeClick { pair: Pair<View?, Boolean> ->
            pair.first ?: return@onSafeClick
            val optionsList = listOf(
                Pair("Add Remainders", R.drawable.outline_alarm_24),
                Pair("Add Quotes", R.drawable.outline_format_quote_24),
            )
            requireContext().showPopupMenuWithIcons(
                view = pair.first,
                menuList = optionsList
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {
                        root.showSnackBar(optionsList[0].first)
                    }
                    optionsList[1].first -> {
                        (requireActivity() as? MainActivity)?.showScreen(AddFragment.newInstance(AddItemType.QUOTE), FragmentsTag.ADD_ITEM, isAdd = true)
                    }
                }
            }
        }

        setOnQuoteClickListener()

        var newsPosition = 0
        var newsImagePosition = 0
        cardNews.onSafeClick {
            val calculatedNewsPosition = if (newsPosition == newsList.lastIndex) {
                newsPosition = 0
                newsPosition
            } else newsPosition
            val calculatedNewsImagePosition = if (newsImagePosition == tempImageDrawableList.lastIndex) {
                newsImagePosition = 0
                newsImagePosition
            } else newsImagePosition
            // TODO get actual news image.
            ivNewsImage.load(tempImageDrawableList.getOrNull(calculatedNewsImagePosition)) {
                placeholder(R.color.black)
            }
            val source = if (newsList.getOrNull(calculatedNewsPosition)?.source.isNullOrBlank()) {
                newsList.getOrNull(calculatedNewsPosition)?.link?.substringAfter("//")?.substringBefore("/")?.replace("www.", "")
            } else {
                newsList.getOrNull(calculatedNewsPosition)?.source
            }
            tvSource.text = "$source  \u2022  ${newsList.getOrNull(calculatedNewsPosition)?.time}"
            tvTitle.text = newsList.getOrNull(calculatedNewsPosition)?.title
            btnFullStory.onSafeClick {
                requireActivity().showWebPage(url = newsList.getOrNull(calculatedNewsPosition)?.link ?: "")
            }
            newsPosition++
            newsImagePosition++
        }

        cardWeather.onSafeClick {
            requireActivity().showWebPage(url = "https://www.google.com/search?q=weather")
        }
    }

    private fun FragmentTodayBinding.observeForData() {
        sharedViewModel.weatherLiveData.observe(viewLifecycleOwner) { it: Weather? ->
            it ?: kotlin.run {
                cardWeather.isVisible = false
                return@observe
            }
            cardWeather.isVisible = true
            ivWeather.load(it.imageUrl) {
                placeholder(R.drawable.ic_baseline_cloud_24)
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
            tvQuote.text = "${quoteList.getOrNull(calculatedQuotePosition)?.title}\n\n- ${quoteList.getOrNull(calculatedQuotePosition)?.author}"
            tvQuote.setTextColor(requireContext().color(quoteColorList.getOrNull(calculatedGradientPosition)?.textColor ?: R.color.purple_500))
            tvQuote.setTypeface(requireContext(), typefaceList.getOrNull(calculatedTypefacePosition) ?: R.font.milkshake)
            clQuotes.background = requireContext().drawable(quoteColorList.getOrNull(calculatedGradientPosition)?.gradientColor ?: R.color.purple_50)
            ivQuoteBackground.imageTintList = ColorStateList.valueOf(requireContext().color(quoteColorList.getOrNull(calculatedGradientPosition)?.iconColor ?: R.color.purple_50))
            quotePosition++
            gradientPosition++
            newsTypefacePosition++
        }

        cardQuotes.onCustomLongClick {
            val quoteContextPosition = if (quotePosition == 0) {
                quoteList.lastIndex
            } else quotePosition - 1
            if (quoteList.getOrNull(quoteContextPosition)?.context?.isBlank() == true) return@onCustomLongClick
            requireContext().showAlertDialog(
                title = "Context",
                message = quoteList.getOrNull(quoteContextPosition)?.context ?: "",
                positiveBtnText = "Ok"
            )
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