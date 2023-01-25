package com.singularitycoder.flowlauncher.helper.quickactionview.animator

import android.view.View
import com.singularitycoder.flowlauncher.helper.quickactionview.Action
import com.singularitycoder.flowlauncher.helper.quickactionview.ActionsTitleInAnimator
import com.singularitycoder.flowlauncher.helper.quickactionview.ActionsTitleOutAnimator

/**
 * Default animator which animates the action title in and out
 */
class FadeInFadeOutActionsTitleAnimator @JvmOverloads constructor(  //ms
        private val mDuration: Int = 50) : ActionsTitleInAnimator, ActionsTitleOutAnimator {
    override fun animateActionTitleIn(action: Action?, index: Int, view: View?) {
        view!!.alpha = 0.0f
        view.animate().alpha(1.0f).duration = mDuration.toLong()
    }

    override fun animateActionTitleOut(action: Action?, index: Int, view: View?): Int {
        view!!.animate().alpha(0.0f).duration = mDuration.toLong()
        return mDuration
    }
}