package com.singularitycoder.flowlauncher.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.singularitycoder.flowlauncher.R
import dagger.hilt.android.AndroidEntryPoint

// Quote of the day
// Weather today
// Headlines today
// Remainders today - Remind Me remainders
// Todos today
// Perfect Me routines

@AndroidEntryPoint
class TodayFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = TodayFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_today, container, false)
    }
}