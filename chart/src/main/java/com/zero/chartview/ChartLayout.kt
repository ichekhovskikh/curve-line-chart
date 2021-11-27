package com.zero.chartview

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.LinearLayout
import com.zero.chartview.extensions.applyStyledAttributes
import com.zero.chartview.model.CurveLine

class ChartLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes), Themeable {

    private var chart: ChartView? = null
    private var selector: ChartSelectorView? = null

    private lateinit var chartColors: Themeable.ChartColors

    init {
        orientation = VERTICAL
        super.setBackgroundColor(resources.getColor(android.R.color.transparent))

        applyStyledAttributes(attrs, R.styleable.ChartLayout, defStyleAttr, defStyleRes) {
            setChartColors(getThemeColorDefault(this))
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child is ChartView) {
                chart = child
                child.addOnLinesChangedListener { lines ->
                    selector?.setLines(lines)
                }
            } else if (child is ChartSelectorView) {
                selector = child
                child.addRangeChangedInvoker { range ->
                    chart?.setRange(range.start, range.endInclusive)
                }
            }
        }
    }

    fun setRange(start: Float, endInclusive: Float) {
        selector?.setRange(start, endInclusive)
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
            this@ChartLayout.chartColors.colorLegend = colorLegend
            this@ChartLayout.chartColors.colorGrid = colorGrid
            this@ChartLayout.chartColors.colorPopupLine = colorPopupLine
        }
        controlColors?.apply {
            this@ChartLayout.chartColors.colorFrameSelector = colorFrameSelector
            this@ChartLayout.chartColors.colorFogSelector = colorFogSelector
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