package com.singularitycoder.flowlauncher.addEditAppFlow.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.addEditAppFlow.model.AppFlow
import com.singularitycoder.flowlauncher.addEditAppFlow.viewModel.AppFlowViewModel
import com.singularitycoder.flowlauncher.databinding.FragmentSelectedAppsBinding
import com.singularitycoder.flowlauncher.helper.GridSpacingItemDecoration
import com.singularitycoder.flowlauncher.helper.collectLatestLifecycleFlow
import com.singularitycoder.flowlauncher.helper.dpToPx
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class FlowAppsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(isAddFlow: Boolean) = FlowAppsFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ARG_PARAM_IS_ADD_FLOW, isAddFlow)
            }
        }
    }

    private lateinit var binding: FragmentSelectedAppsBinding

    private val flowAppsAdapter by lazy { FlowAppsAdapter() }
    private val appFlowViewModel: AppFlowViewModel by viewModels()

    private var isAddFlow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isAddFlow = arguments?.getBoolean(ARG_PARAM_IS_ADD_FLOW) ?: false
    }

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
        rvApps.isVisible = isAddFlow.not()
        llAddFlow.isVisible = isAddFlow
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

    @SuppressLint("NotifyDataSetChanged")
    private fun observeForData() {
        // observe from App Flow table - get list of apps selected along with FLow Name
        // Set list items grid view along with Text field for adding Flow name
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = appFlowViewModel.appFlowListStateFlow) { it: List<AppFlow> ->
            val selectedFlow = it.firstOrNull { it.isSelected }
            flowAppsAdapter.flowAppList = selectedFlow?.appList ?: emptyList()
            withContext(Dispatchers.Main) {
                // https://stackoverflow.com/questions/43221847/cannot-call-this-method-while-recyclerview-is-computing-a-layout-or-scrolling-wh
                flowAppsAdapter.notifyDataSetChanged()
            }
        }
    }
}

private const val ARG_PARAM_IS_ADD_FLOW = "ARG_PARAM_POSITION"