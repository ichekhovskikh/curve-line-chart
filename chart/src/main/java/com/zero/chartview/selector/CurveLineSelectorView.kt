package com.zero.chartview.selector

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import androidx.annotation.ColorInt
import android.util.AttributeSet
import android.widget.FrameLayout
import com.zero.chartview.CurveLineGraphView
import com.zero.chartview.R
import com.zero.chartview.Themeable
import com.zero.chartview.extensions.applyStyledAttributes
import com.zero.chartview.model.CurveLine

class CurveLineSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), Themeable {

    private val scrollFrame = ScrollFrameView(context, attrs, defStyleAttr, defStyleRes)
    private val graph = CurveLineGraphView(context, attrs, defStyleAttr, defStyleRes).apply {
        isScrollEnabled = false
    }

    private lateinit var chartColors: Themeable.ChartColors

    var isSmoothScrollEnabled
        get() = scrollFrame.isSmoothScrollEnabled
        set(value) {
            scrollFrame.isSmoothScrollEnabled = value
        }

    init {
        addView(graph)
        addView(scrollFrame)

        applyStyledAttributes(attrs, R.styleable.CurveLineSelectorView, defStyleAttr, defStyleRes) {
            setChartColors(getThemeColorDefault(this))
        }
    }

    fun setRange(start: Float, endInclusive: Float, smoothScroll: Boolean = false) {
        scrollFrame.setRange(start, endInclusive, smoothScroll)
    }

    fun addOnRangeChangedListener(onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit) {
        scrollFrame.addOnRangeChangedListener(onRangeChangedListener)
    }

    fun removeOnRangeChangedListener(onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit) {
        scrollFrame.removeOnRangeChangedListener(onRangeChangedListener)
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

    fun setFrameColor(@ColorInt frameColor: Int) {
        chartColors.colorFrameSelector = frameColor
        scrollFrame.setFrameColor(chartColors.colorFrameSelector)
        scrollFrame.invalidate()
    }

    fun setFogColor(@ColorInt fogColor: Int) {
        chartColors.colorFogSelector = fogColor
        scrollFrame.setFogColor(chartColors.colorFogSelector)
        scrollFrame.invalidate()
    }

    private fun onThemeColorChanged() {
        scrollFrame.setFrameColor(chartColors.colorFrameSelector)
        scrollFrame.setFogColor(chartColors.colorFogSelector)
        super.setBackgroundColor(chartColors.colorBackground)
        scrollFrame.invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setOnTouchListener(listener: OnTouchListener?) {
        scrollFrame.setOnTouchListener(listener)
    }

    private fun getThemeColorDefault(typedArray: TypedArray): Themeable.ChartColors {
        typedArray.apply {
            val colorFrameSelector =
                getColor(R.styleable.CurveLineSelectorView_selectorFrameColor, resources.getColor(R.color.colorFrameSelector))
            val colorFogSelector =
                getColor(R.styleable.CurveLineSelectorView_selectorFogColor, resources.getColor(R.color.colorFogSelector))
            return Themeable.ChartColors(
                colorBackground = Color.WHITE,
                colorFrameSelector = colorFrameSelector,
                colorFogSelector = colorFogSelector
            )
        }
    }
}
