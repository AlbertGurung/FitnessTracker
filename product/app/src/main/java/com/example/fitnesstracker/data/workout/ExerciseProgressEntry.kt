package com.example.fitnesstracker.data.workout

/**
 * Data class representing a single weight record for exercise progress tracking.
 * - Stores weight lifted on a specific date for chart display.
 * - Links to ExerciseLibrary via exerciseLibraryId.
 */
data class ExerciseProgressEntry(
    val id: String = "", // Document ID in Firestore.
    val exerciseLibraryId: String = "", // Links to ExerciseLibrary.id for grouping.
    val weight: Double = 0.0, // Weight lifted in kg.
    val date: String = "", // Date of record in yyyy-MM-dd format.
    val workoutNumber: Int = 0 // Which workout this record belongs to.
)