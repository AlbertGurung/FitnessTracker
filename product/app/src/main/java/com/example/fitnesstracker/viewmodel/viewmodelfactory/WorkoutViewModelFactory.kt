package com.example.fitnesstracker.viewmodel.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstracker.repository.repositoryinterface.WorkoutRepository
import com.example.fitnesstracker.viewmodel.WorkoutViewModel

/**
 * Factory to create WorkoutViewModel instances.
 * - Needed because WorkoutViewModel requires a WorkoutRepository.
 * - ViewModelProvider cannot automatically create ViewModels with these parameters so the factory tells Android how to construct WorkoutViewModel correctly.
 */
class WorkoutViewModelFactory(

    private val repository: WorkoutRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) { // Check if the requested ViewModel is of type WorkoutViewModel

            // Create a new WorkoutViewModel instance with the provided repository
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(repository) as T // Pass repository to ViewModel
        }
        throw IllegalArgumentException("Unknown ViewModel class") // Error if wrong ViewModel requested
    }
}