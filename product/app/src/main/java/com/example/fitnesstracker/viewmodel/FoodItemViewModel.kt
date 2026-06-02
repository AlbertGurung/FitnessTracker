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
 * ViewModel responsible for food item operations.
 * - Interacts with FoodItemRepository to add food items and load food items.
 * - Manages the state of the food item operation using StateFlow
 *   to represent loading, success or error states for the UI.
 */
class FoodItemViewModel(
    private val foodItemRepository: FoodItemRepository, // Repository for food item data
    private val userProfileRepository: UserProfileRepository, // Repository for user profile data.
    private val nutritionDiaryRepository: NutritionDiaryRepository // Repository for daily nutrition diary data.
) : ViewModel() {

    // Holds current food item state internally.
    private val _foodItemState = MutableStateFlow<FoodItemState>(FoodItemState.Idle)

    // Public read-only state exposed to the UI to observe changes.
    val foodItemState: StateFlow<FoodItemState> = _foodItemState.asStateFlow()

    // Holds the list of today's food items internally, initially empty.
    private val _todayItems = MutableStateFlow<List<FoodItem>>(emptyList())

    // Public read-only StateFlow exposed to the UI to observe changes in today's food items.
    val todayItems: StateFlow<List<FoodItem>> = _todayItems.asStateFlow()

    // Holds total daily calories internally.
    private val _totalDailyCaloriesCount = MutableStateFlow(0)

    // Public read-only calories total exposed to the UI.
    val totalDailyCaloriesCount: StateFlow<Int> = _totalDailyCaloriesCount.asStateFlow()

    // Holds total daily protein internally.
    private val _totalDailyProtein = MutableStateFlow(0)

    // Public read-only protein total exposed to the UI.
    val totalDailyProtein: StateFlow<Int> = _totalDailyProtein.asStateFlow()

    // Holds total daily carbs internally.
    private val _totalDailyCarbs = MutableStateFlow(0)

    // Public read-only carbs total exposed to the UI.
    val totalDailyCarbs: StateFlow<Int> = _totalDailyCarbs.asStateFlow()

    // Holds total daily fat internally.
    private val _totalDailyFat = MutableStateFlow(0)

    // Public read-only fat total exposed to the UI.
    val totalDailyFat: StateFlow<Int> = _totalDailyFat.asStateFlow()

    // Holds user's daily nutrition totals mapped by date for insights and charts.
    private val _userNutritionDiary = MutableStateFlow<Map<String, UserNutritionDiary>>(emptyMap())

    // Public read-only nutrition diary map exposed to the UI.
    val userNutritionDiary: StateFlow<Map<String, UserNutritionDiary>> = _userNutritionDiary.asStateFlow()

    // Holds search suggestions internally.
    private val _searchSuggestions = MutableStateFlow<List<FoodItem>>(emptyList())

    // Public read-only search suggestions exposed to the UI.
    val searchSuggestions: StateFlow<List<FoodItem>> = _searchSuggestions.asStateFlow()

    // Holds search loading state internally.
    private val _isSearching = MutableStateFlow(false)

    // Public read-only search loading state exposed to the UI.
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    /**
     * Adds a food item to the repository and syncs the nutrition diary for that date.
     */
    fun addFoodItem(item: FoodItem) {
        _foodItemState.value = FoodItemState.Loading // Show loading state in UI.
        viewModelScope.launch {
            try {
                foodItemRepository.addFoodItem(item) // Saves food item in data source.
                _foodItemState.value = FoodItemState.Success(item) // Show success state when item is added.
                val itemDate = item.dateAdded ?: LocalDate.now().toString() // Gets the item's date or defaults to today.
                updateNutritionDiaryForDate(itemDate) // Syncs the daily diary totals after adding.
            } catch (e: Exception) {

                _foodItemState.value =
                    FoodItemState.Error(e.message ?: "Add failed") // Show error state if save fails.
            }
        }
    }

    /**
     * Searches for food items by name using the repository.
     * - Updates search suggestions state with results.
     * - Sets loading state while searching.
     */
    fun searchFoodByName(query: String) {
        if (query.isBlank()) {
            _searchSuggestions.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                _isSearching.value = true
                val results = foodItemRepository.searchFoodByName(query)
                _searchSuggestions.value = results
            } catch (e: Exception) {
                _searchSuggestions.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    /**
     * Clears the current search suggestions.
     */
    fun clearSearchSuggestions() {
        _searchSuggestions.value = emptyList()
    }

    /**
     * Loads today's food items from the repository and calculates daily macro totals.
     */
    fun loadTodayFoodItems() {
        viewModelScope.launch {
            try {
                val items = foodItemRepository.getTodayFoodItems() // Fetches today's food items added from repository
                _todayItems.value = items

                // Calculates calories x quantity to represent the full total food item's calories consumed.
                _totalDailyCaloriesCount.value = items.sumOf { (it.calories * it.quantity).toInt() }

                // Calculates total protein based on each item's protein and quantity.
                _totalDailyProtein.value = items.sumOf { (it.protein * it.quantity).toInt() }

                // Calculates total carbs based on each item's carbs and quantity.
                _totalDailyCarbs.value = items.sumOf { (it.carbohydrate * it.quantity).toInt() }

                // Calculates total fat based on each item's fat and quantity.
                _totalDailyFat.value = items.sumOf { (it.fat * it.quantity).toInt() }


            } catch (e: Exception) {
                _foodItemState.value =
                    FoodItemState.Error(e.message ?: "Load failed") // Show error state if fetching fails
            }
        }
    }

    /**
     * Deletes food item from the repository and syncs the nutrition diary for that date.
     */
    fun deleteFoodItem(item: FoodItem) {

        _foodItemState.value = FoodItemState.Idle // Reset state before deletion.

        viewModelScope.launch {
            try {
                foodItemRepository.deleteFoodItem(item) // Removes the food item from the data.

                // Set the specific delete success state with the full item
                _foodItemState.value = FoodItemState.DeleteSuccess(item)
                val itemDate = item.dateAdded ?: LocalDate.now().toString() // Gets the item's date or defaults to today.
                updateNutritionDiaryForDate(itemDate) // Syncs the daily diary totals after deletion.
            } catch (e: Exception) {
                _foodItemState.value =
                    FoodItemState.Error(e.message ?: "Failed to delete") // Show error state if deletion fails.
            }
        }
    }

    /**
     * Loads the nutrition diary records for the past 7 days from the diary repository.
     */
    fun loadUserNutritionDiary() {
        viewModelScope.launch {
            try {
                val endDate = LocalDate.now().toString() // today's date
                val startDate = LocalDate.now().minusDays(6).toString() // Start date computed by minus 6 day from today's date (7-day range including today)

                // Gets predefined aggregated nutrition diaries from database.
                val diaries = nutritionDiaryRepository.getDiariesForDateRange(startDate, endDate)

                // Associate by date for constant-time lookup by the BarChart UI.
                _userNutritionDiary.value = diaries.associateBy { it.date }
            } catch (e: Exception) {
                _foodItemState.value = FoodItemState.Error(e.message ?: "Failed to load nutrition diary") // If error then update state with error message.
            }
        }
    }

    /**
     * Recalculates and saves the nutrition diary for a given date after any food item change.
     */
    private fun updateNutritionDiaryForDate(date: String) {
        viewModelScope.launch {
            try {
                // Fetch items for the date to calculate sums
                val items = foodItemRepository.getFoodItemsForDateRange(date, date)
                
                // Get existing diary to keep goal intact, or create new one with current goal
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

}

/**
 * Sealed class representing all food item UI states: Loading, Success, Error or Idle.
 */
sealed class FoodItemState {
    object Idle : FoodItemState()
    object Loading : FoodItemState()
    data class Success(val item: FoodItem) : FoodItemState()
    data class DeleteSuccess(val item: FoodItem) : FoodItemState()
    data class Error(val message: String) : FoodItemState()
}
