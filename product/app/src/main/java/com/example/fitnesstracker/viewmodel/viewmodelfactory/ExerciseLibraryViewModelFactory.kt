package com.example.fitnesstracker.viewmodel.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstracker.repository.repositoryinterface.ExerciseLibraryRepository
import com.example.fitnesstracker.viewmodel.ExerciseLibraryViewModel

/**
 * Factory for creating ExerciseLibraryViewModel with the required repository dependency.
 */
class ExerciseLibraryViewModelFactory(
    private val repository: ExerciseLibraryRepository // Repository injected into ViewModel.
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseLibraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseLibraryViewModel(repository) as T // Creates and returns ViewModel instance.
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}