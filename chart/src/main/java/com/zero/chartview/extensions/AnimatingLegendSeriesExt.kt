package com.zero.chartview.extensions

import android.graphics.Color
import com.zero.chartview.model.AnimatingLegendSeries

internal fun AnimatingLegendSeries.animatingColor(staticColor: Int): Int =
    Color.argb(alpha, Color.red(staticColor), Color.green(staticColor), Color.blue(staticColor))

private val AnimatingLegendSeries.alpha
    get() = (255 * if (isAppearing) animationValue else 1 - animationValue).toInt()