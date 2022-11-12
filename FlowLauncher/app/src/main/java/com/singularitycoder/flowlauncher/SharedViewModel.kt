package com.singularitycoder.flowlauncher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.singularitycoder.flowlauncher.db.HolidayDao
import com.singularitycoder.flowlauncher.db.NewsDao
import com.singularitycoder.flowlauncher.db.TrendingTweetDao
import com.singularitycoder.flowlauncher.db.WeatherDao
import com.singularitycoder.flowlauncher.model.Holiday
import com.singularitycoder.flowlauncher.model.News
import com.singularitycoder.flowlauncher.model.TrendingTweet
import com.singularitycoder.flowlauncher.model.Weather
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    weatherDao: WeatherDao,
    newsDao: NewsDao,
    holidayDao: HolidayDao,
    trendingTweetDao: TrendingTweetDao,
) : ViewModel() {

    var weatherLiveData: LiveData<Weather> = MutableLiveData<Weather>()
        private set
    var newsListLiveData: LiveData<List<News>> = MutableLiveData<List<News>>()
        private set
    var holidayListLiveData: LiveData<List<Holiday>> = MutableLiveData<List<Holiday>>()
        private set
    var trendingTweetListLiveData: LiveData<List<TrendingTweet>> = MutableLiveData<List<TrendingTweet>>()
        private set

    init {
        weatherLiveData = weatherDao.getLatestWeatherLiveData()
        newsListLiveData = newsDao.getAllNewsLiveData()
        holidayListLiveData = holidayDao.getAllHolidaysLiveData()
        trendingTweetListLiveData = trendingTweetDao.getAllTrendingTweetsLiveData()
    }
}
