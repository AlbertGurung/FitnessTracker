package com.example.fitnesstracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.user.WeightEntry
import com.example.fitnesstracker.repository.repositoryinterface.WeightTrackerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for weight tracker operations: fetching, adding, and deleting weight entries.
 * - Interacts with WeightRepository for saving and getting weight history.
 * - Manages the state of the weight operation by the weightState StateFlow.
 * - Provides the history of all weight entries for graph visualization.
 */
class WeightTrackerViewModel(
    private val repository: WeightTrackerRepository // Repository for weight data
) : ViewModel() {

    // Holds current state internally.
    private val _weightState = MutableStateFlow<WeightState>(WeightState.Idle)

    // Public read-only state exposed to the UI.
    val weightState: StateFlow<WeightState> = _weightState.asStateFlow()

    // Holds the list of weight entries.
    private val _weightEntries = MutableStateFlow<List<WeightEntry>>(emptyList())
    val weightEntries: StateFlow<List<WeightEntry>> = _weightEntries.asStateFlow()

    /**
     *  Fetches all weight entries from repository.
     */
    fun loadWeightEntries() {

        _weightState.value = WeightState.Loading // Show loading state.
        viewModelScope.launch {
            try {
                val entries = repository.getWeightEntries()
                _weightEntries.value = entries
                _weightState.value = WeightState.Success // Show success when loaded.
            } catch (e: Exception) {
                _weightState.value = WeightState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Add a new weight entry and reload the list.
     */
    fun addWeight(weight: Double) {
        _weightState.value = WeightState.Loading
        viewModelScope.launch {
            try {
                val entry = WeightEntry(weight = weight)
                repository.addWeightEntry(entry)
                loadWeightEntries() // Reload the updated list
            } catch (e: Exception) {
                _weightState.value = WeightState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Delete a specific weight entry and reload the list.
     */
    fun deleteWeight(entry: WeightEntry) {
        _weightState.value = WeightState.Loading
        viewModelScope.launch {
            try {
                repository.deleteWeightEntry(entry)
                loadWeightEntries() // Reload the updated list after deleting
            } catch (e: Exception) {
                _weightState.value = WeightState.Error(e.message ?: "Unknown error")
            }
        }
    }

}


/**
 * Sealed class representing all weight tracking states for the UI.
 */
sealed class WeightState {
    object Loading : WeightState()
    object Success : WeightState()
    data class Error(val message: String) : WeightState()
    object Idle : WeightState()
}
