package com.singularitycoder.flowlauncher.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.databinding.FragmentQuickSettingsBottomSheetBinding
import com.singularitycoder.flowlauncher.helper.drawable
import com.singularitycoder.flowlauncher.helper.setTransparentBackground
import com.singularitycoder.flowlauncher.helper.swipebutton.OnStateChangeListener
import dagger.hilt.android.AndroidEntryPoint

// Wifi
// Cellular
// Airplane mode
// Bluetooth
// Torch
// Calc
// Camera
// Barcode scanner
// Set Alarm
// Start Timer
// Auto rotate device
// SOS
// Panic Mode
// Lock screen
// Power button
// Notifications button
// Quick Note
// Brightness slider
// Volume Slider

@AndroidEntryPoint
class QuickSettingsBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = QuickSettingsBottomSheetFragment()
    }

    private lateinit var binding: FragmentQuickSettingsBottomSheetBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentQuickSettingsBottomSheetBinding.inflate(inflater, container, false)
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

    private fun FragmentQuickSettingsBottomSheetBinding.setupUI() {
        setBottomSheetBehaviour()
    }

    private fun FragmentQuickSettingsBottomSheetBinding.setupUserActionListeners() {
        swipeBtnSos.apply {
            setButtonBackground(requireContext().drawable(R.drawable.gradient_default))
            setOnStateChangeListener(object : OnStateChangeListener {
                override fun onStateChange(active: Boolean) {
                    Toast.makeText(requireContext(), "State: " + active, Toast.LENGTH_SHORT).show()
                    if (active) {
                        setButtonBackground(requireContext().drawable(R.drawable.gradient_default))
                    } else {
                        setButtonBackground(requireContext().drawable(R.drawable.gradient_default))
                    }
                }
            })
            setDisabledStateNotAnimated()
        }
        swipeBtnPanic.apply {
            setButtonBackground(requireContext().drawable(R.drawable.gradient_default))
            setOnStateChangeListener(object : OnStateChangeListener {
                override fun onStateChange(active: Boolean) {
                    Toast.makeText(requireContext(), "State: " + active, Toast.LENGTH_SHORT).show()
                    if (active) {
                        setButtonBackground(requireContext().drawable(R.drawable.gradient_default))
                    } else {
                        setButtonBackground(requireContext().drawable(R.drawable.gradient_default))
                    }
                }
            })
            setDisabledStateNotAnimated()
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

    private fun FragmentQuickSettingsBottomSheetBinding.observeForData() {

    }
}