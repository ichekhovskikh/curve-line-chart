package com.chekh.chartview

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
import com.chekh.chartview.delegate.CurveLineGraphDelegate
import com.chekh.chartview.extensions.takeIfNull
import com.chekh.chartview.extensions.applyStyledAttributes
import com.chekh.chartview.extensions.on
import com.chekh.chartview.model.CurveLine
import com.chekh.chartview.model.FloatRange
import com.chekh.chartview.model.PercentRange
import kotlinx.parcelize.Parcelize

/**
 * This view draws a curve line graph
 */
@Suppress("TooManyFunctions")
class CurveLineGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val delegate: CurveLineGraphDelegate

    private val pendingSavedState = SavedState()

    /**
     * Vertical area of the graph to display
     */
    val range get() = delegate.range

    /**
     * @return true if manual scrolling is enabled
     */
    var isScrollEnabled = false
        set(value) {
            if (field != value) {
                pendingSavedState.isScrollEnabled = value
                field = value
            }
        }

    /**
     * Curve line width
     */
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

    /**
     * @return current lines
     */
    fun getLines() = delegate.linesAfterAnimate

    /**
     * Set a new lines for this [CurveLineGraphView]
     * @param lines to be set
     */
    fun setLines(lines: List<CurveLine>) {
        delegate.setLines(lines)
    }

    /**
     * Add a new line into this [CurveLineGraphView]
     * @param line to be added
     */
    fun addLine(line: CurveLine) {
        delegate.addLine(line)
    }

    /**
     * Remove a current line from this [CurveLineGraphView]
     * @param index of the line to remove
     */
    fun removeLine(index: Int) {
        val lines = getLines()
        delegate.removeLine(lines[index])
    }

    /**
     * Remove a current line from this [CurveLineGraphView]
     * @param line to be removed
     */
    fun removeLine(line: CurveLine) {
        delegate.removeLine(line)
    }

    /**
     * Set a new scroll frame position
     * @param start the left border of the selected area as a percentage
     * @param endInclusive the right border of the selected area as a percentage
     * @param smoothScroll allows to support smooth scrolling
     */
    fun setRange(start: Float, endInclusive: Float, smoothScroll: Boolean = false) {
        delegate.setRange(PercentRange(start, endInclusive), smoothScroll)
    }

    /**
     * Add a listener that will be notified of any changes in the displayed lines
     * @param onLinesChangedListener listener to set
     */
    fun addOnLinesChangedListener(onLinesChangedListener: (List<CurveLine>) -> Unit) {
        delegate.addOnLinesChangedListener(onLinesChangedListener)
    }

    /**
     * Remove a listener that will be notified of any changes in the displayed lines
     * @param onLinesChangedListener listener to set
     */
    fun removeOnLinesChangedListener(onLinesChangedListener: (List<CurveLine>) -> Unit) {
        delegate.removeOnLinesChangedListener(onLinesChangedListener)
    }

    /**
     * Add a listener that will be notified of any changes in the displayed area of the graph
     * @param onRangeChangedListener listener to set
     */
    fun addOnRangeChangedListener(
        onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit
    ) {
        delegate.addOnRangeChangedListener(onRangeChangedListener)
    }

    /**
     * Remove a listener that was notified of any changes in displayed area of the graph
     * @param onRangeChangedListener listener to set or null to clear
     */
    fun removeOnRangeChangedListener(
        onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit
    ) {
        delegate.removeOnRangeChangedListener(onRangeChangedListener)
    }

    /**
     * Add a listener that will be notified of any changes in displayed maximum or minimum ordinates
     * @param onYAxisChangedListener listener to set
     */
    fun addOnYAxisChangedListener(
        onYAxisChangedListener: ((minY: Float, maxY: Float, smoothScroll: Boolean) -> Unit)
    ) {
        delegate.addOnYAxisChangedListener(onYAxisChangedListener)
    }

    /**
     * Remove a listener that will be notified of any changes in displayed maximum or minimum ordinates
     * @param onYAxisChangedListener listener to set
     */
    fun removeOnYAxisChangedListener(
        onYAxisChangedListener: ((minY: Float, maxY: Float, smoothScroll: Boolean) -> Unit)
    ) {
        delegate.removeOnYAxisChangedListener(onYAxisChangedListener)
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
