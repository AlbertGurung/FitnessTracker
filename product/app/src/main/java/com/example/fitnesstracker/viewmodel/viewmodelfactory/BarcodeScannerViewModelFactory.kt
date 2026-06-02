package com.example.fitnesstracker.viewmodel.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstracker.repository.repositoryinterface.FoodItemRepository
import com.example.fitnesstracker.repository.repositoryinterface.UserProfileRepository
import com.example.fitnesstracker.repository.repositoryinterface.NutritionDiaryRepository
import com.example.fitnesstracker.viewmodel.BarcodeScannerViewModel

/**
 * Factory to create BarcodeScannerViewModel instances.
 * - Needed because BarcodeScannerViewModel requires FoodItemRepository, UserProfileRepository and NutritionDiaryRepository.
 * - ViewModelProvider cannot automatically create ViewModels with these parameters so the factory tells Android how to construct BarcodeScannerViewModel correctly.
 */
class BarcodeScannerViewModelFactory(
    private val repository: FoodItemRepository,
    private val userProfileRepository: UserProfileRepository,
    private val nutritionDiaryRepository: NutritionDiaryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BarcodeScannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BarcodeScannerViewModel(repository, userProfileRepository, nutritionDiaryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}