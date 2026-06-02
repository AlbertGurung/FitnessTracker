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
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * DailyTotalMacrosChart composable displays a grouped bar chart showing daily macro intake.
 * - Shows last 7 days with three bars per day: protein (blue), carbs (orange), fat (red).
 * - Allows users to compare macro consumption patterns across the week.
 */@Composable
fun DailyTotalMacrosChart(
    userNutritionDiary: Map<String, UserNutritionDiary>
) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        factory = { context ->
            BarChart(context).apply {
                setDrawGridBackground(false) // No background grid lines.
                description.isEnabled = false // No description text on chart.
                legend.isEnabled = true // Shows legend so user knows which color is which macro.
                axisRight.isEnabled = false // Only left side numbers.
                xAxis.position = XAxis.XAxisPosition.BOTTOM // Day names at bottom.
                xAxis.granularity = 1f // One day per label.
                xAxis.setDrawGridLines(false) // No vertical lines.
                axisLeft.setDrawGridLines(false) // No horizontal lines.
                xAxis.setDrawLabels(true) // Shows day names.
                xAxis.setDrawAxisLine(true) // Shows bottom line.
                axisLeft.setDrawAxisLine(true) // Shows left side line.
                axisLeft.axisMinimum = 0f // Bars start at zero.
                xAxis.axisMinimum = 0f // Chart starts at zero.
                xAxis.axisMaximum = 7f // 7 days shown.
                setTouchEnabled(true) // Can touch and move chart.
                setDragEnabled(true) // Can drag to scroll.
                setScaleEnabled(true) // Can zoom with fingers.
                setPinchZoom(true) // Pinch to zoom.
            }
        },
        update = { chart ->
            // Gets last 7 days from oldest to newest.
            val last7Days = (0..6).map { LocalDate.now().minusDays((6 - it).toLong()) }

            // Protein data for first bar.
            val proteinEntries = last7Days.mapIndexed { index, date ->
                val protein = userNutritionDiary[date.toString()]?.totalProtein ?: 0
                BarEntry(index.toFloat(), protein.toFloat())
            }

            // Carbs data for second bar.
            val carbsEntries = last7Days.mapIndexed { index, date ->
                val carbs = userNutritionDiary[date.toString()]?.totalCarbs ?: 0
                BarEntry(index.toFloat(), carbs.toFloat())
            }

            // Fat data for third bar.
            val fatEntries = last7Days.mapIndexed { index, date ->
                val fat = userNutritionDiary[date.toString()]?.totalFat ?: 0
                BarEntry(index.toFloat(), fat.toFloat())
            }

            // Blue bars for protein.
            val proteinDataSet = BarDataSet(proteinEntries, "Protein").apply {
                color = Color(0xFF42A5F5).toArgb()
                valueTextSize = 8f // Number size above bar.
            }

            // Orange bars for carbs.
            val carbsDataSet = BarDataSet(carbsEntries, "Carbs").apply {
                color = Color(0xFFFFA726).toArgb()
                valueTextSize = 8f
            }

            // Red bars for fat.
            val fatDataSet = BarDataSet(fatEntries, "Fat").apply {
                color = Color(0xFFEF5350).toArgb()
                valueTextSize = 8f
            }

            // Sets up the 3 bars together.
            val barData = BarData(proteinDataSet, carbsDataSet, fatDataSet)

            // Spacing for 3 bars: (0.27 + 0.02) * 3 + 0.13 = 1.0
            val groupSpace = 0.13f
            val barSpace = 0.02f
            val barWidth = 0.27f

            barData.barWidth = barWidth

            chart.data = barData

            // Shows short day names like Mon, Tue.
            val dayLabels = last7Days.map { it.format(DateTimeFormatter.ofPattern("EEE")) }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(dayLabels)
            chart.xAxis.setCenterAxisLabels(true) // Centers labels under bar groups.
            chart.xAxis.axisMinimum = 0f
            chart.xAxis.axisMaximum = 7f
            chart.xAxis.granularity = 1f

            // Puts 3 bars next to each other for each day.
            chart.groupBars(0f, groupSpace, barSpace)

            chart.invalidate() // Redraws chart with new data.
        }
    )
}