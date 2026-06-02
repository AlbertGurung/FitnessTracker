package com.example.fitnesstracker.viewmodelsTest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.fitnesstracker.data.user.ActivityLevel
import com.example.fitnesstracker.data.user.Gender
import com.example.fitnesstracker.data.user.GoalType
import com.example.fitnesstracker.data.user.UserProfile
import com.example.fitnesstracker.repository.repositoryinterface.UserProfileRepository
import com.example.fitnesstracker.repository.repositoryinterface.NutritionDiaryRepository
import com.example.fitnesstracker.utils.CaloriesCalculator
import com.example.fitnesstracker.viewmodel.ProfileState
import com.example.fitnesstracker.viewmodel.UserProfileViewModel
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
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations


/**
 * TDD test class with JUnit tests for UserProfileViewModel.
 * - Uses mocks for the UserProfileRepository and CaloriesCalculator to test the ViewModel's
 * logic in isolation, specifically focusing on coroutine-based functions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileViewModelTest {

    // Allows instant LiveData update or testing values.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    //Test dispatcher run coroutines instantly.
    private val testDispatcher = UnconfinedTestDispatcher()


    // General mocks repository to avoid calling the actual database.
    @Mock
    private lateinit var repository: UserProfileRepository

    // Mock for calculator in case of an updated calculator which causes errors which the test code would have relied on even if viewmodel is correct.
    @Mock
    private lateinit var calculator: CaloriesCalculator

    @Mock
    private lateinit var nutritionDiaryRepository: NutritionDiaryRepository

    private lateinit var viewModel: UserProfileViewModel // Viewmodel userprofile which should be one being tested.

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this) // Initialising mocks with @Mocks annotations.
        Dispatchers.setMain(testDispatcher) // Sets test dispatcher as main which is needed as viewmodel scope launches coroutines on Dispatchers.main by default.
        viewModel = UserProfileViewModel(repository, calculator, nutritionDiaryRepository) // Initialised viewmodel with the mocked repository and calculator dependencies.
    }
    @After
    fun tearDown() {
        Dispatchers.resetMain() // Restores original main dispatcher after each test to avoid any issues with other coroutines.
    }

    /**
     * Test for saving profile functionality with mock repository.
     */
    @Test
    fun calculateAndSaveProfileSuccess() = runTest {
        // Set User profile created for test.
        val user = UserProfile(
            name = "Bob",
            weight = 70.0,
            height = 170.0,
            age = 25,
            gender = Gender.MALE,
            activityLevel = ActivityLevel.MODERATE,
            goal = GoalType.MAINTAIN_WEIGHT
        )
        
        // Defined calories which will be expected results after calculation.
        val expectedCalories = 2500
        val expectedUser = user.copy(calculatedCalories = expectedCalories)

        // Tells mock calculator to return set expected calories when called.
        `when`(calculator.calculateDailyCalories(user)).thenReturn(expectedCalories)

        // Calls the viewmodel function.
        viewModel.calculateAndSaveProfile(user)
        advanceUntilIdle() // Wait for coroutines to finish off.

        // Verifies the save functionality with the expected user in the mock repo.
        assertEquals(ProfileState.Success(expectedUser), viewModel.profileState.value)
        verify(repository).saveUserProfile(expectedUser)
    }

    /**
     * Test for a failed save functionality of user profile verifying with profile state.
     */
    @Test
    fun calculateAndSaveProfileFail() = runTest {
        // Creation of userprofile.
        val user = UserProfile(
            name = "Alice",
            weight = 70.0,
            height = 170.0,
            age = 25,
            gender = Gender.MALE,
            activityLevel = ActivityLevel.MODERATE,
            goal = GoalType.MAINTAIN_WEIGHT
        )
        val expectedCalories = 1500
        val expectedUser = user.copy(calculatedCalories = expectedCalories)

        // Mock dependencies to throw an error.
        `when`(calculator.calculateDailyCalories(user)).thenReturn(expectedCalories)
        `when`(repository.saveUserProfile(expectedUser)).thenThrow(RuntimeException("Save has failed"))

        // Calls the function
        viewModel.calculateAndSaveProfile(user)
        advanceUntilIdle()

        //Verifies the error state.
        assertEquals(ProfileState.Error("Save has failed"), viewModel.profileState.value)
    }
}