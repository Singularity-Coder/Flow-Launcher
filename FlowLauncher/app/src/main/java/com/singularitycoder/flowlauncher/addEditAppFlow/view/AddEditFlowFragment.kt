package com.singularitycoder.flowlauncher.addEditAppFlow.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.SharedViewModel
import com.singularitycoder.flowlauncher.addEditAppFlow.model.AppFlow
import com.singularitycoder.flowlauncher.addEditAppFlow.model.SelectedFlowArgs
import com.singularitycoder.flowlauncher.addEditAppFlow.viewModel.AppFlowViewModel
import com.singularitycoder.flowlauncher.databinding.FragmentAddEditFlowBinding
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.constants.BottomSheetTag
import com.singularitycoder.flowlauncher.helper.constants.HOME_LAYOUT_BLURRED_IMAGE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class AddEditFlowFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = AddEditFlowFragment()
    }

    private lateinit var binding: FragmentAddEditFlowBinding

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val appFlowViewModel: AppFlowViewModel by viewModels()
    private var flowList = listOf<AppFlow>()
    private var selectedFlowPosition = 0

    private val viewPager2PageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            println("viewpager2: onPageScrollStateChanged")
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            println("viewpager2: onPageSelected")
            selectedFlowPosition = position
            addBottomDots(currentPage = selectedFlowPosition)
            binding.etFlowName.setText(flowList[position].appFlowName)
            binding.btnMenu.isVisible = position != 0 && position != flowList.lastIndex
            binding.btnDone.isInvisible = position == flowList.lastIndex
            binding.btnCancel.isInvisible = position == flowList.lastIndex
            binding.etFlowName.isEnabled = position != 0 && position != flowList.lastIndex
            sharedViewModel.setSelectedFlowArgs(
                SelectedFlowArgs(
                    isAddFlow = position == flowList.lastIndex,
                    position = position,
                    appFlowId = flowList[selectedFlowPosition].id
                )
            )
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            println("viewpager2: onPageScrolled")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddEditFlowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().setStatusBarColor(R.color.purple_500)
        requireActivity().setNavigationBarColor(R.color.black)
        binding.setUpViewPager()
        binding.observeForData()
        binding.setupUI()
        binding.setupUserActionListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().setStatusBarColor(R.color.purple_500)
        requireActivity().setNavigationBarColor(R.color.white)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.viewpagerAddEditFlow.unregisterOnPageChangeCallback(viewPager2PageChangeListener)
    }

    private fun FragmentAddEditFlowBinding.setUpViewPager() {
        viewpagerAddEditFlow.apply {
            adapter = MainViewPagerAdapter(fragmentManager = requireActivity().supportFragmentManager, lifecycle = lifecycle)
            registerOnPageChangeCallback(viewPager2PageChangeListener)
//            setShowSideItems(pageMarginPx = 24.dpToPx(), offsetPx = 32.dpToPx())
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
//            setPageTransformer { page, position ->
//                val offset = position * -(2 * /* offsetPx */ 32.dpToPx() + /* pageMarginPx */ 24.dpToPx())
//                page.translationX = -offset
//            }
//            binding.viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
//            binding.viewPager.setCurrentItem(0);
//            binding.viewPager.getAdapter().notifyDataSetChanged();
            setPageTransformer(DepthPageTransformer())
        }
    }

    private fun FragmentAddEditFlowBinding.setupUI() {
        lifecycleScope.launch {
            val blurredBitmapFile = File(
                /* parent = */ requireContext().getHomeLayoutBlurredImageFileDir(),
                /* child = */ HOME_LAYOUT_BLURRED_IMAGE
            )
            if (blurredBitmapFile.exists().not()) return@launch
            val blurredBitmap = blurredBitmapFile.toBitmap() ?: return@launch
            withContext(Main) {
                ivBackground.setImageBitmap(blurredBitmap)
            }
        }
    }

    private fun FragmentAddEditFlowBinding.setupUserActionListeners() {
        root.setOnClickListener {}

        btnMenu.onSafeClick { pair: Pair<View?, Boolean> ->
            pair.first ?: return@onSafeClick
            val optionsList = listOf(
                Pair("Edit Name", R.drawable.outline_drive_file_rename_outline_24),
                Pair("Add Apps", R.drawable.outline_add_box_24),
                Pair("Remove", R.drawable.outline_delete_24),
            )
            requireContext().showPopupMenuWithIcons(
                view = pair.first,
                menuList = optionsList
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {
                        lifecycleScope.launch(Main) {
                            etFlowName.requestFocus()
                            delay(500)
                            etFlowName.showKeyboard()
                            etFlowName.setSelection(etFlowName.text.length)
                        }
                    }
                    optionsList[1].first -> {
                        println("logggggg selectedFlowPosition: $selectedFlowPosition")
                        AppSelectorBottomSheetFragment.newInstance(selectedFlowId = flowList[selectedFlowPosition].id).show(
                            requireActivity().supportFragmentManager,
                            BottomSheetTag.APP_SELECTOR
                        )
                    }
                    optionsList[2].first -> {
                        appFlowViewModel.deleteAppFlow(appFlow = flowList[selectedFlowPosition])
                    }
                }
            }
        }

        etFlowName.onImeClick {
            lifecycleScope.launch {
                // TODO change this to db query and pass app name and id
                val appFlow = appFlowViewModel.getAppFlowById(flowList[selectedFlowPosition].id)
                appFlowViewModel.updateAppFlow(
                    appFlow = AppFlow(
                        id = appFlow?.id ?: -1,
                        appFlowName = etFlowName.text.toString(),
                        isSelected = appFlow?.isSelected ?: false,
                        appList = appFlow?.appList ?: emptyList()
                    )
                )
                withContext(Main) {
                    etFlowName.hideKeyboard()
                    etFlowName.clearFocus()
                }
            }
        }

        btnDone.onSafeClick {
            // set the current flow to isSelected true
            lifecycleScope.launch(IO) {
                val selectedAppFlow = appFlowViewModel.getAppFlowById(flowList[selectedFlowPosition].id).apply {
                    this?.isSelected = true
                }
                val allAppFlowIdList = appFlowViewModel.getAllFlowIds()
                appFlowViewModel.selectFlow(
                    isSelected = false,
                    appFlowList = allAppFlowIdList,
                    appFlow = selectedAppFlow
                )
                withContext(Main) {
                    requireActivity().supportFragmentManager.popBackStackImmediate()
                }
            }
        }

        btnCancel.onSafeClick {
            requireActivity().supportFragmentManager.popBackStackImmediate()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentAddEditFlowBinding.observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = appFlowViewModel.appFlowListStateFlow) { it: List<AppFlow> ->
            flowList = it.toMutableList().apply {
                add(AppFlow(appFlowName = "Add Flow", isSelected = false, appList = emptyList()))
            }
            doAfter(500L) {
                addBottomDots(currentPage = selectedFlowPosition)
            }
            viewpagerAddEditFlow.adapter?.notifyDataSetChanged()
            viewpagerAddEditFlow.currentItem = selectedFlowPosition
        }
    }

    private fun addBottomDots(currentPage: Int) {
        val tvDotsArray = arrayOfNulls<TextView>(flowList.size)
        binding.llDots.removeAllViews()
        tvDotsArray.indices.forEach { i: Int ->
            tvDotsArray[i] = TextView(context).apply {
                text = HtmlCompat.fromHtml("&#8226;", HtmlCompat.FROM_HTML_MODE_LEGACY)
                textSize = 35f
                setTextColor(requireContext().color(R.color.white))
            }
            binding.llDots.addView(tvDotsArray[i])
        }
        if (tvDotsArray.isNotEmpty()) {
            val textView = tvDotsArray.getOrNull(currentPage)?.apply {
                setTextColor(requireContext().color(R.color.purple_500))
            }
            tvDotsArray[currentPage] = textView
            binding.llDots.refreshDrawableState()
        }
    }

    inner class MainViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = flowList.size
        override fun createFragment(position: Int): Fragment = FlowSelectedAppsFragment.newInstance(
            isAddFlow = position == flowList.lastIndex,
            position = position,
            appFlowId = flowList[selectedFlowPosition].id
        )
    }
}