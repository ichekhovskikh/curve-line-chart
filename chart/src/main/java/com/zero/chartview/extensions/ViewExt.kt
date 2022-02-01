package com.zero.chartview.extensions

import android.content.res.TypedArray
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import android.util.AttributeSet
import android.view.View

internal fun View.applyStyledAttributes(
    set: AttributeSet,
    @StyleableRes attrs: IntArray,
    @AttrRes defStyleAttr: Int,
    @StyleRes defStyleRes: Int,
    closure: TypedArray.() -> Unit
) {
    context.theme.obtainStyledAttributes(
        set,
        attrs,
        defStyleAttr,
        defStyleRes
    ).apply {
        closure(this)
        recycle()
    }
}

internal var View.isInvisible
    get() = visibility == View.INVISIBLE
    set(value) {
        visibility = if (value) View.INVISIBLE else View.VISIBLE
    }

internal val View.classLoader: ClassLoader?
    get() = if (isInEditMode) this.javaClass.classLoader else context.classLoader
