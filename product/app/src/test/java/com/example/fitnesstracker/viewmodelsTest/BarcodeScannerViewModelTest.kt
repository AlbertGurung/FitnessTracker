package com.example.fitnesstracker.viewmodelsTest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.fitnesstracker.data.food.FoodItem
import com.example.fitnesstracker.repository.repositoryinterface.FoodItemRepository
import com.example.fitnesstracker.repository.repositoryinterface.NutritionDiaryRepository
import com.example.fitnesstracker.repository.repositoryinterface.UserProfileRepository
import com.example.fitnesstracker.viewmodel.BarcodeScannerState
import com.example.fitnesstracker.viewmodel.BarcodeScannerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * TDD tests for BarcodeScannerViewModel.
 * - Uses a mocked FoodItemRepository testing viewmodel
 *   without calling real API.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BarcodeScannerViewModelTest {


    // Allows instant LiveData update or testing values.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test dispatcher run coroutines instantly.
    private val testDispatcher = UnconfinedTestDispatcher()

    // General mocks repository to avoid calling the actual database.
    @Mock
    private lateinit var foodItemRepository: FoodItemRepository

    @Mock
    private lateinit var userProfileRepository: UserProfileRepository

    @Mock
    private lateinit var nutritionDiaryRepository: NutritionDiaryRepository

    private lateinit var viewModel: BarcodeScannerViewModel // ViewModel which should be one being tested.

    private val item = FoodItem(
        name = "Apple",
        calories = 1,
        protein = 0,
        carbohydrate = 3,
        fat = 0
    )

    private val barcode = "123456789"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this) // Initializing mocks with @Mocks annotations.
        Dispatchers.setMain(testDispatcher) // Sets test dispatcher as main which is needed as ViewModel scope launches coroutines on Dispatchers.main by default.
        viewModel = BarcodeScannerViewModel(
            foodItemRepository,
            userProfileRepository,
            nutritionDiaryRepository
        ) // Initialised ViewModel with the mocked repository dependencies.
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Restores original main dispatcher after each test to avoid any issues with other coroutines.
    }


    /**
     * Test case for successful barcode fetch returning food data.
     */
    @Test
    fun fetchFoodItemByBarcodeSuccessAndReturnsFoodData() = runTest {

        `when`(foodItemRepository.getFoodDataByBarcode(barcode)).thenReturn(item)

        viewModel.fetchFoodDataByBarcode(barcode)
        advanceUntilIdle()

        assertEquals(BarcodeScannerState.Success(item), viewModel.scannerState.value)
        verify(foodItemRepository).getFoodDataByBarcode(barcode)
    }

    /**
     * Test case for invalid barcode input.
     */
    @Test
    fun fetchFoodItemByBarcodeWithInvalidAndBarcodeReturnsError() = runTest {

        viewModel.fetchFoodDataByBarcode("")
        advanceUntilIdle()

        assertTrue(viewModel.scannerState.value is BarcodeScannerState.Error)
        assertEquals(
            BarcodeScannerState.Error("Invalid barcode"),
            viewModel.scannerState.value
        )
    }

    /**
     * Test case for item not found returning error message.
     */
    @Test
    fun fetchFoodItemByBarcodeNotFoundAndReturnsError() = runTest {

        `when`(foodItemRepository.getFoodDataByBarcode(barcode))
            .thenThrow(RuntimeException("Food not found"))

        viewModel.fetchFoodDataByBarcode(barcode)
        advanceUntilIdle()

        assertEquals(
            BarcodeScannerState.Error("Food not found"),
            viewModel.scannerState.value
        )
    }

    /**
     * Test case for camera permission denied returning denied permission state.
     */
    @Test
    fun cameraPermissionDeniedState() = runTest {

        viewModel.getCameraPermission(false)
        advanceUntilIdle()

        assertEquals(
            BarcodeScannerState.DeniedCameraPermission,
            viewModel.scannerState.value
        )
    }


    /**
     * Test case for a granted camera permission returning granted permission state.
     */
    @Test
    fun cameraPermissionGrantedUpdate() = runTest {
        viewModel.getCameraPermission(isGranted = true)

        assertEquals(
            BarcodeScannerState.GrantedCameraPermission,
            viewModel.scannerState.value
        )
    }


    /**
     * Test case for successfully adding a scanned food item after a successful lookup.
     */
    @Test
    fun addScannedFoodAfterSuccessAndCallsRepository() = runTest {

        `when`(foodItemRepository.getFoodDataByBarcode(barcode)).thenReturn(item)

        // First fetch by barcode to populate Success state.
        viewModel.fetchFoodDataByBarcode(barcode)
        advanceUntilIdle()

        // Then add the scanned food.
        viewModel.addScannedFood()
        advanceUntilIdle()

        // Verify repository was called with the same item
        verify(foodItemRepository).addFoodItem(item)
        // State should remain Success with the same item
        assertEquals(BarcodeScannerState.Success(item), viewModel.scannerState.value)
    }

    /**
     * Test case for addScannedFood when there is no successful item such that repository should not be called.
     */
    @Test
    fun addScannedFoodWithoutSuccessDoesNothing() = runTest {

        // Initial state is Idle where calling addScannedFood should not crash or call repository.
        viewModel.addScannedFood()
        advanceUntilIdle()

        // State stays Idle.
        assertEquals(BarcodeScannerState.Idle, viewModel.scannerState.value)
    }


    /**
     * Test case for loading food item successfully with quantity multiplier.
     */
    @Test
    fun addScannedFoodWithCustomQuantitySavesCorrectItems() = runTest {

        // Mocks the camera with a scanned 1-quantity item
        val scannedItem = FoodItem(name = "Protein Bar", calories = 200, quantity = 1.0)
        val barcode = "123456789"


        `when`(foodItemRepository.getFoodDataByBarcode(barcode)).thenReturn(scannedItem)
        viewModel.fetchFoodDataByBarcode(barcode)
        advanceUntilIdle()

        // Mocking user selecting 1.5 bars to be added as quantity.
        val eatenQuantity = 1.5
        viewModel.addScannedFood(eatenQuantity)

        advanceUntilIdle()

        // Verifies it successfully saved 1.5 instead of 1.0.
        val expectedSavedItem = scannedItem.copy(quantity = 1.5)
        verify(foodItemRepository).addFoodItem(expectedSavedItem)
        }
}