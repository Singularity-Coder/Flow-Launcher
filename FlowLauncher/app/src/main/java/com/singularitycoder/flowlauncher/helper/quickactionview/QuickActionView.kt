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
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.quickactionview.animator.FadeInFadeOutActionsTitleAnimator
import com.singularitycoder.flowlauncher.helper.quickactionview.animator.SlideFromCenterAnimator
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A QuickActionView, which shows actions when a view is long pressed.
 *
 * @see [https://github.com/ovenbits/QuickActionView](https://github.com/ovenbits/QuickActionView)
 */
open class QuickActionView private constructor(private val mContext: Context) {
    companion object {
        /**
         * Create a QuickActionView which you can configure as desired, then
         * call [.register] to show it.
         */
        fun make(context: Context): QuickActionView {
            return QuickActionView(context)
        }
    }

    private var isShown = false
    private var onActionSelectedListener: OnActionSelectedListener? = null
    private var mOnDismissListener: OnDismissListener? = null
    private var mOnShowListener: OnShowListener? = null
    private var onActionHoverChangedListener: OnActionHoverChangedListener? = null
    private val actionDistance: Float
    private val actionPadding: Int
    private val actionsList: ArrayList<Action> = ArrayList()

    /**
     * Get the extras associated with the QuickActionView. Allows for
     * saving state to the QuickActionView
     *
     * @return the bundle for the QuickActionView
     */
    private var extras: Bundle? = null
    private var quickActionViewLayout: QuickActionViewLayout? = null
    private val mConfig: Config = Config(mContext)
    private var actionsInAnimator: ActionsInAnimator
    private var actionsOutAnimator: ActionsOutAnimator
    private var actionsTitleInAnimator: ActionsTitleInAnimator
    private var actionsTitleOutAnimator: ActionsTitleOutAnimator

    @ColorInt
    private var scrimColor = mContext.color(R.color.sixty_percent_transparent_white)
    private var indicatorDrawable: Drawable?
    private val registeredListeners = HashMap<View, RegisteredListener>()

    /**
     * Get the center point of the [QuickActionView] aka the point at which the actions will eminate from
     * @return the center point, or null if the view has not yet been created
     */
    val centerPoint: Point?
        get() = if (quickActionViewLayout != null) {
            quickActionViewLayout!!.mCenterPoint
        } else null

    /**
     * Retrieve the view that has been long pressed
     * @return the registered view that was long pressed to show the QuickActionView
     */
    var longPressedView: View? = null
        private set

    init {
        indicatorDrawable = ContextCompat.getDrawable(mContext, R.drawable.qav_indicator)
        actionDistance = mContext.resources.getDimensionPixelSize(R.dimen.qav_action_distance).toFloat()
        actionPadding = mContext.resources.getDimensionPixelSize(R.dimen.qav_action_padding)
        val defaultAnimator = SlideFromCenterAnimator(true)
        val defaultTitleAnimator = FadeInFadeOutActionsTitleAnimator()
        actionsInAnimator = defaultAnimator
        actionsOutAnimator = defaultAnimator
        actionsTitleInAnimator = defaultTitleAnimator
        actionsTitleOutAnimator = defaultTitleAnimator
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
        val listener = RegisteredListener()
        registeredListeners[view] = listener
        view.setOnTouchListener(listener)
//        view.setOnLongClickListener(listener)
        return this
    }

    /**
     * Unregister the view so that it can no longer be long pressed to show the QuickActionView
     *
     * @param view the view to unregister
     */
    fun unregister(view: View) {
        registeredListeners.remove(view)
        view.setOnTouchListener(null)
//        view.setOnLongClickListener(null)
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
        this.onActionSelectedListener = onActionSelectedListener
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
        onActionHoverChangedListener = listener
        return this
    }

    /**
     * Set the indicator drawable (the drawable that appears at the point the user has long pressed
     *
     * @param indicatorDrawable the indicator drawable
     * @return the QuickActionView
     */
    fun setIndicatorDrawable(indicatorDrawable: Drawable?): QuickActionView {
        this.indicatorDrawable = indicatorDrawable
        return this
    }

    /**
     * Set the scrim color (the background behind the QuickActionView)
     *
     * @param scrimColor the desired scrim color
     * @return the QuickActionView
     */
    fun setScrimColor(@ColorInt scrimColor: Int): QuickActionView {
        this.scrimColor = scrimColor
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
        this.actionsInAnimator = actionsInAnimator
        return this
    }

    /**
     * Override the animations for when the QuickActionView dismisses
     *
     * @param actionsOutAnimator the animation overrides
     * @return this QuickActionView
     */
    fun setActionsOutAnimator(actionsOutAnimator: ActionsOutAnimator): QuickActionView {
        this.actionsOutAnimator = actionsOutAnimator
        return this
    }

    /**
     * Override the animations for when the QuickActionView action title shows
     *
     * @param actionsTitleInAnimator the custom animator
     * @return this QuickActionView
     */
    fun setActionsTitleInAnimator(actionsTitleInAnimator: ActionsTitleInAnimator): QuickActionView {
        this.actionsTitleInAnimator = actionsTitleInAnimator
        return this
    }

    /**
     * Override the animations for when the QuickActionView dismisses
     *
     * @param actionsTitleOutAnimator the custom animator
     * @return this QuickActionView
     */
    fun setActionsTitleOutAnimator(actionsTitleOutAnimator: ActionsTitleOutAnimator): QuickActionView {
        this.actionsTitleOutAnimator = actionsTitleOutAnimator
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

    private fun display(point: Point) {
        checkNotNull(actionsList) { "You need to give the QuickActionView actions before calling show!" }
        val manager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL)
        params.format = PixelFormat.TRANSLUCENT
        quickActionViewLayout = QuickActionViewLayout(mContext, actionsList, point)
        manager.addView(quickActionViewLayout, params)
        if (mOnShowListener != null) {
            mOnShowListener?.onShow(this)
        }
    }

    private fun animateHide() {
        val duration = quickActionViewLayout!!.animateOut()
        Handler(Looper.getMainLooper()).postDelayed({ removeView() }, duration.toLong())
    }

    private fun removeView() {
        if (quickActionViewLayout != null) {
            val manager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (checkAttachedToWindow(quickActionViewLayout!!)) {
                manager.removeView(quickActionViewLayout)
            }
            quickActionViewLayout = null
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
            mOnDismissListener?.onDismiss(this@QuickActionView)
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
        var textPaddingBottom: Int = textPaddingTop
        var textPaddingLeft: Int = textPaddingTop
        var textPaddingRight: Int = textPaddingTop
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

        constructor(context: Context) : this(
            context = context,
            typeface = Typeface.DEFAULT,
            textSize = context.resources.getInteger(R.integer.qav_action_title_view_text_size),
            textPaddingTop = context.resources.getDimensionPixelSize(R.dimen.qav_action_title_view_text_padding)
        )

        init {
            mDefaultConfig = Action.Config(context)
        }

        fun getTextBackgroundDrawable(context: Context?): Drawable? {
            return mDefaultConfig.getTextBackgroundDrawable(context)
        }

        fun setTextBackgroundDrawable(@DrawableRes textBackgroundDrawable: Int) {
            mDefaultConfig.setTextBackgroundDrawable(textBackgroundDrawable)
        }

        fun setBackgroundColor(@ColorInt backgroundColor: Int) {
            mDefaultConfig.setBackgroundColor(backgroundColor)
        }
    }

    /**
     * Parent layout that actually houses all of the quick action views
     */
    private inner class QuickActionViewLayout(context: Context, actionsList: ArrayList<Action>, val mCenterPoint: Point) : FrameLayout(context) {
        private val indicatorView: View
        private val scrimView: View
        private val actionViews = LinkedHashMap<Action, ActionView>()
        private val mActionTitleViews = LinkedHashMap<Action, ActionTitleView>()
        private val lastTouch = PointF()
        private var mAnimated = false

        private val middleAngleOffset: Float
            get() = maxActionAngle / 2f

        private val maxActionAngle: Float
            get() {
                var index = 0
                var max = 0f
                for (actionView in actionViews.values) {
                    max = getActionOffsetAngle(index, actionView)
                    index++
                }
                return max
            }

        init {
            scrimView = View(context)
            scrimView.setBackgroundColor(scrimColor)
            val scrimParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            addView(scrimView, scrimParams)
            indicatorView = View(context)
            indicatorView.background = indicatorDrawable
            val indicatorParams = LayoutParams(indicatorDrawable?.intrinsicWidth ?: 600, indicatorDrawable?.intrinsicHeight ?: 600)
            addView(indicatorView, indicatorParams)
            for (action in actionsList) {
                val helper = ConfigHelper(action.config, mConfig)
                val actionView = ActionView(context, action, helper)
                actionViews[action] = actionView
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
            scrimView.layout(0, 0, measuredWidth, measuredHeight)
            indicatorView.layout(
                mCenterPoint.x - (indicatorView.measuredWidth / 2.0).toInt(),
                mCenterPoint.y - (indicatorView.measuredHeight / 2.0).toInt(),
                mCenterPoint.x + (indicatorView.measuredWidth / 2.0).toInt(),
                mCenterPoint.y + (indicatorView.measuredHeight / 2.0).toInt()
            )
            var index = 0
            for ((key, actionView) in actionViews) {
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
//                animateActionsIn()
//                animateIndicatorIn()
//                animateScrimIn()
                mAnimated = true
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (isShown.not()) return false
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    lastTouch[event.rawX] = event.rawY
                    var index = 0
                    for (actionView in actionViews.values) {
                        val isInsideCircle = insideCircle(
                            center = getActionPoint(index = index, startAngle = getOptimalStartAngle(actionView.actionCircleRadiusExpanded), view = actionView),
                            radius = actionView.actionCircleRadiusExpanded,
                            x = event.rawX,
                            y = event.rawY
                        )
                        if (isInsideCircle) {
                            if (actionView.isSelected.not()) {
                                actionView.isSelected = true
//                                this@QuickActionView.setBackgroundColor(mContext.color(R.color.purple_500))
                                actionView.animateInterpolation(1f)
                                val actionTitleView = mActionTitleViews[actionView.action]
                                if (actionTitleView != null) {
                                    actionTitleView.visibility = VISIBLE
                                    actionTitleView.bringToFront()
                                    actionsTitleInAnimator.animateActionTitleIn(actionView.action, index, actionTitleView)
                                }
                                if (onActionHoverChangedListener != null) {
                                    onActionHoverChangedListener?.onActionHoverChanged(actionView.action, this@QuickActionView, true)
                                }
                            }
                        } else {
                            if (actionView.isSelected) {
                                actionView.isSelected = false
//                                this@QuickActionView.setBackgroundColor(mContext.color(R.color.purple_50))
                                actionView.animateInterpolation(0f)
                                val actionTitleView = mActionTitleViews[actionView.action]
                                if (actionTitleView != null) {
                                    val timeTaken = actionsTitleOutAnimator.animateActionTitleOut(actionView.action, index, actionTitleView)
                                    actionTitleView.postDelayed(Runnable { actionTitleView.visibility = GONE }, timeTaken.toLong())
                                }
                                if (onActionHoverChangedListener != null) {
                                    onActionHoverChangedListener?.onActionHoverChanged(actionView.action, this@QuickActionView, false)
                                }
                            }
                        }
                        index++
                    }
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    this@QuickActionView.setBackgroundColor(mContext.color(R.color.purple_50))
                    for ((key, value) in actionViews) {
                        if (value.isSelected && onActionSelectedListener != null) {
                            onActionSelectedListener?.onActionSelected(key, this@QuickActionView)
                            break
                        }
                    }
                    dismiss()
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> dismiss()
            }
            return true
        }

        private fun animateActionsIn() {
            var index = 0
            for (view in actionViews.values) {
                actionsInAnimator.animateActionIn(
                    action = view.action,
                    index = index,
                    view = view,
                    center = mCenterPoint
                )
                index++
            }
        }

        private fun animateIndicatorIn() {
            actionsInAnimator.animateIndicatorIn(indicatorView)
        }

        private fun animateScrimIn() {
            actionsInAnimator.animateScrimIn(scrimView)
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
            for (view in actionViews.values) {
                view.clearAnimation()
                maxDuration = Math.max(actionsOutAnimator.animateActionOut(view.action, index, view, mCenterPoint), maxDuration)
                index++
            }
            return maxDuration
        }

        private fun animateLabelsOut(): Int {
            for (view in mActionTitleViews.values) {
                view.animate().alpha(0f).duration = 50
            }
            return 50
        }

        private fun animateIndicatorOut(): Int {
            indicatorView.clearAnimation()
            return actionsOutAnimator.animateIndicatorOut(indicatorView)
        }

        private fun animateScrimOut(): Int {
            scrimView.clearAnimation()
            return actionsOutAnimator.animateScrimOut(scrimView)
        }

        private fun getActionPoint(index: Int, startAngle: Float, view: ActionView): PointF {
            val point = PointF(mCenterPoint)
            val angle = (Math.toRadians(startAngle.toDouble()) + getActionOffsetAngle(index, view)).toFloat()
            point.offset((Math.cos(angle.toDouble()) * getTotalRadius(view.actionCircleRadiusExpanded)).toInt().toFloat(), (Math.sin(angle.toDouble()) * getTotalRadius(view.actionCircleRadiusExpanded)).toInt().toFloat())
            return point
        }

        private fun getActionOffsetAngle(index: Int, view: ActionView): Float {
            return (index * (2 * Math.atan2((view.actionCircleRadiusExpanded + actionPadding).toDouble(), getTotalRadius(view.actionCircleRadiusExpanded).toDouble()))).toFloat()
        }

        private fun getTotalRadius(actionViewRadiusExpanded: Float): Float {
            return actionDistance + Math.max(indicatorView.width, indicatorView.height) + actionViewRadiusExpanded
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

        fun normalizeAngle(angleDegreesParam: Double): Float {
            var angleDegrees = angleDegreesParam % 360
            angleDegrees = (angleDegrees + 360) % 360
            return angleDegrees.toFloat()
        }

        private fun insideCircle(center: PointF, radius: Float, x: Float, y: Float): Boolean {
            return distance(center, x, y) < radius
        }

        private fun distance(point: PointF, x: Float, y: Float): Float {
            return sqrt((x - point.x).toDouble().pow(2.0) + (y - point.y).toDouble().pow(2.0)).toFloat()
        }
    }

    /**
     * A class to combine a long click listener and a touch listener, to register views with
     */
    private inner class RegisteredListener : /*OnLongClickListener,*/ OnTouchListener {
//        private var mTouchX = 0f
//        private var mTouchY = 0f

//        override fun onLongClick(v: View): Boolean {
//            show(v, Point(mTouchX.toInt(), mTouchY.toInt()))
//            return false
//        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
//            if (isShown.not()) show(anchor = v, offset = Point(mTouchX.toInt(), mTouchY.toInt()))
            val x = 16 // deviceWidth - 16dp padding of fab to the right - fab radius 56/2
            val y = 16 // deviceHeight - 16dp padding of fab to the right - fab radius 56/2
            if (isShown.not()) show(anchor = v, offset = Point(x, y))
//            println("xx: ${event.x}, yy: ${event.y}")
//            println("x: ${x}, y: ${y}")
//            mTouchX = event.x
//            mTouchY = event.y
            if (isShown) {
                quickActionViewLayout?.onTouchEvent(event)
            }
            return isShown
        }
    }
}