package com.singularitycoder.flowlauncher.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.singularitycoder.flowlauncher.databinding.FragmentTodayBinding
import com.singularitycoder.flowlauncher.helper.getHtmlFormattedTime
import dagger.hilt.android.AndroidEntryPoint

// Refresh on every swipe
// Rearrangable cards

// Quote of the day
// Weather today
// Headlines today - Location, Category while scraping data
// Remainders today - Remind Me remainders
// My Habits - Todos today
// Perfect Me routines
// Todos - checklist, notes

// Analyze Me - daily analysis
// Trip Me - most used visual meditation


@AndroidEntryPoint
class TodayFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = TodayFragment()
    }

    private lateinit var binding: FragmentTodayBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
    }

    private fun FragmentTodayBinding.setupUI() {
        val html = "21°<small><small><small>C</small></small></small>"
        tvTemperature.text = getHtmlFormattedTime(html)
        setRemainders()
    }

    private fun FragmentTodayBinding.setRemainders() {
        remainder1.apply {
            tvRemainder.text = "Sell mangoes to mango guy to get money for buying mangoes."
            tvRemainderDate.text = "8:45 AM"
        }
        remainder2.apply {
            tvRemainder.text = "Climb mount everest."
            tvRemainderDate.text = "11:00 AM"
        }
        remainder3.apply {
            tvRemainder.text = "Call Chacha Chaudhary."
            tvRemainderDate.text = "3:45 PM"
            dividerRemainders.isVisible = false
        }
    }
}