package com.zero.chartview.popup

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.TextView
import com.zero.chartview.R
import com.zero.chartview.model.CurveLine
import kotlinx.android.synthetic.main.popup_window.view.*
import android.view.animation.AnimationUtils
import com.zero.chartview.axis.formatter.ShortAxisFormatter
import com.zero.chartview.model.IntersectionPoint
import com.zero.chartview.tools.AnimatorListenerAdapter

internal class PopupWindow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val windowView: View
    private val coordinateViews = mutableListOf<View>()
    private val axisFormatter = ShortAxisFormatter()

    private var animationOut = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right)
    private var animationIn = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)

    init {
        visibility = View.INVISIBLE
        windowView = LayoutInflater.from(context).inflate(R.layout.popup_window, this, false)
        windowView.setBackgroundColor(Color.WHITE)
        addView(windowView)
    }

    fun setLines(lines: List<CurveLine>) {
        itemContainer.removeAllViews()
        coordinateViews.clear()
        lines.forEach { line ->
            val view = LayoutInflater.from(context).inflate(R.layout.item_coordinate_container, itemContainer, false)
            val nameView: TextView = view.findViewById(R.id.name)
            val xValueView: TextView = view.findViewById(R.id.xValue)
            val yValueView: TextView = view.findViewById(R.id.yValue)
            nameView.text = line.name
            nameView.setTextColor(line.color)
            xValueView.setTextColor(line.color)
            yValueView.setTextColor(line.color)

            itemContainer.addView(view)
            coordinateViews.add(view)
        }
    }

    fun fill(xPixel: Float?, pointsLine: List<IntersectionPoint>) {
        coordinateViews.forEach { view ->
            val nameView: TextView = view.findViewById(R.id.name)
            val chartPoint = pointsLine.getByName(nameView.text.toString())
            if (chartPoint != null) {
                val xValueView: TextView = view.findViewById(R.id.xValue)
                val yValueView: TextView = view.findViewById(R.id.yValue)
                xValueView.text = axisFormatter.format(chartPoint.x, 1f)
                yValueView.text = axisFormatter.format(chartPoint.y, 1f)
            }
            val isAppearing = chartPoint != null
            startAnimation(view, isAppearing)
        }
        setPopupPosition(xPixel)
        visibility = View.VISIBLE
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

    private fun startAnimation(view: View, isAppearing: Boolean) {
        if (isAppearing && view.visibility != View.VISIBLE) {
            view.startAnimation(animationIn)
            view.visibility = View.VISIBLE
        } else if (!isAppearing && view.visibility == View.VISIBLE && view.animation == null) {
            view.animation = animationOut
            view.animation.setAnimationListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animation?) {
                    view.visibility = View.GONE
                    view.animation = null
                }
            })
            view.startAnimation(view.animation)
        }
    }

    override fun setBackgroundColor(color: Int) {
        windowView.setBackgroundColor(color)
    }

    private fun List<IntersectionPoint>.getByName(name: String) = this.find { it.lineName == name }
}
