package com.example.fitnesstracker.repository.firebaserepository

import com.example.fitnesstracker.data.workout.ExerciseLibrary
import com.example.fitnesstracker.repository.repositoryinterface.ExerciseLibraryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Firebase implementation of ExerciseLibraryRepository.
 * - Handles reading and writing exercises to the exerciseLibrary subcollection in Firestore.
 */
class FirebaseExerciseLibraryRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ExerciseLibraryRepository {

    private fun userId(): String = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in") // Throws if no user is logged in.

    private fun libraryPath() =
        firestore.collection("users").document(userId()).collection("exerciseLibrary") // Path to user's exercise library subcollection.


    /**
     * Fetches all exercises from the library which is sorted alphabetically by name.
     */
    override suspend fun getAllExercises(): Result<List<ExerciseLibrary>> {
        return try {

            val docs = libraryPath().get().await() // Fetches all exercise documents from Firestore.
            val list = docs.documents.mapNotNull { d ->
                d.toObject(ExerciseLibrary::class.java) // Maps each Firestore document to ExerciseLibrary object.
            }.sortedBy { it.name.lowercase() } // Sorts alphabetically by lowercase name.

            Result.success(list) // Returns successful result with sorted exercise list.

        } catch (e: Exception) {
            Result.failure(e) // Catches exception to return failure result with exception.
        }
    }

    /**
     * Fetches exercises filtered by muscle group, sorted alphabetically by name.
     */
    override suspend fun getExercisesByMuscleGroup(muscleGroup: String): Result<List<ExerciseLibrary>> {
        return try {

            val docs = libraryPath()
                .whereEqualTo("muscleGroup", muscleGroup) // Filters documents by muscle group field.
                .get()
                .await()

            val list = docs.documents.mapNotNull { d ->
                d.toObject(ExerciseLibrary::class.java) // Maps each document to ExerciseLibrary object.
            }.sortedBy { it.name.lowercase() } // Sorts results alphabetically.

            Result.success(list) // Returns successful result with filtered exercises.

        } catch (e: Exception) {
            Result.failure(e) // Returns failure result with exception.
        }
    }

    /**
     * Adds a new custom exercise to the library.
     * - Checks for duplicates before creating.
     * - Uses lowercase exercise name as document ID.
     */
    override suspend fun addCustomExercise(name: String, muscleGroup: String): Result<ExerciseLibrary> {
        return try {
            val nameTrimmed = name.trim() // Removes leading and trailing whitespace from name.

            // Queries for existing exercise with matching name and muscle group.
            val existing = libraryPath()
                .whereEqualTo("name", nameTrimmed) // Checks for existing exercise with same name.
                .whereEqualTo("muscleGroup", muscleGroup) // Checks for same muscle group match.
                .get()
                .await()
                .documents
                .firstOrNull() // Returns first match or null if not found.

            // Returns existing exercise if duplicate found to stop new creation.
            if (existing != null) {
                val ex = existing.toObject(ExerciseLibrary::class.java)?.copy(id = existing.id)
                    ?: ExerciseLibrary(id = existing.id, name = nameTrimmed, muscleGroup = muscleGroup)
                return Result.success(ex) // Returns existing entry to avoid creating a duplicate.
            }

            val docId = nameTrimmed.lowercase() // Sets document ID as lowercase exercise name for consistent lookups.

            val doc = libraryPath().document(docId) // Gets reference to new document with that ID.

            val exercise = ExerciseLibrary( // Creates new ExerciseLibrary object with generated ID.
                id = docId,
                name = nameTrimmed,
                muscleGroup = muscleGroup,
                createdByUser = true // Flags this exercise as added by the user.
            )
            doc.set(exercise).await() // Saves new exercise document to Firestore.

            Result.success(exercise) // Returns successful result with created exercise.

        } catch (e: Exception) {
            Result.failure(e) // Returns failure result with exception.
        }
    }
}