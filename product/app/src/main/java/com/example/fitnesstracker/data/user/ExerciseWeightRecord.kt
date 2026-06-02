package com.example.fitnesstracker.data.user

/**
 * Data class representing a single weight record for an exercise.
 * - Used to track how the weight changes over time.
 */
data class ExerciseWeightRecord(
    val weight: Double = 0.0, // The weight recorded in kg.
    val date: String = "" // Date the weight was logged (yyyy-mm-dd).
)