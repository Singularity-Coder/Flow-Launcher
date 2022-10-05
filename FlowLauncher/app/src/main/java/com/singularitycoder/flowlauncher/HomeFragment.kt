package com.singularitycoder.flowlauncher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.singularitycoder.flowlauncher.databinding.FragmentHomeBinding

// Pre built commands for voice search
// 1. OPEN app_name -> opens app, etc
// 2. CALL contact_name -> calls directly
// 3. MESSAGE contact_name SAYING message_body  -> Opens message
// 4. SEARCH FOR query -> Open chrome

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
    }

    override fun onResume() {
        super.onResume()
        println("This triggers everytime we switch the screen")
    }

    private fun FragmentHomeBinding.setupUI() {
        rvApps.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = homeAppsAdapter
            addItemDecoration(GridSpacingItemDecoration(
                spanCount = 4 /* columns */,
                spacing = 24.dpToPx() /* px */,
                includeEdge = false
            ))
        }
        homeAppsList.addAll(requireContext().appList())
        homeAppsAdapter.apply {
            this.homeAppList = this@HomeFragment.homeAppsList
            notifyDataSetChanged()
        }
    }
}