package com.zero.chartview.extensions

import android.graphics.Paint

internal val Paint.textHeight get() = fontMetrics.bottom - fontMetrics.top + fontMetrics.leading
