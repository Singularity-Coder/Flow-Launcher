package com.singularitycoder.flowlauncher

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.singularitycoder.flowlauncher.databinding.ActivityMainBinding
import com.singularitycoder.flowlauncher.glance.view.GlanceFragment
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.constants.Broadcast
import com.singularitycoder.flowlauncher.helper.constants.HomeScreen
import com.singularitycoder.flowlauncher.home.view.HomeFragment
import com.singularitycoder.flowlauncher.today.view.TodayFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// https://steemit.com/utopian-io/@ideba/how-to-build-a-custom-android-launcher-and-home-screen-application-part-1

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var audioManager: AudioManager

    private lateinit var binding: ActivityMainBinding

    private val viewPager2PageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            println("viewpager2: onPageScrollStateChanged")
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            println("viewpager2: onPageSelected")
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            println("viewpager2: onPageScrolled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpViewPager()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    audioManager.raiseVolume()
                    sendBroadcast(Intent(Broadcast.VOLUME_RAISED))
                }
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    audioManager.lowerVolume()
                    sendBroadcast(Intent(Broadcast.VOLUME_LOWERED))
                }
                true
            }
            KeyEvent.KEYCODE_POWER -> {
                true
            }
            else -> super.dispatchKeyEvent(event)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.viewpagerMain.unregisterOnPageChangeCallback(viewPager2PageChangeListener)
    }

    private fun setUpViewPager() {
        binding.viewpagerMain.apply {
            adapter = MainViewPagerAdapter(fragmentManager = supportFragmentManager, lifecycle = lifecycle)
            registerOnPageChangeCallback(viewPager2PageChangeListener)
            currentItem = 1
        }
    }

    fun showHomeScreen(position: Int) {
        binding.viewpagerMain.currentItem = position
    }

    fun showOnBarcodeScanComplete(scannedResult: String) {
        showAlertDialog(
            title = "QR code scan result",
            message = scannedResult,
            positiveBtnText = "Copy",
            positiveAction = {
                clipboard()?.text = scannedResult
                showToast(message = "Copied")
            }
        )
    }

    inner class MainViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = HomeScreen.values().size
        override fun createFragment(position: Int): Fragment = when (position) {
            HomeScreen.GLANCE.ordinal -> GlanceFragment.newInstance()
            HomeScreen.APPS.ordinal -> HomeFragment.newInstance()
            else -> TodayFragment.newInstance()
        }
    }
}