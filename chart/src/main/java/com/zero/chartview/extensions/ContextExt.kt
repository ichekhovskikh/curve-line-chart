package com.zero.chartview.extensions

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

internal fun Context.getColorCompat(@ColorRes id: Int) = ContextCompat.getColor(this, id)
