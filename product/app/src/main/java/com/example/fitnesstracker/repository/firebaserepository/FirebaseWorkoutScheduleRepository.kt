package com.example.fitnesstracker.repository.firebaserepository

import com.example.fitnesstracker.data.workout.WorkoutSchedule
import com.example.fitnesstracker.repository.repositoryinterface.WorkoutScheduleRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * FirebaseWorkoutScheduleRepository saves and gets the user's weekly workout plan.
 * - Stores the plan in users/{userId}/workout_schedule/current.
 */
class FirebaseWorkoutScheduleRepository(
    private val auth: FirebaseAuth, // Firebase login info.
    private val firestore: FirebaseFirestore // Firestore database.
) : WorkoutScheduleRepository {

    private fun getUserId(): String? = auth.currentUser?.uid

    /**
     * Finds & returns the Firestore collection path for the current user's workout schedule in the database.
     * - Throws exception if no user is logged in.
     */
    private fun currentScheduleDoc() =
        getUserId()?.let { uid ->
            firestore.collection("users").document(uid).collection("workoutSchedule").document("current")
        } ?: throw IllegalStateException("User not logged in")

    /**
     * Saves the workout schedule to the database.
     */
    override suspend fun saveSchedule(schedule: WorkoutSchedule): Result<Unit> {
        return try {
            currentScheduleDoc().set(schedule).await() // Saves the schedule map to Firestore.
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets the saved workout schedule from the database.
     */
    override suspend fun getSchedule(): Result<WorkoutSchedule?> {
        return try {
            val snapshot = currentScheduleDoc().get().await() // Fetch the 'current' doc.
            val schedule = snapshot.toObject(WorkoutSchedule::class.java) // Map to WorkoutSchedule object.
            Result.success(schedule)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}