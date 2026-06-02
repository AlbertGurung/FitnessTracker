package com.example.fitnesstracker.viewmodel.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstracker.repository.repositoryinterface.AuthRepository
import com.example.fitnesstracker.viewmodel.AuthViewModel

/**
 * Factory class to create instances of AuthViewModel.
 * - Needed because AuthViewModel has a constructor parameter which is AuthRepository.
 * - ViewModelProvider cannot automatically create ViewModels with these parameter so the factory tells Android how to construct AuthViewModel correctly.
 */
class AuthViewModelFactory(
    private val repository: AuthRepository // Repository passed into AuthViewModel
) : ViewModelProvider.Factory {


    // View model provider calls to create ViewModel instance
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) { // Checks if requested ViewModel is AuthViewModel.
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T // Creates AuthViewModel with the AuthRepository
        }
        throw IllegalArgumentException("Unknown ViewModel class") // if different View Model requested then throws an error.
    }
}