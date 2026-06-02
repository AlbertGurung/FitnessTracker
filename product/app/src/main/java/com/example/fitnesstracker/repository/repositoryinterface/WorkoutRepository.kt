package com.example.fitnesstracker.repository.repositoryinterface

import com.example.fitnesstracker.data.workout.Workout
import com.example.fitnesstracker.data.workout.WorkoutExercise

/**
 * Interface containing methods for workout data access: save, load, and manage workouts and exercises.

 */
interface WorkoutRepository {

    suspend fun saveWorkout(workout: Workout): Result<String> // Saves or updates a workout.
    suspend fun getWorkout(workoutNumber: Int): Result<Workout?> // Gets a specific workout by number.
    suspend fun getAllWorkouts(): Result<List<Workout>> // Returns all workouts for the user.
    suspend fun updateExerciseWeight(workoutNumber: Int, exerciseId: String, exerciseLibraryId: String, newWeight: Double, date: String): Result<Unit> // Updates weight and tracks history.
    suspend fun addExerciseToWorkout(workoutNumber: Int, exercise: WorkoutExercise): Result<Unit> // Adds exercise to a workout.
    suspend fun removeExerciseFromWorkout(workoutNumber: Int, exerciseId: String): Result<Unit> // Removes exercise from a workout.
}