package com.singularitycoder.flowlauncher.helper.pinterestView

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import android.view.animation.AccelerateInterpolator
import android.widget.PopupWindow
import android.widget.TextView
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.helper.changeColor
import com.singularitycoder.flowlauncher.helper.color

/**
 * https://github.com/brucetoo/PinterestView
 * Created by Bruce Too
 * On 10/2/15.
 * At 11:10
 */
class PinterestView : ViewGroup, OnTouchListener {

    companion object {
        private const val TAG = "PinterestView"
        private const val EXPAND_ANIMATION_DURATION = 3
        private const val SCALE_ANIMATION_DURATION = 100
        private const val MAX_SCALE = 1.2f
        private const val DEFAULT_FROM_DEGREES = -90.0f
        private const val DEFAULT_TO_DEGREES = -90.0f
        private const val DEFAULT_CHILD_SIZE = 44
        private const val DEFAULT_TIPS_COLOR = Color.WHITE
        private const val DEFAULT_TIPS_BACKGROUND = R.drawable.shape_child_item
        private const val DEFAULT_TIPS_SIZE = 15
        private const val DEFAULT_RECT_RADIUS = 100
        private const val DEFAULT_RADIUS = 80
        private fun distSq(x1: Double, y1: Double, x2: Double, y2: Double): Double {
            return Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0)
        }

