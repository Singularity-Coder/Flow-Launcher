package com.singularitycoder.flowlauncher.helper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.provider.Settings
import android.text.Spanned
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.singularitycoder.flowlauncher.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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


fun getHtmlFormattedTime(html: String): Spanned {
    return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

fun Context.showPermissionSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", this@showPermissionSettings.packageName, null)
    }
    startActivity(intent)
}

fun Context.showAlertDialog(
    title: String,
    message: String,
    positiveAction: () -> Unit = {},
    negativeAction: () -> Unit = {},
) {
    MaterialAlertDialogBuilder(
        this,
        com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog
    ).apply {
        setCancelable(false)
        setTitle(title)
        setMessage(message)
        background = drawable(R.drawable.alert_dialog_bg)
        setPositiveButton("Ok") { dialog, int ->
            positiveAction.invoke()
        }
        setNegativeButton("Cancel") { dialog, int ->
            negativeAction.invoke()
        }
        val dialog = create()
        dialog.show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).isAllCaps = false
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).isAllCaps = false
    }
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

// Create custom list item to get correct width
// Refer for a fix - https://stackoverflow.com/questions/14200724/listpopupwindow-not-obeying-wrap-content-width-spec
fun Context.showListPopupMenu(
    anchorView: View,
    adapter: ArrayAdapter<String>,
    onItemClick: (position: Int) -> Unit
) {
//    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, todayOptions)
    ListPopupWindow(this, null, com.google.android.material.R.attr.listPopupWindowStyle).apply {
        this.anchorView = anchorView
        setAdapter(adapter)
//        setContentWidth(ListPopupWindow.WRAP_CONTENT)
//        setContentWidth(measureContentWidth(adapter))
        width = ListPopupWindow.MATCH_PARENT
        setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            onItemClick.invoke(position)
            this.dismiss()
        }
        show()
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

fun Context.showPopup(
    view: View,
    menuList: List<String>,
    onItemClick: (position: Int) -> Unit
) {
    PopupMenu(this, view).apply {
        menuList.forEach {
            menu.add(it)
        }
        setOnMenuItemClickListener { it: MenuItem? ->
            onItemClick.invoke(menuList.indexOf(it?.title))
            false
        }
        show()
    }
}

// https://stackoverflow.com/questions/6115715/how-do-i-programmatically-set-the-background-color-gradient-on-a-custom-title-ba
fun getGradientDrawable(): GradientDrawable {
    return GradientDrawable().apply {
        colors = intArrayOf(
            R.color.purple_500,
            R.color.purple_50,
        )
        orientation = GradientDrawable.Orientation.LEFT_RIGHT
        gradientType = GradientDrawable.LINEAR_GRADIENT
        shape = GradientDrawable.RECTANGLE
    }
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

object FlowUtils {
    val gson = Gson()
}