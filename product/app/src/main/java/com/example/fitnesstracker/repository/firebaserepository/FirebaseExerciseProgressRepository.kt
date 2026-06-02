package com.example.fitnesstracker.repository.firebaserepository

import com.example.fitnesstracker.data.workout.ExerciseProgressEntry
import com.example.fitnesstracker.repository.repositoryinterface.ExerciseProgressRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


/**
 * Firebase implementation of ExerciseProgressRepository.
 * - Handles saving and querying exercise weight progress entries from Firestore.
 * - Stores entries in users/{uid}/exerciseProgress subcollection.
 */
class FirebaseExerciseProgressRepository(
    private val auth: FirebaseAuth, // Firebase authentication for user ID.
    private val firestore: FirebaseFirestore  // Firestore database instance.
) : ExerciseProgressRepository {

    private fun userId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")  // Throws if no user logged in.
    private fun progressPath() = // For Path to user's progress subcollection called exerciseProgress saved in database.
        firestore.collection("users").document(userId()).collection("exerciseProgress")

    /**
     * Saves a new weight progress entry to Firestore.
     * - Generates document ID if not given.
     */
    override suspend fun addProgressEntry(entry: ExerciseProgressEntry): Result<Unit> {

        return try {
            val doc = if (entry.id.isBlank()) progressPath().document() else progressPath().document(entry.id) // Creates new doc or uses existing ID.

            val payload = if (entry.id.isBlank()) entry.copy(id = doc.id) else entry // Sets entry ID to match document ID.

            doc.set(payload).await() // Saves entry to Firestore.
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches all progress entries for a specific exercise, sorted by date.
     */
    override suspend fun getProgressForExercise(exerciseLibraryId: String): Result<List<ExerciseProgressEntry>> {
        return try {

            val docs = progressPath()
                .whereEqualTo("exerciseLibraryId", exerciseLibraryId) // Filters by exercise library ID.
                .get()
                .await()  // Fetches the matching documents.

            val list = docs.documents.mapNotNull { d ->
                d.toObject(ExerciseProgressEntry::class.java)?.copy(id = d.id) // Maps documents to data class.
            }.sortedBy { it.date }  // Sorts by date ascending (yyyy-MM-dd).

            Result.success(list)// Returns success result with sorted list of progress entries.
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}