package com.singularitycoder.flowlauncher

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.renderscript.*
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * https://developer.android.com/training/package-visibility
 * Add query activities in manifest for getting full access to all apps
 * queryIntentActivities(), getPackageInfo(), and getInstalledApplications() need this
 * The limited visibility also affects explicit interactions with other apps, such as starting another app's service.
 * To allow your app to see all other installed apps, Android 11 introduces the QUERY_ALL_PACKAGES permission.
 * */
fun Context.appList(): List<App> {
    val appList = mutableListOf<App>()
    val intent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val allApps = if (Build.VERSION.SDK_INT > 33) {
        packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
    } else {
        packageManager.queryIntentActivities(intent, 0)
    }
    for (item in allApps) {
        val app = App().apply {
            title = item.loadLabel(packageManager).toString()
            packageName = item.activityInfo.packageName
            icon = item.activityInfo.loadIcon(packageManager)
            println("packageName: $packageName")
        }
        appList.add(app)
    }
    return appList
}

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
//            A = Color.alpha(pixel)
//            R = Color.red(pixel)
//            G = Color.green(pixel)
            A = Color.blue(pixel)
            R = Color.blue(pixel)
            G = Color.blue(pixel)
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

fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Context.color(@ColorRes colorRes: Int) = ContextCompat.getColor(this, colorRes)

fun Context.drawable(@DrawableRes drawableRes: Int): Drawable? =
    ContextCompat.getDrawable(this, drawableRes)

fun View.showSnackBar(
    message: String,
    anchorView: View? = null,
    duration: Int = Snackbar.LENGTH_SHORT,
    actionBtnText: String = "NA",
    action: () -> Unit = {},
) {
    Snackbar.make(this, message, duration).apply {
        this.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
        if (null != anchorView) this.anchorView = anchorView
        if ("NA" != actionBtnText) setAction(actionBtnText) { action.invoke() }
        this.show()
    }
}

// Get Epoch Time
val timeNow: Long
    get() = System.currentTimeMillis()

fun Long.toIntuitiveDateTime(): String {
    val postedTime = this
    val elapsedTimeMillis = timeNow - postedTime
    val elapsedTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis)
    val elapsedTimeInMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTimeMillis)
    val elapsedTimeInHours = TimeUnit.MILLISECONDS.toHours(elapsedTimeMillis)
    val elapsedTimeInDays = TimeUnit.MILLISECONDS.toDays(elapsedTimeMillis)
    val elapsedTimeInMonths = elapsedTimeInDays / 30
    return when {
        elapsedTimeInSeconds < 60 -> "Now"
        elapsedTimeInMinutes == 1L -> "$elapsedTimeInMinutes Minute ago"
        elapsedTimeInMinutes < 60 -> "$elapsedTimeInMinutes Minutes ago"
        elapsedTimeInHours == 1L -> "$elapsedTimeInHours Hour ago"
        elapsedTimeInHours < 24 -> "$elapsedTimeInHours Hours ago"
        elapsedTimeInDays == 1L -> "$elapsedTimeInDays Day ago"
        elapsedTimeInDays < 30 -> "$elapsedTimeInDays Days ago"
        elapsedTimeInMonths == 1L -> "$elapsedTimeInMonths Month ago"
        elapsedTimeInMonths < 12 -> "$elapsedTimeInMonths Months ago"
        else -> postedTime toTimeOfType DateType.dd_MMM_yyyy_hh_mm_a
    }
}

infix fun Long.toTimeOfType(type: DateType): String {
    val date = Date(this)
    val dateFormat = SimpleDateFormat(type.value, Locale.getDefault())
    return dateFormat.format(date)
}

fun TextView.showHideIcon(
    context: Context,
    showTick: Boolean,
    @DrawableRes leftIcon: Int = android.R.drawable.ic_delete,
    @DrawableRes topIcon: Int = android.R.drawable.ic_delete,
    @DrawableRes rightIcon: Int = android.R.drawable.ic_delete,
    @DrawableRes bottomIcon: Int = android.R.drawable.ic_delete,
    @ColorRes leftIconColor: Int = android.R.color.white,
    @ColorRes topIconColor: Int = android.R.color.white,
    @ColorRes rightIconColor: Int = android.R.color.white,
    @ColorRes bottomIconColor: Int = android.R.color.white,
    direction: Int
) {
    val left = 1
    val top = 2
    val right = 3
    val bottom = 4
    val leftRight = 5
    val topBottom = 6

    val leftDrawable = ContextCompat.getDrawable(context, leftIcon)
        ?.changeColor(context = context, color = leftIconColor)
    val topDrawable = ContextCompat.getDrawable(context, topIcon)
        ?.changeColor(context = context, color = topIconColor)
    val rightDrawable = ContextCompat.getDrawable(context, rightIcon)
        ?.changeColor(context = context, color = rightIconColor)
    val bottomDrawable = ContextCompat.getDrawable(context, bottomIcon)
        ?.changeColor(context = context, color = bottomIconColor)

    if (showTick) {
        when (direction) {
            left -> this.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, null, null)
            top -> this.setCompoundDrawablesWithIntrinsicBounds(null, topDrawable, null, null)
            right -> this.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null)
            bottom -> this.setCompoundDrawablesWithIntrinsicBounds(null, null, null, bottomDrawable)
            leftRight -> this.setCompoundDrawablesWithIntrinsicBounds(
                leftDrawable,
                null,
                rightDrawable,
                null
            )
            topBottom -> this.setCompoundDrawablesWithIntrinsicBounds(
                null,
                topDrawable,
                null,
                bottomDrawable
            )
        }
    } else this.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
}

