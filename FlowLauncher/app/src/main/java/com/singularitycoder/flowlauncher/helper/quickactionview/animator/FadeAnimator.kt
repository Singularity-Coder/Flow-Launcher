package com.singularitycoder.flowlauncher.helper.quickactionview.animator

import android.graphics.Point
import android.view.View
import android.view.animation.LinearInterpolator
import com.singularitycoder.flowlauncher.helper.quickactionview.Action
import com.singularitycoder.flowlauncher.helper.quickactionview.ActionView
import com.singularitycoder.flowlauncher.helper.quickactionview.ActionsInAnimator
import com.singularitycoder.flowlauncher.helper.quickactionview.ActionsOutAnimator

/**
 * Fades in the quick actions
 */
class FadeAnimator : ActionsInAnimator, ActionsOutAnimator {
    private val mInterpolator = LinearInterpolator()
    override fun animateActionIn(action: Action?, index: Int, view: ActionView?, center: Point?) {
        view!!.animate()
                .alpha(1.0f)
                .setDuration(100).interpolator = mInterpolator
    }

    override fun animateIndicatorIn(indicator: View?) {
        indicator!!.alpha = 0f
        indicator.animate().alpha(1f).duration = 100
    }

    override fun animateScrimIn(scrim: View?) {
        scrim!!.alpha = 0f
        scrim.animate().alpha(1f).duration = 100
    }

    override fun animateActionOut(action: Action?, position: Int, view: ActionView?, center: Point?): Int {
        view!!.animate().scaleX(0.1f)
                .scaleY(0.1f)
                .alpha(0.0f)
                .setStartDelay(0).duration = 100
        return 100
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