package com.chekh.chartview.extensions

import com.chekh.chartview.model.Size

internal infix fun Int.on(height: Int) = Size(this, height)
