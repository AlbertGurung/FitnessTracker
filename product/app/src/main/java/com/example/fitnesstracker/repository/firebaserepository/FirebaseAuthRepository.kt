package com.example.fitnesstracker.repository.firebaserepository

import com.example.fitnesstracker.repository.repositoryinterface.AuthRepository
import com.example.fitnesstracker.viewmodel.AuthState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

/**
 * FirebaseAuthRepository implements AuthRepository using Firebase Authentication.
 * - Handles all Firebase-specific logic.
 * - Provides results to ViewModel through AuthState callbacks.
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    // Log in user with email and password
    override fun login(email: String, password: String, onResult: (AuthState) -> Unit) {
        // Check for empty fields and return error
        if (email.isEmpty() || password.isEmpty()) {
            onResult(AuthState.Error("Fields cannot be left blank"))
            return
        }

        onResult(AuthState.Loading) // Login in progress

        // Login done through firebase authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(AuthState.Authenticated) // If login success then authorised state.
                } else {
                    val message = when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> "Incorrect Credentials." // If password or email is incorrect
                        else -> "Login failed. Try again." // Else exception message
                    }
                    onResult(AuthState.Error(message)) // Login failure
                }
            }
    }


    // Sign up a new user with email and password.
    override fun signup(
        email: String,
        password: String,
        onResult: (AuthState) -> Unit
    ) {
        // Check for empty fields and return error message.
        if (email.isEmpty() || password.isEmpty()) {
            onResult(AuthState.Error("Email or password can't be empty"))
            return
        }

        onResult(AuthState.Loading) // Sign up in progress

        // Signs up through firebase to create user.
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(AuthState.Authenticated) // If sign up is successful then authorised state set.
                } else {
                    val message = task.exception?.message ?: "Something went wrong"
                    onResult(AuthState.Error(message)) //Sign up failure
                }
            }
    }

    // Signs out current user and clears firebase session with auth.signOut operation so they are not authenticated.
    override fun signOut() {
        auth.signOut()
    }
    // Checking if user is logged in to either show user login screen or main screen.
    override fun isLoggedIn(): Boolean = auth.currentUser != null

    // Get current user's id from firebase so only specific user's queries are done.
    override fun getCurrentUserId(): String? = auth.currentUser?.uid
}