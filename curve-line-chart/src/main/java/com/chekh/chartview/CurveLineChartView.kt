package com.chekh.chartview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.StyleRes
import com.chekh.chartview.axis.AxisGridView
import com.chekh.chartview.axis.XAxisView
import com.chekh.chartview.axis.YAxisView
import com.chekh.chartview.axis.formatter.AxisFormatter
import com.chekh.chartview.extensions.abscissas
import com.chekh.chartview.extensions.ordinates
import com.chekh.chartview.extensions.marginTop
import com.chekh.chartview.extensions.applyStyledAttributes
import com.chekh.chartview.model.CurveLine
import com.chekh.chartview.popup.PopupLineView
import com.chekh.chartview.popup.PopupView

/**
 * This view draws a curve line graph with axes and popup view
 */
@Suppress("TooManyFunctions")
class CurveLineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val grid = AxisGridView(context, attrs, defStyleAttr, defStyleRes)
    private val graph = CurveLineGraphView(context, attrs, defStyleAttr, defStyleRes)
    private val xAxis = XAxisView(context, attrs, defStyleAttr, defStyleRes)
    private val yAxis = YAxisView(context, attrs, defStyleAttr, defStyleRes)
    private val popupLine = PopupLineView(context, attrs, defStyleAttr, defStyleRes)

    /**
     * Popup window with intersection points
     */
    var popupView: PopupView?
        get() = popupLine.popupView
        set(value) {
            popupLine.popupView?.let(::removeView)
            popupLine.popupView = null
            value?.let { view ->
                addView(view)
                popupLine.popupView = view
            }
        }

    /**
     * Vertical area of the graph to display
     */
    val range get() = graph.range

    /**
     * @return true if manual scrolling is enabled
     */
    var isScrollEnabled
        get() = graph.isScrollEnabled
        set(value) {
            graph.isScrollEnabled = value
        }

    /**
     * Formatter for formatting the values of the abscissa axis
     */
    var xAxisFormatter: AxisFormatter
        get() = xAxis.axisFormatter
        set(value) {
            xAxis.axisFormatter = value
        }

    /**
     * Formatter for formatting the values of the ordinate axis
     */
    var yAxisFormatter: AxisFormatter
        get() = yAxis.axisFormatter
        set(value) {
            yAxis.axisFormatter = value
        }

    /**
     * Color of the legend text on the abscissa axis
     */
    @get:ColorInt
    @setparam:ColorInt
    var xAxisTextColor: Int
        get() = xAxis.textColor
        set(value) {
            xAxis.textColor = value
        }

    /**
     * Color of the legend text on the ordinate axis
     */
    @get:ColorInt
    @setparam:ColorInt
    var yAxisTextColor: Int
        get() = yAxis.textColor
        set(value) {
            yAxis.textColor = value
        }

    /**
     * Color of the axis lines
     */
    @get:ColorInt
    @setparam:ColorInt
    var axisLineColor: Int
        get() = grid.lineColor
        set(value) {
            grid.lineColor = value
        }

    /**
     * Color of the vertical touch line
     */
    @get:ColorInt
    @setparam:ColorInt
    var popupLineColor: Int
        get() = popupLine.lineColor
        set(value) {
            popupLine.lineColor = value
        }

    /**
     * Color of the intersection point on the vertical touch line
     */
    @get:ColorInt
    @setparam:ColorInt
    var popupLinePointInnerColor: Int
        get() = popupLine.pointInnerColor
        set(value) {
            popupLine.pointInnerColor = value
        }

    /**
     * Curve line width
     */
    @get:Px
    @setparam:Px
    var lineWidth: Float
        get() = graph.lineWidth
        set(value) {
            graph.lineWidth = value
        }

    /**
     * Vertical touch line width
     */
    @get:Px
    @setparam:Px
    var popupLineWidth: Float
        get() = popupLine.lineWidth
        set(value) {
            popupLine.lineWidth = value
        }

    /**
     * Legend text size on the abscissa axis
     */
    @get:Px
    @setparam:Px
    var xAxisTextSize: Float
        get() = xAxis.textSize
        set(value) {
            xAxis.textSize = value
        }

    /**
     * Legend text size on the ordinate axis
     */
    @get:Px
    @setparam:Px
    var yAxisTextSize: Float
        get() = yAxis.textSize
        set(value) {
            yAxis.textSize = value
        }

    /**
     * Width of the axis lines
     */
    @get:Px
    @setparam:Px
    var axisLineWidth: Float
        get() = grid.lineWidth
        set(value) {
            grid.lineWidth = value
        }

    /**
     * Number of legends on the abscissa axis
     */
    var xAxisLegendCount: Int
        get() = xAxis.legendCount
        set(value) {
            xAxis.legendCount = value
        }

    /**
     * Number of legends on the ordinate axis
     */
    var yAxisLegendCount: Int
        get() = yAxis.legendCount
        set(value) {
            yAxis.legendCount = value
        }

    /**
     * @return true if the lines on the abscissa axis are visible
     */
    var isXAxisLegendLinesVisible: Boolean
        get() = xAxis.isLegendLinesAvailable
        set(value) {
            xAxis.isLegendLinesAvailable = value
        }

    /**
     * @return true if the lines on the ordinate axis are visible
     */
    var isYAxisLegendLinesVisible: Boolean
        get() = yAxis.isLegendLinesAvailable
        set(value) {
            yAxis.isLegendLinesAvailable = value
        }

    init {
        grid.id = R.id.curve_line_chart_grid_view
        graph.id = R.id.curve_line_graph_view
        xAxis.id = R.id.curve_line_x_axis_view
        yAxis.id = R.id.curve_line_y_axis_view
        popupLine.id = R.id.curve_line_popup_line_view

        addView(grid)
        addView(graph)
        addView(xAxis)
        addView(yAxis)
        addView(popupLine)

        applyStyledAttributes(attrs, R.styleable.CurveLineChartView, defStyleAttr, defStyleRes) {
            popupView = PopupView(
                parent = this@CurveLineChartView,
                className = getString(R.styleable.CurveLineChartView_popupView),
                attrs = attrs,
                defStyleAttr = defStyleAttr,
                defStyleRes = defStyleRes
            )
        }
        xAxis.setOnXAxisLinesChangedListener(grid::setXAxisLines)
        yAxis.setOnYAxisLinesChangedListener(grid::setYAxisLines)
        graph.addOnYAxisChangedListener(yAxis::setYAxis)
        graph.addOnRangeChangedListener { start: Float, endInclusive: Float, smoothScroll: Boolean ->
            xAxis.setRange(start, endInclusive, smoothScroll)
            popupLine.setRange(start, endInclusive)
        }
    }

    /**
     * Set a new scroll frame position
     * @param start the left border of the selected area as a percentage
     * @param endInclusive the right border of the selected area as a percentage
     * @param smoothScroll allows to support smooth scrolling
     */
    fun setRange(start: Float, endInclusive: Float, smoothScroll: Boolean = false) {
        graph.setRange(start, endInclusive, smoothScroll)
    }

    /**
     * @return current lines
     */
    fun getLines() = graph.getLines()

    /**
     * Set a new lines for this [CurveLineChartView]
     * @param lines to be set
     */
    fun setLines(lines: List<CurveLine>) {
        graph.setLines(lines)
        popupLine.setLines(lines)
        yAxis.setOrdinates(lines.ordinates)
        xAxis.setAbscissas(lines.abscissas)
    }

    /**
     * Add a new line into this [CurveLineChartView]
     * @param line to be added
     */
    fun addLine(line: CurveLine) {
        val lines = graph.getLines() + line
        graph.addLine(line)
        popupLine.setLines(lines)
        yAxis.setOrdinates(lines.ordinates)
        xAxis.setAbscissas(lines.abscissas)
    }

    /**
     * Remove a current line from this [CurveLineChartView]
     * @param index of the line to remove
     */
    fun removeLine(index: Int) {
        val lines = graph.getLines()
        removeLine(lines[index])
    }

    /**
     * Remove a current line from this [CurveLineChartView]
     * @param line to be removed
     */
    fun removeLine(line: CurveLine) {
        val lines = graph.getLines() - line
        graph.removeLine(line)
        popupLine.setLines(lines)
        yAxis.setOrdinates(lines.ordinates)
        xAxis.setAbscissas(lines.abscissas)
    }

    /**
     * Add a listener that will be notified of any changes in the displayed lines
     * @param onLinesChangedListener listener to set
     */
    fun addOnLinesChangedListener(onLinesChangedListener: (List<CurveLine>) -> Unit) {
        graph.addOnLinesChangedListener(onLinesChangedListener)
    }

    /**
     * Remove a listener that will be notified of any changes in the displayed lines
     * @param onLinesChangedListener listener to set
     */
    fun removeOnLinesChangedListener(onLinesChangedListener: (List<CurveLine>) -> Unit) {
        graph.removeOnLinesChangedListener(onLinesChangedListener)
    }

    /**
     * Add a listener that will be notified of any changes in the displayed area of the graph
     * @param onRangeChangedListener listener to set
     */
    fun addOnRangeChangedListener(
        onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit
    ) {
        graph.addOnRangeChangedListener(onRangeChangedListener)
    }

    /**
     * Remove a listener that was notified of any changes in displayed area of the graph
     * @param onRangeChangedListener listener to set or null to clear
     */
    fun removeOnRangeChangedListener(
        onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit
    ) {
        graph.removeOnRangeChangedListener(onRangeChangedListener)
    }

    /**
     * Add a listener that will be notified of any changes in displayed maximum or minimum ordinates
     * @param onYAxisChangedListener listener to set
     */
    fun addOnYAxisChangedListener(
        onYAxisChangedListener: ((minY: Float, maxY: Float, smoothScroll: Boolean) -> Unit)
    ) {
        graph.addOnYAxisChangedListener(onYAxisChangedListener)
    }

    /**
     * Remove a listener that will be notified of any changes in displayed maximum or minimum ordinates
     * @param onYAxisChangedListener listener to set
     */
    fun removeOnYAxisChangedListener(
        onYAxisChangedListener: ((minY: Float, maxY: Float, smoothScroll: Boolean) -> Unit)
    ) {
        graph.removeOnYAxisChangedListener(onYAxisChangedListener)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        popupLine.dispatchTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val legendHeightUsed = xAxis.legendTextHeightUsed
        val marginTopUsed = popupLine.paddingVerticalUsed.toInt()
        val marginBottomUsed = legendHeightUsed + popupLine.paddingVerticalUsed.toInt()
        graph.marginTop = marginTopUsed
        grid.marginTop = marginTopUsed
        yAxis.marginTop = marginTopUsed
        measureChildWithMargins(
            graph,
            widthMeasureSpec,
            0,
            heightMeasureSpec,
            marginBottomUsed
        )
        measureChildWithMargins(
            grid,
            widthMeasureSpec,
            0,
            heightMeasureSpec,
            marginBottomUsed
        )
        measureChildWithMargins(
            yAxis,
            widthMeasureSpec,
            0,
            heightMeasureSpec,
            marginBottomUsed
        )
        measureChildWithMargins(
            popupLine,
            widthMeasureSpec,
            0,
            heightMeasureSpec,
            legendHeightUsed
        )
    }
}
