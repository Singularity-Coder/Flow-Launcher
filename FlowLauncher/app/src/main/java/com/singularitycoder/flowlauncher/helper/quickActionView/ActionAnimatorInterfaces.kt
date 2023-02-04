package com.singularitycoder.flowlauncher.helper.quickActionView

import android.graphics.Point
import android.view.View


/**
 * Custom animations for QuickActionView animating in
 */
interface ActionsInAnimator {
    /**
     * Animate in the action view within the QuickActionView
     *
     * @param action the action
     * @param index  the index of the action in the list of actions
     * @param view   the action view to animate
     * @param center the final resting center point of the Action
     */
    fun animateActionIn(action: Action?, index: Int, view: ActionView?, center: Point?)

    /**
     * Animate in the indicator as the QuickActionView shows
     *
     * @param indicator the indicator view
     */
    fun animateIndicatorIn(indicator: View?)

    /**
     * Animate in the scrim as the QuickActionView shows
     *
     * @param scrim the scrim view
     */
    fun animateScrimIn(scrim: View?)
}

/**
 * Custom animations for QuickActionView animating out
 */
interface ActionsOutAnimator {
    /**
     * Animate the action view as the QuickActionView dismisses
     *
     * @param action The action being animated
     * @param index  The position of the actionview in its parent
     * @param view   The action view
     * @param center The center of the indicator
     * @return The duration of this animation, in milliseconds
     */
    fun animateActionOut(action: Action?, index: Int, view: ActionView?, center: Point?): Int

    /**
     * Animate the indicator view as the QuickActionView dismisses
     *
     * @param indicator The indicator view
     * @return The duration of this animation, in milliseconds
     */
    fun animateIndicatorOut(indicator: View?): Int

    /**
     * Animate the scrim as the QuickActionView dismisses
     *
     * @param scrim The scrimView to animate
     * @return The duration of this animation, in milliseconds
     */
    fun animateScrimOut(scrim: View?): Int
}

/**
 * Custom animations for an [Action] label animating in
 */
interface ActionsTitleInAnimator {
    /**
     * Animate the action title view as the QuickActionView action title appears
     *
     * @param action The action being animated
     * @param index  The position of the action in its parent
     * @param view   The action title view
     */
    fun animateActionTitleIn(action: Action?, index: Int, view: View?)
}

/**
 * Custom animations for an [Action] label animating in
 */
interface ActionsTitleOutAnimator {
    /**
     * Animate the action title view as the QuickActionView action title disappears
     *
     * @param action The action being animated
     * @param index  The position of the action in its parent
     * @param view   The action title view
     * @return The duration of this animation, in milliseconds, so that the view can be properly
     * hidden when the animation completes
     */
    fun animateActionTitleOut(action: Action?, index: Int, view: View?): Int
}
