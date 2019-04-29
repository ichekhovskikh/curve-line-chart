package com.zero.chartview

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.zero.chartview.model.CurveLine

class ChartLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), Themeable {

    private val chartLayout =
        LayoutInflater.from(context).inflate(R.layout.chart_layout_view, this, false) as LinearLayout

    private var chart: ChartView? = null
    private var control: ChartControlView? = null

    private lateinit var themeColor: Themeable.ThemeColor

    init {
        while (chartLayout.childCount > 0) {
            val v = chartLayout.getChildAt(0)
            removeViewAt(0)
            addView(v)
        }
        addView(chartLayout)
        this.setAddStatesFromChildren(true)
        super.setBackgroundColor(resources.getColor(android.R.color.transparent))

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ChartLayout, defStyleAttr, defStyleRes)
        val themeDefault = getThemeColorDefault(typedArray)
        typedArray.recycle()
        setThemeColor(themeDefault)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        var index = 0
        while (index < childCount) {
            val child = getChildAt(index)
            if (child is ChartView || child is ChartControlView) {
                removeViewAt(index)
                addView(child)
            } else index++
        }
    }

    override fun addView(child: View) {
        if (child is ChartView) {
            chart = child
            chartLayout.addView(chart)
            child.addLinesChangedInvoker { lines ->
                control?.setLines(lines)
            }
        } else if (child is ChartControlView) {
            control = child
            chartLayout.addView(control)
            child.addRangeChangedInvoker { range ->
                chart?.setRange(range.start, range.endInclusive)
            }
        } else {
            super.addView(child)
        }
    }

    fun setRange(start: Float, endInclusive: Float) {
        control?.setRange(start, endInclusive)
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

    override fun setBackgroundColor(color: Int) {
        chartLayout.setBackgroundColor(color)
    }

    override fun getThemeColor(): Themeable.ThemeColor {
        val chartColors = chart?.getThemeColor()
        val controlColors = control?.getThemeColor()
        chartColors?.apply {
            themeColor.colorLegend = colorLegend
            themeColor.colorGrid = colorGrid
            themeColor.colorPopupLine = colorPopupLine
        }
        controlColors?.apply {
            themeColor.colorFrameControl = colorFrameControl
            themeColor.colorFogControl = colorFogControl
        }
        return themeColor
    }

    override fun setThemeColor(colors: Themeable.ThemeColor) {
        themeColor = colors
        chart?.setThemeColor(colors)
        control?.setThemeColor(colors)
        val colorBackground = colors.colorBackground
        if (colorBackground != null) {
            setBackgroundColor(colorBackground)
        }
    }

    private fun getThemeColorDefault(typedArray: TypedArray): Themeable.ThemeColor {
        typedArray.apply {
            val colorBackground =
                getColor(R.styleable.ChartLayout_colorBackground, resources.getColor(R.color.colorBackground))
            return Themeable.ThemeColor(colorBackground = colorBackground)
        }
    }
}