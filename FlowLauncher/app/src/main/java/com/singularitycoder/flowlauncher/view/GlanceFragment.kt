package com.singularitycoder.flowlauncher.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.databinding.FragmentGlanceBinding
import com.singularitycoder.flowlauncher.helper.deviceWidth
import com.singularitycoder.flowlauncher.helper.dpToPx
import com.singularitycoder.flowlauncher.helper.drawable

// Image or video glances
// Remainders next 3 days
// Events
// Unread message count
// Missed calls
// Fav Youtube videos links

// My Mind - pinned notes
// My Loans
// My Expenses
// My Bills
// Trip Me - most used visual meditation
// 10k Hours - top 3 skills
// Perfect Me - routines
// My Goals - Top 3 goals

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
    }

    private fun FragmentGlanceBinding.setupUserActionListeners() {
        var currentImagePosition = 0
        ivGlanceImage.setOnClickListener {
            tvImageCount.text = "${currentImagePosition + 1}/${tempImageDrawableList.size}"
            ivGlanceImage.setImageDrawable(requireContext().drawable(tempImageDrawableList[currentImagePosition]))
            if (currentImagePosition == tempImageDrawableList.lastIndex) {
                currentImagePosition = 0
            } else {
                currentImagePosition++
            }
        }
        ivGlanceImage.setOnLongClickListener {
            // Show full screen image
            false
        }
    }
}