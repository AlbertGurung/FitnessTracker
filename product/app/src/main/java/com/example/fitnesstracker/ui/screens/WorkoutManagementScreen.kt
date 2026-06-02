package com.example.fitnesstracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstracker.data.workout.WorkoutExercise
import com.example.fitnesstracker.viewmodel.ExerciseLibraryViewModel
import com.example.fitnesstracker.viewmodel.WorkoutScheduleViewModel
import com.example.fitnesstracker.viewmodel.viewmodelfactory.WorkoutScheduleViewModelFactory
import com.example.fitnesstracker.viewmodel.WorkoutViewModel
import com.example.fitnesstracker.viewmodel.viewmodelfactory.ExerciseLibraryViewModelFactory
import com.example.fitnesstracker.viewmodel.viewmodelfactory.WorkoutViewModelFactory

/**
 * Workout Management Screen lets the user create and manage their weekly workout schedule.
 * - Assigns up to 7 workouts representing the 7 days of the week using dropdowns.
 * - Add exercises to each workout with sets, reps and weight.
 * - Edit exercise weight or remove exercises from a workout.
 * - Saves schedule and workout data to Firebase database.
 * ^ Design choice made of not having workout deletion to prevent an accidental deletion for a user would delete entire exercises weight history.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutManagementScreen(
    workoutFactory: WorkoutViewModelFactory,
    scheduleFactory: WorkoutScheduleViewModelFactory,
    exerciseLibraryFactory: ExerciseLibraryViewModelFactory
) {

    val scheduleViewModel: WorkoutScheduleViewModel = viewModel(factory = scheduleFactory)

    val workoutViewModel: WorkoutViewModel = viewModel(factory = workoutFactory)

    val exerciseLibraryViewModel: ExerciseLibraryViewModel = viewModel(factory = exerciseLibraryFactory)


    // Observe states from schedule and workouts ViewModels to update UI when data changes.
    val schedule by scheduleViewModel.schedule.collectAsState()
    val workouts by workoutViewModel.workouts.collectAsState()

    // 7 workout slots with day assignments.
    val workoutDays = remember {
        mutableStateMapOf(
            1 to "None",
            2 to "None",
            3 to "None",
            4 to "None",
            5 to "None",
            6 to "None",
            7 to "None"
        )
    }

    // Dialog visibility states.
    var showAddDialog by remember { mutableStateOf(false) } // Handles add exercise dialog checking if open or closed.
    var showUpdateWeightDialog by remember { mutableStateOf(false) } // Handles update weight dialog if open or closed.

    // Tracks which workout and exercise is selected for dialogs.
    var selectedWorkoutNumber by remember { mutableIntStateOf(1) }
    var selectedExercise by remember { mutableStateOf<WorkoutExercise?>(null) }

    // Available day options when clicked dropdown button for day selection.
    val dayOptions = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "None")

    // Loads schedule and workouts when screen opens.
    LaunchedEffect(Unit) {
        scheduleViewModel.loadSchedule()
        workoutViewModel.loadAllWorkouts()
    }

    // Loads existing schedule into workout slots.
    LaunchedEffect(schedule) {
        schedule?.let {

            // Clear all slots first
            for (i in 1..7) {
                workoutDays[i] = "None"
            }

            // Fills slots with existing assignments.
            it.scheduledDays.forEach { (day, workoutNum) ->
                if (workoutNum in 1..7) {
                    workoutDays[workoutNum] = day
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Manage Workout") }) // Title for top of the screen.
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Section header.
            Text(
                text = "Schedule Workouts to Days",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Instruction text for the user.
            Text(
                text = "Select what day each workout should be on. Leave as None if not needed for a workout.",
                fontSize = 14.sp,
            )

            // 7 workout slots with day selection for each day of the week.
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Loops through each workout slot and displays a day picker.
                    for (i in 1..7) {
                        WorkoutDaySlot(
                            workoutNumber = i,
                            selectedDay = workoutDays[i] ?: "None", // Defaults to None if no day assigned.
                            dayOptions = dayOptions,
                            onDaySelected = { day ->
                                workoutDays[i] = day // Updates the selected day for this slot.
                            }
                        )
                    }
                }
            }

            // Button to save schedule and create workouts.
            Button(
                onClick = {

                    // Create days map from assigned workouts.
                    val daysMap = workoutDays
                        .filter { it.value != "None" } // Removes unassigned workout slots.
                        .map { it.value to it.key } // Makes day to Workout number format.
                        .toMap()

                    scheduleViewModel.saveSchedule(daysMap.size, daysMap)  // Save schedule to Firebase Database.

                    // Create workouts for used slots only.
                    for (i in 1..7) {
                        if (workoutDays[i] != "None") { // Skip unassigned slots.
                            val existing = workouts.find { it.workoutNumber == i }  // Check if workout already exists.

                            if (existing == null) { // Then creates only if it doesn't exist yet.
                                workoutViewModel.createWorkout(i, "Workout $i") // Creates Workout with the workout number as part of the name.
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()

            ) {
                Text("Save Schedule")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shows workouts for assigned days.
            Text(
                text = "Your Workouts",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Shows each workout card with assigned day and exercises.
            workouts.forEach { workout ->
                val assignedDay = workoutDays[workout.workoutNumber]

                if (assignedDay != "None") {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFC))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "$assignedDay - ${workout.name}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                // Button to add exercise to this workout.
                                Button(
                                    onClick = {
                                        selectedWorkoutNumber = workout.workoutNumber
                                        showAddDialog = true
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Add Exercise")
                                }
                            }

                            // Shows exercises.
                            workout.exercises.forEach { exercise ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(exercise.name, fontWeight = FontWeight.SemiBold)
                                        Text("${exercise.sets}×${exercise.reps} @ ${exercise.currentWeight}kg", color = Color.Gray)
                                    }

                                    // Edit and delete buttons.
                                    Row {
                                        IconButton(onClick = {
                                            selectedWorkoutNumber = workout.workoutNumber
                                            selectedExercise = exercise
                                            showUpdateWeightDialog = true}) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Exercise",
                                                tint = Color.Gray
                                            )
                                        }

                                        IconButton(onClick = {
                                            exercise.id?.let {
                                                workoutViewModel.removeExerciseFromWorkout(workout.workoutNumber, it)
                                            }
                                        }) {

                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Exercise",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                }
                            }

                            if (workout.exercises.isEmpty()) { // If a workout's exercise is empty then a displayed text message saying no exercises yet.
                                Text("No exercises yet", color = Color.Gray, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog to add a new exercise to a workout.
    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add Exercise", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    // State variables for exercise input fields.
                    var exerciseName by remember { mutableStateOf("") }
                    var sets by remember { mutableStateOf("3") }
                    var reps by remember { mutableStateOf("10") }
                    var weight by remember { mutableStateOf("0") }
                    var selectedMuscleGroup by remember { mutableStateOf("Other") }
                    var muscleGroupExpanded by remember { mutableStateOf(false) }
                    val muscleGroups = listOf("Chest", "Back", "Shoulders", "Arms", "Legs", "Core", "Other")


                    // Exercise name input field.
                    OutlinedTextField(
                        value = exerciseName,
                        onValueChange = { exerciseName = it },
                        label = { Text("Exercise Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Muscle group dropdown.
                    ExposedDropdownMenuBox(
                        expanded = muscleGroupExpanded,
                        onExpandedChange = { muscleGroupExpanded = !muscleGroupExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedMuscleGroup,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Muscle Group") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = muscleGroupExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = muscleGroupExpanded,
                            onDismissRequest = { muscleGroupExpanded = false }
                        ) {
                            muscleGroups.forEach { group ->
                                DropdownMenuItem(
                                    text = { Text(group) },
                                    onClick = {
                                        selectedMuscleGroup = group
                                        muscleGroupExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                        // Sets and reps input fields side by side.
                        OutlinedTextField(
                            value = sets,
                            onValueChange = { sets = it },
                            label = { Text("Sets") },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = reps,
                            onValueChange = { reps = it },
                            label = { Text("Reps") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Weight input field.
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight (kg)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showAddDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        // Validates and adds exercise to the workout.
                        Button(
                            onClick = {
                                if (exerciseName.isNotBlank()) {  // Only adds if name is not empty.
                                    exerciseLibraryViewModel.addCustomExercise(
                                        name = exerciseName,
                                        muscleGroup = selectedMuscleGroup
                                    ) { libraryEntry ->
                                        val exercise = WorkoutExercise(
                                            exerciseLibraryId = libraryEntry.id,
                                            name = exerciseName, // Defaults to 3 if input is invalid.
                                            sets = sets.toIntOrNull()
                                                ?: 3, // Defaults to 3 if input is invalid.
                                            reps = reps.toIntOrNull()
                                                ?: 10, // Defaults to 10 if input is invalid.
                                            currentWeight = weight.toDoubleOrNull()
                                                ?: 0.0 // Defaults to 0 if input is invalid.
                                        )
                                        workoutViewModel.addExerciseToWorkout(
                                            selectedWorkoutNumber,
                                            exercise
                                        ) // Adds to Firebase.
                                    }
                                    showAddDialog = false // Close dialog after adding.
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }

    // Dialog to update an exercise's weight for when the user clicks the edit button.
    if (showUpdateWeightDialog && selectedExercise != null) { // Only shows if dialog is open & an exercise is selected.
        Dialog(onDismissRequest = { showUpdateWeightDialog = false }) {

            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Update ${selectedExercise!!.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    // Holds new weight input & defaults to current weight.
                    var newWeight by remember { mutableStateOf(selectedExercise!!.currentWeight.toString()) }

                    // Displays selected exercises current exercises weight record to help user with progression.
                    Text("Current: ${selectedExercise!!.currentWeight} kg", color = Color.DarkGray)

                    // Displays input field to add new exercise weight record.
                    OutlinedTextField(
                        value = newWeight,
                        onValueChange = { newWeight = it },
                        label = { Text("New Weight (kg)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                        // Validates and updates weight with today's date.
                        Button(
                            onClick = {
                                val weight = newWeight.toDoubleOrNull() // Convert input to Double or null if invalid.
                                if (weight != null && weight > 0) { // If weight is valid and positive...
                                    selectedExercise?.id?.let { exerciseId -> // Gets  exercise ID if it exists.
                                        val today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE) // Today's date as string.

                                        workoutViewModel.updateExerciseWeight(
                                            workoutNumber = selectedWorkoutNumber,
                                            exerciseId = exerciseId,
                                            exerciseLibraryId = selectedExercise!!.exerciseLibraryId,
                                            newWeight = weight,
                                            date = today) // Save new weight to Firebase.
                                    }
                                    showUpdateWeightDialog = false // Close dialog after update.
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Update")
                        }
                        Button(
                            onClick = { showUpdateWeightDialog = false }, // Cancel button which closes dialog without any Firebase Database update saves.
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }


                    }
                }
            }
        }
    }
}


/**
 * Composable for a single workout slot with a day dropdown.
 * - Displays the workout number label and a dropdown to select a day.
 * - Calls onDaySelected when user picks a day from the dropdown.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDaySlot( // Single workout slot row with day picker.
    workoutNumber: Int, // Which workout slot (1-7).
    selectedDay: String, // Currently assigned day for this slot.
    dayOptions: List<String>, // List of available days to choose from.
    onDaySelected: (String) -> Unit // Runs when a day is selected.
) {
    // Tracks whether the dropdown is open or closed.
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label showing the workout number.
        Text(
            text = "Workout $workoutNumber:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        // Dropdown menu for selecting the day of the week.
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1f)
        ) {
            // Text field showing selected day.
            OutlinedTextField(
                value = selectedDay,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {

                // Lists each day option as a selectable menu item.
                dayOptions.forEach { day ->
                    DropdownMenuItem(
                        text = { Text(day) },
                        onClick = {
                            onDaySelected(day) // Notifies parent of selected day.
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}