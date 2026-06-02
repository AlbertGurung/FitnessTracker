package com.example.fitnesstracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fitnesstracker.repository.firebaserepository.FirebaseExerciseLibraryRepository
import com.example.fitnesstracker.repository.firebaserepository.FirebaseExerciseProgressRepository
import com.example.fitnesstracker.repository.firebaserepository.FirebaseFoodItemRepository
import com.example.fitnesstracker.repository.firebaserepository.FirebaseNutritionDiaryRepository
import com.example.fitnesstracker.repository.firebaserepository.FirebaseUserProfileRepository
import com.example.fitnesstracker.repository.firebaserepository.FirebaseWeightRepository
import com.example.fitnesstracker.repository.firebaserepository.FirebaseWorkoutRepository
import com.example.fitnesstracker.repository.firebaserepository.FirebaseWorkoutScheduleRepository
import com.example.fitnesstracker.ui.screens.*
import com.example.fitnesstracker.utils.CaloriesCalculator
import com.example.fitnesstracker.viewmodel.AuthViewModel
import com.example.fitnesstracker.viewmodel.viewmodelfactory.BarcodeScannerViewModelFactory
import com.example.fitnesstracker.viewmodel.viewmodelfactory.ExerciseLibraryViewModelFactory
import com.example.fitnesstracker.viewmodel.viewmodelfactory.ExerciseProgressViewModelFactory
import com.example.fitnesstracker.viewmodel.viewmodelfactory.FoodItemViewModelFactory
import com.example.fitnesstracker.viewmodel.viewmodelfactory.UserProfileViewModelFactory
import com.example.fitnesstracker.viewmodel.viewmodelfactory.WeightTrackerViewModelFactory
import com.example.fitnesstracker.viewmodel.viewmodelfactory.WorkoutScheduleViewModelFactory
import com.example.fitnesstracker.viewmodel.viewmodelfactory.WorkoutViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * MyAppNavigation sets up all navigation routes in the app.
 * - Central location for managing all the screen navigation in one place.
 * - Decides which screen loaded first for app.
 * - Creates all Firebase instances, repositories and ViewModel factories in one place.
 * - Passes dependencies to screens to keep them free of Firebase and repository creation code.
 * - Navigates the screens based on the authentication state.
 */
@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {

    // Create a NavController to handle navigation between screens
    val navController = rememberNavController()


    // Firebase instances for authentication and database access.
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    val calc = remember { CaloriesCalculator() }

    // Repositories handling data operations with Firebase.
    val diaryRepo = remember { FirebaseNutritionDiaryRepository(auth, firestore) } // Reads and writes daily food entries to calculate calorie and macro totals.
    val userRepo = remember { FirebaseUserProfileRepository(auth, firestore) } // // Fetches and updates user profile calculating daily calorie goals from data.
    val foodItemRepo = remember { FirebaseFoodItemRepository(auth, firestore) }   // Fetches item information by barcode from OpenFoodFacts API and manages user's logged food items (add, delete, retrieve daily and date range queries).
    val exerciseProgressRepo = remember { FirebaseExerciseProgressRepository(auth, firestore) }
    val workoutRepo = remember { FirebaseWorkoutRepository(auth, firestore, exerciseProgressRepo) }  // Creates and save workouts with exercises, updates weights and tracks weight history per exercise.
    val scheduleRepo = remember { FirebaseWorkoutScheduleRepository(auth, firestore) } // Stores which workout number is assigned to each day of the week.
    val weightRepo = remember { FirebaseWeightRepository(auth, firestore) } // Records timestamped weight entries and fetches the weight entry's history from the database for progress chart.
    val exerciseLibraryRepo = remember { FirebaseExerciseLibraryRepository(auth, firestore) }


    // Factories creating ViewModels with their required dependencies injected.
    val weightFactory = remember { WeightTrackerViewModelFactory(weightRepo) } // Injects weightRepo for weight logging and chart display.
    val workoutFactory = remember { WorkoutViewModelFactory(workoutRepo) } // Injects workoutRepo for managing exercises and workouts.
    val scheduleFactory = remember { WorkoutScheduleViewModelFactory(scheduleRepo) } // Injects scheduleRepo for assigning workouts to days.
    val foodItemFactory = remember { FoodItemViewModelFactory(foodItemRepo, userRepo, diaryRepo) } // Injects repos for food search and diary logging.
    val userFactory = remember { UserProfileViewModelFactory(userRepo, calc, diaryRepo) } // Injects repos and calc for profile data and macro calculations.
    val barcodeFactory = remember { BarcodeScannerViewModelFactory(foodItemRepo, userRepo, diaryRepo) } // Injects repos for barcode lookup and food logging.
    val exerciseLibraryFactory = remember { ExerciseLibraryViewModelFactory(exerciseLibraryRepo) }
    val exerciseProgressFactory = remember { ExerciseProgressViewModelFactory(exerciseProgressRepo) }


    // Set up navigation host with start destination and route mapping using builder pattern
    NavHost(navController = navController, startDestination = Routes.LOGIN_SCREEN, builder = {

        // Define composable destinations: each links route to a screen composable.

        composable(Routes.LOGIN_SCREEN){ // Login route (start destination).
            LoginScreen(modifier,navController,authViewModel)
        }
        composable(Routes.SIGNUP_SCREEN){ // Sign up route for new users.
            SignUpScreen(modifier,navController,authViewModel)
        }
        composable(Routes.MAIN_SCREEN){ // Main dashboard after login.
            MainScreen(modifier,navController, authViewModel,
                foodItemFactory = foodItemFactory,
                userFactory = userFactory,
                workoutFactory = workoutFactory,
                scheduleFactory = scheduleFactory
            )
        }
        composable(Routes.USER_PROFILE_SCREEN){ // User profile settings.
            UserProfileScreen(navController, userFactory)
        }
        composable(Routes.FOOD_ITEM_SCREEN) { // Food logging screen.
            FoodItemScreen(navController, foodItemFactory)
        }
        composable(Routes.BARCODE_SCANNER_SCREEN) { // Barcode scanner for quick food entry.
                BarcodeScannerScreen(navController, barcodeFactory)
        }
        composable(Routes.WEIGHT_TRACKER_SCREEN) { // Weight tracking screen.
                WeightTrackerScreen(weightFactory, userFactory)

        }
        composable(Routes.WORKOUT_MANAGEMENT_SCREEN) { // Workout scheduling and managing screen.
                WorkoutManagementScreen(workoutFactory, scheduleFactory, exerciseLibraryFactory)
        }
        composable(Routes.INSIGHTS_SCREEN) { // Data insights and visualisation screen.
                InsightsScreen(foodItemFactory, userFactory, exerciseLibraryFactory, exerciseProgressFactory)
        }

    })
}