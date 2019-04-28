package com.zero.chartview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.zero.chartview.utils.testLines
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        control.addRangeChangedInvoker {
            chart.setRange(it.start, it.endInclusive)
        }
        chart.addLinesChangedInvoker {
            control.setLines(it)
        }
        chart.setLines(testLines)

        var flag = true
        button.setOnClickListener {
            if (flag) {
                flag = false
                chart.removeLine(0)
                chart.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark))
            } else {
                flag = true
                chart.setLines(testLines)
                chart.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
            }
        }
    }
}
