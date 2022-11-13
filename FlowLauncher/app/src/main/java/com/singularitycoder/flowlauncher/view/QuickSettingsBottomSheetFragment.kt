package com.singularitycoder.flowlauncher.view

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
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
import com.google.android.material.slider.Slider
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.databinding.FragmentQuickSettingsBottomSheetBinding
import com.singularitycoder.flowlauncher.helper.color
import com.singularitycoder.flowlauncher.helper.dpToPx
import com.singularitycoder.flowlauncher.helper.drawable
import com.singularitycoder.flowlauncher.helper.setTransparentBackground
import com.singularitycoder.flowlauncher.helper.swipebutton.OnStateChangeListener
import dagger.hilt.android.AndroidEntryPoint

// Try Blur background

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

// Accessibility permission api - to control power btn, notifcations, etc

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

    // https://stackoverflow.com/questions/15543186/how-do-i-create-colorstatelist-programmatically
    private fun FragmentQuickSettingsBottomSheetBinding.setupUI() {
        setBottomSheetBehaviour()
        layoutWifi.apply {
            ivIcon.setImageDrawable(requireContext().drawable(R.drawable.ic_round_wifi_24))
            tvPlaceholder.text = "Wifi"
            tvName.text = "Not Connected"
        }
        layoutNetwork.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.cell_tower_black_24dp))
        }
        layoutBluetooth.apply {
            ivIcon.setImageDrawable(requireContext().drawable(R.drawable.ic_baseline_bluetooth_24))
            tvPlaceholder.text = "Bluetooth"
            tvName.text = "Not Connected"
        }
        layoutAirplaneMode.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.ic_round_airplanemode_active_24))
        }
        layoutTorch.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.ic_round_flashlight_on_24))
        }
        layoutLocation.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.location_on_black_24dp))
        }
        layoutCamera.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.ic_baseline_camera_alt_24))
        }
        layoutBarcodeScanner.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.ic_round_qr_code_scanner_24))
        }
        layoutAlarm.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.ic_round_access_alarm_24))
        }
        layoutTimer.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.ic_outline_timer_24))
        }
        layoutQuickNote.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.ic_round_edit_note_24))
        }
        layoutRotateScreen.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.ic_round_screen_rotation_24))
        }
        layoutSettings.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.ic_round_settings_24))
        }
        layoutNotifications.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.ic_round_notifications_24))
        }
        layoutLockScreen.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.ic_round_lock_24))
        }
        layoutPower.apply {
            ivAppIcon.setImageDrawable(requireContext().drawable(R.drawable.ic_round_power_settings_new_24))
            val states = arrayOf(
                intArrayOf(android.R.attr.state_enabled), // enabled
                intArrayOf(-android.R.attr.state_enabled), // disabled
                intArrayOf(-android.R.attr.state_checked), // unchecked
                intArrayOf(android.R.attr.state_pressed) // pressed
            )
            val colors = intArrayOf(
                requireContext().color(R.color.md_red_500),
                requireContext().color(R.color.title_color),
                requireContext().color(R.color.md_red_500),
                requireContext().color(R.color.md_red_500),
            )
            val colorsBackground = intArrayOf(
                requireContext().color(R.color.md_red_50),
                requireContext().color(R.color.title_color),
                requireContext().color(R.color.md_red_50),
                requireContext().color(R.color.md_red_50),
            )
            root.backgroundTintList = ColorStateList(states, colorsBackground)
            ivAppIcon.imageTintList = ColorStateList(states, colors)
        }
    }

    private fun FragmentQuickSettingsBottomSheetBinding.setupUserActionListeners() {
//        sliderBrightness.apply {
//            valueFrom = 0.0F
//            valueTo = 100.0F
//            value = 10.0F   // starting value
//            trackHeight = 52.dpToPx()
//            setCustomThumbDrawable(R.drawable.shape_rounded_slider_thumb)
//            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
//                override fun onStartTrackingTouch(slider: Slider) = Unit // Responds to when slider's touch event is being started
//                override fun onStopTrackingTouch(slider: Slider) = Unit // Responds to when slider's touch event is being stopped
//            })
//            addOnChangeListener { slider, value, fromUser ->
//                // Responds to when slider's value is changed
//                println("Continuous Slider value is $value")
//            }
//        }
//        sliderVolume.apply {
//            valueFrom = 0.0F
//            valueTo = 100.0F
//            value = 10.0F   // starting value
//            trackHeight = 52.dpToPx()
//            setCustomThumbDrawable(R.drawable.shape_rounded_slider_thumb)
//            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
//                override fun onStartTrackingTouch(slider: Slider) = Unit // Responds to when slider's touch event is being started
//                override fun onStopTrackingTouch(slider: Slider) = Unit // Responds to when slider's touch event is being stopped
//            })
//            addOnChangeListener { slider, value, fromUser ->
//                // Responds to when slider's value is changed
//                println("Continuous Slider value is $value")
//            }
//        }
        swipeBtnSos.apply {
            setButtonBackground(requireContext().drawable(R.drawable.gradient_red))
            setOnStateChangeListener(object : OnStateChangeListener {
                override fun onStateChange(active: Boolean) {
                    Toast.makeText(requireContext(), "State: " + active, Toast.LENGTH_SHORT).show()
                    if (active) {
                        setButtonBackground(requireContext().drawable(R.drawable.gradient_red))
                    } else {
                        setButtonBackground(requireContext().drawable(R.drawable.gradient_red))
                    }
                }
            })
            setDisabledStateNotAnimated()
        }
        swipeBtnPanic.apply {
            setButtonBackground(requireContext().drawable(R.drawable.gradient_red))
            setOnStateChangeListener(object : OnStateChangeListener {
                override fun onStateChange(active: Boolean) {
                    Toast.makeText(requireContext(), "State: " + active, Toast.LENGTH_SHORT).show()
                    if (active) {
                        setButtonBackground(requireContext().drawable(R.drawable.gradient_red))
                    } else {
                        setButtonBackground(requireContext().drawable(R.drawable.gradient_red))
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