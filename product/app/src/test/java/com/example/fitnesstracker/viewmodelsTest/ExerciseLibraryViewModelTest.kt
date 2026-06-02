package com.example.fitnesstracker.viewmodelsTest

import com.example.fitnesstracker.data.workout.ExerciseLibrary
import com.example.fitnesstracker.repository.repositoryinterface.ExerciseLibraryRepository
import com.example.fitnesstracker.viewmodel.ExerciseLibraryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * TDD test class with JUnit tests for ExerciseLibraryViewModel.
 * - Uses a fake ExerciseLibraryRepository to test the ViewModel's logic
 *   in isolation without calling the real Firebase database.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseLibraryViewModelTest {

    // Test dispatcher for coroutine control.
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var exerciseLibraryRepository: ExerciseLibraryRepository
    private lateinit var viewModel: ExerciseLibraryViewModel

    // Sample exercises returned by the fake mock repository.
    private val sampleExercises = listOf(
        ExerciseLibrary(id = "bench press", name = "Bench Press", muscleGroup = "Chest", createdByUser = true),
        ExerciseLibrary(id = "squat", name = "Squat", muscleGroup = "Legs", createdByUser = true)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher) // Sets the test dispatcher for coroutine execution.

        // Mock repository returning sample data.
        exerciseLibraryRepository = object : ExerciseLibraryRepository {
            override suspend fun getAllExercises() = Result.success(sampleExercises) // Returns all exercises in library.

            override suspend fun getExercisesByMuscleGroup(muscleGroup: String) =
                Result.success(sampleExercises.filter { it.muscleGroup == muscleGroup }) // Filters exercises by muscle group.

            override suspend fun addCustomExercise(name: String, muscleGroup: String) =
                Result.success(ExerciseLibrary(id = name.lowercase(), name = name, muscleGroup = muscleGroup, createdByUser = true)) // Creates new exercise with lowercase ID.
        }


        viewModel = ExerciseLibraryViewModel(exerciseLibraryRepository) // Initialises the ViewModel with the mocked repository.
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Restores the original main dispatcher after test completion.
    }

    /**
     * Test case for loading all exercises populating the exercises StateFlow.
     */
    @Test
    fun loadAllExercisesPopulatesExercisesState() = runTest {

        viewModel.loadAllExercises() // Triggers the loading operation.

        advanceUntilIdle() // Waits for all coroutines to complete execution.

        assertEquals(sampleExercises, viewModel.exercises.value) // Verifies the StateFlow is populated with expected data.
    }

    /**
     * Test case for adding custom exercise triggering list refresh from load.
     */
    @Test
    fun addCustomExerciseTriggersListRefresh() = runTest {

        viewModel.addCustomExercise("Bench Press", "Chest") // Triggers the add operation.

        advanceUntilIdle() // Waits for all coroutines to complete.

        assertEquals(sampleExercises, viewModel.exercises.value) // Verifies the list is refreshed.
    }

    /**
     * Test case for addCustomExercise callback returning correct library entry data.
     */
    @Test
    fun addCustomExerciseCallsOnSuccessWithCorrectLibraryEntry() = runTest {

        var returned: ExerciseLibrary? = null // saves the returned value (fallback).

        viewModel.addCustomExercise("Squat", "Legs") { returned = it } // Adds with callback listener.

        advanceUntilIdle() // Waits for all coroutines to complete.

        assertEquals("squat", returned?.id) // Verifies the ID is lowercase name.
        assertEquals("Squat", returned?.name) // Verifies the original name is preserved.
        assertEquals("Legs", returned?.muscleGroup) // Verifies the muscle group is stored correctly.
    }
}