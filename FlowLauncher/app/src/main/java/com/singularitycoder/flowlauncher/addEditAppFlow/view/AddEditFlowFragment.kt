package com.singularitycoder.flowlauncher.addEditAppFlow.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.addEditAppFlow.model.AppFlow
import com.singularitycoder.flowlauncher.addEditAppFlow.viewModel.AppFlowViewModel
import com.singularitycoder.flowlauncher.databinding.FragmentAddEditFlowBinding
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.constants.BottomSheetTag
import com.singularitycoder.flowlauncher.helper.constants.HOME_LAYOUT_BLURRED_IMAGE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// TODO
// Add remove apps - new checklist screen
// Add new flow - new card in view pager

// Disable apps that are not selected
// Set recycler view for apps
// Flow name

// Button to add remove apps
// On Clicking the card they select that flow

// Add flow name - bottom sheet - this adds frag in viewpager
// Then add
// change status bar color
@AndroidEntryPoint
class AddEditFlowFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = AddEditFlowFragment()
    }

    private lateinit var binding: FragmentAddEditFlowBinding

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
            addBottomDots(currentPage = position)
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
        setUpViewPager()
        binding.setupUI()
        binding.setupUserActionListeners()
        setupObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().setStatusBarColor(R.color.purple_500)
        binding.viewpagerAddEditFlow.unregisterOnPageChangeCallback(viewPager2PageChangeListener)
    }

    private fun setUpViewPager() {
        binding.viewpagerAddEditFlow.apply {
            adapter = MainViewPagerAdapter(fragmentManager = requireActivity().supportFragmentManager, lifecycle = lifecycle)
            registerOnPageChangeCallback(viewPager2PageChangeListener)
//            setShowSideItems(pageMarginPx = 24.dpToPx(), offsetPx = 32.dpToPx())
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer { page, position ->
                val offset = position * -(2 * /* offsetPx */ 32.dpToPx() + /* pageMarginPx */ 24.dpToPx())
                page.translationX = -offset
            }
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
            withContext(Dispatchers.Main) {
                ivBackground.setImageBitmap(blurredBitmap)
            }
        }
    }

    private fun FragmentAddEditFlowBinding.setupUserActionListeners() {
        btnMenu.setOnClickListener { view: View? ->
            view ?: return@setOnClickListener
            val options = listOf("Edit Name", "Add Apps")
            requireContext().showPopup(
                view = view,
                menuList = options
            ) { position: Int ->
                when (options[position]) {
                    options[0] -> {
                        lifecycleScope.launch(Main) {
                            tvFlowName.requestFocus()
                            delay(500)
                            tvFlowName.showKeyboard()
                            tvFlowName.setSelection(tvFlowName.text.length)
                        }
                    }
                    options[1] -> {
                        AppSelectorBottomSheetFragment.newInstance().show(requireActivity().supportFragmentManager, BottomSheetTag.APP_SELECTOR)
                        root.showSnackBar(options[1])
                    }
                }
            }
        }

        btnDone.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStackImmediate()
        }
    }

    private fun setupObservers() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = appFlowViewModel.appFlowListStateFlow) { it: List<AppFlow> ->
            flowList = it
            val selectedFlow = it.getOrNull(selectedFlowPosition)

        }
    }

    fun addBottomDots(currentPage: Int) {
        val tvDotsArray = arrayOfNulls<TextView>(flowList.size)
        binding.llDots.removeAllViews()
        tvDotsArray.indices.forEach { i: Int ->
            tvDotsArray[i] = TextView(context).apply {
                text = HtmlCompat.fromHtml("&#8226;", HtmlCompat.FROM_HTML_MODE_LEGACY)
                textSize = 35f
                setTextColor(requireContext().color(R.color.purple_200))
            }
            binding.llDots.addView(tvDotsArray[i])
        }
        if (tvDotsArray.isNotEmpty()) {
            tvDotsArray[currentPage]?.setTextColor(requireContext().color(R.color.purple_500))
        }
    }

    inner class MainViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = flowList.size
        override fun createFragment(position: Int): Fragment = FlowAppsFragment.newInstance()
    }
}