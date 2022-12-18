package com.singularitycoder.flowlauncher.addEditAppFlow.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.addEditAppFlow.model.AppFlow
import com.singularitycoder.flowlauncher.addEditAppFlow.viewModel.AppFlowViewModel
import com.singularitycoder.flowlauncher.databinding.FragmentSelectedAppsBinding
import com.singularitycoder.flowlauncher.helper.GridSpacingItemDecoration
import com.singularitycoder.flowlauncher.helper.collectLatestLifecycleFlow
import com.singularitycoder.flowlauncher.helper.dpToPx
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class FlowSelectedAppsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(
            isAddFlow: Boolean,
            position: Int
        ) = FlowSelectedAppsFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ARG_PARAM_IS_ADD_FLOW, isAddFlow)
                putInt(ARG_PARAM_POSITION, position)
            }
        }
    }

    private lateinit var binding: FragmentSelectedAppsBinding

    private val flowAppsAdapter by lazy { FlowAppsAdapter() }
    private val appFlowViewModel: AppFlowViewModel by viewModels()

    private var isAddFlow = false
    private var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isAddFlow = arguments?.getBoolean(ARG_PARAM_IS_ADD_FLOW) ?: false
        position = arguments?.getInt(ARG_PARAM_POSITION) ?: 0
        println("logggggg isAddFlow: $isAddFlow")
        println("logggggg position: $position")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
        rvApps.isVisible = isAddFlow.not()
        llAddFlow.isVisible = isAddFlow
        rvApps.isVisible = isAddFlow.not()
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

        setAddNewFlowClickListener()
    }

    private fun FragmentSelectedAppsBinding.setAddNewFlowClickListener() {
        if (isAddFlow.not()) return
        root.setOnClickListener {
            lifecycleScope.launch {
                appFlowViewModel.deleteAllAppFlows()
                val allAppFlows = appFlowViewModel.getAllAppFlows().map {
                    it.isSelected = false
                    it
                }
                appFlowViewModel.addAllAppFlows(allAppFlows)
                appFlowViewModel.addAppFlow(
                    AppFlow(
                        appFlowName = "App Flow ${allAppFlows.size + 1}",
                        isSelected = true,
                        appList = emptyList()
                    )
                )
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeForData() {
        // observe from App Flow table - get list of apps selected along with FLow Name
        // Set list items grid view along with Text field for adding Flow name
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = appFlowViewModel.appFlowListStateFlow) { it: List<AppFlow> ->
            if (isAddFlow) return@collectLatestLifecycleFlow
            binding.llNoAppsPlaceholder.isVisible = it.isEmpty()
            val selectedFlow = it.getOrNull(position)
            flowAppsAdapter.flowAppList = selectedFlow?.appList ?: emptyList()
            withContext(Main) {
                // https://stackoverflow.com/questions/43221847/cannot-call-this-method-while-recyclerview-is-computing-a-layout-or-scrolling-wh
                flowAppsAdapter.notifyDataSetChanged()
            }
        }
    }
}

private const val ARG_PARAM_IS_ADD_FLOW = "ARG_PARAM_POSITION"
private const val ARG_PARAM_POSITION = "ARG_PARAM_POSITION"