package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * MacroPieChart composable function that renders a pie chart representing macronutrients ratio.
 * - Displays protein, carbs, and fat as proportional slices of total macros.
 * - Includes a legend (labeled section) with colored indicators for each macro.
 * - Shows a fallback message when no data is available.
 */
@Composable
fun MacroPieChart(protein: Int, carbs: Int, fat: Int) {

    // Calculate total macros to find how the pie is divided.
    val totalMacros = protein + carbs + fat

    // Show fallback UI message when no macros exist (no food logged yet)
    if (totalMacros == 0) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Must be Hungry, Have a bite!!")
        }
    }

    // Convert macro values into angles for the pie chart where each value becomes a portion of 360 degrees (full circle)
    val proteinAngle = (protein.toFloat() / totalMacros) * 360f
    val carbsAngle = (carbs.toFloat() / totalMacros) * 360f
    val fatAngle = (fat.toFloat() / totalMacros) * 360f


    // Calculations of macros percentages compared to the total macros.
    val carbsPercent = (carbs.toFloat() / totalMacros) * 100
    val proteinPercent = (protein.toFloat() / totalMacros) * 100
    val fatPercent = (fat.toFloat() / totalMacros) * 100


    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Canvas used to draw pie chart slices.
        Canvas(modifier = Modifier.size(200.dp)) {

            // Draw carbs slice first starting from 0°
            drawArc(
                color = Color(0xFFFBBC04),
                startAngle = 0f, // Starting point of the slice.
                sweepAngle = carbsAngle, // Size of slice.
                useCenter = true // Connects arc to center.
            )
            // Carbs outline
            drawArc(
                color = Color.Black,
                startAngle = 0f,
                sweepAngle = carbsAngle,
                useCenter = true,
                style = Stroke(width = 1.dp.toPx())
            )

            // Draw protein slice right after carbs slice ends.
            drawArc(
                color = Color(0xFF34A853),
                startAngle = carbsAngle, // Starts where carbs finishes.
                sweepAngle = proteinAngle,
                useCenter = true
            )

            // Protein outline
            drawArc(
                color = Color.Black,
                startAngle = carbsAngle,
                sweepAngle = proteinAngle,
                useCenter = true,
                style = Stroke(width = 1.dp.toPx())
            )

            // Draw fat slice last.
            drawArc(
                color = Color(0xFFEA4335),
                startAngle = carbsAngle + proteinAngle, // Starts after carbs + protein.
                sweepAngle = fatAngle,
                useCenter = true
            )

            // Fat outline
            drawArc(
                color = Color.Black,
                startAngle = carbsAngle + proteinAngle,
                sweepAngle = fatAngle,
                useCenter = true,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Carbs legend item (colored dot + label)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(color = Color(0xFFFBBC04)) // Yellow indicator
                }
                Spacer(modifier = Modifier.size(6.dp))
                Text("Carbs: $carbs g (${carbsPercent.toInt()}%)") // $carbs shows current total carbs value.
            }

            // Protein legend item
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(color = Color(0xFF34A853)) // Green indicator

                }
                Spacer(modifier = Modifier.size(6.dp))
                Text("Protein: $protein g (${proteinPercent.toInt()}%)") // $protein shows current total protein value.
            }

            // Fat legend item
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(color = Color(0xFFEA4335)) // Orange indicator
                }
                Spacer(modifier = Modifier.size(6.dp))
                Text("Fat: $fat g (${fatPercent.toInt()}%)") // $fat displays current total fat amount.
            }
        }
    }
}