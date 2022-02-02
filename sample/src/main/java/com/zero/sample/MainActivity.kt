package com.zero.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.zero.chartview.CurveLineChartView
import com.zero.chartview.axis.formatter.ShortAxisFormatter
import com.zero.sample.utils.testLines

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setupViews()
    }

    private fun setupViews() {
        val clcvChart = findViewById<CurveLineChartView>(R.id.clcvChart).apply {
            xAxisFormatter = ShortAxisFormatter()
            yAxisFormatter = ShortAxisFormatter()
        }
        val clvLabels = findViewById<ChartLabelsView>(R.id.clvLabels)
        clvLabels.setOnCheckboxChangedListener { isChecked, line ->
            if (isChecked) clcvChart.addLine(line) else clcvChart.removeLine(line)
        }
        testLines.forEach(clvLabels::addLabel)
    }
}
