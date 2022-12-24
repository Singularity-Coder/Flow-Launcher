package com.singularitycoder.flowlauncher.quickSettings

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.databinding.FragmentQuickSettingsBottomSheetBinding
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.constants.Broadcast
import com.singularitycoder.flowlauncher.helper.swipebutton.OnStateChangeListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


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

// Accessibility permission api - to control power btn, notifications, etc

// Lock Unlock phone: https://stackoverflow.com/questions/14352648/how-to-lock-unlock-screen-programmatically
// https://rdcworld-android.blogspot.com/2012/03/lock-phone-screen-programmtically.html

@AndroidEntryPoint
class QuickSettingsBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = QuickSettingsBottomSheetFragment()
    }

    @Inject
    lateinit var audioManager: AudioManager

    @Inject
    lateinit var wifiManager: WifiManager

    private lateinit var binding: FragmentQuickSettingsBottomSheetBinding

    private val cameraPermissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean? ->
        isGranted ?: return@registerForActivityResult

        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
            requireContext().showPermissionSettings()
            return@registerForActivityResult
        }

        if (isGranted.not()) {
            requestCameraPermission()
            return@registerForActivityResult
        }

        if (requireContext().isCameraPermissionGranted()) {
            requireActivity().launchApp("com.android.camera2")
        }
    }

    private val quickSettingsBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val volumeFactor = Math.ceil(100.0 / 15.0).toInt() // Step size of volume
            when (intent?.action) {
                Broadcast.VOLUME_RAISED -> {
                    binding.sliderVolume.progress = (binding.sliderVolume.progress / volumeFactor) + volumeFactor
                }
                Broadcast.VOLUME_LOWERED -> {
                    binding.sliderVolume.progress = (binding.sliderVolume.progress / volumeFactor) - volumeFactor
                }
            }
        }
    }

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

    override fun onResume() {
        super.onResume()
        activity?.registerReceiver(quickSettingsBroadcastReceiver, IntentFilter(Broadcast.VOLUME_RAISED))
        activity?.registerReceiver(quickSettingsBroadcastReceiver, IntentFilter(Broadcast.VOLUME_LOWERED))
        activity?.registerReceiver(quickSettingsBroadcastReceiver, IntentFilter("android.intent.action.SERVICE_STATE")) // For airplane mode
    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(quickSettingsBroadcastReceiver)
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
        setCurrentScreenBrightness()
        setCurrentVolume()
        setCurrentWifiStatus()
        setCurrentAirplaneModeStatus()
        layoutWifi.apply {
            ivIcon.setImageDrawable(requireContext().drawable(R.drawable.ic_round_wifi_24))
            tvPlaceholder.text = "Wifi"
            tvName.text = "Not Connected"
        }
        layoutNetwork.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.cell_tower_black_24dp))
        }
        layoutBluetooth.apply {
            ivIcon.setImageDrawable(requireContext().drawable(R.drawable.ic_round_bluetooth_24))
            tvPlaceholder.text = "Bluetooth"
            tvName.text = "Not Connected"
        }
        layoutAirplaneMode.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.ic_round_airplanemode_active_24))
        }
        layoutTorch.ivAppIcon.apply {
            // https://stackoverflow.com/questions/6068803/how-to-turn-on-front-flash-light-programmatically-in-android#:~:text=For%20turning%20on%2Foff%20flashlight%3A&text=The%20main%20parameter%20used%20here,to%20turn%20on%20camera%20flashlight.
            setImageDrawable(requireContext().drawable(R.drawable.ic_round_flashlight_on_24))
            if (requireContext().isFlashAvailable().not()) return@apply
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
            val colors500 = intArrayOf(
                requireContext().color(R.color.md_red_500),
                requireContext().color(R.color.title_color),
                requireContext().color(R.color.md_red_500),
                requireContext().color(R.color.md_red_500),
            )
            val colors200 = intArrayOf(
                requireContext().color(R.color.md_red_200),
                requireContext().color(R.color.title_color),
                requireContext().color(R.color.md_red_200),
                requireContext().color(R.color.md_red_200),
            )
            val colorsBackground = intArrayOf(
                requireContext().color(R.color.md_red_50),
                requireContext().color(R.color.title_color),
                requireContext().color(R.color.md_red_50),
                requireContext().color(R.color.md_red_50),
            )
            root.backgroundTintList = ColorStateList(states, colorsBackground)
            ivAppIcon.imageTintList = ColorStateList(states, colors500)
            root.rippleColor = ColorStateList(states, colors200)
        }
    }

    // https://stackoverflow.com/questions/41693154/custom-seekbar-thumb-size-color-and-background
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
            setButtonBackground(requireContext().drawable(R.drawable.gradient_red_panic))
            setOnStateChangeListener(object : OnStateChangeListener {
                override fun onStateChange(active: Boolean) {
                    Toast.makeText(requireContext(), "State: " + active, Toast.LENGTH_SHORT).show()
                    if (active) {
                        setButtonBackground(requireContext().drawable(R.drawable.gradient_red_panic))
                    } else {
                        setButtonBackground(requireContext().drawable(R.drawable.gradient_red_panic))
                    }
                }
            })
            setDisabledStateNotAnimated()
        }
        swipeBtnPanic.apply {
            setButtonBackground(requireContext().drawable(R.drawable.gradient_red_panic))
            setOnStateChangeListener(object : OnStateChangeListener {
                override fun onStateChange(active: Boolean) {
                    Toast.makeText(requireContext(), "State: " + active, Toast.LENGTH_SHORT).show()
                    if (active) {
                        setButtonBackground(requireContext().drawable(R.drawable.gradient_red_panic))
                    } else {
                        setButtonBackground(requireContext().drawable(R.drawable.gradient_red_panic))
                    }
                }
            })
            setDisabledStateNotAnimated()
        }
        sliderBrightness.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                println("seekbar progress: $progress")
                /** 1 unit of progress = 2.55 times progress. Seekbar range is 0 to 100 while brightness range is 0 to 255 */
