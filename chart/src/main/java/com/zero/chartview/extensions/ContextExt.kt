package com.zero.chartview.extensions

import android.content.Context
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat

internal fun Context.getColorCompat(@ColorRes id: Int) = ContextCompat.getColor(this, id)