package com.singularitycoder.flowlauncher.helper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.InsetDrawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.provider.Settings
import android.text.Spanned
import android.util.TypedValue
import android.view.*
import android.view.View.MeasureSpec
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.forEach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.reflect.Method
import java.util.*

val callContactSmsPermissionList = arrayOf(
    Manifest.permission.READ_CONTACTS,
    Manifest.permission.READ_SMS,
    Manifest.permission.WRITE_CALL_LOG,
    Manifest.permission.READ_CALL_LOG,
)

fun Context.isCallContactSmsPermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
}

// https://stackoverflow.com/questions/32083410/cant-get-write-settings-permission
fun Context.setRingtone(ringtoneUri: Uri) {
    RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, ringtoneUri);
}

fun getHtmlFormattedTime(html: String): Spanned {
    return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

fun Context.showPermissionSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", this@showPermissionSettings.packageName, null)
    }
    startActivity(intent)
}

fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

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

fun deviceWidth() = Resources.getSystem().displayMetrics.widthPixels

fun deviceHeight() = Resources.getSystem().displayMetrics.heightPixels

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

fun setMarginBtwMenuIconAndText(context: Context, menu: Menu, iconMarginDp: Int) {
    menu.forEach { item: MenuItem ->
        val iconMarginPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, iconMarginDp.toFloat(), context.resources.displayMetrics).toInt()
        if (null != item.icon) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
            } else {
                item.icon = object : InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0) {
                    override fun getIntrinsicWidth(): Int = intrinsicHeight + iconMarginPx + iconMarginPx
                }
            }
        }
    }
}

fun doAfter(duration: Long, task: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed(task, duration)
}

fun Timer.doEvery(
    duration: Long,
    withInitialDelay: Long = 2.seconds(),
    task: suspend () -> Unit
) = scheduleAtFixedRate(
    object : TimerTask() {
        override fun run() {
            CoroutineScope(Dispatchers.IO).launch { task.invoke() }
        }
    },
    withInitialDelay,
    duration
)

// https://www.programmersought.com/article/39074216761/
fun Menu.invokeSetMenuIconMethod() {
    if (this.javaClass.simpleName.equals("MenuBuilder", ignoreCase = true)) {
        try {
            val method: Method = this.javaClass.getDeclaredMethod("setOptionalIconsVisible", java.lang.Boolean.TYPE)
            method.isAccessible = true
            method.invoke(this, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// https://proandroiddev.com/look-deep-into-viewpager2-13eb8e06e419
// https://stackoverflow.com/questions/56114430/android-viewpager2-setpagemargin-unresolved
fun ViewPager2.setShowSideItems(
    pageMarginPx: Int,
    offsetPx: Int
) {
    clipToPadding = false
    clipChildren = false
    offscreenPageLimit = 3
    setPageTransformer { page, position ->
        val offset = position * -(2 * offsetPx + pageMarginPx)
        if (this.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                page.translationX = -offset
            } else {
                page.translationX = offset
            }
        } else {
            page.translationY = offset
        }
    }
}

// https://stackoverflow.com/questions/14200724/listpopupwindow-not-obeying-wrap-content-width-spec
private fun Context.measureContentWidth(listAdapter: ListAdapter): Int {
    var mMeasureParent: ViewGroup? = null
    var maxWidth = 0
    var itemView: View? = null
    var itemType = 0
    val adapter: ListAdapter = listAdapter
    val widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
    val heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
    val count: Int = adapter.count
    for (i in 0 until count) {
        val positionType: Int = adapter.getItemViewType(i)
        if (positionType != itemType) {
            itemType = positionType
            itemView = null
        }
        if (mMeasureParent == null) {
            mMeasureParent = FrameLayout(this)
        }
        itemView = adapter.getView(i, itemView, mMeasureParent)
        itemView.measure(widthMeasureSpec, heightMeasureSpec)
        val itemWidth = itemView.measuredWidth
        if (itemWidth > maxWidth) {
            maxWidth = itemWidth
        }
    }
    return maxWidth
}

// https://stackoverflow.com/questions/3718687/get-number-of-unread-sms
fun Context.unreadSmsCount(): Int {
    return try {
        val cursor = contentResolver.query(
            /* uri = */ Uri.parse("content://sms/inbox"),
            /* projection = */ arrayOf("count(_id)"),
            /* selection = */ "read = 0",
            /* selectionArgs = */ null,
            /* sortOrder = */ null
        )
        cursor?.moveToFirst()
        val unreadMessageCount = cursor?.getInt(0) ?: 0
        cursor?.close()
        unreadMessageCount
    } catch (e: Exception) {
        0
    }
}

// https://stackoverflow.com/questions/7621893/how-to-get-missed-call-sms-count
fun Context.missedCallCount(): Int {
    return try {
        val projection = arrayOf(
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.CACHED_NUMBER_LABEL,
            CallLog.Calls.TYPE
        )
        val where = CallLog.Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE
        val cursor = contentResolver.query(
            /* uri = */ CallLog.Calls.CONTENT_URI.buildUpon()
                .appendQueryParameter(CallLog.Calls.LIMIT_PARAM_KEY, "1")
                .build(),
            /* projection = */ projection,
            /* selection = */ where,
            /* selectionArgs = */ null,
            /* sortOrder = */ null
        )
        cursor?.moveToFirst()
        val missedCallCount = cursor?.count ?: 0
        cursor?.close()
        missedCallCount
    } catch (e: Exception) {
        0
    }
}

fun Activity?.avoidScreenShots() {
    this ?: return
    window?.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
    )
}

fun Activity?.fullScreen() {
    this ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.hide(WindowInsets.Type.statusBars())
    } else {
        @Suppress("DEPRECATION")
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }
}

fun <T> AppCompatActivity.collectLatestLifecycleFlow(flow: Flow<T>, collect: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest(collect)
        }
    }
}

fun Context?.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.getRealUrlFromWebView(
    url: String,
    onRealUrlReady: (url: String) -> Unit,
) {
    WebView(this).apply {
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, realUrl: String) {
                println("real url: $realUrl")
                onRealUrlReady.invoke(realUrl)
            }
        }
        loadUrl(url)
    }
}