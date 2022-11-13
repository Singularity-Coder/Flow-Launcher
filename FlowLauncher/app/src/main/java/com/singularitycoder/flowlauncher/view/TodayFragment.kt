package com.singularitycoder.flowlauncher.view

import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import coil.load
import com.google.android.material.chip.Chip
import com.singularitycoder.flowlauncher.SharedViewModel
import com.singularitycoder.flowlauncher.databinding.FragmentTodayBinding
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.blur.BlurBox
import com.singularitycoder.flowlauncher.helper.blur.BlurEngine
import com.singularitycoder.flowlauncher.helper.blur.BlurStackOptimized
import com.singularitycoder.flowlauncher.model.News
import com.singularitycoder.flowlauncher.model.TrendingTweet
import com.singularitycoder.flowlauncher.model.Weather
import com.singularitycoder.flowlauncher.worker.NewsWorker
import com.singularitycoder.flowlauncher.worker.TrendingTweetsWorker
import com.singularitycoder.flowlauncher.worker.WeatherWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

// Refresh on every swipe
// Rearrangable cards

// Add Quote of the day
// Weather today
// Headlines today - Location, Category while scraping data
// Remainders today - Remind Me remainders
// My Habits - Todos today
// Perfect Me routines
// Todos - checklist, notes

// Analyze Me - daily analysis
// Trip Me - most used visual meditation

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
            "4. Anime Mania",
            "5. Bikaari war",
            "6. Jump Jump",
            "7. Abo Dabooo",
            "8. Chaka Tak Chak",
            "9. Icecream Fry",
            "10. Chocolate Noodles"
        ).forEach { it: String ->
            val chip = Chip(requireContext()).apply {
                text = it
                isCheckable = false
                isClickable = false
                chipBackgroundColor = ColorStateList.valueOf(requireContext().color(com.singularitycoder.flowlauncher.R.color.black_50))
//                setTextColor(nnContext.color(R.color.purple_500))
                elevation = 0f
                setOnClickListener {
                }
            }
            chipGroupTrendingTweets.addView(chip)
        }
    }

    private fun FragmentTodayBinding.setupUserActionListeners() {
        btnMenu.setOnClickListener { view: View? ->
            view ?: return@setOnClickListener
            val todayOptions = listOf("Add Remainders", "Add Quotes")
            requireContext().showPopup(
                view = view,
                menuList = todayOptions
            ) { position: Int ->
                when (todayOptions[position]) {
                    todayOptions[0] -> {
                        root.showSnackBar(todayOptions[0])
                    }
                    todayOptions[1] -> {
                        root.showSnackBar(todayOptions[1])
                    }
                }
            }
        }

        var quotePosition = 0
        var gradientPosition = 0
        var newsTypefacePosition = 0
        cardQuotes.setOnClickListener {
            val calculatedQuotePosition = if (quotePosition == animeQuoteList.size) {
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
            tvQuote.text = "${animeQuoteList[calculatedQuotePosition].quote}\n\n- ${animeQuoteList[calculatedQuotePosition].author}"
            tvQuote.setTextColor(requireContext().color(quoteColorList[calculatedGradientPosition].textColor))
            tvQuote.typeface(requireContext(), typefaceList[calculatedTypefacePosition])
            clQuotes.background = requireContext().drawable(quoteColorList[calculatedGradientPosition].gradientColor)
            ivQuoteBackground.imageTintList = ColorStateList.valueOf(requireContext().color(quoteColorList[calculatedGradientPosition].iconColor))
            quotePosition++
            gradientPosition++
            newsTypefacePosition++
        }

        var newsPosition = 0
        var newsImagePosition = 0
        val blurEngine = BlurStackOptimized()
        cardNews.setOnClickListener {
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
                placeholder(com.singularitycoder.flowlauncher.R.color.black)
            }
            val source = if (newsList[calculatedNewsPosition].source.isNullOrBlank()) {
                newsList[calculatedNewsPosition].link?.substringAfter("//")?.substringBefore("/")?.replace("www.", "")
            } else {
                newsList[calculatedNewsPosition].source
            }
            tvSource.text = "$source  \u2022  ${newsList[calculatedNewsPosition].time}"
            tvTitle.text = newsList[calculatedNewsPosition].title
            btnFullStory.setOnClickListener {
                requireActivity().openWithChrome(url = newsList[calculatedNewsPosition].link ?: "")
            }
            lifecycleScope.launch {
                val radius = 5
//                val measureTime = measureTimeMillis {
//                    val bitmapToBlur = (ivNewsImage.drawable as BitmapDrawable).bitmap
//                    val blurredBitmap = blurEngine.blur(bitmapToBlur, radius)
//                    doAfter(10.seconds()) {
//                        ivNewsImage.setImageBitmap(blurredBitmap)
//                    }
//                }
//                println("Time $measureTime ms with Radius: $radius using ${blurEngine.getType()}")
            }
            newsPosition++
            newsImagePosition++
        }

        cardWeather.setOnClickListener {
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