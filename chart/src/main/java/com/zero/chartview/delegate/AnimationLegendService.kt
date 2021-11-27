package com.zero.chartview.delegate

import com.zero.chartview.anim.TensionAnimator
import com.zero.chartview.model.AnimatingLegendSeries
import kotlin.math.max

internal class AnimationLegendService(
    var onUpdate: (() -> Unit)? = null
) {

    var maxY = 0F
        private set
    var minY = 0F
        private set

    internal var legendSeries: MutableList<AnimatingLegendSeries> = mutableListOf()
        private set

    private val tensionAnimator = TensionAnimator { tension, minY, maxY ->
        this.minY = minY
        this.maxY = maxY
        legendSeries.forEach { series ->
            series.animationValue = max(series.animationValue, tension)
        }
        onUpdate?.invoke()
    }.doOnEnd(::removeDisappearingLegendSeries)

    private fun removeDisappearingLegendSeries() {
        legendSeries.removeAll { !it.isAppearing }
    }

    fun setYAxis(minY: Float, maxY: Float) {
        if (this.maxY == maxY && this.minY == minY) return
        tensionAnimator.reStart(
            fromTension = 0.8f,
            toTension = 1f,
            fromMin = this.minY,
            toMin = minY,
            fromMax = this.maxY,
            toMax = maxY
        )
    }
}