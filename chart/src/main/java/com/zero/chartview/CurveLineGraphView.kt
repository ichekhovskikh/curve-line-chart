package com.zero.chartview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.PercentRange
import com.zero.chartview.delegate.CurveLineGraphDelegate
import com.zero.chartview.extensions.*

class CurveLineGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    val range get() = delegate.range

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val delegate = CurveLineGraphDelegate(onUpdate = ::invalidate)

    init {
        applyStyledAttributes(attrs, R.styleable.GraphicsView, defStyleAttr, defStyleRes) {
            paint.strokeWidth = getDimensionPixelSize(
                R.styleable.GraphicsView_lineWidth,
                resources.getDimensionPixelSize(R.dimen.line_width_default)
            ).toFloat()
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

    fun setOnRangeChangedListener(onRangeChangedListener: ((start: Float, endInclusive: Float) -> Unit)?) {
        delegate.setOnRangeChangedListener(onRangeChangedListener)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        delegate.viewSize = measuredWidth on measuredHeight
    }

    override fun onDraw(canvas: Canvas) {
        delegate.drawLines(canvas, paint)
    }
}
