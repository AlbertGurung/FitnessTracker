package com.example.fitnesstracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.workout.ExerciseLibrary
import com.example.fitnesstracker.repository.repositoryinterface.ExerciseLibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the exercise library.
 * - Loads all exercises from the repository and exposes them as StateFlow.
 * - Handles adding new custom exercises with callback for immediate use.
 */
class ExerciseLibraryViewModel(
    private val repository: ExerciseLibraryRepository // Repository for exercise library data access.
) : ViewModel() {

    private val _exercises = MutableStateFlow<List<ExerciseLibrary>>(emptyList())

    val exercises: StateFlow<List<ExerciseLibrary>> = _exercises.asStateFlow() // List of all exercises in the library.

    /**
     * Loads all exercises from the repository into the exercises StateFlow.
     */
    fun loadAllExercises() {

        viewModelScope.launch {
            repository.getAllExercises().onSuccess {
                _exercises.value = it // Updates StateFlow with loaded exercises.
            }
        }
    }

    /**
     * Adds a new custom exercise to the library.
     * - Refreshes the exercise list after successful addition.
     * - Calls onSuccess callback with the created ExerciseLibrary entry.
     */
    fun addCustomExercise(

        name: String, // Name of the exercise to add.
        muscleGroup: String, // Muscle group category for the exercise.

        onSuccess: (ExerciseLibrary) -> Unit = {} // Callback invoked with the created entry.
    ) {
        viewModelScope.launch {
            repository.addCustomExercise(name, muscleGroup).onSuccess {

                loadAllExercises() // Refreshes list to include new exercise
                onSuccess(it) // Invokes callback with the created library entry.

            }
        }
    }
}