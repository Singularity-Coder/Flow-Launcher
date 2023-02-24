package com.singularitycoder.flowlauncher.addEditMedia.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.SharedViewModel
import com.singularitycoder.flowlauncher.addEditMedia.model.AddItem
import com.singularitycoder.flowlauncher.databinding.FragmentAddBinding
import com.singularitycoder.flowlauncher.glance.model.GlanceImage
import com.singularitycoder.flowlauncher.glance.model.YoutubeVideo
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.constants.AddItemType
import com.singularitycoder.flowlauncher.helper.constants.QuickActionAddMedia
import com.singularitycoder.flowlauncher.helper.pinterestView.CircleImageView
import com.singularitycoder.flowlauncher.helper.pinterestView.PinterestView
import com.singularitycoder.flowlauncher.helper.quickActionView.Action
import com.singularitycoder.flowlauncher.helper.quickActionView.QuickActionView
import com.singularitycoder.flowlauncher.today.model.Quote
import kotlinx.coroutines.launch
import java.util.*


class AddFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(listType: String) = AddFragment().apply {
            arguments = Bundle().apply {
                putString(KEY_ADD_LIST_TYPE, listType)
            }
        }
    }

    private lateinit var binding: FragmentAddBinding

    private val addItemAdapter = AddItemAdapter()
    private var addItemList = mutableListOf<AddItem>()
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private var listType: String? = null

    private val readStoragePermissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isPermissionGranted: Boolean? ->
        isPermissionGranted ?: return@registerForActivityResult
        if (isPermissionGranted.not()) {
            requireContext().showPermissionSettings()
            return@registerForActivityResult
        }

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        if (intent.resolveActivity(requireContext().packageManager) == null) return@registerForActivityResult
        imageSelectionResult.launch(intent)
    }

    private val imageSelectionResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it: ActivityResult? ->
        if (it?.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val data = it.data ?: return@registerForActivityResult
        val file = requireContext().readFileFromExternalDbAndWriteFileToInternalDb(data.data ?: Uri.EMPTY) ?: return@registerForActivityResult

        println("originalImageUri: ${data.data}")

        val glanceImage = GlanceImage(link = file.absolutePath, title = file.name)
        sharedViewModel.addGlanceImageToDb(glanceImage)
    }

    private val singleMediaSelectionResult = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { it: Uri? ->
        it ?: return@registerForActivityResult
        val file = requireContext().readFileFromExternalDbAndWriteFileToInternalDb(it) ?: return@registerForActivityResult

        println("originalImageUri: $it")

        val glanceImage = GlanceImage(link = file.absolutePath, title = file.name)
        sharedViewModel.addGlanceImageToDb(glanceImage)
    }

    private val multipleMediaSelectionResult = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { list: List<@JvmSuppressWildcards Uri>? ->
        if (list.isNullOrEmpty()) return@registerForActivityResult
        list.forEach { it: @JvmSuppressWildcards Uri ->
            val file = requireContext().readFileFromExternalDbAndWriteFileToInternalDb(it) ?: return@registerForActivityResult

            println("originalImageUri: $it")

            val glanceImage = GlanceImage(link = file.absolutePath, title = file.name)
            sharedViewModel.addGlanceImageToDb(glanceImage)
        }
    }

    private val videoSelectionResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it: ActivityResult? ->
        if (it?.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val data = it.data ?: return@registerForActivityResult
        val file = requireContext().readFileFromExternalDbAndWriteFileToInternalDb(data.data ?: Uri.EMPTY) ?: return@registerForActivityResult

        println("originalVideoUri: ${data.data}")

        val glanceImage = GlanceImage(link = file.absolutePath, title = file.name)
        sharedViewModel.addGlanceImageToDb(glanceImage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            listType = it.getString(KEY_ADD_LIST_TYPE, "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.observeForData()
        binding.setupUserActionListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        deleteAllFilesFrom(directory = requireContext().internalFilesDir(directory = "glance_images"), withName = "glance_image_")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentAddBinding.observeForData() {
        sharedViewModel.glanceImageListLiveData.observe(viewLifecycleOwner) { glanceImageList: List<GlanceImage>? ->
            if (listType != AddItemType.GLANCE_IMAGE) return@observe
            addItemAdapter.itemsList = glanceImageList?.map {
                val item = AddItem(link = it.link, title = it.title)
                item
            } ?: emptyList()
            addItemList = addItemAdapter.itemsList.toMutableList()
            addItemAdapter.notifyDataSetChanged()
            etAddItem.setText("")
        }
        sharedViewModel.quoteListLiveData.observe(viewLifecycleOwner) { quoteList: List<Quote>? ->
            if (listType != AddItemType.QUOTE) return@observe
            addItemAdapter.itemsList = quoteList?.map {
                val item = AddItem(link = it.title, title = it.author)
                item
            } ?: emptyList()
            addItemList = addItemAdapter.itemsList.toMutableList()
            addItemAdapter.notifyDataSetChanged()
            etAddItem.setText("")
        }
        sharedViewModel.youtubeVideoListLiveData.observe(viewLifecycleOwner) { youtubeVideoList: List<YoutubeVideo>? ->
            if (listType != AddItemType.YOUTUBE_VIDEO) return@observe
            addItemAdapter.itemsList = youtubeVideoList?.map {
                val item = AddItem(link = it.videoId, title = it.title)
                item
            } ?: emptyList()
            addItemList = addItemAdapter.itemsList.toMutableList()
            addItemAdapter.notifyDataSetChanged()
            etAddItem.setText("")
        }
    }

    private fun FragmentAddBinding.setupUI() {
        when (listType) {
            AddItemType.GLANCE_IMAGE -> {
                tvAddTitle.text = "Add Images"
                etAddItem.hint = "Add an image"
                cardAddItemParent.isVisible = false
                fabAddFlowImage.isVisible = true
            }
            AddItemType.QUOTE -> {
                tvAddTitle.text = "Add Quotes"
                etAddItem.hint = "Format: Quote by Author"
                cardAddItemParent.isVisible = true
                fabAddFlowImage.isVisible = false
            }
            AddItemType.YOUTUBE_VIDEO -> {
                tvAddTitle.text = "Add Youtube Videos"
                etAddItem.hint = "Format: Link Title"
                cardAddItemParent.isVisible = true
                fabAddFlowImage.isVisible = false
            }
        }
        rvRoutineSteps.apply {
            layoutAnimation = when (listType) {
                AddItemType.QUOTE -> rvRoutineSteps.context.layoutAnimationController(R.anim.layout_animation_fall_down)
                AddItemType.YOUTUBE_VIDEO -> rvRoutineSteps.context.layoutAnimationController(R.anim.layout_animation_slide_from_bottom)
                AddItemType.GLANCE_IMAGE -> rvRoutineSteps.context.layoutAnimationController(R.anim.layout_animation_fade_in)
                else -> null
            }
            layoutManager = LinearLayoutManager(context)
            adapter = addItemAdapter
            addItemAdapter.setListType(listType)
        }
    }

    // https://stackoverflow.com/questions/3467205/android-key-dispatching-timed-out
    private fun FragmentAddBinding.setupUserActionListeners() {
        setAddFabTouchOptions()
//        setAddFabTouchOptions2()

        ibAddItem.onSafeClick {
            if (etAddItem.text.isNullOrBlank()) return@onSafeClick
            when (listType) {
                AddItemType.QUOTE -> {
                    lifecycleScope.launch {
                        val quote = Quote(
                            title = etAddItem.text.toString().substringBeforeLastIgnoreCase(" by ").trim(),
                            author = etAddItem.text.toString().substringAfterLastIgnoreCase(" by ")?.trim() ?: "Unknown"
                        )
                        sharedViewModel.addQuoteToDb(quote)
                    }
                }
                AddItemType.YOUTUBE_VIDEO -> {
                    lifecycleScope.launch {
                        val videoId = etAddItem.text.toString().substringBefore(" ").substringAfter("watch?v=").trim()
                        val cleanVideoId = if (videoId.contains("&")) {
                            videoId.substringBefore("&")
                        } else videoId
                        val youtubeVideo = YoutubeVideo(
                            videoId = cleanVideoId,
                            title = etAddItem.text.toString().substringAfter(" ").trim()
                        )
                        sharedViewModel.addYoutubeVideoToDb(youtubeVideo)
                    }
                }
            }
        }
        ibBack.onSafeClick {
            requireActivity().supportFragmentManager.popBackStackImmediate()
        }
        addItemAdapter.setItemClickListener { item: AddItem ->
            root.context.clipboard()?.text = when (listType) {
                AddItemType.YOUTUBE_VIDEO -> "https://www.youtube.com/watch?v=${item.link} item.title"
                AddItemType.QUOTE -> "${item.link} by ${item.title}"
                AddItemType.GLANCE_IMAGE -> item.link
                else -> ""
            }
            root.showSnackBar(message = "Copied", anchorView = cardAddItemParent)
        }
        addItemAdapter.setItemLongClickListener { it: AddItem ->
            requireContext().showAlertDialog(
                title = "Delete Item",
                message = "This action cannot be undone. Are you sure?",
                positiveBtnText = "Delete",
                negativeBtnText = "Cancel",
                positiveAction = {
                    lifecycleScope.launch {
                        when (listType) {
                            AddItemType.QUOTE -> {
                                sharedViewModel.deleteQuoteFromDb(link = it.link)
                            }
                            AddItemType.YOUTUBE_VIDEO -> {
                                sharedViewModel.deleteYoutubeVideoFromDb(videoId = it.link)
                            }
                            AddItemType.GLANCE_IMAGE -> {
                                sharedViewModel.deleteGlanceImageFromDb(link = it.link)
                            }
                        }
                    }
                }
            )
        }
        etAddItem.onImeClick {
            ibAddItem.performClick()
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            /* Drag Directions */ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
            /* Swipe Directions */0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                // FIXME drag is not smooth and gets attached to its immediate next position
                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition
                val fromPositionItem = addItemList[fromPosition]
                addItemList[fromPosition] = addItemList[toPosition]
                addItemList[toPosition] = fromPositionItem
                addItemAdapter.notifyItemMoved(fromPosition, toPosition)
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvRoutineSteps)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setAddFabTouchOptions2() {
        fun createChildView(
            imageId: Int,
            tip: String?,
            @ColorRes colorRes: Int
        ): View = CircleImageView(requireContext()).apply {
            borderWidth = 0
            scaleType = ImageView.ScaleType.CENTER_CROP
            fillColor = requireContext().color(colorRes)
            setImageDrawable(requireContext().drawable(imageId)?.changeColor(requireContext(), R.color.purple_500))
            tag = tip // just for save Menu item tips
        }
        binding.pinterestView.addMenuItem(
            createChildView(R.drawable.round_image_24, "", R.color.purple_50),
            createChildView(R.drawable.round_image_24, QuickActionAddMedia.SELECT_FROM_GALLERY.value, R.color.purple_50),
            createChildView(R.drawable.round_photo_camera_24, QuickActionAddMedia.TAKE_PHOTO.value, R.color.purple_50),
            createChildView(R.drawable.baseline_videocam_24, QuickActionAddMedia.TAKE_VIDEO.value, R.color.purple_50)
        )
        binding.pinterestView.setPinClickListener(object : PinterestView.PinMenuClickListener {
            override fun onMenuItemClick(checkedView: View?, clickItemPos: Int) {
                requireContext().showToast(checkedView?.tag.toString() + " clicked!")
            }

            override fun onAnchorViewClick() {
                requireContext().showToast("button clicked!")
            }
        })
        binding.fabAddFlowImage.setOnTouchListener { v, event ->
            binding.pinterestView.dispatchTouchEvent(event)
            true
        }
    }

    private fun setAddFabTouchOptions() {
        val icon1 = requireContext().drawable(R.drawable.round_image_24)?.changeColor(requireContext(), R.color.purple_500)
        val action1 = Action(id = QuickActionAddMedia.SELECT_FROM_GALLERY.ordinal, icon = icon1!!, title = QuickActionAddMedia.SELECT_FROM_GALLERY.value)
        val icon2 = requireContext().drawable(R.drawable.round_photo_camera_24)?.changeColor(requireContext(), R.color.purple_500)
        val action2 = Action(id = QuickActionAddMedia.TAKE_PHOTO.ordinal, icon = icon2!!, title = QuickActionAddMedia.TAKE_PHOTO.value)
        val icon3 = requireContext().drawable(R.drawable.baseline_videocam_24)?.changeColor(requireContext(), R.color.purple_500)
        val action3 = Action(id = QuickActionAddMedia.TAKE_VIDEO.ordinal, icon = icon3!!, title = QuickActionAddMedia.TAKE_VIDEO.value)
        val addFabQuickActionView = QuickActionView.make(requireContext()).apply {
            addAction(action1)
            addAction(action2)
            addAction(action3)
            register(binding.fabAddFlowImage, 66, -96)
            setBackgroundColor(requireContext().color(R.color.purple_50))
            setIndicatorDrawable(createGradientDrawable(width = 150, height = 150))
        }
        addFabQuickActionView.setOnActionHoverChangedListener { action: Action?, quickActionView: QuickActionView?, isHovering: Boolean ->
            if (isHovering) {
                quickActionView?.setBackgroundColor(requireContext().color(R.color.purple_500))
                quickActionView?.setIconColor(R.color.purple_50)
            } else {
                quickActionView?.setBackgroundColor(requireContext().color(R.color.purple_50))
                quickActionView?.setIconColor(R.color.purple_500)
            }
        }
        addFabQuickActionView.setOnActionSelectedListener { action: Action?, quickActionView: QuickActionView? ->
            when (action?.id) {
                QuickActionAddMedia.SELECT_FROM_GALLERY.ordinal -> {
                    when (listType) {
                        AddItemType.GLANCE_IMAGE -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                // https://stackoverflow.com/questions/73999566/how-to-construct-pickvisualmediarequest-for-activityresultlauncher
                                // https://www.youtube.com/watch?v=uHX5NB6wHao
                                val mediaType = ActivityResultContracts.PickVisualMedia.ImageAndVideo as ActivityResultContracts.PickVisualMedia.VisualMediaType
                                val request: PickVisualMediaRequest = PickVisualMediaRequest.Builder().setMediaType(mediaType).build()
                                multipleMediaSelectionResult.launch(request)
                            } else {
                                readStoragePermissionResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                        }
                    }
                }
                QuickActionAddMedia.TAKE_PHOTO.ordinal -> {

                }
                QuickActionAddMedia.TAKE_VIDEO.ordinal -> {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "video/*"
                    }
                    if (intent.resolveActivity(requireContext().packageManager) == null) return@setOnActionSelectedListener
                    videoSelectionResult.launch(intent)
                }
            }
        }
    }
}

private const val KEY_ADD_LIST_TYPE = "KEY_ADD_LIST_TYPE"