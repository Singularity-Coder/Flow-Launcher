package com.singularitycoder.flowlauncher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.singularitycoder.flowlauncher.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


// Maybe option to change color
// 12 hr, 24 hr clock
// Time listener
// Fav Walls - My Wall - set of 10 fav images - Glance

// Pre built commands for voice search
// 1. OPEN app_name -> opens app, etc
// 2. CALL contact_name -> calls directly
// 3. MESSAGE contact_name SAYING message_body  -> Opens message
// 4. SEARCH FOR query -> Open chrome

@AndroidEntryPoint
class HomeFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }

    private lateinit var binding: FragmentHomeBinding

    private val homeAppsList = mutableListOf<App>()
    private val homeAppsAdapter = HomeAppsAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()

        // https://stackoverflow.com/questions/5369682/how-to-get-current-time-and-date-in-android#:~:text=Date%3B%20Date%20currentTime%20%3D%20Calendar.,getTime()%3B
        // https://stackoverflow.com/questions/7672597/how-to-get-timezone-from-android-mobile
        println("Time now: ${Calendar.getInstance().time}") // Sat Oct 08 00:58:23 GMT+05:30 2022
        println("Time zone: ${TimeZone.getDefault()}") // libcore.util.ZoneInfo[mDstSavings=0,mUseDst=false,mDelegate=[id="Asia/Kolkata",mRawOffset=19800000,mEarliestRawOffset=21208000,transitions=7]]
    }

    override fun onResume() {
        super.onResume()
        println("This triggers everytime we switch the screen")
    }

    private fun FragmentHomeBinding.setupUI() {
        rvApps.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = homeAppsAdapter
            addItemDecoration(
                GridSpacingItemDecoration(
                    spanCount = 4 /* columns */,
                    spacing = 24.dpToPx() /* px */,
                    includeEdge = false
                )
            )
        }
        homeAppsList.addAll(requireContext().appList())
        homeAppsAdapter.apply {
            this.homeAppList = this@HomeFragment.homeAppsList
            notifyDataSetChanged()
        }

        val time = convertLongToTime(timeNow, DateType.h_mm_a)
        val hours = time.substringBefore(":")
        val minutes = time.substringAfter(":").substringBefore(" ")
        val dayPeriod = time.substringAfter(" ")
        val html = "$hours : $minutes <small><small><small>$dayPeriod</small></small></small>"
        tvTime.text = getHtmlFormattedTime(html)
        tvFlowType.text = "Work Flow  |  ${convertLongToTime(timeNow, DateType.dd_MMM_yyyy)}"
    }

    private fun FragmentHomeBinding.setupUserActionListeners() {
        homeAppsAdapter.setItemClickListener { app, position ->
            requireActivity().launchApp(app.packageName)
        }
        homeAppsAdapter.setItemLongClickListener { app, position ->
            requireContext().showAlertDialog(
                title = app.title,
                message = "Do you want to remove this app?",
                positiveAction = {
                    CoroutineScope(IO).launch {
                        // FIXME uninstall callback
                        requireActivity().uninstallApp(app.packageName)
                        withContext(Main) {
                            homeAppsAdapter.notifyItemRemoved(position)
                        }
                    }
                }
            )
        }
        root.setOnLongClickListener {
            // Open flow switcher
            false
        }
    }
}