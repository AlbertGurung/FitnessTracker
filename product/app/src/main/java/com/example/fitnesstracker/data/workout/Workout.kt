package com.example.fitnesstracker.data.workout

/**
 * Data class representing a single workout session.
 * - Groups a set of exercises under one named workout linked to a day in the schedule.
 */
data class Workout(
    val id: String? = null, // Document ID in Firestore.
    val workoutNumber: Int = 1, // Which workout this is e.g. 1 for Workout 1.
    val name: String = "", // Name of the workout (Workout No.).
    val exercises: List<WorkoutExercise> = emptyList(), // List of exercises in this workout.
)