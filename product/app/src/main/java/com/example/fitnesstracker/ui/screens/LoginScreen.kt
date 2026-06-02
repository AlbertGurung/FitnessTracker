package com.example.fitnesstracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fitnesstracker.R
import com.example.fitnesstracker.navigation.Routes
import com.example.fitnesstracker.viewmodel.AuthState
import com.example.fitnesstracker.viewmodel.AuthViewModel

/**
 * Login Screen allows the user to sign in with their email and password.
 * - Observes authentication state from the AuthViewModel.
 * - Navigates to the main screen if login is successful or shows error message if login failure.
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    // Holds the email input state and keeps it during recompositions like screen rotations etc.
    var email by rememberSaveable{
        mutableStateOf("")
    }
    // Holds the password input state and keeps it during recompositions.
    var password by rememberSaveable{
        mutableStateOf("")
    }
    // Observes authentication state from ViewModel.
    val authState = authViewModel.authState.observeAsState()

    // Gets the current Android context used to show Toast messages.
    val context = LocalContext.current

    // Checks for changes in auth. state.
    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Authenticated -> navController.navigate(Routes.MAIN_SCREEN) // Navigates to main screen if login success.
            is AuthState.Error -> Toast.makeText(context,
                (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show() // Show error message using Toast if login fails.
            else -> Unit
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Displays FitCore App logo

        Image(
            painter = painterResource(id = R.drawable.fitcore),
            contentDescription = "FitCore App Logo",
            modifier = Modifier.size(150.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Screen title name.
        Text(text = "Login", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Email field for login.
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text(text = "Email")
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password field hidden for login.
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it // When user is typing, updates password state.
            },
            label = {
                Text(text = "Password")
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            )

        )
        Spacer(modifier = Modifier.height(16.dp))

        // Login button with authentication request on click.
        Button(onClick = {
            authViewModel.login(email,password)
        },
            enabled = authState.value != AuthState.Loading // Disables the login button when authentication loading so that it stops multiple requests.
        ) {
            Text(text = "Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sign up button which navigates to sign up screen when clicked on.
        Button(onClick = {
            navController.navigate(Routes.SIGNUP_SCREEN)
        },

        ) {
            Text(text = "Don't have an account? Sign up")
        }
    }

}