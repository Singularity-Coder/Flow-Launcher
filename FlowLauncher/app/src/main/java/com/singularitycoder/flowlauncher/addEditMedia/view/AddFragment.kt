package com.singularitycoder.flowlauncher.addEditMedia.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.flowlauncher.SharedViewModel
import com.singularitycoder.flowlauncher.addEditMedia.model.AddItem
import com.singularitycoder.flowlauncher.databinding.FragmentAddBinding
import com.singularitycoder.flowlauncher.glance.model.GlanceImage
import com.singularitycoder.flowlauncher.glance.model.YoutubeVideo
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.constants.AddItemType
import com.singularitycoder.flowlauncher.today.model.Quote
import kotlinx.coroutines.launch

// TODO upload stuff from csv
// TODO Export stuff to csv
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

        val glanceImage = GlanceImage(
            link = file.absolutePath,
            title = file.name
        )
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
        binding.observeForData()
        binding.setupUI()
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
                val item = AddItem(
                    link = it.link,
                    title = it.title
                )
                item
            } ?: emptyList()
            addItemList = addItemAdapter.itemsList.toMutableList()
            addItemAdapter.notifyDataSetChanged()
            etAddItem.setText("")
        }
        sharedViewModel.quoteListLiveData.observe(viewLifecycleOwner) { quoteList: List<Quote>? ->
            if (listType != AddItemType.QUOTE) return@observe
            addItemAdapter.itemsList = quoteList?.map {
                val item = AddItem(
                    link = it.title,
                    title = it.author
                )
                item
            } ?: emptyList()
            addItemList = addItemAdapter.itemsList.toMutableList()
            addItemAdapter.notifyDataSetChanged()
            etAddItem.setText("")
        }
        sharedViewModel.youtubeVideoListLiveData.observe(viewLifecycleOwner) { youtubeVideoList: List<YoutubeVideo>? ->
            if (listType != AddItemType.YOUTUBE_VIDEO) return@observe
            addItemAdapter.itemsList = youtubeVideoList?.map {
                val item = AddItem(
                    link = it.videoId,
                    title = it.title
                )
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
            layoutManager = LinearLayoutManager(context)
            adapter = addItemAdapter
            addItemAdapter.setListType(listType)
        }
    }

    // https://stackoverflow.com/questions/3467205/android-key-dispatching-timed-out
    private fun FragmentAddBinding.setupUserActionListeners() {
        ibAddItem.onSafeClick {
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
        fabAddFlowImage.onSafeClick {
            when (listType) {
                AddItemType.GLANCE_IMAGE -> {
                    readStoragePermissionResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
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

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int,
            ) = Unit
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvRoutineSteps)
    }
}

private const val KEY_ADD_LIST_TYPE = "KEY_ADD_LIST_TYPE"