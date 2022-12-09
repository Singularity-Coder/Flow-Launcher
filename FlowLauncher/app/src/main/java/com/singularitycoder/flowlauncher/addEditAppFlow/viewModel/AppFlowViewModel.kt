package com.singularitycoder.flowlauncher.addEditAppFlow.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.flowlauncher.today.dao.QuoteDao
import com.singularitycoder.flowlauncher.today.dao.WeatherDao
import com.singularitycoder.flowlauncher.today.model.Quote
import com.singularitycoder.flowlauncher.today.model.Weather
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppFlowViewModel @Inject constructor(
    weatherDao: WeatherDao,
    private val quoteDao: QuoteDao
) : ViewModel() {

    var weatherLiveData: LiveData<Weather> = MutableLiveData<Weather>()
        private set

    init {
        weatherLiveData = weatherDao.getLatestWeatherLiveData()
    }

    fun addQuoteToDb(quote: Quote) = viewModelScope.launch {
        quoteDao.insert(quote)
    }

    //--------------------------------------------------------------------------------

    fun deleteQuoteFromDb(link: String) = viewModelScope.launch {
        quoteDao.deleteByTitle(link)
    }
}
