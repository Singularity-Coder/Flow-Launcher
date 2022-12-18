package com.singularitycoder.flowlauncher.addEditAppFlow.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.databinding.FragmentAppSelectorBottomSheetBinding
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.home.model.App
import com.singularitycoder.flowlauncher.home.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

// TODO Letter strip
// Maybe pagination for room
// Shimmer loading until it fetches all apps from DB
@AndroidEntryPoint
class AppSelectorBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = AppSelectorBottomSheetFragment()
    }

    private val homeViewModel: HomeViewModel by viewModels()
    private val appSelectorAdapter: AppSelectorAdapter by lazy { AppSelectorAdapter() }

    private lateinit var binding: FragmentAppSelectorBottomSheetBinding

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

    override fun onResume() {
        super.onResume()
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
        rvApps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appSelectorAdapter
        }
        appSelectorAdapter.selectedAppList = requireContext().appInfoList().map { item: ResolveInfo? ->
            App().apply {
                title = item?.loadLabel(requireContext().packageManager).toString()
                packageName = item?.activityInfo?.packageName ?: ""
                icon = item?.activityInfo?.loadIcon(requireContext().packageManager)
            }
        }
        appSelectorAdapter.notifyDataSetChanged()
    }

    // https://stackoverflow.com/questions/41693154/custom-seekbar-thumb-size-color-and-background
    private fun FragmentAppSelectorBottomSheetBinding.setupUserActionListeners() {

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

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentAppSelectorBottomSheetBinding.observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = homeViewModel.appListStateFlow) { it: List<App> ->
            appSelectorAdapter.selectedAppList = it
            appSelectorAdapter.notifyDataSetChanged()
        }
    }
}