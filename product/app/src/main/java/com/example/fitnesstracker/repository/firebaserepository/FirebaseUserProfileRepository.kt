package com.example.fitnesstracker.repository.firebaserepository

import com.example.fitnesstracker.data.user.UserProfile
import com.example.fitnesstracker.repository.repositoryinterface.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * FirebaseUserProfileRepository handles saving and getting user profiles using Firebase Firestore.
 * - Uses the Firebase authentication unique UID that Firebase Auth. provide for new user sign up and uses that same unique ID to specify each user in database.
 */
class FirebaseUserProfileRepository( // Inject FirebaseAuth and FirebaseFirestore instances.
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserProfileRepository {

    /**
    * Saves a user profile to Firestore database under the current user's ID in Authentication.
    * Creates a "users" collection if it doesn't exist and adds a document with the user's UID.
    */
    override suspend fun saveUserProfile(profile: UserProfile) {   // Overrides normal functions from the UserProfileRepository interface to create own implementation.
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in") // Saves profile with current user ID from firebase.auth else throws exception.
        firestore.collection("users").document(userId).set(profile).await() // await(): Waits for Firestore to finish saving without blocking the app.
    }

    /**
     * Get the current user's profile from Firestore.
     */
    override suspend fun getUserProfile(): UserProfile? {
        val userId = auth.currentUser?.uid ?: return null // Returns null if no user logged in.
        val snapshot = firestore.collection("users").document(userId).get().await() // Get user document which contains profile details from Firestore.
        return snapshot.toObject(UserProfile::class.java) // Convert Firestore document into a UserProfile object by mapping fields like `name`, `age`... to its variables defined for user profile: UserProfile.name,UserProfile.age.

    }
}