        private fun computeChildFrame(
            centerX: Float, centerY: Float, radius: Int, degrees: Float,
            size: Int
        ): Rect {
            val childCenterX = centerX + radius * Math.cos(Math.toRadians(degrees.toDouble()))
            val childCenterY = centerY + radius * Math.sin(Math.toRadians(degrees.toDouble()))
            return Rect((childCenterX - size / 2).toInt(), (childCenterY - size / 2).toInt(), (childCenterX + size / 2).toInt(), (childCenterY + size / 2).toInt())
        }
    }

    private var mChildSize = 0
    private var mTipsColor = 0
    private var mTipsBackground = 0
    private var mTipsSize = 0
    private var mFromDegrees = DEFAULT_FROM_DEGREES
    private var mToDegrees = DEFAULT_TO_DEGREES
    private var mRadius = 0
    private var mMaxScale = 0f
    private var mContext: Context
    private var mExpanded = false
    private val mChildViews = ArrayList<View>()
    private var mCenterX = 0f
    private var mCenterY = 0f
    private var mPinMenuClickListener: PinMenuClickListener? = null
    private var mPopTips: PopupWindow? = null
    private val mInner = Rect()
    private var mLastNearestView: View? = null
    private var mIsAnimating = false
    private val gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
        override fun onShowPress(e: MotionEvent) {
            mCenterX = e.rawX
            mCenterY = e.rawY
            Log.i(TAG, "centerX:$mCenterX  centerY:$mCenterY")
            confirmDegreeRangeByCenter(mCenterX, mCenterY)
            this@PinterestView.visibility = VISIBLE
            switchState()
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (mPinMenuClickListener != null) {
                mPinMenuClickListener!!.onAnchorViewClick()
            }
            return true
        }
    })

    constructor(context: Context) : super(context) {
        mContext = context
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
        if (attrs != null) {
            val a = getContext().obtainStyledAttributes(attrs, R.styleable.PinterestView, 0, 0)
            mChildSize = a.getDimensionPixelSize(R.styleable.PinterestView_child_size, DEFAULT_CHILD_SIZE)
            mTipsColor = a.getColor(R.styleable.PinterestView_tips_color, DEFAULT_TIPS_COLOR)
            mTipsBackground = a.getResourceId(R.styleable.PinterestView_tips_background, DEFAULT_TIPS_BACKGROUND)
            mTipsSize = a.getDimensionPixelSize(R.styleable.PinterestView_tips_size, DEFAULT_TIPS_SIZE)
            mRadius = a.getDimensionPixelOffset(R.styleable.PinterestView_child_radius, dp2px(DEFAULT_RADIUS.toFloat()))
            mMaxScale = a.getFloat(R.styleable.PinterestView_child_max_scale, MAX_SCALE)
            createTipsPopWindow(context)
            a.recycle()
        }
        setOnTouchListener(this)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (visibility == VISIBLE) {
            handleTouchEvent(event)
            return true
        }
        return gestureDetector.onTouchEvent(event)
    }

    /**
     * find the nearest child view
     */
    private fun nearest(x: Float, y: Float, views: List<View>): View? {
        var minDistSq = Double.MAX_VALUE
        var minView: View? = null
        for (view in views) {
            val rect = Rect()
            view.getGlobalVisibleRect(rect)
            val distSq = distSq(
                x.toDouble(), y.toDouble(), rect.centerX().toDouble(),
                rect.centerY().toDouble()
            )
            if (distSq < Math.pow((1.2f * view.measuredWidth).toDouble(), 2.0) && distSq < minDistSq) {
                minDistSq = distSq
                minView = view
            }
        }
        return minView
    }

    private fun handleTouchEvent(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_MOVE ->                 //only listen ACTION_MOVE when PinterestView is visible
                if (this@PinterestView.visibility == VISIBLE) {
                    val nearest = nearest(event.rawX, event.rawY, mChildViews)
                    if (nearest != null) {
                        if (mLastNearestView != null && mLastNearestView === nearest) return
                        DurX.putOn(nearest).animate().scale(mMaxScale).duration(SCALE_ANIMATION_DURATION.toLong())
                        (nearest as CircleImageView).fillColor = mContext.color(R.color.purple_500)
                        if (mPopTips?.isShowing == true) {
                            mPopTips?.dismiss()
                        }
                        val contentView = mPopTips?.contentView as? TextView
                        contentView?.text = nearest.getTag() as String
                        val width = contentView?.measuredWidth ?: 0
                        val offsetLeft = if (width == 0) -mChildSize / 4 else -width / 2 + mChildSize / 2
                        if (!mIsAnimating) {
                            mPopTips?.showAsDropDown(nearest, offsetLeft, -mChildSize * 2)
                        }
                        for (view in mChildViews) {
                            if (view !== nearest) {
                                (view as CircleImageView).apply {
                                    fillColor = mContext.color(R.color.purple_50)
//                                    setImageDrawable(drawable.changeColor(mContext, R.color.purple_50))
                                }
                                DurX.putOn(view).animate().scale(1F).duration(SCALE_ANIMATION_DURATION.toLong())
                            }
                        }
                        mLastNearestView = nearest
                    } else {
                        mLastNearestView = null
                        mPopTips?.dismiss()
                        for (view in mChildViews) {
                            DurX.putOn(view).scale(1F)
                            (view as CircleImageView).apply {
                                fillColor = mContext.color(R.color.purple_50)
//                                setImageDrawable(drawable.changeColor(mContext, R.color.purple_500))
                            }
                        }
                    }
                }
            MotionEvent.ACTION_UP -> if (this@PinterestView.visibility == VISIBLE) {
                mPopTips!!.dismiss()
                val nearest = nearest(event.rawX, event.rawY, mChildViews)
                if (nearest != null && nearest.tag != null) {
                    val clickItemPos = if (this@PinterestView.tag == null) -1 else this@PinterestView.tag as Int
                    mPinMenuClickListener?.onMenuItemClick(nearest, clickItemPos)
                }
                switchState()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        //Screen width and height
        setMeasuredDimension(mContext.resources.displayMetrics.widthPixels, mContext.resources.displayMetrics.heightPixels)
        val count = childCount
        for (i in 0 until count) {
            getChildAt(i).measure(
                MeasureSpec.makeMeasureSpec(mChildSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mChildSize, MeasureSpec.EXACTLY)
            )
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        getGlobalVisibleRect(mInner) // get PintertstView Height
        val innerTop = (resources.displayMetrics.heightPixels - (mInner.bottom - mInner.top)).toFloat() // distance from screen top
        mCenterY = mCenterY - innerTop
        Log.i(TAG, "innerTop:$innerTop centerX:$mCenterX  centerY:$mCenterY")
        val childCount = childCount
        //single degrees
        val perDegrees = (mToDegrees - mFromDegrees) / (childCount - 1)
        var degrees = mFromDegrees
        mChildViews.clear()
        //Note if i = 1 indicate ignore the center view
        for (i in 1 until getChildCount()) {
            mChildViews.add(getChildAt(i))
        }

        //add centerView
        val centerRect = computeChildFrame(mCenterX, mCenterY, 0, perDegrees, mChildSize)
        getChildAt(0).layout(centerRect.left, centerRect.top, centerRect.right, centerRect.bottom)
        degrees += perDegrees
        //add other view
        for (i in 1 until childCount) {
            val frame = computeChildFrame(mCenterX, mCenterY, mRadius, degrees, mChildSize)
            if (i == 1) {
                Log.i("computeChildFrame:", frame.toString() + "")
            }
            degrees += perDegrees
            getChildAt(i).layout(frame.left, frame.top, frame.right, frame.bottom)
        }
    }

    private fun createTipsPopWindow(context: Context) {
        val tips = TextView(context).apply {
            setTypeface(null, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, mTipsSize.toFloat())
            setTextColor(mTipsColor)
            setBackgroundResource(mTipsBackground)
            gravity = Gravity.CENTER
        }
        mPopTips = PopupWindow(tips, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    private fun confirmDegreeRangeByCenter(centerX: Float, centerY: Float) {
        val metrics = mContext.resources.displayMetrics

        //left-top (-60,90)
        val leftTopRect = Rect(0, 0, dp2px(DEFAULT_RECT_RADIUS.toFloat()), dp2px(DEFAULT_RECT_RADIUS.toFloat()))

        //top (-10,140)
        val topRect = Rect(dp2px(DEFAULT_RECT_RADIUS.toFloat()), 0, metrics.widthPixels - dp2px(DEFAULT_RECT_RADIUS.toFloat()), dp2px(DEFAULT_RECT_RADIUS.toFloat()))

        //right-top  50,200
        val rightTopRect = Rect(metrics.widthPixels - dp2px(DEFAULT_RECT_RADIUS.toFloat()), 0, metrics.widthPixels, dp2px(DEFAULT_RECT_RADIUS.toFloat()))

        //left -100,50
        val leftRect = Rect(0, dp2px(DEFAULT_RECT_RADIUS.toFloat()), dp2px(DEFAULT_RECT_RADIUS.toFloat()), metrics.heightPixels - dp2px(DEFAULT_RECT_RADIUS.toFloat()))

        //right 80,230
        val rightRect = Rect(metrics.widthPixels - dp2px(DEFAULT_RECT_RADIUS.toFloat()), dp2px(DEFAULT_RECT_RADIUS.toFloat()), metrics.widthPixels, metrics.heightPixels - dp2px(DEFAULT_RECT_RADIUS.toFloat()))

        //left_bottom -140,10
        val leftBottomRect = Rect(0, metrics.heightPixels - dp2px(DEFAULT_RECT_RADIUS.toFloat()), dp2px(DEFAULT_RECT_RADIUS.toFloat()), metrics.heightPixels)

        //bottom  170,320
        val bottomRect = Rect(dp2px(DEFAULT_RECT_RADIUS.toFloat()), metrics.heightPixels - dp2px(DEFAULT_RECT_RADIUS.toFloat()), metrics.widthPixels - dp2px(DEFAULT_RECT_RADIUS.toFloat()), metrics.heightPixels)

        //right_bottom 150,300 and center
        val rightBottomRect = Rect(metrics.widthPixels - dp2px(DEFAULT_RECT_RADIUS.toFloat()), metrics.heightPixels - dp2px(DEFAULT_RECT_RADIUS.toFloat()), metrics.widthPixels, metrics.heightPixels)
        val centerRect = Rect(dp2px(DEFAULT_RECT_RADIUS.toFloat()), dp2px(DEFAULT_RECT_RADIUS.toFloat()), metrics.widthPixels - dp2px(DEFAULT_RECT_RADIUS.toFloat()), metrics.heightPixels - dp2px(DEFAULT_RECT_RADIUS.toFloat()))
        if (leftTopRect.contains(centerX.toInt(), centerY.toInt())) {
            mFromDegrees = -60f
            mToDegrees = 90f
        } else if (topRect.contains(centerX.toInt(), centerY.toInt())) {
            mFromDegrees = -10f
            mToDegrees = 150f
        } else if (rightTopRect.contains(centerX.toInt(), centerY.toInt())) {
            mFromDegrees = 50f
            mToDegrees = 200f
        } else if (leftRect.contains(centerX.toInt(), centerY.toInt())) {
            mFromDegrees = -100f
            mToDegrees = 50f
        } else if (rightRect.contains(centerX.toInt(), centerY.toInt())) {
            mFromDegrees = 80f
            mToDegrees = 230f
        } else if (leftBottomRect.contains(centerX.toInt(), centerY.toInt())) {
            mFromDegrees = -140f
            mToDegrees = 10f
        } else if (bottomRect.contains(centerX.toInt(), centerY.toInt())) {
            mFromDegrees = 170f
            mToDegrees = 320f
        } else if (rightBottomRect.contains(centerX.toInt(), centerY.toInt()) || centerRect.contains(centerX.toInt(), centerY.toInt())) {
            mFromDegrees = 150f
            mToDegrees = 300f
        }
        requestLayout()
    }

    private fun getChildDisPlayBounds(view: View): Rect {
        // scale the rect range
        val rect = Rect()
        view.getHitRect(rect)
        return rect
    }

    private fun bindChildAnimation(child: View, position: Int) {
        //in case when init in,child.getWidth = 0 cause get wrong rect
        child.viewTreeObserver.addOnGlobalLayoutListener {
            val childRect = getChildDisPlayBounds(child)
            if (mExpanded) {
                expandAnimation(child, childRect)
            }
        }
        val childRect = getChildDisPlayBounds(child)
        if (!mExpanded) {
            collapseAnimation(child, childRect)
        }
    }

    private fun collapseAnimation(child: View, childRect: Rect) {
        DurX.putOn(child).animate()
            .translationX(0F, (mCenterX - childRect.exactCenterX()) / 2)
            .translationY(0F, (mCenterY - childRect.exactCenterY()) / 2)
            .alpha(0.5f)
            .duration(EXPAND_ANIMATION_DURATION.toLong())
            .interpolator(AccelerateInterpolator())
            .end(object : DurX.Listeners.End {
                override fun onEnd() {
                    recoverChildView()
                    this@PinterestView.visibility = GONE
                }
            })
    }

    private fun expandAnimation(child: View, childRect: Rect) {
        DurX.putOn(child).animate()
            .translationX((mCenterX - childRect.exactCenterX()) / 2, 0F)
            .translationY((mCenterY - childRect.exactCenterY()) / 2, 0F)
            .alpha(0.5f, 1F)
            .duration((EXPAND_ANIMATION_DURATION / 2).toLong())
            .interpolator(AccelerateInterpolator())
            .start(object : DurX.Listeners.Start {
                override fun onStart() {
                    mIsAnimating = true
                }
            })
            .end(object : DurX.Listeners.End {
                override fun onEnd() {
                    mIsAnimating = false
                    recoverChildView()
                }
            })
    }

    /**
     * center view animation
     *
     * @param child
     */
    private fun bindCenterViewAnimation(child: View) {
        val from = if (mExpanded) 0.5f else 1.0f
        val to = if (mExpanded) 1.0f else 0.5f
        DurX.putOn(child).animate()
            .scale(from, to)
            .alpha(from, to)
            .duration(EXPAND_ANIMATION_DURATION.toLong())
            .interpolator(AccelerateInterpolator())
            .end(object : DurX.Listeners.End {
                override fun onEnd() {
                    recoverChildView()
                    if (!mExpanded) {
                        this@PinterestView.visibility = GONE
                    }
                }
            })
    }

    private fun recoverChildView() {
        val childCount = childCount
        for (i in 0 until childCount) {
//            getChildAt(i).animate().setDuration(100).translationX(0).translationY(0).scaleX(1).scaleX(1).start()
            DurX.putOn(getChildAt(i)).scale(1F).translation(0f, 0f)
        }
    }

    fun switchState() {
        mExpanded = !mExpanded
        val childCount = childCount
        //other view
        for (i in 1 until childCount) {
            (getChildAt(i) as CircleImageView).fillColor = mContext.color(R.color.purple_50)
            bindChildAnimation(getChildAt(i), i)
        }
        //center view
        bindCenterViewAnimation(getChildAt(0))
    }

    /**
     * addView to PinterestView
     *
     * @param centerView
     * @param normalViews
     */
    fun addMenuItem(centerView: View?, vararg normalViews: View?) {
        addView(centerView, 0)
        for (normalView in normalViews) {
            addView(normalView)
        }
    }

    /**
     * size (dp)
     * default all item child size are same
     *
     * @param size //dp
     */
    fun setChildSize(size: Int) {
        if (mChildSize == size || size < 0) {
            return
        }
        //convert to px
        mChildSize = dp2px(size.toFloat())
        requestLayout()
    }

    private fun dp2px(dpVal: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpVal, resources.displayMetrics
        ).toInt()
    }

    /**
     * set Pinterest click listener
     *
     * @param pinMenuClickListener callback
     */
    fun setPinClickListener(pinMenuClickListener: PinMenuClickListener?) {
        mPinMenuClickListener = pinMenuClickListener
    }

    interface PinMenuClickListener {
        /**
         * PinterestView item click
         *
         * @param checkedView view has be checked
         */
        fun onMenuItemClick(checkedView: View?, clickItemPos: Int)

        /**
         * Anchor view(the view click to show pinterestView) click
         */
        fun onAnchorViewClick()
    }
}