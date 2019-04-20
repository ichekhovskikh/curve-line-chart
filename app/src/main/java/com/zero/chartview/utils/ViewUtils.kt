package com.zero.chartview.utils

import com.zero.chartview.App

fun dpToPx(dp: Int): Int {
    val displayMetrics = App.context.resources.displayMetrics
    return ((dp * displayMetrics.density) + 0.5).toInt()
}