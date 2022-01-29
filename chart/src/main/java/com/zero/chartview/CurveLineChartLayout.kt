package com.zero.chartview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.zero.chartview.extensions.getColorCompat
import com.zero.chartview.selector.CurveLineSelectorView

class CurveLineChartLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var chart: CurveLineChartView? = null
    private var selector: CurveLineSelectorView? = null

    init {
        orientation = VERTICAL
        super.setBackgroundColor(context.getColorCompat(android.R.color.transparent))
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
