package com.zero.chartview.extensions

import android.graphics.Paint

val Paint.textHeight get() = fontMetrics.bottom - fontMetrics.top + fontMetrics.leading
