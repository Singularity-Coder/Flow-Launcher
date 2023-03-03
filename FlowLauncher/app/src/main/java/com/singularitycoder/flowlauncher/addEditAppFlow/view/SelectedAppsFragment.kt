package com.singularitycoder.flowlauncher.addEditAppFlow.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.SharedViewModel
import com.singularitycoder.flowlauncher.addEditAppFlow.model.AppFlow
import com.singularitycoder.flowlauncher.addEditAppFlow.model.SelectedFlowArgs
import com.singularitycoder.flowlauncher.addEditAppFlow.viewModel.AppFlowViewModel
import com.singularitycoder.flowlauncher.databinding.FragmentSelectedAppsBinding
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.constants.AppGrid
import com.singularitycoder.flowlauncher.helper.constants.BottomSheetTag
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ARG_PARAM_IS_ADD_FLOW = "ARG_PARAM_POSITION"
private const val ARG_PARAM_POSITION = "ARG_PARAM_POSITION"
private const val ARG_PARAM_APP_FLOW_ID = "ARG_PARAM_APP_FLOW_ID"

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
    private val sharedViewModel: SharedViewModel by activityViewModels()
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
        binding.observeForData()
        binding.setupUI()
        binding.setupUserActionListeners()
    }

    private fun FragmentSelectedAppsBinding.setupUI() {
        rvApps.apply {
            layoutManager = GridLayoutManager(context, AppGrid.COLUMNS)
            adapter = selectedAppsAdapter
            addItemDecoration(
                GridSpacingItemDecoration(
                    spanCount = AppGrid.COLUMNS /* columns */,
                    spacing = AppGrid.ONE_APP_SIDE_SPACING /* px */,
                )
            )
        }
    }

    private fun FragmentSelectedAppsBinding.setupUserActionListeners() {
        root.viewTreeObserver.addOnGlobalLayoutListener {
            // Do this when view inflate complete
        }

        llAddFlow.onSafeClick {
            cardAddFlow.performClick()
        }

        cardAddFlow.onSafeClick {
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

        btnShowAppSelectorSheet.onSafeClick {
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
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = sharedViewModel.selectedAppArgsFlow) { it: SelectedFlowArgs ->
//            isAddFlow = it.isAddFlow
//            position = it.position
            appFlowId = it.appFlowId
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = appFlowViewModel.appFlowListStateFlow) { it: List<AppFlow> ->
            isAddFlow = it.size == position
            rvApps.isVisible = isAddFlow.not()
            llAddFlow.isVisible = isAddFlow
            llNoAppsPlaceholder.isVisible = isAddFlow.not()
            layoutShimmerAppLoader.shimmerLoader.isVisible = false
            if (isAddFlow) return@collectLatestLifecycleFlow
            val selectedFlow = it.getOrNull(position)
            Preferences.write(requireContext()).putInt(Preferences.KEY_SELECTED_FLOW_POSITION, position).apply()
            llNoAppsPlaceholder.isVisible = selectedFlow?.appList?.isEmpty() == true
            selectedAppsAdapter.flowAppList = selectedFlow?.appList ?: emptyList()
            withContext(Main) {
                // https://stackoverflow.com/questions/43221847/cannot-call-this-method-while-recyclerview-is-computing-a-layout-or-scrolling-wh
                selectedAppsAdapter.notifyDataSetChanged()
                layoutShimmerAppLoader.shimmerLoader.isVisible = false
            }
        }
    }
}