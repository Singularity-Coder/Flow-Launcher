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
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.flowlauncher.SharedViewModel
import com.singularitycoder.flowlauncher.databinding.FragmentVoiceSearchBottomSheetBinding
import com.singularitycoder.flowlauncher.helper.isRecordAudioPermissionGranted
import com.singularitycoder.flowlauncher.helper.setTransparentBackground
import com.singularitycoder.flowlauncher.helper.showPermissionSettings
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


const val VOICE_SEARCH_QUERY = "VOICE_SEARCH_QUERY"

@AndroidEntryPoint
class VoiceSearchBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance(voiceSearchQuery: String) = VoiceSearchBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putString(VOICE_SEARCH_QUERY, voiceSearchQuery)
            }
        }
    }

    private val sharedViewModel: SharedViewModel by viewModels()

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var binding: FragmentVoiceSearchBottomSheetBinding
    private lateinit var animationDrawable: AnimationDrawable

    private var voiceSearchQuery = ""

//    private val speechToTextResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
//        result ?: return@registerForActivityResult
//        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
//        val data: Intent? = result.data
//        val text = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.trim()
//        println("speech result: $text")
//    }

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

        if (animationDrawable.isRunning) {
            stopVoiceSearch()
            if (animationDrawable.isRunning) animationDrawable.stop()
        } else {
            startVoiceSearch()
            if (animationDrawable.isRunning.not()) animationDrawable.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        voiceSearchQuery = arguments?.getString(VOICE_SEARCH_QUERY, "") ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentVoiceSearchBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        binding.initVoiceSearch()
        checkPermission()
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
        stopVoiceSearch()
    }

    private fun setupUI() {
        setTransparentBackground()
        setBottomSheetBehaviour()
        setAnimatedGradientBackground()
    }

    // https://gist.github.com/magdamiu/77389efb66ae9e693dcf1d5680fdf531
    private fun setAnimatedGradientBackground() {
        animationDrawable = binding.llRoot.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(500)
        animationDrawable.setExitFadeDuration(500)
        if (animationDrawable.isRunning.not()) animationDrawable.start()
    }

    private fun FragmentVoiceSearchBottomSheetBinding.initVoiceSearch() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                tvSpokenText.hint = ""
            }

            override fun onBeginningOfSpeech() {
                tvSpokenText.hint = "Listening..."
            }

            override fun onRmsChanged(p0: Float) = Unit
            override fun onBufferReceived(p0: ByteArray?) = Unit
            override fun onEndOfSpeech() {
                stopVoiceSearch()
                this@VoiceSearchBottomSheetFragment.dismiss()
            }
            override fun onError(p0: Int) = Unit
            override fun onResults(bundle: Bundle?) {
                val data = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                tvSpokenText.text = data?.firstOrNull()
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
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context?.packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        if (animationDrawable.isRunning.not()) animationDrawable.start()
        binding.tvSpokenText.text = "Speak now"
        speechRecognizer.startListening(speechRecognizerIntent)
    }

    private fun stopVoiceSearch() {
        if (animationDrawable.isRunning) animationDrawable.stop()
        binding.tvSpokenText.text = "Start speaking"
        speechRecognizer.stopListening()
    }

    private fun checkPermission() {
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