package com.singularitycoder.flowlauncher.universalSearch

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.load
import coil.request.ImageRequest
import com.google.gson.Gson
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.addEditAppFlow.model.AppFlow
import com.singularitycoder.flowlauncher.addEditAppFlow.viewModel.AppFlowViewModel
import com.singularitycoder.flowlauncher.databinding.FragmentUniversalSearchBinding
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.constants.HOME_LAYOUT_BLURRED_IMAGE
import com.singularitycoder.flowlauncher.home.model.App
import com.singularitycoder.flowlauncher.home.model.Contact
import com.singularitycoder.flowlauncher.home.model.Sms
import com.singularitycoder.flowlauncher.toBitmapOf
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

// Why sanskrit words? https://manojkumargarg.wordpress.com/sanskrit/
// I think the language has some deep wisdom hidden in it. I also like the idea of using it in computers given the algorithmic nature of the language.

@AndroidEntryPoint
class UniversalSearchFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = UniversalSearchFragment()
    }

    @Inject
    lateinit var gson: Gson

    private var sanskritVocabMap: Map<String, String> = HashMap()
    private var englishVocabMap: Map<String, String> = HashMap()
    private var androidSettingsMap: Map<String, String> = HashMap()
    private var contactsList = emptyList<Contact>()
    private var smsList = emptyList<Sms>()
    private var appList = emptyList<App>()

    private lateinit var binding: FragmentUniversalSearchBinding

    private val universalSearchViewModel: UniversalSearchViewModel by viewModels()
    private val appFlowViewModel: AppFlowViewModel by viewModels()

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

        // TODO ideally these should be stored in DB in work manager and fetched from DB. Work will start on home screen. Triggered again to referesh data when opening this screen. Absolutely ZERO patience to handle this now.
        lifecycleScope.launch {
            val sanskritWordsJsonString = requireContext().loadJsonStringFrom(rawResource = R.raw.sanskrit_dictionary)
            sanskritVocabMap = (JSONObject(sanskritWordsJsonString ?: "").toMap() as? Map<String, String>) ?: emptyMap()
        }

        lifecycleScope.launch {
            val englishWordsJsonString = requireContext().loadJsonStringFrom(rawResource = R.raw.websters_english_dictionary)
            englishVocabMap = (JSONObject(englishWordsJsonString ?: "").toMap() as? Map<String, String>) ?: emptyMap()
        }

        lifecycleScope.launch {
            val androidSettingsJsonString = requireContext().loadJsonStringFrom(rawResource = R.raw.android_settings)
            androidSettingsMap = (JSONObject(androidSettingsJsonString ?: "").toMap() as? Map<String, String>) ?: emptyMap()
        }

        lifecycleScope.launch {
            contactsList = requireActivity().getContactsList()
        }

        lifecycleScope.launch {
            smsList = requireContext().getSmsList()
        }

        doAfter(1.seconds()) {
            etSearch.requestFocus()
            etSearch.showKeyboard()
        }

