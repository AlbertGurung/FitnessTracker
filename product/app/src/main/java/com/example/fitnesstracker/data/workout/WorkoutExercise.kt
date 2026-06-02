package com.example.fitnesstracker.data.workout

import com.example.fitnesstracker.data.user.ExerciseWeightRecord

/**
 * Data class representing a single exercise inside a workout.
 * - Stores the exercise details and tracks weight progress over time.
 */
data class WorkoutExercise(
    val id: String? = null, // Unique ID for this exercise.
    val exerciseLibraryId: String = "",
    val name: String = "", // Name of the exercise.
    val sets: Int = 0, // Number of sets.
    val reps: Int = 0, // Number of reps per set.
    val currentWeight: Double = 0.0, // Current weight used in kg.
    val weightHistory: List<ExerciseWeightRecord> = emptyList() // List of past weight records for tracking progress.
)