package com.zero.chartview.extensions

import com.zero.chartview.model.Size

internal infix fun Int.on(height: Int) = Size(this, height)
