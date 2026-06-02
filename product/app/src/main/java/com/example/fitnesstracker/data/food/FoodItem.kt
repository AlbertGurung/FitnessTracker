package com.example.fitnesstracker.data.food

/**
 * Data class for a foodItem containing macro information about the food item as well as date food item added and an unique id.
 */
data class FoodItem(
    val id: String? = null,
    val name: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val carbohydrate: Int = 0,
    val fat: Int = 0,
    val quantity: Double = 1.0,
    val dateAdded: String? = null // "DD/MM/YYYY" for filtering today's items
)