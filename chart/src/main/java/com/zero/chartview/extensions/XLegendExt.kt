package com.zero.chartview.extensions

import android.graphics.Color
import androidx.annotation.ColorInt
import com.zero.chartview.model.AxisLine
import com.zero.chartview.model.XLegend

@ColorInt
internal fun XLegend.alphaColor(staticColor: Int): Int =
    Color.argb(alpha, Color.red(staticColor), Color.green(staticColor), Color.blue(staticColor))

internal fun List<XLegend>.toAxisLines() = map { legend ->
    AxisLine(
        position = legend.left + (legend.right - legend.left) / 2,
        alpha = legend.alpha
    )
}
