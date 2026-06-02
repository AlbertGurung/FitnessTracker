package com.example.fitnesstracker.openfoodfacts

import com.google.gson.annotations.SerializedName

/**
 * Full response from OpenFoodFacts API.
 * - Includes status and the product data if item found.
 */
data class OpenFoodFactsResponse(
    val status: Int, // API status (code 1 if success or else not found)
    val product: OpenFoodFactsProduct? // Product info if available
)

/**
 * Search response from OpenFoodFacts API when searching by name.
 * - Contains a list of products matching the search terms.
 */
data class OpenFoodFactsSearchResponse(
    val count: Int, // Total number of products found
    val page: Int, // Current page number
    @SerializedName("page_size")
    val pageSize: Int, // Number of products per page
    val products: List<OpenFoodFactsProduct> // List of matching products
)

/**
 * OpenFoodFactsProduct info. returned by OpenFoodFacts.
 * Includes name and nutrition info.
 */
data class OpenFoodFactsProduct(
    @SerializedName("product_name") //Links JSON field name to this variable
    // Used because JSON uses snake_case but Kotlin uses camelCase
    val productName: String?, // Name of the product

    val nutriments: OpenFoodFactsNutriments? // Nutrition details
)

/**
 * Nutrition values for a product.
 * - Usually based on 100g of the food such that macros are done per 100g.
 */
data class OpenFoodFactsNutriments(
    @SerializedName("energy-kcal_100g") // Calories per 100g
    val energyKcal100g: Double?,

    @SerializedName("proteins_100g") // Protein per 100g
    val proteins100g: Double?,

    @SerializedName("carbohydrates_100g") // Carbs per 100g
    val carbohydrates100g: Double?,

    @SerializedName("fat_100g") // Fat per 100g
    val fat100g: Double?
)