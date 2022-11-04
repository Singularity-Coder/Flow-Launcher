package com.singularitycoder.flowlauncher.helper.blur

import android.graphics.Bitmap

// Credit: https://github.com/elye/demo_android_blur_image
// https://medium.com/mobile-app-development-publication/blurring-image-algorithm-example-in-android-cec81911cd5e
interface BlurEngine {
    fun blur(image: Bitmap, radius: Int): Bitmap
    fun getType(): String
}
