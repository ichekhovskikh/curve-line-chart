package com.chekh.chartview.extensions

import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StyleableRes
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.annotation.Px

internal inline fun View.applyStyledAttributes(
    set: AttributeSet?,
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

internal inline var View.isInvisible
    get() = visibility == View.INVISIBLE
    set(value) {
        visibility = if (value) View.INVISIBLE else View.VISIBLE
    }

@get:Px
@setparam:Px
internal inline var View.marginTop: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin ?: 0
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin = value
    }

internal inline val View.classLoader: ClassLoader?
    get() = if (isInEditMode) this.javaClass.classLoader else context.classLoader
