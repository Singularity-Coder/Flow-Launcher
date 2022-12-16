package com.singularitycoder.flowlauncher.addEditAppFlow.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.singularitycoder.flowlauncher.databinding.FragmentSelectedAppsBinding
import com.singularitycoder.flowlauncher.helper.GridSpacingItemDecoration
import com.singularitycoder.flowlauncher.helper.dpToPx
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FlowAppsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = FlowAppsFragment()
    }

    private lateinit var binding: FragmentSelectedAppsBinding

    private val flowAppsAdapter by lazy { FlowAppsAdapter() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSelectedAppsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    private fun FragmentSelectedAppsBinding.setupUI() {
        rvApps.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = flowAppsAdapter
            addItemDecoration(
                GridSpacingItemDecoration(
                    spanCount = 4 /* columns */,
                    spacing = 24.dpToPx() /* px */,
                    includeEdge = false
                )
            )
        }
    }

    private fun FragmentSelectedAppsBinding.setupUserActionListeners() {
        root.viewTreeObserver.addOnGlobalLayoutListener {
            // Do this when view inflate complete
            // root.layoutParams.width = deviceWidth() - 60.dpToPx()
        }
    }

    private fun observeForData() {
        // observe from App Flow table - get list of apps selected along with FLow Name
        // Set list items grid view along with Text field for adding Flow name
    }
}