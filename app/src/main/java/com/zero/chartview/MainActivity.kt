package com.zero.chartview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.zero.chartview.utils.testLines
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chartLayout.setLines(testLines)

        var flag = true
        button.setOnClickListener {
            if (flag) {
                flag = false
                chartLayout.removeLine(testLines[0])
            } else {
                flag = true
                chartLayout.addLine(testLines[0])
            }
        }
    }
}