fun Drawable.changeColor(
    context: Context,
    @ColorRes color: Int
): Drawable {
    val unwrappedDrawable = this
    val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable)
    DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(context, color))
    return this
}

fun deviceWidth() = Resources.getSystem().displayMetrics.widthPixels

fun deviceHeight() = Resources.getSystem().displayMetrics.heightPixels

// https://stackoverflow.com/questions/37104960/bottomsheetdialog-with-transparent-background
fun BottomSheetDialogFragment.setTransparentBackground() {
    dialog?.apply {
        // window?.setDimAmount(0.2f) // Set dim amount here
        setOnShowListener {
            val bottomSheet =
                findViewById<View?>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
        }
    }
}

/** Request focus before showing keyboard - editText.requestFocus() */
fun EditText?.showKeyboard() {
    if (this?.hasFocus() == true) {
        val imm =
            this.context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

/** Request focus before hiding keyboard - editText.requestFocus() */
fun EditText?.hideKeyboard() {
    if (this?.hasFocus() == true) {
        val imm = this.context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(this.windowToken, 0)
    }
}

fun Activity.hideKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    // Find the currently focused view, so we can grab the correct window token from it.
    var view = currentFocus
    // If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

// https://stackoverflow.com/questions/4745988/how-do-i-detect-if-software-keyboard-is-visible-on-android-device-or-not
val View.isKeyboardVisible: Boolean
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        WindowInsetsCompat
            .toWindowInsetsCompat(rootWindowInsets)
            .isVisible(WindowInsetsCompat.Type.ime())
    } else {
        false
    }

// https://stackoverflow.com/questions/37672833/android-m-light-and-dark-status-bar-programmatically-how-to-make-it-dark-again
fun setLightStatusBar(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        var flags = activity.window.decorView.systemUiVisibility // get current flag
        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // add LIGHT_STATUS_BAR to flag
        activity.window.decorView.systemUiVisibility = flags
        activity.window.statusBarColor = Color.GRAY // optional
    }
}

fun clearLightStatusBar(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        var flags = activity.window.decorView.systemUiVisibility // get current flag
        flags =
            flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // use XOR here for remove LIGHT_STATUS_BAR from flags
        activity.window.decorView.systemUiVisibility = flags
        activity.window.statusBarColor = Color.GREEN // optional
    }
}

fun clearLightStatusBar(view: View) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        var flags = view.systemUiVisibility
        flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        view.systemUiVisibility = flags
    }
}

fun Context?.clipboard(): ClipboardManager? =
    this?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

fun EditText.disableCopyPaste() {
    CoroutineScope(IO).launch {
        context.clipboard()?.addPrimaryClipChangedListener {
            context.clipboard()?.text = ""
        }
        isLongClickable = false
        setTextIsSelectable(false)
        customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(p0: ActionMode?, p1: Menu?): Boolean = false
            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean = false
            override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean = false
            override fun onDestroyActionMode(p0: ActionMode?) = Unit
        }
    }
}

fun EditText.setDigits(allowedChars: String) {
    keyListener = DigitsKeyListener.getInstance(allowedChars)
    setRawInputType(InputType.TYPE_CLASS_TEXT)
}

fun AppCompatActivity.showScreen(
    fragment: Fragment,
    tag: String
) {
    supportFragmentManager.beginTransaction()
        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        .add(R.id.fl_container, fragment, tag)
        .addToBackStack(null)
        .commit()
}

enum class DateType(val value: String) {
    dd_MMM_yyyy(value = "dd MMM yyyy"),
    dd_MMM_yyyy_h_mm_a(value = "dd-MMM-yyyy h:mm a"),
    dd_MMM_yyyy_hh_mm_a(value = "dd MMM yyyy, hh:mm a"),
    dd_MMM_yyyy_hh_mm_ss_a(value = "dd MMM yyyy, hh:mm:ss a"),
    dd_MMM_yyyy_h_mm_ss_aaa(value = "dd MMM yyyy, h:mm:ss aaa"),
    yyyy_MM_dd_T_HH_mm_ss_SS_Z(value = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
}