package com.singularitycoder.flowlauncher.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.singularitycoder.flowlauncher.databinding.FragmentAppsBinding
import com.singularitycoder.flowlauncher.helper.deviceHeight
import com.singularitycoder.flowlauncher.helper.deviceWidth
import com.singularitycoder.flowlauncher.helper.dpToPx

class AppsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = AppsFragment()
    }

    private lateinit var binding: FragmentAppsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAppsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
    }

    private fun FragmentAppsBinding.setupUI() {
        root.viewTreeObserver.addOnGlobalLayoutListener {
            // Do this when view inflate complete
            // root.layoutParams.width = deviceWidth() - 60.dpToPx()
        }
    }
}