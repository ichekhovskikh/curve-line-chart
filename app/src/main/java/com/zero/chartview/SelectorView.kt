package com.zero.chartview

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.graphics.Paint
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import com.zero.chartview.model.FloatRange

class SelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val framePaint = Paint()
    private val fogPaint = Paint()

    private var frameThicknessHorizontal: Float = resources.getDimension(R.dimen.frame_thickness_horizontal_default)
    private var frameThicknessVertical: Float = resources.getDimension(R.dimen.frame_thickness_vertical_default)
    private var frameMaxWidthPercent: Float = resources.getDimension(R.dimen.frame_max_width_percent_default)
    private var frameMinWidthPercent: Float = resources.getDimension(R.dimen.frame_min_width_percent_default)

    private lateinit var range: MutableLiveData<FloatRange>

    init {
        range.value = FloatRange(0f, 0f)
        context.theme.obtainStyledAttributes(attrs, R.styleable.SelectorView, defStyleAttr, defStyleRes).apply {
            frameThicknessHorizontal = getDimension(R.styleable.SelectorView_frameThicknessHorizontal, frameThicknessHorizontal)
            frameThicknessVertical = getDimension(R.styleable.SelectorView_frameThicknessVertical, frameThicknessVertical)
            frameMaxWidthPercent = getDimension(R.styleable.SelectorView_frameMaxWidthPercent, frameMaxWidthPercent)
            frameMinWidthPercent = getDimension(R.styleable.SelectorView_frameMinWidthPercent, frameMinWidthPercent)
            recycle()
        }
    }

    fun setRange(start: Float, endInclusive: Float) {
        range.value = FloatRange(start, endInclusive)
        invalidate()
    }

    fun getRange(): LiveData<FloatRange> = range

    fun setFrameControlColor(@ColorInt frameControlColor: Int) {
        framePaint.color = frameControlColor
        invalidate()
    }

    fun setFogControlColor(@ColorInt fogControlColor: Int) {
        fogPaint.color = fogControlColor
        invalidate()
    }

    fun setThemeColor(@ColorInt frameControlColor: Int, @ColorInt fogControlColor: Int) {
        framePaint.color = frameControlColor
        fogPaint.color = fogControlColor
        invalidate()
    }
}