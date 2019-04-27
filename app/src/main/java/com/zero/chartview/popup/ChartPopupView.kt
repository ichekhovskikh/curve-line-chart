package com.zero.chartview.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.zero.chartview.R
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.FloatRange

class ChartPopupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var pointInnerRadius = resources.getDimension(R.dimen.point_inner_radius)
    private var pointOuterRadius = resources.getDimension(R.dimen.point_outer_radius)

    private val linePaint = Paint()
    private val pointPaint = Paint()

    private var range = FloatRange(0F, 1F)

    private lateinit var lines: List<CurveLine>
    private lateinit var correspondingLegends: Map<Float, String>

    private val popupWindow = PopupWindow(context, attrs, defStyleAttr, defStyleRes)

    var showCorrespondingLegends = false

    init {
        addView(popupWindow)
        linePaint.color = resources.getColor(R.color.colorLegendLine)
        pointPaint.color = resources.getColor(R.color.colorPointInner)
        linePaint.strokeWidth = resources.getDimension(R.dimen.popup_line_width)
        pointPaint.style = Paint.Style.FILL
    }

    fun setRange(start: Float, endInclusive: Float) {
        range.start = Math.max(start, 0f)
        range.endInclusive = Math.min(endInclusive, 1f)
    }

    fun setLines(lines: List<CurveLine>) {
        this.lines = lines
        popupWindow.setLines(lines)
    }

    fun setCorrespondingLegends(correspondingLegends: Map<Float, String>) {
        this.correspondingLegends = correspondingLegends
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                parent.requestDisallowInterceptTouchEvent(false)
                return false
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }
}