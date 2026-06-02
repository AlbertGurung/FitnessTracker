package com.example.fitnesstracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.workout.ExerciseProgressEntry
import com.example.fitnesstracker.repository.repositoryinterface.ExerciseProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing exercise weight progress entries.
 * - Tracks the currently selected exercise and loads its progress history.
 */
class ExerciseProgressViewModel(
    private val repository: ExerciseProgressRepository // Repository for progress data access.
) : ViewModel() {

    private val _selectedExerciseLibraryId = MutableStateFlow<String?>(null) // Internal mutable storage for selected exercise ID.
    val selectedExerciseLibraryId: StateFlow<String?> = _selectedExerciseLibraryId.asStateFlow() // Read-only StateFlow exposing selected exercise ID to observers.

    private val _progressEntries = MutableStateFlow<List<ExerciseProgressEntry>>(emptyList()) // Internal mutable StateFlow for progress entries list that can be updated.
    val progressEntries: StateFlow<List<ExerciseProgressEntry>> = _progressEntries.asStateFlow() // List of progress entries for selected exercise exposed as immutable StateFlow.


    /**
     * Selects an exercise and loads its progress history.
     * - Updates the selected exercise ID StateFlow.
     * - Triggers loading of progress entries for the chart.
     */
    fun selectExercise(exerciseLibraryId: String) {
        _selectedExerciseLibraryId.value = exerciseLibraryId // Updates selected exercise ID.
        loadProgress(exerciseLibraryId) // Triggers progress load for new selection.
    }

    /**
     * Loads progress entries for the specified exercise from the repository.
     * - Fetches entries and sorts them by date for chronological display.
     * - Updates the progressEntries StateFlow on successful load.
     */
    private fun loadProgress(exerciseLibraryId: String) {
        viewModelScope.launch {  // Launches coroutine for async repository call.
            repository.getProgressForExercise(exerciseLibraryId).onSuccess { // Fetches progress entries from repository.
                _progressEntries.value =
                    it.sortedBy { e -> e.date } // Sorts entries by date for chart display.
            }
        }
    }
}