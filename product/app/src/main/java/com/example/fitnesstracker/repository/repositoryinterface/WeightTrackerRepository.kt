package com.example.fitnesstracker.repository.repositoryinterface

import com.example.fitnesstracker.data.user.WeightEntry

/**
 * Interface defining weight repository operations.
 * - Adding and deleting weight entries as well as getting list of the weight entries.
 */
interface WeightTrackerRepository {
    suspend fun addWeightEntry(entry: WeightEntry)
    suspend fun getWeightEntries(): List<WeightEntry>
    suspend fun deleteWeightEntry(entry: WeightEntry)
}