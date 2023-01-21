package com.singularitycoder.flowlauncher.helper.quickactionview

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.*
import android.view.View.OnLongClickListener
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.helper.quickactionview.animator.FadeInFadeOutActionsTitleAnimator
import com.singularitycoder.flowlauncher.helper.quickactionview.animator.SlideFromCenterAnimator

/**
 * A QuickActionView, which shows actions when a view is long pressed.
 *
 * @see [https://github.com/ovenbits/QuickActionView](https://github.com/ovenbits/QuickActionView)
 */
open class QuickActionView private constructor(private val mContext: Context) {
    private var isShown = false
    private var mOnActionSelectedListener: OnActionSelectedListener? = null
    private var mOnDismissListener: OnDismissListener? = null
    private var mOnShowListener: OnShowListener? = null
    private var mOnActionHoverChangedListener: OnActionHoverChangedListener? = null
    private val mActionDistance: Float
    private val mActionPadding: Int
    private val actionsList: ArrayList<Action> = ArrayList()

    /**
     * Get the extras associated with the QuickActionView. Allows for
     * saving state to the QuickActionView
     *
     * @return the bundle for the QuickActionView
     */
    var extras: Bundle? = null
        private set
    private var mQuickActionViewLayout: QuickActionViewLayout? = null
    private val mConfig: Config = Config(mContext)
    private var mActionsInAnimator: ActionsInAnimator
    private var mActionsOutAnimator: ActionsOutAnimator
    private var mActionsTitleInAnimator: ActionsTitleInAnimator
    private var mActionsTitleOutAnimator: ActionsTitleOutAnimator

    @ColorInt
    private var mScrimColor = Color.parseColor("#99000000")
    private var mIndicatorDrawable: Drawable?
    private val mRegisteredListeners = HashMap<View, RegisteredListener>()

    /**
     * Retrieve the view that has been long pressed
     *
     * @return the registered view that was long pressed to show the QuickActionView
     */
    var longPressedView: View? = null
        private set

    init {
        mIndicatorDrawable = ContextCompat.getDrawable(mContext, R.drawable.qav_indicator)
        mActionDistance = mContext.resources.getDimensionPixelSize(R.dimen.qav_action_distance).toFloat()
        mActionPadding = mContext.resources.getDimensionPixelSize(R.dimen.qav_action_padding)
        val defaultAnimator = SlideFromCenterAnimator(true)
        val defaultTitleAnimator = FadeInFadeOutActionsTitleAnimator()
        mActionsInAnimator = defaultAnimator
        mActionsOutAnimator = defaultAnimator
        mActionsTitleInAnimator = defaultTitleAnimator
        mActionsTitleOutAnimator = defaultTitleAnimator
    }

    private fun show(anchor: View, offset: Point) {
        if (isShown) {
            throw RuntimeException("Show cannot be called when the QuickActionView is already visible")
        }
        isShown = true
        val parent = anchor.parent
        if (parent is View) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
        longPressedView = anchor
        val loc = IntArray(2)
        anchor.getLocationInWindow(loc)
        val point = Point(offset)
        point.offset(loc[0], loc[1])
        display(point)
    }

    /**
     * Register the QuickActionView to appear when the passed view is long pressed
     *
     * @param view the view to have long press responses
     * @return the QuickActionView
     */
    fun register(view: View): QuickActionView {
        val listener: RegisteredListener = RegisteredListener()
        mRegisteredListeners[view] = listener
        view.setOnTouchListener(listener)
        view.setOnLongClickListener(listener)
        return this
    }

    /**
     * Unregister the view so that it can no longer be long pressed to show the QuickActionView
     *
     * @param view the view to unregister
     */
    fun unregister(view: View) {
        mRegisteredListeners.remove(view)
        view.setOnTouchListener(null)
        view.setOnLongClickListener(null)
    }

    /**
     * Adds an action to the QuickActionView
     *
     * @param action the action to add
     * @return the QuickActionView
     */
    fun addAction(action: Action): QuickActionView {
        checkShown()
        actionsList.add(action)
        return this
    }

    /**
     * Adds a collection of actions to the QuickActionView
     *
     * @param actions the actions to add
     * @return the QuickActionView
     */
    fun addActions(actions: Collection<Action>?): QuickActionView {
        checkShown()
        actionsList.addAll(actions!!)
        return this
    }

