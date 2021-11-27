package com.zero.chartview

import android.content.Context
import android.content.res.TypedArray
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.widget.FrameLayout
import com.zero.chartview.axis.XAxisView
import com.zero.chartview.axis.YAxisView
import com.zero.chartview.extensions.abscissas
import com.zero.chartview.extensions.applyStyledAttributes
import com.zero.chartview.model.CurveLine
import com.zero.chartview.popup.PopupLineView
import com.zero.chartview.popup.PopupWindow
import com.zero.chartview.tools.*

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

    private val onLinesChangedListeners = mutableListOf<(List<CurveLine>) -> Unit>()
    private lateinit var chartColors: Themeable.ChartColors

    init {
        addView(xAxis)
        addView(yAxis)
        addView(graph)
        addView(popup)
        addView(window)
        popup.popupWindow = window
        graph.setOnYAxisChangedListener(yAxis::setYAxis)

        applyStyledAttributes(attrs, R.styleable.ChartView, defStyleAttr, defStyleRes) {
            setChartColors(getThemeColorDefault(this))
        }
    }

    fun setRange(start: Float, endInclusive: Float, smoothScroll: Boolean = false) {
        graph.setRange(start, endInclusive, smoothScroll)
        xAxis.setRange(start, endInclusive)
        popup.setRange(start, endInclusive)
    }

    fun getLines() = graph.getLines()

    fun setLines(lines: List<CurveLine>, correspondingLegends: Map<Float, String>? = null) {
        graph.setLines(lines)
        popup.setLines(lines)
        updateCorrespondingLegends(lines.abscissas, correspondingLegends)
        onLinesChanged(lines)
    }

    fun addLine(line: CurveLine, correspondingLegends: Map<Float, String>? = null) {
        val lines = graph.getLines() + line
        graph.addLine(line)
        popup.setLines(lines)
        updateCorrespondingLegends(lines.abscissas, correspondingLegends)
        onLinesChanged(lines)
    }

    fun removeLine(index: Int) {
        val lines = graph.getLines()
        removeLine(lines[index])
    }

    fun removeLine(line: CurveLine) {
        val lines = graph.getLines() - line
        graph.removeLine(line)
        popup.setLines(lines)
        onLinesChanged(lines)
    }

    fun addOnLinesChangedListener(listener: (List<CurveLine>) -> Unit) {
        onLinesChangedListeners.add(listener)
    }

    fun removeOnRangeChangedListener(listener: (List<CurveLine>) -> Unit) {
        onLinesChangedListeners.remove(listener)
    }

    private fun onLinesChanged(lines: List<CurveLine>) {
        xAxis.setAbscissas(lines.abscissas)
        onLinesChangedListeners.forEach { it.invoke(lines) }
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