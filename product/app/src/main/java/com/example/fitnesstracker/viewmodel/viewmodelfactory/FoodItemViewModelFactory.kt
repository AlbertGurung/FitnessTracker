package com.example.fitnesstracker.viewmodel.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstracker.repository.repositoryinterface.FoodItemRepository
import com.example.fitnesstracker.repository.repositoryinterface.UserProfileRepository
import com.example.fitnesstracker.repository.repositoryinterface.NutritionDiaryRepository
import com.example.fitnesstracker.viewmodel.FoodItemViewModel

/**
 * Factory to create FoodItemViewModel instances.
 * - Needed because FoodItemViewModel requires FoodItemRepository, UserProfileRepository and NutritionDiaryRepository.
 * - ViewModelProvider cannot automatically create ViewModels with these parameters so the factory tells Android how to construct FoodItemViewModel correctly.
 */
class FoodItemViewModelFactory(
    private val foodItemRepository: FoodItemRepository,
    private val userProfileRepository: UserProfileRepository,
    private val nutritionDiaryRepository: NutritionDiaryRepository
) : ViewModelProvider.Factory {

    // Called by Android to create the ViewModel
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodItemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodItemViewModel(foodItemRepository, userProfileRepository, nutritionDiaryRepository) as T // Pass repository to ViewModel
        }
        throw IllegalArgumentException("Unknown ViewModel class") // Error if wrong ViewModel requested
    }
}