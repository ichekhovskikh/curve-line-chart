package com.zero.chartview.service

import com.zero.chartview.anim.AppearanceAnimator
import com.zero.chartview.anim.TensionAnimator
import com.zero.chartview.model.AnimatingCurveLine
import com.zero.chartview.model.CurveLine

internal class AnimationLineService(
    var onInvalidate: (() -> Unit)? = null
) {

    var maxY = 0F
        private set
    var minY = 0F
        private set

    internal var animationLines: MutableList<AnimatingCurveLine> = mutableListOf()
        private set

    private val appearanceAnimator = AppearanceAnimator { value ->
        animationLines.forEach { line ->
            if (line.animationValue <= value) {
                line.animationValue = value
                onInvalidate?.invoke()
            }
        }
    }.apply {
        doOnEnd { animationLines.removeAll { !it.isAppearing } }
    }

    private val tensionAnimator = TensionAnimator { _, minY, maxY ->
        this.minY = minY
        this.maxY = maxY
        onInvalidate?.invoke()
    }

    fun getLines() = animationLines.map { it.curveLine }

    fun setLines(newLines: List<CurveLine>) {
        val current = animationLines.map { it.curveLine }

        if (newLines == current) return

        appearanceAnimator.cancel()
        val appearing = newLines.minus(current)
        appearing.forEach { line ->
            animationLines.add(AnimatingCurveLine(line, true, 0F))
        }

        val disappearing = current.minus(newLines)
        disappearing.forEach { line ->
            animationLines.find { it.curveLine == line }
                ?.apply {
                    isAppearing = false
                    animationValue = 0f
                }
        }
        appearanceAnimator.start()
    }

    fun addLine(line: CurveLine) {
        val current = animationLines.map { it.curveLine }
        if (current.contains(line)) return

        appearanceAnimator.cancel()
        animationLines.add(AnimatingCurveLine(line, true, 0F))
        appearanceAnimator.start()
    }

    fun removeLine(line: CurveLine) {
        val current = animationLines.map { it.curveLine }
        if (!current.contains(line)) return

        appearanceAnimator.cancel()
        animationLines.find { it.curveLine == line }
            ?.apply {
                isAppearing = false
                animationValue = 0f
            }
        appearanceAnimator.start()
    }

    fun setYAxis(minY: Float, maxY: Float) {
        if (this.maxY == maxY && this.minY == minY) return
        tensionAnimator.reStart(
            fromMin = this.minY,
            toMin = minY,
            fromMax = this.maxY,
            toMax = maxY
        )
    }
}