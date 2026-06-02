package com.example.fitnesstracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fitnesstracker.repository.repositoryinterface.AuthRepository


/**
 * ViewModel responsible for handling user authentication logic.
 *
 * - Observes authentication state changes from authState LiveData and interacts with
 *   AuthRepository to perform login, signup, sign-out, and check authentication status.
 * - Receives results from the repository and updates authState so the UI can react
 *   to loading, success or error states.
 */
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    // Holds authentication state internally
    private val _authState = MutableLiveData<AuthState>()
    // Exposed authentication state for observers

    val authState: LiveData<AuthState> = _authState // LiveData is lifecycle aware.

    init {
        checkAuthStatus() // Check current authentication status when ViewModel is created
    }

    // Check current authentication status when ViewModel is created

    fun checkAuthStatus() {
        _authState.value = if (repository.isLoggedIn()) {
            AuthState.Authenticated // When user is already logged in
        } else {
            AuthState.Unauthenticated // User is not logged in
        }
    }

    // Login through the repository and updates authState with Loading, Authenticated or Error
    fun login(email: String, password: String) {
        repository.login(email, password) { state ->
            _authState.postValue(state)
        }
    }

    // Signup through the repository and updates authState with Loading, Authenticated, or Error
    fun signup(email: String, password: String) {
        repository.signup(email, password) { state ->
            _authState.postValue(state)
        }
    }

    // Signs out current user and updates authState to unauthenticated.
    fun signOut() {
        repository.signOut() // Clears firebase session
        _authState.value = AuthState.Unauthenticated
    }
}

// Sealed class which represents all the authentication states for UI as it's needed because Firebase only provides success or failure callbacks.

sealed class AuthState {
    object Authenticated : AuthState() // User logged in
    object Unauthenticated : AuthState() // User logged out
    object Loading : AuthState() // Firebase operation in progress.
    data class Error(val message: String) : AuthState() // Error occurred through user authentication operation.
}