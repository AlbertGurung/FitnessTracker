package com.example.fitnesstracker.repository.firebaserepository

import com.example.fitnesstracker.data.user.WeightEntry
import com.example.fitnesstracker.repository.repositoryinterface.WeightTrackerRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.time.LocalDate


/**
 * Firebase implementation of WeightRepository to save and load WeightEntry objects in Firestore.
 * - Weight entries stored in users/{uid}/weightEntries/{entryId} where firestore provides the unique id.
 */
class FirebaseWeightRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : WeightTrackerRepository {


    /**
     * Returns today's date as a string in yyyy-mm-dd format.
     */
    private fun todayDateString() = LocalDate.now().toString()

    /**
     * Returns the Firestore collection path for the current user's weight entries.
     * - Throws exception if no user is logged in.
     */
    private fun currentWeightEntriesPath() =
        auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).collection("weightEntries")
        } ?: throw IllegalStateException("User not logged in")

    /**
     * Adds a new weight entry for the current user.
     * - Automatically assigns today's date if not given.
     */
    override suspend fun addWeightEntry(entry: WeightEntry) {
        val entryWithDate = entry.copy(dateAdded = todayDateString())
        val path = currentWeightEntriesPath().document() // auto-generated ID
        path.set(entryWithDate).await()
    }

    /**
     * Gets all weight entries for the current user which is ordered by date descending.
     */
    override suspend fun getWeightEntries(): List<WeightEntry> {
        val snapshot = currentWeightEntriesPath()
            .orderBy("dateAdded", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(WeightEntry::class.java)?.copy(id = doc.id) // Adds the initial null id with the Firestore uid.
        }
    }

    /**
     * Deletes a specific weight entry from Firestore.
     */
    override suspend fun deleteWeightEntry(entry: WeightEntry) {
        val entryId = entry.id ?: throw IllegalArgumentException("WeightEntry needs the ID to be deleted")
        currentWeightEntriesPath()
            .document(entryId)
            .delete()
            .await()
    }
}
