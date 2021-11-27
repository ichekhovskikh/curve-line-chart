package com.zero.chartview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.PercentRange
import com.zero.chartview.delegate.CurveLineDelegate
import com.zero.chartview.extensions.*

internal class GraphicsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    val range get() = delegate.range

    val maxY get() = delegate.maxY

    val minY get() = delegate.minY

    private val paint = Paint()

    private val delegate = CurveLineDelegate(onUpdate = ::invalidate)

    init {
        var lineWidth = resources.getDimensionPixelSize(R.dimen.line_width_default)
        applyStyledAttributes(attrs, R.styleable.GraphicsView, defStyleAttr, defStyleRes) {
            lineWidth = getDimensionPixelSize(R.styleable.GraphicsView_lineWidth, lineWidth)
        }
        setupPaint(lineWidth)
    }

    private fun setupPaint(lineWidth: Int) {
        paint.apply {
            style = Paint.Style.STROKE
            strokeWidth = lineWidth.toFloat()
            isAntiAlias = true
        }
    }

    fun getLines() = delegate.lines

    fun setLines(lines: List<CurveLine>) {
        delegate.setLines(lines)
    }

    fun addLine(line: CurveLine) {
        delegate.addLine(line)
    }

    fun removeLine(line: CurveLine) {
        delegate.removeLine(line)
    }

    fun setRange(start: Float, endInclusive: Float, smoothScroll: Boolean = false) {
        delegate.setRange(PercentRange(start, endInclusive), smoothScroll)
    }

    fun setOnYAxisChangedListener(onYAxisChangedListener: ((minY: Float, maxY: Float) -> Unit)?) {
        delegate.setOnYAxisChangedListener(onYAxisChangedListener)
    }

    override fun onDraw(canvas: Canvas) {
        delegate.drawLines(canvas, paint, measuredWidth on measuredHeight)
    }
}
