\# Final Year Project



\# \*\*FitCore – Android Fitness \& Nutrition Tracker\*\*



\---



\## \*\*Overview\*\*



\*\*FitCore\*\* is a native Android fitness and nutrition tracking application built with \*\*Kotlin\*\* and \*\*Jetpack Compose\*\*. It combines personalised calorie tracking, food logging with barcode scanning, workout scheduling with progressive overload, and data visualisation insights into a single free application. The app follows the \*\*MVVM\*\* architecture pattern and uses \*\*Firebase Authentication\*\* and \*\*Cloud Firestore\*\* for secure, user-specific data storage.



\---



\## \*\*Features\*\*



\- \*\*User Authentication:\*\* Secure login and signup using Firebase Authentication.

\- \*\*Profile Management:\*\* Create and update personal profile with age, weight, height, gender, activity level and fitness goal.

\- \*\*Personalised Calorie Goal:\*\* Daily calorie target calculated using the Mifflin-St Jeor equation, adjusted for activity level and goal.

\- \*\*Food Tracking:\*\* Add food items manually, search by name via the OpenFoodFacts API, or scan product barcodes.

\- \*\*Barcode Scanning:\*\* Live camera barcode scanning using CameraX and ML Kit, with manual barcode entry fallback.

\- \*\*Nutrition Diary:\*\* Automatic daily diary that tracks total calories, protein, carbohydrates and fat.

\- \*\*Workout Scheduling:\*\* Assign up to 7 workouts to days of the week with a customisable weekly schedule.

\- \*\*Progressive Overload Tracking:\*\* Record exercise weights, track weight history and monitor strength progression.

\- \*\*Exercise Library:\*\* Browse predefined exercises or create custom exercises with muscle group tags.

\- \*\*Body Weight Tracking:\*\* Log daily body weight entries and view progress against a goal weight.

\- \*\*Insights \& Charts:\*\* 7-day calories vs goal bar chart, daily macronutrient bar chart, and exercise weight progress line chart using MPAndroidChart.

\- \*\*Macro Pie Chart:\*\* Visual breakdown of daily protein, carbohydrate and fat intake on the dashboard.



\---



\## \*\*Tech Stack\*\*



\- \*\*Language:\*\* Kotlin 2.2

\- \*\*UI Framework:\*\* Jetpack Compose with Material3

\- \*\*Architecture:\*\* MVVM (Model-View-ViewModel)

\- \*\*Backend:\*\* Firebase Authentication, Cloud Firestore

\- \*\*Core Libraries:\*\*

&#x20; - `Firebase Auth` – User authentication

&#x20; - `Firebase Firestore` – Cloud database for user-specific data

&#x20; - `Retrofit 3.0` – HTTP client for OpenFoodFacts API

&#x20; - `Gson` – JSON serialisation/deserialisation

&#x20; - `CameraX` – Live camera preview for barcode scanning

&#x20; - `ML Kit Barcode Scanning` – On-device barcode detection

&#x20; - `MPAndroidChart` – Bar charts and line charts for insights

&#x20; - `Navigation Compose` – Screen navigation

&#x20; - `LiveData \& StateFlow` – Reactive state management

&#x20; - `Kotlin Coroutines` – Asynchronous operations

\- \*\*Testing:\*\*

&#x20; - `JUnit 4` – Unit testing

&#x20; - `Mockito` – Mocking dependencies

&#x20; - `Coroutines Test` – Coroutine testing utilities

&#x20; - `Arch Core Testing` – InstantTaskExecutorRule for LiveData

\- \*\*Build System:\*\* Gradle with Kotlin DSL and Version Catalog

\- \*\*Min SDK:\*\* 26 (Android 8.0)

\- \*\*Target SDK:\*\* 36



\---



\## \*\*Project Structure\*\*



