package com.singularitycoder.flowlauncher.glance.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import coil.request.ImageRequest
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.SharedViewModel
import com.singularitycoder.flowlauncher.addEditMedia.view.AddFragment
import com.singularitycoder.flowlauncher.databinding.FragmentGlanceBinding
import com.singularitycoder.flowlauncher.glance.model.GlanceImage
import com.singularitycoder.flowlauncher.glance.model.Holiday
import com.singularitycoder.flowlauncher.glance.model.YoutubeVideo
import com.singularitycoder.flowlauncher.glance.worker.PublicHolidaysWorker
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.blur.BlurStackOptimized
import com.singularitycoder.flowlauncher.helper.constants.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@AndroidEntryPoint
class GlanceFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = GlanceFragment()
    }

    private val sharedViewModel: SharedViewModel by viewModels()

    private var youtubeVideoList = listOf<YoutubeVideo>()
    private var glanceImageList = listOf<GlanceImage>()
    private var exoPlayer: ExoPlayer? = null
    private var exoPlayerExpanded: ExoPlayer? = null

    private lateinit var binding: FragmentGlanceBinding
    private lateinit var currentGlanceImage: GlanceImage

    private val glanceImageFileDir: String by lazy {
        "${requireContext().filesDir.absolutePath}/glance_images"
    }

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
        binding.observeForData()
        binding.setupUI()
        binding.setupUserActionListeners()
    }

    override fun onResume() {
        super.onResume()
        callSmsPermissionsResult.launch(callContactSmsPermissionList)
        val lastHolidayFetchTime = Preferences.read(requireContext()).getLong(Preferences.KEY_LAST_HOLIDAYS_FETCH_TIME, timeNow - THIRTY_DAYS_IN_MILLIS - /* grace 3k mills */ 3000)
        if (timeNow > lastHolidayFetchTime + THIRTY_DAYS_IN_MILLIS) {
            parsePublicHolidaysWithWorker()
        }
        if (youtubeVideoList.isNotEmpty()) binding.cardYoutubeVideos.performClick()
        if (glanceImageList.isNotEmpty()) binding.cardGlanceImages.performClick()
    }

    // https://stackoverflow.com/questions/30239627/how-to-change-the-style-of-a-datepicker-in-android
    private fun FragmentGlanceBinding.setupUI() {
        ivGlanceImage.layoutParams.height = deviceWidth() - 32.dpToPx() // 32 is 16dp padding left + 16 dp padding right
        exoPlayerView.layoutParams.height = deviceWidth() - 32.dpToPx() // 32 is 16dp padding left + 16 dp padding right
        setupRemaindersCard()
        setupTakeActionCard()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun FragmentGlanceBinding.setupUserActionListeners() {
        btnFullScreen.setOnTouchListener { v, motionEvent ->
            val eventType = motionEvent.actionMasked
            when (eventType) {
                MotionEvent.ACTION_DOWN -> {
                    // User touched screen with finger
                    onMotionEventActionDown()
                }
                MotionEvent.ACTION_UP -> {
                    // User lifted his finger up
                    if (exoGlanceVideoExpanded.isVisible) {
                        exoGlanceVideoExpanded.isVisible = false
                    } else {
                        ivGlanceImageExpanded.isVisible = false
                        ivGlanceImageExpandedBackground.isVisible = false
                    }
                }
            }
            return@setOnTouchListener true
        }

        ivGlanceImageExpandedBackground.onSafeClick {
            ivGlanceImageExpanded.isVisible = false
            ivGlanceImageExpandedBackground.isVisible = false
        }

        exoGlanceVideoExpanded.onSafeClick {
            exoGlanceVideoExpanded.isVisible = false
        }

        btnShowInBrowser.onSafeClick {
            requireActivity().openWithChrome(
                glanceImageList[currentImagePosition].title.ifBlank {
                    glanceImageList[currentImagePosition].link
                }
            )
        }

        setOnGlanceImageClickListener()

        setOnYoutubeVideoClickListener()

        btnMenu.onSafeClick { it: Pair<View?, Boolean> ->
            it.first ?: return@onSafeClick
            val glanceOptions = listOf("Add Image", "Add Remainders", "Add Youtube Videos")
            requireContext().showPopup(
                view = it.first!!,
                menuList = glanceOptions
            ) { position: Int ->
                when (glanceOptions[position]) {
                    glanceOptions[0] -> {
                        (requireActivity() as? MainActivity)?.showScreen(AddFragment.newInstance(AddItemType.GLANCE_IMAGE), FragmentsTag.ADD_ITEM, isAdd = true)
                    }
                    glanceOptions[1] -> {
                        root.showSnackBar(glanceOptions[1])
                    }
                    glanceOptions[2] -> {
                        (requireActivity() as? MainActivity)?.showScreen(AddFragment.newInstance(AddItemType.YOUTUBE_VIDEO), FragmentsTag.ADD_ITEM, isAdd = true)
                    }
                }
            }
        }

        layoutUnreadSms.root.onSafeClick {
            // TODO fix this
            requireContext().sendSms("", "")
        }

        layoutMissedCalls.root.onSafeClick {
            // TODO fix this
            requireContext().openDialer("")
        }

        sliderGlanceImage.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                println("seekbar progress: $progress")
                ivGlanceImage.load(glanceImageList[progress].link) {
                    placeholder(R.color.black)
                    error(R.color.md_red_dark)
                }
                tvImageCount.text = "${progress + 1}/${glanceImageList.size}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                println("seekbar touch started!")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                println("seekbar touch stopped!")
                currentImagePosition = seekBar.progress
                cardGlanceImages.performClick()
            }
        })

        sliderYoutubeVideos.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                println("seekbar progress: $progress")
                val youtubeVideo = youtubeVideoList.getOrNull(progress)
                val youtubeVideoThumbnailUrl = youtubeVideo?.videoId?.toYoutubeThumbnailUrl()
                ivVideoThumbnail.load(youtubeVideoThumbnailUrl) {
                    placeholder(R.color.black)
                    error(R.color.md_red_dark)
                }
                tvVideoTitle.text = youtubeVideo?.title ?: ""
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                println("seekbar touch started!")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                println("seekbar touch stopped!")
                currentYoutubeVideoPosition = seekBar.progress
                cardYoutubeVideos.performClick()
            }
        })
    }

    private fun FragmentGlanceBinding.observeForData() {
        sharedViewModel.holidayListLiveData.observe(viewLifecycleOwner) { it: List<Holiday>? ->
            if (it.isNullOrEmpty()) return@observe
            updateHolidaysView(it)
        }
        sharedViewModel.youtubeVideoListLiveData.observe(viewLifecycleOwner) { it: List<YoutubeVideo>? ->
            youtubeVideoList = it?.ifEmpty {
                allYoutubeVideos
            } ?: emptyList()
            sliderYoutubeVideos.max = youtubeVideoList.lastIndex
            currentYoutubeVideoPosition = 0
            cardYoutubeVideos.performClick()
        }
        sharedViewModel.glanceImageListLiveData.observe(viewLifecycleOwner) { imageList: List<GlanceImage>? ->
            glanceImageList = imageList?.ifEmpty {
                tempImageUrlList
            } ?: emptyList()
            tvImageCount.text = "${1}/${glanceImageList.size}"
            sliderGlanceImage.max = glanceImageList.lastIndex
            currentImagePosition = 0
            cardGlanceImages.performClick()
        }
    }

    private fun FragmentGlanceBinding.onMotionEventActionDown() {
        val currentPosition = if (currentImagePosition == 0) glanceImageList.size else currentImagePosition - 1
        val isGlanceVideoShown = VideoFormat.values().map { it.extension.toLowCase() }.contains(glanceImageList[currentPosition].link.substringAfterLast(".").toLowCase())
        if (isGlanceVideoShown) {
            exoGlanceVideoExpanded.isVisible = true
            exoPlayerExpanded?.release()
            exoPlayerExpanded = ExoPlayer.Builder(requireContext()).build()
            exoGlanceVideoExpanded.player = exoPlayerExpanded
            exoPlayerExpanded?.apply {
                addMediaSource(
                    DefaultMediaSourceFactory(requireContext()).createMediaSource(
                        MediaItem.fromUri(glanceImageList[currentPosition].link)
                    )
                )
                repeatMode = Player.REPEAT_MODE_ONE
                prepare()
                playWhenReady = true // Since we are loading from url, we cannot directly set play()
            }
        } else {
            ivGlanceImageExpandedBackground.isVisible = true
            ivGlanceImageExpanded.isVisible = true
            lifecycleScope.launch {
                val blurredBitmapFile = File(
                    /* parent = */ glanceImageFileDir,
                    /* child = */ "glance_image_$currentImagePosition.jpg"
                )
                if (blurredBitmapFile.exists().not()) {
                    blurBitmapForImageBackground()
                }
                val bitmapToBlur = blurredBitmapFile.toBitmap() ?: return@launch
                withContext(Main) {
                    val blurredBitmap = BlurStackOptimized().blur(image = bitmapToBlur, radius = 30)
                    ivGlanceImageExpandedBackground.setImageBitmap(blurredBitmap)
                }
            }
        }
    }

    private suspend fun blurBitmapForImageBackground() = try {
        val imageRequest = ImageRequest.Builder(requireContext()).data(currentGlanceImage.link).listener(
            onStart = {
                // set your progressbar visible here
            },
            onSuccess = { request, metadata ->
                // set your progressbar invisible here
            }
        ).build()
        val drawable = ImageLoader(requireContext()).execute(imageRequest).drawable
        val bitmapToBlurAndSave = (drawable as BitmapDrawable).bitmap
        bitmapToBlurAndSave.saveToInternalStorage(
            fileName = "glance_image_$currentImagePosition.jpg",
            fileDir = glanceImageFileDir,
        )
    } catch (_: Exception) {
    }

    private var currentImagePosition = 0

    // https://coil-kt.github.io/coil/
    private fun FragmentGlanceBinding.setOnGlanceImageClickListener() {
        cardGlanceImages.onSafeClick {
            cardImageCount.isVisible = true
            currentGlanceImage = glanceImageList[currentImagePosition]
            tvImageCount.text = "${currentImagePosition + 1}/${glanceImageList.size}"

            fun loadExpandedImageView() {
                lifecycleScope.launch {
                    blurBitmapForImageBackground()
                }
                ivGlanceImageExpanded.load(glanceImageList[currentImagePosition].link)
                ivGlanceImageExpandedBackground.load(R.drawable.black_wall)
            }

            when {
                glanceImageList[currentImagePosition].link.endsWith(suffix = ".gif", ignoreCase = true) -> {
                    exoPlayerView.isVisible = false
                    exoPlayer?.release()
                    lifecycleScope.launch {
                        val imageLoader = ImageLoader.Builder(requireContext())
                            .components {
                                if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
                            }
                            .build()
                        val imageRequest = ImageRequest.Builder(requireContext()).data(glanceImageList[currentImagePosition].link).build()
                        val drawable = imageLoader.execute(imageRequest).drawable

                        withContext(Main) {
                            ivGlanceImage.load(drawable, imageLoader) {
                                placeholder(R.color.black)
                                error(R.color.md_red_dark)
                            }
                            loadExpandedImageView()
                        }
                    }
                }
                VideoFormat.values().map { it.extension.toLowCase() }.contains(glanceImageList[currentImagePosition].link.substringAfterLast(".").toLowCase()) -> {
                    exoPlayerView.isVisible = true
                    exoPlayer?.release()
                    exoPlayer = ExoPlayer.Builder(requireContext()).build()
                    exoPlayerView.player = exoPlayer
                    exoPlayer?.apply {
                        addMediaSource(
                            DefaultMediaSourceFactory(requireContext()).createMediaSource(
                                MediaItem.fromUri(glanceImageList[currentImagePosition].link)
                            )
                        )
                        repeatMode = Player.REPEAT_MODE_ONE
                        prepare()
                        playWhenReady = true // Since we are loading from url, we cannot directly set play()
                    }
                }
                else -> {
                    exoPlayerView.isVisible = false
                    exoPlayer?.release()
                    ivGlanceImage.load(glanceImageList[currentImagePosition].link) {
                        placeholder(R.color.black)
                        error(R.color.md_red_dark)
                    }
                    loadExpandedImageView()
                }
            }

            sliderGlanceImage.progress = currentImagePosition
            if (currentImagePosition == glanceImageList.lastIndex) {
                currentImagePosition = 0
            } else {
                currentImagePosition++
            }
        }
    }

    private var currentYoutubeVideoPosition = 0
    private fun FragmentGlanceBinding.setOnYoutubeVideoClickListener() {
        cardYoutubeVideos.onSafeClick {
            val youtubeVideo = youtubeVideoList.getOrNull(currentYoutubeVideoPosition)
            val youtubeVideoThumbnailUrl = youtubeVideo?.videoId?.toYoutubeThumbnailUrl()
            ivVideoThumbnail.load(youtubeVideoThumbnailUrl) {
                placeholder(R.color.black)
                error(R.color.md_red_dark)
            }
            tvVideoTitle.text = youtubeVideo?.title ?: ""
            sliderYoutubeVideos.progress = currentYoutubeVideoPosition
            btnPlayYoutubeVideo.onSafeClick {
                val intent = Intent(requireContext(), YoutubeVideoActivity::class.java).apply {
                    putExtra(IntentKey.YOUTUBE_VIDEO_ID, youtubeVideo?.videoId)
                    putParcelableArrayListExtra(IntentKey.YOUTUBE_VIDEO_LIST, youtubeVideoList.toArrayList())
                }
                startActivity(intent)
            }
            if (currentYoutubeVideoPosition == youtubeVideoList.lastIndex) {
                currentYoutubeVideoPosition = 0
            } else {
                currentYoutubeVideoPosition++
            }
        }
    }

    private fun FragmentGlanceBinding.updateHolidaysView(it: List<Holiday>) {
        tvHolidaysPlaceholder.apply {
            text = "${it.getOrNull(1)?.header} | ${it.firstOrNull()?.location}"
            onSafeClick {
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
                    root.onSafeClick { it: Pair<View?, Boolean> ->
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
                    root.onSafeClick { it: Pair<View?, Boolean> ->
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
                    root.onSafeClick { it: Pair<View?, Boolean> ->
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