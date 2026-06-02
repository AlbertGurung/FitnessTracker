package com.example.fitnesstracker.repository.repositoryinterface

import com.example.fitnesstracker.data.workout.ExerciseProgressEntry

/**
 * Interface containing methods for exercise progress data access: save and load progress entries.
 */
interface ExerciseProgressRepository {
    suspend fun addProgressEntry(entry: ExerciseProgressEntry): Result<Unit> // Saves a new weight progress entry.
    suspend fun getProgressForExercise(exerciseLibraryId: String): Result<List<ExerciseProgressEntry>> // Returns all progress entries for a given exercise.
}