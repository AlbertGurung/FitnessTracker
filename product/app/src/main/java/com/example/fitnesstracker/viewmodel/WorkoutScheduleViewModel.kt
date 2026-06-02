package com.example.fitnesstracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.workout.WorkoutSchedule
import com.example.fitnesstracker.repository.repositoryinterface.WorkoutScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale


/**
 * WorkoutScheduleViewModel handles the user's weekly workout plan.
 * - Saves which days of the week the user works out.
 * - Helps the app find today's workout number.
 */
class WorkoutScheduleViewModel(
    private val repository: WorkoutScheduleRepository // Repository used to save and get data.
) : ViewModel() {

    // Internal state for the current screen status.
    private val _state = MutableStateFlow<ScheduleState>(ScheduleState.Idle)
    val state: StateFlow<ScheduleState> = _state.asStateFlow()

    // Holds the current workout schedule.
    private val _schedule = MutableStateFlow<WorkoutSchedule?>(null)
    val schedule: StateFlow<WorkoutSchedule?> = _schedule.asStateFlow()

    /**
     * Loads the saved workout schedule from the database.
     */
    fun loadSchedule() {
        viewModelScope.launch { // Starts a background task.
            _state.value = ScheduleState.Loading // Shows the loading spinner.
            repository.getSchedule().fold(
                onSuccess = { scheduleData ->
                    _schedule.value = scheduleData // Updates the schedule on the screen.
                    _state.value = ScheduleState.Idle // Done loading.
                },
                onFailure = { error ->
                    _state.value = ScheduleState.Error(error.message ?: "Failed to load") // Shows error message.
                }
            )
        }
    }

    /**
     * Saves a new weekly schedule with the chosen days.
     */
    fun saveSchedule(workoutsPerWeek: Int, selectedDays: Map<String, Int>) {
        viewModelScope.launch { // Starts the save task.
            _state.value = ScheduleState.Loading
            val schedule = WorkoutSchedule(
                workoutsPerWeek = workoutsPerWeek,
                scheduledDays = selectedDays
            )
            repository.saveSchedule(schedule).fold(
                onSuccess = {
                    _schedule.value = schedule // Updates local data immediately.
                    _state.value = ScheduleState.Success("Schedule has been saved") // Success feedback.
                },
                onFailure = { error ->
                    _state.value = ScheduleState.Error(error.message ?: "Failed to save")
                }
            )
        }
    }

    /**
     * Finds the workout number for today (e.g., Returns 2 if today is Wednesday).
     */
    fun getTodayWorkoutNumber(): Int? {
        val currentSchedule = _schedule.value ?: return null // Stops if no schedule exists.
        // Gets today's name like "Monday" or "Tuesday".
        val today = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        return currentSchedule.scheduledDays[today] // Looks up the day in the map.
    }
}

/**
 * ScheduleState defines if the UI is busy, finished, or has an error.
 * - Idle: Doing nothing.
 * - Loading: Working on something (like saving).
 * - Success: Done and showing a message.
 * - Error: Something went wrong.
 */
sealed class ScheduleState {
    object Idle : ScheduleState()
    object Loading : ScheduleState()
    data class Success(val message: String) : ScheduleState()
    data class Error(val message: String) : ScheduleState()
}
