package com.zero.chartview.popup

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.zero.chartview.R
import com.zero.chartview.model.CurveLine
import kotlinx.android.synthetic.main.popup_window.view.*

class PopupWindow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val coordinateViews = mutableListOf<View>()

    init {
        visibility = View.GONE
        val windowView = LayoutInflater.from(context).inflate(R.layout.popup_window, this, false)
        addView(windowView)
    }

    fun setLines(lines: List<CurveLine>) {
        itemContainer.removeAllViews()
        lines.map { line ->
            val view = LayoutInflater.from(context).inflate(R.layout.item_coordinate_container, itemContainer, false)
            val nameView: TextView = view.findViewById(R.id.name)
            val xValueView: TextView = view.findViewById(R.id.xValue)
            val yValueView: TextView = view.findViewById(R.id.yValue)
            nameView.text = line.name
            nameView.setTextColor(line.color)
            xValueView.setTextColor(line.color)
            yValueView.setTextColor(line.color)
        }
        requestLayout()
    }

    fun fill(xPixel: Float, xValue: String, yValues: List<String>) {
        coordinateViews.forEachIndexed { index, view ->
            val xValueView: TextView = view.findViewById(R.id.xValue)
            val yValueView: TextView = view.findViewById(R.id.yValue)
            xValueView.text = xValue
            yValueView.text = yValues[index]
        }
        x = xPixel
        requestLayout()
    }
}