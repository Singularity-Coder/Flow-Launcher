package com.singularitycoder.flowlauncher.glance.view

import android.os.Bundle
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.singularitycoder.flowlauncher.databinding.ActivityYoutubeVideoBinding
import com.singularitycoder.flowlauncher.helper.avoidScreenShots
import com.singularitycoder.flowlauncher.helper.constants.*
import com.singularitycoder.flowlauncher.helper.fullScreen
import com.singularitycoder.flowlauncher.glance.model.YoutubeVideo

// https://developers.google.com/youtube/android/player
// https://guides.codepath.com/android/Streaming-Youtube-Videos-with-YouTubePlayerView
// https://www.sitepoint.com/using-the-youtube-api-to-embed-video-in-an-android-app/
// https://stackoverflow.com/questions/18175397/add-youtube-data-api-to-android-studio
// https://stackoverflow.com/questions/5712849/how-do-i-keep-the-screen-on-in-my-app
// https://guides.codepath.com/android/Streaming-Youtube-Videos-with-YouTubePlayerView
// https://stackoverflow.com/questions/7818717/why-not-use-always-androidconfigchanges-keyboardhiddenorientation

class YoutubeVideoActivity : YouTubeBaseActivity() {

    private lateinit var binding: ActivityYoutubeVideoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoutubeVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        avoidScreenShots()
        fullScreen()
        initYoutubePlayer()
    }

    override fun onResume() {
        super.onResume()
        binding.root.keepScreenOn = true
    }

    override fun onPause() {
        super.onPause()
        binding.root.keepScreenOn = false
    }

    private fun initYoutubePlayer() = binding.youtubePlayerView.initialize(
        "YOUTUBE_API_AUTH_TOKEN",
        object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(
                provider: YouTubePlayer.Provider,
                youTubePlayer: YouTubePlayer,
                wasRestored: Boolean
            ) {
                if (wasRestored) return
                val youtubeVideoId = intent.getStringExtra(IntentKey.YOUTUBE_VIDEO_ID)
                val youtubeVideosFromDb = intent.getParcelableArrayListExtra<YoutubeVideo>(IntentKey.YOUTUBE_VIDEO_LIST)
                val youtubeVideoIdList = youtubeVideosFromDb?.map { it.videoId } ?: emptyList()
                if (youtubeVideoIdList.isEmpty() || youtubeVideoIdList.size == 1) {
                    if (youtubeVideoId.isNullOrBlank()) return
                    youTubePlayer.loadVideo(youtubeVideoId) // Load Single Video
                    return
                }
                youTubePlayer.apply {
                    val start = youtubeVideoIdList.indexOf(youtubeVideoId)
                    val end = youtubeVideoIdList.size
                    setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT) // Player styles: CHROMELESS, MINIMAL, DEFAULT
                    loadVideos(youtubeVideoIdList.subList(start, end)) // Set PlayList
                }
            }

            override fun onInitializationFailure(
                provider: YouTubePlayer.Provider,
                youTubeInitializationResult: YouTubeInitializationResult
            ) = print("Youtube failed to load video")
        })
}
