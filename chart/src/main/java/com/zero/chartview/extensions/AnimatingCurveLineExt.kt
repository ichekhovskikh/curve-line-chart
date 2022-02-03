package com.zero.chartview.extensions

import android.graphics.Color
import androidx.annotation.ColorInt
import com.zero.chartview.model.AnimatingCurveLine

internal fun AnimatingCurveLine.setDisappearing() {
    if (animationValue == 1f) {
        animationValue = 0f
    }
    isAppearing = false
}

@get:ColorInt
internal val AnimatingCurveLine.animatingColor: Int
    get() {
        val color = curveLine.color
        return Color.argb(
            alpha,
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
    }

private val AnimatingCurveLine.alpha
    get() = (255 * if (isAppearing) animationValue else 1 - animationValue).toInt()
