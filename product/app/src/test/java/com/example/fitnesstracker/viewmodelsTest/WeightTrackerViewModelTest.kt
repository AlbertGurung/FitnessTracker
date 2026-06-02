package com.example.fitnesstracker.viewmodelsTest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.fitnesstracker.data.user.WeightEntry
import com.example.fitnesstracker.repository.repositoryinterface.WeightTrackerRepository
import com.example.fitnesstracker.viewmodel.WeightState
import com.example.fitnesstracker.viewmodel.WeightTrackerViewModel
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
 * TDD test class with JUnit tests for WeightTrackerViewModel.
 * - Uses Mock for the WeightRepository to test ViewModel logic in isolation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WeightTrackerViewModelTest {

    // Allows instant LiveData/StateFlow updates.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test dispatcher to run coroutines instantly.
    private val testDispatcher = UnconfinedTestDispatcher()

    // Mock repository to avoid real database calls.
    @Mock
    private lateinit var repository: WeightTrackerRepository

    private lateinit var viewModel: WeightTrackerViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = WeightTrackerViewModel(repository)
    }

    // Setup method to initialise mocks and ViewModel before each test.
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Test successful loading of weight entries.
     */
    @Test
    fun loadWeightEntriesSuccess() = runTest {
        val mockEntries = listOf(
            WeightEntry(id = "1", weight = 73.0, dateAdded = "2023-10-01"),
            WeightEntry(id = "2", weight = 62.5, dateAdded = "2023-10-02")
        )

        // Mock repository returns list.
        `when`(repository.getWeightEntries()).thenReturn(mockEntries)

        // Call ViewModel
        viewModel.loadWeightEntries()
        advanceUntilIdle()

        // Verify correct state and entries list
        assertEquals(WeightState.Success, viewModel.weightState.value)
        assertEquals(mockEntries, viewModel.weightEntries.value)
        verify(repository).getWeightEntries()
    }

    /**
     * Test adding a weight entry successfully.
     */
    @Test
    fun addWeightSuccess() = runTest {
        val testWeight = 71.0
        val mockEntries = listOf(WeightEntry(weight = testWeight))

        // Upon reload, return the new mock list.
        `when`(repository.getWeightEntries()).thenReturn(mockEntries)

        // Call add function.
        viewModel.addWeight(testWeight)
        advanceUntilIdle()

        // Verify state resolves to Success (via loadWeightEntries).
        assertEquals(WeightState.Success, viewModel.weightState.value)
        assertEquals(mockEntries, viewModel.weightEntries.value)
    }

    /**
     * Test successfully deleting a weight entry.
     */
    @Test
    fun deleteWeightSuccess() = runTest {
        val entryToDelete = WeightEntry(id = "1", weight = 70.0)

        // After deletion, the repository should return an empty list when reloading.
        `when`(repository.getWeightEntries()).thenReturn(emptyList())

        // Call ViewModel
        viewModel.deleteWeight(entryToDelete)
        advanceUntilIdle()

        // Verify the repository delete function was called.
        verify(repository).deleteWeightEntry(entryToDelete)

        // Verify state is Success and the list is now empty.
        assertEquals(WeightState.Success, viewModel.weightState.value)
        assertEquals(emptyList<WeightEntry>(), viewModel.weightEntries.value)
    }

    /**
     * Test error handling when deleting a weight entry fails.
     */
    @Test
    fun deleteWeightError() = runTest {
        val entryToDelete = WeightEntry(id = "1", weight = 50.0)

        // Force the repository mock to throw an error
        `when`(repository.deleteWeightEntry(entryToDelete)).thenThrow(RuntimeException("Delete failed"))

        viewModel.deleteWeight(entryToDelete)
        advanceUntilIdle()

        // Verify the ViewModel caught the error and updated the state safely.
        assertEquals(WeightState.Error("Delete failed"), viewModel.weightState.value)
    }


}
