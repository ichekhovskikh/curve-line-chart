package com.zero.chartview.popup

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.zero.chartview.model.IntersectionPoint

abstract class PopupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    abstract fun bind(intersections: List<IntersectionPoint>)
}
