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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeviceActivityBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = DeviceActivityBottomSheetFragment()
    }

    private val deviceActivityViewModel by viewModels<DeviceActivityViewModel>()
    private val deviceActivityAdapter: DeviceActivityAdapter by lazy { DeviceActivityAdapter() }

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
        rvDeviceActivity.apply {
            layoutManager = linearLayoutManager
            adapter = deviceActivityAdapter
        }
    }

    private fun FragmentDeviceActivityBottomSheetBinding.setupUserActionListeners() {
        deviceActivityAdapter.setDeleteListener { it: DeviceActivity ->

        }

        rvDeviceActivity.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (deviceActivityAdapter.deviceActivityList.isNotEmpty()) {
                    tvDate.text = deviceActivityAdapter.deviceActivityList.get(linearLayoutManager.findFirstVisibleItemPosition()).title.substring(0, 1)
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentDeviceActivityBottomSheetBinding.observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = deviceActivityViewModel.deviceActivityListStateFlow) { activityList: List<DeviceActivity> ->
            val sortedDeviceActivitiesList = ArrayList<DeviceActivity>()
            val deviceActivityMap = HashMap<Long, ArrayList<DeviceActivity>>()
            activityList.forEach { it: DeviceActivity ->
                val key = it.date
                deviceActivityMap.put(
                    /* key = */ key,
                    /* value = */ deviceActivityMap.get(key)?.apply { add(it) } ?: ArrayList<DeviceActivity>().apply { add(it) }
                )
            }
            deviceActivityMap.keys.sorted().forEach { date: Long ->
                val preparedList = deviceActivityMap.get(date)?.mapIndexed { index, deviceActivity ->
                    if (index == 0) deviceActivity.isDateShown = true
                    deviceActivity
                } ?: emptyList()
                sortedDeviceActivitiesList.addAll(preparedList)
            }
            deviceActivityAdapter.deviceActivityList = sortedDeviceActivitiesList
            deviceActivityAdapter.notifyDataSetChanged()
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