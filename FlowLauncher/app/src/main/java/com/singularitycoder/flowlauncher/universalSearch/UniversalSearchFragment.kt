package com.singularitycoder.flowlauncher.universalSearch

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.SharedViewModel
import com.singularitycoder.flowlauncher.addEditAppFlow.model.AppFlow
import com.singularitycoder.flowlauncher.databinding.FragmentUniversalSearchBinding
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.constants.HOME_LAYOUT_BLURRED_IMAGE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class UniversalSearchFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = UniversalSearchFragment()
    }

    private lateinit var binding: FragmentUniversalSearchBinding

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var flowList = listOf<AppFlow>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUniversalSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().setStatusBarColor(R.color.purple_500)
        binding.observeForData()
        binding.setupUI()
        binding.setupUserActionListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().setStatusBarColor(R.color.purple_500)
    }

    private fun FragmentUniversalSearchBinding.setupUI() {
        lifecycleScope.launch {
            val blurredBitmapFile = File(
                /* parent = */ requireContext().getHomeLayoutBlurredImageFileDir(),
                /* child = */ HOME_LAYOUT_BLURRED_IMAGE
            )
            if (blurredBitmapFile.exists().not()) return@launch
            val blurredBitmap = blurredBitmapFile.toBitmap() ?: return@launch
            withContext(Main) {
                ivBackground.setImageBitmap(blurredBitmap)
            }
        }

        doAfter(1.seconds()) {
            etAddItem.requestFocus()
            etAddItem.showKeyboard()
        }

        val sanskritWordsJsonString = requireContext().loadJsonStringFrom(rawResource = R.raw.sanskrit_dictionary)
        val englishWordsJsonString = requireContext().loadJsonStringFrom(rawResource = R.raw.websters_english_dictionary)
    }

    private fun FragmentUniversalSearchBinding.setupUserActionListeners() {
        etAddItem.onImeClick {
            lifecycleScope.launch {

                withContext(Main) {
                    etAddItem.requestFocus()
                    etAddItem.hideKeyboard()
                    etAddItem.clearFocus()
                }
            }
        }

        btnCancel.onSafeClick {
            requireActivity().supportFragmentManager.popBackStackImmediate()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentUniversalSearchBinding.observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = sharedViewModel.voiceToTextStateFlow) { it: String ->

        }
    }
}