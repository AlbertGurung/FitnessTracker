package com.example.fitnesstracker.viewmodelsTest

import com.example.fitnesstracker.data.workout.ExerciseProgressEntry
import com.example.fitnesstracker.repository.repositoryinterface.ExerciseProgressRepository
import com.example.fitnesstracker.viewmodel.ExerciseProgressViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * TDD test class with JUnit tests for ExerciseProgressViewModel.
 * - Uses a mock ExerciseProgressRepository to test the ViewModel's logic
 *   in isolation without calling the real Firebase database.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseProgressViewModelTest {

    // Test dispatcher for coroutine control.
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var exerciseProgressRepository: ExerciseProgressRepository
    private lateinit var viewModel: ExerciseProgressViewModel

    // Sample progress entries spanning multiple dates and workouts.
    private val benchPressEntries = listOf(
        ExerciseProgressEntry(id = "1", exerciseLibraryId = "bench press", weight = 80.0, date = "2026-03-01", workoutNumber = 1),
        ExerciseProgressEntry(id = "2", exerciseLibraryId = "bench press", weight = 85.0, date = "2026-03-05", workoutNumber = 2),
        ExerciseProgressEntry(id = "3", exerciseLibraryId = "bench press", weight = 90.0, date = "2026-03-07", workoutNumber = 1)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher) // Sets the test dispatcher for coroutine execution.

        // Mock repository returning entries for bench press, empty for others.
        exerciseProgressRepository = object : ExerciseProgressRepository {
            override suspend fun addProgressEntry(entry: ExerciseProgressEntry) = Result.success(Unit) // Simulates saving a progress entry.

            override suspend fun getProgressForExercise(exerciseLibraryId: String): Result<List<ExerciseProgressEntry>> {
                return if (exerciseLibraryId == "bench press") Result.success(benchPressEntries) // Returns entries for bench press.
                else Result.success(emptyList()) // Returns empty list for unknown exercises.
            }
        }

        viewModel = ExerciseProgressViewModel(exerciseProgressRepository) // Initialises the ViewModel with the fake repository.
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Restores the original main dispatcher after test completion.
    }

    /**
     * Test case for verifying initial state has no selection and empty entries.
     */
    @Test
    fun initialStateHasNoSelectedExerciseAndEmptyEntries() = runTest {
        assertNull(viewModel.selectedExerciseLibraryId.value) // Confirms no exercise is selected.
        assertEquals(emptyList<ExerciseProgressEntry>(), viewModel.progressEntries.value) // Confirms the list is empty.
    }

    /**
     * Test case for selecting exercise updating the selected ID StateFlow.
     */
    @Test
    fun selectExerciseSetsSelectedExerciseLibraryId() = runTest {
        viewModel.selectExercise("bench press") // Selects the exercise.

        advanceUntilIdle() // Waits for all coroutines to complete loading.

        assertEquals("bench press", viewModel.selectedExerciseLibraryId.value) // Verifies the selection is stored.
    }

    /**
     * Test case for progress entries loading sorted chronologically by date.
     */
    @Test
    fun selectExerciseLoadsProgressEntriesSortedByDate() = runTest {
        viewModel.selectExercise("bench press") // Selects and loads the exercise.

        advanceUntilIdle() // Waits for all coroutines to complete.

        val dates = viewModel.progressEntries.value.map { it.date } // Extracts the dates from entries.
        assertEquals(listOf("2026-03-01", "2026-03-05", "2026-03-07"), dates) // Verifies the chronological order.
    }

    /**
     * Test case for loading all entries including those from different workout numbers.
     */
    @Test
    fun selectExerciseLoadsAllEntriesIncludingEntriesFromDifferentWorkouts() = runTest {
        viewModel.selectExercise("bench press") // Selects the exercise.

        advanceUntilIdle() // Waits for all coroutines to complete.

        assertEquals(3, viewModel.progressEntries.value.size) // Verifies all 3 entries are loaded from workouts 1 and 2.
    }

    /**
     * Test case for selecting unknown exercise returning empty entries.
     */
    @Test
    fun selectingUnknownExerciseReturnsEmptyEntries() = runTest {
        viewModel.selectExercise("unknown exercise") // Selects a non-existent exercise.

        advanceUntilIdle() // Waits for all coroutines to complete.

        assertEquals(emptyList<ExerciseProgressEntry>(), viewModel.progressEntries.value) // Verifies empty result.
    }

    /**
     * Test case for selecting new exercise replacing previous entries in StateFlow.
     */
    @Test
    fun selectingNewExerciseReplacesPreviousEntries() = runTest {
        viewModel.selectExercise("bench press") // Makes first selection with data.

        advanceUntilIdle()

        assertEquals(3, viewModel.progressEntries.value.size) // Confirms 3 entries are present.

        viewModel.selectExercise("squat") // Makes second selection with no data.

        advanceUntilIdle()

        assertEquals(emptyList<ExerciseProgressEntry>(), viewModel.progressEntries.value) // Verifies replaced with empty.
    }
}