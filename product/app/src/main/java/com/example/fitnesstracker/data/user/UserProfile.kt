package com.example.fitnesstracker.data.user

/**
 * Data class for a user's profile containing the information
 * needed to calculate daily calorie requirements using the Mifflin-St Jeor Equation.
 */

data class UserProfile(
    val name: String = "",
    val height: Double = 0.0, // User's height in centimeters
    val weight: Double = 0.0, // User's weight in kilograms
    val age: Int = 0,       // User's age in years
    val gender: Gender = Gender.MALE, // User's gender
    val activityLevel: ActivityLevel = ActivityLevel.SEDENTARY,  // User's daily activity multiplier - set as default option: SEDENTARY
    val goal: GoalType = GoalType.MAINTAIN_WEIGHT,  // User's weight goal (lose, maintain or gain) - set as default option: MAINTAIN
    val calculatedCalories: Int? = null, // Calculated daily calorie requirements
    val goalWeight: Double = 0.0
)

enum class Gender {
    MALE, FEMALE
}

// Activity levels with multipliers from the Mifflin St Jeor reference used to adjust BMR for daily energy expenditure.
enum class ActivityLevel(val multiplier: Double) {
    SEDENTARY(1.2),  // Little or no exercise.
    LIGHT(1.375),   // Light exercise 1-3 days/week.
    MODERATE(1.55), // Moderate exercise 3-5 days/week.
    ACTIVE(1.725), // Hard exercise 6-7 days/week.
    VERY_ACTIVE(1.9)  // Very hard exercise or physical job.
}

// User's goal with calorie adjustment for weight management.
enum class GoalType(val calorieAdjustment: Int) {
    LOSE_WEIGHT(-400), // Reduce calories to lose weight.
    MAINTAIN_WEIGHT(0), // Maintain current weight.
    GAIN_WEIGHT(400) // Increase calories to gain weight.
}
