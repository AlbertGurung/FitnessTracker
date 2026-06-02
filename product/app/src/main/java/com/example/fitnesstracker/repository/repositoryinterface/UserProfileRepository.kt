package com.example.fitnesstracker.repository.repositoryinterface

import com.example.fitnesstracker.data.user.UserProfile

/**
 * Interface for defining user profile data access operations.
 * - Save and get user profile data.
 */
interface UserProfileRepository {
    suspend fun saveUserProfile(
        profile: UserProfile
    )
    suspend fun getUserProfile(): UserProfile?
}