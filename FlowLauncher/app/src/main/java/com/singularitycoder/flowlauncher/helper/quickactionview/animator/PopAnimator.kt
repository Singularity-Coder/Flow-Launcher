package com.singularitycoder.flowlauncher.helper.quickactionview.animator

import android.graphics.Point
import android.view.View
import android.view.animation.OvershootInterpolator
import com.singularitycoder.flowlauncher.helper.quickactionview.Action
import com.singularitycoder.flowlauncher.helper.quickactionview.ActionView
import com.singularitycoder.flowlauncher.helper.quickactionview.ActionsInAnimator
import com.singularitycoder.flowlauncher.helper.quickactionview.ActionsOutAnimator

/**
 * Animator where actions pop in
 */
class PopAnimator @JvmOverloads constructor(private val mStaggered: Boolean = false) : ActionsInAnimator, ActionsOutAnimator {
    private val mOvershootInterpolator = OvershootInterpolator()
    override fun animateActionIn(action: Action?, index: Int, view: ActionView?, center: Point?) {
        view!!.scaleX = 0.01f
        view.scaleY = 0.01f
        val viewPropertyAnimator = view.animate().scaleY(1.0f)
                .scaleX(1.0f)
                .setDuration(50)
                .setInterpolator(mOvershootInterpolator)
        if (mStaggered) {
            viewPropertyAnimator.startDelay = (index * 50).toLong()
        }
    }

    override fun animateIndicatorIn(indicator: View?) {
        indicator!!.alpha = 0f
        indicator.animate().alpha(1f).duration = 50
    }

    override fun animateScrimIn(scrim: View?) {
        scrim!!.alpha = 0f
        scrim.animate().alpha(1f).duration = 50
    }

    override fun animateActionOut(action: Action?, position: Int, view: ActionView?, center: Point?): Int {
        view!!.animate().scaleX(0.01f)
                .scaleY(0.01f)
                .alpha(0.0f)
                .setStartDelay(0).duration = 50
        return 50
    }

    override fun animateIndicatorOut(indicator: View?): Int {
        indicator!!.animate().alpha(0f).duration = 50
        return 50
    }

    override fun animateScrimOut(scrim: View?): Int {
        scrim!!.animate().alpha(0f).duration = 50
        return 50
    }
}