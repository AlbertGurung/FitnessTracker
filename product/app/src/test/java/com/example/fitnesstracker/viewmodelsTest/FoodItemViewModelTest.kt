package com.example.fitnesstracker.viewmodelsTest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.fitnesstracker.data.food.FoodItem
import com.example.fitnesstracker.data.food.UserNutritionDiary
import com.example.fitnesstracker.repository.repositoryinterface.FoodItemRepository
import com.example.fitnesstracker.repository.repositoryinterface.NutritionDiaryRepository
import com.example.fitnesstracker.repository.repositoryinterface.UserProfileRepository
import com.example.fitnesstracker.viewmodel.FoodItemState
import com.example.fitnesstracker.viewmodel.FoodItemViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations.openMocks
import org.mockito.kotlin.any


/**
 * TDD test class with JUnit tests for FoodItemViewModel.
 * - Uses mocks for FoodItemRepository, UserProfileRepository and NutritionDiaryRepository
 *   to test the ViewModel's logic in isolation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FoodItemViewModelTest {

    // Allows instant LiveData update or testing values.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test dispatcher runs coroutines instantly.
    private val testDispatcher = UnconfinedTestDispatcher()

    // Mock repository to avoid calling the actual food item database.
    @Mock
    private lateinit var foodItemRepository: FoodItemRepository

    // Mock repository to avoid calling the actual user profile database.
    @Mock
    private lateinit var userProfileRepository: UserProfileRepository

    // Mock repository to avoid calling the actual nutrition diary database.
    @Mock
    private lateinit var nutritionDiaryRepository: NutritionDiaryRepository

    private lateinit var viewModel: FoodItemViewModel // ViewModel under test.

    @Before
    fun setup() {
        openMocks(this) // Initialising mocks with @Mock annotations.
        Dispatchers.setMain(testDispatcher)  // Sets test dispatcher as main which is needed as viewModelScope launches coroutines on Dispatchers.Main by default.
        viewModel =
            FoodItemViewModel( // Initialised viewmodel with all three mocked repository dependencies.
                foodItemRepository,
                userProfileRepository,
                nutritionDiaryRepository
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Restores original main dispatcher after each test to avoid issues with other coroutines.
    }

    /**
     * Test for adding a food item successfully verifying the state and background diary sync.
     */
    @Test
    fun addFoodItemAndSavesItemSuccess() = runTest {
        val item = FoodItem(
            name = "Apple",
            calories = 5,
            dateAdded = "2026-04-05"
        )

        // Mock nested calls used by the background diary sync in the ViewModel.
        `when`(foodItemRepository.getFoodItemsForDateRange(anyString(), anyString())).thenReturn(
            listOf(item)
        )

        viewModel.addFoodItem(item)
        advanceUntilIdle() // Wait for coroutines to finish.

        assertEquals(
            FoodItemState.Success(item),
            viewModel.foodItemState.value
        )   // Verifies success state is returned with the correct item.

        verify(foodItemRepository).addFoodItem(item) // Verifies the repository add FoodItem function was called.
        verify(nutritionDiaryRepository).saveDiary(any())  // Verifies diary sync was triggered.
    }

    /**
     * Test for a failed food item addition verifying the error state and message is thrown.
     */
    @Test
    fun addFoodItemAndSaveItemError() = runTest {
        val item = FoodItem(name = "Apple", calories = 95) // Food item created for test.

        `when`(foodItemRepository.addFoodItem(item)).thenThrow(RuntimeException("Add failed")) // Mocks repository to throw an error.

        viewModel.addFoodItem(item)
        advanceUntilIdle() // Wait for coroutines to finish.

        // Verifies error state was thrown with the correct message.
        assertEquals(FoodItemState.Error("Add failed"), viewModel.foodItemState.value)
    }

    /**
     * Test for loading today's food items verifying the state matches the returned items.
     */
    @Test
    fun loadTodayFoodItemSuccess() = runTest {
        val items = listOf(
            FoodItem(name = "Apple", calories = 95),
            FoodItem(name = "Banana", calories = 1)
        )

        `when`(foodItemRepository.getTodayFoodItems()).thenReturn(items) // Mocks repository to return the test items.

        viewModel.loadTodayFoodItems()
        advanceUntilIdle() // Wait for coroutines to finish.

        assertEquals(
            items,
            viewModel.todayItems.value
        ) // Verifies today's items state matches the mocked data.

        verify(foodItemRepository).getTodayFoodItems()
    }

    /**
     * Test for successful food item deletion verifying the state and diary sync.
     */
    @Test
    fun deleteFoodItemSuccess() = runTest {
        val deleteItem = FoodItem(
            name = "Apple",
            calories = 2,
            dateAdded = "2026-04-05"
        ) // Food item to be deleted.

        // Mocks nested calls used by the background diary sync in the ViewModel.
        `when`(foodItemRepository.getFoodItemsForDateRange(anyString(), anyString())).thenReturn(
            emptyList()
        )

        viewModel.deleteFoodItem(deleteItem)
        advanceUntilIdle() // Wait for coroutines to finish.

        // Verifies delete success state is thrown with the correct item.
        assertEquals(FoodItemState.DeleteSuccess(deleteItem), viewModel.foodItemState.value)
        verify(foodItemRepository).deleteFoodItem(deleteItem)

        verify(nutritionDiaryRepository).saveDiary(any()) // Verifies diary was updated after deletion.
    }

    /**
     * Test for loading nutrition diary verifying correct data is fetched from the diary repository.
     */
    @Test
    fun loadUserNutritionDiaryFetchesCorrectlyFromDiaryRepository() = runTest {

        // Actual Summary objects to be used for test.
        val summaries = listOf(
            UserNutritionDiary(date = "2026-04-01", totalCalories = 301, totalProtein = 46),
            UserNutritionDiary(date = "2026-04-02", totalCalories = 300, totalProtein = 6)
        )

        // Mocks diary repository to return the test summaries.
        `when`(nutritionDiaryRepository.getDiariesForDateRange(anyString(), anyString()))
            .thenReturn(summaries)

        viewModel.loadUserNutritionDiary()
        advanceUntilIdle() // Wait for coroutines to finish.

        val result = viewModel.userNutritionDiary.value

        // Verifies totals match the summaries provided by the repository.
        assertEquals(301,result["2026-04-01"]?.totalCalories) // Getting the date's data for total Calories to see if it matches.
        assertEquals(300, result["2026-04-02"]?.totalCalories) // Getting the date's data for total Calories to verify a match.

        verify(nutritionDiaryRepository).getDiariesForDateRange(anyString(), anyString()) // Verifies the correct repository nutritionDiary was called.
    }

    /**
     * Test for loading nutrition diary returning an empty map when no diary records exist.
     */
    @Test
    fun loadUserNutritionDiaryReturnsEmptyMapWhenNoItems() = runTest {

        // Mocks the nutritionDiary repository to return empty list when method called.
        `when`(nutritionDiaryRepository.getDiariesForDateRange(anyString(), anyString()))
            .thenReturn(emptyList())

        viewModel.loadUserNutritionDiary()
        advanceUntilIdle() // Wait for coroutines to finish.

        // Verifies the diary state is an empty map.
        assertEquals(emptyMap<String, UserNutritionDiary>(), viewModel.userNutritionDiary.value)
    }

    /**
     * Test for searching food by name successfully verifying _searchSuggestions updates with results
     * and clearSearchSuggestions resets the list to empty.
     */
    @Test
    fun searchFoodByNameSuccessAndClear() = runTest {

        val searchResults = listOf(
            FoodItem(name = "Apple", calories = 52, protein = 0, carbohydrate = 14, fat = 0),
            FoodItem(name = "Banana", calories = 89, protein = 1, carbohydrate = 23, fat = 0)
        )

        `when`(foodItemRepository.searchFoodByName("fruit")).thenReturn(searchResults) // Mocks repository to return search results.

        assertEquals(emptyList<FoodItem>(), viewModel.searchSuggestions.value) // Verifies suggestions are empty before search.

        assertEquals(false, viewModel.isSearching.value) // Verifies isSearching is false before search.

        // Calls search to populate suggestions.
        viewModel.searchFoodByName("fruit")
        advanceUntilIdle() // Wait for coroutines to finish.

        assertEquals(searchResults, viewModel.searchSuggestions.value) // Verifies suggestions are updated with the returned results.
        assertEquals(false, viewModel.isSearching.value) // Verifies isSearching resets to false after completion.

        verify(foodItemRepository).searchFoodByName("fruit") // Verifies the repository search function was called.

        // Calls clearSearchSuggestions and verifies suggestions are reset.
        viewModel.clearSearchSuggestions()
        assertEquals(emptyList<FoodItem>(), viewModel.searchSuggestions.value)
    }

    /**
     * Test for searching food by name with empty query verifying suggestions are cleared
     * without calling repository.
     */
    @Test
    fun searchFoodByNameEmptyQuery() = runTest {

        // Pre-populate with previous results.
        val chickenResults = listOf(FoodItem(name = "Chicken Breast", calories = 165))

        `when`(foodItemRepository.searchFoodByName("chicken")).thenReturn(chickenResults) // Mocks repository to return chicken search results.

        viewModel.searchFoodByName("chicken") // Pre-populates suggestions with a valid query.
        advanceUntilIdle() // Wait for coroutines to finish.

        // Now call with empty query.
        viewModel.searchFoodByName("")

        assertEquals(emptyList<FoodItem>(), viewModel.searchSuggestions.value) // Verifies suggestions are cleared when query is empty.

        verify(foodItemRepository).searchFoodByName("chicken") // Verifies searchFoodByName was only called once for "chicken" but not for empty string.
    }

    /**
     * Test for searching food by name with error verifying API exceptions result in
     * empty suggestions and isSearching resets to false.
     */
    @Test
    fun searchFoodByNameErrorHandling() = runTest {

        `when`(foodItemRepository.searchFoodByName("pizza"))
            .thenThrow(RuntimeException("Network error")) // Mocks repository to throw a network error.

        viewModel.searchFoodByName("pizza")

        advanceUntilIdle() // Wait for coroutines to finish.

        assertEquals(
            emptyList<FoodItem>(), viewModel.searchSuggestions.value) // Verifies suggestions are empty after error.

        assertEquals(false, viewModel.isSearching.value) // Verifies isSearching resets to false after error.
    }
}