package com.singularitycoder.flowlauncher.helper.quickActionView.animator

import android.view.View
import com.singularitycoder.flowlauncher.helper.quickActionView.Action
import com.singularitycoder.flowlauncher.helper.quickActionView.ActionsTitleInAnimator
import com.singularitycoder.flowlauncher.helper.quickActionView.ActionsTitleOutAnimator

/**
 * Default animator which animates the action title in and out
 */
class FadeInFadeOutActionsTitleAnimator @JvmOverloads constructor(  //ms
        private val mDuration: Int = 20) : ActionsTitleInAnimator, ActionsTitleOutAnimator {
    override fun animateActionTitleIn(action: Action?, index: Int, view: View?) {
        view!!.alpha = 0.0f
        view.animate().alpha(1.0f).duration = mDuration.toLong()
    }

    override fun animateActionTitleOut(action: Action?, index: Int, view: View?): Int {
        view!!.animate().alpha(0.0f).duration = mDuration.toLong()
        return mDuration
    }
}