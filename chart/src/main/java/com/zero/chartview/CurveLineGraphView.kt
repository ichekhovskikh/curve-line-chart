package com.zero.chartview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.zero.chartview.delegate.CurveLineGraphDelegate
import com.zero.chartview.extensions.applyStyledAttributes
import com.zero.chartview.extensions.on
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.PercentRange

class CurveLineGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    val range get() = delegate.range

    var isScrollEnabled = false

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val delegate = CurveLineGraphDelegate(onUpdate = ::invalidate)

    init {
        applyStyledAttributes(attrs, R.styleable.GraphicsView, defStyleAttr, defStyleRes) {
            isScrollEnabled = getBoolean(R.styleable.GraphicsView_scrollEnabled, false)
            paint.strokeWidth = getDimensionPixelSize(
                R.styleable.GraphicsView_lineWidth,
                resources.getDimensionPixelSize(R.dimen.line_width_default)
            ).toFloat()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent) = when {
        !isScrollEnabled -> super.onTouchEvent(event)
        delegate.onTouchEvent(event) -> true
        else -> super.onTouchEvent(event)
    }

    fun getLines() = delegate.linesAfterAnimate

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

    fun addOnLinesChangedListener(onLinesChangedListener: (List<CurveLine>) -> Unit) {
        delegate.addOnLinesChangedListener(onLinesChangedListener)
    }

    fun removeOnLinesChangedListener(onLinesChangedListener: (List<CurveLine>) -> Unit) {
        delegate.removeOnLinesChangedListener(onLinesChangedListener)
    }

    fun addOnRangeChangedListener(onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit) {
        delegate.addOnRangeChangedListener(onRangeChangedListener)
    }

    fun removeOnRangeChangedListener(onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit) {
        delegate.removeOnRangeChangedListener(onRangeChangedListener)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        delegate.onMeasure(measuredWidth on measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        delegate.drawLines(canvas, paint)
    }
}
