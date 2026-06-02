package com.example.fitnesstracker.viewmodel.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstracker.repository.repositoryinterface.ExerciseProgressRepository
import com.example.fitnesstracker.viewmodel.ExerciseProgressViewModel

/**
 * Factory for creating ExerciseProgressViewModel with the required repository dependency.
 */
class ExerciseProgressViewModelFactory(
    private val repository: ExerciseProgressRepository // Repository injected into ViewModel.
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseProgressViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseProgressViewModel(repository) as T // Creates and returns ViewModel instance.
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}