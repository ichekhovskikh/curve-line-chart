package com.zero.chartview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.Px
import androidx.annotation.StyleRes
import com.zero.chartview.delegate.CurveLineGraphDelegate
import com.zero.chartview.extensions.applyStyledAttributes
import com.zero.chartview.extensions.on
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.PercentRange

class CurveLineGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val delegate: CurveLineGraphDelegate

    val range get() = delegate.range

    var isScrollEnabled = false

    @get:Px
    @setparam:Px
    var lineWidth: Float
        get() = delegate.paint.strokeWidth
        set(value) {
            if (delegate.paint.strokeWidth != value) {
                delegate.paint.strokeWidth = value
                invalidate()
            }
        }

    init {
        val paint = Paint().apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        applyStyledAttributes(attrs, R.styleable.CurveLineGraphView, defStyleAttr, defStyleRes) {
            isScrollEnabled = getBoolean(R.styleable.CurveLineGraphView_scrollEnabled, false)
            paint.strokeWidth = getDimensionPixelSize(
                R.styleable.CurveLineGraphView_lineWidth,
                resources.getDimensionPixelSize(R.dimen.line_width_default)
            ).toFloat()
        }
        delegate = CurveLineGraphDelegate(
            paint,
            onUpdate = ::postInvalidateOnAnimation
        )
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

    internal fun setOnYAxisChangedListener(onYAxisChangedListener: ((minY: Float, maxY: Float) -> Unit)?) {
        delegate.setOnYAxisChangedListener(onYAxisChangedListener)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        delegate.onMeasure(measuredWidth on measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        delegate.drawLines(canvas)
    }
}
