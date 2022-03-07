package com.chekh.chartview.selector

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import com.chekh.chartview.CurveLineGraphView
import com.chekh.chartview.R
import com.chekh.chartview.model.CurveLine

/**
 * This view allows you to select a vertical area of the graph to display
 */
class CurveLineSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val scrollFrame = ScrollFrameView(context, attrs, defStyleAttr, defStyleRes)
    private val graph = CurveLineGraphView(context, attrs, defStyleAttr, defStyleRes)

    /**
     * Vertical area of the graph to display
     */
    val range get() = scrollFrame.range

    /**
     * @return true is currently in the state of smooth scrolling
     */
    var isSmoothScrollEnabled
        get() = scrollFrame.isSmoothScrollEnabled
        set(value) {
            scrollFrame.isSmoothScrollEnabled = value
        }

    /**
     * @return minimum frame length as a percentage
     */
    val frameMinWidthPercent
        get() = scrollFrame.frameMinWidthPercent

    /**
     * @return maximum frame length as a percentage
     */
    val frameMaxWidthPercent
        get() = scrollFrame.frameMaxWidthPercent

    /**
     * Color of the scroll frame
     */
    @get:ColorInt
    @setparam:ColorInt
    var frameColor: Int
        get() = scrollFrame.frameColor
        set(value) {
            scrollFrame.frameColor = value
        }

    /**
     * Fog color of the unselected area
     */
    @get:ColorInt
    @setparam:ColorInt
    var fogColor: Int
        get() = scrollFrame.fogColor
        set(value) {
            scrollFrame.fogColor = value
        }

    init {
        graph.id = R.id.curve_line_selector_graph_view
        scrollFrame.id = R.id.curve_line_selector_scroll_frame_view

        addView(graph)
        addView(scrollFrame)
    }

    /**
     * Set a new scroll frame position
     * @param start the left border of the selected area as a percentage
     * @param endInclusive the right border of the selected area as a percentage
     * @param smoothScroll allows to support smooth scrolling
     */
    fun setRange(start: Float, endInclusive: Float, smoothScroll: Boolean = false) {
        scrollFrame.setRange(start, endInclusive, smoothScroll)
    }

    /**
     * Set a new lines for this [CurveLineSelectorView]
     * @param lines to be set
     */
    fun setLines(lines: List<CurveLine>) {
        graph.setLines(lines)
    }

    /**
     * Add a new line into this [CurveLineSelectorView]
     * @param line to be added
     */
    fun addLine(line: CurveLine) {
        graph.addLine(line)
    }

    /**
     * Remove a current line from this [CurveLineSelectorView]
     * @param index of the line to remove
     */
    fun removeLine(index: Int) {
        graph.removeLine(index)
    }

    /**
     * Remove a current line from this [CurveLineSelectorView]
     * @param line to be removed
     */
    fun removeLine(line: CurveLine) {
        graph.removeLine(line)
    }

    /**
     * Add a listener that will be notified of any changes in displayed area of the graph
     * @param onRangeChangedListener listener to set
     */
    fun addOnRangeChangedListener(
        onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit
    ) {
        scrollFrame.addOnRangeChangedListener(onRangeChangedListener)
    }

    /**
     * Remove a listener that was notified of any changes in displayed area of the graph
     * @param onRangeChangedListener listener to set or null to clear
     */
    fun removeOnRangeChangedListener(
        onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit
    ) {
        scrollFrame.removeOnRangeChangedListener(onRangeChangedListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setOnTouchListener(listener: OnTouchListener?) {
        scrollFrame.setOnTouchListener(listener)
    }
}
