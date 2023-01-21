package com.singularitycoder.flowlauncher.helper.quickactionview.animator

import android.graphics.Point
import android.view.View
import android.view.animation.OvershootInterpolator
import com.singularitycoder.flowlauncher.helper.quickactionview.Action
import com.singularitycoder.flowlauncher.helper.quickactionview.ActionView
import com.singularitycoder.flowlauncher.helper.quickactionview.ActionsInAnimator
import com.singularitycoder.flowlauncher.helper.quickactionview.ActionsOutAnimator

/**
 * Animator that slides actions out from the center point. This is the default animation
 * a QuickActionView uses
 */
class SlideFromCenterAnimator @JvmOverloads constructor(private val mStaggered: Boolean = false) : ActionsInAnimator, ActionsOutAnimator {
    private val mOvershootInterpolator = OvershootInterpolator()
    override fun animateActionIn(action: Action?, index: Int, view: ActionView?, center: Point?) {
        val actionCenter = view!!.circleCenterPoint
        actionCenter.offset(view.left, view.top)
        view.translationY = (center!!.y - actionCenter.y).toFloat()
        view.translationX = (center.x - actionCenter.x).toFloat()
        val viewPropertyAnimator = view.animate()
                .translationX(0f)
                .translationY(0f)
                .setInterpolator(mOvershootInterpolator)
                .setDuration(150)
        if (mStaggered) {
            viewPropertyAnimator.startDelay = (index * 100).toLong()
        }
    }

    override fun animateIndicatorIn(indicator: View?) {
        indicator!!.alpha = 0f
        indicator.animate().alpha(1f).duration = 100
    }

    override fun animateScrimIn(scrim: View?) {
        scrim!!.alpha = 0f
        scrim.animate().alpha(1f).duration = 100
    }

    override fun animateActionOut(action: Action?, index: Int, view: ActionView?, center: Point?): Int {
        val actionCenter = view!!.circleCenterPoint
        actionCenter.offset(view.left, view.top)
        val translateViewPropertyAnimator = view.animate()
                .translationY((center!!.y - actionCenter.y).toFloat())
                .translationX((center.x - actionCenter.x).toFloat())
                .setInterpolator(mOvershootInterpolator)
                .setStartDelay(0)
                .setDuration(100)
        val alphaViewPropertyAnimator = view.animate()
                .alpha(0f)
                .setStartDelay(0)
                .setDuration(100)
        if (mStaggered) {
            translateViewPropertyAnimator.startDelay = (index * 100).toLong()
            alphaViewPropertyAnimator.startDelay = (index * 100).toLong()
        }
        return index * 100 + 100
    }

    override fun animateIndicatorOut(indicator: View?): Int {
        indicator!!.animate().alpha(0f).duration = 100
        return 100
    }

    override fun animateScrimOut(scrim: View?): Int {
        scrim!!.animate().alpha(0f).duration = 100
        return 100
    }
}