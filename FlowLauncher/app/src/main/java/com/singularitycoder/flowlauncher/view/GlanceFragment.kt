package com.singularitycoder.flowlauncher.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.databinding.FragmentGlanceBinding
import com.singularitycoder.flowlauncher.helper.*
import dagger.hilt.android.AndroidEntryPoint

// Refresh on every swipe
// Rearrangable cards

// Image or video glances - ability to add own images
// Unread message count
// Missed calls
// next 3 Remainders/Events

// Fav Youtube videos links
// 10k Hours - top 3 skills
// Perfect Me - routines
// My Goals - Top 3 goals

// My Mind - pinned notes
// My Loans
// My Expenses
// My Bills

@AndroidEntryPoint
class GlanceFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = GlanceFragment()
    }

    private lateinit var binding: FragmentGlanceBinding
    private val tempImageDrawableList = listOf(
        R.drawable.p1,
        R.drawable.p2,
        R.drawable.p3,
        R.drawable.p4,
        R.drawable.p5,
        R.drawable.p6,
        R.drawable.p7,
        R.drawable.p8,
        R.drawable.p9,
        R.drawable.p10,
        R.drawable.p11,
        R.drawable.p12,
        R.drawable.p13,
        R.drawable.p14,
        R.drawable.p15,
        R.drawable.p16,
        R.drawable.p17,
        R.drawable.p18,
        R.drawable.p19,
        R.drawable.p20,
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGlanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
    }

    private fun FragmentGlanceBinding.setupUI() {
        ivGlanceImage.layoutParams.height = deviceWidth() - 32.dpToPx()
        tvImageCount.text = "${1}/${tempImageDrawableList.size}"
        setupRemaindersCard()
        setupTakeActionCard()
    }

    private fun FragmentGlanceBinding.setupUserActionListeners() {
        var currentImagePosition = 0
        ivGlanceImage.setOnClickListener {
            doAfter(3.seconds()) {
                cardImageCount.isVisible = false
            }
            cardImageCount.isVisible = true
            tvImageCount.text = "${currentImagePosition + 1}/${tempImageDrawableList.size}"
            ivGlanceImage.setImageDrawable(requireContext().drawable(tempImageDrawableList[currentImagePosition]))
            if (currentImagePosition == tempImageDrawableList.lastIndex) {
                currentImagePosition = 0
            } else {
                currentImagePosition++
            }
        }
        ivGlanceImage.setOnLongClickListener {
            // Add or remove images. new bottom sheet
            false
        }
        cardYoutubeVideos.setOnClickListener {
            // Open youtube video in new screen. Auto oriented horizontally
        }
        cardYoutubeVideos.setOnLongClickListener {
            // Add or remove youtube video. new bottom sheet
            false
        }
        btnOpenExternal.setOnClickListener { view: View? ->
            view ?: return@setOnClickListener
            val glanceOptions = listOf("Add Media", "Add Remainders", "Add Youtube Videos")
            requireContext().showPopup(
                view = view,
                menuList = glanceOptions
            ) { position: Int ->
                when (glanceOptions[position]) {
                    glanceOptions[0] -> {
                        root.showSnackBar(glanceOptions[0])
                    }
                    glanceOptions[1] -> {
                        root.showSnackBar(glanceOptions[1])
                    }
                    glanceOptions[2] -> {
                        root.showSnackBar(glanceOptions[2])
                    }
                }
            }
        }
    }

    private fun FragmentGlanceBinding.setupTakeActionCard() {
        layoutMissedCalls.apply {
            tvKey.text = "Missed Calls"
            tvValue.text = "14"
        }
        layoutUnreadSms.apply {
            tvKey.text = "Unread SMS"
            tvValue.text = "293"
            divider.isVisible = false
        }
    }

    private fun FragmentGlanceBinding.setupRemaindersCard() {
        remainder1.apply {
            tvKey.text = "Sell mangoes to mango guy to get money for buying mangoes."
            tvValue.text = "12 SPT"
        }
        remainder2.apply {
            tvKey.text = "Climb mount everest."
            tvValue.text = "6 NOV"
        }
        remainder3.apply {
            tvKey.text = "Call Chacha Chaudhary."
            tvValue.text = "1 JAN"
            divider.isVisible = false
        }
    }
}