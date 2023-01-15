package com.singularitycoder.flowlauncher.helper.swipebutton

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.helper.onSafeClick


/**
 * Created by leandroferreira on 07/03/17.
 */
class ToggleButton : RelativeLayout {
    private var isClicked: Boolean = false
    private var mSwipeButton: Button? = null
    private var initialButtonWidth: Int = 0
    private var animationButtonWidth: Int = 0

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context)
    }

    private fun init(context: Context) {
        isClicked = false
        mSwipeButton = Button(context)
        mSwipeButton!!.text = null
        mSwipeButton!!.background = ContextCompat.getDrawable(context, R.drawable.shape_button)
        val layoutParamsButton: LayoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        layoutParamsButton.addRule(ALIGN_PARENT_LEFT, TRUE)
        layoutParamsButton.addRule(CENTER_VERTICAL, TRUE)
        addView(mSwipeButton, layoutParamsButton)
        val clickListener: OnClickListener = object : OnClickListener {
            override fun onClick(v: View) {
                if (!isClicked) {
                    animateCheck()
                    isClicked = true
                } else {
                    animateUncheck()
                    isClicked = false
                }
            }
        }
        onSafeClick {
            clickListener.onClick(it.first)
        }
        mSwipeButton!!.onSafeClick {
            clickListener.onClick(it.first)
        }
    }

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        initialButtonWidth = mSwipeButton!!.width
        animationButtonWidth = width
    }

    private fun animateCheck() {
        val expandAnimator: ValueAnimator = ValueAnimator.ofInt(initialButtonWidth, animationButtonWidth)
        expandAnimator.addUpdateListener(object : AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator) {
                mSwipeButton!!.width = (animation.animatedValue as Int?)!!
            }
        })
        expandAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                gravity = Gravity.LEFT
            }

            override fun onAnimationEnd(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        val shirinkAnimator: ValueAnimator = ValueAnimator.ofInt(animationButtonWidth, initialButtonWidth)
        shirinkAnimator.addUpdateListener(object : AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator) {
                mSwipeButton!!.width = (animation.animatedValue as Int?)!!
            }
        })
        shirinkAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                gravity = Gravity.RIGHT
            }

            override fun onAnimationEnd(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        val animatorSet: AnimatorSet = AnimatorSet()
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.playSequentially(expandAnimator, shirinkAnimator)
        animatorSet.addListener(clickableListener)
        animatorSet.start()
    }

    private fun animateUncheck() {
        val expandAnimator: ValueAnimator = ValueAnimator.ofInt(initialButtonWidth, animationButtonWidth)
        expandAnimator.addUpdateListener(object : AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator) {
                mSwipeButton!!.width = (animation.animatedValue as Int?)!!
            }
        })
        expandAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                gravity = Gravity.RIGHT
            }

            override fun onAnimationEnd(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        val shirinkAnimator: ValueAnimator = ValueAnimator.ofInt(animationButtonWidth, initialButtonWidth)
        shirinkAnimator.addUpdateListener(object : AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator) {
                mSwipeButton!!.width = (animation.animatedValue as Int?)!!
            }
        })
        shirinkAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                gravity = Gravity.LEFT
            }

            override fun onAnimationEnd(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        val animatorSet: AnimatorSet = AnimatorSet()
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.playSequentially(expandAnimator, shirinkAnimator)
        animatorSet.addListener(clickableListener)
        animatorSet.start()
    }

    private val clickableListener: Animator.AnimatorListener
        private get() = object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                isClickable = false
                mSwipeButton!!.isClickable = false
            }

            override fun onAnimationEnd(animation: Animator) {
                isClickable = true
                mSwipeButton!!.isClickable = true
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        }
}