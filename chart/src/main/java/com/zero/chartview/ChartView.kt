package com.zero.chartview

import android.content.Context
import android.content.res.TypedArray
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.widget.FrameLayout
import com.zero.chartview.axis.XAxisView
import com.zero.chartview.axis.YAxisView
import com.zero.chartview.model.CurveLine
import com.zero.chartview.popup.PopupLineView
import com.zero.chartview.popup.PopupWindow
import com.zero.chartview.utils.*

class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), Themeable {

    private val graph = GraphicsView(context, attrs, defStyleAttr, defStyleRes)
    private val yAxis = YAxisView(context, attrs, defStyleAttr, defStyleRes)
    private val xAxis = XAxisView(context, attrs, defStyleAttr, defStyleRes)
    private val popup = PopupLineView(context, attrs, defStyleAttr, defStyleRes)
    private val window = PopupWindow(context, attrs, defStyleAttr, defStyleRes)

    private val linesChangedInvokers = mutableListOf<(List<CurveLine>) -> Unit>()
    private lateinit var chartColors: Themeable.ChartColors

    init {
        addView(xAxis)
        addView(yAxis)
        addView(graph)
        addView(popup)
        addView(window)
        popup.popupWindow = window

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ChartView, defStyleAttr, defStyleRes)
        val themeDefault = getThemeColorDefault(typedArray)
        typedArray.recycle()
        setChartColors(themeDefault)
    }

    fun setRange(start: Float, endInclusive: Float) {
        graph.setRange(start, endInclusive)
        xAxis.setRange(start, endInclusive)
        popup.setRange(start, endInclusive)
        updateAxis(graph.getLines())
    }

    fun getLines() = graph.getLines()

    fun setLines(lines: List<CurveLine>, correspondingLegends: Map<Float, String>? = null) {
        graph.setLines(lines)
        popup.setLines(lines)
        updateAxis(lines, correspondingLegends)
        linesChanged(lines)
    }

    fun addLine(line: CurveLine, correspondingLegends: Map<Float, String>? = null) {
        val lines = graph.getLines() + line
        graph.addLine(line)
        popup.setLines(lines)
        updateAxis(lines, correspondingLegends)
        linesChanged(lines)
    }

    fun removeLine(index: Int) {
        val lines = graph.getLines()
        removeLine(lines[index])
    }

    fun removeLine(line: CurveLine) {
        val lines = graph.getLines() - line
        graph.removeLine(line)
        popup.setLines(lines)
        updateAxis(lines)
        linesChanged(lines)
    }

    fun addLinesChangedInvoker(invoker: (List<CurveLine>) -> Unit) {
        linesChangedInvokers.add(invoker)
    }

    fun removeRangeChangedInvoker(invoker: (List<CurveLine>) -> Unit) {
        linesChangedInvokers.remove(invoker)
    }

    private fun linesChanged(lines: List<CurveLine>) {
        linesChangedInvokers.forEach { it.invoke(lines) }
    }

    private fun updateAxis(lines: List<CurveLine>) {
        val (minY, maxY) = findMinMaxYValueRanged(lines, graph.range)
        val abscissas = getAbscissas(lines)
        graph.setYAxis(minY, maxY)
        yAxis.setYAxis(minY, maxY)
        xAxis.setCoordinates(abscissas)
    }

    private fun updateAxis(lines: List<CurveLine>, correspondingLegends: Map<Float, String>?) {
        val (minY, maxY) = findMinMaxYValueRanged(lines, graph.range)
        val abscissas = getAbscissas(lines)
        graph.setYAxis(minY, maxY)
        yAxis.setYAxis(minY, maxY)
        xAxis.setCoordinates(abscissas)
        updateCorrespondingLegends(abscissas, correspondingLegends)
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
        yAxis.setLegendColor(legendColor)
        xAxis.invalidate()
        yAxis.invalidate()
    }

    fun setGridColor(@ColorInt gridColor: Int) {
        chartColors.colorGrid = gridColor
        yAxis.setGridColor(gridColor)
        yAxis.invalidate()
    }

    fun setPopupLineColor(@ColorInt popupLineColor: Int) {
        chartColors.colorPopupLine = popupLineColor
        popup.setPopupLineColor(popupLineColor)
        popup.invalidate()
    }

    private fun onThemeColorChanged() {
        yAxis.setGridColor(chartColors.colorGrid)
        xAxis.setLegendColor(chartColors.colorLegend)
        yAxis.setLegendColor(chartColors.colorLegend)
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
            val colorLegend = getColor(R.styleable.ChartView_colorLegend, resources.getColor(R.color.colorLegend))
            val colorGrid = getColor(R.styleable.ChartView_colorGrid, resources.getColor(R.color.colorGrid))
            val colorPopupLine =
                getColor(R.styleable.ChartView_colorPopupLine, resources.getColor(R.color.colorPopupLine))
            return Themeable.ChartColors(colorBackground, colorLegend, colorGrid, colorPopupLine)
        }
    }
}