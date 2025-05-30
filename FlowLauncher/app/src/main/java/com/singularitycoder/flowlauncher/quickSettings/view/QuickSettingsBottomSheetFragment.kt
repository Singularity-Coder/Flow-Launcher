package com.singularitycoder.flowlauncher.quickSettings.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.*
import android.content.res.ColorStateList
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraManager.TorchCallback
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.databinding.FragmentQuickSettingsBottomSheetBinding
import com.singularitycoder.flowlauncher.databinding.ItemQuickSettingBinding
import com.singularitycoder.flowlauncher.databinding.LongItemQuickSettingBinding
import com.singularitycoder.flowlauncher.deviceActivity.view.DeviceActivityBottomSheetFragment
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.constants.*
import com.singularitycoder.flowlauncher.helper.swipebutton.OnStateChangeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject


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

    @Inject
    lateinit var bluetoothManager: BluetoothManager

    @Inject
    lateinit var notificationUtils: NotificationUtils

    private lateinit var binding: FragmentQuickSettingsBottomSheetBinding

    private val cameraManager: CameraManager by lazy {
        context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private val torchCallback: TorchCallback = object : TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            super.onTorchModeChanged(cameraId, enabled)
            // https://stackoverflow.com/questions/51131361/how-to-detect-whether-the-flashlight-is-on-or-off
            setFlashLightStatus(enabled)
        }
    }

    private val quickSettingsPermissionsResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions: Map<String, @JvmSuppressWildcards Boolean>? ->
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
                    // permission permanently denied. Show settings dialog enable blocking layout and show popup to go to settings
                    requireContext().showPermissionSettings()
                }
                else -> {
                    // Permission denied but not permanently, tell user why you need it. Ideally provide a button to request it again and another to dismiss
                    // enable blocking layout
                }
            }
        }
    }

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

        if (requireContext().hasPermission(Manifest.permission.CAMERA)) {
            requireActivity().launchApp("com.android.camera2")
        }
    }

    private val phoneStatePermissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean? ->
        isGranted ?: return@registerForActivityResult

        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
            requireContext().showPermissionSettings()
            return@registerForActivityResult
        }

        if (isGranted.not()) {
            requestPhoneStatePermission()
            return@registerForActivityResult
        }

        if (requireContext().hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            requireContext().setMobileDataStateTo(requireContext().getMobileDataState().not())
            setNetworkStatus()
        }
    }

    private val barcodeScanLauncherResult = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents.isNullOrBlankOrNaOrNullString()) {
            requireContext().showToast(message = getString(R.string.something_is_wrong))
            return@registerForActivityResult
        }

        val scannedResult = result.contents
        println("Scanned result : ${result.contents}")
        (requireActivity() as? MainActivity)?.showOnBarcodeScanComplete(scannedResult)
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
        requireActivity().setNavigationBarColor(R.color.black)
        quickSettingsPermissionsResult.launch(quickSettingsPermissions)
        binding.setupUserActionListeners()
    }

    override fun onResume() {
        super.onResume()
        binding.setupUI()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.registerReceiver(quickSettingsBroadcastReceiver, IntentFilter(Broadcast.VOLUME_RAISED), Context.RECEIVER_EXPORTED)
            activity?.registerReceiver(quickSettingsBroadcastReceiver, IntentFilter(Broadcast.VOLUME_LOWERED), Context.RECEIVER_EXPORTED)
            activity?.registerReceiver(quickSettingsBroadcastReceiver, IntentFilter("android.intent.action.SERVICE_STATE"), Context.RECEIVER_EXPORTED) // For airplane mode
        } else {
            activity?.registerReceiver(quickSettingsBroadcastReceiver, IntentFilter(Broadcast.VOLUME_RAISED))
            activity?.registerReceiver(quickSettingsBroadcastReceiver, IntentFilter(Broadcast.VOLUME_LOWERED))
            activity?.registerReceiver(quickSettingsBroadcastReceiver, IntentFilter("android.intent.action.SERVICE_STATE")) // For airplane mode
        }
        registerTorchState()
    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(quickSettingsBroadcastReceiver)
        unregisterTorchState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().setNavigationBarColor(R.color.white)
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
        setNetworkStatus()
        setWifiHotspotStatus()
        setBluetoothStatus()
        setLocationStatus()
        layoutDeviceActivity.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.round_perm_device_information_24))
        }
        layoutBarcodeScanner.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.ic_round_qr_code_scanner_24))
        }
        layoutVolume.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.ic_round_volume_up_24))
        }
        layoutNfc.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.ic_round_nfc_24))
        }
        layoutScreenShot.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.baseline_screenshot_24))
        }
        layoutSettings.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.ic_round_settings_24))
        }
        layoutFileManager.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.folder_black_24dp))
        }
        layoutLockScreen.ivAppIcon.apply {
            setImageDrawable(requireContext().drawable(R.drawable.ic_round_lock_24))
        }
        setPowerButton()
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
                override fun onStateChange(isActive: Boolean) {
                    Toast.makeText(requireContext(), "State: " + isActive, Toast.LENGTH_SHORT).show()
                    if (isActive) {
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
                ivBrightness.setSliderIconColor(progress)
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
                ivVolume.setSliderIconColor(progress)
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
//            wifiManager.isWifiEnabled = wifiManager.isWifiEnabled.not()
//            context.showToast("Wifi status: ${wifiManager.isWifiEnabled}")
            requireContext().openSettings(screen = SettingsScreen.QUICK_NETWORK_TOGGLE_POPUP)
            dismiss()
        }
        layoutNetwork.root.onSafeClick {
//            phoneStatePermissionResult.launch(Manifest.permission.READ_PHONE_STATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requireContext().openSettings(screen = Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            } else {
                requireContext().openSettings(screen = SettingsScreen.QUICK_NETWORK_TOGGLE_POPUP)
            }
            dismiss()
        }
        layoutBluetooth.root.onSafeClick {
            requireContext().openSettings(screen = SettingsScreen.BLUETOOTH)
            dismiss()
        }
        layoutAirplaneMode.root.onSafeClick {
            requireContext().openSettings(screen = SettingsScreen.AIRPLANE_MODE)
            dismiss()
        }
        layoutTorch.root.onSafeClick {
            if (requireContext().isFlashAvailable().not()) {
                root.showSnackBar("Flash light is not supported on this device!")
            } else {
                flashLight(isOn = it.second, cameraManager = cameraManager)
            }
        }
        layoutLocation.root.onSafeClick {
            requireContext().openSettings(screen = SettingsScreen.LOCATION)
            dismiss()
        }
        layoutDeviceActivity.root.onSafeClick {
            DeviceActivityBottomSheetFragment.newInstance().show(
                /* manager = */ requireActivity().supportFragmentManager,
                /* tag = */ BottomSheetTag.DEVICE_ACTIVITY
            )
            dismiss()
        }
        layoutBarcodeScanner.root.onSafeClick {
            val scanOptions = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Scan QR code")
                captureActivity = BarcodeScanActivity::class.java
                setTorchEnabled(true)
                setOrientationLocked(true)
                setBeepEnabled(true)
                setDesiredBarcodeFormats(
                    listOf(
                        ScanOptions.PRODUCT_CODE_TYPES.toList(),
                        ScanOptions.ONE_D_CODE_TYPES.toList(),
                        listOf(ScanOptions.QR_CODE, ScanOptions.DATA_MATRIX, ScanOptions.PDF_417),
                    ).flatten()
                )
            }
            barcodeScanLauncherResult.launch(scanOptions)
        }
        layoutVolume.root.onSafeClick {
            requireContext().showVolumeQuickSettings()
        }
        layoutWifiHotspot.root.onSafeClick {
            requireContext().openSettings(screen = SettingsScreen.WIFI_HOTSPOT)
            dismiss()
        }
        layoutNfc.root.onSafeClick {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requireContext().openSettings(screen = Settings.Panel.ACTION_NFC)
            } else {
                requireContext().openSettings(screen = SettingsScreen.NFC_POPUP)
            }
            dismiss()
        }
        layoutScreenShot.root.onSafeClick {
            notificationUtils.showNotification(
                data = FlowNotification(title = "Quick Actions"),
                mainActivity = MainActivity::class.java,
                isFloatBubble = true
            )
//            lifecycleScope.launch {
//                (10 downTo 0).forEach {
//                    delay(1.seconds())
//                    withContext(Main) {
//                        notificationUtils.showNotification(
//                            data = FlowNotification(
//                                title = "Screenshot will start in $it sec.",
//                                intentKey = IntentKey.NOTIF_SCREENSHOT_COUNTDOWN
//                            ),
//                            mainActivity = MainActivity::class.java
//                        )
//                        if (it == 0) {
//                            doAfter(3.seconds()) {
//                                requireContext().takeScreenshot()
//                            }
//                            requireContext().clearNotification(Notif.SCREENSHOT_COUNTDOWN.ordinal)
//                        }
//                    }
//                }
//            }
        }
        layoutSettings.root.onSafeClick {
            requireContext().openSettings(screen = SettingsScreen.HOME)
        }
        layoutFileManager.root.onSafeClick {
            requireContext().showFileManager()
        }
        layoutLockScreen.root.onSafeClick {
            requireContext().lockDevice()
        }
        layoutPower.root.onSafeClick {
            requireContext().showPowerButtonActions()
        }
    }

    private fun setNetworkStatus() {
        binding.layoutNetwork.enableIcon(
            isEnabled = requireContext().getMobileDataState(),
            enabledIcon = R.drawable.cell_tower_black_24dp,
            disabledIcon = R.drawable.cell_tower_disabled_black_24dp
        )
    }

    private fun setCurrentAirplaneModeStatus() {
        binding.layoutAirplaneMode.enableIcon(
            isEnabled = requireContext().isAirplaneModeEnabled(),
            enabledIcon = R.drawable.ic_round_airplanemode_active_24,
            disabledIcon = R.drawable.ic_round_airplanemode_inactive_24
        )
    }

    private fun requestCameraPermission() {
        cameraPermissionResult.launch(Manifest.permission.CAMERA)
    }

    private fun setPowerButton() {
        binding.layoutPower.apply {
            ivAppIcon.setImageDrawable(requireContext().drawable(R.drawable.ic_round_power_settings_new_24))
            val states = arrayOf(intArrayOf(android.R.attr.state_enabled))
            val colors500 = intArrayOf(requireContext().color(R.color.md_red_500))
            val colors200 = intArrayOf(requireContext().color(R.color.md_red_200))
            val colorsBackground = intArrayOf(requireContext().color(R.color.md_red_50))
            root.backgroundTintList = ColorStateList(states, colorsBackground)
            ivAppIcon.imageTintList = ColorStateList(states, colors500)
            root.rippleColor = ColorStateList(states, colors200)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setCurrentWifiStatus() {
        binding.layoutWifi.apply {
            enableIcon(
                isEnabled = wifiManager.isWifiEnabled,
                enabledIcon = R.drawable.ic_round_wifi_24,
                disabledIcon = R.drawable.ic_round_wifi_off_24
            )
            if (requireContext().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION).not()) {
                tvPlaceholder.text = "Wifi"
                tvName.text = "Not Connected"
                return
            }
            tvPlaceholder.text = "Wifi"
            val availableWifiNetworks = wifiManager.scanResults // Get results for newer APIs
            val isConnectedToWifi = wifiManager.configuredNetworks.firstOrNull {
                it.SSID == wifiManager.connectionInfo.ssid.replace("\"", "")
            }?.status == 1
            tvName.text = if (wifiManager.isWifiEnabled) {
                "Connected to ${wifiManager.connectionInfo.ssid.replace("\"", "")}"
            } else "Not Connected"
        }
    }

    @SuppressLint("MissingPermission")
    private fun setBluetoothStatus() {
        binding.layoutBluetooth.apply {
            enableIcon(
                isBluetoothEnabled(),
                R.drawable.ic_round_bluetooth_24,
                R.drawable.ic_round_bluetooth_disabled_24
            )
            if (requireContext().isBluetoothPermissionGranted().not()) {
                tvPlaceholder.text = "Bluetooth"
                tvName.text = "Not Connected"
                return
            }
            val connectedDevices = try {
                // STATE_CONNECTED is not allowed. So add all other profiles to a list
                bluetoothManager.getConnectedDevices(BluetoothProfile.STATE_CONNECTED)
            } catch (e: Exception) {
                emptyList()
            }
            if (isBluetoothEnabled() && connectedDevices.isNotEmpty()) {
                tvPlaceholder.text = "Bluetooth"
                tvName.text = "Connected to ${connectedDevices.firstOrNull()?.name}"
            } else {
                tvPlaceholder.text = "Bluetooth"
                tvName.text = "Not Connected"
            }
        }
    }

    private fun setWifiHotspotStatus() {
        binding.layoutWifiHotspot.enableIcon(
            isEnabled = requireContext().isWifiHotspotEnabled(),
            enabledIcon = R.drawable.ic_round_wifi_tethering_24,
            disabledIcon = R.drawable.ic_round_wifi_tethering_off_24
        )
    }

    private fun setFlashLightStatus(isEnabled: Boolean) {
        // https://stackoverflow.com/questions/6068803/how-to-turn-on-front-flash-light-programmatically-in-android#:~:text=For%20turning%20on%2Foff%20flashlight%3A&text=The%20main%20parameter%20used%20here,to%20turn%20on%20camera%20flashlight.
        binding.layoutTorch.enableIcon(
            isEnabled = isEnabled,
            enabledIcon = R.drawable.ic_round_flashlight_on_24,
            disabledIcon = R.drawable.round_flashlight_off_24
        )
    }

    private fun setLocationStatus() {
        val isFineLocationPermissionGranted = requireContext().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val isLocationToggleEnabled = requireContext().isLocationToggleEnabled()
        binding.layoutLocation.enableIcon(
            isEnabled = isFineLocationPermissionGranted && isLocationToggleEnabled,
            enabledIcon = R.drawable.location_on_black_24dp,
            disabledIcon = R.drawable.round_location_off_24
        )
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
                    BottomSheetBehavior.STATE_HIDDEN -> dismiss()
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

    private fun ItemQuickSettingBinding.enableIcon(
        isEnabled: Boolean,
        @DrawableRes enabledIcon: Int,
        @DrawableRes disabledIcon: Int,
    ) {
        if (isEnabled) {
            val iconColor = intArrayOf(requireContext().color(R.color.purple_500))
            val iconStates = arrayOf(intArrayOf(android.R.attr.state_enabled))
            ivAppIcon.setImageDrawable(requireContext().drawable(enabledIcon))
            ivAppIcon.imageTintList = ColorStateList(iconStates, iconColor)
            val backgroundColor = intArrayOf(requireContext().color(R.color.purple_50))
            val backgroundStates = arrayOf(intArrayOf(android.R.attr.state_enabled))
            root.backgroundTintList = ColorStateList(backgroundStates, backgroundColor)
        } else {
            val iconColor = intArrayOf(requireContext().color(R.color.subtitle_color))
            val iconStates = arrayOf(intArrayOf(android.R.attr.state_enabled))
            ivAppIcon.setImageDrawable(requireContext().drawable(disabledIcon))
            ivAppIcon.imageTintList = ColorStateList(iconStates, iconColor)
            val backgroundColor = intArrayOf(requireContext().color(R.color.black_50))
            val backgroundStates = arrayOf(intArrayOf(android.R.attr.state_enabled))
            root.backgroundTintList = ColorStateList(backgroundStates, backgroundColor)
        }
    }

    private fun LongItemQuickSettingBinding.enableIcon(
        isEnabled: Boolean,
        @DrawableRes enabledIcon: Int,
        @DrawableRes disabledIcon: Int,
    ) {
        if (isEnabled) {
            val iconColor = intArrayOf(requireContext().color(R.color.purple_500))
            val iconStates = arrayOf(intArrayOf(android.R.attr.state_enabled))
            ivIcon.setImageDrawable(requireContext().drawable(enabledIcon))
            ivIcon.imageTintList = ColorStateList(iconStates, iconColor)
            tvPlaceholder.setTextColor(requireContext().color(R.color.purple_500))
            tvName.setTextColor(requireContext().color(R.color.purple_200))
            val backgroundColor = intArrayOf(requireContext().color(R.color.purple_50))
            val backgroundStates = arrayOf(intArrayOf(android.R.attr.state_enabled))
            root.backgroundTintList = ColorStateList(backgroundStates, backgroundColor)
        } else {
            val iconColor = intArrayOf(requireContext().color(R.color.subtitle_color))
            val iconStates = arrayOf(intArrayOf(android.R.attr.state_enabled))
            ivIcon.setImageDrawable(requireContext().drawable(disabledIcon))
            ivIcon.imageTintList = ColorStateList(iconStates, iconColor)
            tvPlaceholder.setTextColor(requireContext().color(R.color.subtitle_color))
            tvName.setTextColor(requireContext().color(R.color.subtitle_color))
            val backgroundColor = intArrayOf(requireContext().color(R.color.black_50))
            val backgroundStates = arrayOf(intArrayOf(android.R.attr.state_enabled))
            root.backgroundTintList = ColorStateList(backgroundStates, backgroundColor)
        }
    }

    private fun ImageView.setSliderIconColor(progress: Int) {
        if (progress < 10) {
            val purple500Drawable = drawable.changeColor(context = requireContext(), color = R.color.purple_500)
            setImageDrawable(purple500Drawable)
        } else {
            val purple50Drawable = drawable.changeColor(context = requireContext(), color = R.color.purple_50)
            setImageDrawable(purple50Drawable)
        }
    }

    private fun requestPhoneStatePermission() {
        cameraPermissionResult.launch(Manifest.permission.READ_PHONE_STATE)
    }

    private fun registerTorchState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager.registerTorchCallback(torchCallback, null)
        }
    }

    private fun unregisterTorchState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager.unregisterTorchCallback(torchCallback)
        }
    }
}