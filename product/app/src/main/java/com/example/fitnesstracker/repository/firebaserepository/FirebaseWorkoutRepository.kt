package com.example.fitnesstracker.repository.firebaserepository

import com.example.fitnesstracker.data.user.ExerciseWeightRecord
import com.example.fitnesstracker.data.workout.ExerciseProgressEntry
import com.example.fitnesstracker.data.workout.Workout
import com.example.fitnesstracker.data.workout.WorkoutExercise
import com.example.fitnesstracker.repository.repositoryinterface.ExerciseProgressRepository
import com.example.fitnesstracker.repository.repositoryinterface.WorkoutRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * FirebaseWorkoutRepository saves and gets workout data from Firestore.
 * - Saves workouts under users/{userId}/workouts.
 * - Can add, remove, or update exercises in a workout.
 */
class FirebaseWorkoutRepository(
    private val auth: FirebaseAuth, // Firebase login info.
    private val firestore: FirebaseFirestore, // Firestore database.
    private val progressRepository: ExerciseProgressRepository // Exercise Progress Repository
) : WorkoutRepository {

    private fun getUserId(): String? = auth.currentUser?.uid

    /**
     * Finds the path to the user's workouts in the database.
     */
    private fun currentWorkoutsPath() =
        getUserId()?.let { uid ->
            firestore.collection("users").document(uid).collection("workouts")
        } ?: throw IllegalStateException("User not logged in")

    /**
     * Saves a workout to the database.
     */
    override suspend fun saveWorkout(workout: Workout): Result<String> {
        return try {
            val path = currentWorkoutsPath() // Get the Firestore path.

            // If the workout has an ID, updates it, otherwise create a new one.
            val docRef = if (workout.id != null) {
                path.document(workout.id)
            } else {
                path.document()
            }

            docRef.set(workout).await() // Save data to Firestore.
            Result.success(docRef.id) // Return the document ID.
        } catch (e: Exception) {
            Result.failure(e) // Return error if something fails.
        }
    }

    /**
     * Gets a specific workout by its number.
     */
    override suspend fun getWorkout(workoutNumber: Int): Result<Workout?> {
        return try {
            val snapshot = currentWorkoutsPath()
                .whereEqualTo("workoutNumber", workoutNumber) // Query by workout number.
                .get()
                .await()

            // Map the first matching document to a Workout object.
            val workout = snapshot.documents.firstOrNull()?.let { doc ->
                doc.toObject(Workout::class.java)?.copy(id = doc.id)
            }

            Result.success(workout)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets every workout the user has created.
     */
    override suspend fun getAllWorkouts(): Result<List<Workout>> {
        return try {
            val snapshot = currentWorkoutsPath()
                .get() // Fetch all docs in the subcollection.
                .await()

            // Convert documents to objects and sort them by number.
            val workouts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Workout::class.java)?.copy(id = doc.id)
            }.sortedBy { it.workoutNumber }

            Result.success(workouts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Changes the weight of an exercise and records the date.
     */
    override suspend fun updateExerciseWeight(
        workoutNumber: Int,
        exerciseId: String,
        exerciseLibraryId: String,
        newWeight: Double,
        date: String
    ): Result<Unit> {
        return try {
            val workoutResult = getWorkout(workoutNumber)
            val workout = workoutResult.getOrNull() ?: return Result.failure(Exception("Workout not found"))

            // Find the exercise and update its current weight and history.
            val updatedExercises = workout.exercises.map { exercise ->
                if (exercise.id == exerciseId) {
                    val newRecord = ExerciseWeightRecord(newWeight, date) // Create record.
                    val updatedHistory = exercise.weightHistory + newRecord // Add to history list.
                    exercise.copy(
                        currentWeight = newWeight, // Set new current weight record.
                        weightHistory = updatedHistory
                    )
                } else {
                    exercise // Keep other exercises as they are.
                }
            }

            val updatedWorkout = workout.copy(exercises = updatedExercises) // Creates updated workout with new exercise data.
            saveWorkout(updatedWorkout) // Save the whole object back to Firestore.

            // Adds progress entry for insights chart tracking.
            progressRepository.addProgressEntry(
                ExerciseProgressEntry(
                    exerciseLibraryId = exerciseLibraryId, // Links progress to library exercise ID.
                    weight = newWeight, // Records the updated weight value.
                    date = date, // Sets the date of weight change.
                    workoutNumber = workoutNumber // Tracks which workout this occurred in.
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /**
     * Adds a new exercise to a workout.
     */
    override suspend fun addExerciseToWorkout(workoutNumber: Int, exercise: WorkoutExercise): Result<Unit> {
        return try {
            val workoutResult = getWorkout(workoutNumber) // Find the target workout.
            val workout = workoutResult.getOrNull() ?: return Result.failure(Exception("Workout not found"))

            // Generate a unique ID for the new exercise.
            val exerciseWithId = exercise.copy(id = firestore.collection("temp").document().id) // Create random ID.
            val updatedExercises = workout.exercises + exerciseWithId // Add to existing list.
            val updatedWorkout = workout.copy(exercises = updatedExercises) // Update the list in the workout.

            saveWorkout(updatedWorkout) // Saves changes to database.

            // Create initial progress entry if exercise has starting weight.
            if (exercise.currentWeight > 0 && exercise.exerciseLibraryId.isNotBlank()) {
                val today = java.time.LocalDate.now()
                    .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)  // Formats today's date as string.
                progressRepository.addProgressEntry(
                    ExerciseProgressEntry(
                        exerciseLibraryId = exercise.exerciseLibraryId, // Links entry to library exercise.
                        weight = exercise.currentWeight, // Records starting weight value.
                        date = today, // Sets record date to today.
                        workoutNumber = workoutNumber  // Links with a specific workout.
                    )
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes an exercise from a workout.
     */
    override suspend fun removeExerciseFromWorkout(workoutNumber: Int, exerciseId: String): Result<Unit> {
        return try {
            val workoutResult = getWorkout(workoutNumber)
            val workout = workoutResult.getOrNull() ?: return Result.failure(Exception("Workout not found"))

            // Filter out the exercise user decides to remove.
            val updatedExercises = workout.exercises.filter { it.id != exerciseId } // Remove by ID matches.
            val updatedWorkout = workout.copy(exercises = updatedExercises) // Create updated workout object.

            saveWorkout(updatedWorkout) // Save updated list to database.
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}