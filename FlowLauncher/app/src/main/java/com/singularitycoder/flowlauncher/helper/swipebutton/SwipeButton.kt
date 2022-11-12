package com.singularitycoder.flowlauncher.helper.swipebutton

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.helper.color

/**
 * https://github.com/ebanx/swipe-button
 * Created by leandroferreira on 07/03/17.
 * more info: https://stackoverflow.com/questions/14910226/how-to-make-slide-to-unlock-button-in-android
 */
class SwipeButton : RelativeLayout {
    private var swipeButtonInner: ImageView? = null
    private var initialX = 0f
    var isActive = false
        private set
    private var centerText: TextView? = null
    private var background: ViewGroup? = null
    private var disabledDrawable: Drawable? = null
    private var enabledDrawable: Drawable? = null
    private var onStateChangeListener: OnStateChangeListener? = null
    private var onActiveListener: OnActiveListener? = null
    private var collapsedWidth = 0
    private var collapsedHeight = 0
    private var layer: LinearLayout? = null
    private var trailEnabled = false
    private var hasActivationState = false
    private var buttonLeftPadding = 0f
    private var buttonTopPadding = 0f
    private var buttonRightPadding = 0f
    private var buttonBottomPadding = 0f

    constructor(context: Context) : super(context) {
        init(context, null, -1, -1)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, -1, -1)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr, -1)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    fun setText(text: String?) {
        centerText!!.text = text
    }

    override fun setBackground(drawable: Drawable) {
        background!!.background = drawable
    }

    fun setSlidingButtonBackground(drawable: Drawable?) {
        background!!.background = drawable
    }

    fun setDisabledDrawable(drawable: Drawable?) {
        disabledDrawable = drawable
        if (!isActive) {
            swipeButtonInner!!.setImageDrawable(drawable)
        }
    }

    fun setButtonBackground(buttonBackground: Drawable?) {
        if (buttonBackground != null) {
            swipeButtonInner!!.background = buttonBackground
        }
    }

    fun setEnabledDrawable(drawable: Drawable?) {
        enabledDrawable = drawable
        if (isActive) {
            swipeButtonInner!!.setImageDrawable(drawable)
        }
    }

    fun setOnStateChangeListener(onStateChangeListener: OnStateChangeListener?) {
        this.onStateChangeListener = onStateChangeListener
    }

    fun setOnActiveListener(onActiveListener: OnActiveListener?) {
        this.onActiveListener = onActiveListener
    }

    fun setInnerTextPadding(left: Int, top: Int, right: Int, bottom: Int) {
        centerText!!.setPadding(left, top, right, bottom)
    }

    fun setSwipeButtonPadding(left: Int, top: Int, right: Int, bottom: Int) {
        swipeButtonInner!!.setPadding(left, top, right, bottom)
    }

    fun setHasActivationState(hasActivationState: Boolean) {
        this.hasActivationState = hasActivationState
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        hasActivationState = true
        background = RelativeLayout(context)
        val layoutParamsView = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParamsView.addRule(CENTER_IN_PARENT, TRUE)
        addView(background, layoutParamsView)
        val centerText = TextView(context)
        this.centerText = centerText
        centerText.gravity = Gravity.CENTER
        val layoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.addRule(CENTER_IN_PARENT, TRUE)
        background?.addView(centerText, layoutParams)
        val swipeButton = ImageView(context)
        swipeButtonInner = swipeButton
        if (attrs != null && defStyleAttr == -1 && defStyleRes == -1) {
            val typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.SwipeButton,
                defStyleAttr, defStyleRes
            )
            collapsedWidth = typedArray.getDimension(
                R.styleable.SwipeButton_button_image_width,
                ViewGroup.LayoutParams.WRAP_CONTENT.toFloat()
            ).toInt()
            collapsedHeight = typedArray.getDimension(
                R.styleable.SwipeButton_button_image_height,
                ViewGroup.LayoutParams.WRAP_CONTENT.toFloat()
            ).toInt()
            trailEnabled = typedArray.getBoolean(
                R.styleable.SwipeButton_button_trail_enabled,
                false
            )
            val trailingDrawable = typedArray.getDrawable(R.styleable.SwipeButton_button_trail_drawable)
            val backgroundDrawable = typedArray.getDrawable(R.styleable.SwipeButton_inner_text_background)
            if (backgroundDrawable != null) {
                background?.background = backgroundDrawable
            } else {
                background?.background = ContextCompat.getDrawable(context, R.drawable.shape_rounded)
            }
            if (trailEnabled) {
                layer = LinearLayout(context)
                if (trailingDrawable != null) {
                    layer!!.background = trailingDrawable
                } else {
                    layer!!.background = typedArray.getDrawable(R.styleable.SwipeButton_button_background)
                }
                layer!!.gravity = Gravity.START
                layer!!.visibility = GONE
                background?.addView(layer, layoutParamsView)
            }
            centerText.text = typedArray.getText(R.styleable.SwipeButton_inner_text)
            centerText.setTextColor(
                typedArray.getColor(
                    R.styleable.SwipeButton_inner_text_color,
                    Color.WHITE
                )
            )
            val textSize: Float = convertPixelsToSp(
                typedArray.getDimension(R.styleable.SwipeButton_inner_text_size, 0f), context
            )
            if (textSize != 0f) {
                centerText.textSize = textSize
            } else {
                centerText.textSize = 12f
            }
            disabledDrawable = typedArray.getDrawable(R.styleable.SwipeButton_button_image_disabled)
            enabledDrawable = typedArray.getDrawable(R.styleable.SwipeButton_button_image_enabled)
            val innerTextLeftPadding = typedArray.getDimension(
                R.styleable.SwipeButton_inner_text_left_padding, 0f
            )
            val innerTextTopPadding = typedArray.getDimension(
                R.styleable.SwipeButton_inner_text_top_padding, 0f
            )
            val innerTextRightPadding = typedArray.getDimension(
                R.styleable.SwipeButton_inner_text_right_padding, 0f
            )
            val innerTextBottomPadding = typedArray.getDimension(
                R.styleable.SwipeButton_inner_text_bottom_padding, 0f
            )
            val initialState = typedArray.getInt(R.styleable.SwipeButton_initial_state, DISABLED)
            if (initialState == ENABLED) {
                val layoutParamsButton = LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParamsButton.addRule(ALIGN_PARENT_LEFT, TRUE)
                layoutParamsButton.addRule(CENTER_VERTICAL, TRUE)
                swipeButtonInner!!.setImageDrawable(enabledDrawable)
                addView(swipeButtonInner, layoutParamsButton)
                isActive = true
            } else {
                val layoutParamsButton = LayoutParams(collapsedWidth, collapsedHeight)
                layoutParamsButton.addRule(ALIGN_PARENT_LEFT, TRUE)
                layoutParamsButton.addRule(CENTER_VERTICAL, TRUE)
                swipeButtonInner!!.setImageDrawable(disabledDrawable)
                addView(swipeButtonInner, layoutParamsButton)
                isActive = false
            }
            centerText.setPadding(innerTextLeftPadding.toInt(), innerTextTopPadding.toInt(), innerTextRightPadding.toInt(), innerTextBottomPadding.toInt())
            val buttonBackground = typedArray.getDrawable(R.styleable.SwipeButton_button_background)
            if (buttonBackground != null) {
                swipeButtonInner!!.background = buttonBackground
            } else {
                swipeButtonInner!!.background = ContextCompat.getDrawable(context, R.drawable.shape_button)
            }
            buttonLeftPadding = typedArray.getDimension(R.styleable.SwipeButton_button_left_padding, 0f)
            buttonTopPadding = typedArray.getDimension(R.styleable.SwipeButton_button_top_padding, 0f)
            buttonRightPadding = typedArray.getDimension(R.styleable.SwipeButton_button_right_padding, 0f)
            buttonBottomPadding = typedArray.getDimension(R.styleable.SwipeButton_button_bottom_padding, 0f)
            swipeButtonInner!!.setPadding(buttonLeftPadding.toInt(), buttonTopPadding.toInt(), buttonRightPadding.toInt(), buttonBottomPadding.toInt())
            hasActivationState = typedArray.getBoolean(R.styleable.SwipeButton_has_activate_state, true)
            typedArray.recycle()
        }
        setOnTouchListener(buttonTouchListener)
    }

    private val buttonTouchListener: OnTouchListener
        get() = object : OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> return !isTouchOutsideInitialPosition(event, swipeButtonInner as View)
                    MotionEvent.ACTION_MOVE -> {
                        if (initialX == 0f) {
                            initialX = swipeButtonInner!!.x
                        }
                        if (event.x > swipeButtonInner!!.width / 2 &&
                            event.x + swipeButtonInner!!.width / 2 < width
                        ) {
                            swipeButtonInner!!.x = event.x - swipeButtonInner!!.width / 2
                            centerText!!.alpha = 1 - 1.3f * (swipeButtonInner!!.x + swipeButtonInner!!.width) / width
                            setTrailingEffect()
                        }
                        if (event.x + swipeButtonInner!!.width / 2 > width &&
                            swipeButtonInner!!.x + swipeButtonInner!!.width / 2 < width
                        ) {
                            swipeButtonInner!!.x = (width - swipeButtonInner!!.width).toFloat()
                        }
                        if (event.x < swipeButtonInner!!.width / 2) {
                            swipeButtonInner!!.x = 0f
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isActive) {
                            collapseButton()
                        } else {
                            if (swipeButtonInner!!.x + swipeButtonInner!!.width > width * 0.9) {
                                if (hasActivationState) {
                                    expandButton()
                                } else if (onActiveListener != null) {
                                    onActiveListener?.onActive()
                                    moveButtonBack()
                                }
                            } else {
                                moveButtonBack()
                            }
                        }
                        return true
                    }
                }
                return false
            }
        }

    private fun expandButton() {
        val positionAnimator = ValueAnimator.ofFloat(swipeButtonInner!!.x, 0f)
        positionAnimator.addUpdateListener {
            val x = positionAnimator.animatedValue as Float
            swipeButtonInner!!.x = x
        }
        val widthAnimator = ValueAnimator.ofInt(
            swipeButtonInner!!.width,
            width
        )
        widthAnimator.addUpdateListener {
            val params = swipeButtonInner!!.layoutParams
            params.width = (widthAnimator.animatedValue as Int)
            swipeButtonInner!!.layoutParams = params
        }
        val animatorSet = AnimatorSet()
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                isActive = true
                swipeButtonInner!!.setImageDrawable(enabledDrawable)
                if (onStateChangeListener != null) {
                    onStateChangeListener?.onStateChange(isActive)
                }
                if (onActiveListener != null) {
                    onActiveListener?.onActive()
                }
            }
        })
        animatorSet.playTogether(positionAnimator, widthAnimator)
        animatorSet.start()
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun moveButtonBack() {
        val positionAnimator = ValueAnimator.ofFloat(swipeButtonInner!!.x, 0f)
        positionAnimator.interpolator = AccelerateDecelerateInterpolator()
        positionAnimator.addUpdateListener {
            val x = positionAnimator.animatedValue as Float
            swipeButtonInner!!.x = x
            setTrailingEffect()
        }
        positionAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (layer != null) {
                    layer!!.visibility = GONE
                }
            }
        })
        val objectAnimator = ObjectAnimator.ofFloat(
            /* target = */ centerText,
            /* propertyName = */ "alpha",
            /* ...values = */ 1f
        )
        positionAnimator.duration = 200
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(objectAnimator, positionAnimator)
        animatorSet.start()
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun collapseButton() {
        val finalWidth: Int = if (collapsedWidth == ViewGroup.LayoutParams.WRAP_CONTENT) {
            swipeButtonInner!!.height
        } else {
            collapsedWidth
        }
        val widthAnimator = ValueAnimator.ofInt(swipeButtonInner!!.width, finalWidth)
        widthAnimator.addUpdateListener {
            val params = swipeButtonInner!!.layoutParams
            params.width = (widthAnimator.animatedValue as Int)
            swipeButtonInner!!.layoutParams = params
            setTrailingEffect()
        }
        widthAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                isActive = false
                swipeButtonInner!!.setImageDrawable(disabledDrawable)
                if (onStateChangeListener != null) {
                    onStateChangeListener?.onStateChange(isActive)
                }
                if (layer != null) {
                    layer!!.visibility = GONE
                }
            }
        })
        val objectAnimator = ObjectAnimator.ofFloat(
            centerText, "alpha", 1f
        )
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(objectAnimator, widthAnimator)
        animatorSet.start()
    }

    fun setEnabledStateNotAnimated() {
        swipeButtonInner!!.x = 0f
        val params = swipeButtonInner!!.layoutParams
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        swipeButtonInner!!.layoutParams = params
        swipeButtonInner!!.setImageDrawable(enabledDrawable)
        isActive = true
        centerText!!.alpha = 0f
    }

    fun setDisabledStateNotAnimated() {
        val params = swipeButtonInner!!.layoutParams
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        collapsedWidth = ViewGroup.LayoutParams.WRAP_CONTENT
        swipeButtonInner!!.setImageDrawable(disabledDrawable)
        isActive = false
        centerText!!.alpha = 1f
        swipeButtonInner!!.setPadding(buttonLeftPadding.toInt(), buttonTopPadding.toInt(), buttonRightPadding.toInt(), buttonBottomPadding.toInt())
        swipeButtonInner!!.layoutParams = params
    }

    private fun setTrailingEffect() {
        if (trailEnabled) {
            layer!!.visibility = VISIBLE
            layer!!.layoutParams = LayoutParams((swipeButtonInner!!.x + swipeButtonInner!!.width / 3).toInt(), centerText!!.height)
        }
    }

    fun setTrailBackground(trailingDrawable: Drawable) {
        if (trailEnabled) {
            layer!!.background = trailingDrawable
        }
    }

    fun toggleState() {
        if (isActive) {
            collapseButton()
        } else {
            expandButton()
        }
    }

    fun setCenterTextColor(context: Context, color: Int) {
        centerText!!.setTextColor(context.color(color))
    }

    companion object {
        private const val ENABLED = 0
        private const val DISABLED = 1
    }
}

fun isTouchOutsideInitialPosition(event: MotionEvent, view: View): Boolean {
    return event.x > view.x + view.width
}

fun convertPixelsToSp(px: Float, context: Context): Float {
    return px / context.resources.displayMetrics.scaledDensity
}

interface OnStateChangeListener {
    fun onStateChange(active: Boolean)
}

interface OnActiveListener {
    fun onActive()
}

