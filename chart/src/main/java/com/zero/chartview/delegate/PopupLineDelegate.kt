package com.zero.chartview.delegate

import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import com.zero.chartview.extensions.getIntersections
import com.zero.chartview.extensions.getMinMaxY
import com.zero.chartview.extensions.interpolateByLineAbscissas
import com.zero.chartview.model.*
import com.zero.chartview.model.Size
import com.zero.chartview.tools.xPixelToValue
import com.zero.chartview.tools.xValueToPixel
import com.zero.chartview.tools.yValueToPixel

internal class PopupLineDelegate(
    internal val linePaint: Paint,
    internal val pointInnerPaint: Paint,
    internal val pointOuterPaint: Paint,
    private val pointInnerRadius: Float,
    private val pointOuterRadius: Float,
    private val deltaTrackingTouchPercent: Float,
    private val onUpdate: () -> Unit
) {

    private var viewSize = Size()
    private var lines = emptyList<CurveLine>()
    private var touchX: Float? = null
    private var popupLine: PopupLine? = null

    internal var range = FloatRange(0f, 1f)
        private set

    private val intersections
        get() = popupLine?.intersections?.map { it.point }.orEmpty()

    private val onIntersectionsChangedListeners = mutableListOf<(List<IntersectionPoint>) -> Unit>()

    fun setRange(range: FloatRange) {
        this.range = range
        touchX = null
        onPopupLineChanged()
        onUpdate()
    }

    fun setLines(lines: List<CurveLine>) {
        this.lines = lines
        touchX = null
        onPopupLineChanged()
        onUpdate()
    }

    fun addOnIntersectionsChangedListener(onIntersectionsChangedListener: (List<IntersectionPoint>) -> Unit) {
        onIntersectionsChangedListeners.add(onIntersectionsChangedListener)
    }

    fun removeOnIntersectionsChangedListener(onIntersectionsChangedListener: (List<IntersectionPoint>) -> Unit) {
        onIntersectionsChangedListeners.remove(onIntersectionsChangedListener)
    }

    private fun onIntersectionsChanged() {
        onIntersectionsChangedListeners.forEach { it(intersections) }
    }

    fun onTouchEvent(event: MotionEvent) = when (event.actionMasked) {
        MotionEvent.ACTION_DOWN -> {
            touchX = event.x
            onPopupLineChanged()
            onUpdate()
            false
        }
        MotionEvent.ACTION_MOVE -> {
            touchX = null
            onPopupLineChanged()
            onUpdate()
            false
        }
        else -> false
    }

    fun onMeasure(viewSize: Size) {
        this.viewSize = viewSize
    }

    fun onLayout() {
        touchX = null
        onPopupLineChanged()
    }

    private fun onPopupLineChanged() {
        popupLine = calculatePopupLine()
        onIntersectionsChanged()
    }

    private fun calculatePopupLine(): PopupLine? {
        val touchX = touchX ?: return null
        val (startValue, endValue) = range.interpolateByLineAbscissas(lines)
        val intersections = lines.getIntersections(touchX, startValue, endValue)
        val intersectionX = intersections.firstOrNull()?.x ?: return null
        val (minY, maxY) = lines.getMinMaxY(range)
        val xDrawPixel = xValueToPixel(intersectionX, viewSize.width, startValue, endValue)
        return PopupLine(
            xDrawPixel = xDrawPixel,
            intersections = intersections.map {
                PopupLine.Intersection(
                    point = it,
                    xDrawPixel = xDrawPixel,
                    yDrawPixel = yValueToPixel(it.y, viewSize.height, minY, maxY)
                )
            }
        )
    }

    fun drawPopupLine(canvas: Canvas) {
        val popupLine = popupLine ?: return
        canvas.drawPopupLine(popupLine)
        popupLine.intersections.forEach { canvas.drawIntersectionPoint(it) }
    }

    private fun Canvas.drawPopupLine(popupLine: PopupLine) {
        drawLine(
            popupLine.xDrawPixel,
            0f,
            popupLine.xDrawPixel,
            viewSize.height.toFloat(),
            linePaint
        )
    }

    private fun Canvas.drawIntersectionPoint(intersection: PopupLine.Intersection) {
        pointOuterPaint.color = intersection.point.lineColor
        drawCircle(
            intersection.xDrawPixel,
            intersection.yDrawPixel,
            pointOuterRadius,
            pointOuterPaint
        )
        drawCircle(
            intersection.xDrawPixel,
            intersection.yDrawPixel,
            pointInnerRadius,
            pointInnerPaint
        )
    }

    private fun List<CurveLine>.getIntersections(
        xPixel: Float,
        minX: Float,
        maxX: Float
    ): List<IntersectionPoint> {
        val xValue = xPixelToValue(xPixel, viewSize.width, minX, maxX)
        val delta = (maxX - minX) * deltaTrackingTouchPercent
        return this.getIntersections(xValue, delta)
    }
}
