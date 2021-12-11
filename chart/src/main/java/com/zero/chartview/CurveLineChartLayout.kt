package com.zero.chartview

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import com.zero.chartview.extensions.applyStyledAttributes
import com.zero.chartview.model.CurveLine
import com.zero.chartview.selector.CurveLineSelectorView

class CurveLineChartLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes), Themeable {

    private var chart: CurveLineChartView? = null
    private var selector: CurveLineSelectorView? = null

    private lateinit var chartColors: Themeable.ChartColors

    init {
        orientation = VERTICAL
        super.setBackgroundColor(resources.getColor(android.R.color.transparent))

        applyStyledAttributes(attrs, R.styleable.ChartLayout, defStyleAttr, defStyleRes) {
            setChartColors(getThemeColorDefault(this))
        }
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

    fun setRange(start: Float, endInclusive: Float, smoothScroll: Boolean = false) {
        selector?.setRange(start, endInclusive, smoothScroll)
    }

    fun getLines() = chart?.getLines()

    fun setLines(lines: List<CurveLine>, correspondingLegends: Map<Float, String>? = null) {
        chart?.setLines(lines, correspondingLegends)
    }

    fun addLine(line: CurveLine, correspondingLegends: Map<Float, String>? = null) {
        chart?.addLine(line, correspondingLegends)
    }

    fun removeLine(index: Int) {
        chart?.removeLine(index)
    }

    fun removeLine(line: CurveLine) {
        chart?.removeLine(line)
    }

    override fun getChartColors(): Themeable.ChartColors {
        val chartColors = chart?.getChartColors()
        val controlColors = selector?.getChartColors()
        chartColors?.apply {
            this@CurveLineChartLayout.chartColors.colorLegend = colorLegend
            this@CurveLineChartLayout.chartColors.colorGrid = colorGrid
            this@CurveLineChartLayout.chartColors.colorPopupLine = colorPopupLine
        }
        controlColors?.apply {
            this@CurveLineChartLayout.chartColors.colorFrameSelector = colorFrameSelector
            this@CurveLineChartLayout.chartColors.colorFogSelector = colorFogSelector
        }
        return this.chartColors
    }

    override fun setChartColors(colors: Themeable.ChartColors) {
        chartColors = colors
        chart?.setChartColors(colors)
        selector?.setChartColors(colors)
        setBackgroundColor(colors.colorBackground)
    }

    private fun getThemeColorDefault(typedArray: TypedArray): Themeable.ChartColors {
        typedArray.apply {
            val colorBackground =
                getColor(R.styleable.ChartLayout_colorBackground, resources.getColor(R.color.colorBackground))
            return Themeable.ChartColors(colorBackground = colorBackground)
        }
    }
}