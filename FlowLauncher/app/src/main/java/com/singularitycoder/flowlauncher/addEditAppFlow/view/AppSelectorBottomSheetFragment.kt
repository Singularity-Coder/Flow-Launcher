package com.singularitycoder.flowlauncher.addEditAppFlow.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.addEditAppFlow.model.AppFlow
import com.singularitycoder.flowlauncher.addEditAppFlow.viewModel.AppFlowViewModel
import com.singularitycoder.flowlauncher.databinding.FragmentAppSelectorBottomSheetBinding
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.home.model.App
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO Letter strip
// Maybe pagination for room
// Shimmer loading until it fetches all apps from DB
// Categorise them by letters
@AndroidEntryPoint
class AppSelectorBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance(selectedFlowId: Long) = AppSelectorBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putLong(SELECTED_FLOW_ID, selectedFlowId)
            }
        }
    }

    private val appFlowViewModel: AppFlowViewModel by viewModels()
    private val appSelectorAdapter: AppSelectorAdapter by lazy { AppSelectorAdapter() }
    private lateinit var linearLayoutManager: LinearLayoutManager

    private val selectedAppsList = mutableListOf<App>()

    private var selectedFlowId = 0L

    private lateinit var binding: FragmentAppSelectorBottomSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedFlowId = arguments?.getLong(SELECTED_FLOW_ID, 0) ?: 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAppSelectorBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTransparentBackground()
        binding.setupUI()
        binding.setupUserActionListeners()
        binding.observeForData()
    }

    // https://stackoverflow.com/questions/42301845/android-bottom-sheet-after-state-changed
    // https://stackoverflow.com/questions/35937453/set-state-of-bottomsheetdialogfragment-to-expanded
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface: DialogInterface? ->
            // FIXME not working
        }
        return dialog
    }

    // https://stackoverflow.com/questions/40616833/bottomsheetdialogfragment-listen-to-dismissed-by-user-event
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
    }

    // https://stackoverflow.com/questions/15543186/how-do-i-create-colorstatelist-programmatically
    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentAppSelectorBottomSheetBinding.setupUI() {
        setBottomSheetBehaviour()
        linearLayoutManager = LinearLayoutManager(context)
        rvApps.apply {
            layoutManager = linearLayoutManager
            adapter = appSelectorAdapter
        }
    }

    private fun FragmentAppSelectorBottomSheetBinding.setupUserActionListeners() {
        btnDone.setOnClickListener {
            println("loggggg selectedAppsList: ${selectedAppsList.map { it.title }}")
            println("loggggg selectedFlowId: $selectedFlowId}")
            lifecycleScope.launch {
                val appFlow = appFlowViewModel.getAppFlowById(selectedFlowId)
                appFlowViewModel.updateAppFlow(
                    appFlow = AppFlow(
                        id = appFlow?.id ?: -1,
                        appFlowName = appFlow?.appFlowName ?: "",
                        isSelected = appFlow?.isSelected ?: false,
                        appList = selectedAppsList
                    )
                )
                withContext(Main) {
                    dismiss()
                }
            }
        }

        appSelectorAdapter.setCheckboxListener { isChecked: Boolean, app: App ->
            if (isChecked) selectedAppsList.add(app) else selectedAppsList.remove(app)
        }

        rvApps.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                println("loggggg dx: $dx, dy: $dy")
                if (appSelectorAdapter.selectedAppList.isNotEmpty()) {
                    tvAlphabet.text = appSelectorAdapter.selectedAppList.get(linearLayoutManager.findFirstVisibleItemPosition()).title.substring(0, 1)
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentAppSelectorBottomSheetBinding.observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = appFlowViewModel.appFlowListStateFlow) { appFlowList: List<AppFlow> ->
            val sortedAppList = ArrayList<App>()
            val appMap = HashMap<String, ArrayList<App>>()
            appFlowList.firstOrNull()?.appList?.forEach { it: App ->
                appMap.put(
                    it.title.subSequence(0, 1).toString(),
                    appMap.get(it.title.subSequence(0, 1).toString())?.apply {
                        add(it)
                    } ?: ArrayList<App>().apply {
                        add(it)
                    }
                )
            }
            appMap.keys.sorted().forEach { it: String ->
                val preparedList = appMap.get(it)?.mapIndexed { index, contact ->
                    if (index == 0) contact.isAlphabetShown = true
                    contact
                } ?: emptyList()
                sortedAppList.addAll(preparedList)
            }
            val selectedAppFlowAppList = appFlowViewModel.getAppFlowById(selectedFlowId)?.appList
            appSelectorAdapter.selectedAppList = sortedAppList.map { sortedApp: App ->
                selectedAppFlowAppList?.forEach { selectedApp: App ->
                    if (sortedApp.packageName == selectedApp.packageName) {
                        sortedApp.isSelected = true
                        selectedAppsList.add(sortedApp)
                    }
                }
                sortedApp
            }
            appSelectorAdapter.notifyDataSetChanged()
            layoutShimmerAppSelectorLoader.shimmerLoader.isVisible = false
        }
    }

    private fun setBottomSheetBehaviour() {
        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout? ?: return
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
//        bottomSheet.layoutParams.height = deviceHeight()
//        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        var oldState = BottomSheetBehavior.STATE_HIDDEN
        behavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                println("bottom sheet state: ${behavior.state}")
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        behavior.state = BottomSheetBehavior.STATE_HIDDEN
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> Unit
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        oldState = BottomSheetBehavior.STATE_EXPANDED
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> Unit
                    BottomSheetBehavior.STATE_HIDDEN -> Unit
                    BottomSheetBehavior.STATE_SETTLING -> {
                        if (oldState == BottomSheetBehavior.STATE_EXPANDED) {
                            behavior.state = BottomSheetBehavior.STATE_HIDDEN
                        }
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // React to dragging events
            }
        })
    }
}

const val SELECTED_FLOW_ID = "SELECTED_FLOW_POSITION"