package com.example.fitnesstracker.data.food

/**
 * Data class represents a user's daily nutrition totals.
 * - Stores total calories and macros for the day.
 */
data class UserNutritionDiary(
    val id: String? = null, // Document ID representing the date
    val date: String = "", // "YYYY-MM-DD" format.
    val totalCalories: Int = 0, // Total calories consumed (calories × quantity added).
    val totalProtein: Int = 0, // Total protein in grams (protein × quantity added).
    val totalCarbs: Int = 0, // Total carbohydrates in grams (carbohydrate × quantity added).
    val totalFat: Int = 0, // Total fat in grams (fat × quantity added).
    val goalCalories: Int = 0 // The goal calories for this specific date
)
