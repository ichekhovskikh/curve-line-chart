package com.zero.chartview.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.zero.chartview.R
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.FloatRange
import com.zero.chartview.utils.*
import kotlin.math.abs

class ChartPopupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var pointInnerRadius = resources.getDimension(R.dimen.point_inner_radius)
    private var pointOuterRadius = resources.getDimension(R.dimen.point_outer_radius)
    private val pointColor = resources.getColor(R.color.colorPointInner)

    private val linePaint = Paint()
    private val pointPaint = Paint()

    private var range = FloatRange(0F, 1F)

    private lateinit var lines: List<CurveLine>
    private lateinit var correspondingLegends: Map<Float, String>

    var popupWindow: PopupWindow? = null

    private var epsilonXPercent = resources.getFraction(R.fraction.epsilon_x_tracking_touch, 1, 1)
    private var dyStopTrackingTouch = resources.getDimension(R.dimen.dy_stop_tracking_touch)
    private var touchX: Float? = null
    private var touchY: Float = 0f

    var showCorrespondingLegends = false

    init {
        linePaint.color = resources.getColor(R.color.colorLegendLine)
        linePaint.strokeWidth = resources.getDimension(R.dimen.popup_line_width)
        pointPaint.style = Paint.Style.FILL
    }

    fun setRange(start: Float, endInclusive: Float) {
        range.start = Math.max(start, 0f)
        range.endInclusive = Math.min(endInclusive, 1f)
    }

    fun setLines(lines: List<CurveLine>) {
        this.lines = lines
        popupWindow?.setLines(lines)
    }

    fun setCorrespondingLegends(correspondingLegends: Map<Float, String>) {
        this.correspondingLegends = correspondingLegends
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
                touchX = event.x
                touchY = event.y
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                if (Math.abs(event.y - touchY) > dyStopTrackingTouch) {
                    parent.requestDisallowInterceptTouchEvent(false)
                    popupWindow?.visibility = GONE
                    touchX = null
                    invalidate()
                    return false
                }
                touchX = event.x
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                popupWindow?.visibility = GONE
                touchX = null
                invalidate()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        touchX?.let { x ->
            val minX = findMinXValue(lines)
            val maxX = findMaxXValue(lines)
            val minY = findMinYValue(lines)
            val maxY = findMaxYValue(lines)
            val intersectionPoints = getIntersectionPoint(x, minX, maxX)
            if (intersectionPoints.isNotEmpty()) {
                val intersectionXValue = intersectionPoints[0].x
                val xDrawPixel = xValueToPixel(intersectionXValue, measuredWidth, minX, maxX)
                canvas.drawLine(xDrawPixel, 0F, xDrawPixel, height.toFloat(), linePaint)
                intersectionPoints.forEach {
                    val yDrawPixel = yValueToPixel(it.y, measuredHeight, minY, maxY)
                    drawIntersectionPoint(canvas, xDrawPixel, yDrawPixel, it.color)
                }
                popupWindow?.fill(touchX, intersectionPoints)
            }
        }
    }

    private fun drawIntersectionPoint(canvas: Canvas, x: Float, y: Float, outerColor: Int) {
        pointPaint.color = outerColor
        canvas.drawCircle(x, y, pointOuterRadius, pointPaint)
        pointPaint.color = pointColor
        canvas.drawCircle(x, y, pointInnerRadius, pointPaint)
    }

    private fun getIntersectionPoint(xPixel: Float, minX: Float, maxX: Float): List<ChartPoint> {
        val xValue = xPixelToValue(xPixel, measuredWidth, minX, maxX)
        val points = mutableListOf<ChartPoint>()
        var minDistance = (maxX - minX) * epsilonXPercent
        var nearestX: Float? = null
        lines.forEach { line ->
            line.points.forEach { point ->
                points.add(ChartPoint(line.name, line.color, point.x, point.y, getIntersectionLegend(point)))
                val distance = abs(xValue - point.x)
                if (distance <= minDistance) {
                    nearestX = point.x
                    minDistance = distance
                }
            }
        }
        return points.filter { it.x == nearestX }
    }

    private fun getIntersectionLegend(point: PointF) =
        if (::correspondingLegends.isInitialized && showCorrespondingLegends) correspondingLegends[point.x].toString()
        else point.y.toString()

    data class ChartPoint(
        var name: String,
        var color: Int,
        var x: Float,
        var y: Float,
        var correspondingLegend: String
    )
}