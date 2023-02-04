package com.singularitycoder.flowlauncher.helper.pinterestView

import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Interpolator
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.core.view.ViewPropertyAnimatorUpdateListener
import java.lang.ref.WeakReference

/**
 * Created by florentchampigny on 19/04/2016.
 */
class DurX(var view: View?) {

    companion object {
        fun putOn(view: View?): DurX {
            return DurX(view)
        }
    }

    fun andPutOn(view: View?): DurX {
        this.view = view
        return this
    }

    fun waitForSize(sizeListener: Listeners.Size?) {
        view!!.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (view != null) {
                    view!!.viewTreeObserver.removeOnPreDrawListener(this)
                    sizeListener?.onSize(this@DurX)
                }
                return false
            }
        })
    }

    val y: Float
        get() {
            val rect = Rect()
            view!!.getGlobalVisibleRect(rect)
            return rect.top.toFloat()
        }

    /**
     * 获取当前view的x坐标  实际值= translationX + getLeft
     * @return
     */
    val x: Float
        get() = ViewCompat.getX(view)

    fun alpha(alpha: Float): DurX {
        if (view != null) {
            ViewCompat.setAlpha(view, alpha)
        }
        return this
    }

    fun scaleX(scale: Float): DurX {
        if (view != null) {
            ViewCompat.setScaleX(view, scale)
        }
        return this
    }

    fun scaleY(scale: Float): DurX {
        if (view != null) {
            ViewCompat.setScaleY(view, scale)
        }
        return this
    }

    fun scale(scale: Float): DurX {
        if (view != null) {
            ViewCompat.setScaleX(view, scale)
            ViewCompat.setScaleY(view, scale)
        }
        return this
    }

    fun translationX(translation: Float): DurX {
        if (view != null) {
            ViewCompat.setTranslationX(view, translation)
        }
        return this
    }

    fun translationY(translation: Float): DurX {
        if (view != null) {
            ViewCompat.setTranslationY(view, translation)
        }
        return this
    }

    fun translation(translationX: Float, translationY: Float): DurX {
        if (view != null) {
            ViewCompat.setTranslationX(view, translationX)
            ViewCompat.setTranslationY(view, translationY)
        }
        return this
    }

    fun pivotX(percent: Float): DurX {
        if (view != null) {
            ViewCompat.setPivotX(view, view!!.width * percent)
        }
        return this
    }

    fun pivotY(percent: Float): DurX {
        if (view != null) {
            ViewCompat.setPivotY(view, view!!.height * percent)
        }
        return this
    }

    fun visible(): DurX {
        if (view != null) {
            view!!.visibility = View.VISIBLE
        }
        return this
    }

    fun invisible(): DurX {
        if (view != null) {
            view!!.visibility = View.INVISIBLE
        }
        return this
    }

    fun gone(): DurX {
        if (view != null) {
            view!!.visibility = View.GONE
        }
        return this
    }

    fun animate(): DurXAnimator {
        return DurXAnimator(this)
    }

    internal class DurXAnimatorListener(durXAnimator: DurXAnimator) : ViewPropertyAnimatorListener {
        var reference: WeakReference<DurXAnimator>

        init {
            reference = WeakReference(durXAnimator)
        }

        override fun onAnimationStart(view: View) {
            val durXAnimator = reference.get()
            if (durXAnimator?.startListener != null) {
                val startListener = durXAnimator.startListener!!.get()
                startListener?.onStart()
            }
        }

        override fun onAnimationEnd(view: View) {
            val durXAnimator = reference.get()
            if (durXAnimator?.endListener != null) {
                val endListener = durXAnimator.endListener!!.get()
                endListener?.onEnd()
            }
        }

        override fun onAnimationCancel(view: View) {}
    }

    internal class DurXAnimatorUpdate(durXAnimator: DurXAnimator) : ViewPropertyAnimatorUpdateListener {
        var reference: WeakReference<DurXAnimator>

        init {
            reference = WeakReference(durXAnimator)
        }

        override fun onAnimationUpdate(view: View) {
            val durXAnimator = reference.get()
            if (durXAnimator?.updateListener != null) {
                val updateListener = durXAnimator.updateListener!!.get()
                updateListener?.update()
            }
        }
    }

    class DurXAnimator internal constructor(durX: DurX) {
        val animator: ViewPropertyAnimatorCompat
        val durX: DurX

        var startListener: WeakReference<Listeners.Start>? = null
        var endListener: WeakReference<Listeners.End>? = null
        var updateListener: WeakReference<Listeners.Update>? = null

        init {
            animator = ViewCompat.animate(durX.view!!)
            this.durX = durX
            animator.setListener(DurXAnimatorListener(this))
        }

        fun alpha(alpha: Float): DurXAnimator {
            animator.alpha(alpha)
            return this
        }

        fun alpha(from: Float, to: Float): DurXAnimator {
            durX.alpha(from)
            return alpha(to)
        }

        fun scaleX(scale: Float): DurXAnimator {
            animator.scaleX(scale)
            return this
        }

        fun scaleX(from: Float, to: Float): DurXAnimator {
            durX.scaleX(from)
            return scaleX(to)
        }

        fun scaleY(scale: Float): DurXAnimator {
            animator.scaleY(scale)
            return this
        }

        fun scaleY(from: Float, to: Float): DurXAnimator {
            durX.scaleY(from)
            return scaleY(to)
        }

        fun scale(scale: Float): DurXAnimator {
            animator.scaleX(scale)
            animator.scaleY(scale)
            return this
        }

        fun scale(from: Float, to: Float): DurXAnimator {
            durX.scale(from)
            return scale(to)
        }

        fun translationX(translation: Float): DurXAnimator {
            animator.translationX(translation)
            return this
        }

        fun translationX(from: Float, to: Float): DurXAnimator {
            durX.translationX(from)
            return translationX(to)
        }

        fun translationY(translation: Float): DurXAnimator {
            animator.translationY(translation)
            return this
        }

        fun translationY(from: Float, to: Float): DurXAnimator {
            durX.translationY(from)
            return translationY(to)
        }

        fun translation(translationX: Float, translationY: Float): DurXAnimator {
            animator.translationX(translationX)
            animator.translationY(translationY)
            return this
        }

        fun rotation(rotation: Float): DurXAnimator {
            animator.rotation(rotation)
            return this
        }

        fun duration(duration: Long): DurXAnimator {
            animator.duration = duration
            return this
        }

        fun startDelay(duration: Long): DurXAnimator {
            animator.startDelay = duration
            return this
        }

        fun interpolator(interpolator: Interpolator?): DurXAnimator {
            animator.interpolator = interpolator
            return this
        }

        fun end(listener: Listeners.End): DurXAnimator {
            endListener = WeakReference(listener)
            return this
        }

        fun update(listener: Listeners.Update): DurXAnimator {
            updateListener = WeakReference(listener)
            animator.setUpdateListener(DurXAnimatorUpdate(this))
            return this
        }

        fun start(listener: Listeners.Start): DurXAnimator {
            startListener = WeakReference(listener)
            return this
        }

        fun pullOut(): DurX {
            return durX
        }

        fun thenAnimate(view: View?): DurXAnimator {
            val durX = DurX(view)
            val durXAnimator = durX.animate()
            durXAnimator.startDelay(animator.startDelay + animator.duration)
            return durXAnimator
        }

        fun andAnimate(view: View?): DurXAnimator {
            val durX = DurX(view)
            val durXAnimator = durX.animate()
            durXAnimator.startDelay(animator.startDelay)
            return durX.animate()
        }
    }

    class Listeners {
        interface End {
            fun onEnd()
        }

        interface Start {
            fun onStart()
        }

        interface Size {
            fun onSize(durX: DurX?)
        }

        interface Update {
            fun update()
        }
    }
}