//                val brightness: Float = normalizedBrightness(brightness = progress.toFloat(), inMin = 0f, inMax = 100f, outMin = 0.0f, outMax = 255.0f)
//                val normalizedBrightness = Math.floor(progress * (255.0 / 100.0))
                requireActivity().setScreenBrightnessTo(value = progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                println("seekbar touch started!")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                println("seekbar touch stopped!")
            }
        })
        sliderVolume.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                println("seekbar progress: $progress")
                val volumeFactor = Math.ceil(100.0 / 15.0).toInt() // Step size of volume
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress / volumeFactor, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                println("seekbar touch started!")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                println("seekbar touch stopped!")
            }
        })
        layoutWifi.root.onSafeClick {
            // https://stackoverflow.com/questions/3930990/android-how-to-enable-disable-wifi-or-internet-connection-programmatically
            wifiManager.isWifiEnabled = wifiManager.isWifiEnabled.not()
            context.showToast("Wifi status: ${wifiManager.isWifiEnabled}")
        }
        layoutNetwork.root.onSafeClick {

        }
        layoutBluetooth.root.onSafeClick {

        }
        layoutAirplaneMode.root.onSafeClick {
            if (requireContext().isWriteSettingsPermissionGranted()) {
                requireContext().setAirplaneMode(requireContext().isAirplaneModeEnabled().not())
                setCurrentAirplaneModeStatus()
            }
        }
        layoutTorch.root.onSafeClick {

        }
        layoutLocation.root.onSafeClick {

        }
        layoutCamera.root.onSafeClick {
            requestCameraPermission()
        }
        layoutBarcodeScanner.root.onSafeClick {

        }
        layoutAlarm.root.onSafeClick {

        }
        layoutTimer.root.onSafeClick {

        }
        layoutQuickNote.root.onSafeClick {

        }
        layoutRotateScreen.root.onSafeClick {

        }
        layoutSettings.root.onSafeClick {
            requireContext().openSettings()
        }
        layoutNotifications.root.onSafeClick {

        }
        layoutLockScreen.root.onSafeClick {

        }
        layoutPower.root.onSafeClick {

        }
    }

    private fun setCurrentAirplaneModeStatus() {
        binding.layoutAirplaneMode.apply {
            if (requireContext().isAirplaneModeEnabled()) {
                ivAppIcon.setImageDrawable(requireContext().drawable(R.drawable.ic_round_airplanemode_active_24))
                val colorsBackground = intArrayOf(requireContext().color(R.color.purple_50))
                val states = arrayOf(intArrayOf(android.R.attr.state_enabled))
                root.backgroundTintList = ColorStateList(states, colorsBackground)
            } else {
                ivAppIcon.setImageDrawable(requireContext().drawable(R.drawable.ic_round_airplanemode_inactive_24))
                val colorsBackground = intArrayOf(requireContext().color(R.color.black_50))
                val states = arrayOf(intArrayOf(android.R.attr.state_enabled))
                root.backgroundTintList = ColorStateList(states, colorsBackground)
            }
        }
    }

    private fun requestCameraPermission() {
        cameraPermissionResult.launch(Manifest.permission.CAMERA)
    }

    private fun setCurrentWifiStatus() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        binding.layoutWifi.apply {
            ivIcon.setImageDrawable(
                if (wifiManager.isWifiEnabled) {
                    requireContext().drawable(R.drawable.ic_round_wifi_24)
                } else {
                    requireContext().drawable(R.drawable.ic_round_wifi_off_24)
                }
            )
            tvName.text = wifiManager.connectionInfo.ssid
            tvPlaceholder.text = if (wifiManager.configuredNetworks.firstOrNull()?.status == 1) {
                "Connected"
            } else "Not Connected"
        }
    }

    @SuppressLint("NewApi")
    private fun setCurrentVolume() {
        println(
            """
            current volume: ${audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)}
            max volume 15: ${audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)}
            min volume 0: ${audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)}
        """.trimIndent()
        )
        binding.sliderVolume.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * Math.ceil(100.0 / 15.0).toInt()
    }

    private fun setCurrentScreenBrightness() {
        val currentScreenBrightness = Math.ceil(requireContext().getScreenBrightness() / 2.55)
        binding.sliderBrightness.progress = currentScreenBrightness.toInt()
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