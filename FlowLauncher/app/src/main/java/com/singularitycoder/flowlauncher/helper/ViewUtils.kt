package com.singularitycoder.flowlauncher.helper

import android.app.Activity
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.text.*
import android.text.style.BackgroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.AnimRes
import androidx.annotation.AnyRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.singularitycoder.flowlauncher.MainActivity
import com.singularitycoder.flowlauncher.R


fun Context.getThemeAttrColor(attributeColor: Int): Int {
    this.theme.resolveAttribute(attributeColor, FlowUtils.typedValue, true)
    return FlowUtils.typedValue.data
}

// underline text programatically
fun TextView.strike() {
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

// https://stackoverflow.com/questions/2004344/how-do-i-handle-imeoptions-done-button-click
fun EditText.onImeClick(
    imeAction: Int = EditorInfo.IME_ACTION_DONE,
    callback: () -> Unit
) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == imeAction) {
            callback.invoke()
            return@setOnEditorActionListener true
        }
        false
    }
}

// https://stackoverflow.com/questions/6115715/how-do-i-programmatically-set-the-background-color-gradient-on-a-custom-title-ba
// https://stackoverflow.com/questions/17823451/set-android-shape-color-programmatically
// https://stackoverflow.com/questions/28578701/how-to-create-android-shape-background-programmatically
fun gradientDrawable(): GradientDrawable {
    return GradientDrawable().apply {
        colors = intArrayOf(
            R.color.purple_500,
            R.color.purple_50,
        )
        orientation = GradientDrawable.Orientation.RIGHT_LEFT
        gradientType = GradientDrawable.SWEEP_GRADIENT
        shape = GradientDrawable.RECTANGLE
    }
}

// https://stackoverflow.com/questions/22192291/how-to-change-the-status-bar-color-in-android
fun Activity.setStatusBarColor(@ColorRes color: Int) {
    window.statusBarColor = ContextCompat.getColor(this, color)
}

// https://stackoverflow.com/questions/27839105/android-lollipop-change-navigation-bar-color
fun Activity.setNavigationBarColor(@ColorRes color: Int) {
    window.navigationBarColor = ContextCompat.getColor(this, color)
}

fun MainActivity.showScreen(
    fragment: Fragment,
    tag: String,
    isAdd: Boolean = false
) {
    if (isAdd) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_to_left, R.anim.slide_to_right, R.anim.slide_to_left, R.anim.slide_to_right)
            .add(R.id.fl_container, fragment, tag)
            .addToBackStack(null)
            .commit()
    } else {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_container, fragment, tag)
            .commit()
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

// https://stackoverflow.com/questions/37104960/bottomsheetdialog-with-transparent-background
fun BottomSheetDialogFragment.setTransparentBackground() {
    dialog?.apply {
        // window?.setDimAmount(0.2f) // Set dim amount here
        setOnShowListener {
            val bottomSheet = findViewById<View?>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
        }
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

    val leftDrawable = ContextCompat.getDrawable(context, leftIcon)?.changeColor(context = context, color = leftIconColor)
    val topDrawable = ContextCompat.getDrawable(context, topIcon)?.changeColor(context = context, color = topIconColor)
    val rightDrawable = ContextCompat.getDrawable(context, rightIcon)?.changeColor(context = context, color = rightIconColor)
    val bottomDrawable = ContextCompat.getDrawable(context, bottomIcon)?.changeColor(context = context, color = bottomIconColor)

    if (showTick) {
        when (direction) {
            left -> this.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, null, null)
            top -> this.setCompoundDrawablesWithIntrinsicBounds(null, topDrawable, null, null)
            right -> this.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null)
            bottom -> this.setCompoundDrawablesWithIntrinsicBounds(null, null, null, bottomDrawable)
            leftRight -> this.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, rightDrawable, null)
            topBottom -> this.setCompoundDrawablesWithIntrinsicBounds(null, topDrawable, null, bottomDrawable)
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

fun Context.color(@ColorRes colorRes: Int) = ContextCompat.getColor(this, colorRes)

fun Context.drawable(@DrawableRes drawableRes: Int): Drawable? = ContextCompat.getDrawable(this, drawableRes)

fun Context.showAlertDialog(
    title: String = "",
    message: String,
    positiveBtnText: String,
    negativeBtnText: String = "",
    neutralBtnText: String = "",
    icon: Drawable? = null,
    @ColorRes positiveBtnColor: Int? = null,
    @ColorRes negativeBtnColor: Int? = null,
    @ColorRes neutralBtnColor: Int? = null,
    positiveAction: () -> Unit = {},
    negativeAction: () -> Unit = {},
    neutralAction: () -> Unit = {},
) {
    MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog).apply {
        setCancelable(false)
        if (title.isNotBlank()) setTitle(title)
        setMessage(message)
        background = drawable(R.drawable.alert_dialog_bg)
        if (icon != null) setIcon(icon)
        setPositiveButton(positiveBtnText) { dialog, int ->
            positiveAction.invoke()
        }
        if (negativeBtnText.isNotBlank()) {
            setNegativeButton(negativeBtnText) { dialog, int ->
                negativeAction.invoke()
            }
        }
        if (neutralBtnText.isNotBlank()) {
            setNeutralButton(neutralBtnText) { dialog, int ->
                neutralAction.invoke()
            }
        }
        val dialog = create()
        dialog.show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).apply {
            setHapticFeedback()
            isAllCaps = false
            setPadding(0, 0, 16.dpToPx().toInt(), 0)
            if (positiveBtnColor != null) setTextColor(this@showAlertDialog.color(positiveBtnColor))
        }
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).apply {
            setHapticFeedback()
            isAllCaps = false
            if (negativeBtnColor != null) setTextColor(this@showAlertDialog.color(negativeBtnColor))
        }
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).apply {
            setHapticFeedback()
            isAllCaps = false
            setPadding(16.dpToPx().toInt(), 0, 0, 0)
            if (neutralBtnColor != null) setTextColor(this@showAlertDialog.color(neutralBtnColor))
        }
    }
}

