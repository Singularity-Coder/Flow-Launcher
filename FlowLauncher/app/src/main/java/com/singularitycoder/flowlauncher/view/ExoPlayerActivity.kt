package com.singularitycoder.flowlauncher.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.util.Util
import com.singularitycoder.flowlauncher.databinding.ActivityExoPlayerBinding
import com.singularitycoder.flowlauncher.helper.constants.IntentKey
import com.singularitycoder.flowlauncher.helper.avoidScreenShots
import com.singularitycoder.flowlauncher.helper.fullScreen
import com.singularitycoder.flowlauncher.model.YoutubeVideo
import dagger.hilt.android.AndroidEntryPoint

// https://developer.android.com/codelabs/exoplayer-intro#2
// https://exoplayer.dev/

// Exo player can do audio and video streaming
// Use playlists, clip or merge media
// stream audio and video files directly from server without downloading
// Provides smooth encryption and streaming of video and audio files
// can customise media player
// supports dynamic streaming over HTTP
// works on devices >= API 16
@AndroidEntryPoint
class ExoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExoPlayerBinding

    private var isPlayWhenReady = true
    private var currentTime = 0
    private var playbackPosition = 0L
    private var exoPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        avoidScreenShots()
        fullScreen()
        setUpExoPlayer()
    }

    override fun onStart() {
        super.onStart()
        if ((Util.SDK_INT <= 23 || exoPlayer == null)) initializeExoPlayer()
    }

    override fun onResume() {
        super.onResume()
        binding.root.keepScreenOn = true
    }

    override fun onPause() {
        super.onPause()
        binding.root.keepScreenOn = false
        if (Util.SDK_INT <= 23) releaseExoPlayer()
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT <= 23) releaseExoPlayer()
    }

    private fun setUpExoPlayer() {
        val episodeList = try {
            intent.getParcelableArrayListExtra<YoutubeVideo?>(IntentKey.YOUTUBE_VIDEO_LIST) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        val mediaItemsList = episodeList.mapNotNull { it: YoutubeVideo -> MediaItem.fromUri(it.videoId ?: "") }
        exoPlayer = ExoPlayer.Builder(this).build().also { it: ExoPlayer ->
            binding.exoPlayerView.player = it
        }.apply {
            addMediaItems(mediaItemsList)
            prepare()
            playWhenReady = true // Since we are loading from url, we cannot directly set play()
        }
    }

    private fun initializeExoPlayer() {
        TODO("Not yet implemented")
    }

    private fun releaseExoPlayer() {
        TODO("Not yet implemented")
    }
}