    /**
     * Add actions to the QuickActionView from the given menu resource id.
     *
     * @param menuId menu resource id
     * @return the QuickActionView
     */
    fun addActions(@MenuRes menuId: Int): QuickActionView {
        @SuppressLint("RestrictedApi") val menu: Menu = MenuBuilder(mContext)
        MenuInflater(mContext).inflate(menuId, menu)
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val action = Action(item.itemId, item.icon!!, item.title!!)
            addAction(action)
        }
        return this
    }

    /**
     * Removes all actions from the QuickActionView
     *
     * @return the QuickActionView
     */
    fun removeActions(): QuickActionView {
        actionsList.clear()
        return this
    }

    /**
     * Remove an individual action from the QuickActionView
     *
     * @param actionId the action id
     * @return the QuickActionView
     */
    fun removeAction(actionId: Int): QuickActionView {
        for (i in actionsList.indices) {
            if (actionsList[i].id == actionId) {
                actionsList.removeAt(i)
                return this
            }
        }
        throw IllegalArgumentException("No action exists for actionId$actionId")
    }

    fun setOnActionSelectedListener(onActionSelectedListener: OnActionSelectedListener?): QuickActionView {
        mOnActionSelectedListener = onActionSelectedListener
        return this
    }

    fun setOnDismissListener(onDismissListener: OnDismissListener?): QuickActionView {
        mOnDismissListener = onDismissListener
        return this
    }

    fun setOnShowListener(onShowListener: OnShowListener?): QuickActionView {
        mOnShowListener = onShowListener
        return this
    }

    fun setOnActionHoverChangedListener(listener: OnActionHoverChangedListener?): QuickActionView {
        mOnActionHoverChangedListener = listener
        return this
    }

    /**
     * Set the indicator drawable (the drawable that appears at the point the user has long pressed
     *
     * @param indicatorDrawable the indicator drawable
     * @return the QuickActionView
     */
    fun setIndicatorDrawable(indicatorDrawable: Drawable?): QuickActionView {
        mIndicatorDrawable = indicatorDrawable
        return this
    }

    /**
     * Set the scrim color (the background behind the QuickActionView)
     *
     * @param scrimColor the desired scrim color
     * @return the QuickActionView
     */
    fun setScrimColor(@ColorInt scrimColor: Int): QuickActionView {
        mScrimColor = scrimColor
        return this
    }

    /**
     * Set the drawable that appears behind the Action text labels
     *
     * @param textBackgroundDrawable the desired drawable
     * @return the QuickActionView
     */
    fun setTextBackgroundDrawable(@DrawableRes textBackgroundDrawable: Int): QuickActionView {
        mConfig.setTextBackgroundDrawable(textBackgroundDrawable)
        return this
    }

    /**
     * Set the background color state list for all action items
     *
     * @param backgroundColorStateList the desired colorstatelist
     * @return the QuickActionView
     */
    fun setBackgroundColorStateList(backgroundColorStateList: ColorStateList?): QuickActionView {
        mConfig.backgroundColorStateList = backgroundColorStateList
        return this
    }

    /**
     * Set the text color for the Action labels
     *
     * @param textColor the desired text color
     * @return the QuickActionView
     */
    fun setTextColor(@ColorInt textColor: Int): QuickActionView {
        mConfig.textColor = textColor
        return this
    }

    /**
     * Set the action's background color. If you want to have a pressed state,
     * see [.setBackgroundColorStateList]
     *
     * @param backgroundColor the desired background color
     * @return the QuickActionView
     */
    fun setBackgroundColor(@ColorInt backgroundColor: Int): QuickActionView {
        mConfig.setBackgroundColor(backgroundColor)
        return this
    }

    /**
     * Set the typeface for the Action labels
     *
     * @param typeface the desired typeface
     * @return the QuickActionView
     */
    fun setTypeface(typeface: Typeface): QuickActionView {
        mConfig.typeface = typeface
        return this
    }

    /**
     * Set the text size for the Action labels
     *
     * @param textSize the desired textSize (in pixels)
     * @return the QuickActionView
     */
    fun setTextSize(textSize: Int): QuickActionView {
        mConfig.textSize = textSize
        return this
    }

    /**
     * Set the text top padding for the Action labels
     *
     * @param textPaddingTop the top padding in pixels
     * @return the QuickActionView
     */
    fun setTextPaddingTop(textPaddingTop: Int): QuickActionView {
        mConfig.textPaddingTop = textPaddingTop
        return this
    }

    /**
     * Set the text bottom padding for the Action labels
     *
     * @param textPaddingBottom the top padding in pixels
     * @return the QuickActionView
     */
    fun setTextPaddingBottom(textPaddingBottom: Int): QuickActionView {
        mConfig.textPaddingBottom = textPaddingBottom
        return this
    }

    /**
     * Set the text left padding for the Action labels
     *
     * @param textPaddingLeft the top padding in pixels
     * @return the QuickActionView
     */
    fun setTextPaddingLeft(textPaddingLeft: Int): QuickActionView {
        mConfig.textPaddingLeft = textPaddingLeft
        return this
    }

    /**
     * Set the text right padding for the Action labels
     *
     * @param textPaddingRight the top padding in pixels
     * @return the QuickActionView
     */
    fun setTextPaddingRight(textPaddingRight: Int): QuickActionView {
        mConfig.textPaddingRight = textPaddingRight
        return this
    }

    /**
     * Override the animations for when the QuickActionView shows
     *
     * @param actionsInAnimator the animation overrides
     * @return this QuickActionView
     */
    fun setActionsInAnimator(actionsInAnimator: ActionsInAnimator): QuickActionView {
        mActionsInAnimator = actionsInAnimator
        return this
    }

    /**
     * Override the animations for when the QuickActionView dismisses
     *
     * @param actionsOutAnimator the animation overrides
     * @return this QuickActionView
     */
    fun setActionsOutAnimator(actionsOutAnimator: ActionsOutAnimator): QuickActionView {
        mActionsOutAnimator = actionsOutAnimator
        return this
    }

    /**
     * Override the animations for when the QuickActionView action title shows
     *
     * @param actionsTitleInAnimator the custom animator
     * @return this QuickActionView
     */
    fun setActionsTitleInAnimator(actionsTitleInAnimator: ActionsTitleInAnimator): QuickActionView {
        mActionsTitleInAnimator = actionsTitleInAnimator
        return this
    }

    /**
     * Override the animations for when the QuickActionView dismisses
     *
     * @param actionsTitleOutAnimator the custom animator
     * @return this QuickActionView
     */
    fun setActionsTitleOutAnimator(actionsTitleOutAnimator: ActionsTitleOutAnimator): QuickActionView {
        mActionsTitleOutAnimator = actionsTitleOutAnimator
        return this
    }

    /**
     * Set a custom configuration for the action with the given id
     *
     * @param config   the configuration to attach
     * @param actionId the action id
     * @return this QuickActionView
     */
    fun setActionConfig(config: Action.Config?, @IdRes actionId: Int): QuickActionView {
        for (action in actionsList) {
            if (action.id == actionId) {
                action.config = config!!
                return this
            }
        }
        throw IllegalArgumentException("No Action exists with id $actionId")
    }

    /**
     * Get the center point of the [QuickActionView] aka the point at which the actions will eminate from
     *
     * @return the center point, or null if the view has not yet been created
     */
    val centerPoint: Point?
        get() = if (mQuickActionViewLayout != null) {
            mQuickActionViewLayout!!.mCenterPoint
        } else null

    private fun display(point: Point) {
//        checkNotNull(actionsList) { "You need to give the QuickActionView actions before calling show!" }
        val manager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL)
        params.format = PixelFormat.TRANSLUCENT
        mQuickActionViewLayout = QuickActionViewLayout(mContext, actionsList, point)
        manager.addView(mQuickActionViewLayout, params)
        if (mOnShowListener != null) {
            mOnShowListener!!.onShow(this)
        }
    }

    private fun animateHide() {
        val duration = mQuickActionViewLayout!!.animateOut()
        Handler(Looper.getMainLooper()).postDelayed({ removeView() }, duration.toLong())
    }

    private fun removeView() {
        if (mQuickActionViewLayout != null) {
            val manager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (checkAttachedToWindow(mQuickActionViewLayout!!)) {
                manager.removeView(mQuickActionViewLayout)
            }
            mQuickActionViewLayout = null
            isShown = false
        }
        if (longPressedView != null) {
            val parent = longPressedView!!.parent
            if (parent is View) {
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
    }

    private fun checkAttachedToWindow(view: View): Boolean = view.isAttachedToWindow

    private fun dismiss() {
        if (!isShown) {
            throw RuntimeException("The QuickActionView must be visible to call dismiss()")
        }
        if (mOnDismissListener != null) {
            mOnDismissListener!!.onDismiss(this@QuickActionView)
        }
        animateHide()
    }

    private fun checkShown() {
        if (isShown) {
            throw RuntimeException("QuickActionView cannot be configured if show has already been called.")
        }
    }

    /**
     * Set extras to associate with the QuickActionView to allow saving state
     *
     * @param extras the bundle
     * @return the QuickActionView
     */
    fun setExtras(extras: Bundle?): QuickActionView {
        this.extras = extras
        return this
    }

    /**
     * Listener for when an action is selected (hovered, then released)
     */
    fun interface OnActionSelectedListener {
        fun onActionSelected(action: Action?, quickActionView: QuickActionView?)
    }

    /**
     * Listener for when an action has its hover state changed (hovering or stopped hovering)
     */
    fun interface OnActionHoverChangedListener {
        fun onActionHoverChanged(action: Action?, quickActionView: QuickActionView?, hovering: Boolean)
    }

    /**
     * Listen for when the QuickActionView is dismissed
     */
    fun interface OnDismissListener {
        fun onDismiss(quickActionView: QuickActionView?)
    }

    /**
     * Listener for when the QuickActionView is shown
     */
    fun interface OnShowListener {
        fun onShow(quickActionView: QuickActionView?)
    }

    class Config private constructor(context: Context, var typeface: Typeface, var textSize: Int, var textPaddingTop: Int) {
        private val mDefaultConfig: Action.Config
        var textPaddingBottom: Int
        var textPaddingLeft: Int
        var textPaddingRight: Int

        constructor(context: Context) : this(context,
                Typeface.DEFAULT,
                context.resources.getInteger(R.integer.qav_action_title_view_text_size),
                context.resources.getDimensionPixelSize(R.dimen.qav_action_title_view_text_padding)) {
        }

        init {
            textPaddingBottom = textPaddingTop
            textPaddingLeft = textPaddingTop
            textPaddingRight = textPaddingTop
            mDefaultConfig = Action.Config(context)
        }

        fun getTextBackgroundDrawable(context: Context?): Drawable? {
            return mDefaultConfig.getTextBackgroundDrawable(context)
        }

        fun setTextBackgroundDrawable(@DrawableRes textBackgroundDrawable: Int) {
            mDefaultConfig.setTextBackgroundDrawable(textBackgroundDrawable)
        }

        var textColor: Int
            get() = mDefaultConfig.textColor
            set(textColor) {
                mDefaultConfig.setTextColor(textColor)
            }
        var backgroundColorStateList: ColorStateList?
            get() = mDefaultConfig.backgroundColorStateList
            set(backgroundColorStateList) {
                mDefaultConfig.setBackgroundColorStateList(backgroundColorStateList)
            }

        fun setBackgroundColor(@ColorInt backgroundColor: Int) {
            mDefaultConfig.setBackgroundColor(backgroundColor)
        }
    }

    /**
     * Parent layout that actually houses all of the quick action views
     */
    private inner class QuickActionViewLayout(context: Context?, actionsList: ArrayList<Action>, val mCenterPoint: Point) : FrameLayout(context!!) {
        private val mIndicatorView: View
        private val mScrimView: View
        private val mActionViews = LinkedHashMap<Action, ActionView>()
        private val mActionTitleViews = LinkedHashMap<Action, ActionTitleView>()
        private val mLastTouch = PointF()
        private var mAnimated = false

        init {
            mScrimView = View(context)
            mScrimView.setBackgroundColor(mScrimColor)
            val scrimParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            addView(mScrimView, scrimParams)
            mIndicatorView = View(context)
            mIndicatorView.background = mIndicatorDrawable
            val indicatorParams = LayoutParams(mIndicatorDrawable!!.intrinsicWidth, mIndicatorDrawable!!.intrinsicHeight)
            addView(mIndicatorView, indicatorParams)
            for (action in actionsList) {
                val helper = ConfigHelper(action.config, mConfig)
                val actionView = ActionView(context, action, helper)
                mActionViews[action] = actionView
                val params = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                addView(actionView, params)
                if (!TextUtils.isEmpty(action.title)) {
                    val actionTitleView = ActionTitleView(context, action, helper)
                    actionTitleView.visibility = GONE
                    mActionTitleViews[action] = actionTitleView
                    val titleParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    addView(actionTitleView, titleParams)
                }
            }
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            mScrimView.layout(0, 0, measuredWidth, measuredHeight)
            mIndicatorView.layout(mCenterPoint.x - (mIndicatorView.measuredWidth / 2.0).toInt(),
                    mCenterPoint.y - (mIndicatorView.measuredHeight / 2.0).toInt(),
                    mCenterPoint.x + (mIndicatorView.measuredWidth / 2.0).toInt(),
                    mCenterPoint.y + (mIndicatorView.measuredHeight / 2.0).toInt())
            var index = 0
            for ((key, actionView) in mActionViews) {
                val startAngle = getOptimalStartAngle(actionView.actionCircleRadiusExpanded)
                val point = getActionPoint(index, startAngle, actionView)
                point.offset(-actionView.circleCenterX, -actionView.circleCenterY)
                actionView.layout(point.x.toInt(), point.y.toInt(), (point.x + actionView.measuredWidth).toInt(), (point.y + actionView.measuredHeight).toInt())
                val titleView = mActionTitleViews[key]
                if (titleView != null) {
                    val titleLeft = point.x + actionView.measuredWidth / 2 - titleView.measuredWidth / 2
                    val titleTop = point.y - 10 - titleView.measuredHeight
                    titleView.layout(titleLeft.toInt(), titleTop.toInt(), (titleLeft + titleView.measuredWidth).toInt(), (titleTop + titleView.measuredHeight).toInt())
                }
                index++
            }
            if (!mAnimated) {
                animateActionsIn()
                animateIndicatorIn()
                animateScrimIn()
                mAnimated = true
            }
        }

        private fun animateActionsIn() {
            var index = 0
            for (view in mActionViews.values) {
                mActionsInAnimator.animateActionIn(view.action, index, view, mCenterPoint)
                index++
            }
        }

        private fun animateIndicatorIn() {
            mActionsInAnimator.animateIndicatorIn(mIndicatorView)
        }

        private fun animateScrimIn() {
            mActionsInAnimator.animateScrimIn(mScrimView)
        }

        fun animateOut(): Int {
            var maxDuration = 0
            maxDuration = Math.max(maxDuration, animateActionsOut())
            maxDuration = Math.max(maxDuration, animateScrimOut())
            maxDuration = Math.max(maxDuration, animateIndicatorOut())
            maxDuration = Math.max(maxDuration, animateLabelsOut())
            return maxDuration
        }

        private fun animateActionsOut(): Int {
            var index = 0
            var maxDuration = 0
            for (view in mActionViews.values) {
                view.clearAnimation()
                maxDuration = Math.max(mActionsOutAnimator.animateActionOut(view.action, index, view, mCenterPoint), maxDuration)
                index++
            }
            return maxDuration
        }

        private fun animateLabelsOut(): Int {
            for (view in mActionTitleViews.values) {
                view.animate().alpha(0f).duration = 100
            }
            return 200
        }

        private fun animateIndicatorOut(): Int {
            mIndicatorView.clearAnimation()
            return mActionsOutAnimator.animateIndicatorOut(mIndicatorView)
        }

        private fun animateScrimOut(): Int {
            mScrimView.clearAnimation()
            return mActionsOutAnimator.animateScrimOut(mScrimView)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (isShown) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        mLastTouch[event.rawX] = event.rawY
                        var index = 0
                        for (actionView in mActionViews.values) {
                            if (insideCircle(getActionPoint(index, getOptimalStartAngle(actionView.actionCircleRadiusExpanded), actionView), actionView.actionCircleRadiusExpanded, event.rawX, event.rawY)) {
                                if (!actionView.isSelected) {
                                    actionView.isSelected = true
                                    actionView.animateInterpolation(1f)
                                    val actionTitleView = mActionTitleViews[actionView.action]
                                    if (actionTitleView != null) {
                                        actionTitleView.visibility = VISIBLE
                                        actionTitleView.bringToFront()
                                        mActionsTitleInAnimator.animateActionTitleIn(actionView.action, index, actionTitleView)
                                    }
                                    if (mOnActionHoverChangedListener != null) {
                                        mOnActionHoverChangedListener!!.onActionHoverChanged(actionView.action, this@QuickActionView, true)
                                    }
                                }
                            } else {
                                if (actionView.isSelected) {
                                    actionView.isSelected = false
                                    actionView.animateInterpolation(0f)
                                    val actionTitleView = mActionTitleViews[actionView.action]
                                    if (actionTitleView != null) {
                                        val timeTaken = mActionsTitleOutAnimator.animateActionTitleOut(actionView.action, index, actionTitleView)
                                        actionTitleView.postDelayed(Runnable { actionTitleView.visibility = GONE }, timeTaken.toLong())
                                    }
                                    if (mOnActionHoverChangedListener != null) {
                                        mOnActionHoverChangedListener!!.onActionHoverChanged(actionView.action, this@QuickActionView, false)
                                    }
                                }
                            }
                            index++
                        }
                        invalidate()
                    }
                    MotionEvent.ACTION_UP -> {
                        for ((key, value) in mActionViews) {
                            if (value.isSelected && mOnActionSelectedListener != null) {
                                mOnActionSelectedListener!!.onActionSelected(key, this@QuickActionView)
                                break
                            }
                        }
                        dismiss()
                    }
                    MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> dismiss()
                }
            }
            return true
        }

        private fun getActionPoint(index: Int, startAngle: Float, view: ActionView): PointF {
            val point = PointF(mCenterPoint)
            val angle = (Math.toRadians(startAngle.toDouble()) + getActionOffsetAngle(index, view)).toFloat()
            point.offset((Math.cos(angle.toDouble()) * getTotalRadius(view.actionCircleRadiusExpanded)).toInt().toFloat(), (Math.sin(angle.toDouble()) * getTotalRadius(view.actionCircleRadiusExpanded)).toInt().toFloat())
            return point
        }

        private fun getActionOffsetAngle(index: Int, view: ActionView): Float {
            return (index * (2 * Math.atan2((view.actionCircleRadiusExpanded + mActionPadding).toDouble(), getTotalRadius(view.actionCircleRadiusExpanded).toDouble()))).toFloat()
        }

        private val maxActionAngle: Float
            private get() {
                var index = 0
                var max = 0f
                for (actionView in mActionViews.values) {
                    max = getActionOffsetAngle(index, actionView)
                    index++
                }
                return max
            }

        private fun getTotalRadius(actionViewRadiusExpanded: Float): Float {
            return mActionDistance + Math.max(mIndicatorView.width, mIndicatorView.height) + actionViewRadiusExpanded
        }

        private fun getOptimalStartAngle(actionViewRadiusExpanded: Float): Float {
            if (measuredWidth > 0) {
                val radius = getTotalRadius(actionViewRadiusExpanded)
                val top = -mCenterPoint.y
                val topIntersect = !java.lang.Double.isNaN(Math.acos((top / radius).toDouble()))
                val horizontalOffset = (mCenterPoint.x - measuredWidth / 2.0f) / (measuredWidth / 2.0f)
                val angle: Float
                val offset = Math.pow(Math.abs(horizontalOffset).toDouble(), 1.2).toFloat() * Math.signum(horizontalOffset)
                angle = if (topIntersect) {
                    90 + 90 * offset
                } else {
                    270 - 90 * offset
                }
                normalizeAngle(angle.toDouble())
                return (angle - Math.toDegrees(middleAngleOffset.toDouble())).toFloat()
            }
            return (270 - Math.toDegrees(middleAngleOffset.toDouble())).toFloat()
        }

        fun normalizeAngle(angleDegrees: Double): Float {
            var angleDegrees = angleDegrees
            angleDegrees = angleDegrees % 360
            angleDegrees = (angleDegrees + 360) % 360
            return angleDegrees.toFloat()
        }

        private val middleAngleOffset: Float
            private get() = maxActionAngle / 2f

        private fun insideCircle(center: PointF, radius: Float, x: Float, y: Float): Boolean {
            return distance(center, x, y) < radius
        }

        private fun distance(point: PointF, x: Float, y: Float): Float {
            return Math.sqrt(Math.pow((x - point.x).toDouble(), 2.0) + Math.pow((y - point.y).toDouble(), 2.0)).toFloat()
        }
    }

    /**
     * A class to combine a long click listener and a touch listener, to register views with
     */
    private inner class RegisteredListener : OnLongClickListener, OnTouchListener {
        private var mTouchX = 0f
        private var mTouchY = 0f

        override fun onLongClick(v: View): Boolean {
//            show(v, Point(mTouchX.toInt(), mTouchY.toInt()))
            return false
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (isShown.not()) show(anchor = v, offset = Point(mTouchX.toInt(), mTouchY.toInt()))
            mTouchX = event.x
            mTouchY = event.y
            if (isShown) {
                mQuickActionViewLayout!!.onTouchEvent(event)
            }
            return isShown
        }
    }

    companion object {
        /**
         * Create a QuickActionView which you can configure as desired, then
         * call [.register] to show it.
         *
         * @param context activity context
         * @return the QuickActionView for you to
         */
        fun make(context: Context): QuickActionView {
            return QuickActionView(context)
        }
    }
}