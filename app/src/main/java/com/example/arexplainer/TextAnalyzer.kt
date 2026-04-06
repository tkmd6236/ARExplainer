package com.example.arexplainer

import android.media.Image
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions

class TextAnalyzer(
    private val onTextRecognized: (Text) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage: Image? = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            recognizer.process(image)
                .addOnSuccessListener { text ->
                    onTextRecognized(text)
                }
                .addOnFailureListener { e ->
                    // Handle OCR failure if necessary
                }
                .addOnCompleteListener {
                    // Must call close to free up resources for the next frame
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
