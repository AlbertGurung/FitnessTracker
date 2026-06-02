package com.example.fitnesstracker.data.user

/**
 * Data class representing a single weight entry for a user.
 * - Used to track weight progression over time.
 */
data class WeightEntry(
    val id: String? = null, // Document ID in Firestore
    val weight: Double = 0.0, // The weight recorded in kg
    val dateAdded: String = "" // Date of entry in yyyy-mm-dd format
)