```plaintext

PROJECT/

│

├── product/

│   ├── app/

│   │   ├── src/

│   │   │   ├── main/java/com/example/fitnesstracker/

│   │   │   │   ├── MainActivity.kt                        # App entry point

│   │   │   │   │

│   │   │   │   ├── camera/                                # Camera \& barcode scanning

│   │   │   │   │   ├── BarcodeCameraPreview.kt            # CameraX live preview composable

│   │   │   │   │   └── MLKitBarcodeAnalyzer.kt            # ML Kit barcode frame analyser

│   │   │   │   │

│   │   │   │   ├── data/                                  # Data models

│   │   │   │   │   ├── food/

│   │   │   │   │   │   ├── FoodItem.kt                    # Food item model

│   │   │   │   │   │   └── UserNutritionDiary.kt          # Daily nutrition diary model

│   │   │   │   │   ├── user/

│   │   │   │   │   │   ├── UserProfile.kt                 # User profile, Gender, ActivityLevel, GoalType

│   │   │   │   │   │   ├── WeightEntry.kt                 # Body weight entry model

│   │   │   │   │   │   └── ExerciseWeightRecord.kt        # Exercise weight history record

│   │   │   │   │   └── workout/

│   │   │   │   │       ├── ExerciseLibrary.kt             # Exercise library entry model

│   │   │   │   │       ├── ExerciseProgressEntry.kt       # Exercise progress record model

│   │   │   │   │       ├── Workout.kt                     # Workout model

│   │   │   │   │       ├── WorkoutExercise.kt             # Exercise within a workout model

│   │   │   │   │       └── WorkoutSchedule.kt             # Weekly schedule model

│   │   │   │   │

│   │   │   │   ├── navigation/                            # App navigation

│   │   │   │   │   ├── MyAppNavigation.kt                 # NavHost and route definitions

│   │   │   │   │   └── Routes.kt                          # Route string constants

│   │   │   │   │

│   │   │   │   ├── openfoodfacts/                         # OpenFoodFacts API integration

│   │   │   │   │   ├── OpenFoodFactsApi.kt                # Retrofit API interface

│   │   │   │   │   ├── OpenFoodFactsModels.kt             # API response data classes

│   │   │   │   │   ├── OpenFoodFactsObjectMapper.kt       # API-to-FoodItem mapper

│   │   │   │   │   └── OpenFoodFactsService.kt            # Retrofit service singleton

│   │   │   │   │

│   │   │   │   ├── repository/                            # Data access layer

│   │   │   │   │   ├── repositoryinterface/               # Repository interfaces

│   │   │   │   │   │   ├── AuthRepository.kt

│   │   │   │   │   │   ├── ExerciseLibraryRepository.kt

│   │   │   │   │   │   ├── ExerciseProgressRepository.kt

│   │   │   │   │   │   ├── FoodItemRepository.kt

│   │   │   │   │   │   ├── NutritionDiaryRepository.kt

│   │   │   │   │   │   ├── UserProfileRepository.kt

│   │   │   │   │   │   ├── WeightTrackerRepository.kt

│   │   │   │   │   │   └── WorkoutRepository.kt

│   │   │   │   │   │   └── WorkoutScheduleRepository.kt

│   │   │   │   │   └── firebaserepository/                # Firebase implementations

│   │   │   │   │       ├── FirebaseAuthRepository.kt

│   │   │   │   │       ├── FirebaseExerciseLibraryRepository.kt

│   │   │   │   │       ├── FirebaseExerciseProgressRepository.kt

│   │   │   │   │       ├── FirebaseFoodItemRepository.kt

│   │   │   │   │       ├── FirebaseNutritionDiaryRepository.kt

│   │   │   │   │       ├── FirebaseUserProfileRepository.kt

│   │   │   │   │       ├── FirebaseWeightTrackerRepository.kt

│   │   │   │   │       ├── FirebaseWorkoutRepository.kt

│   │   │   │   │       └── FirebaseWorkoutScheduleRepository.kt

│   │   │   │   │

│   │   │   │   ├── ui/                                    # User interface

│   │   │   │   │   ├── components/                        # Reusable chart composables

│   │   │   │   │   │   ├── CaloriesVsGoalChart.kt         # Calories vs goal grouped bar chart

│   │   │   │   │   │   ├── DailyTotalMacrosChart.kt       # Daily macros grouped bar chart

│   │   │   │   │   │   ├── ExerciseProgressChart.kt       # Exercise weight progress line chart

│   │   │   │   │   │   └── MacroPieChart.kt               # Macronutrient pie chart

│   │   │   │   │   ├── screens/                           # App screens

│   │   │   │   │   │   ├── BarcodeScannerScreen.kt        # Barcode scanning \& manual entry

│   │   │   │   │   │   ├── FoodItemScreen.kt              # Manual food entry \& search

│   │   │   │   │   │   ├── InsightsScreen.kt              # Charts and progress insights

│   │   │   │   │   │   ├── LoginScreen.kt                 # User login

│   │   │   │   │   │   ├── MainScreen.kt                  # Dashboard with sidebar navigation

│   │   │   │   │   │   ├── SignUpScreen.kt                # User registration

│   │   │   │   │   │   ├── UserProfileScreen.kt           # Profile creation \& update

│   │   │   │   │   │   ├── WeightTrackerScreen.kt         # Body weight tracking \& chart

│   │   │   │   │   │   └── WorkoutManagementScreen.kt     # Workout scheduling \& exercises

│   │   │   │   │   └── theme/                             # Material theme definitions

│   │   │   │   │       ├── Color.kt

│   │   │   │   │       ├── Theme.kt

│   │   │   │   │       └── Type.kt

│   │   │   │   │

│   │   │   │   ├── utils/                                 # Utility classes

│   │   │   │   │   └── CaloriesCalculator.kt              # Mifflin-St Jeor calorie calculation

│   │   │   │   │

│   │   │   │   └── viewmodel/                             # ViewModels \& factories

│   │   │   │       ├── AuthViewModel.kt

│   │   │   │       ├── BarcodeScannerViewModel.kt

│   │   │   │       ├── ExerciseLibraryViewModel.kt

│   │   │   │       ├── ExerciseProgressViewModel.kt

│   │   │   │       ├── FoodItemViewModel.kt

│   │   │   │       ├── UserProfileViewModel.kt

│   │   │   │       ├── WeightTrackerViewModel.kt

│   │   │   │       ├── WorkoutScheduleViewModel.kt

│   │   │   │       ├── WorkoutViewModel.kt

│   │   │   │       └── viewmodelfactory/                  # ViewModel factory classes

│   │   │   │           ├── AuthViewModelFactory.kt

│   │   │   │           ├── BarcodeScannerViewModelFactory.kt

│   │   │   │           ├── ExerciseLibraryViewModelFactory.kt

│   │   │   │           ├── ExerciseProgressViewModelFactory.kt

│   │   │   │           ├── FoodItemViewModelFactory.kt

│   │   │   │           ├── UserProfileViewModelFactory.kt

│   │   │   │           ├── WeightTrackerViewModelFactory.kt

│   │   │   │           ├── WorkoutScheduleViewModelFactory.kt

│   │   │   │           └── WorkoutViewModelFactory.kt

│   │   │   │

│   │   │   └── test/java/com/example/fitnesstracker/      # Unit tests

│   │   │       ├── utilsTest/

│   │   │       │   └── CaloriesCalculatorTest.kt           # TDD calorie calculation tests

│   │   │       └── viewmodelsTest/

│   │   │           ├── BarcodeScannerViewModelTest.kt

│   │   │           ├── ExerciseLibraryViewModelTest.kt

│   │   │           ├── ExerciseProgressViewModelTest.kt

│   │   │           ├── FoodItemViewModelTest.kt

│   │   │           ├── UserProfileViewModelTest.kt

│   │   │           ├── WeightTrackerViewModelTest.kt

│   │   │           ├── WorkoutScheduleViewModelTest.kt

│   │   │           └── WorkoutViewModelTest.kt

│   │   │

│   │   └── build.gradle.kts                               # App-level build config

│   │

│   ├── gradle/

│   │   └── libs.versions.toml                             # Version catalog

│   ├── build.gradle.kts                                   # Project-level build config

│   ├── settings.gradle.kts                                # Gradle settings

│   └── gradlew / gradlew.bat                              # Gradle wrappers

│

├── README.md                                              # This file

└── .gitignore                                             # Git ignored files

```



