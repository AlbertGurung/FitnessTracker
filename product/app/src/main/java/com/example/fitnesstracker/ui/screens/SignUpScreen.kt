package com.example.fitnesstracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fitnesstracker.R
import com.example.fitnesstracker.navigation.Routes
import com.example.fitnesstracker.viewmodel.AuthState
import com.example.fitnesstracker.viewmodel.AuthViewModel

/**
 * SignUp Screen allows the user to create a new account using Email, Password
 * - It checks that the passwords match before creating the user account.
 */
@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    navController: NavController, // Handles navigation between the screens.
    authViewModel: AuthViewModel // Looks at the authentication logic like login and show auth state.
) {
    // All remembered input states where they keep user input during UI recomposition
    var email by rememberSaveable {
        mutableStateOf("") }

    var password by rememberSaveable {
        mutableStateOf("") }

    var confirmPassword by rememberSaveable {
        mutableStateOf("") }

    var passwordError by remember {
        mutableStateOf("") }

    val authState = authViewModel.authState.observeAsState( )// Observes authentication state from the ViewModel
    val context = LocalContext.current


     // Observes auth state
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {   // Navigate to user profile screen if authenticated as first time user signs up, they would have no user profile.
                navController.navigate(Routes.USER_PROFILE_SCREEN)
            }

            is AuthState.Error -> {
                Toast.makeText(
                    context,(authState.value as AuthState.Error).message,Toast.LENGTH_SHORT // Show error message if signup fails
                ).show()
            }

            else -> Unit
        }
    }


    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Displays Fitcore App logo

        Image(
            painter = painterResource(id = R.drawable.fitcore),
            contentDescription = "FITCORE App Logo",
            modifier = Modifier.size(150.dp),
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "Sign Up", fontSize = 32.sp)

        Spacer(modifier = Modifier.height(16.dp))

        //Email Input Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Email") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        //Password Input Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = "" // Clear error when user types
            },
            label = { Text(text = "Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Confirm Password field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                passwordError = "" // Clear error when user types
            },
            label = { Text(text = "Confirm Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        // Shows visible error message if passwords do not match
        if (passwordError.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = passwordError,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Create Account Button
        Button(
            onClick = {
                // Checking if the passwords match and if not then throws error message as UI automatically updates.
                if (password != confirmPassword) {
                    passwordError = "The passwords do not match"
                } else {
                    authViewModel.signup(email, password)
                }
            },
            enabled = authState.value != AuthState.Loading
        ) {
            Text(text = "Create account")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation to Login Screen
        TextButton(onClick = {
            navController.navigate(Routes.LOGIN_SCREEN)
        }) {
            Text(text = "Already have an account? Login")
        }
    }
}
