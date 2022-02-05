package com.zero.chartview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.Px
import androidx.annotation.StyleRes
import com.zero.chartview.delegate.CurveLineGraphDelegate
import com.zero.chartview.extensions.*
import com.zero.chartview.extensions.applyStyledAttributes
import com.zero.chartview.extensions.on
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.FloatRange
import com.zero.chartview.model.PercentRange
import kotlinx.parcelize.Parcelize

class CurveLineGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val delegate: CurveLineGraphDelegate

    private val pendingSavedState = SavedState()

    val range get() = delegate.range

    var isScrollEnabled = false
        set(value) {
            if (field != value) {
                pendingSavedState.isScrollEnabled = value
                field = value
            }
        }

    @get:Px
    @setparam:Px
    var lineWidth: Float
        get() = delegate.paint.strokeWidth
        set(value) {
            if (delegate.paint.strokeWidth != value) {
                pendingSavedState.lineWidth = value
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
        delegate.addOnRangeChangedListener { _, _, _ ->
            pendingSavedState.range = range
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

    internal fun setOnYAxisChangedListener(onYAxisChangedListener: ((minY: Float, maxY: Float, smoothScroll: Boolean) -> Unit)?) {
        delegate.setOnYAxisChangedListener(onYAxisChangedListener)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        delegate.onMeasure(measuredWidth on measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        delegate.drawLines(canvas)
    }

    override fun onSaveInstanceState(): Parcelable = pendingSavedState.apply {
        superSavedState = super.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superSavedState)

        state.isScrollEnabled?.takeIfNull(pendingSavedState.isScrollEnabled)?.let {
            pendingSavedState.isScrollEnabled = it
            isScrollEnabled = it
        }
        state.range?.takeIfNull(pendingSavedState.range)?.also {
            pendingSavedState.range = it
        }
        state.lineWidth?.takeIfNull(pendingSavedState.lineWidth)?.also {
            pendingSavedState.lineWidth = it
        }
        post {
            delegate.onRestoreInstanceState(
                pendingSavedState.range,
                pendingSavedState.lineWidth
            )
        }
    }

    @Parcelize
    private data class SavedState(
        var superSavedState: Parcelable? = null,
        var isScrollEnabled: Boolean? = null,
        var range: FloatRange? = null,
        var lineWidth: Float? = null
    ) : Parcelable
}
