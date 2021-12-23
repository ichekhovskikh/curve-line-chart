package com.zero.sample

import android.content.Context
import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import com.zero.chartview.CurveLineChartView
import com.zero.chartview.model.CurveLine

class ChartLabelsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    var chart: CurveLineChartView? = null
    private var items = mutableListOf<View>()

    init {
        orientation = VERTICAL
    }

    fun addLineLabel(line: CurveLine) {
        if (chart == null) return
        val itemView = createLabelItem(line)
        items.add(itemView)
        addView(itemView)
    }

    private fun createLabelItem(line: CurveLine): View {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_label, this, false)
        val labelCheckbox = itemView.findViewById<CheckBox>(R.id.labelCheckbox).apply {
            buttonTintList = getColorStateList(line)
            text = line.name
            isChecked = true
            chart?.addLine(line)
        }
        itemView.setOnClickListener {
            if (labelCheckbox.isChecked) {
                labelCheckbox.isChecked = false
                chart?.removeLine(line)
            } else {
                labelCheckbox.isChecked = true
                chart?.addLine(line)

            }
        }
        return itemView
    }

    private fun getColorStateList(line: CurveLine): ColorStateList {
        val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val colors = intArrayOf(line.color, line.color)
        return ColorStateList(states, colors)
    }

    @ColorInt fun getTextColor(): Int {
        if (items.isEmpty()) return 0
        val labelCheckbox = items.first().findViewById<CheckBox>(R.id.labelCheckbox)
        return labelCheckbox.textColors.defaultColor
    }

    fun setTextColor(@ColorInt color: Int) {
        items.forEach {
            val labelCheckbox = it.findViewById<CheckBox>(R.id.labelCheckbox)
            labelCheckbox.setTextColor(color)
        }
    }
}
