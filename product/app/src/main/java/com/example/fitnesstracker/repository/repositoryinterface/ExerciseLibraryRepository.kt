package com.example.fitnesstracker.repository.repositoryinterface

import com.example.fitnesstracker.data.workout.ExerciseLibrary

/**
 * Interface containing methods for exercise library data access.
 * - Provides operations for fetching and adding exercises to the library.
 */
interface ExerciseLibraryRepository {

    suspend fun getAllExercises(): Result<List<ExerciseLibrary>> // Fetches all exercises from the library.
    suspend fun getExercisesByMuscleGroup(muscleGroup: String): Result<List<ExerciseLibrary>> // Fetches exercises filtered by muscle group.
    suspend fun addCustomExercise(name: String, muscleGroup: String): Result<ExerciseLibrary> // Adds a new custom exercise to the library.
}