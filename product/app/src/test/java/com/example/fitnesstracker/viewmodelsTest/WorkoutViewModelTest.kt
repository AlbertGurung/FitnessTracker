package com.example.fitnesstracker.viewmodelsTest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.fitnesstracker.data.workout.Workout
import com.example.fitnesstracker.data.workout.WorkoutExercise
import com.example.fitnesstracker.repository.repositoryinterface.WorkoutRepository
import com.example.fitnesstracker.viewmodel.WorkoutState
import com.example.fitnesstracker.viewmodel.WorkoutViewModel
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
 * TDD test class with JUnit tests for WorkoutViewModel.
 * - Uses mocks for WorkoutRepository to test the ViewModel's workout management logic
 *   in isolation, specifically focusing on coroutine-based functions for creating,
 *   loading, modifying workouts and exercises.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModelTest {

    // Allows instant LiveData/StateFlow updates during testing.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test dispatcher to run coroutines instantly for predictable test execution.
    private val testDispatcher = UnconfinedTestDispatcher()

    // Mock repository to avoid calling the actual Firebase database.
    @Mock
    private lateinit var repository: WorkoutRepository

    private lateinit var viewModel: WorkoutViewModel

    // Setup method to initialise mocks and ViewModel before each test.
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = WorkoutViewModel(repository)
    }

    // Teardown method to restore original dispatcher after each test.
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Test case for successfully loading all workouts from the mock repository.
     */
    @Test
    fun loadAllWorkoutsSuccess(): Unit = runTest {
        val workouts = listOf(
            Workout(id = "1", workoutNumber = 1, name = "Push", exercises = emptyList()),
            Workout(id = "2", workoutNumber = 2, name = "Pull", exercises = emptyList())
        )
        `when`(repository.getAllWorkouts()).thenReturn(Result.success(workouts)) // Configures mock to return workouts when method called.

        viewModel.loadAllWorkouts()
        
        assertEquals(workouts, viewModel.workouts.value)  // Checks that workouts loaded with workoutStateflow value.
        assertEquals(WorkoutState.Idle, viewModel.state.value) // Checks for idle state.
    }

    /**
     * Test case for handling errors when loading workouts fails.
     */
    @Test
    fun loadAllWorkoutsError() = runTest {
        `when`(repository.getAllWorkouts()).thenReturn(Result.failure(Exception("Load failed")))// Mocks throwing error message.

        viewModel.loadAllWorkouts()
        
        assertEquals(WorkoutState.Error("Load failed"), viewModel.state.value) // Verify error state captured.
    }

    /**
     * Test case for successfully creating a new workout.
     */
    @Test
    fun createWorkoutSuccess() = runTest {
        val workout = Workout(workoutNumber = 1, name = "Workout 1", exercises = emptyList())

        `when`(repository.saveWorkout(workout)).thenReturn(Result.success("docId")) // Mocks save success.

        `when`(repository.getAllWorkouts()).thenReturn(Result.success(listOf(workout))) // Mocks refresh list.

        viewModel.createWorkout(1, "Workout 1")
        
        assertEquals(WorkoutState.Success("Workout created!"), viewModel.state.value) // Verifies success message with StateFlowValue.
        verify(repository).saveWorkout(workout) // Checks that save called on repo
        verify(repository).getAllWorkouts() // Checks for refresh call on repo to know viewmodel behaviour.
    }

    /**
     * Test case for successfully adding an exercise to an existing workout.
     */
    @Test
    fun addExerciseToWorkoutSuccess() = runTest {
        val exercise = WorkoutExercise(name = "Bench Press", sets = 3, reps = 10, exerciseLibraryId = "bench press")

        `when`(repository.addExerciseToWorkout(1, exercise)).thenReturn(Result.success(Unit))  // Mock add success

        `when`(repository.getWorkout(1)).thenReturn(Result.success(null))

        `when`(repository.getAllWorkouts()).thenReturn(Result.success(emptyList()))

        viewModel.addExerciseToWorkout(1, exercise)
        
        assertEquals(WorkoutState.Success("Exercise has been added!"), viewModel.state.value)
        verify(repository).addExerciseToWorkout(1, exercise)
    }

    /**
     * Test case for successfully removing an exercise from a workout.
     */
    @Test
    fun removeExerciseFromWorkoutSuccess() = runTest {

        val exerciseId = "exercise_123"

        `when`(repository.removeExerciseFromWorkout(1, exerciseId)).thenReturn(Result.success(Unit))

        `when`(repository.getWorkout(1)).thenReturn(Result.success(null))

        `when`(repository.getAllWorkouts()).thenReturn(Result.success(emptyList()))

        viewModel.removeExerciseFromWorkout(1, exerciseId)
        
        assertEquals(WorkoutState.Success("Exercise has been deleted!"), viewModel.state.value)
        verify(repository).removeExerciseFromWorkout(1, exerciseId)
    }

    /**
     *  Test case for successfully updating exercise weight with exercise library ID and progress tracking.
     */
    @Test
    fun updateExerciseWeightSuccess() = runTest {

        val exerciseId = "exercise123"
        val date = "2026-12-25"
        val exerciseLibraryId = "bench press" // Library ID for progress tracking.

        `when`(repository.updateExerciseWeight(1, exerciseId,exerciseLibraryId, 100.0, date)).thenReturn(Result.success(Unit))

        `when`(repository.getWorkout(1)).thenReturn(Result.success(null))

        `when`(repository.getAllWorkouts()).thenReturn(Result.success(emptyList()))

        viewModel.updateExerciseWeight(1, exerciseId,exerciseLibraryId, 100.0, date)

        assertEquals(WorkoutState.Success("Weight updated!"), viewModel.state.value)
        verify(repository).updateExerciseWeight(1, exerciseId,exerciseLibraryId, 100.0, date)
    }
}