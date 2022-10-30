package com.singularitycoder.flowlauncher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.singularitycoder.flowlauncher.db.WeatherDao
import com.singularitycoder.flowlauncher.model.Weather
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(weatherDao: WeatherDao) : ViewModel() {

    var weather: LiveData<Weather> = MutableLiveData<Weather>()

    init {
        weather = weatherDao.getLatestNewsLiveData()
    }
}
