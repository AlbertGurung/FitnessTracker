package com.example.fitnesstracker.data.workout

/**
 * Data class representing an exercise in the central library.
 * - Stores exercise details for reuse across workouts.
 * - Links to exercise progress entries via lowercase name ID.
 */
data class ExerciseLibrary(
    val id: String = "", // Document ID matching lowercase exercise name.
    val name: String = "", // Display name of the exercise.
    val muscleGroup: String = "", // Category (Chest, Back, Legs, Shoulders, Others).
    val createdByUser: Boolean = false // True if user added or false if predefined.
)