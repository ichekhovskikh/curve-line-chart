package com.zero.chartview

import android.content.Context
import android.content.res.TypedArray
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.zero.chartview.axis.YAxisView
import com.zero.chartview.axis.XAxisView
import com.zero.chartview.axis.formatter.AxisFormatter
import com.zero.chartview.extensions.abscissas
import com.zero.chartview.extensions.applyStyledAttributes
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
        xAxis.setAbscissas(abscissas)
        updateCorrespondingLegends(abscissas, correspondingLegends)
    }

    fun addLine(line: CurveLine, correspondingLegends: Map<Float, String>? = null) {
        val lines = graph.getLines() + line
        val abscissas = lines.abscissas
        graph.addLine(line)
        popup.setLines(lines)
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
        if (correspondingLegends == null) {
            val legends = createCorrespondingLegends(abscissas)
            xAxis.setCorrespondingLegends(legends)
            popup.setCorrespondingLegends(legends)
        } else {
            xAxis.setCorrespondingLegends(correspondingLegends)
            popup.setCorrespondingLegends(correspondingLegends)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        popup.dispatchTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measureChildWithMargins(
            graph,
            widthMeasureSpec,
            0,
            heightMeasureSpec,
            xAxis.getLegendWidth()
        )
        measureChildWithMargins(
            yAxis,
            widthMeasureSpec,
            0,
            heightMeasureSpec,
            xAxis.getLegendWidth()
        )
        measureChildWithMargins(
            popup,
            widthMeasureSpec,
            0,
            heightMeasureSpec,
            xAxis.getLegendWidth()
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

    fun setLegendColor(@ColorInt legendColor: Int) {
        chartColors.colorLegend = legendColor
        xAxis.setLegendColor(legendColor)
        yAxis.legendColor = legendColor
        xAxis.invalidate()
        yAxis.invalidate()
    }

    fun setGridColor(@ColorInt gridColor: Int) {
        chartColors.colorGrid = gridColor
        yAxis.gridColor = gridColor
        yAxis.invalidate()
    }

    fun setPopupLineColor(@ColorInt popupLineColor: Int) {
        chartColors.colorPopupLine = popupLineColor
        popup.setPopupLineColor(popupLineColor)
        popup.invalidate()
    }

    private fun onThemeColorChanged() {
        yAxis.gridColor = chartColors.colorGrid
        xAxis.setLegendColor(chartColors.colorLegend)
        yAxis.legendColor = chartColors.colorLegend
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
            val colorLegend = getColor(R.styleable.ChartView_legendTextColor, resources.getColor(R.color.colorLegend))
            val colorGrid = getColor(R.styleable.ChartView_gridColor, resources.getColor(R.color.colorGrid))
            val colorPopupLine =
                getColor(R.styleable.ChartView_colorPopupLine, resources.getColor(R.color.colorPopupLine))
            return Themeable.ChartColors(colorBackground, colorLegend, colorGrid, colorPopupLine)
        }
    }
}
