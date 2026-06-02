package com.example.fitnesstracker.data.workout

/**
 * Data class representing the user's weekly workout schedule.
 * - Stores which days of the week the user works out and how many times per week.
 */
data class WorkoutSchedule(
    val workoutsPerWeek: Int = 0, // Number of workouts done per week.
    val scheduledDays: Map<String, Int> = emptyMap() // Map of day name to workout number e.g. "Monday" to 1.
)