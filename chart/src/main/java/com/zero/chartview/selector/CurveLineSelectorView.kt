package com.zero.chartview.selector

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.zero.chartview.CurveLineGraphView
import com.zero.chartview.model.CurveLine

class CurveLineSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val scrollFrame = ScrollFrameView(context, attrs, defStyleAttr, defStyleRes)
    private val graph = CurveLineGraphView(context, attrs, defStyleAttr, defStyleRes).apply {
        isScrollEnabled = false
    }

    var isSmoothScrollEnabled
        get() = scrollFrame.isSmoothScrollEnabled
        set(value) {
            scrollFrame.isSmoothScrollEnabled = value
        }

    val frameMinWidthPercent
        get() = scrollFrame.frameMinWidthPercent

    val frameMaxWidthPercent
        get() = scrollFrame.frameMaxWidthPercent

    var frameColor: Int
        get() = scrollFrame.frameColor
        set(value) {
            scrollFrame.frameColor = value
        }

    var fogColor: Int
        get() = scrollFrame.fogColor
        set(value) {
            scrollFrame.fogColor = value
        }

    init {
        addView(graph)
        addView(scrollFrame)
    }

    fun setRange(start: Float, endInclusive: Float, smoothScroll: Boolean = false) {
        scrollFrame.setRange(start, endInclusive, smoothScroll)
    }

    fun addOnRangeChangedListener(onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit) {
        scrollFrame.addOnRangeChangedListener(onRangeChangedListener)
    }

    fun removeOnRangeChangedListener(onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit) {
        scrollFrame.removeOnRangeChangedListener(onRangeChangedListener)
    }

    fun setLines(lines: List<CurveLine>) {
        graph.setLines(lines)
    }

    fun addLine(line: CurveLine) {
        graph.addLine(line)
    }

    fun removeLine(index: Int) {
        val lines = graph.getLines()
        removeLine(lines[index])
    }

    fun removeLine(line: CurveLine) {
        graph.removeLine(line)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setOnTouchListener(listener: OnTouchListener?) {
        scrollFrame.setOnTouchListener(listener)
    }
}
