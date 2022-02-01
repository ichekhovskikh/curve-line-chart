package com.zero.sample

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.zero.chartview.axis.formatter.ShortAxisFormatter
import com.zero.chartview.model.IntersectionPoint
import com.zero.chartview.popup.PopupView
import kotlinx.android.synthetic.main.popup_window.view.*

class PopupWindow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : PopupView(context, attrs, defStyleAttr, defStyleRes) {

    private val windowView: View
    private val coordinateViews = mutableListOf<View>()
    private val axisFormatter = ShortAxisFormatter()

    init {
        windowView = LayoutInflater.from(context).inflate(R.layout.popup_window, this, false)
        windowView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
        addView(windowView)
        addLineInfoView(count = 2)
    }

    @Suppress("SameParameterValue")
    private fun addLineInfoView(count: Int) {
        itemContainer.removeAllViews()
        coordinateViews.clear()
        (0 until count).forEach { _ ->
            val view = LayoutInflater.from(context).inflate(
                R.layout.item_coordinate_container,
                itemContainer,
                false
            )
            itemContainer.addView(view)
            coordinateViews.add(view)
            view.visibility = View.GONE
        }
    }

    override fun bind(xPixel: Float?, intersections: List<IntersectionPoint>) {
        coordinateViews.forEachIndexed { index, view ->
            val chartPoint = intersections.getOrNull(index)
            if (chartPoint != null) {
                val nameView: TextView = view.findViewById(R.id.name)
                val xValueView: TextView = view.findViewById(R.id.xValue)
                val yValueView: TextView = view.findViewById(R.id.yValue)
                nameView.text = chartPoint.lineName
                nameView.setTextColor(chartPoint.lineColor)
                xValueView.setTextColor(chartPoint.lineColor)
                yValueView.setTextColor(chartPoint.lineColor)
                xValueView.text = axisFormatter.format(chartPoint.x, 1f)
                yValueView.text = axisFormatter.format(chartPoint.y, 1f)
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.GONE
            }
        }
        setPopupPosition(xPixel)
    }

    private fun setPopupPosition(xPixel: Float?) {
        val halfPopupWidth = popupWindow.measuredWidth / 2
        if (xPixel != null && (xPixel + halfPopupWidth) > measuredWidth) {
            x = measuredWidth - popupWindow.measuredWidth.toFloat()
        } else if (xPixel != null && (xPixel - halfPopupWidth) < 0) {
            x = 0f
        } else if (xPixel != null) {
            x = xPixel - halfPopupWidth
        }
        y = 0f
    }

    override fun setBackgroundColor(color: Int) {
        windowView.setBackgroundColor(color)
    }
}
