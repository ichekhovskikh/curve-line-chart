package com.zero.chartview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.zero.chartview.selector.CurveLineSelectorView

class CurveLineChartLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var chart: CurveLineChartView? = null
    private var selector: CurveLineSelectorView? = null

    init {
        orientation = VERTICAL
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onFinishInflate() {
        super.onFinishInflate()
        for (index in 0 until childCount) {
            when (val child = getChildAt(index)) {
                is CurveLineChartView -> chart = child
                is CurveLineSelectorView -> selector = child
            }
        }
        chart?.addOnLinesChangedListener { lines ->
            selector?.setLines(lines)
        }
        var ignoreSelectorChanged = false
        selector?.addOnRangeChangedListener { start, endInclusive, smoothScroll ->
            if (!ignoreSelectorChanged) {
                chart?.setRange(start, endInclusive, smoothScroll)
            }
            ignoreSelectorChanged = false
        }
        chart?.addOnRangeChangedListener { start, endInclusive, smoothScroll ->
            ignoreSelectorChanged = true
            selector?.setRange(start, endInclusive, smoothScroll)
        }
    }
}
