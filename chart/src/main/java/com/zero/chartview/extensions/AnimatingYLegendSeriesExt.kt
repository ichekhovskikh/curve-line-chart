package com.zero.chartview.extensions

import android.graphics.Color
import com.zero.chartview.model.AnimatingYLegendSeries

internal fun AnimatingYLegendSeries.setDisappearing() {
    if (animationValue == 1f) {
        animationValue = 0f
    }
    isAppearing = false
}

internal fun AnimatingYLegendSeries.animatingColor(staticColor: Int): Int =
    Color.argb(alpha, Color.red(staticColor), Color.green(staticColor), Color.blue(staticColor))

private val AnimatingYLegendSeries.alpha
    get() = (255 * if (isAppearing) animationValue else 1 - animationValue).toInt()
