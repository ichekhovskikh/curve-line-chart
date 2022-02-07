package com.chekh.chartview.extensions

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

internal fun Context.getColorCompat(@ColorRes id: Int) = ContextCompat.getColor(this, id)

internal fun Context.getFullClassName(className: String): String =
    if (className.firstOrNull() == '.') packageName + className else className
