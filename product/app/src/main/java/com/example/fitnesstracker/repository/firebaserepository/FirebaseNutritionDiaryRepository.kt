package com.example.fitnesstracker.repository.firebaserepository

import com.example.fitnesstracker.data.food.UserNutritionDiary
import com.example.fitnesstracker.repository.repositoryinterface.NutritionDiaryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * FirebaseNutritionDiaryRepository handles saving and getting nutrition diary data using Firebase Firestore.
 * - Uses the Firebase Authentication UID to store each user's diary entries under their own account in the database.
 */
class FirebaseNutritionDiaryRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : NutritionDiaryRepository {

    /**
     * Function to get the Firestore collection reference for nutrition diaries.
     */
    private fun diaryCollectionPath() =
        auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).collection("nutritionDiaries") // Defines path to the nutritionDiaries collection in user document.
        } ?: throw IllegalStateException("User not logged in")

    /**
     *  Saves or updates function for a daily nutrition diary document in Firestore.
     */
    override suspend fun saveDiary(diary: UserNutritionDiary) {
        val documentId = diary.date // Uses the date as the document ID YYYY-MM-DD.
        val diaryWithId = diary.copy(id = documentId) // Sets ID field as document ID.
        diaryCollectionPath().document(documentId).set(diaryWithId).await() // Overwrites existing diary document with latest calculated totals and goals.
    }

    /**
     * Gets a single nutrition diary record by date.
     */
    override suspend fun getDiary(date: String): UserNutritionDiary? {
        val snapshot = diaryCollectionPath().document(date).get().await() // Fetches document by date from nutritionDiaries collection.
        return snapshot.toObject(UserNutritionDiary::class.java) // Returns diary data class or null if not found.
    }

    /**
     * Gets a list of nutrition diary records for a specific date range.
     */
    override suspend fun getDiariesForDateRange(startDate: String, endDate: String): List<UserNutritionDiary> {
        val snapshot = diaryCollectionPath()
            .whereGreaterThanOrEqualTo("date", startDate) // Range start: 6 days ago.
            .whereLessThanOrEqualTo("date", endDate) // Range end: today.
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                doc.toObject(UserNutritionDiary::class.java)?.copy(id = doc.id)
            } catch (e: Exception) {
                null // Ignores this document if its data is incorrect to prevent the app from crashing(e.g., String instead of Number).
            }
        }
    }
}