//        sanskritVocabMap = gson.fromJson(sanskritWordsJsonString, Vocabulary::class.java).wordMap
//        englishVocabMap = gson.fromJson(englishWordsJsonString, Vocabulary::class.java).wordMap
    }

    private fun FragmentUniversalSearchBinding.setupUserActionListeners() {
        etSearch.onImeClick {
            etSearch.requestFocus()
            etSearch.hideKeyboard()
            etSearch.clearFocus()
        }

        etSearch.doAfterTextChanged { it: Editable? ->
            val query = it.toString().toLowCase().trim()

            cardContacts.isVisible = contactsList.any { (it.name.contains(query, true) || it.mobileNumber.contains(query, true)) && query.isNotBlank() }
            cardMessages.isVisible = smsList.any { (it.body?.contains(query, true) == true) && query.isNotBlank() }
            cardAndroidSettings.isVisible = androidSettingsMap.keys.any { it.contains(query, true) && query.isNotBlank() }
            cardSanskritWords.isVisible = sanskritVocabMap.keys.any { it.contains(query, true) && query.isNotBlank() }
            cardEnglishWords.isVisible = englishVocabMap.keys.any { it.contains(query, true) && query.isNotBlank() }

            universalSearchViewModel.setQueryValue(query)
        }

        btnCancel.onSafeClick {
            requireActivity().supportFragmentManager.popBackStackImmediate()
        }

        nestedScrollview.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            println("scrollY: $scrollY oldScrollY: $oldScrollY".trimIndent())
            if (scrollY - oldScrollY > 20) {
                etSearch.requestFocus()
                etSearch.hideKeyboard()
            }

            if (scrollY == 0) {
                etSearch.requestFocus()
                etSearch.showKeyboard()
            }
        })
    }

    private fun FragmentUniversalSearchBinding.observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = appFlowViewModel.appFlowListStateFlow) { it: List<AppFlow> ->
            val position = Preferences.read(requireContext()).getInt(Preferences.KEY_SELECTED_FLOW_POSITION, 0)
            val selectedFlow = it.getOrNull(position)
            appList = selectedFlow?.appList ?: emptyList()
        }

        // TODO fix this
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = universalSearchViewModel.appsStateFlow) { query: String ->
            if (appList.any { it.title.contains(query, true) && query.isNotBlank() }.not()) return@collectLatestLifecycleFlow
            val allAppsList = appList.filter { it: App -> it.title.contains(query, true) }
            val tvAppsLayoutList = listOf(layoutApp1, layoutApp2, layoutApp3, layoutApp4)
            var count = 0
            for (app: App in allAppsList) {
                if (count >= 4) break
                tvAppsLayoutList[count].apply {
                    root.isVisible = true
                    ivAppIcon.load(app.iconPath)
                    tvAppName.text = app.title
                    root.onSafeClick {
                        requireActivity().launchApp(app.packageName)
                    }
                }
                count++
            }
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = universalSearchViewModel.contactsStateFlow) { query: String ->
            if (contactsList.any { it.name.contains(query, true) && query.isNotBlank() }.not()) return@collectLatestLifecycleFlow
            val allContactsList = contactsList.filter { it: Contact -> it.name.contains(query, true) || it.mobileNumber.contains(query, true) }.distinctBy { it.mobileNumber }
            val tvContactsLayoutList = listOf(layoutContact1, layoutContact2, layoutContact3)
            var count = 0
            for (contact: Contact in allContactsList) {
                if (count >= 3) break
                tvContactsLayoutList[count].apply {
                    root.isVisible = true
                    tvInitials.text = if (contact.name.trim().contains(" ")) {
                        contact.name.firstOrNull().toString().toUpCase() + contact.name.substringAfter(" ").firstOrNull().toString().toUpCase()
                    } else {
                        contact.name.firstOrNull().toString().toUpCase()
                    }
                    val bitmapDrawableOfLayout = tvInitials.toBitmapOf(width = tvInitials.width, height = tvInitials.height)?.toDrawable(requireContext().resources)
                    ivPicture.load(contact.photoURI) {
                        placeholder(bitmapDrawableOfLayout)
                        error(bitmapDrawableOfLayout)
                    }
                    tvTitle.text = contact.name
                    tvSubtitle.text = contact.mobileNumber
                    root.onSafeClick {
                        requireContext().openDialer(contact.mobileNumber)
                    }
                }
                count++
            }
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = universalSearchViewModel.contactsStateFlow) { query: String ->
            if (smsList.any { it.body?.contains(query, true) == true && query.isNotBlank() }.not()) return@collectLatestLifecycleFlow
            val allSmsList = smsList.filter { it: Sms -> it.body?.contains(query, true) == true || it.number?.contains(query, true) == true }.distinctBy { it.body }
            val tvContactsLayoutList = listOf(layoutMessage1, layoutMessage2, layoutMessage3)
            var count = 0
            for (sms: Sms in allSmsList) {
                if (count >= 3) break
                tvContactsLayoutList[count].apply {
                    root.isVisible = true
                    cardPicture.isVisible = false
                    tvTitle.text = sms.number
                    tvSubtitle.text = sms.body
                    ivCall.setImageDrawable(requireContext().drawable(R.drawable.outline_sms_24))
                    root.onSafeClick {
                        requireContext().sendSms(phoneNumber = sms.number ?: "", body = "")
                    }
                }
                count++
            }
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = universalSearchViewModel.androidSettingsStateFlow) { query: String ->
            if (androidSettingsMap.values.any { it.contains(query, true) && query.isNotBlank() }.not()) return@collectLatestLifecycleFlow
            val mapList = androidSettingsMap.filter { it: Map.Entry<String, String> -> it.value.contains(query, true) }
            val tvAndroidSettingsLayoutList = listOf(layoutSetting1, layoutSetting2, layoutSetting3)
            var count = 0
            for (item: Map.Entry<String, String> in mapList) {
                if (count >= 3) break
                tvAndroidSettingsLayoutList[count].apply {
                    root.isVisible = true
                    ivOpenOutward.isVisible = true
                    tvWord.text = androidSettingsMap[item.key]
                    root.onSafeClick {
                        // TODO find another way
                        requireContext().openSettings(screen = "android.settings.${item.key.replace(oldValue = "ACTION_", newValue = "")}")
                    }
                }
                count++
            }
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = universalSearchViewModel.sanskritWordsStateFlow) { query: String ->
            if (sanskritVocabMap.keys.any { it.contains(query, true) && query.isNotBlank() }.not()) return@collectLatestLifecycleFlow
            val keysList = sanskritVocabMap.keys.filter { key: String -> key.contains(query, true) }
            val tvSanskritWordLayoutList = listOf(layoutWord1, layoutWord2, layoutWord3)
            var count = 0
            for (key: String in keysList) {
                if (count >= 3) break
                tvSanskritWordLayoutList[count].apply {
                    root.isVisible = true
                    ivOpenOutward.isVisible = true
                    ivOpenOutward.setImageDrawable(requireContext().drawable(R.drawable.sharp_content_copy_24))
                    tvWord.text = "$key: ${sanskritVocabMap[key]}"
                    root.onSafeClick {
                        requireContext().clipboard()?.text = tvWord.text
                        binding.root.showSnackBar("Copied!")
                    }
                }
                count++
            }
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = universalSearchViewModel.englishWordsStateFlow) { query: String ->
            if (englishVocabMap.keys.any { it.contains(query, true) && query.isNotBlank() }.not()) return@collectLatestLifecycleFlow
            val keysList = englishVocabMap.keys.filter { key: String -> key.contains(query, true) }
            val tvEnglishWordLayoutList = listOf(layoutWord4, layoutWord5, layoutWord6)
            var count = 0
            for (key: String in keysList) {
                if (count >= 3) break
                tvEnglishWordLayoutList[count].apply {
                    root.isVisible = true
                    ivOpenOutward.isVisible = true
                    ivOpenOutward.setImageDrawable(requireContext().drawable(R.drawable.sharp_content_copy_24))
                    tvWord.text = "$key: ${englishVocabMap[key]}"
                    root.onSafeClick {
                        requireContext().clipboard()?.text = tvWord.text
                        binding.root.showSnackBar("Copied!")
                    }
                }
                count++
            }
        }
    }

    private data class Vocabulary(val wordMap: HashMap<String, String>)

    private data class Word(
        val word: String? = "",
        val meaning: String? = ""
    )
}