package com.zero.chartview

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.ColorInt
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.zero.chartview.axis.YAxisView
import com.zero.chartview.axis.XAxisView
import com.zero.chartview.axis.formatter.AxisFormatter
import com.zero.chartview.extensions.abscissas
import com.zero.chartview.extensions.applyStyledAttributes
import com.zero.chartview.extensions.ordinates
import com.zero.chartview.model.CurveLine
import com.zero.chartview.popup.PopupLineView
import com.zero.chartview.popup.PopupWindow
import com.zero.chartview.tools.*

class CurveLineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), Themeable {

    private val graph = CurveLineGraphView(context, attrs, defStyleAttr, defStyleRes)
    private val yAxis = YAxisView(context, attrs, defStyleAttr, defStyleRes)
    private val xAxis = XAxisView(context, attrs, defStyleAttr, defStyleRes)
    private val popup = PopupLineView(context, attrs, defStyleAttr, defStyleRes)
    private val window = PopupWindow(context, attrs, defStyleAttr, defStyleRes)

    private lateinit var chartColors: Themeable.ChartColors

    val range get() = graph.range

    var isScrollEnabled
        get() = graph.isScrollEnabled
        set(value) {
            graph.isScrollEnabled = value
        }

    var yAxisFormatter: AxisFormatter
        get() = yAxis.axisFormatter
        set(value) {
            yAxis.axisFormatter = value
        }

    var yAxisLegendCount: Int
        get() = yAxis.legendCount
        set(value) {
            yAxis.legendCount = value
        }

    init {
        addView(xAxis)
        addView(yAxis)
        addView(graph)
        addView(popup)
        addView(window)
        popup.popupWindow = window
        graph.setOnYAxisChangedListener(yAxis::setYAxis)
        graph.addOnRangeChangedListener { start: Float, endInclusive: Float, smoothScroll: Boolean ->
            xAxis.setRange(start, endInclusive, smoothScroll)
            popup.setRange(start, endInclusive)
        }

        applyStyledAttributes(attrs, R.styleable.ChartView, defStyleAttr, defStyleRes) {
            setChartColors(getThemeColorDefault(this))
        }
    }

    fun setRange(start: Float, endInclusive: Float, smoothScroll: Boolean = false) {
        graph.setRange(start, endInclusive, smoothScroll)
    }

    fun getLines() = graph.getLines()

    fun setLines(lines: List<CurveLine>, correspondingLegends: Map<Float, String>? = null) {
        val abscissas = lines.abscissas
        graph.setLines(lines)
        popup.setLines(lines)
        yAxis.setOrdinates(lines.ordinates)
        xAxis.setAbscissas(abscissas)
        updateCorrespondingLegends(abscissas, correspondingLegends)
    }

    fun addLine(line: CurveLine, correspondingLegends: Map<Float, String>? = null) {
        val lines = graph.getLines() + line
        val abscissas = lines.abscissas
        graph.addLine(line)
        popup.setLines(lines)
        yAxis.setOrdinates(lines.ordinates)
        xAxis.setAbscissas(abscissas)
        updateCorrespondingLegends(abscissas, correspondingLegends)
    }

    fun removeLine(index: Int) {
        val lines = graph.getLines()
        removeLine(lines[index])
    }

    fun removeLine(line: CurveLine) {
        val lines = graph.getLines() - line
        graph.removeLine(line)
        popup.setLines(lines)
        yAxis.setOrdinates(lines.ordinates)
        xAxis.setAbscissas(lines.abscissas)
    }

    fun addOnLinesChangedListener(onLinesChangedListener: (List<CurveLine>) -> Unit) {
        graph.addOnLinesChangedListener(onLinesChangedListener)
    }

    fun removeOnLinesChangedListener(onLinesChangedListener: (List<CurveLine>) -> Unit) {
        graph.removeOnLinesChangedListener(onLinesChangedListener)
    }

    fun addOnRangeChangedListener(onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit) {
        graph.addOnRangeChangedListener(onRangeChangedListener)
    }

    fun removeOnRangeChangedListener(onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit) {
        graph.removeOnRangeChangedListener(onRangeChangedListener)
    }

    private fun updateCorrespondingLegends(abscissas: List<Float>, correspondingLegends: Map<Float, String>?) {
        popup.setCorrespondingLegends(correspondingLegends ?: createCorrespondingLegends(abscissas))
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        popup.dispatchTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val legendTextHeightUsed = xAxis.legendTextHeightUsed
        measureChildWithMargins(
            graph,
            widthMeasureSpec,
            0,
            heightMeasureSpec,
            legendTextHeightUsed
        )
        measureChildWithMargins(
            yAxis,
            widthMeasureSpec,
            0,
            heightMeasureSpec,
            legendTextHeightUsed
        )
        measureChildWithMargins(
            popup,
            widthMeasureSpec,
            0,
            heightMeasureSpec,
            legendTextHeightUsed
        )
    }

    override fun getChartColors() = chartColors

    override fun setChartColors(colors: Themeable.ChartColors) {
        chartColors = colors
        onThemeColorChanged()
    }

    override fun setBackgroundColor(@ColorInt backgroundColor: Int) {
        chartColors.colorBackground = backgroundColor
        popup.setPointColor(backgroundColor)
        window.setBackgroundColor(backgroundColor)
        super.setBackgroundColor(backgroundColor)
    }

    fun setLegendTextColor(@ColorInt textColor: Int) {
        chartColors.colorLegend = textColor
        xAxis.textColor = textColor
        yAxis.textColor = textColor
        xAxis.invalidate()
        yAxis.invalidate()
    }

    fun setYLegendLineColor(@ColorInt colorYLegendLine: Int) {
        chartColors.colorYLegendLine = colorYLegendLine
        yAxis.lineColor = colorYLegendLine
        yAxis.invalidate()
    }

    fun setPopupLineColor(@ColorInt popupLineColor: Int) {
        chartColors.colorPopupLine = popupLineColor
        popup.setPopupLineColor(popupLineColor)
        popup.invalidate()
    }

    private fun onThemeColorChanged() {
        yAxis.lineColor = chartColors.colorYLegendLine
        xAxis.textColor = chartColors.colorLegend
        yAxis.textColor = chartColors.colorLegend
        popup.setPopupLineColor(chartColors.colorPopupLine)
        popup.setPointColor(chartColors.colorBackground)
        window.setBackgroundColor(chartColors.colorBackground)
        super.setBackgroundColor(chartColors.colorBackground)
        xAxis.invalidate()
        yAxis.invalidate()
        popup.invalidate()
    }

    private fun getThemeColorDefault(typedArray: TypedArray): Themeable.ChartColors {
        typedArray.apply {
            val colorBackground =
                getColor(R.styleable.ChartView_colorBackground, resources.getColor(R.color.colorBackground))
            val colorLegend = getColor(R.styleable.ChartView_yLegendTextColor, resources.getColor(R.color.colorYLegendText))
            val colorGrid = getColor(R.styleable.ChartView_yLegendLineColor, resources.getColor(R.color.colorYLegendLine))
            val colorPopupLine =
                getColor(R.styleable.ChartView_colorPopupLine, resources.getColor(R.color.colorPopupLine))
            return Themeable.ChartColors(colorBackground, colorLegend, colorGrid, colorPopupLine)
        }
    }
}
