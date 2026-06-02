package com.example.fitnesstracker.openfoodfacts

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Service that sets up and provides access to the OpenFoodFacts API.
 * - Creates a Retrofit instance for network calls.
 * - Uses Gson to convert JSON responses into Kotlin objects.
 * - Exposes the API interface for making requests.
 * - Lazily initialised so it’s only created when first used.
 */
object OpenFoodFactsService {
    val api: OpenFoodFactsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/") // URL for all API calls
            .addConverterFactory(GsonConverterFactory.create()) // converts JSON into Kotlin
            .build()
            .create(OpenFoodFactsApi::class.java) // creates the API implementation
    }
}