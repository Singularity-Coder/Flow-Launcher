package com.singularitycoder.flowlauncher.home.view

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.SharedViewModel
import com.singularitycoder.flowlauncher.databinding.FragmentVoiceSearchBottomSheetBinding
import com.singularitycoder.flowlauncher.helper.*
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

// Primary source: https://medium.com/voice-tech-podcast/android-speech-to-text-tutorial-8f6fa71606ac

// https://developer.android.com/guide/app-actions/action-schema
// https://developers.google.com/voice-actions/system
// https://developer.android.com/guide/app-actions/get-started
// https://developer.android.com/reference/android/speech/SpeechRecognizer
// https://cloud.google.com/speech-to-text

// SpeechRecognizer class - gives access to speech recogniser service

@AndroidEntryPoint
class VoiceSearchBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = VoiceSearchBottomSheetFragment()
    }

    //    @Inject
    private val networkStatus: NetworkStatus by lazy { NetworkStatus(requireContext()) }

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var binding: FragmentVoiceSearchBottomSheetBinding
    private lateinit var animationDrawable: AnimationDrawable

    private val recordAudioPermissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isPermissionGranted: Boolean? ->
        isPermissionGranted ?: return@registerForActivityResult

        // This can be called twice. A user can deny the permission twice and then is directed to rationale
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.RECORD_AUDIO)) {
            return@registerForActivityResult
        }

        // Permission permanently denied
        if (isPermissionGranted.not()) {
            requireContext().showPermissionSettings()
            return@registerForActivityResult
        }

        if (requireContext().isRecordAudioPermissionGranted().not()) {
            return@registerForActivityResult
        }

        startVoiceSearch()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycle.addObserver(networkStatus)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentVoiceSearchBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        binding.initVoiceSearch()
        observeForData()
        checkPermissionAndStartSpeechToText()
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

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().setNavigationBarColor(R.color.white)
    }

    // https://stackoverflow.com/questions/40616833/bottomsheetdialogfragment-listen-to-dismissed-by-user-event
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        stopVoiceSearch()
    }

    private fun observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = networkStatus.networkState) { it: NetworkState ->
            when (it) {
                NetworkState.AVAILABLE -> {
                    binding.tvNetworkState.apply {
                        isVisible = true
                        text = "Back Online"
                    }
                }
                NetworkState.UNAVAILABLE, NetworkState.LOST -> {
                    stop()
                    binding.tvNetworkState.apply {
                        isVisible = true
                        text = "No Internet"
                    }
                }
                else -> Unit
            }
        }
    }

    private fun setupUI() {
        requireActivity().setNavigationBarColor(R.color.black)
//        binding.llRoot.background = requireContext().drawable(animatedGradientList[Random().nextInt(9)])
        binding.llRoot.background = requireContext().drawable(R.drawable.animated_gradient_1)
        setTransparentBackground()
        setBottomSheetBehaviour()
        setAnimatedGradientBackground()
        setupUserActionListeners()
    }

    private fun setupUserActionListeners() {
        binding.btnRetry.setOnClickListener {
            checkPermissionAndStartSpeechToText()
        }
    }

    // https://gist.github.com/magdamiu/77389efb66ae9e693dcf1d5680fdf531
    private fun setAnimatedGradientBackground() {
        animationDrawable = binding.llRoot.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(500)
        animationDrawable.setExitFadeDuration(500)
    }

    private fun FragmentVoiceSearchBottomSheetBinding.initVoiceSearch() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                tvSpokenText.hint = ""
            }

            override fun onBeginningOfSpeech() {
                tvSpokenText.hint = "Listening..."
            }

            override fun onRmsChanged(p0: Float) = Unit
            override fun onBufferReceived(p0: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit

            override fun onError(p0: Int) {
                stop()
                btnRetry.isVisible = true
            }

            override fun onResults(bundle: Bundle?) {
                val data = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                tvSpokenText.text = data?.firstOrNull()
                sharedViewModel.setVoiceToTextValue(text = data?.firstOrNull() ?: "")
                stopVoiceSearch()
                this@VoiceSearchBottomSheetFragment.dismiss()
            }

            override fun onPartialResults(bundle: Bundle?) {
                val data = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                tvSpokenText.text = data?.firstOrNull()
            }

            override fun onEvent(p0: Int, p1: Bundle?) = Unit
        })
    }

    private fun startVoiceSearch() {
        if (networkStatus.isOnline().not()) {
            stop()
            requireContext().showToast("No Internet")
            return
        }
        if (animationDrawable.isRunning.not()) animationDrawable.start()
        binding.tvNetworkState.isVisible = false
        binding.tvSpokenText.text = "Speak now"
        binding.llVoiceToTextState.isVisible = false
        binding.tvSpokenText.isVisible = true
        binding.ivVoiceSearch.isVisible = true
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context?.packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(speechRecognizerIntent)
    }

    private fun stopVoiceSearch() {
        stop()
        speechRecognizer?.stopListening()
    }

    private fun stop() {
        if (animationDrawable.isRunning) animationDrawable.stop()
        binding.llVoiceToTextState.isVisible = true
        binding.tvSpokenText.isVisible = false
        binding.ivVoiceSearch.isVisible = false
        binding.btnRetry.isVisible = true
    }

    private fun checkPermissionAndStartSpeechToText() {
        recordAudioPermissionResult.launch(Manifest.permission.RECORD_AUDIO)
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
}