package com.zero.chartview

import android.content.Context
import android.content.res.TypedArray
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.widget.FrameLayout
import com.zero.chartview.axis.XAxisView
import com.zero.chartview.axis.YAxisView
import com.zero.chartview.model.CurveLine
import com.zero.chartview.utils.*

class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), Themeable {

    private val graph: GraphicView = GraphicView(context, attrs, defStyleAttr, defStyleRes)
    private val yAxis: YAxisView = YAxisView(context, attrs, defStyleAttr, defStyleRes)
    private val xAxis: XAxisView = XAxisView(context, attrs, defStyleAttr, defStyleRes)

    private lateinit var themeColor: Themeable.ThemeColor

    init {
        addView(xAxis)
        addView(yAxis)
        addView(graph)

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ChartView, defStyleAttr, defStyleRes)
        val themeDefault = getThemeColorDefault(typedArray)
        typedArray.recycle()
        setThemeColor(themeDefault)
    }

    fun setRange(start: Float, endInclusive: Float) {
        graph.setRange(start, endInclusive)
        xAxis.setRange(start, endInclusive)
        updateAxis(graph.getLines(), emptyMap())
    }

    fun setLines(lines: List<CurveLine>, correspondingLegends: Map<Float, String> = emptyMap()) {
        graph.setLines(lines)
        updateAxis(lines, correspondingLegends)
    }

    fun addLine(line: CurveLine, correspondingLegends: Map<Float, String> = emptyMap()) {
        val lines = graph.getLines()
        graph.addLine(line)
        updateAxis(lines + line, correspondingLegends)
    }

    fun removeLine(index: Int) {
        val lines = graph.getLines()
        removeLine(lines[index])
    }

    fun removeLine(line: CurveLine) {
        val lines = graph.getLines()
        graph.removeLine(line)
        updateAxis(lines - line, emptyMap())
    }

    private fun updateAxis(lines: List<CurveLine>, correspondingLegends: Map<Float, String>) {
        val (minY, maxY) = findMinMaxYValueRanged(lines, graph.range)
        val abscissas = getAbscissas(lines)
        graph.setYAxis(minY, maxY)
        yAxis.setYAxis(minY, maxY)
        xAxis.setCoordinates(abscissas)
        if (correspondingLegends.isEmpty()) {
            xAxis.setCorrespondingLegends(createCorrespondingLegends(abscissas))
        } else {
            xAxis.setCorrespondingLegends(correspondingLegends)
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
    }

    override fun getThemeColor() = themeColor

    override fun setThemeColor(colors: Themeable.ThemeColor) {
        themeColor = colors
        onThemeColorChanged()
    }

    override fun setBackgroundColor(@ColorInt backgroundColor: Int) {
        themeColor.colorBackground = backgroundColor
        super.setBackgroundColor(backgroundColor)
    }

    fun setLegendColor(@ColorInt legendColor: Int) {
        themeColor.colorLegend = legendColor
        xAxis.setLegendColor(themeColor.colorLegend)
        yAxis.setLegendColor(themeColor.colorLegend)
        invalidate()
    }

    fun setGridColor(@ColorInt gridColor: Int) {
        themeColor.colorGrid = gridColor
        yAxis.setGridColor(themeColor.colorGrid)
        invalidate()
    }

    private fun onThemeColorChanged() {
        yAxis.setGridColor(themeColor.colorGrid)
        xAxis.setLegendColor(themeColor.colorLegend)
        yAxis.setLegendColor(themeColor.colorLegend)
        super.setBackgroundColor(themeColor.colorBackground)
        invalidate()
    }

    private fun getThemeColorDefault(typedArray: TypedArray): Themeable.ThemeColor {
        typedArray.apply {
            val colorBackground =
                getColor(R.styleable.ChartView_colorBackground, resources.getColor(R.color.colorBackground))
            val colorLegend = getColor(R.styleable.ChartView_colorLegend, resources.getColor(R.color.colorLegend))
            val colorGrid = getColor(R.styleable.ChartView_colorGrid, resources.getColor(R.color.colorGrid))
            return Themeable.ThemeColor(colorBackground, colorLegend, colorGrid)
        }
    }
}