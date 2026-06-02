package com.example.fitnesstracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.food.FoodItem
import com.example.fitnesstracker.data.food.UserNutritionDiary
import com.example.fitnesstracker.repository.repositoryinterface.FoodItemRepository
import com.example.fitnesstracker.repository.repositoryinterface.NutritionDiaryRepository
import com.example.fitnesstracker.repository.repositoryinterface.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel responsible for barcode scanning operations.
 * - Interacts with FoodItemRepository to retrieve food information.
 * - Manages the scanner state using StateFlow to represent
 *   loading, success or error states for the UI.
 */
class BarcodeScannerViewModel(
    private val repository: FoodItemRepository, // Repository for food data.
    private val userProfileRepository: UserProfileRepository, // Repository for user profile data.
    private val nutritionDiaryRepository: NutritionDiaryRepository // Repository for daily nutrition diary.
) : ViewModel() {

    // Holds the current barcode scanner state internally.
    private val _barcodeScannerState =
        MutableStateFlow<BarcodeScannerState>(BarcodeScannerState.Idle)

    // Public read-only state exposed to the UI to observe scanner updates.
    val scannerState: StateFlow<BarcodeScannerState> = _barcodeScannerState.asStateFlow()

    /**
     * Fetch food data by barcode from repository whilst checking for null or empty cases.
     */
    fun fetchFoodDataByBarcode(barcode: String?) {
        if (barcode.isNullOrEmpty()) { // Checks against null or empty barcode input.
            _barcodeScannerState.value = BarcodeScannerState.Error("Invalid barcode")
            return
        }

        viewModelScope.launch {
            try {
                _barcodeScannerState.value = BarcodeScannerState.Loading // Loading state to be used in UI.

                val foodData = repository.getFoodDataByBarcode(barcode)  // Gets food data from repository.
                _barcodeScannerState.value = BarcodeScannerState.Success(foodData) // Shows success state with the fetched food data.

            } catch (e: Exception) {
                _barcodeScannerState.value = BarcodeScannerState.Error(  // Notify UI if fetching fails.
                    e.message ?: "Failed to fetch food data"
                )
            }
        }
    }

    /**
     * Add the last successfully fetched food item to the repository.
     */
    fun addScannedFood(quantity: Double = 1.0) { // default of 1 quantity/serving
        val current = _barcodeScannerState.value
        if (current !is BarcodeScannerState.Success) return // Early returns when scan is unsuccessful.

        viewModelScope.launch {
            try {
                // Copy the fetched food item, but override its quantity with the user's input
                val foodWithQuantity = current.foodItem.copy(quantity = quantity) // Copies scanned food item with user's chosen quantity.
                repository.addFoodItem(foodWithQuantity)  // Saves food item.
                
                // Sync to diary
                val itemDate = foodWithQuantity.dateAdded ?: LocalDate.now().toString() // Gets the item's date to today.
                updateNutritionDiaryForDate(itemDate)

            } catch (e: Exception) {
                _barcodeScannerState.value = BarcodeScannerState.Error(
                    e.message ?: "Failed to add food item"
                )
            }
        }
    }

    /**
     * Syncs daily nutrition to Firestore using the diary repository. It recalculates and saves the nutrition diary for a given date after a scanned food item is added.
     */
    private fun updateNutritionDiaryForDate(date: String) {
        viewModelScope.launch {
            try {
                // Fetch items for the date to calculate sums
                val items = repository.getFoodItemsForDateRange(date, date)
                
                // Get existing diary to keep goal intact or create new one with current goal.
                val existingDiary = nutritionDiaryRepository.getDiary(date)
                val targetGoal = existingDiary?.goalCalories ?: (userProfileRepository.getUserProfile()?.calculatedCalories ?: 0)

                val updatedDiary = UserNutritionDiary(
                    id = date,
                    date = date,
                    totalCalories = items.sumOf { (it.calories * it.quantity).toInt() },
                    totalProtein = items.sumOf { (it.protein * it.quantity).toInt() },
                    totalCarbs = items.sumOf { (it.carbohydrate * it.quantity).toInt() },
                    totalFat = items.sumOf { (it.fat * it.quantity).toInt() },
                    goalCalories = targetGoal
                )
                nutritionDiaryRepository.saveDiary(updatedDiary)
            } catch (e: Exception) {
            }
        }
    }

    /**
     * Updates the barcode scanner state based on camera permission result.
     * - isGranted True if camera permission is granted otherwise it's false.
     */
    fun getCameraPermission(isGranted: Boolean) {
        _barcodeScannerState.value = if (isGranted) {
            BarcodeScannerState.GrantedCameraPermission
        } else {
            BarcodeScannerState.DeniedCameraPermission
        }
    }

}


/**
 * Sealed class representing all barcode scanner UI states.
 */
sealed class BarcodeScannerState {

    object Loading : BarcodeScannerState()
    data class Success(val foodItem: FoodItem) : BarcodeScannerState()
    data class Error(val message: String) : BarcodeScannerState()
    object Idle : BarcodeScannerState()
    object DeniedCameraPermission : BarcodeScannerState()
    object GrantedCameraPermission : BarcodeScannerState()
}
