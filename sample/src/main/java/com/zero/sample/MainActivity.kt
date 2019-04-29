package com.zero.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chartLayout.setLines(testLines)
        button.setOnClickListener {
            val countLines = chartLayout.getLines()?.size ?: 0
            if (countLines > 1) {
                chartLayout.removeLine(testLines[0])
            } else {
                chartLayout.addLine(testLines[0])
            }
        }
    }
}
