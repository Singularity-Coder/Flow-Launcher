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
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.addEditAppFlow.model.AppFlow
import com.singularitycoder.flowlauncher.addEditAppFlow.viewModel.AppFlowViewModel
import com.singularitycoder.flowlauncher.databinding.FragmentSelectedAppsBinding
import com.singularitycoder.flowlauncher.helper.GridSpacingItemDecoration
import com.singularitycoder.flowlauncher.helper.collectLatestLifecycleFlow
import com.singularitycoder.flowlauncher.helper.constants.BottomSheetTag
import com.singularitycoder.flowlauncher.helper.dpToPx
import com.singularitycoder.flowlauncher.helper.hideKeyboard
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
            position: Int,
            appFlowId: Long
        ) = FlowSelectedAppsFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ARG_PARAM_IS_ADD_FLOW, isAddFlow)
                putInt(ARG_PARAM_POSITION, position)
                putLong(ARG_PARAM_APP_FLOW_ID, appFlowId)
            }
        }
    }

    private lateinit var binding: FragmentSelectedAppsBinding

    private val selectedAppsAdapter by lazy { SelectedAppsAdapter() }
    private val appFlowViewModel: AppFlowViewModel by viewModels()

    private var isAddFlow = false
    private var position = 0
    private var appFlowId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isAddFlow = arguments?.getBoolean(ARG_PARAM_IS_ADD_FLOW) ?: false
        position = arguments?.getInt(ARG_PARAM_POSITION) ?: 0
        appFlowId = arguments?.getLong(ARG_PARAM_APP_FLOW_ID) ?: 0L
        println("logggggg isAddFlow: $isAddFlow")
        println("logggggg position: $position")
        println("logggggg appFlowId: $appFlowId")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSelectedAppsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        binding.observeForData()
    }

    private fun FragmentSelectedAppsBinding.setupUI() {
        rvApps.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = selectedAppsAdapter
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
        }

        llAddFlow.setOnClickListener {
            cardAddFlow.performClick()
        }

        cardAddFlow.setOnClickListener {
            lifecycleScope.launch {
                appFlowViewModel.addAppFlow(
                    AppFlow(
                        appFlowName = "App Flow",
                        isSelected = false,
                        appList = emptyList()
                    )
                )
            }
        }

        btnShowAppSelectorSheet.setOnClickListener {
            AppSelectorBottomSheetFragment.newInstance(selectedFlowId = appFlowId).show(
                /* manager = */ requireActivity().supportFragmentManager,
                /* tag = */ BottomSheetTag.APP_SELECTOR
            )
        }

        rvApps.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) requireActivity().hideKeyboard()
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentSelectedAppsBinding.observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = appFlowViewModel.appFlowListStateFlow) { it: List<AppFlow> ->
            isAddFlow = it.size == position
            rvApps.isVisible = isAddFlow.not()
            llAddFlow.isVisible = isAddFlow
            rvApps.isVisible = isAddFlow.not()
            if (isAddFlow) return@collectLatestLifecycleFlow
            val selectedFlow = it.getOrNull(position)
            binding.llNoAppsPlaceholder.isVisible = selectedFlow?.appList?.isEmpty() == true
            selectedAppsAdapter.flowAppList = selectedFlow?.appList ?: emptyList()
            withContext(Main) {
                // https://stackoverflow.com/questions/43221847/cannot-call-this-method-while-recyclerview-is-computing-a-layout-or-scrolling-wh
                selectedAppsAdapter.notifyDataSetChanged()
                layoutShimmerAppLoader.shimmerLoader.isVisible = false
            }
        }
    }
}

private const val ARG_PARAM_IS_ADD_FLOW = "ARG_PARAM_POSITION"
private const val ARG_PARAM_POSITION = "ARG_PARAM_POSITION"
private const val ARG_PARAM_APP_FLOW_ID = "ARG_PARAM_APP_FLOW_ID"