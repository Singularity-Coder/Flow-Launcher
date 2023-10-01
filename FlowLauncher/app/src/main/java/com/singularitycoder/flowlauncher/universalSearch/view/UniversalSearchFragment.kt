package com.singularitycoder.flowlauncher.universalSearch.view

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import coil.load
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.databinding.FragmentUniversalSearchBinding
import com.singularitycoder.flowlauncher.databinding.ListItemAppBinding
import com.singularitycoder.flowlauncher.databinding.ListItemContactBinding
import com.singularitycoder.flowlauncher.databinding.ListItemSanskritWordBinding
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.constants.HOME_LAYOUT_BLURRED_IMAGE
import com.singularitycoder.flowlauncher.helper.constants.WorkerData
import com.singularitycoder.flowlauncher.helper.constants.WorkerTag
import com.singularitycoder.flowlauncher.home.model.App
import com.singularitycoder.flowlauncher.home.model.Contact
import com.singularitycoder.flowlauncher.home.model.Sms
import com.singularitycoder.flowlauncher.toBitmapOf
import com.singularitycoder.flowlauncher.universalSearch.viewmodel.UniversalSearchViewModel
import com.singularitycoder.flowlauncher.universalSearch.worker.UniversalSearchWorker
import com.singularitycoder.flowlauncher.universalSearch.worker.WebLinkFetchWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

// Why sanskrit words? https://manojkumargarg.wordpress.com/sanskrit/
// I think the language has some deep wisdom hidden in it. I also like the idea of using it in computers given the algorithmic nature of the language.

