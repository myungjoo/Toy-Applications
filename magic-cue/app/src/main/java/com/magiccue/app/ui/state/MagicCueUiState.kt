package com.magiccue.app.ui.state

import com.magiccue.app.data.LLMProvider
import com.magiccue.app.domain.CueSuggestion
import com.magiccue.app.domain.ResponseTone

data class MagicCueUiState(
    val conversationNotes: String = "",
    val scenario: String = "",
    val persona: String = "",
    val latestQuestion: String = "",
    val tone: ResponseTone = ResponseTone.PROFESSIONAL,
    val suggestions: List<CueSuggestion> = emptyList(),
    val followUps: List<String> = emptyList(),
    val summary: String = "",
    val confidenceTips: List<String> = emptyList(),
    val selectedProvider: LLMProvider = LLMProvider.GEMINI,
    val apiKey: String = "",
    val model: String = "gemini-1.5-flash",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastUpdated: Long? = null
)
