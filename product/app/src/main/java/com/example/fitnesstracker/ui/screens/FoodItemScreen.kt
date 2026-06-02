package com.example.fitnesstracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fitnesstracker.data.food.FoodItem
import com.example.fitnesstracker.navigation.Routes
import com.example.fitnesstracker.viewmodel.FoodItemState
import com.example.fitnesstracker.viewmodel.FoodItemViewModel
import com.example.fitnesstracker.viewmodel.viewmodelfactory.FoodItemViewModelFactory

/**
 * Food Item Screen lets the user add a new food item with nutritional values.
 * - Shows fields for name, calories, protein, carbohydrate and fat.
 * - Includes a search button to search for food items by name using Open Food Facts API.
 * - Displays top 3 search suggestions that populate the form when selected.
 * - Validates that all fields are filled and have the correct data type
 * - Displays a single toast message if fields are empty or incorrectly formatted.
 * - After checks done, sends data to ViewModel which saves it to Firebase.
 * - Navigates back to the main screen after add item triggered.
 */
@Composable
fun FoodItemScreen(
    navController: NavController,
    factory: FoodItemViewModelFactory

) {
    // Get current Android context for showing toast messages.
    val context = LocalContext.current

    val viewModel: FoodItemViewModel = viewModel(factory = factory)

    // Observes addState from ViewModel to handle Success, Loading and Error states.
    val addState by viewModel.foodItemState.collectAsState()

    // Observes search suggestions and search loading state.
    val searchSuggestions by viewModel.searchSuggestions.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    // State variables for storing user input.
    // rememberSaveable preserves values across recompositions.
    var name by rememberSaveable { mutableStateOf("") }
    var calories by rememberSaveable { mutableStateOf("") }
    var protein by rememberSaveable { mutableStateOf("") }
    var carbohydrate by rememberSaveable { mutableStateOf("") }
    var fat by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("1") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showSearch by rememberSaveable { mutableStateOf(false) }


    // Navigates back to Main Screen ONLY when add FoodItem state succeeds.
    LaunchedEffect(addState) {
        if (addState is FoodItemState.Success) {
            navController.navigate(Routes.MAIN_SCREEN) {
                popUpTo(Routes.FOOD_ITEM_SCREEN) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(50.dp))
        Text("Add Food Item", fontSize = 18.sp)
        Spacer(Modifier.height(16.dp))

        // Search button to toggle search functionality
        Button(
            onClick = { showSearch = !showSearch },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(if (showSearch) "Hide Search" else "Search Food Database")
        }

        Spacer(Modifier.height(8.dp))

        // Search section - visible when showSearch is true
        if (showSearch) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    // Search when user types
                    if (it.isNotBlank()) {
                        viewModel.searchFoodByName(it)
                    } else {
                        viewModel.clearSearchSuggestions()
                    }
                },
                label = { Text("Search for food...") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { viewModel.searchFoodByName(searchQuery) }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                }
            )

            Spacer(Modifier.height(8.dp))

            // Display search suggestions
            if (searchSuggestions.isNotEmpty()) {
                Text(
                    "Top suggestions (click to select):",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))

                searchSuggestions.forEach { suggestion ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                // Populate form with selected suggestion
                                name = suggestion.name
                                calories = suggestion.calories.toString()
                                protein = suggestion.protein.toString()
                                carbohydrate = suggestion.carbohydrate.toString()
                                fat = suggestion.fat.toString()
                                quantity = "1"
                                // Clear search after selection
                                viewModel.clearSearchSuggestions()
                                searchQuery = ""
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE0E0E0)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = suggestion.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${suggestion.calories} kcal per 100g | " +
                                        "P:${suggestion.protein}g C:${suggestion.carbohydrate}g F:${suggestion.fat}g",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else if (!isSearching && searchQuery.isNotBlank()) {
                Text(
                    "No results found. Try a different search term.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))
        }

        // Name input field.
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        // Calories input field.
            OutlinedTextField(
                value = calories,
                onValueChange = { calories = it },
                label = { Text("Calories (kcal)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // Protein input field.
            OutlinedTextField(
                value = protein,
                onValueChange = { protein = it },
                label = { Text("Protein (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // Carbohydrate input field.
            OutlinedTextField(
                value = carbohydrate,
                onValueChange = { carbohydrate = it },
                label = { Text("Carbohydrate (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // Fat input field.
            OutlinedTextField(
                value = fat,
                onValueChange = { fat = it },
                label = { Text("Fat (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // Quantity/Servings input field.
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity/Servings") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // Checks for FoodItem State error and if so to show error message.
            if (addState is FoodItemState.Error) {
                Text(
                    text = (addState as FoodItemState.Error).message
                )
            }

            // Button to add food item.
            Button(
                onClick = {

                    // Convert numeric fields or it will be null if input is invalid.
                    val c = calories.toIntOrNull()
                    val p = protein.toIntOrNull()
                    val carb = carbohydrate.toIntOrNull()
                    val f = fat.toIntOrNull()
                    val q = quantity.toDoubleOrNull()

                    // Validate fields before sending to ViewModel.
                    if (name.isNotBlank() && name.toIntOrNull() == null && c != null && p != null && carb != null && f != null && q != null && q > 0) {
                        // All fields valid, create FoodItem object
                        val foodItem = FoodItem(name = name, calories = c, protein = p, carbohydrate = carb, fat = f, quantity = q)
                        viewModel.addFoodItem(foodItem)

                    } else {
                        // Generic toast for empty or incorrectly formatted fields.
                        Toast.makeText(
                            context,
                            "Fields cannot be empty, have incorrect format, or quantity must be greater than 0",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = addState !is FoodItemState.Loading // Disables button when loading state to avoid duplicate adds.
            ) {
                if (addState is FoodItemState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Add Food Item")
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth() // Cancel button to go back to previous screen.
            ) {
                    Text("Cancel")

            }
        }
    }
