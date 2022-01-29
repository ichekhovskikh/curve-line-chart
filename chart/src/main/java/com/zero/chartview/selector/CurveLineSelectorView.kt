package com.zero.chartview.selector

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.ColorInt
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

    fun setFrameColor(@ColorInt frameColor: Int) {
        scrollFrame.setFrameColor(frameColor)
    }

    fun setFogColor(@ColorInt fogColor: Int) {
        scrollFrame.setFogColor(fogColor)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setOnTouchListener(listener: OnTouchListener?) {
        scrollFrame.setOnTouchListener(listener)
    }
}
