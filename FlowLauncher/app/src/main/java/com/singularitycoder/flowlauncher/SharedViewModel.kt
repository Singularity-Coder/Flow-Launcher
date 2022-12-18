package com.singularitycoder.flowlauncher

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.flowlauncher.glance.dao.GlanceImageDao
import com.singularitycoder.flowlauncher.glance.dao.HolidayDao
import com.singularitycoder.flowlauncher.glance.dao.YoutubeVideoDao
import com.singularitycoder.flowlauncher.glance.model.GlanceImage
import com.singularitycoder.flowlauncher.glance.model.Holiday
import com.singularitycoder.flowlauncher.glance.model.YoutubeVideo
import com.singularitycoder.flowlauncher.today.dao.NewsDao
import com.singularitycoder.flowlauncher.today.dao.QuoteDao
import com.singularitycoder.flowlauncher.today.dao.TrendingTweetDao
import com.singularitycoder.flowlauncher.today.dao.WeatherDao
import com.singularitycoder.flowlauncher.today.model.News
import com.singularitycoder.flowlauncher.today.model.Quote
import com.singularitycoder.flowlauncher.today.model.TrendingTweet
import com.singularitycoder.flowlauncher.today.model.Weather
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

    val weatherLiveData: LiveData<Weather> by lazy {
        weatherDao.getLatestWeatherLiveData()
    }
    val newsListLiveData: LiveData<List<News>> by lazy {
        newsDao.getAllNewsLiveData()
    }
    val holidayListLiveData: LiveData<List<Holiday>> by lazy {
        holidayDao.getAllHolidaysLiveData()
    }
    val trendingTweetListLiveData: LiveData<List<TrendingTweet>> by lazy {
        trendingTweetDao.getAllTrendingTweetsLiveData()
    }

    val glanceImageListLiveData: LiveData<List<GlanceImage>> by lazy {
        glanceImageDao.getAllLiveData()
    }
    val youtubeVideoListLiveData: LiveData<List<YoutubeVideo>> by lazy {
        youtubeVideoDao.getAllLiveData()
    }
    val quoteListLiveData: LiveData<List<Quote>> by lazy {
        quoteDao.getAllLiveData()
    }

    //--------------------------------------------------------------------------------

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

    //--------------------------------------------------------------------------------
}
