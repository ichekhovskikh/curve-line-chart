package com.zero.chartview.popup

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.TextView
import com.zero.chartview.R
import com.zero.chartview.model.CurveLine
import com.zero.chartview.utils.getByName
import kotlinx.android.synthetic.main.popup_window.view.*
import android.view.animation.AnimationUtils
import com.zero.chartview.utils.AnimatorListenerAdapter

class PopupWindow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val coordinateViews = mutableListOf<View>()

    private var animationOut = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right)
    private var animationIn = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)

    init {
        visibility = View.INVISIBLE
        val windowView = LayoutInflater.from(context).inflate(R.layout.popup_window, this, false)
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

    fun fill(xPixel: Float?, chartPoints: List<ChartPopupView.ChartPoint>) {
        coordinateViews.forEachIndexed { index, view ->
            val nameView: TextView = view.findViewById(R.id.name)
            val chartPoint = chartPoints.getByName(nameView.text.toString())
            if (chartPoint != null) {
                val xValueView: TextView = view.findViewById(R.id.xValue)
                val yValueView: TextView = view.findViewById(R.id.yValue)
                xValueView.text = chartPoint.x.toString()
                yValueView.text = chartPoint.correspondingLegend
                if (view.visibility != View.VISIBLE) {
                    view.startAnimation(animationIn)
                    view.visibility = View.VISIBLE
                }
            } else if (view.visibility == View.VISIBLE) {
                animationOut.setAnimationListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animation?) {
                        view.visibility = View.GONE
                    }
                })
                view.startAnimation(animationOut)
            }
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
    }
}