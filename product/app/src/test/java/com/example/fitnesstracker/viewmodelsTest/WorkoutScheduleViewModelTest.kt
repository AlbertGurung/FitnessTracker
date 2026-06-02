package com.example.fitnesstracker.viewmodelsTest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.fitnesstracker.data.workout.WorkoutSchedule
import com.example.fitnesstracker.repository.repositoryinterface.WorkoutScheduleRepository
import com.example.fitnesstracker.viewmodel.ScheduleState
import com.example.fitnesstracker.viewmodel.WorkoutScheduleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
 * TDD test class with JUnit tests for WorkoutScheduleViewModel.
 * - Uses mocks for WorkoutScheduleRepository to test the ViewModel's schedule
 *   management logic in isolation, specifically focusing on coroutine-based functions
 *   for saving, loading, and retrieving today's workout assignment.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutScheduleViewModelTest {

    // Allows instant LiveData/StateFlow updates during testing.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test dispatcher to run coroutines instantly for predictable test execution.
    private val testDispatcher = UnconfinedTestDispatcher()

    // Mock repository to avoid calling the actual Firebase database.
    @Mock
    private lateinit var repository: WorkoutScheduleRepository

    private lateinit var viewModel: WorkoutScheduleViewModel

    // Setup method to initialise mocks and ViewModel before each test.
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = WorkoutScheduleViewModel(repository)
    }

    // Teardown method to restore original dispatcher after each test.
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Test case for successfully loading the schedule from the mock repository.
     */
    @Test
    fun loadScheduleSuccess() = runTest {
        val schedule = WorkoutSchedule(workoutsPerWeek = 3, scheduledDays = mapOf("Monday" to 1))         // Create a sample schedule for the test.

        // Mock repository to return successful schedule.
        `when`(repository.getSchedule()).thenReturn(Result.success(schedule))

        viewModel.loadSchedule() // Calls the ViewModel function to load schedule.

        // Verifies the schedule StateFlow is updated with the expected schedule.
        assertEquals(schedule, viewModel.schedule.value)

        // Verifies the state returns to Idle after successful loading.
        assertEquals(ScheduleState.Idle, viewModel.state.value)

    }

    /**
     * Test case for handling errors when loading schedule fails.
     */
    @Test
    fun loadScheduleError() = runTest {

        // Mock repository to return failure with exception message.
        `when`(repository.getSchedule()).thenReturn(Result.failure(Exception("Load error")))

        viewModel.loadSchedule() // Calls the ViewModel function to load schedule.

        // Verifies the ViewModel sets the state to Error with the exception message.
        assertEquals(ScheduleState.Error("Load error"), viewModel.state.value)
    }

    /**
     * Test case for successfully saving a workout schedule.
     */
    @Test
    fun saveScheduleSuccess() = runTest {

        // Create sample schedule data for the test.
        val days = mapOf("Monday" to 1, "Wednesday" to 2) // Saving Monday to 1 linked to Workout 1 & Wednesday to 2 linked to Workout 2.
        val schedule = WorkoutSchedule(workoutsPerWeek = 2, scheduledDays = days)


        // Mock repository to return success for saving schedule.
        `when`(repository.saveSchedule(schedule)).thenReturn(Result.success(Unit))

        // Calls the ViewModel function to save schedule.
        viewModel.saveSchedule(2, days)

        // Verifies the schedule StateFlow is updated immediately with the saved schedule.
        assertEquals(schedule, viewModel.schedule.value)

        // Verifies the state is set to Success with confirmation message.
        assertEquals(ScheduleState.Success("Schedule has been saved"), viewModel.state.value)

        // Verifies the repository save function was called with correct schedule.
        verify(repository).saveSchedule(schedule)
    }


    /**
     * Test case for correctly identifying today's workout number based on the current day of week.
     */
    @Test
    fun getTodayWorkoutNumberReturnsCorrectNumber() = runTest {

        // Create a sample schedule with all days of the week assigned.
        val schedule = WorkoutSchedule(
            workoutsPerWeek = 2,
            scheduledDays = mapOf(
                "Monday" to 1,
                "Tuesday" to 2,
                "Wednesday" to 3,
                "Thursday" to 4,
                "Friday" to 5,
                "Saturday" to 6,
                "Sunday" to 7
            )
        )

        // Mock repository to return the schedule with all days.
        `when`(repository.getSchedule()).thenReturn(Result.success(schedule))

        viewModel.loadSchedule() // Load the schedule into the ViewModel.

        // Finds the current system day and looks up the expected workout number and returns full day name.
        val today = java.time.LocalDate.now().dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
        val expected = schedule.scheduledDays[today]

        // Verifies the schedule StateFlow is updated with the saved schedule.
        assertEquals(expected, viewModel.getTodayWorkoutNumber())
    }
}
