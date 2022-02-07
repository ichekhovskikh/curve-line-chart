package com.chekh.sample

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.chekh.chartview.model.CurveLine

class ChartLabelsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var onCheckboxChanged: ((isChecked: Boolean, line: CurveLine) -> Unit)? = null

    init {
        orientation = VERTICAL
    }

    fun setOnCheckboxChangedListener(onCheckboxChangedListener: ((isChecked: Boolean, line: CurveLine) -> Unit)?) {
        onCheckboxChanged = onCheckboxChangedListener
    }

    fun addLabel(line: CurveLine) {
        addView(createLabel(line))
    }

    private fun createLabel(line: CurveLine): View {
        val flLabel = LayoutInflater
            .from(context)
            .inflate(R.layout.view_item_label, this, false)

        val cbLabel = flLabel.findViewById<CheckBox>(R.id.cbLabel).apply {
            buttonTintList = line.color.toColorStateList()
            text = line.name
            isChecked = true
            onCheckboxChanged?.invoke(isChecked, line)
        }
        flLabel.setOnClickListener {
            cbLabel.isChecked = !cbLabel.isChecked
            onCheckboxChanged?.invoke(cbLabel.isChecked, line)
        }
        return flLabel
    }

    private fun Int.toColorStateList(): ColorStateList {
        val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val colors = intArrayOf(this, this)
        return ColorStateList(states, colors)
    }
}
