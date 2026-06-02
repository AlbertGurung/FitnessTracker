package com.example.fitnesstracker.repository.repositoryinterface

import com.example.fitnesstracker.data.food.FoodItem

/**
 * Interface for food item data access: add item and get today's items for daily calories count.
 */
interface FoodItemRepository {
    suspend fun addFoodItem(item: FoodItem) // Adds Food Item.
    suspend fun getTodayFoodItems(): List<FoodItem> // Returns list for the multiple food items added
    suspend fun deleteFoodItem(item: FoodItem) // Deletes existing food item.
    suspend fun getFoodDataByBarcode(barcode: String): FoodItem // Gets food info from barcode.
    suspend fun searchFoodByName(query: String): List<FoodItem> // Search food by name, returns top suggestions.
    suspend fun getFoodItemsForDateRange(startDate: String, endDate: String): List<FoodItem> // Gets food items between two dates.
}