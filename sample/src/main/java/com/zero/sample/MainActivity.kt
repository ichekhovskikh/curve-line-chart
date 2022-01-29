package com.zero.sample

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.zero.chartview.axis.formatter.ShortAxisFormatter
import com.zero.chartview.model.CurveLine
import com.zero.sample.utils.testLines
import kotlinx.android.synthetic.main.activity_main.labels
import kotlinx.android.synthetic.main.activity_main.chart

class MainActivity : AppCompatActivity() {

    private lateinit var view: View

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        view = layoutInflater.inflate(R.layout.activity_main, null)
        setContentView(view)
        initLabels()
        addLabels(testLines)
    }

    private fun initLabels() {
        labels.chart = chart.apply { yAxisFormatter = ShortAxisFormatter() }
    }

    private fun addLabels(lines: List<CurveLine>) {
        lines.forEach { labels.addLineLabel(it) }
    }
}
