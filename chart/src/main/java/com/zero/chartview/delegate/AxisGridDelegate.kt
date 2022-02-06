package com.zero.chartview.delegate

import android.graphics.Canvas
import android.graphics.Paint
import com.zero.chartview.extensions.alphaColor
import com.zero.chartview.model.AxisLine
import com.zero.chartview.model.Size

internal class AxisGridDelegate(
    internal val linePaint: Paint,
    private val onUpdate: () -> Unit
) {

    private var viewSize = Size()
    private var xAxisLines = emptyList<AxisLine>()
    private var yAxisLines = emptyList<AxisLine>()

    fun setXAxisLines(xAxisLines: List<AxisLine>) {
        if (this.xAxisLines == xAxisLines) return
        this.xAxisLines = xAxisLines
        onUpdate()
    }

    fun setYAxisLines(yAxisLines: List<AxisLine>) {
        if (this.yAxisLines == yAxisLines) return
        this.yAxisLines = yAxisLines
        onUpdate()
    }

    fun onRestoreInstanceState(
        lineColor: Int?,
        lineWidth: Float?
    ) {
        if (linePaint.color == lineColor && linePaint.strokeWidth == lineWidth) return
        lineColor?.let(linePaint::setColor)
        lineWidth?.let(linePaint::setStrokeWidth)
        onUpdate()
    }

    fun onMeasure(viewSize: Size) {
        this.viewSize = viewSize
    }

    fun drawChartGrid(canvas: Canvas) {
        canvas.drawXLines(linePaint)
        canvas.drawYLines(linePaint)
    }

    private fun Canvas.drawXLines(linePaint: Paint) {
        xAxisLines.forEach { line ->
            linePaint.color = line.alphaColor(linePaint.color)
            drawLine(line.position, 0f, line.position, viewSize.height.toFloat(), linePaint)
        }
    }

    private fun Canvas.drawYLines(linePaint: Paint) {
        yAxisLines.forEach { line ->
            linePaint.color = line.alphaColor(linePaint.color)
            drawLine(0f, line.position, viewSize.width.toFloat(), line.position, linePaint)
        }
    }
}
