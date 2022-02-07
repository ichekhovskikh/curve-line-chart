package com.chekh.sample

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.chekh.chartview.axis.formatter.ShortAxisFormatter
import com.chekh.chartview.model.IntersectionPoint
import com.chekh.chartview.popup.PopupView

class CustomPopupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : PopupView(context, attrs, defStyleAttr, defStyleRes) {

    private val axisFormatter = ShortAxisFormatter()

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.view_popup, this, true)

        addLineInfoViews(count = 2)
    }

    @Suppress("SameParameterValue")
    private fun addLineInfoViews(count: Int) {
        val llContainer = findViewById<LinearLayout>(R.id.llContainer).apply {
            removeAllViews()
        }
        (0 until count).forEach { _ ->
            LayoutInflater
                .from(context)
                .inflate(R.layout.view_item_line_info, llContainer, true)
        }
    }

    override fun bind(xPixel: Float?, intersections: List<IntersectionPoint>) {
        val llContainer = findViewById<LinearLayout>(R.id.llContainer)
        (0 until llContainer.childCount).forEach { index ->
            val view = llContainer.getChildAt(index)
            val intersection = intersections.getOrNull(index)
            if (intersection == null) {
                view.visibility = View.GONE
                return@forEach
            }
            view.findViewById<TextView>(R.id.tvName).apply {
                text = intersection.lineName
                setTextColor(intersection.lineColor)
            }
            view.findViewById<TextView>(R.id.tvXValue).apply {
                text = axisFormatter.format(intersection.x, 1f)
                setTextColor(intersection.lineColor)
            }
            view.findViewById<TextView>(R.id.tvYValue).apply {
                text = axisFormatter.format(intersection.y, 1f)
                setTextColor(intersection.lineColor)
            }
            view.visibility = View.VISIBLE
        }
        xPixel?.let(::setPopupPosition)
    }

    private fun setPopupPosition(xPixel: Float) {
        val llPopup = findViewById<LinearLayout>(R.id.llPopup)
        val popupCenter = llPopup.measuredWidth / 2
        x = when {
            xPixel + popupCenter > measuredWidth -> {
                measuredWidth.toFloat() - llPopup.measuredWidth
            }
            xPixel - popupCenter < 0 -> 0f
            else -> xPixel - popupCenter
        }
    }
}
