package com.example.fitnesstracker.utils

import com.example.fitnesstracker.data.user.Gender
import com.example.fitnesstracker.data.user.UserProfile

/**
 * Calculator for daily calorie requirements using
 * Mifflin-St Jeor Equation, activity level, and goal.
 */
class CaloriesCalculator {

    fun calculateDailyCalories(user: UserProfile): Int {
        // Calculate BMR based on gender
        val bmr = when (user.gender) {
            Gender.MALE -> 10 * user.weight + 6.25 * user.height - 5 * user.age + 5
            Gender.FEMALE -> 10 * user.weight + 6.25 * user.height - 5 * user.age - 161
        }

        // Multiply by activity multiplier
        val caloriesWithActivity = bmr * user.activityLevel.multiplier

        // Adjust calories based on goal
        val finalCalories = caloriesWithActivity + user.goal.calorieAdjustment

        return finalCalories.toInt()
    }
}
