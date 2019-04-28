package com.zero.chartview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class ChartLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val chartLayoutView = LayoutInflater.from(context).inflate(R.layout.chart_layout_view, this, false)
    private val titleView: TextView
    private val lineNameContainer: LinearLayout
    private val chartContainer: LinearLayout

    private lateinit var chart: ChartView
    private lateinit var control: ChartControlView

    init {
        titleView = chartLayoutView.findViewById(R.id.title)
        chartContainer = chartLayoutView.findViewById(R.id.chartContainer)
        lineNameContainer = chartLayoutView.findViewById(R.id.lineNameContainer)

        chartLayoutView.setBackgroundColor(resources.getColor(R.color.colorBackground))
        addView(chartLayoutView)
    }

    override fun addView(child: View) {
        if (child is ChartView) {
            chart = child
            //TODO
        } else if (child is ChartControlView) {
            control = child
            //TODO
        } else {
            super.addView(child)
        }
    }

    override fun setBackgroundColor(color: Int) {
        chartLayoutView.setBackgroundColor(color)
    }
}