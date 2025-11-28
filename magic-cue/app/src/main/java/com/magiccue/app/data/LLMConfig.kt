package com.magiccue.app.data

import kotlinx.serialization.Serializable

enum class LLMProvider(val displayName: String) {
    GEMINI("Gemini"),
    OPEN_AI("ChatGPT");
}

@Serializable
data class LLMConfig(
    val provider: LLMProvider = LLMProvider.GEMINI,
    val apiKey: String = "",
    val model: String = "gemini-1.5-flash"
) {
    val isValid: Boolean get() = apiKey.isNotBlank() && model.isNotBlank()
}
