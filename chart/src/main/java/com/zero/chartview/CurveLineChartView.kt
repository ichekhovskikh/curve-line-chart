package com.zero.chartview

import android.content.Context
import androidx.annotation.ColorInt
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.zero.chartview.axis.YAxisView
import com.zero.chartview.axis.XAxisView
import com.zero.chartview.axis.formatter.AxisFormatter
import com.zero.chartview.extensions.abscissas
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
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val graph = CurveLineGraphView(context, attrs, defStyleAttr, defStyleRes)
    private val yAxis = YAxisView(context, attrs, defStyleAttr, defStyleRes)
    private val xAxis = XAxisView(context, attrs, defStyleAttr, defStyleRes)
    private val popup = PopupLineView(context, attrs, defStyleAttr, defStyleRes)
    private val window = PopupWindow(context, attrs, defStyleAttr, defStyleRes)

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

    var lineWidth: Float
        get() = graph.lineWidth
        set(value) {
            graph.lineWidth = value
        }

    var yAxisLegendCount: Int
        get() = yAxis.legendCount
        set(value) {
            yAxis.legendCount = value
        }

    var xAxisLegendCount: Int
        get() = xAxis.legendCount
        set(value) {
            xAxis.legendCount = value
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

    fun setXLegendTextColor(@ColorInt textColor: Int) {
        xAxis.textColor = textColor
    }

    fun setYLegendTextColor(@ColorInt textColor: Int) {
        yAxis.textColor = textColor
    }

    fun setYLegendLineColor(@ColorInt lineColor: Int) {
        yAxis.lineColor = lineColor
    }

    fun setPopupLineColor(@ColorInt popupLineColor: Int) {
        popup.setPopupLineColor(popupLineColor)
    }
}
