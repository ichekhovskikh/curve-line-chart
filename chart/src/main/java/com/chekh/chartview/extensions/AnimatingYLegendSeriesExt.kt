package com.chekh.chartview.extensions

import android.graphics.Color
import androidx.annotation.ColorInt
import com.chekh.chartview.model.AnimatingYLegendSeries
import com.chekh.chartview.model.AxisLine

internal fun AnimatingYLegendSeries.setDisappearing() {
    if (animationValue == 1f) {
        animationValue = 0f
    }
    isAppearing = false
}

@ColorInt
internal fun AnimatingYLegendSeries.animatingColor(staticColor: Int): Int =
    Color.argb(alpha, Color.red(staticColor), Color.green(staticColor), Color.blue(staticColor))

private val AnimatingYLegendSeries.alpha
    get() = (255 * if (isAppearing) animationValue else 1 - animationValue).toInt()

internal fun List<AnimatingYLegendSeries>.toAxisLines() = flatMap { series ->
    series.legends.map { legend ->
        AxisLine(legend.yDrawPixel, series.alpha)
    }
}
