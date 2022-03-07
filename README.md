# Curve Line Chart
[![Release](https://jitpack.io/v/ichekhovskikh/curve-line-chart.svg)](https://jitpack.io/#ichekhovskikh/curve-line-chart)
[![Kotlin Version](https://img.shields.io/badge/Kotlin-1.6.10-blue.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-21%2B-red.svg?style=flat)](https://android-arsenal.com/api?level=21)

<table>
    <tr>
        <td>
            <img src="https://github.com/ichekhovskikh/ChartView/blob/master/screenshots/screen%20recording.gif" width="256"/>
        </td>
    </tr>
    <tr>
        <td>
            Screen recording
        </td>
    </tr>
</table>

Meet Curve Line Chart, a android library for drawing a line graph.

## Setup
To get a Git project into your build:

***Step 1.*** Add the JitPack repository to your build file

Add the dependency in your build.gradle:
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
```
***Step 2.*** Add the dependency
```gradle
dependencies {
    implementation 'com.github.ichekhovskikh:curve-line-chart:X.X.X'
}
```

## Usage

### CurveLineChartLayout
<table>
    <tr>
        <td>
            <img src="https://github.com/ichekhovskikh/ChartView/blob/master/screenshots/CurveLineChartLayout.png" width="256"/>
        </td>
    </tr>
    <tr>
        <td>
            CurveLineChartLayout Preview
        </td>
    </tr>
</table>
<br>

Using from xml:

```xml
<com.chekh.chartview.CurveLineChartLayout     
    ... >

    <com.chekh.chartview.CurveLineChartView
        ... />

    <com.chekh.chartview.selector.CurveLineSelectorView
        ... />

</com.chekh.chartview.CurveLineChartLayout>
```

### CurveLineChartView
<table>
    <tr>
        <td>
            <img src="https://github.com/ichekhovskikh/ChartView/blob/master/screenshots/CurveLineChartView.png" width="256"/>
        </td>
    </tr>
    <tr>
        <td>
            CurveLineChartView Preview
        </td>
    </tr>
</table>
<br>

Using from xml:

```xml
<com.chekh.chartview.CurveLineChartView
    app:lineWidth="dimension"
    app:scrollEnabled="boolean"
    app:axisLineWidth="dimension"
    app:axisLineColor="color"
    app:yLegendCount="integer"
    app:yLegendTextSize="dimension"
    app:yLegendMarginBottom="dimension"
    app:yLegendMarginStart="dimension"
    app:yLegendTextColor="color"
    app:yLegendLinesVisible="boolean"
    app:xLegendTextColor="color"
    app:xLegendTextSize="dimension"
    app:xLegendMarginTop="dimension"
    app:xLegendMarginHorizontalPercent="fraction"
    app:xLegendCount="integer"
    app:xLegendLinesVisible="boolean"
    app:popupLineColor="color"
    app:popupLinePointInnerColor="color"
    app:popupLineWidth="dimension"
    app:popupView="string"
    ... />
```
#### Axis

To change the displayed text of the axes you need to set the values of the xAxisFormatter or yAxisFormatter:
```kotlin
findViewById<CurveLineChartView>(R.id.clcvChart).apply {
    xAxisFormatter = DefaultAxisFormatter()
    yAxisFormatter = DefaultAxisFormatter()
}
```
*The library has two implemented formatters in its set.*

DefaultAxisFormatter converts a value to a string without changes (used by default):
- 1000 -> "1000"

ShortAxisFormatter uses a shortened number format:
- 1000000000000 -> "1mm"
- 1000000 -> "1m"
- 1000 -> "1k"
- 1 -> "1"
- 0.000001 -> "1e-6"
- 0.000000001 -> "1e-9"

You can create your custom axis formatter.  To do this, implement the AxisFormatter interface:

```kotlin
class CustomAxisFormatter : AxisFormatter {

    override fun format(value: Float, zoom: Float): String {
        ...
    }
}
```

#### PopupView
You can create a view that pops up when you touch the graph:

***Step 1.*** Implement the PopupView abstract class
```kotlin
package com.chekh.sample

class CustomPopupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : PopupView(context, attrs, defStyleAttr, defStyleRes) {

    override fun bind(xPixel: Float?, intersections: List<IntersectionPoint>) {
        ...
    }
}
```

***Step 2.*** Set your custom PopupView from xml
```xml
<com.chekh.chartview.CurveLineChartView
    android:id="@+id/clcvChart"
    app:popupView="com.chekh.sample.CustomPopupView"
    ... />
```
or from kotlin
```kotlin
findViewById<CurveLineChartView>(R.id.clcvChart).apply {
    popupView = CustomPopupView(context)
}
```

### CurveLineSelectorView
<table>
    <tr>
        <td>
            <img src="https://github.com/ichekhovskikh/ChartView/blob/master/screenshots/CurveLineSelectorView.png" width="256"/>
        </td>
    </tr>
    <tr>
        <td>
            CurveLineSelectorView Preview
        </td>
    </tr>
</table>
<br>

Using from xml:

```xml
<com.chekh.chartview.selector.CurveLineSelectorView
    app:selectorFrameColor="color"
    app:selectorFogColor="color"
    app:selectorFrameMinWidthPercent="fraction"
    app:selectorFrameMaxWidthPercent="fraction"
    app:smoothScrollEnabled="boolean"
    ... />
```

### CurveLineGraphView
<table>
    <tr>
        <td>
            <img src="https://github.com/ichekhovskikh/ChartView/blob/master/screenshots/CurveLineGraphView.png" width="256"/>
        </td>
    </tr>
    <tr>
        <td>
            CurveLineGraphView Preview
        </td>
    </tr>
</table>
<br>

Using from xml:

```xml
<com.chekh.chartview.CurveLineGraphView
    app:lineWidth="dimension"
    app:scrollEnabled="boolean"
    ... />
```
