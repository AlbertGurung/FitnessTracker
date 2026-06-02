package com.example.fitnesstracker.repository.repositoryinterface

import com.example.fitnesstracker.viewmodel.AuthState

/**
* Interface for authentication operations.
* - Defines and declares the basic authentication related actions for login, signup, sign-out, and user info.
*/
interface AuthRepository {
    fun login(
        email: String,
        password: String,
        onResult: (AuthState) -> Unit
    )

    fun signup(
        email: String,
        password: String,
        onResult: (AuthState) -> Unit
    )

    fun signOut()

    fun isLoggedIn(): Boolean

    fun getCurrentUserId(): String?
}