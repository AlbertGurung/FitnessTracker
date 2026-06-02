package com.example.fitnesstracker.repository.repositoryinterface

import com.example.fitnesstracker.data.workout.WorkoutSchedule

/**
 * Interface for workout schedule data access: save and load weekly schedule.
 */
interface WorkoutScheduleRepository {
    suspend fun saveSchedule(schedule: WorkoutSchedule): Result<Unit> // Saves the weekly workout schedule.
    suspend fun getSchedule(): Result<WorkoutSchedule?> // Gets the saved schedule or null if none exists.
}