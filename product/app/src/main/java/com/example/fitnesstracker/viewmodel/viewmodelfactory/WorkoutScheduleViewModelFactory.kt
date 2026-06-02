package com.example.fitnesstracker.viewmodel.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstracker.repository.repositoryinterface.WorkoutScheduleRepository
import com.example.fitnesstracker.viewmodel.WorkoutScheduleViewModel

/**
 * Factory to create WorkoutScheduleViewModel instances.
 * - Needed because WorkoutScheduleViewModel requires a WorkoutScheduleRepository.
 * - ViewModelProvider cannot automatically create ViewModels with these parameters so the factory tells Android how to construct WorkoutScheduleViewModel correctly.
 */
class WorkoutScheduleViewModelFactory(
    private val repository: WorkoutScheduleRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutScheduleViewModel(repository) as T // Pass repository to ViewModel
        }
        throw IllegalArgumentException("Unknown ViewModel class") // Error if wrong ViewModel requested
    }
}