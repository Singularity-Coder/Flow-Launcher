package com.singularitycoder.flowlauncher.deviceActivity.view

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.databinding.FragmentDeviceActivityBottomSheetBinding
import com.singularitycoder.flowlauncher.deviceActivity.model.DeviceActivity
import com.singularitycoder.flowlauncher.deviceActivity.viewmodel.DeviceActivityViewModel
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.home.model.App
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeviceActivityBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = DeviceActivityBottomSheetFragment()
    }

    private val deviceActivityViewModel by viewModels<DeviceActivityViewModel>()
    private val appSelectorAdapter: DeviceActivityAdapter by lazy { DeviceActivityAdapter() }
    private val selectedAppsList = mutableListOf<App>()

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var binding: FragmentDeviceActivityBottomSheetBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDeviceActivityBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
    private fun FragmentDeviceActivityBottomSheetBinding.setupUI() {
        setTransparentBackground()
        setBottomSheetBehaviour()
        linearLayoutManager = LinearLayoutManager(context)
        rvApps.apply {
            layoutManager = linearLayoutManager
            adapter = appSelectorAdapter
        }
    }

    private fun FragmentDeviceActivityBottomSheetBinding.setupUserActionListeners() {
        appSelectorAdapter.setCheckboxListener { isChecked: Boolean, app: App ->
            if (isChecked) selectedAppsList.add(app) else selectedAppsList.remove(app)
        }

        rvApps.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
//                println("loggggg dx: $dx, dy: $dy")
                if (appSelectorAdapter.selectedAppList.isNotEmpty()) {
                    tvAlphabet.text = appSelectorAdapter.selectedAppList.get(linearLayoutManager.findFirstVisibleItemPosition()).title.substring(0, 1)
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentDeviceActivityBottomSheetBinding.observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = deviceActivityViewModel.appFlowListStateFlow) { it: List<DeviceActivity> ->
            val sortedAppList = ArrayList<App>()
            val appMap = HashMap<String, ArrayList<App>>()
//            appFlowList.firstOrNull()?.appList?.forEach { it: App ->
//                appMap.put(
//                    it.title.subSequence(0, 1).toString(),
//                    appMap.get(it.title.subSequence(0, 1).toString())?.apply {
//                        add(it)
//                    } ?: ArrayList<App>().apply {
//                        add(it)
//                    }
//                )
//            }
            appMap.keys.sorted().forEach { it: String ->
                val preparedList = appMap.get(it)?.mapIndexed { index, contact ->
                    if (index == 0) contact.isAlphabetShown = true
                    contact
                } ?: emptyList()
                sortedAppList.addAll(preparedList)
            }
//            val selectedAppFlowAppList = deviceActivityViewModel.getAppFlowById(selectedFlowId)?.appList
//            appSelectorAdapter.selectedAppList = sortedAppList.map { sortedApp: App ->
//                sortedApp.isSelected = false
//                selectedAppFlowAppList?.forEach { selectedApp: App ->
//                    if (sortedApp.packageName == selectedApp.packageName) {
//                        sortedApp.isSelected = true
//                        selectedAppsList.add(sortedApp)
//                    }
//                }
//                sortedApp
//            }
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