package com.example.fitnesstracker.viewmodel.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstracker.repository.repositoryinterface.WeightTrackerRepository
import com.example.fitnesstracker.viewmodel.WeightTrackerViewModel

/**
 * Factory for creating WeightTrackerViewModel with its repository dependency.
 */
class WeightTrackerViewModelFactory(
    private val repository: WeightTrackerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeightTrackerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeightTrackerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}