package com.example.fitnesstracker.repository.repositoryinterface

import com.example.fitnesstracker.data.food.UserNutritionDiary

/**
 * Interface for daily nutrition diary data access handling saving and retrieval of daily summaries.
 */
interface NutritionDiaryRepository {
    suspend fun saveDiary(diary: UserNutritionDiary) // Saves the daily nutrition total in database.
    suspend fun getDiary(date: String): UserNutritionDiary? // Fetches a single diary starting from a specific date.
    suspend fun getDiariesForDateRange(startDate: String, endDate: String): List<UserNutritionDiary> // GETS list of diaries for the bar chart within a date range.
}
