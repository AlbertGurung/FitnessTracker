package com.example.fitnesstracker.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fitnesstracker.camera.BarcodeCameraPreview
import com.example.fitnesstracker.viewmodel.BarcodeScannerState
import com.example.fitnesstracker.viewmodel.BarcodeScannerViewModel
import com.example.fitnesstracker.viewmodel.viewmodelfactory.BarcodeScannerViewModelFactory


/**
 * Barcode Scanner Screen allows the user to scan food item barcodes using camera sensor.
 * - Requests camera permission if not already granted and handles permission being denied.
 * - Shows a live camera preview and detects barcodes in real-time.
 * - Manual barcode number entry available to fetch food information.
 * - Fetches food information for scanned barcode from Firestore via the repository.
 * - Displays scanned barcode and food details including name and calories.
 * - Allows the user to add the scanned food item to today’s tracker.
 */
@Composable
fun BarcodeScannerScreen(
    navController: NavController,
    factory: BarcodeScannerViewModelFactory
) {

    val viewModel: BarcodeScannerViewModel = viewModel(factory = factory)

    // Observe scanner state from ViewModel.
    val state by viewModel.scannerState.collectAsState()

    // Stores last scanned or entered barcode.
    var lastBarcode by remember { mutableStateOf<String?>(null) }

    // Stores for the manual barcode text entry.
    var manualBarcode by remember { mutableStateOf("") }

    // Quantity/Serving input for food item.
    var quantityInput by remember { mutableStateOf("1.0") }

    // Handles camera permission request result.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.getCameraPermission(granted) // Update permission state in ViewModel.
    }

    Spacer(Modifier.height(50.dp))


    // Request camera permission when screen loads.
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Scrollable column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {



        Spacer(Modifier.height(50.dp))


        // Camera and Permission handling.
        when (state) {

            // If permission denied then shows message and options.
            BarcodeScannerState.DeniedCameraPermission -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = "       Camera permission denied.\nPlease allow it in settings to scan barcodes.",
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Buttons for retrying permission or going back.
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(onClick = {
                                permissionLauncher.launch(Manifest.permission.CAMERA) // App should launch permission but Android OS may restrict until permission changed in setting.
                            }) {
                            Text("Try again")
                        }

                        Button(onClick = {
                                navController.popBackStack() // Navigate back to home screen.
                            }) {
                            Text("Back")
                        }
                    }
                }

            }
            else -> {

                // If permission granted then show camera preview.
                BarcodeCameraPreview(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),

                    // Triggered when barcode is detected.
                    onBarcodeDetected = { barcode ->
                        lastBarcode = barcode // Save last scanned barcode.
                        manualBarcode = barcode // Autofill the manual entry box when the camera detects a code

                        viewModel.fetchFoodDataByBarcode(barcode) // Fetch food data.
                    }
                )
            }
        }

        Spacer(Modifier.height(300.dp))


        // Display UI based on scanner state.
        when (val s = state) {
            BarcodeScannerState.GrantedCameraPermission -> Text("            POINT CAMERA AT THE BARCODE")

            BarcodeScannerState.Idle -> Text("Ready to scan")

            is BarcodeScannerState.Loading -> Text("Loading food info…")

            // Show food details when fetch is successful.
            is BarcodeScannerState.Success -> {
                Text("Food: ${s.foodItem.name} (${s.foodItem.calories} kcal per 100g, ${s.foodItem.protein}P, ${s.foodItem.carbohydrate}C, ${s.foodItem.fat}F )" )

                Spacer(Modifier.height(8.dp))


                // Quantity/Servings input field.
                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it },
                    label = { Text("Quantity / Servings") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // Add scanned food to tracker.
                Button(
                    onClick = {
                        // Convert string to double, fallback to 1.0 if they left it blank
                        val eatenQuantity = quantityInput.toDoubleOrNull() ?: 1.0
                        viewModel.addScannedFood(eatenQuantity)
                        navController.popBackStack() // navigates back to main screen after adding.
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add to today's tracker")
                }
            }
            // Show error message if fetch fails.
            is BarcodeScannerState.Error -> Text(s.message)

            else -> Unit
        }

        Spacer(Modifier.height(30.dp))


        // Manual barcode entry field.
        OutlinedTextField(
            value = manualBarcode,
            onValueChange = { manualBarcode = it }, // Updates input as user types.
            label = { Text("Enter Barcode Manually") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true // Restricts input to one line.
        )

        Spacer(Modifier.height(8.dp))

        // Button to fetch food data using manual input.
        Button(
            onClick = {
                lastBarcode = manualBarcode // Save entered barcode.
                viewModel.fetchFoodDataByBarcode(manualBarcode) // Fetch data.
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Fetch Food Information")
        }

        Spacer(Modifier.height(20.dp))

        // Display last scanned or entered barcode.
        lastBarcode?.let {
            Text(text = "Previous Barcode: $it")
            Spacer(Modifier.height(8.dp))
        }
    }
}