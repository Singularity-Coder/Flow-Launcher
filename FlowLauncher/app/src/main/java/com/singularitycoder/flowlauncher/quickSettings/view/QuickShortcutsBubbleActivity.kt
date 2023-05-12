package com.singularitycoder.flowlauncher.quickSettings.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.databinding.ActivityQuickShortcutsBubbleBinding

/**
 * https://github.com/android/user-interface-samples/tree/main/People
 * Entry point of the app when it is launched as an expanded Bubble.
 */
class QuickShortcutsBubbleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuickShortcutsBubbleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuickShortcutsBubbleBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}