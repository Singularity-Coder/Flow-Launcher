package com.singularitycoder.flowlauncher.deviceActivity.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.databinding.FragmentDeviceActivityBottomSheetBinding
import com.singularitycoder.flowlauncher.deviceActivity.model.DeviceActivity
import com.singularitycoder.flowlauncher.deviceActivity.viewmodel.DeviceActivityViewModel
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.constants.allAndroidPermissions
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

    private val permissionsResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions: Map<String, @JvmSuppressWildcards Boolean>? ->
        permissions ?: return@registerForActivityResult
        permissions.entries.forEach { it: Map.Entry<String, @JvmSuppressWildcards Boolean> ->
            println("Permission status: ${it.key} = ${it.value}")
            val permission = it.key
            val isGranted = it.value
            when {
                isGranted -> {
                    // disable blocking layout and proceed
                }
                ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission) -> {
                    // permission permanently denied. Show settings dialog
                    // enable blocking layout and show popup to go to settings
                    requireContext().showAlertDialog(
                        title = "Grant Permissions",
                        message = "Grant all permissions in settings to register all device events.",
                        positiveBtnText = "Settings",
                        negativeBtnText = getString(R.string.tb_action_cancel),
                        positiveAction = {
                            requireContext().showPermissionSettings()
                        }
                    )
                }
                else -> {
                    // Permission denied but not permanently, tell user why you need it. Ideally provide a button to request it again and another to dismiss
                    // enable blocking layout
                }
            }
        }
//        if (permissions.values.all { it }.not()) {
//            requireContext().showPermissionSettings()
//        }
    }

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

    // https://stackoverflow.com/questions/15543186/how-do-i-create-colorstatelist-programmatically
    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentDeviceActivityBottomSheetBinding.setupUI() {
        setTransparentBackground()
        setBottomSheetBehaviour()
        grantPermissions()
        linearLayoutManager = LinearLayoutManager(
            /* context = */ context,
            /* orientation = */ RecyclerView.VERTICAL,
            /* reverseLayout = */ true
        )
        rvDeviceActivity.apply {
            layoutManager = linearLayoutManager
            adapter = deviceActivityAdapter
        }
    }

    private fun FragmentDeviceActivityBottomSheetBinding.setupUserActionListeners() {
        btnClear.onSafeClick {
            requireContext().showAlertDialog(
                title = "Delete all activity?",
                message = "Careful! This cannot be undone.",
                positiveBtnText = "Delete All",
                negativeBtnText = getString(R.string.tb_action_cancel),
                positiveAction = {
                    // TODO Do biometric auth to confirm delete all
                    deviceActivityViewModel.deleteAllDeviceActivity()
                    dismiss()
                }
            )
        }

        deviceActivityAdapter.setDeleteListener { it: DeviceActivity ->
            requireContext().showAlertDialog(
                title = "Delete this activity?",
                message = it.title,
                positiveBtnText = "Delete",
                negativeBtnText = getString(R.string.tb_action_cancel),
                positiveAction = {
                    // TODO Do biometric auth to confirm delete all
                    deviceActivityViewModel.deleteDeviceActivity(deviceActivity = it)
                    if (deviceActivityAdapter.deviceActivityList.isEmpty()) dismiss()
                }
            )
        }

        rvDeviceActivity.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (deviceActivityAdapter.deviceActivityList.isNotEmpty()) {
                    tvDate.text = deviceActivityAdapter.deviceActivityList.get(linearLayoutManager.findFirstVisibleItemPosition()).date.toDeviceActivityDate()
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentDeviceActivityBottomSheetBinding.observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = deviceActivityViewModel.deviceActivityListStateFlow) { activityList: List<DeviceActivity> ->
            val sortedDeviceActivitiesList = ArrayList<DeviceActivity>()
            val deviceActivityMap = HashMap<String?, ArrayList<DeviceActivity>>()
            activityList.forEach { it: DeviceActivity ->
                val key = it.date.toDeviceActivityDate()
                deviceActivityMap.put(
                    /* key = */ key,
                    /* value = */ deviceActivityMap.get(key)?.apply { add(it) } ?: ArrayList<DeviceActivity>().apply { add(it) }
                )
            }
            deviceActivityMap.keys.forEach { date: String? ->
                val preparedList = deviceActivityMap.get(date)?.mapIndexed { index, deviceActivity ->
                    if (index == deviceActivityMap.get(date)?.lastIndex) deviceActivity.isDateShown = true
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

    private fun grantPermissions() {
        permissionsResult.launch(allAndroidPermissions)
    }
}