package com.singularitycoder.flowlauncher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.flowlauncher.db.*
import com.singularitycoder.flowlauncher.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    weatherDao: WeatherDao,
    newsDao: NewsDao,
    holidayDao: HolidayDao,
    trendingTweetDao: TrendingTweetDao,
    private val glanceImageDao: GlanceImageDao,
    private val youtubeVideoDao: YoutubeVideoDao,
    private val quoteDao: QuoteDao
) : ViewModel() {

    var weatherLiveData: LiveData<Weather> = MutableLiveData<Weather>()
        private set
    var newsListLiveData: LiveData<List<News>> = MutableLiveData<List<News>>()
        private set
    var holidayListLiveData: LiveData<List<Holiday>> = MutableLiveData<List<Holiday>>()
        private set
    var trendingTweetListLiveData: LiveData<List<TrendingTweet>> = MutableLiveData<List<TrendingTweet>>()
        private set

    var glanceImageListLiveData: LiveData<List<GlanceImage>> = MutableLiveData<List<GlanceImage>>()
        private set
    var youtubeVideoListLiveData: LiveData<List<YoutubeVideo>> = MutableLiveData<List<YoutubeVideo>>()
        private set
    var quoteListLiveData: LiveData<List<Quote>> = MutableLiveData<List<Quote>>()
        private set


    init {
        weatherLiveData = weatherDao.getLatestWeatherLiveData()
        newsListLiveData = newsDao.getAllNewsLiveData()
        holidayListLiveData = holidayDao.getAllHolidaysLiveData()
        trendingTweetListLiveData = trendingTweetDao.getAllTrendingTweetsLiveData()

        glanceImageListLiveData = glanceImageDao.getAllLiveData()
        youtubeVideoListLiveData = youtubeVideoDao.getAllLiveData()
        quoteListLiveData = quoteDao.getAllLiveData()
    }

    fun addQuoteToDb(quote: Quote) = viewModelScope.launch {
        quoteDao.insert(quote)
    }

    fun addGlanceImageToDb(glanceImage: GlanceImage) = viewModelScope.launch {
        glanceImageDao.insert(glanceImage)
    }

    fun addYoutubeVideoToDb(youtubeVideo: YoutubeVideo) = viewModelScope.launch {
        youtubeVideoDao.insert(youtubeVideo)
    }

    //--------------------------------------------------------------------------------

    fun deleteQuoteFromDb(link: String) = viewModelScope.launch {
        quoteDao.deleteByTitle(link)
    }

    fun deleteGlanceImageFromDb(link: String) = viewModelScope.launch {
        glanceImageDao.deleteByLink(link)
    }

    fun deleteYoutubeVideoFromDb(videoId: String) = viewModelScope.launch {
        youtubeVideoDao.deleteByVideoId(videoId)
    }
}
