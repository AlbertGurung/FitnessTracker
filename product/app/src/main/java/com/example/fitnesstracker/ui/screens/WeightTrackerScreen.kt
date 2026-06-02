package com.example.fitnesstracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstracker.viewmodel.ProfileState
import com.example.fitnesstracker.viewmodel.UserProfileViewModel
import com.example.fitnesstracker.viewmodel.viewmodelfactory.UserProfileViewModelFactory
import com.example.fitnesstracker.viewmodel.WeightState
import com.example.fitnesstracker.viewmodel.WeightTrackerViewModel
import com.example.fitnesstracker.viewmodel.viewmodelfactory.WeightTrackerViewModelFactory
import kotlin.math.max
import kotlin.math.min


/**
 * Weight Tracker Screen lets the user view their weight progress and log new entries.
 * - Shows a simple line graph with Y-Axis margins indicating the weight range.
 * - Displays a dashed goal-weight line representing user's set weight goal.
 * - Provides an input field and button to add a new weight.
 * - Displays a list of historical weight entries.
 */
@Composable
fun WeightTrackerScreen(
    weightFactory: WeightTrackerViewModelFactory,
    userFactory: UserProfileViewModelFactory
) {

    val context = LocalContext.current

    val viewModel: WeightTrackerViewModel = viewModel(factory = weightFactory)

    // ViewModel that manages the user's profile data.
    val userProfileViewModel: UserProfileViewModel = viewModel(factory = userFactory)

    // Observe profile state from ViewModel.
    val profileState by userProfileViewModel.profileState.collectAsState()


    // Observe states.
    val weightState by viewModel.weightState.collectAsState()
    val weightEntries by viewModel.weightEntries.collectAsState()

    // Holds weight input.
    var newWeight by rememberSaveable { mutableStateOf("") }

    // Load entries and gets user's profile when screen opens.
    LaunchedEffect(Unit) {
        viewModel.loadWeightEntries()
        userProfileViewModel.getUserProfile()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Title of the screen
        Text("Weight Tracker", fontSize = 24.sp, modifier = Modifier.padding(top = 28.dp))
        Spacer(modifier = Modifier.height(24.dp))


        // Line Graph Visualisation with Y-Axis.

        if (profileState is ProfileState.Success) {  // When user profile is loaded successfully
            val user = (profileState as ProfileState.Success).userProfile

            // finds max and min weight from list.
            val maxWeightRaw = weightEntries.maxOfOrNull { it.weight } ?: 100.0
            val minWeightRaw = weightEntries.minOfOrNull { it.weight } ?: 0.0

            // adds small margin so graph looks nicer.
            val maxWeight = max(maxWeightRaw, user.goalWeight) + 2.0
            val minWeight = min(minWeightRaw, user.goalWeight) - 2.0

            val weightRange = maxWeight - minWeight

            // Calculates the mathematical true middle of the graph scale.
            val midWeight = (maxWeight + minWeight) / 2

            //  Shows labels above graph and displaying what the green dashed line represents (Goal Weight).
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Progress Chart", fontSize = 14.sp, color = Color.Gray)
                Text(text = "Goal: ${user.goalWeight}kg", fontSize = 14.sp, color = Color(0xFF4CAF50))
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
            ) {

                // Main Graph Area which is separated into Left (Text) and Right (Canvas)
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // The Y-Axis Margins (Max, Mid, and Min weight labels)
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(text = "%.1f".format(maxWeight), fontSize = 12.sp, color = Color.Gray)
                        Text(text = "%.1f".format(midWeight), fontSize = 12.sp, color = Color.Gray)
                        Text(text = "%.1f".format(minWeight), fontSize = 12.sp, color = Color.Gray)
                    }

                    // Draws the actual graph
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val graphWidth = size.width
                        val graphHeight = size.height

                        // Draw Top Boundary Line (Max weight ceiling)
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(0f, 0f),
                            end = Offset(graphWidth, 0f),
                            strokeWidth = 2f
                        )
                        // Draw Bottom Boundary Line (Min weight floor)
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(0f, graphHeight),
                            end = Offset(graphWidth, graphHeight),
                            strokeWidth = 2f
                        )

                        // calculates where goal line should be on graph.
                        val goalNormalizedY =
                            if (weightRange == 0.0) 0.5f else ((maxWeight - user.goalWeight) / weightRange).toFloat()
                        val goalY = goalNormalizedY * graphHeight

                        // Draw Goal Line (Dashed Green)
                        drawLine(
                            color = Color(0xFF4CAF50),
                            start = Offset(0f, goalY),
                            end = Offset(graphWidth, goalY),
                            strokeWidth = 3f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        // Draw tracking lines and dots with spacing between each point on X-axis.
                        val stepX =
                            graphWidth / (if (weightEntries.size > 1) weightEntries.size - 1 else 1)

                        var previousPoint: Offset? = null

                        // loops through entries and draws graph line in reversed given inputs such that it shows a progression going to right of graph.
                        weightEntries.reversed().forEachIndexed { index, entry ->
                            val normalizedY =
                                if (weightRange == 0.0) 0.5f else ((maxWeight - entry.weight) / weightRange).toFloat()
                            val currentPoint = Offset(
                                x = index * stepX,
                                y = normalizedY * graphHeight
                            )
                            // Draw connecting lines.
                            if (previousPoint != null) {
                                drawLine(
                                    color = Color.Magenta,
                                    start = previousPoint,
                                    end = currentPoint,
                                    strokeWidth = 4f
                                )
                            }
                            // Draw white filled circle.
                            drawCircle(
                                color = Color.White,
                                radius = 4f,
                                center = currentPoint
                            )

                            // Draw black outline.
                            drawCircle(
                                color = Color.Black,
                                radius = 4f,
                                center = currentPoint,
                                style = Stroke(width = 1.5f)  // Thin black border
                            )

                            previousPoint = currentPoint
                        }
                    }
                }
            }

        } else {
            // message if no data yet.
            Text("Start Tracking Weight! Achieve Your Goal", modifier = Modifier.padding(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input section to add new weight.
        OutlinedTextField(
            value = newWeight,
            onValueChange = { newWeight = it },
            label = { Text("Enter Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Button to add weight.
        Button(
            onClick = {
                val weightDouble = newWeight.toDoubleOrNull()
                if (weightDouble != null && weightDouble > 0) {
                    viewModel.addWeight(weightDouble)
                    newWeight = "" // Resets field after adding
                } else {
                    Toast.makeText(context, "Please enter a valid weight", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = weightState !is WeightState.Loading

        ) {
            // Loading state shows loading spinner when saving.
            if (weightState is WeightState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Add Weight")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History List
        Text("Weight History", fontSize = 20.sp, modifier = Modifier.align(Alignment.Start))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Shows each weight entry in a card.
            items(weightEntries) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Shows weight and date.
                        Column {
                            Text(text = "${entry.weight} kg", fontSize = 18.sp)
                            Text(text = entry.dateAdded, color = Color.Gray, fontSize = 14.sp)
                        }

                        // Delete button for removing entry.
                        IconButton(
                            onClick = { viewModel.deleteWeight(entry) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete recorded weight",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}