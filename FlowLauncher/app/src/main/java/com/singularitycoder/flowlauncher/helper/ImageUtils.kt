package com.singularitycoder.flowlauncher

import android.content.Context
import android.graphics.*
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Matrix4f
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicColorMatrix
import androidx.annotation.RequiresApi

// https://stackoverflow.com/questions/5699810/how-to-change-bitmap-image-color-in-android
// https://stackoverflow.com/questions/3373860/convert-a-bitmap-to-grayscale-in-android#comment15100006_7727183
// https://developer.android.com/reference/android/graphics/ColorMatrix
// https://kazzkiq.github.io/svg-color-filter/
// https://developer.android.com/reference/android/graphics/ColorMatrix
// https://medium.com/@lokeshdeshmukh_33593/android-imageview-bitmap-filter-using-color-matrix-1a37666266a6

// R’ = a*R + b*G + c*B + d*A + e;
// G’ = f*R + g*G + h*B + i*A + j;
// B’ = k*R + l*G + m*B + n*A + o;
// A’ = p*R + q*G + r*B + s*A + t;

// [ 1 0 0 0 0   - red vector
//   0 1 0 0 0   - green vector
//   0 0 1 0 0   - blue vector
//   0 0 0 1 0 ] - alpha vector
val blackishFilter = floatArrayOf(
    0.136f, 0.458f, 0.046f, 0.000f, 0.180f,
    0.136f, 0.458f, 0.046f, 0.000f, 0.180f,
    0.136f, 0.458f, 0.046f, 0.000f, 0.180f,
    0.000f, 0.000f, 0.000f, 1.000f, 0.000f
)
val blueishFilter = floatArrayOf(
    1f, 0f, 0f, 0f, 0f,
    -0.2f, 1.0f, 0.3f, -1.9f, -3f,
    -0.1f, 0f, 1f, 0f, 0f,
    0f, 0f, 0f, 1f, -0.7f
)
val maroonishFilter = floatArrayOf(
    1.250f, -0.110f, -0.321f, 0.000f, 0.226f,
    -0.032f, 1.170f, -0.321f, 0.000f, 0.226f,
    -0.032f, -0.110f, 0.960f, 0.000f, 0.226f,
    0.000f, 0.000f, -0.275f, 1.000f, 0.000f
)
val blackFilter = floatArrayOf(
    0.136f, 0.458f, 0.046f, 0.000f, 0.180f,
    0.136f, 0.458f, 0.046f, 0.000f, 0.180f,
    0.136f, 0.458f, 0.046f, 0.000f, 0.180f,
    0.000f, 0.000f, 0.000f, 1.000f, 0.000f
)
val blueFilter1 = floatArrayOf(
    0f, 0.458f, 0f, 0.000f, 0f,
    0.136f, 0.458f, 0f, 0.000f, 0f,
    0f, 0f, 1f, 0.000f, 0f,
    0.000f, 0.000f, 0.000f, 1.000f, 0.000f
)
val blueFilter2 = floatArrayOf(
    0f, 0f, 0f, 0f, 0f,
    0f, 0f, 0f, 0f, 0f,
    0f, 0f, 1f, 0f, 0f,
    0f, 0f, 0f, 1f, 1f
)
val purpleFilter = floatArrayOf(
    1f, -0.2f, 0f, 0f, 0f,
    0f, 1f, -2.6f, -0.1f, 0f,
    0f, 1.2f, 1f, 0.1f, 0f,
    0f, 0f, 1.7f, 1f, 0f
)
val purpleFilter2 = floatArrayOf(
    0.136f, 0.458f, 0f, 0.000f, 0f,
    0.136f, 0.458f, 0f, 0.000f, 0f,
    0.136f, 0f, 1f, 0.000f, 0f,
    0.000f, 0.000f, 0.000f, 1.000f, 0.000f
)

