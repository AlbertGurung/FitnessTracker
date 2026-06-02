package com.example.fitnesstracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fitnesstracker.navigation.Routes
import com.example.fitnesstracker.ui.components.MacroPieChart
import com.example.fitnesstracker.viewmodel.AuthState
import com.example.fitnesstracker.viewmodel.AuthViewModel
import com.example.fitnesstracker.viewmodel.FoodItemState
import com.example.fitnesstracker.viewmodel.FoodItemViewModel
import com.example.fitnesstracker.viewmodel.viewmodelfactory.FoodItemViewModelFactory
import com.example.fitnesstracker.viewmodel.ProfileState
import com.example.fitnesstracker.viewmodel.UserProfileViewModel
import com.example.fitnesstracker.viewmodel.viewmodelfactory.UserProfileViewModelFactory
import com.example.fitnesstracker.viewmodel.WorkoutScheduleViewModel
import com.example.fitnesstracker.viewmodel.viewmodelfactory.WorkoutScheduleViewModelFactory
import com.example.fitnesstracker.viewmodel.WorkoutViewModel
import com.example.fitnesstracker.viewmodel.viewmodelfactory.WorkoutViewModelFactory
import kotlinx.coroutines.launch

/**
 * Main Screen is displayed after the user logs in.
 * - Gets the user profile from Firestore database and shows user's name and daily calorie goal.
 * - Observes authentication state and navigates back to login screen if user signs out.
 * - Displays today's date food items added by the user.
 * - Allows the user to update profile information which updates data in the Firestore database.
 * - Provides side taskbar navigation for the app features.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    foodItemFactory: FoodItemViewModelFactory,
    userFactory: UserProfileViewModelFactory,
    workoutFactory: WorkoutViewModelFactory,
    scheduleFactory: WorkoutScheduleViewModelFactory
) {

    val authState = authViewModel.authState.observeAsState()

    // ViewModel that manages the user's profile data.
    val userProfileViewModel: UserProfileViewModel = viewModel(factory = userFactory)

    // Observe profile state from ViewModel.
    val profileState by userProfileViewModel.profileState.collectAsState()

    // Food item repositories and ViewModel for food items.
    val foodItemViewModel: FoodItemViewModel = viewModel(factory = foodItemFactory)
    val todayItems by foodItemViewModel.todayItems.collectAsState()
    val totalDailyCaloriesCount by foodItemViewModel.totalDailyCaloriesCount.collectAsState()


    // Holds current total protein (in grams) for today from ViewModel and updates UI when it changes
    val totalDailyProtein by foodItemViewModel.totalDailyProtein.collectAsState()

    // Holds current total carbs (in grams) for today.
    val totalDailyCarbs by foodItemViewModel.totalDailyCarbs.collectAsState()

    // Holds current total fat (in grams) for today.
    val totalDailyFat by foodItemViewModel.totalDailyFat.collectAsState()

    // Observe food item state from ViewModel.
    val foodItemState by foodItemViewModel.foodItemState.collectAsState()

    // Workout ViewModel using passed factory.
    val workoutViewModel: WorkoutViewModel = viewModel(factory = workoutFactory)
    val workouts by workoutViewModel.workouts.collectAsState() // Observes workout list.

    // Schedule ViewModel using passed factory.
    val scheduleViewModel: WorkoutScheduleViewModel = viewModel(factory = scheduleFactory)


    // State to manage the open and closed status of the navigation drawer.
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Used to launch coroutines for opening/closing the drawer.
    val coroutineScope = rememberCoroutineScope()

    // Remembers if the food list is minimised or expanded (starts closed)
    var isFoodListExpanded by remember { mutableStateOf(false) }

    // State to expand/collapse workout section
    var isWorkoutExpanded by remember { mutableStateOf(false) }


    // Navigate to login if unauthenticated.
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate(Routes.LOGIN_SCREEN)
            else -> Unit
        }
    }
    LaunchedEffect(Unit) { // On launch
        userProfileViewModel.getUserProfile() // Loads user profile data when screen is first opened.
        foodItemViewModel.loadTodayFoodItems() // Load today's food items when Main screen is shown.
        scheduleViewModel.loadSchedule() // Load weekly workout schedule.
        workoutViewModel.loadAllWorkouts() // Load all saved workouts.
    }

    // Wrapper for sliding drawer navigation UI.
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Container for the drawer's content items.
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))

                // Gets user-name to display at the top of the side menu.
                val name = if (profileState is ProfileState.Success) {
                    (profileState as ProfileState.Success).userProfile.name
                } else "Fitness Tracker"

                Text(
                    text = name,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Button to add food item manually.
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Food Item") },
                    label = { Text("Add Food Item") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(Routes.FOOD_ITEM_SCREEN)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                // Button to open barcode scanner screen.
                NavigationDrawerItem(
                        icon = { Icon(Icons.Default.DocumentScanner, contentDescription = "Scan Barcode") },
                    label = { Text("Scan Barcode") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(Routes.BARCODE_SCANNER_SCREEN)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                // Button to navigate to Weight Tracker screen.
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Default.MonitorWeight,
                            contentDescription = "Track Weight"
                        )
                    }, label = { Text("Track Weight") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(Routes.WEIGHT_TRACKER_SCREEN)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                // Button to navigate to Workout Management & Scheduler Screen.
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = "Workout Scheduler & Manager"
                        )
                    }, label = { Text("Manage Workout") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(Routes.WORKOUT_MANAGEMENT_SCREEN)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                // Button to navigate to Insights screen.
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = "View Insights"
                        )
                    },
                    label = { Text("View Insights") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(Routes.INSIGHTS_SCREEN)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                // Button to navigate to update profile screen.
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Update Profile") },
                    label = { Text("Update Profile") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(Routes.USER_PROFILE_SCREEN)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Button to sign the user out.
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out"
                        )
                    },
                    label = { Text("Sign out") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        authViewModel.signOut()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        // Main content layout with a top app bar and body content.
        Scaffold(
            topBar = {
                // Top header providing access to open the side menu.
                CenterAlignedTopAppBar(
                    title = {
                        when (val state = profileState) {
                            is ProfileState.Success -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Hello, ${state.userProfile.name}",
                                        fontSize = 24.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (state.userProfile.calculatedCalories != null) {
                                        Text(
                                            text = "Daily Goal: ${state.userProfile.calculatedCalories} kcal",
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                            }

                            is ProfileState.Loading -> {
                                CircularProgressIndicator()
                            }

                            is ProfileState.Error -> {
                                Text(text = "Error loading profile", fontSize = 14.sp)
                            }

                            else -> {
                                Text(text = "Loading...", fontSize = 14.sp)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }

        ) { innerPadding ->

            // Column container to be a fixed outer-layer which will stops everything to be scrolled at once.
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                // Displays error message for food item operations failure.
                if (foodItemState is FoodItemState.Error) {
                    Text(
                        text = (foodItemState as FoodItemState.Error).message,
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                }

                // Handles profile state to display calorie summary.
                when (val state = profileState) {

                    // If profile state is success then ...
                    is ProfileState.Success -> {

                        val goalCalories = state.userProfile.calculatedCalories // Assigns user's daily calorie target.
                        val remainingCalories = goalCalories?.minus(totalDailyCaloriesCount) // Goal Calories - Current Total Calories

                        // Displays calorie calculation only when remaining calories can be computed.
                        if (remainingCalories != null) {
                            Text(
                                text = "$goalCalories - $totalDailyCaloriesCount = $remainingCalories kcal",
                                fontSize = 20.sp,
                                color = if (remainingCalories >= 0) Color.Black else Color.Red // Changes text colour to red when at a negative calories remaining.
                            )

                        }

                        // Formula text showing the formula used for remaining calories.
                        Text(
                            text = "Daily Goal - Current Calories = Remaining",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (totalDailyCaloriesCount != 0) {
                            // Displays the macro pie chart using the latest values from the foodItemViewModel and these values update automatically when food items change.
                            MacroPieChart(
                                protein = totalDailyProtein,
                                carbs = totalDailyCarbs,
                                fat = totalDailyFat
                            )
                        }

                    }


                    // Loading state
                    is ProfileState.Loading -> {
                        CircularProgressIndicator() // Indicates that profile data is currently loading.
                    }

                    // Error state
                    is ProfileState.Error -> {
                        Text(
                            text = "Error loading calories",
                            color = Color.Red
                        )
                    }

                    else -> {
                        Text("Loading...") // Else defaults back to general state while data still being loaded or processed
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Clickable row to show/hide food list.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()

                        // Toggles to expanded state when clicked.
                        .clickable { isFoodListExpanded = !isFoodListExpanded }
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Food Entries (${todayItems.size})",
                        fontSize = 18.sp
                    )
                    // Arrow indicator (Maximise + OR Minimise -).
                    Text(
                        text = if (isFoodListExpanded) "-"
                        else "+"
                    )
                }


                // Only show food list when expanded.
                if (isFoodListExpanded) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)

                    ) {
                        items(todayItems) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF42A5F5))
                            ) { // Card slot for each food item added.

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Row 1: Name + Servings
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        Text(
                                            text = item.name,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis, // Show "..." if text is too long.
                                            modifier = Modifier.weight(1f) // Takes available space without pushing out the other parts.
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "x ${item.quantity} servings",
                                            color = Color.White
                                        )
                                    }

                                    // Row 2: Macros displaying food item macros info.
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "${item.calories} kcal ", color = Color.White)
                                        Text(text = "${item.protein}P ", color = Color.White)
                                        Text(text = "${item.carbohydrate}C ", color = Color.White)
                                        Text(text = "${item.fat}F ", color = Color.White)

                                    }
                                }

                                // Delete Icon Button
                                IconButton(
                                    onClick = {
                                        foodItemViewModel.deleteFoodItem(item)
                                        foodItemViewModel.loadTodayFoodItems()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White

                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Workout Section for finding today's scheduled workout from the schedule.
                val todayWorkoutNum = scheduleViewModel.getTodayWorkoutNumber() // Get workout number for today.
                val todayWorkout = todayWorkoutNum?.let { num -> workouts.find { it.workoutNumber == num } } // Match to workout data.

                // Clickable header row where users can expand/minimise workout exercise list visibility.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isWorkoutExpanded = !isWorkoutExpanded }
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text( // Show workout name when workout scheduled or else a default message.
                        text =
                            if (todayWorkout != null)
                                "Today's Workout: ${todayWorkout.name}"
                            else
                                "No Workout Scheduled Today",
                        fontSize = 18.sp
                    )
                    Text(text = if (isWorkoutExpanded) "-" else "+") // Expand/collapse indicator.
                }

                // Shows exercise list when expanded and a workout exists for today.
                if (isWorkoutExpanded && todayWorkout != null) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        // Display each exercise as a green card with name, sets, reps and weight.
                        items(todayWorkout.exercises) { exercise ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF6AD76F))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Text( // Exercise name.
                                        text = exercise.name,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text( // Sets and reps info.
                                        text = "${exercise.sets} Sets x ${exercise.reps} Reps",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Text( // Current weight displayed for exercise for user to notice.
                                        text = "Current Weight: ${exercise.currentWeight} kg",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        // Empty state when no exercises have been added.
                        if (todayWorkout.exercises.isEmpty()) {
                            item {
                                Text(
                                    text = "No exercises added to this workout",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
