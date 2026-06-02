package com.example.fitnesstracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstracker.ui.components.CaloriesVsGoalChart
import com.example.fitnesstracker.ui.components.DailyTotalMacrosChart
import com.example.fitnesstracker.ui.components.ExerciseProgressChart
import com.example.fitnesstracker.viewmodel.ExerciseLibraryViewModel
import com.example.fitnesstracker.viewmodel.ExerciseProgressViewModel
import com.example.fitnesstracker.viewmodel.FoodItemState
import com.example.fitnesstracker.viewmodel.FoodItemViewModel
import com.example.fitnesstracker.viewmodel.ProfileState
import com.example.fitnesstracker.viewmodel.viewmodelfactory.FoodItemViewModelFactory
import com.example.fitnesstracker.viewmodel.UserProfileViewModel
import com.example.fitnesstracker.viewmodel.viewmodelfactory.ExerciseLibraryViewModelFactory
import com.example.fitnesstracker.viewmodel.viewmodelfactory.ExerciseProgressViewModelFactory
import com.example.fitnesstracker.viewmodel.viewmodelfactory.UserProfileViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Insights Screen with clickable sections that expand to show charts.
 * - Tap each section to open or close the chart.
 * - Shows calorie vs goal and macros charts separately.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    foodItemFactory: FoodItemViewModelFactory,
    userFactory: UserProfileViewModelFactory,
    exerciseLibraryFactory: ExerciseLibraryViewModelFactory,
    exerciseProgressFactory: ExerciseProgressViewModelFactory
) {

    val foodItemViewModel: FoodItemViewModel = viewModel(factory = foodItemFactory) // ViewModel for loading nutrition diary and food operations.
    val userViewModel: UserProfileViewModel = viewModel(factory = userFactory) // ViewModel for fetching user profile and calorie goals.
    val exerciseLibraryViewModel: ExerciseLibraryViewModel = viewModel(factory = exerciseLibraryFactory)
    val exerciseProgressViewModel: ExerciseProgressViewModel = viewModel(factory = exerciseProgressFactory)


    val profileState by userViewModel.profileState.collectAsState()  // Observes profile loading state from ViewModel.
    val foodItemState by foodItemViewModel.foodItemState.collectAsState() // Observes food item operation states from ViewModel.
    val userNutritionDiary by foodItemViewModel.userNutritionDiary.collectAsState() // Observes the 7-day nutrition diary map from ViewModel.

    val exerciseLibrary by exerciseLibraryViewModel.exercises.collectAsState()
    val selectedExerciseId by exerciseProgressViewModel.selectedExerciseLibraryId.collectAsState()
    val progressEntries by exerciseProgressViewModel.progressEntries.collectAsState()

    // Tracks which chart sections are open (true = expanded).
    var caloriesSectionOpen by remember { mutableStateOf(true) }
    var macrosSectionOpen by remember { mutableStateOf(false) }
    var exerciseProgressSectionOpen by remember { mutableStateOf(false) } // NEW


    // Calculates the date range showing on charts (oldest to newest).
    val last7Days = remember {
        (0..6).map { LocalDate.now().minusDays((6 - it).toLong()) }
    }

    // Formats as "Jan 1 - Jan 7, 2026" for display.
    val dateRangeText = remember(last7Days) {
        val oldest = last7Days.first().format(DateTimeFormatter.ofPattern("MMM d"))
        val newest = last7Days.last().format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
        "$oldest - $newest"
    }

    // Loads user profile and nutrition diary when screen opens.
    LaunchedEffect(Unit) {
        userViewModel.getUserProfile() // Fetches current user profile to get calorie goal.
        foodItemViewModel.loadUserNutritionDiary() // Fetches last 7 days of nutrition diary records.
        exerciseLibraryViewModel.loadAllExercises()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Insights") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Display error if diary specifically failed to load.
            if (foodItemState is FoodItemState.Error) {
                Text(
                    text = (foodItemState as FoodItemState.Error).message,
                    color = colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Displays chart based on profile loading state.
            when (val state = profileState) {
                is ProfileState.Success -> {
                    val goalCalories = state.userProfile.calculatedCalories ?: 0 // Gets user's calorie goal or defaults to 0.

                    // Clickable card for calorie chart section.
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { caloriesSectionOpen = !caloriesSectionOpen },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Header row with title and arrow icon.
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Calories vs Goal",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Total Calories consumed - $dateRangeText",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Icon(
                                    imageVector = if (caloriesSectionOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (caloriesSectionOpen) "Hide chart" else "Show chart"
                                )
                            }

                            // Shows chart only when section is open.
                            if (caloriesSectionOpen) {
                                Spacer(modifier = Modifier.height(12.dp))
                                CaloriesVsGoalChart(
                                    userNutritionDiary = userNutritionDiary,
                                    goalCalories = goalCalories
                                )
                            }
                        }
                    }

                    // Clickable card for macros chart section.
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { macrosSectionOpen = !macrosSectionOpen },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Header row with title and arrow icon.
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Daily Macros",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Total Protein, Carbs, Fat consumed - $dateRangeText",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Icon(
                                    imageVector = if (macrosSectionOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (macrosSectionOpen) "Hide chart" else "Show chart"
                                )
                            }

                            // Shows chart only when section is open.
                            if (macrosSectionOpen) {
                                Spacer(modifier = Modifier.height(12.dp))
                                DailyTotalMacrosChart(
                                    userNutritionDiary = userNutritionDiary
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { exerciseProgressSectionOpen = !exerciseProgressSectionOpen },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Exercise Weight Progress", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Select exercise to view progression history",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Icon(
                                    imageVector = if (exerciseProgressSectionOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (exerciseProgressSectionOpen) "Hide chart" else "Show chart"
                                )
                            }
                            if (exerciseProgressSectionOpen) {
                                Spacer(modifier = Modifier.height(12.dp))
                                if (exerciseLibrary.isEmpty()) {
                                    Text("No exercises found in library yet.", color = Color.Gray)
                                } else {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(exerciseLibrary, key = { it.id }) { exercise ->
                                            FilterChip(
                                                selected = selectedExerciseId == exercise.id,
                                                onClick = { exerciseProgressViewModel.selectExercise(exercise.id) },
                                                label = { Text(exercise.name) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFFD1C4E9)
                                                )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    ExerciseProgressChart(
                                        entries = progressEntries,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(260.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (progressEntries.isEmpty()) {
                                        Text("No progression records for selected exercise.", color = Color.Gray)
                                    } else {
                                        val latest = progressEntries.last()
                                        Text(
                                            text = "Latest: ${latest.weight} kg on ${latest.date}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                is ProfileState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator() // Shows loading spinner while profile is loading.
                    }
                }
                is ProfileState.Error -> {
                    Text(
                        text = "Error loading profile: ${state.message}",
                        color = colorScheme.error
                    )
                }
                else -> {
                    Text("Loading...") // Default loading text for idle state.
                }
            }
        }
    }
}