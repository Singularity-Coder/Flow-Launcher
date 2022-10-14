package com.singularitycoder.flowlauncher.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.singularitycoder.flowlauncher.databinding.FragmentAddEditFlowBinding
import com.singularitycoder.flowlauncher.helper.dpToPx
import com.singularitycoder.flowlauncher.helper.setShowSideItems
import dagger.hilt.android.AndroidEntryPoint

// TODO
// Add remove apps - new checklist screen
// Disable apps that are not selected
// Set recycler view for apps
// Flow name
// Button to add remove apps
// Done button only
// Add new flow - new card in view pager

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAddEditFlowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewPager()
    }

    override fun onDestroy() {
        super.onDestroy()
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

    inner class MainViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = 3
        override fun createFragment(position: Int): Fragment = AppsFragment.newInstance()
    }
}