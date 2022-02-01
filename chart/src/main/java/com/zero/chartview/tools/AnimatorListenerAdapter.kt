package com.zero.chartview.tools

import android.animation.Animator

internal open class AnimatorListenerAdapter : Animator.AnimatorListener {

    override fun onAnimationRepeat(animation: Animator?) {}

    override fun onAnimationEnd(animation: Animator?) { }

    override fun onAnimationCancel(animation: Animator?) { }

    override fun onAnimationStart(animation: Animator?) { }
}
