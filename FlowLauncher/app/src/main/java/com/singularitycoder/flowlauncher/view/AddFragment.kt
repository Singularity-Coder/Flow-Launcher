package com.singularitycoder.flowlauncher.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.flowlauncher.SharedViewModel
import com.singularitycoder.flowlauncher.databinding.FragmentAddBinding
import com.singularitycoder.flowlauncher.helper.constants.AddItemType
import com.singularitycoder.flowlauncher.helper.hideKeyboard
import com.singularitycoder.flowlauncher.helper.showAlertDialog
import com.singularitycoder.flowlauncher.helper.showKeyboard
import com.singularitycoder.flowlauncher.model.GlanceImage
import com.singularitycoder.flowlauncher.model.Quote
import com.singularitycoder.flowlauncher.model.YoutubeVideo

// TODO double back press to exit
// TODO upload routines from csv
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
    private val addItemList = mutableListOf<AddItem>()
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private var listType: String? = null

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

    private fun FragmentAddBinding.observeForData() {
        sharedViewModel.glanceImageListLiveData.observe(viewLifecycleOwner) { glanceImageList: List<GlanceImage>? ->
            if (glanceImageList.isNullOrEmpty()) return@observe
            if (listType != AddItemType.GLANCE_IMAGE) return@observe
            addItemAdapter.itemsList = glanceImageList.map {
                val item = AddItem(
                    link = it.link,
                    title = it.title
                )
                addItemList.add(item)
                item
            }
            addItemAdapter.notifyItemInserted(if (addItemAdapter.itemsList.isEmpty()) 0 else addItemAdapter.itemsList.lastIndex)
            etAddItem.setText("")
        }
        sharedViewModel.quoteListLiveData.observe(viewLifecycleOwner) { quoteList: List<Quote>? ->
            if (quoteList.isNullOrEmpty()) return@observe
            if (listType != AddItemType.QUOTE) return@observe
            addItemAdapter.itemsList = quoteList.map {
                val item = AddItem(
                    link = it.title,
                    title = it.author
                )
                addItemList.add(item)
                item
            }
            addItemAdapter.notifyItemInserted(if (addItemAdapter.itemsList.isEmpty()) 0 else addItemAdapter.itemsList.lastIndex)
            etAddItem.setText("")
        }
        sharedViewModel.youtubeVideoListLiveData.observe(viewLifecycleOwner) { youtubeVideoList: List<YoutubeVideo>? ->
            if (youtubeVideoList.isNullOrEmpty()) return@observe
            if (listType != AddItemType.YOUTUBE_VIDEO) return@observe
            addItemAdapter.itemsList = youtubeVideoList.map {
                val item = AddItem(
                    link = it.videoId,
                    title = it.title
                )
                addItemList.add(item)
                item
            }
            addItemAdapter.notifyItemInserted(if (addItemAdapter.itemsList.isEmpty()) 0 else addItemAdapter.itemsList.lastIndex)
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
                etAddItem.hint = "Quote Format: Quote by Author"
                cardAddItemParent.isVisible = true
                fabAddFlowImage.isVisible = false
            }
            AddItemType.YOUTUBE_VIDEO -> {
                tvAddTitle.text = "Add Youtube Videos"
                etAddItem.hint = "Video Format: Link Title"
                cardAddItemParent.isVisible = true
                fabAddFlowImage.isVisible = false
            }
        }
        rvRoutineSteps.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = addItemAdapter
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

    private fun FragmentAddBinding.setupUserActionListeners() {
        ibAddStep.setOnClickListener {
            when (listType) {
                AddItemType.QUOTE -> {
                    val quote = Quote(
                        title = etAddItem.text.toString().substringBeforeLast("by").trim(),
                        author = etAddItem.text.toString().substringAfterLast("by").trim()
                    )
                    sharedViewModel.addQuoteToDb(quote)
                }
                AddItemType.YOUTUBE_VIDEO -> {
                    val youtubeVideo = YoutubeVideo(
                        videoId = etAddItem.text.toString().substringBefore(" ").substringAfter("watch?v=").trim(),
                        title = etAddItem.text.toString().substringAfter(" ").trim()
                    )
                    sharedViewModel.addYoutubeVideoToDb(youtubeVideo)
                }
            }
        }
        fabAddFlowImage.setOnClickListener {
            when (listType) {
                AddItemType.GLANCE_IMAGE -> {
//                    val flowImage = FlowImage(
//                        link = etAddItem.text.toString(),
//                        title = ""
//                    )
//                    sharedViewModel.addFlowImageToDb(flowImage)
                    // TODO show multi image picker
                }
            }
        }
        ibBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStackImmediate()
        }
        addItemAdapter.setItemClickListener { it: AddItem ->
            etAddItem.requestFocus()
            etAddItem.showKeyboard()
        }
        addItemAdapter.setItemLongClickListener { it: AddItem ->
            requireContext().showAlertDialog(
                title = "Delete Item",
                message = "This action cannot be undone. Are you sure?",
                positiveBtnText = "Delete",
                negativeBtnText = "Cancel",
                positiveAction = {
                    when (listType) {
                        AddItemType.QUOTE -> {
                           sharedViewModel.deleteQuoteFromDb(title = it.title)
                        }
                        AddItemType.YOUTUBE_VIDEO -> {

                        }
                        AddItemType.GLANCE_IMAGE -> {

                        }
                    }
                }
            )
        }
        addItemAdapter.setCancelClickListener {
            etAddItem.requestFocus()
            etAddItem.hideKeyboard()
        }
        addItemAdapter.setUpdateClickListener {
            etAddItem.requestFocus()
            etAddItem.hideKeyboard()
        }
        // TODO IME check close keyboard
        // TODO on scroll down close keyboard
    }
}

private const val KEY_ADD_LIST_TYPE = "KEY_ADD_LIST_TYPE"