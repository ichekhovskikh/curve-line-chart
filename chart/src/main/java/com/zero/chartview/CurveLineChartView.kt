package com.zero.chartview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.StyleRes
import com.zero.chartview.axis.AxisGridView
import com.zero.chartview.axis.XAxisView
import com.zero.chartview.axis.YAxisView
import com.zero.chartview.axis.formatter.AxisFormatter
import com.zero.chartview.extensions.*
import com.zero.chartview.extensions.applyStyledAttributes
import com.zero.chartview.model.CurveLine
import com.zero.chartview.popup.PopupLineView
import com.zero.chartview.popup.PopupView

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

    val range get() = graph.range

    var isScrollEnabled
        get() = graph.isScrollEnabled
        set(value) {
            graph.isScrollEnabled = value
        }

    var xAxisFormatter: AxisFormatter
        get() = xAxis.axisFormatter
        set(value) {
            xAxis.axisFormatter = value
        }

    var yAxisFormatter: AxisFormatter
        get() = yAxis.axisFormatter
        set(value) {
            yAxis.axisFormatter = value
        }

    @get:ColorInt
    @setparam:ColorInt
    var xAxisTextColor: Int
        get() = xAxis.textColor
        set(value) {
            xAxis.textColor = value
        }

    @get:ColorInt
    @setparam:ColorInt
    var yAxisTextColor: Int
        get() = yAxis.textColor
        set(value) {
            yAxis.textColor = value
        }

    @get:ColorInt
    @setparam:ColorInt
    var axisLineColor: Int
        get() = grid.lineColor
        set(value) {
            grid.lineColor = value
        }

    @get:ColorInt
    @setparam:ColorInt
    var popupLineColor: Int
        get() = popupLine.lineColor
        set(value) {
            popupLine.lineColor = value
        }

    @get:ColorInt
    @setparam:ColorInt
    var popupLinePointInnerColor: Int
        get() = popupLine.pointInnerColor
        set(value) {
            popupLine.pointInnerColor = value
        }

    @get:Px
    @setparam:Px
    var lineWidth: Float
        get() = graph.lineWidth
        set(value) {
            graph.lineWidth = value
        }

    @get:Px
    @setparam:Px
    var popupLineWidth: Float
        get() = popupLine.lineWidth
        set(value) {
            popupLine.lineWidth = value
        }

    @get:Px
    @setparam:Px
    var xAxisTextSize: Float
        get() = xAxis.textSize
        set(value) {
            xAxis.textSize = value
        }

    @get:Px
    @setparam:Px
    var yAxisTextSize: Float
        get() = yAxis.textSize
        set(value) {
            yAxis.textSize = value
        }

    @get:Px
    @setparam:Px
    var axisLineWidth: Float
        get() = grid.lineWidth
        set(value) {
            grid.lineWidth = value
        }

    var xAxisLegendCount: Int
        get() = xAxis.legendCount
        set(value) {
            xAxis.legendCount = value
        }

    var yAxisLegendCount: Int
        get() = yAxis.legendCount
        set(value) {
            yAxis.legendCount = value
        }

    var isXAxisLegendLinesVisible: Boolean
        get() = xAxis.isLegendLinesAvailable
        set(value) {
            xAxis.isLegendLinesAvailable = value
        }

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

    fun setRange(start: Float, endInclusive: Float, smoothScroll: Boolean = false) {
        graph.setRange(start, endInclusive, smoothScroll)
    }

    fun getLines() = graph.getLines()

    fun setLines(lines: List<CurveLine>) {
        val abscissas = lines.abscissas
        graph.setLines(lines)
        popupLine.setLines(lines)
        yAxis.setOrdinates(lines.ordinates)
        xAxis.setAbscissas(abscissas)
    }

    fun addLine(line: CurveLine) {
        val lines = graph.getLines() + line
        val abscissas = lines.abscissas
        graph.addLine(line)
        popupLine.setLines(lines)
        yAxis.setOrdinates(lines.ordinates)
        xAxis.setAbscissas(abscissas)
    }

    fun removeLine(index: Int) {
        val lines = graph.getLines()
        removeLine(lines[index])
    }

    fun removeLine(line: CurveLine) {
        val lines = graph.getLines() - line
        graph.removeLine(line)
        popupLine.setLines(lines)
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

    fun addOnYAxisChangedListener(onYAxisChangedListener: ((minY: Float, maxY: Float, smoothScroll: Boolean) -> Unit)) {
        graph.addOnYAxisChangedListener(onYAxisChangedListener)
    }

    fun removeOnYAxisChangedListener(onYAxisChangedListener: ((minY: Float, maxY: Float, smoothScroll: Boolean) -> Unit)) {
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
