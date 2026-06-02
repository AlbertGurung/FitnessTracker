package com.example.fitnesstracker.openfoodfacts

import com.example.fitnesstracker.data.food.FoodItem

/**
 * This function takes the product from the OpenFoodFacts API and turns it into a FoodItem used in the app.
 * - It pulls out the needed fields like name and nutrition values and handles missing data with default values.
 */
fun OpenFoodFactsProduct.toFoodItem(): FoodItem {
    val nutriments = this.nutriments
    return FoodItem(
        id = null,
        name = productName ?: "Unknown product", // If product name not found then fallback name of unknown product shown.
        calories = nutriments?.energyKcal100g?.toInt() ?: 0,
        protein = nutriments?.proteins100g?.toInt() ?: 0,
        carbohydrate = nutriments?.carbohydrates100g?.toInt() ?: 0,
        fat = nutriments?.fat100g?.toInt() ?: 0,
        dateAdded = null
    )
}