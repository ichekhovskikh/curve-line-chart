package com.zero.chartview

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.LinearLayout
import com.zero.chartview.model.CurveLine

class ChartLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes), Themeable {

    private var chart: ChartView? = null
    private var selector: ChartSelectorView? = null

    private lateinit var themeColor: Themeable.ThemeColor

    init {
        orientation = VERTICAL
        super.setBackgroundColor(resources.getColor(android.R.color.transparent))

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ChartLayout, defStyleAttr, defStyleRes)
        val themeDefault = getThemeColorDefault(typedArray)
        typedArray.recycle()
        setThemeColor(themeDefault)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child is ChartView) {
                chart = child
                child.addLinesChangedInvoker { lines ->
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

    override fun getThemeColor(): Themeable.ThemeColor {
        val chartColors = chart?.getThemeColor()
        val controlColors = selector?.getThemeColor()
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
        selector?.setThemeColor(colors)
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