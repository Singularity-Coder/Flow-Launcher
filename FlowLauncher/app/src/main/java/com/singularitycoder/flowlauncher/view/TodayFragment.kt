package com.singularitycoder.flowlauncher.view

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.singularitycoder.flowlauncher.databinding.FragmentTodayBinding
import com.singularitycoder.flowlauncher.helper.*
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
        binding.setupUserActionListeners()
    }

    private fun FragmentTodayBinding.setupUI() {
        val html = "21°<small><small><small>C</small></small></small>"
        tvTemperature.text = getHtmlFormattedTime(html)
        setRemainders()
        setQuotes()
    }

    private fun FragmentTodayBinding.setupUserActionListeners() {
        btnOpenExternal.setOnClickListener { view: View? ->
            view ?: return@setOnClickListener
            val todayOptions = listOf("Add Remainders", "Add Quotes")
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, todayOptions)
            requireContext().showListPopupMenu(view, adapter) { position: Int ->
                when (todayOptions[position]) {
                    todayOptions[0] -> {
                        root.showSnackBar(todayOptions[0])
                    }
                    todayOptions[1] -> {
                        root.showSnackBar(todayOptions[1])
                    }
                }
            }
        }

        var quotePosition = 0
        var gradientPosition = 0
        cardQuotes.setOnClickListener {
            val calculatedQuotePosition = if (quotePosition == animeQuoteList.lastIndex) {
                quotePosition = 0
                quotePosition
            } else quotePosition
            val calculatedGradientPosition = if (gradientPosition == gradientList.lastIndex) {
                gradientPosition = 0
                gradientPosition
            } else gradientPosition
            // TODO replace this with db list
            tvQuote.text = "${animeQuoteList[calculatedQuotePosition].quote}\n\n- ${animeQuoteList[calculatedQuotePosition].author}"
            tvQuote.setTextColor(requireContext().color(quoteColorList[calculatedGradientPosition].textColor))
            clQuotes.background = requireContext().drawable(quoteColorList[calculatedGradientPosition].gradientColor)
            ivQuoteBackground.imageTintList = ColorStateList.valueOf(requireContext().color(quoteColorList[calculatedGradientPosition].iconColor))
            quotePosition++
            gradientPosition++
        }
    }

    private fun FragmentTodayBinding.setRemainders() {
        remainder1.apply {
            tvKey.text = "Sell mangoes to mango guy to get money for buying mangoes."
            tvValue.text = "8:45 AM"
        }
        remainder2.apply {
            tvKey.text = "Climb mount everest."
            tvValue.text = "11:00 AM"
        }
        remainder3.apply {
            tvKey.text = "Call Chacha Chaudhary."
            tvValue.text = "3:45 PM"
            divider.isVisible = false
        }
    }

    private fun setQuotes() {

    }
}