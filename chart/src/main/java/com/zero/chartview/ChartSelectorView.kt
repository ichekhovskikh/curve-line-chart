package com.zero.chartview

import android.content.Context
import android.content.res.TypedArray
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.widget.FrameLayout
import com.zero.chartview.extensions.applyStyledAttributes
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.FloatRange

class ChartSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), Themeable {

    private val graph: GraphicsView = GraphicsView(context, attrs, defStyleAttr, defStyleRes)
    private var scrollFrame = ScrollFrameView(context, attrs, defStyleAttr, defStyleRes)

    private lateinit var chartColors: Themeable.ChartColors

    init {
        addView(graph)
        addView(scrollFrame)

        applyStyledAttributes(attrs, R.styleable.ChartSelectorView, defStyleAttr, defStyleRes) {
            setChartColors(getThemeColorDefault(this))
        }
    }

    fun setRange(start: Float, endInclusive: Float) {
        scrollFrame.setRange(start, endInclusive)
    }

    fun addOnRangeChangedListener(listener: (FloatRange) -> Unit) {
        scrollFrame.addOnRangeChangedListener(listener)
    }

    fun removeOnRangeChangedListener(listener: (FloatRange) -> Unit) {
        scrollFrame.removeOnRangeChangedListener(listener)
    }

    fun setLines(lines: List<CurveLine>) {
        graph.setLines(lines)
    }

    fun addLine(line: CurveLine) {
        graph.addLine(line)
    }

    fun removeLine(index: Int) {
        val lines = graph.getLines()
        removeLine(lines[index])
    }

    fun removeLine(line: CurveLine) {
        graph.removeLine(line)
    }

    override fun getChartColors() = chartColors

    override fun setChartColors(colors: Themeable.ChartColors) {
        chartColors = colors
        onThemeColorChanged()
    }

    override fun setBackgroundColor(@ColorInt backgroundColor: Int) {
        chartColors.colorBackground = backgroundColor
        super.setBackgroundColor(backgroundColor)
    }

    fun setFrameSelectorColor(@ColorInt frameSelectorColor: Int) {
        chartColors.colorFrameSelector = frameSelectorColor
        scrollFrame.setFrameSelectorColor(chartColors.colorFrameSelector)
        scrollFrame.invalidate()
    }

    fun setFogCSelectorColor(@ColorInt fogSelectorColor: Int) {
        chartColors.colorFogSelector = fogSelectorColor
        scrollFrame.setFogSelectorColor(chartColors.colorFogSelector)
        scrollFrame.invalidate()
    }

    private fun onThemeColorChanged() {
        scrollFrame.setFrameSelectorColor(chartColors.colorFrameSelector)
        scrollFrame.setFogSelectorColor(chartColors.colorFogSelector)
        super.setBackgroundColor(chartColors.colorBackground)
        scrollFrame.invalidate()
    }

    private fun getThemeColorDefault(typedArray: TypedArray): Themeable.ChartColors {
        typedArray.apply {
            val colorBackground =
                getColor(R.styleable.ChartSelectorView_colorBackground, resources.getColor(R.color.colorBackground))
            val colorFrameSelector =
                getColor(R.styleable.ChartSelectorView_colorFrameSelector, resources.getColor(R.color.colorFrameSelector))
            val colorFogSelector =
                getColor(R.styleable.ChartSelectorView_colorFogSelector, resources.getColor(R.color.colorFogSelector))
            return Themeable.ChartColors(
                colorBackground = colorBackground,
                colorFrameSelector = colorFrameSelector,
                colorFogSelector = colorFogSelector
            )
        }
    }
}