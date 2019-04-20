package com.zero.chartview

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.zero.chartview.model.CurveLine
import com.zero.chartview.utils.findMaxYValue
import com.zero.chartview.utils.findMinYValue

class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val graph: GraphicView = GraphicView(context, attrs, defStyleAttr, defStyleRes)

    init {
        addView(graph)
    }

    fun setRange(start: Float, endInclusive: Float) {
        graph.setRange(start, endInclusive)
    }

    fun setLines(lines: List<CurveLine>) {
        graph.setLines(lines)
        updateYAxis(lines)
    }

    fun addLine(line: CurveLine) {
        val lines = graph.getLines()
        graph.addLine(line)
        updateYAxis(lines + line)
    }

    fun removeLine(line: CurveLine) {
        val lines = graph.getLines()
        graph.removeLine(line)
        updateYAxis(lines - line)
    }

    private fun updateYAxis(lines: List<CurveLine>) {
        val maxY = findMaxYValue(lines)
        val minY = findMinYValue(lines)
        graph.setYAxis(maxY, minY)
    }
}