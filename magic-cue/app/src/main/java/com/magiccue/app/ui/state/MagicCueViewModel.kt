package com.magiccue.app.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.magiccue.app.data.LLMConfig
import com.magiccue.app.data.LLMPreferenceStore
import com.magiccue.app.data.LLMProvider
import com.magiccue.app.data.MagicCueRepository
import com.magiccue.app.domain.MagicCueRequest
import com.magiccue.app.domain.ResponseTone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MagicCueViewModel(
    private val repository: MagicCueRepository,
    private val preferenceStore: LLMPreferenceStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(MagicCueUiState())
    val uiState: StateFlow<MagicCueUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferenceStore.configFlow.collect { config ->
                _uiState.update {
                    it.copy(
                        selectedProvider = config.provider,
                        apiKey = config.apiKey,
                        model = config.model
                    )
                }
            }
        }
    }

    fun updateConversationNotes(value: String) = updateState { copy(conversationNotes = value) }
    fun updateScenario(value: String) = updateState { copy(scenario = value) }
    fun updatePersona(value: String) = updateState { copy(persona = value) }
    fun updateLatestQuestion(value: String) = updateState { copy(latestQuestion = value) }
    fun updateTone(value: ResponseTone) = updateState { copy(tone = value) }
    fun updateProvider(provider: LLMProvider) = updateState { copy(selectedProvider = provider) }
    fun updateApiKey(key: String) = updateState { copy(apiKey = key) }
    fun updateModel(model: String) = updateState { copy(model = model) }

    fun persistConfig() {
        val current = _uiState.value
        viewModelScope.launch {
            preferenceStore.updateConfig(
                LLMConfig(
                    provider = current.selectedProvider,
                    apiKey = current.apiKey,
                    model = current.model
                )
            )
        }
    }

    fun generateCues() {
        val current = _uiState.value
        if (current.conversationNotes.isBlank() && current.latestQuestion.isBlank()) {
            _uiState.update { it.copy(errorMessage = "컨텍스트나 최근 질문을 입력해주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val request = MagicCueRequest(
                conversationNotes = current.conversationNotes,
                scenario = current.scenario,
                persona = current.persona,
                latestQuestion = current.latestQuestion,
                tone = current.tone
            )
            val config = LLMConfig(
                provider = current.selectedProvider,
                apiKey = current.apiKey,
                model = current.model
            )

            val result = repository.generateCues(request, config)
            _uiState.update {
                result.fold(
                    onSuccess = { response ->
                        it.copy(
                            suggestions = response.quickPrompts,
                            followUps = response.followUps,
                            summary = response.summary,
                            confidenceTips = response.confidenceTips,
                            isLoading = false,
                            lastUpdated = System.currentTimeMillis(),
                            errorMessage = null
                        )
                    },
                    onFailure = { throwable ->
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Magic Cue 생성에 실패했습니다."
                        )
                    }
                )
            }
        }
    }

    fun resetContext() {
        _uiState.update {
            it.copy(
                conversationNotes = "",
                scenario = "",
                persona = "",
                latestQuestion = "",
                suggestions = emptyList(),
                followUps = emptyList(),
                summary = "",
                confidenceTips = emptyList(),
                errorMessage = null,
                lastUpdated = null
            )
        }
    }

    private fun updateState(block: MagicCueUiState.() -> MagicCueUiState) {
        _uiState.update(block)
    }

    class Factory(
        private val repository: MagicCueRepository,
        private val preferenceStore: LLMPreferenceStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MagicCueViewModel::class.java)) {
                return MagicCueViewModel(repository, preferenceStore) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
