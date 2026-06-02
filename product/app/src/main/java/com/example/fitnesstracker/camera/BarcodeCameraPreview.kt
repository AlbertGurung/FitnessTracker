package com.example.fitnesstracker.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner


/**
 * Camera preview used to scan barcodes.
 * - Displays live camera on screen using CameraX.
 * - Uses image analysis to process camera frames.
 * - Detects barcodes using ML Kit.
 * - Calls a callback when a new barcode is found.
 */
@Composable
fun BarcodeCameraPreview(
    modifier: Modifier = Modifier,
    onBarcodeDetected: (String) -> Unit
) {
    // Gets lifecycle so camera stops/starts with the screen
    val lifecycleOwner = LocalLifecycleOwner.current

    // Keeps last barcode to avoid repeating the same scan
    var lastBarcode by remember { mutableStateOf<String?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val previewView = PreviewView(context) // Camera preview UI

            // Uses Guava's ListenableFuture.
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context).get()

            val cameraPreview = Preview.Builder()  // Builder() sets up the camera preview use
                .build()
                .also { it.surfaceProvider = previewView.surfaceProvider } // Connects camera to UI preview

            // Sets up image analysis for processing camera frames
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // Runs barcode scanning on each frame
            imageAnalysis.setAnalyzer(
                ContextCompat.getMainExecutor(context),
                MlKitBarcodeAnalyzer { barcode ->
                    if (barcode != lastBarcode) {
                        lastBarcode = barcode
                        onBarcodeDetected(barcode)
                    }
                }
            )

            cameraProviderFuture.unbindAll() // Clear any previous camera setup

            // Start the camera, show the preview on screen and run barcode scanning whilst
            cameraProviderFuture.bindToLifecycle( // Access controller where camera connected based on screen lifecycle.
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA, // Uses back camera.
                cameraPreview, //Shows live camera on screen,
                imageAnalysis // Processing of frames from camera which is send to MLKItBarcodeAnalyzer.
            )
            previewView
        }
    )

}