fun Context.showPopupMenu(
    view: View?,
    title: String? = null,
    menuList: List<String?>,
    onItemClick: (position: Int) -> Unit
) {
    val popupMenu = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        PopupMenu(
            /* context = */ this,
            /* anchor = */ view,
            /* gravity = */ 0,
            /* popupStyleAttr = */ 0,
            /* popupStyleRes = */ R.style.PopupMenuTheme
        )
    } else {
        PopupMenu(
            /* context = */ this,
            /* anchor = */ view
        )
    }
    popupMenu.apply {
        if (title != null) {
            menu.add(Menu.NONE, -1, 0, title).apply {
                isEnabled = false
            }
        }
        menuList.forEach {
            menu.add(it)
        }
        setOnMenuItemClickListener { it: MenuItem? ->
            view?.setHapticFeedback()
            onItemClick.invoke(menuList.indexOf(it?.title))
            false
        }
        show()
    }
}

// TODO set bottom n top margins - popupStyleAttr or popupStyleRes for both PopupMenu n ListPopupWindow. Default attributes like R.attr.listPopupWindowStyle seem to have their own background which is overriding mine
// popupStyleAttr = R.attr.popupMenuStyle
// popupStyleAttr = com.google.android.material.R.attr.popupMenuStyle
fun Context.showPopupMenuWithIcons(
    view: View?,
    title: String? = null,
    customColorItemText: String = "",
    @ColorRes customColor: Int = 0,
    menuList: List<Pair<String, Int>>,
    iconWidth: Int = -1,
    iconHeight: Int = -1,
    defaultSpaceBtwIconTitle: String = "    ",
    isColoredIcon: Boolean = true,
    colorsList: List<Int> = emptyList(),
    onItemClick: (menuItem: MenuItem?) -> Unit
) {
    val popupMenu = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        PopupMenu(
            /* context = */ this,
            /* anchor = */ view,
            /* gravity = */ 0,
            /* popupStyleAttr = */ 0,
            /* popupStyleRes = */ R.style.PopupMenuTheme
        )
    } else {
        PopupMenu(
            /* context = */ this,
            /* anchor = */ view
        )
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        popupMenu.menu.setGroupDividerEnabled(true)
    }
    if (title != null) {
        popupMenu.menu.add(Menu.NONE, -1, 0, title).apply {
            isEnabled = false
        }
    }
    val groupId = if (menuList.last().first.contains(other = "delete", ignoreCase = true)) {
        menuList.lastIndex
    } else 0
    menuList.forEachIndexed { index, pair ->
        val icon = if (colorsList.isNotEmpty()) {
            drawable(pair.second)?.changeColor(this, colorsList[index])
        } else {
            if (pair.first == customColorItemText) {
                drawable(pair.second)?.changeColor(this, customColor)
            } else {
                drawable(pair.second)?.apply {
                    if (isColoredIcon) changeColor(this@showPopupMenuWithIcons, R.color.purple_500)
                }
            }
        }
        val insetDrawable = InsetDrawable(
            /* drawable = */ icon,
            /* insetLeft = */ 0,
            /* insetTop = */ 0,
            /* insetRight = */ 0,
            /* insetBottom = */ 0
        )
        popupMenu.menu.add(
            /* groupId */ /* if (index == groupId) groupId else 0 */ 0,
            /* itemId */ 1,
            /* order */ 1,
            /* title */ menuIconWithText(
                icon = insetDrawable,
                title = pair.first,
                iconWidth = iconWidth,
                iconHeight = iconHeight,
                defaultSpace = defaultSpaceBtwIconTitle
            )
        )
//        popupMenu.menu.get(index).actionView?.setMargins(start = 0, top = 0, end = 0, bottom = 8.dpToPx().toInt())
//        findViewById<ViewGroup>(popupMenu.menu.get(index).itemId).get(index)
    }
    popupMenu.setOnMenuItemClickListener { it: MenuItem? ->
        view?.setHapticFeedback()
        onItemClick.invoke(it)
        false
    }
    popupMenu.show()
}

