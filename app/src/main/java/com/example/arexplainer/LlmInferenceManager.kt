package com.example.arexplainer

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LlmInferenceManager(private val context: Context) {
    private var llmInference: LlmInference? = null
    var isModelLoaded = false
        private set

    suspend fun loadModel(modelPath: String, is4B: Boolean) {
        withContext(Dispatchers.IO) {
            val file = File(modelPath)
            if (!file.exists()) {
                throw Exception("Model file not found at: $modelPath")
            }
            
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                // Adjust max tokens as needed
                .setMaxTokens(512)
                .build()
                
            llmInference = LlmInference.createFromOptions(context, options)
            isModelLoaded = true
        }
    }

    suspend fun generateExplanation(term: String): String {
        return withContext(Dispatchers.IO) {
            if (!isModelLoaded || llmInference == null) {
                return@withContext "Model not loaded yet."
            }
            
            val prompt = """
                Identify if "$term" is a technical term. If so, provide a very brief (1-sentence) explanation in Japanese.
                Term: $term
                Explanation:
            """.trimIndent()
            
            try {
                llmInference!!.generateResponse(prompt)
            } catch (e: Exception) {
                "Error generating explanation: ${e.message}"
            }
        }
    }

    fun close() {
        llmInference?.close()
        llmInference = null
        isModelLoaded = false
    }
}