\---



\## \*\*Installation Instructions\*\*



\### Prerequisites



\- \*\*Android Studio\*\* (Ladybug or later recommended)

\- \*\*JDK 21\*\* (required by the project's `jvmToolchain(21)` configuration)

\- \*\*Android SDK\*\* with API level 36 installed

\- A \*\*Firebase project\*\* with Authentication and Firestore enabled, and `google-services.json` placed in `product/app/`



\---



\### Running from Source



1\. \*\*Clone the repository:\*\*

&#x20;  ```bash

&#x20;  git clone https://gitlab.cim.rhul.ac.uk/zmac248/PROJECT.git

&#x20;  ```



2\. \*\*Open the project in Android Studio:\*\*

&#x20;  - Launch Android Studio.

&#x20;  - Select \*\*File > Open\*\* and navigate to the `product/` folder.

&#x20;  - Click \*\*OK\*\* and wait for Gradle sync to complete.



3\. \*\*Add Firebase configuration:\*\*

&#x20;  - Ensure `google-services.json` is placed in `product/app/`.

&#x20;  - This file is not included in the repository for security reasons.



4\. \*\*Run the app:\*\*

&#x20;  - Connect a physical Android device with Developer Mode enabled, or start an Android emulator (API 26+).

&#x20;  - Click the \*\*Run\*\* button or press `Shift + F10`.



\---



\### Running Unit Tests



Run all unit tests from the terminal:



```bash

cd product

./gradlew test

```



Or in Android Studio: right-click the `test/` directory and select \*\*Run Tests\*\*.



\---



\## \*\*Usage Instructions\*\*



\### Sign Up / Login

\- Create a new account with email and password, or log in with existing credentials.



\### Set Up Profile

\- After first sign-up, enter personal details (age, height, weight, gender, activity level, goal) to calculate a personalised daily calorie target.



\### Log Food

\- \*\*Manual entry:\*\* Enter food name, calories, macros and quantity.

\- \*\*Search:\*\* Search by food name using the OpenFoodFacts database (top 3 suggestions).

\- \*\*Barcode scan:\*\* Point the camera at a product barcode, or enter the barcode number manually.



\### Manage Workouts

\- Assign workouts to days of the week and save the weekly schedule.

\- Add exercises with sets, reps and starting weight.

\- Update exercise weights after workouts to track progressive overload.



\### Track Body Weight

\- Log daily body weight entries and view a progress chart against your goal weight.



\### View Insights

\- \*\*Calories vs Goal:\*\* 7-day grouped bar chart comparing intake against the calorie target.

\- \*\*Daily Macros:\*\* 7-day grouped bar chart showing protein, carbohydrate and fat totals.

\- \*\*Exercise Progress:\*\* Select an exercise to view a line chart of weight progression over time.



\---



\## \*\*Testing \& Development\*\*



\### Unit Tests

\- \*\*CaloriesCalculatorTest:\*\* TDD tests verifying Mifflin-St Jeor calculations across multiple user profiles.

\- \*\*UserProfileViewModelTest:\*\* Tests for profile save, state transitions and error handling using Mockito mocks.

\- \*\*FoodItemViewModelTest:\*\* Tests for adding, deleting and loading food items with diary synchronisation.

\- \*\*BarcodeScannerViewModelTest:\*\* Tests for barcode fetch, scanned food addition and permission handling.

\- \*\*WorkoutViewModelTest:\*\* Tests for workout creation, exercise management and weight updates.

\- \*\*WorkoutScheduleViewModelTest:\*\* Tests for saving and loading weekly schedules.

\- \*\*ExerciseLibraryViewModelTest:\*\* Tests for loading exercises and adding custom entries.

\- \*\*ExerciseProgressViewModelTest:\*\* Tests for exercise selection and progress loading.

\- \*\*WeightTrackerViewModelTest:\*\* Tests for recording, loading and deleting weight entries.



\### Manual Testing

\- Full user workflows tested on Android emulators and physical devices.

\- Firebase Authentication and Firestore data verified through Firebase console.

\- Input validation tested for empty fields, invalid data and edge cases.



\---



\## \*\*Firestore Data Structure\*\*



All user data is stored under `users/{uid}/` with the following subcollections:



| Collection | Purpose |

|---|---|

| `users/{uid}` | User profile and calorie goal |

| `foodItems` | Logged food entries |

| `nutritionDiaries/{date}` | Daily aggregated nutrition totals |

| `workouts` | Workout documents with exercise lists |

| `workoutSchedule/current` | Weekly day-to-workout mapping |

| `exerciseLibrary` | Predefined and custom exercises |

| `exerciseProgress` | Historical exercise weight records |

| `weightEntries` | Body weight history |



\---



