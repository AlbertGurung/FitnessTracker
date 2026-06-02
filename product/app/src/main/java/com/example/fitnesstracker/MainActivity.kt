package com.example.fitnesstracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.fitnesstracker.navigation.MyAppNavigation
import com.example.fitnesstracker.repository.firebaserepository.FirebaseAuthRepository
import com.example.fitnesstracker.ui.theme.FitnessTrackerTheme
import com.example.fitnesstracker.viewmodel.AuthViewModel
import com.example.fitnesstracker.viewmodel.viewmodelfactory.AuthViewModelFactory

/**
 * MainActivity is the entry point of the app.
 * - Sets up the Jetpack Compose UI.
 * - Creates an AuthViewModel using the AuthViewModelFactory.
 * - Passes the AuthViewModel to the navigation component so all screens
 *   can observe authentication state.
 */
class MainActivity : ComponentActivity() {

    // Creates AuthViewModel using AuthViewModelFactory and passes FireBaseAuthRepository into the factory
    // which will allow AuthViewModel to handle user login, signup, sign-out and account info.
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(FirebaseAuthRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //Sets up Compose UI content
        setContent {
            FitnessTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyAppNavigation( // MyAppNavigation handles all screen navigation.
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel // Passes AuthViewModel so screens can react to authentication state.
                    )
                }
            }
        }
    }
}