@AndroidEntryPoint
class UniversalSearchFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = UniversalSearchFragment()
    }

    private lateinit var binding: FragmentUniversalSearchBinding
    private var searchQuery: String = ""
    private val universalSearchViewModel: UniversalSearchViewModel by viewModels()

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
                    requireContext().showPermissionSettings()
                }
                else -> {
                    // Permission denied but not permanently, tell user why you need it. Ideally provide a button to request it again and another to dismiss
                    // enable blocking layout
                }
            }
        }
        doAfter(1.seconds()) {
            binding.etSearch.showKeyboard()
        }
        if (permissions.values.all { it }) {
            if (requireContext().isUsageAccessPermissionGranted2().not()) {
                try {
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                } catch (_: ActivityNotFoundException) {
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUniversalSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().setStatusBarColor(R.color.purple_700)
        requireActivity().setNavigationBarColor(R.color.black)
        binding.observeForData()
        binding.setupUI()
        binding.setupUserActionListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().setNavigationBarColor(R.color.white)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().setStatusBarColor(R.color.purple_700)
    }

    private fun FragmentUniversalSearchBinding.observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = universalSearchViewModel.textChangeFlow) { query: String ->
            if (FlowUtils.recentAppList.isEmpty()) {
                cardRecentApps.isVisible = false
                return@collectLatestLifecycleFlow
            }
            val allRecentAppsList = FlowUtils.recentAppList/*.distinctBy { it.packageName }*/
            val tvAppsLayoutList = listOf(layoutRecentApp1, layoutRecentApp2, layoutRecentApp3, layoutRecentApp4)
            tvAppsLayoutList.forEach { it: ListItemAppBinding ->
                it.root.isInvisible = true
            }
            var count = 0
            for (app: App in allRecentAppsList) {
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

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = universalSearchViewModel.textChangeFlow) { query: String ->
            if (FlowUtils.appList.any { it.title.contains(query, true) && query.isNotBlank() }.not()) return@collectLatestLifecycleFlow
            val allAppsList = FlowUtils.appList.filter { it: App -> it.title.contains(query, true) }.distinctBy { it.packageName }
            val tvAppsLayoutList = listOf(layoutApp1, layoutApp2, layoutApp3, layoutApp4)
            tvAppsLayoutList.forEach { it: ListItemAppBinding ->
                it.root.isInvisible = true
            }
            var count = 0
            for (app: App in allAppsList) {
                if (count >= 4) break
                tvAppsLayoutList[count].apply {
                    root.isVisible = true
                    ivAppIcon.load(app.iconPath)
                    tvAppName.text = app.title
                    tvAppName.highlightText(query = query, result = tvAppName.text.toString())
                    root.onSafeClick {
                        requireActivity().launchApp(app.packageName)
                    }
                }
                count++
            }
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = universalSearchViewModel.textChangeFlow) { query: String ->
            if (FlowUtils.contactsList.any { it.name.contains(query, true) && query.isNotBlank() }.not()) return@collectLatestLifecycleFlow
            val allContactsList = FlowUtils.contactsList.filter { it: Contact -> it.name.contains(query, true) || it.mobileNumber.contains(query, true) }.distinctBy { it.mobileNumber }
            val tvContactsLayoutList = listOf(layoutContact1, layoutContact2, layoutContact3)
            tvContactsLayoutList.forEach { it: ListItemContactBinding ->
                it.root.isVisible = false
            }
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
                    tvTitle.highlightText(query = query, result = tvTitle.text.toString())
                    tvSubtitle.text = contact.mobileNumber
                    tvSubtitle.highlightText(query = query, result = tvSubtitle.text.toString())
                    root.onSafeClick {
                        requireContext().openDialer(contact.mobileNumber)
                    }
                }
                count++
            }
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = universalSearchViewModel.textChangeFlow) { query: String ->
            if (FlowUtils.smsList.any { it.body?.contains(query, true) == true && query.isNotBlank() }.not()) return@collectLatestLifecycleFlow
            val allSmsList = FlowUtils.smsList.filter { it: Sms -> it.body?.contains(query, true) == true || it.number?.contains(query, true) == true }.distinctBy { it.body }
            val tvSmsLayoutList = listOf(layoutMessage1, layoutMessage2, layoutMessage3)
            tvSmsLayoutList.forEach { it: ListItemContactBinding ->
                it.root.isVisible = false
            }
            var count = 0
            for (sms: Sms in allSmsList) {
                if (count >= 3) break
                tvSmsLayoutList[count].apply {
                    root.isVisible = true
                    cardPicture.isVisible = false
                    tvTitle.text = sms.number
                    tvTitle.highlightText(query = query, result = tvTitle.text.toString())
                    tvSubtitle.text = sms.body
                    tvSubtitle.highlightText(query = query, result = tvSubtitle.text.toString())
                    ivCall.setImageDrawable(requireContext().drawable(R.drawable.outline_sms_24))
                    root.onSafeClick {
                        requireContext().sendSms(phoneNumber = sms.number ?: "", body = "")
                    }
                }
                count++
            }
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = universalSearchViewModel.textChangeFlow) { query: String ->
            if (FlowUtils.androidSettingsMap.values.any { it.contains(query, true) && query.isNotBlank() }.not()) return@collectLatestLifecycleFlow
            val mapList = FlowUtils.androidSettingsMap.filter { it: Map.Entry<String, String> -> it.value.contains(query, true) }
            val tvAndroidSettingsLayoutList = listOf(layoutSetting1, layoutSetting2, layoutSetting3)
            tvAndroidSettingsLayoutList.forEach { it: ListItemSanskritWordBinding ->
                it.root.isVisible = false
            }
            var count = 0
            for (item: Map.Entry<String, String> in mapList) {
                if (count >= 3) break
                tvAndroidSettingsLayoutList[count].apply {
                    root.isVisible = true
                    ivOpenOutward.isVisible = true
                    tvWord.text = FlowUtils.androidSettingsMap[item.key]
                    tvWord.highlightText(query = query, result = tvWord.text.toString())
                    root.onSafeClick {
                        // TODO find another way
                        requireContext().openSettings(screen = "android.settings.${item.key.replace(oldValue = "ACTION_", newValue = "")}")
                    }
                }
                count++
            }
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = universalSearchViewModel.textChangeFlow) { query: String ->
            if (FlowUtils.sanskritVocabMap.keys.any { it.contains(query, true) && query.isNotBlank() }.not()) return@collectLatestLifecycleFlow
            val keysList = FlowUtils.sanskritVocabMap.keys.filter { key: String -> key.contains(query, true) }
            val tvSanskritWordLayoutList = listOf(layoutWord1, layoutWord2, layoutWord3)
            tvSanskritWordLayoutList.forEach { it: ListItemSanskritWordBinding ->
                it.root.isVisible = false
            }
            var count = 0
            for (key: String in keysList) {
                if (count >= 3) break
                tvSanskritWordLayoutList[count].apply {
                    root.isVisible = true
                    ivOpenOutward.isVisible = true
                    ivOpenOutward.setImageDrawable(requireContext().drawable(R.drawable.outline_content_copy_24))
                    tvWord.text = "$key: ${FlowUtils.sanskritVocabMap[key]}"
                    tvWord.highlightText(query = query, result = tvWord.text.toString())
                    root.onSafeClick {
                        requireContext().clipboard()?.text = tvWord.text
                        binding.root.showSnackBar("Copied!")
                    }
                }
                count++
            }
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = universalSearchViewModel.textChangeFlow) { query: String ->
            if (FlowUtils.englishVocabMap.keys.any { it.contains(query, true) && query.isNotBlank() }.not()) return@collectLatestLifecycleFlow
            val keysList = FlowUtils.englishVocabMap.keys.filter { key: String -> key.contains(query, true) }
            val tvEnglishWordLayoutList = listOf(layoutWord4, layoutWord5, layoutWord6)
            tvEnglishWordLayoutList.forEach { it: ListItemSanskritWordBinding ->
                it.root.isVisible = false
            }
            var count = 0
            for (key: String in keysList) {
                if (count >= 3) break
                tvEnglishWordLayoutList[count].apply {
                    root.isVisible = true
                    ivOpenOutward.isVisible = true
                    ivOpenOutward.setImageDrawable(requireContext().drawable(R.drawable.outline_content_copy_24))
                    tvWord.text = "$key: ${FlowUtils.englishVocabMap[key]}"
                    tvWord.highlightText(query = query, result = tvWord.text.toString())
                    root.onSafeClick {
                        requireContext().clipboard()?.text = tvWord.text
                        binding.root.showSnackBar("Copied!")
                    }
                }
                count++
            }
        }
    }

    private fun FragmentUniversalSearchBinding.setupUI() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            if (Environment.isExternalStorageManager().not()) {
//            } else {
//                if (requireContext().isOldStorageReadPermissionGranted()) {
//
//                }
//
//                if (requireContext().isOldStorageWritePermissionGranted()) {
//
//                }
//            }
//        }
        getAllFiles().forEach {
            println("File nameeeeee" + it?.name)
        }
        grantPermissions()
        parseUniversalSearchDataWithWorker()
        setBlurredBackgroundImage()
    }

    private fun FragmentUniversalSearchBinding.setupUserActionListeners() {
        etSearch.onImeClick {
            etSearch.hideKeyboard()
            etSearch.clearFocus()
        }

        etSearch.doAfterTextChanged { it: Editable? ->
            val query = it.toString().toLowCase().trim()
            searchQuery = query

            cardRecentApps.isVisible = FlowUtils.recentAppList.isNotEmpty() && query.isBlank()
            cardApps.isVisible = FlowUtils.appList.any { it.title.contains(query, true) && query.isNotBlank() }
            cardContacts.isVisible = FlowUtils.contactsList.any { (it.name.contains(query, true) || it.mobileNumber.contains(query, true)) && query.isNotBlank() }
            cardMessages.isVisible = FlowUtils.smsList.any { (it.body?.contains(query, true) == true) && query.isNotBlank() }
            cardAndroidSettings.isVisible = FlowUtils.androidSettingsMap.keys.any { it.contains(query, true) && query.isNotBlank() }
            cardSanskritWords.isVisible = FlowUtils.sanskritVocabMap.keys.any { it.contains(query, true) && query.isNotBlank() }
            cardEnglishWords.isVisible = FlowUtils.englishVocabMap.keys.any { it.contains(query, true) && query.isNotBlank() }
            cardWebSearch.isVisible = false

            if (query.isBlank()) return@doAfterTextChanged

            universalSearchViewModel.setQueryValue(query)
            fetchWebLinksWithWorker(query)
        }

        btnCancel.onSafeClick {
            requireActivity().supportFragmentManager.popBackStackImmediate()
        }

        nestedScrollview.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            println("scrollY: $scrollY oldScrollY: $oldScrollY".trimIndent())
            if (scrollY - oldScrollY > 20) {
                etSearch.hideKeyboard()
            }

            if (scrollY == 0) {
                etSearch.showKeyboard()
            }
        })
    }

    private fun parseUniversalSearchDataWithWorker() {
        val workManager = WorkManager.getInstance(requireContext())
        val workRequest = PeriodicWorkRequestBuilder<UniversalSearchWorker>(PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MINUTES).build()
        workManager.enqueueUniquePeriodicWork(WorkerTag.UNIVERSAL_SEARCH, ExistingPeriodicWorkPolicy.UPDATE, workRequest)
    }

    private fun setBlurredBackgroundImage() {
        lifecycleScope.launch {
            val blurredBitmapFile = File(
                /* parent = */ requireContext().getHomeLayoutBlurredImageFileDir(),
                /* child = */ HOME_LAYOUT_BLURRED_IMAGE
            )
            if (blurredBitmapFile.exists().not()) return@launch
            val blurredBitmap = blurredBitmapFile.toBitmap() ?: return@launch
            withContext(Main) {
                binding.ivBackground.setImageBitmap(blurredBitmap)
            }
        }
    }

    private fun grantPermissions() {
        permissionsResult.launch(callContactSmsPermissionList)
    }

    private fun FragmentUniversalSearchBinding.setWebSearchData(webLinksList: List<String>) {
        val tvAndroidSettingsLayoutList = listOf(layoutWebLink1, layoutWebLink2, layoutWebLink3)
        tvAndroidSettingsLayoutList.forEach { it: ListItemSanskritWordBinding ->
            it.root.isVisible = false
        }
        var count = 0
        for (link: String in webLinksList) {
            if (count >= 3) break
            tvAndroidSettingsLayoutList[count].apply {
                root.isVisible = true
                ivOpenOutward.isVisible = true
                tvWord.text = link
                tvWord.highlightText(query = searchQuery, result = tvWord.text.toString())
                root.onSafeClick {
                    requireContext().showWebPage(url = "https://www.google.com/search?q=$link")
                }
            }
            count++
        }
        cardWebSearch.isVisible = webLinksList.isNotEmpty() && searchQuery.isNotBlank()
    }

    private fun fetchWebLinksWithWorker(query: String) {
        val workConstraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val data = Data.Builder().apply {
            putString(WorkerData.URL, "https://www.google.com/search?q=$query")
            putString(WorkerData.QUERY, searchQuery)
        }.build()
        val workRequest = OneTimeWorkRequestBuilder<WebLinkFetchWorker>()
            .setInputData(data)
            .setConstraints(workConstraints)
            .build()
        WorkManager.getInstance(requireContext()).enqueueUniqueWork(WorkerTag.WEB_LINKS_FETCH, ExistingWorkPolicy.REPLACE, workRequest)
        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(workRequest.id).observe(viewLifecycleOwner) { workInfo: WorkInfo? ->
            when (workInfo?.state) {
                WorkInfo.State.RUNNING -> println("RUNNING: show Progress")
                WorkInfo.State.ENQUEUED -> println("ENQUEUED: show Progress")
                WorkInfo.State.SUCCEEDED -> {
                    println("SUCCEEDED: stop Progress")
                    val webLinksList = workInfo.outputData.getStringArray(WorkerData.URL_LINKS_LIST)
                    binding.setWebSearchData(webLinksList?.toList() ?: emptyList())
                }
                WorkInfo.State.FAILED -> println("FAILED: stop showing Progress")
                WorkInfo.State.BLOCKED -> println("BLOCKED: show Progress")
                WorkInfo.State.CANCELLED -> println("CANCELLED: stop showing Progress")
                else -> Unit
            }
        }
    }
}