package com.singularitycoder.flowlauncher

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ThisApp : Application() {
    var isHomeScreenLoaded = false
}
