package com.example.fitnesstracker.camera

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

/**
 * Analyzer that scans camera frames for barcodes using ML Kit.
 * - Receives frames from CameraX image analysis.
 * - Converts frames into format ML Kit can read and scans for barcodes to return the result.
 */
class MlKitBarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    // ML Kit barcode scanner instance
    private val barcodeScanner = BarcodeScanning.getClient()

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {

        // Gets the image from the camera frame
        val mediaImage = imageProxy.image

        // If image is null, stops processing the frame
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        // Converts camera image into ML Kit format
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees // fixes image rotation
        )

        // Runs barcode scanning on the image
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                // Gets the first detected barcode if there is one.
                val raw = barcodes.firstOrNull()?.rawValue

                // Calls callback if valid barcode is found
                if (!raw.isNullOrBlank()) onBarcodeDetected(raw)
            }
            .addOnCompleteListener {
                // Close the image to free resources
                imageProxy.close()
            }
    }
}