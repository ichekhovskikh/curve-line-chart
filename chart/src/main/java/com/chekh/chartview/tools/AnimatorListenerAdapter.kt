package com.chekh.chartview.tools

import android.animation.Animator

internal open class AnimatorListenerAdapter : Animator.AnimatorListener {
    override fun onAnimationRepeat(animation: Animator?) = Unit
    override fun onAnimationEnd(animation: Animator?) = Unit
    override fun onAnimationCancel(animation: Animator?) = Unit
    override fun onAnimationStart(animation: Animator?) = Unit
}