fun Context.showSingleSelectionPopupMenu(
    view: View?,
    title: String? = null,
    selectedOption: String? = null,
    @ColorRes enabledColor: Int = R.color.purple_500,
    @ColorRes disabledColor: Int = android.R.color.transparent,
    menuList: List<Pair<String, Int>>,
    onItemClick: (menuItem: MenuItem?) -> Unit
) {
    val popupMenu = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        PopupMenu(
            /* context = */ this,
            /* anchor = */ view,
            /* gravity = */ 0,
            /* popupStyleAttr = */ 0,
            /* popupStyleRes = */ R.style.PopupMenuTheme
        )
    } else {
        PopupMenu(
            /* context = */ this,
            /* anchor = */ view
        )
    }
    popupMenu.menu.add(Menu.NONE, -1, 0, title).apply {
        isEnabled = false
    }
    menuList.forEach { pair: Pair<String, Int> ->
        popupMenu.menu.add(
            0, 1, 1, menuIconWithText(
                icon = this.drawable(pair.second)?.changeColor(
                    context = this,
                    color = if (selectedOption == pair.first) enabledColor else disabledColor
                ),
                title = pair.first
            )
        )
    }
    popupMenu.setOnMenuItemClickListener { menuItem: MenuItem? ->
        view?.setHapticFeedback()
        onItemClick.invoke(menuItem)
        false
    }
    popupMenu.show()
}

// https://stackoverflow.com/questions/32969172/how-to-display-menu-item-with-icon-and-text-in-appcompatactivity
// https://developer.android.com/develop/ui/views/text-and-emoji/spans
fun menuIconWithText(
    icon: Drawable?,
    title: String,
    iconWidth: Int = -1,
    iconHeight: Int = -1,
    defaultSpace: String = "    "
): CharSequence {
    icon?.setBounds(
        /* left = */ 0,
        /* top = */ 0,
        /* right = */ if (iconWidth == -1) icon.intrinsicWidth else iconWidth,
        /* bottom = */ if (iconHeight == -1) icon.intrinsicHeight else iconHeight
    )
    icon ?: return title
    val imageSpan = ImageSpan(icon, ImageSpan.ALIGN_BOTTOM)
    return SpannableString("$defaultSpace$title").apply {
        setSpan(
            /* what = */ imageSpan,
            /* startCharPos = */ 0,
            /* endCharPos = */ 1,
            /* flags = */ Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}

// https://stackoverflow.com/questions/28578701/how-to-create-android-shape-background-programmatically
fun Context.createCustomView(backgroundColor: Int, borderColor: Int): View {
    val shape = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadii = floatArrayOf(8f, 8f, 8f, 8f, 0f, 0f, 0f, 0f)
        setColor(backgroundColor)
        setStroke(3, borderColor)
    }
    return View(this).apply {
        background = shape
    }
}

// https://stackoverflow.com/questions/5776684/how-can-i-convert-a-view-to-a-drawable
fun createGradientDrawable(width: Int, height: Int): GradientDrawable {
    return GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(Color.TRANSPARENT)
        setSize(width, height)
    }
}

