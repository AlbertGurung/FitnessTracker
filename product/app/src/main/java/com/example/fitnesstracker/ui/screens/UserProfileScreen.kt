package com.example.fitnesstracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fitnesstracker.data.user.ActivityLevel
import com.example.fitnesstracker.data.user.Gender
import com.example.fitnesstracker.data.user.GoalType
import com.example.fitnesstracker.data.user.UserProfile
import com.example.fitnesstracker.navigation.Routes
import com.example.fitnesstracker.viewmodel.ProfileState
import com.example.fitnesstracker.viewmodel.UserProfileViewModel
import com.example.fitnesstracker.viewmodel.viewmodelfactory.UserProfileViewModelFactory


/**
 * User Profile Screen lets the user view and edit their personal info.
 * - Shows fields for name, age, weight, height, gender, activity level and daily calories' goal.
 * - Pre-fills fields with existing data from Firestore.
 * - Calculates user daily calories goals based on user details and saves profile when calculate calories button pressed.
 * - Navigates to MainScreen after saving and prevents going back to profile.
 * - Shows Toast messages if fields are missing or invalid.
 */
@Composable
fun UserProfileScreen(
    navController: NavController,
    factory: UserProfileViewModelFactory
) {

    // Get the current Android context needed for Toast messages.
    val context = LocalContext.current

    // ViewModel that manages user profile data.
    val viewModel: UserProfileViewModel = viewModel(factory = factory)

    // Observe profile state from ViewModel (Success, Loading, Error).
    val profileState by viewModel.profileState.collectAsState()

    // Stores user input and selections using remember so values stay during UI updates and recomposition.
    var name by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var selectedGender by rememberSaveable { mutableStateOf(Gender.MALE) }
    var selectedActivityLevel by rememberSaveable { mutableStateOf(ActivityLevel.SEDENTARY) }
    var selectedGoal by rememberSaveable { mutableStateOf(GoalType.MAINTAIN_WEIGHT) }
    var goalWeight by rememberSaveable { mutableStateOf("") }

    // Gets existing user data when screen opens.
    LaunchedEffect(Unit) {
        viewModel.getUserProfile()
    }

    // Pre-fill fields when data arrives
    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Success) {
            val user = (profileState as ProfileState.Success).userProfile

            // Only fills if the form is currently empty.
            if (name.isEmpty()) {
                name = user.name
                age = if (user.age > 0)
                    user.age.toString() else ""
                weight = if (user.weight > 0)
                    user.weight.toString() else ""
                height = if (user.height > 0)
                    user.height.toString() else ""
                selectedGender = user.gender
                selectedActivityLevel = user.activityLevel
                selectedGoal = user.goal
                goalWeight = if (user.goalWeight > 0)
                    user.goalWeight.toString() else ""
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "User Profile") // Displays to indicate user profile screen.

        // Text fields for user input.
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Gender Selection
        Text("Gender", modifier = Modifier.align(Alignment.Start))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Gender.entries.forEach { gender ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedGender == gender,
                        onClick = { selectedGender = gender }
                    )
                    Text(text = gender.name)
                }
            }
        }

        // Activity Level Selection
        Text("Activity Level", modifier = Modifier.align(Alignment.Start))
        Column {
            ActivityLevel.entries.forEach { level ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedActivityLevel == level,
                        onClick = { selectedActivityLevel = level }
                    )
                    Text(text = level.name.replace("_", " "))
                }
            }
        }

        // Goal Selection
        Text("Goal", modifier = Modifier.align(Alignment.Start))
        Column {
            GoalType.entries.forEach { goal ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedGoal == goal,
                        onClick = { selectedGoal = goal }
                    )
                    Text(text = goal.name.replace("_", " "))
                }
            }
        }

        // Goal Weight Setting
        OutlinedTextField(
            value = goalWeight,
            onValueChange = { goalWeight = it },
            label = { Text("Goal Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )


        // Button to calculate and save profile.
        Button(
            onClick = {
                val ageInt = age.toIntOrNull()
                val weightDouble = weight.toDoubleOrNull()
                val heightDouble = height.toDoubleOrNull()
                val goalWeightDouble = goalWeight.toDoubleOrNull()


                // Checks input fields for user profile filled and then calculates and saves profile.
                if (name.isNotBlank() && ageInt != null && weightDouble != null && heightDouble != null && goalWeightDouble != null) {
                    val userProfile = UserProfile(
                        name = name,
                        age = ageInt,
                        weight = weightDouble,
                        height = heightDouble,
                        gender = selectedGender,
                        activityLevel = selectedActivityLevel, // Default placeholder of sedentary set so it will always be valid.
                        goal = selectedGoal, // Default placeholder goal of maintain set so it will always be valid.
                        goalWeight = goalWeightDouble
                    )
                    viewModel.calculateAndSaveProfile(userProfile)

                    // Go to MainScreen and remove UserProfileScreen from back stack so back button won’t go back to profile screen.
                    navController.navigate(Routes.MAIN_SCREEN) {
                        popUpTo(Routes.USER_PROFILE_SCREEN) { inclusive = true }
                    }
                } else {
                    Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show() // If empty fields or wrong format.
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = profileState !is ProfileState.Loading // Disable button while profile is loading to prevent multiple click.
        ) {
            if (profileState is ProfileState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp)) // loading indicator.
            } else {
                Text("Save Profile")
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}