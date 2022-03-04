package com.chekh.chartview.selector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcelable
import androidx.annotation.ColorInt
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.chekh.chartview.R
import com.chekh.chartview.delegate.ScrollFrameDelegate
import com.chekh.chartview.extensions.applyStyledAttributes
import com.chekh.chartview.extensions.getColorCompat
import com.chekh.chartview.extensions.on
import com.chekh.chartview.extensions.takeIfNull
import com.chekh.chartview.model.FloatRange
import com.chekh.chartview.model.PercentRange
import kotlinx.parcelize.Parcelize

/**
 * This view is a representation of the selected vertical area of the graph
 */
internal class ScrollFrameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val delegate: ScrollFrameDelegate

    private val pendingSavedState = SavedState()

    val range get() = delegate.range

    var isSmoothScrollEnabled
        get() = delegate.isSmoothScrollEnabled
        set(value) {
            pendingSavedState.isSmoothScrollEnabled = value
            delegate.isSmoothScrollEnabled = value
        }

    val frameMinWidthPercent
        get() = delegate.frameMinWidthPercent

    val frameMaxWidthPercent
        get() = delegate.frameMaxWidthPercent

    @get:ColorInt
    @setparam:ColorInt
    var frameColor: Int
        get() = delegate.framePaint.color
        set(value) {
            if (delegate.framePaint.color != value) {
                pendingSavedState.frameColor = value
                delegate.framePaint.color = value
                invalidate()
            }
        }

    @get:ColorInt
    @setparam:ColorInt
    var fogColor: Int
        get() = delegate.fogPaint.color
        set(value) {
            if (delegate.fogPaint.color != value) {
                pendingSavedState.fogColor = value
                delegate.fogPaint.color = value
                invalidate()
            }
        }

    init {
        val framePaint = Paint().apply {
            style = Paint.Style.FILL
        }
        val fogPaint = Paint().apply {
            style = Paint.Style.FILL
        }
        val dragIndicatorPaint = Paint().apply {
            style = Paint.Style.FILL
            color = context.getColorCompat(R.color.colorFrameDragIndicator)
        }
        var frameMaxWidthPercent = resources.getFraction(
            R.fraction.frame_max_width_percent_default,
            1,
            1
        )
        var frameMinWidthPercent = resources.getFraction(
            R.fraction.frame_min_width_percent_default,
            1,
            1
        )
        var isSmoothScrollEnabled = true
        val frameCornerRadius = resources.getDimension(
            R.dimen.frame_corner_radius_default
        )
        val frameThicknessHorizontal = resources.getDimension(
            R.dimen.frame_thickness_horizontal_default
        )
        val frameThicknessVertical = resources.getDimension(
            R.dimen.frame_thickness_vertical_default
        )
        val dragIndicatorCornerRadius = resources.getDimension(
            R.dimen.frame_drag_indicator_corner_radius_default
        )
        val dragIndicatorWidth = resources.getDimension(
            R.dimen.frame_drag_indicator_width_default
        )
        val dragIndicatorMaxHeight = resources.getDimension(
            R.dimen.frame_drag_indicator_max_height_default
        )

        applyStyledAttributes(attrs, R.styleable.ScrollFrameView, defStyleAttr, defStyleRes) {
            framePaint.color = getColor(
                R.styleable.ScrollFrameView_selectorFrameColor,
                context.getColorCompat(R.color.colorSelectorFrame)
            )
            fogPaint.color = getColor(
                R.styleable.ScrollFrameView_selectorFogColor,
                context.getColorCompat(R.color.colorSelectorFog)
            )
            frameMaxWidthPercent = getDimension(
                R.styleable.ScrollFrameView_selectorFrameMaxWidthPercent,
                frameMaxWidthPercent
            )
            frameMinWidthPercent = getDimension(
                R.styleable.ScrollFrameView_selectorFrameMinWidthPercent,
                frameMinWidthPercent
            )
            isSmoothScrollEnabled = getBoolean(
                R.styleable.ScrollFrameView_smoothScrollEnabled,
                isSmoothScrollEnabled
            )
        }
        delegate = ScrollFrameDelegate(
            framePaint,
            fogPaint,
            dragIndicatorPaint,
            frameCornerRadius,
            frameThicknessHorizontal,
            frameThicknessVertical,
            frameMaxWidthPercent,
            frameMinWidthPercent,
            dragIndicatorCornerRadius,
            dragIndicatorWidth,
            dragIndicatorMaxHeight,
            isSmoothScrollEnabled,
            onUpdate = ::postInvalidateOnAnimation
        )
        delegate.addOnRangeChangedListener { _, _, _ ->
            pendingSavedState.range = range
        }
    }

    fun setRange(start: Float, endInclusive: Float, smoothScroll: Boolean = false) {
        delegate.setRange(PercentRange(start, endInclusive), smoothScroll)
    }

    fun addOnRangeChangedListener(
        onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit
    ) {
        delegate.addOnRangeChangedListener(onRangeChangedListener)
    }

    fun removeOnRangeChangedListener(
        onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit
    ) {
        delegate.removeOnRangeChangedListener(onRangeChangedListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent) = when (event.actionMasked) {
        MotionEvent.ACTION_DOWN -> {
            parent.requestDisallowInterceptTouchEvent(true)
            delegate.onActionDown(event)
            true
        }
        MotionEvent.ACTION_MOVE -> {
            delegate.onActionMove(event)
            true
        }
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
            parent.requestDisallowInterceptTouchEvent(false)
            true
        }
        else -> super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        delegate.onMeasure(measuredWidth on measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        delegate.drawScrollFrame(canvas)
    }

    override fun onSaveInstanceState(): Parcelable = pendingSavedState.apply {
        superSavedState = super.onSaveInstanceState()
        range = delegate.range
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superSavedState)

        state.range?.takeIfNull(pendingSavedState.range)?.also {
            pendingSavedState.range = it
        }
        state.isSmoothScrollEnabled?.takeIfNull(pendingSavedState.isSmoothScrollEnabled)?.also {
            pendingSavedState.isSmoothScrollEnabled = it
        }
        state.frameColor?.takeIfNull(pendingSavedState.frameColor)?.also {
            pendingSavedState.frameColor = it
        }
        state.fogColor?.takeIfNull(pendingSavedState.fogColor)?.also {
            pendingSavedState.fogColor = it
        }
        post {
            delegate.onRestoreInstanceState(
                pendingSavedState.range,
                pendingSavedState.isSmoothScrollEnabled,
                pendingSavedState.frameColor,
                pendingSavedState.fogColor
            )
        }
    }

    @Parcelize
    private data class SavedState(
        var superSavedState: Parcelable? = null,
        var range: FloatRange? = null,
        var isSmoothScrollEnabled: Boolean? = null,
        var frameColor: Int? = null,
        var fogColor: Int? = null
    ) : Parcelable
}
