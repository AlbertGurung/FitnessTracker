package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.fitnesstracker.data.workout.ExerciseProgressEntry
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Composable for displaying exercise weight progression as a line chart.
 * - Uses MPAndroidChart LineChart to render weight history over time.
 * - X-axis shows formatted dates (dd MMM), Y-axis shows weight in kg.
 * - Displays purple line with circle data points.
 * - Sorts entries chronologically (oldest to newest) for left-to-right progression.
 * - Handles empty state with "No progression records" message.
 */
@Composable
fun ExerciseProgressChart(
    entries: List<ExerciseProgressEntry>, // Weight progress entries to display.
    modifier: Modifier = Modifier

) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
        factory = { context ->
            LineChart(context).apply {
                setDrawGridBackground(false) // Disables grid background.
                description.isEnabled = false // Hides chart description text.
                legend.isEnabled = false // Hides legend.

                setTouchEnabled(true) // Enables touch gestures.
                setDragEnabled(true) // Allows dragging the chart.
                setScaleEnabled(true) // Allows scaling the chart.
                setPinchZoom(true) // Enables pinch to zoom.

                axisRight.isEnabled = false // Disables right Y-axis.

                xAxis.position = XAxis.XAxisPosition.BOTTOM // Places X-axis at bottom.
                xAxis.granularity = 1f // Sets minimum interval between labels.
                xAxis.setDrawGridLines(false) // Removes vertical grid lines.
                xAxis.setDrawAxisLine(true) // Shows X-axis line.
                xAxis.labelRotationAngle = 0f // Keeps labels horizontal (no rotation).

                axisLeft.setDrawGridLines(true) // Shows horizontal grid lines.
                axisLeft.gridColor = Color(0xFFE0E0E0).toArgb() // Sets light grey grid colour.
                axisLeft.setDrawAxisLine(true) // Shows Y-axis line.
            }
        },

        update = { chart ->
            if (entries.isEmpty()) {
                chart.clear() // Clears chart when no data.
                chart.setNoDataText("No progression records for selected exercise.") // Shows empty state message.
                chart.invalidate()
                return@AndroidView // Early exit to stop further execution.
            }

            // Sorts oldest to newest for left-to-right progression.
            val sorted = entries.sortedWith(
                compareBy<ExerciseProgressEntry> { it.date }
                    .thenBy { it.id } // Breaks ties by ID for consistent ordering.
            )

            val lineEntries = sorted.mapIndexed { index, e ->
                Entry(index.toFloat(), e.weight.toFloat()) // Maps each entry to X/Y point.
            }

            val dataSet = LineDataSet(lineEntries, "Weight").apply {
                color = Color(0xFF8E24AA).toArgb() // Sets purple line colour.
                lineWidth = 2.8f

                setDrawCircles(true) // Draws circles at data points.
                circleRadius = 4f
                circleHoleRadius = 2f
                setCircleColor(Color(0xFF8E24AA).toArgb())
                circleHoleColor = Color.White.toArgb() // White inner circle.

                setDrawValues(false) // Hides value labels on points.
                mode = LineDataSet.Mode.LINEAR
            }

            chart.data = LineData(dataSet)

            // Formats dates as "dd MMM", falls back to raw string if parsing fails.
            val xLabels = sorted.map { e ->
                runCatching {
                    LocalDate.parse(e.date).format(DateTimeFormatter.ofPattern("dd MMM"))
                }.getOrElse { e.date }
            }

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(xLabels) // Applies date labels.
            chart.xAxis.labelCount = minOf(xLabels.size, 6) // Limits label count to avoid crowding.
            chart.xAxis.axisMinimum = 0f // Sets X-axis start at first entry.
            chart.xAxis.axisMaximum = (xLabels.size - 1).coerceAtLeast(0).toFloat() // Sets X-axis end at last entry.

            val minW = sorted.minOf { it.weight }.toFloat() // Finds minimum weight.
            val maxW = sorted.maxOf { it.weight }.toFloat() // Finds maximum weight.
            val pad = if (maxW == minW) 2f else (maxW - minW) * 0.1f // Adds padding if range is zero.
            chart.axisLeft.axisMinimum = (minW - pad).coerceAtLeast(0f) // Sets Y-axis minimum with padding.
            chart.axisLeft.axisMaximum = maxW + pad // Sets Y-axis maximum with padding.

            chart.invalidate() // Redraws chart with updated data.
        }
    )

}