fun Context.layoutAnimationController(@AnimRes animationRes: Int): LayoutAnimationController {
    return AnimationUtils.loadLayoutAnimation(this, animationRes)
}

// To reanimate list if necessary
fun RecyclerView.runLayoutAnimation(@AnimRes animationRes: Int) {
    layoutAnimation = context.layoutAnimationController(animationRes)
    scheduleLayoutAnimation()
}

inline fun <reified T> List<T>.toArrayList(): ArrayList<T> = ArrayList(this)

fun Context.resourceUri(@AnyRes resourceId: Int): Uri = Uri.Builder()
    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
    .authority(packageName)
    .path(resourceId.toString())
    .build()

fun Context.getResourceUri(@AnyRes resourceId: Int): Uri {
    return Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://$packageName/$resourceId")
}

fun Context.clearNotification(notificationId: Int) {
    (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.cancel(packageName, notificationId)
}

// https://github.com/LineageOS/android_packages_apps_Jelly
// https://c1ctech.com/android-highlight-a-word-in-texttospeech/
// https://medium.com/androiddevelopers/spantastic-text-styling-with-spans-17b0c16b4568
// setSpan(ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
// setSpan(QuoteSpan(itemBinding.root.context.color(R.color.purple_500)), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
// setSpan(RelativeSizeSpan(1.5f), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
fun TextView?.highlightText(
    query: String,
    result: String,
    spanList: List<ParcelableSpan> = listOf(StyleSpan(Typeface.BOLD), BackgroundColorSpan(Color.YELLOW))
): TextView? {
    if (query.isBlank() || result.isBlank()) return this
    val spannable = SpannableStringBuilder(result)
    var queryTextPos = result.toLowCase().indexOf(string = query)
    while (queryTextPos >= 0) {
        spanList.forEach { span: ParcelableSpan ->
            spannable.setSpan(
                /* what = */ span,
                /* start = */ queryTextPos,
                /* end = */ queryTextPos + query.length,
                /* flags = */ Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        queryTextPos = result.toLowCase().indexOf(string = query, startIndex = queryTextPos + query.length)
    }
    this?.text = spannable
    return this
}

fun getBitmapOf(width: Int = 1, height: Int = 1): Bitmap {
    val conf = Bitmap.Config.ARGB_8888 // see other conf types
    val bitmap = Bitmap.createBitmap(width, height, conf) // this creates a MUTABLE bitmap
    val canvas = Canvas(bitmap)
    return bitmap
}

// https://stackoverflow.com/questions/2228151/how-to-enable-haptic-feedback-on-button-view
fun View.setHapticFeedback() {
    isHapticFeedbackEnabled = true
    performHapticFeedback(
        HapticFeedbackConstants.VIRTUAL_KEY,
        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING  // Ignore device's setting. Otherwise, you can use FLAG_IGNORE_VIEW_SETTING to ignore view's setting.
    )
}

// https://stackoverflow.com/questions/5608720/android-preventing-double-click-on-a-button
fun View.onSafeClick(
    delayAfterClick: Long = 100.milliSeconds(),
    onSafeClick: (Pair<View?, Boolean>) -> Unit
) {
    val onSafeClickListener = OnSafeClickListener(delayAfterClick, onSafeClick)
    setOnClickListener(onSafeClickListener)
}

fun View.onCustomLongClick(
    onCustomLongClick: (view: View?) -> Unit
) {
    val onCustomLongClickListener = OnCustomLongClickListener(onCustomLongClick)
    setOnLongClickListener(onCustomLongClickListener)
}

class OnSafeClickListener(
    private val delayAfterClick: Long,
    private val onSafeClick: (Pair<View?, Boolean>) -> Unit
) : View.OnClickListener {
    private var lastClickTime = 0L
    private var isClicked = false

    override fun onClick(v: View?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            v?.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
        val elapsedRealtime = SystemClock.elapsedRealtime()
        if (elapsedRealtime - lastClickTime < delayAfterClick) return
        lastClickTime = elapsedRealtime
//        v?.startAnimation(AlphaAnimation(1F, 0.8F))
//        v?.setTouchEffect()
        isClicked = !isClicked
        onSafeClick(v to isClicked)
//        v?.setHapticFeedback()
    }
}

class OnCustomLongClickListener(
    private val onCustomClick: (view: View?) -> Unit
) : View.OnLongClickListener {
    override fun onLongClick(v: View?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            v?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
        onCustomClick.invoke(v)
        return false
    }
}