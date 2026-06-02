package com.example.fitnesstracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.workout.Workout
import com.example.fitnesstracker.data.workout.WorkoutExercise
import com.example.fitnesstracker.repository.repositoryinterface.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * WorkoutViewModel handles adding, deleting and updating workouts.
 * - Connects to the database to save workout data.
 * - Updates the UI when workouts change.
 */
class WorkoutViewModel(
    private val repository: WorkoutRepository // Repository used to save and get data.
) : ViewModel() {

    // Internal state for the current screen status (Idle, Loading, Success, Error).
    private val _state = MutableStateFlow<WorkoutState>(WorkoutState.Idle)

    val state: StateFlow<WorkoutState> = _state.asStateFlow()

    // Holds the list of all workouts for this user.
    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts: StateFlow<List<Workout>> = _workouts.asStateFlow()


    /**
     * Loads every workout the user has saved.
     */
    fun loadAllWorkouts() {
        viewModelScope.launch { // Launch coroutine for background work.
            repository.getAllWorkouts().fold( // Handle success or failure.
                onSuccess = { workoutList ->
                    _workouts.value = workoutList // Update workouts list.
                },
                onFailure = { error ->
                    _state.value = WorkoutState.Error(error.message ?: "Failed to load") // Set error state.
                }
            )
        }
    }

    /**
     * Loads one specific workout by its number.
     */
    fun loadWorkout(workoutNumber: Int) {
        viewModelScope.launch { // Launch coroutine for background work.
            repository.getWorkout(workoutNumber).fold( // Handle success or failure.
                onSuccess = { workout ->
                    workout?.let {
                        // Replace the workout in the list with updated data.
                        val updatedList = _workouts.value.map { w ->
                            if (w.workoutNumber == workoutNumber) it else w
                        }
                        _workouts.value = updatedList
                    }
                },
                onFailure = { error ->
                    _state.value = WorkoutState.Error(error.message ?: "Failed to load") // Set error state.
                }
            )
        }
    }

    /**
     * Creates a new workout with a name Workout & it's number.
     */
    fun createWorkout(workoutNumber: Int, name: String) {
        viewModelScope.launch { // Launch coroutine for background work.
            _state.value = WorkoutState.Loading // Show loading state.
            val workout = Workout( // Create new workout object.
                workoutNumber = workoutNumber,
                name = name,
                exercises = emptyList()
            )
            repository.saveWorkout(workout).fold( // Save and handle result.
                onSuccess = {
                    _state.value = WorkoutState.Success("Workout created!") // Success feedback.
                    loadAllWorkouts() // Refresh list.
                },
                onFailure = { error ->
                    _state.value = WorkoutState.Error(error.message ?: "Failed to create") // Set error state.
                }
            )
        }
    }

    /**
     * Adds a new exercise to an existing workout.
     */
    fun addExerciseToWorkout(workoutNumber: Int, exercise: WorkoutExercise) {
        viewModelScope.launch { // Launch coroutine for background work.
            _state.value = WorkoutState.Loading // Show loading state.
            repository.addExerciseToWorkout(workoutNumber, exercise).fold( // Add and handle result.
                onSuccess = {
                    _state.value = WorkoutState.Success("Exercise has been added!") // Success feedback.
                    loadWorkout(workoutNumber) // Refresh specific workout.
                    loadAllWorkouts() // Refresh full list.
                },
                onFailure = { error ->
                    _state.value = WorkoutState.Error(error.message ?: "Failed to add") // Set error state.
                }
            )
        }
    }

    /**
     * Removes an exercise from a workout.
     */
    fun removeExerciseFromWorkout(workoutNumber: Int, exerciseId: String) {
        viewModelScope.launch { // Launch coroutine for background work.
            _state.value = WorkoutState.Loading // Show loading state.
            repository.removeExerciseFromWorkout(workoutNumber, exerciseId).fold( // Remove and handle result.
                onSuccess = {
                    _state.value = WorkoutState.Success("Exercise has been deleted!") // Success feedback.
                    loadWorkout(workoutNumber) // Refresh after delete.
                    loadAllWorkouts() // Refresh full list.
                },
                onFailure = { error ->
                    _state.value = WorkoutState.Error(error.message ?: "Failed to delete") // Set error state.
                }
            )
        }
    }

    /**
     * Updates the weight of an exercise and saves it into the history.
     */
    fun updateExerciseWeight(workoutNumber: Int, exerciseId: String, exerciseLibraryId: String, newWeight: Double, date: String) {
        viewModelScope.launch { // Launch coroutine for background work.
            _state.value = WorkoutState.Loading // Show loading state.
            repository.updateExerciseWeight(workoutNumber = workoutNumber, exerciseId = exerciseId ,exerciseLibraryId = exerciseLibraryId, newWeight = newWeight, date = date).fold( // Update and handle result.
                onSuccess = {
                    _state.value = WorkoutState.Success("Weight updated!") // Success feedback.
                    loadWorkout(workoutNumber) // Refresh with new weight.
                    loadAllWorkouts() // Refresh full list.
                },
                onFailure = { error ->
                    _state.value = WorkoutState.Error(error.message ?: "Failed to update weight") // Set error state.
                }
            )
        }
    }
}


/**
 * Sealed class representing all workout states for the UI.
 */
sealed class WorkoutState {
    object Idle : WorkoutState()
    object Loading : WorkoutState()
    data class Success(val message: String) : WorkoutState()
    data class Error(val message: String) : WorkoutState()
}