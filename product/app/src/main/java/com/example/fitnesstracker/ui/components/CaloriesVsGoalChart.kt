package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.fitnesstracker.data.food.UserNutritionDiary
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * CalorieVsGoalChart composable displays a grouped bar chart comparing daily calorie intake against goal.
 * - Shows last 7 days with two bars per day: calories consumed (blue) vs goal (green).
 * - Calories bar is red when exceeding goal, blue when under goal.
 */
@Composable
fun CaloriesVsGoalChart(
    userNutritionDiary: Map<String, UserNutritionDiary>,
    goalCalories: Int
) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            BarChart(context).apply {
                setDrawGridBackground(false) // Removes grid background for cleaner look.
                description.isEnabled = false // Disables chart description text.
                legend.isEnabled = true // Shows legend to distinguish consumed vs goal.
                legend.setCustom(listOf(
                    LegendEntry(
                        "Consumed",
                        Legend.LegendForm.SQUARE,
                        10f,
                        Float.NaN,
                        null,
                        Color(0xFF42A5F5).toArgb()
                    ),
                    LegendEntry("Goal", Legend.LegendForm.SQUARE, 10f, Float.NaN, null, Color(0xFF6AD76F).toArgb())
                ))
                axisRight.isEnabled = false // Disables right Y-axis, only left axis used.
                xAxis.position = XAxis.XAxisPosition.BOTTOM // Positions X-axis labels at bottom.
                xAxis.granularity = 1f // Sets interval between X-axis labels to 1 day.
                xAxis.setDrawGridLines(false) // Removes vertical grid lines.
                axisLeft.setDrawGridLines(false) // Removes horizontal grid lines.
                xAxis.setDrawLabels(true) // Shows day labels (Mon, Tue, etc.).
                xAxis.setDrawAxisLine(true) // Shows X-axis line.
                axisLeft.setDrawAxisLine(true) // Shows Y-axis line.
                setTouchEnabled(true) // Enables touch interactions.
                setDragEnabled(true) // Enables dragging to pan chart.
                setScaleEnabled(true) // Pinch zoom.
                setPinchZoom(true) // Pinch zoom gesture.
                axisLeft.axisMinimum = 0f // Bars start from Y=0 (touch X-axis).
                xAxis.axisMinimum = 0f // Centers grouped bars on X-axis.
                xAxis.axisMaximum = 7f // 7 days with spacing for grouped bars.
            }
        },

        // Recomposes chart whenever userNutritionDiary or goalCalories change.
        update = { chart ->

            // Generates last 7 days in chronological order (oldest first) for chart left-to-right display.
            val last7Days = (0..6).map { LocalDate.now().minusDays((6 - it).toLong()) }

            // Creates entries for calories consumed (first bar in each group).
            val consumedEntries = last7Days.mapIndexed { index, date ->
                val diaryEntry = userNutritionDiary[date.toString()]
                val cals = diaryEntry?.totalCalories ?: 0 // Defaults to 0 if no data for date.
                BarEntry(index.toFloat(), cals.toFloat())
            }

            // Creates entries for goal (second bar in each group) which uses historical goal if available.
            val goalEntries = last7Days.mapIndexed { index, date ->
                val diaryEntry = userNutritionDiary[date.toString()]
                val goal = diaryEntry?.goalCalories?.takeIf { it > 0 } ?: goalCalories // Uses stored historical goal from diary if available otherwise falls back to current profile goal.
                BarEntry(index.toFloat(), goal.toFloat())
            }

            // Datasets for calories consumed with color coding.
            val consumedDataSet = BarDataSet(consumedEntries, "Consumed").apply {
                colors = last7Days.mapIndexed { _, date ->
                    val diaryEntry = userNutritionDiary[date.toString()]
                    val cals = diaryEntry?.totalCalories ?: 0
                    val goal = diaryEntry?.goalCalories?.takeIf { it > 0 } ?: goalCalories

                    // Red for surplus days & blue for days under goal.
                    if (cals > goal) Color.Red.toArgb() else Color(0xFF42A5F5).toArgb()
                }
                valueTextSize = 8f // Sets calories value text size above bars.
            }

            // Datasets for goal (green bars).
            val goalDataSet = BarDataSet(goalEntries, "Goal").apply {
                color = Color(0xFF6AD76F).toArgb() // Green for goal bars.
                valueTextSize = 8f // Sets goal value text size above bars.
            }

            // Configures grouped bar data.
            val barData = BarData(consumedDataSet, goalDataSet)
            
            val groupSpace = 0.08f
            val barSpace = 0.02f
            val barWidth = 0.41f
            
            barData.barWidth = barWidth

            // Applies data to chart.
            chart.data = barData

            // Formats X-axis labels as abbreviated day names (Mon, Tue, etc.).
            val dayLabels = last7Days.map {
                it.format(DateTimeFormatter.ofPattern("EEE"))
            }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(dayLabels)
            chart.xAxis.setCenterAxisLabels(true)
            chart.xAxis.axisMinimum = 0f
            chart.xAxis.axisMaximum = 7f
            chart.xAxis.granularity = 1f

            // Groups the bars side by side starting from 0.0.
            chart.groupBars(0f, groupSpace, barSpace)

            chart.invalidate() // Forces immediate redraw with updated data.
        }
    )
}
