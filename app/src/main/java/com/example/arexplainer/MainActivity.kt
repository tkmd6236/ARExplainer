package com.example.arexplainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppScreen()
                }
            }
        }
    }
}

@Composable
fun AppScreen() {
    var selectedModel by remember { mutableStateOf("Gemma 2B") }
    var explanationText by remember { mutableStateOf("Tap a word to explain it...") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Toggle for Model Selection
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Model: $selectedModel", style = MaterialTheme.typography.titleMedium)
            Button(onClick = { 
                selectedModel = if (selectedModel == "Gemma 2B") "Gemma 4 E4B" else "Gemma 2B" 
            }) {
                Text("Switch Model")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Camera Preview and AR Overlay component
        var detectedTextBlocks by remember { mutableStateOf<List<com.google.mlkit.vision.text.Text.TextBlock>>(emptyList()) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            CameraView(
                onTextRecognized = { text ->
                    detectedTextBlocks = text.textBlocks
                }
            )
            
            // Overlay Canvas for drawing bounding boxes and intercepting taps
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .androidx.compose.foundation.gestures.pointerInput(detectedTextBlocks) {
                        androidx.compose.foundation.gestures.detectTapGestures { offset ->
                            // Simplified hit testing logic
                            val tappedBlock = detectedTextBlocks.find { block ->
                                val rect = block.boundingBox
                                rect != null && 
                                // (Note: Proper implementation requires mapping image coordinates to screen coordinates)
                                offset.x >= rect.left && offset.x <= rect.right &&
                                offset.y >= rect.top && offset.y <= rect.bottom
                            }
                            
                            if (tappedBlock != null) {
                                explanationText = "Explaining: ${tappedBlock.text}...\n(Inference via $selectedModel ongoing)"
                                // Coroutine to call: llmInferenceManager.generateExplanation(tappedBlock.text)
                            }
                        }
                    }
            ) {
                // Draw bounding boxes around detected text
                detectedTextBlocks.forEach { block ->
                    val rect = block.boundingBox
                    if (rect != null) {
                        drawRect(
                            color = androidx.compose.ui.graphics.Color.Yellow.copy(alpha = 0.3f),
                            topLeft = androidx.compose.ui.geometry.Offset(rect.left.toFloat(), rect.top.toFloat()),
                            size = androidx.compose.ui.geometry.Size(rect.width().toFloat(), rect.height().toFloat())
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Explanation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Explanation", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = explanationText, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
