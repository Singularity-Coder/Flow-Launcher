package com.singularitycoder.flowlauncher.addEditAppFlow.view

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.databinding.FragmentAddEditFlowBinding
import com.singularitycoder.flowlauncher.helper.blur.BlurStackOptimized
import com.singularitycoder.flowlauncher.helper.constants.BottomSheetTag
import com.singularitycoder.flowlauncher.helper.constants.HOME_LAYOUT_BLURRED_IMAGE
import com.singularitycoder.flowlauncher.helper.dpToPx
import com.singularitycoder.flowlauncher.helper.getHomeLayoutBlurredImageFileDir
import com.singularitycoder.flowlauncher.helper.setStatusBarColor
import com.singularitycoder.flowlauncher.helper.toBitmap
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
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

    private val viewPager2PageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            println("viewpager2: onPageScrollStateChanged")
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            println("viewpager2: onPageSelected")
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
        requireActivity().setStatusBarColor(R.color.black)
        setUpViewPager()
        binding.setupUI()
        binding.setupUserActionListeners()
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
        listOf("Edit Name", "Add Apps")
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
//        btnAddApps.setOnClickListener {
//            AppSelectorBottomSheetFragment.newInstance().show(requireActivity().supportFragmentManager, BottomSheetTag.APP_SELECTOR)
//        }
    }

    fun addBottomDots(currentPage: Int) {
        val tvDotsArray = arrayOfNulls<TextView>(/* get flows list size */0)
        binding.llDots.removeAllViews()
        for (i in tvDotsArray.indices) {
            tvDotsArray[i] = TextView(requireContext()).apply {
                text = Html.fromHtml("&#8226;")
                textSize = 35f
                setTextColor(resources.getColor(R.color.purple_200))
            }
            binding.llDots.addView(tvDotsArray[i])
        }
        if (tvDotsArray.isNotEmpty()) {
            tvDotsArray[currentPage]!!.setTextColor(resources.getColor(R.color.purple_500))
        }
    }

    inner class MainViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = 3
        override fun createFragment(position: Int): Fragment = FlowAppsFragment.newInstance()
    }
}