// https://stackoverflow.com/questions/3373860/convert-a-bitmap-to-grayscale-in-android
fun Bitmap.toGrayscale(): Bitmap? {
    val bitmapGrayScaled = Bitmap.createBitmap(
        /* width = */ this.width,
        /* height = */ this.height,
        /* config = */ Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmapGrayScaled)
    val paint = Paint()
    val colorMatrix = ColorMatrix().apply {
        setSaturation(0f)
    }
    val colorMatrixColorFilter = ColorMatrixColorFilter(colorMatrix)
    paint.colorFilter = colorMatrixColorFilter
    canvas.drawBitmap(
        /* bitmap = */ this,
        /* left = */ 0f,
        /* top = */ 0f,
        /* paint = */ paint
    )
    return bitmapGrayScaled
}

fun Bitmap.toBlueScale(): Bitmap? {
    val bitmapGrayScaled = Bitmap.createBitmap(
        /* width = */ this.width,
        /* height = */ this.height,
        /* config = */ Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmapGrayScaled)
    val paint = Paint()
    val colorMatrix = ColorMatrix(purpleFilter2)
    val colorMatrixColorFilter = ColorMatrixColorFilter(colorMatrix)
    paint.colorFilter = colorMatrixColorFilter
    canvas.drawBitmap(
        /* bitmap = */ this,
        /* left = */ 0f,
        /* top = */ 0f,
        /* paint = */ paint
    )
    return bitmapGrayScaled
}

// https://xjaphx.wordpress.com/2011/06/21/image-processing-grayscale-image-on-the-fly/
fun Bitmap.toGrayScaledBitmapFallback(
    redVal: Float = 0.299f,
    greenVal: Float = 0.587f,
    blueVal: Float = 0.114f
): Bitmap {
    // create output bitmap
    val bitmapGrayScaled = Bitmap.createBitmap(this.width, this.height, this.config)

    // pixel information
    var A: Int
    var R: Int
    var G: Int
    var B: Int
    var pixel: Int

    // get image size
    val width = this.width
    val height = this.height

    // scan through every single pixel
    for (x in 0 until width) {
        for (y in 0 until height) {
            // get one pixel color
            pixel = this.getPixel(x, y)
            // retrieve color of all channels
            A = Color.alpha(pixel)
            R = Color.red(pixel)
            G = Color.green(pixel)
            B = Color.blue(pixel)
            // take conversion up to one single value
            B = (redVal * R + greenVal * G + blueVal * B).toInt()
            G = B
            R = G
            // set new pixel color to output bitmap
            bitmapGrayScaled.setPixel(
                /* x = */ x,
                /* y = */ y,
                /* color = */ Color.argb(A, R, G, B)
            )
        }
    }

    return bitmapGrayScaled
}

// https://gist.github.com/imminent/cf4ab750104aa286fa08
// https://en.wikipedia.org/wiki/Grayscale
fun Bitmap.toGrayScaledBitmap(context: Context): Bitmap {
    val redVal = 0.299f
    val greenVal = 0.587f
    val blueVal = 0.114f
    val render = RenderScript.create(context)
    val matrix = Matrix4f(floatArrayOf(-redVal, -redVal, -redVal, 1.0f, -greenVal, -greenVal, -greenVal, 1.0f, -blueVal, -blueVal, -blueVal, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f))
    val result = this.copy(this.config, true)
    val input = Allocation.createFromBitmap(render, this, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT)
    val output = Allocation.createTyped(render, input.type)
    // Inverts and do grayscale to the image
    val inverter = ScriptIntrinsicColorMatrix.create(render)
    inverter.setColorMatrix(matrix)
    inverter.forEach(input, output)
    output.copyTo(result)
    this.recycle()
    render.destroy()
    return result
}

// https://stackoverflow.com/questions/44109057/get-video-thumbnail-from-uri
@RequiresApi(Build.VERSION_CODES.O_MR1)
fun Context.getVideoThumbnailBitmap(docUri: Uri): Bitmap? {
    return try {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(this, docUri)
        mmr.getScaledFrameAtTime(
            1000, /* Time in Video */
            MediaMetadataRetriever.OPTION_NEXT_SYNC,
            128,
            128
        )
    } catch (e: Exception) {
        null
    }
}