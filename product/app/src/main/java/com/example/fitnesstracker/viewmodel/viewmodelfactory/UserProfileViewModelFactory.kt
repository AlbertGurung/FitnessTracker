package com.example.fitnesstracker.viewmodel.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstracker.repository.repositoryinterface.UserProfileRepository
import com.example.fitnesstracker.repository.repositoryinterface.NutritionDiaryRepository
import com.example.fitnesstracker.utils.CaloriesCalculator
import com.example.fitnesstracker.viewmodel.UserProfileViewModel

/**
 * Factory class to create instances of UserProfileViewModel.
 * - Needed because UserProfileViewModel has constructor parameters (repository, calculator and nutritionDiaryRepository).
 * - ViewModelProvider cannot automatically create ViewModels with these parameters so the factory tells Android how to construct UserProfileViewModel correctly.
 */
class UserProfileViewModelFactory(
    private val repository: UserProfileRepository,
    private val calculator: CaloriesCalculator,
    private val nutritionDiaryRepository: NutritionDiaryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(repository, calculator, nutritionDiaryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}