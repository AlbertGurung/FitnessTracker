package com.example.fitnesstracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.user.UserProfile
import com.example.fitnesstracker.repository.repositoryinterface.NutritionDiaryRepository
import com.example.fitnesstracker.repository.repositoryinterface.UserProfileRepository
import com.example.fitnesstracker.utils.CaloriesCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel responsible for user profile operations: fetching, calculating and saving profile data.
 * - Interacts with UserProfileRepository for saving(updates/creation) and getting user profile.
 * - Uses CaloriesCalculator to calculate user's daily caloric needs with Mifflin-St Jeor formula.
 * - Manages the state of the profile operation by the profileState StateFlow showing states(loading, success or error).
 */
class UserProfileViewModel(
    private val repository: UserProfileRepository, // Repository for user profile data
    private val calculator: CaloriesCalculator,
    private val nutritionDiaryRepository: NutritionDiaryRepository  // Repository for updating today's diary goal on profile save.
) : ViewModel() {

    // Holds current state internally.
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)

    // Public read-only state exposed to the UI so only the ViewModel can update it.
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    /**
     * Calculate daily calories and save updated profile.
     */
    fun calculateAndSaveProfile(userProfile: UserProfile) {
        _profileState.value = ProfileState.Loading // Show loading state.
        viewModelScope.launch {
            try {
                val calories = calculator.calculateDailyCalories(userProfile) // Compute calories for user using their profile details.
                val updatedProfile = userProfile.copy(calculatedCalories = calories)  // Copies profile with the newly calculated calories.
                repository.saveUserProfile(updatedProfile) // Saves profile in Firestore.
                
                // Immediately update today's diary with new goal calories 
                val today = LocalDate.now().toString() // Gets today's date to update the diary goal.
                val existingDiary = nutritionDiaryRepository.getDiary(today) // Fetches today's existing diary if it exists.
                if (existingDiary != null) { // Only updates if a diary record already exists for today.
                    val updatedDiary = existingDiary.copy(goalCalories = calories) // Copies diary with the new goal calories.
                    nutritionDiaryRepository.saveDiary(updatedDiary) // Saves the updated diary back to the database.
                }
                
                _profileState.value = ProfileState.Success(updatedProfile) // Show success state when profile is successfully saved.
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Unknown error") // Show error state if calculation or save fails

            }
        }
    }

    /**
     * Fetches the user profile from the repository and updates the profile state.
     */
    fun getUserProfile() {
        _profileState.value = ProfileState.Loading // Show loading in UI whilst getting the user profile.
        viewModelScope.launch {
            try {
                val profile = repository.getUserProfile() // Gets profile from Firestore and stored locally as profile.
                if (profile != null) { // Checks if it's found by null check.
                    _profileState.value = ProfileState.Success(profile)  // Show success state when profile is found.
                } else {
                    _profileState.value = ProfileState.Idle // Show idle state if no profile exists
                }
            } catch (e: Exception) {  // Show error state if for some reason like user not existing or cannot access FireStore when trying to get user profile.
                _profileState.value = ProfileState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

/**
 * Sealed class representing all user profile states for the UI: Loading, Success, Error, or Idle.
 */
sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val userProfile: UserProfile) : ProfileState()
    data class Error(val message: String) : ProfileState()
    object Idle : ProfileState()
}