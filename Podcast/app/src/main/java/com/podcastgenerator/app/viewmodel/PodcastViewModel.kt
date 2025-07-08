package com.podcastgenerator.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.podcastgenerator.app.models.*
import com.podcastgenerator.app.services.LLMService
import com.podcastgenerator.app.services.VoiceService
import com.podcastgenerator.app.services.WebContentParser
import kotlinx.coroutines.launch

/**
 * ViewModel for managing podcast generation and playback
 */
class PodcastViewModel(application: Application) : AndroidViewModel(application) {
    
    // Services
    private val webContentParser = WebContentParser()
    private val llmService = LLMService()
    private val voiceService = VoiceService(application)
    
    // UI State
    private val _uiState = MutableLiveData(PodcastUIState())
    val uiState: LiveData<PodcastUIState> = _uiState
    
    // Current script
    private val _currentScript = MutableLiveData<PodcastScript?>()
    val currentScript: LiveData<PodcastScript?> = _currentScript
    
    // Playback status
    private val _playbackStatus = MutableLiveData<VoiceService.PlaybackStatus>()
    val playbackStatus: LiveData<VoiceService.PlaybackStatus> = _playbackStatus
    
    // Error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        setupVoiceServiceListeners()
    }

    /**
     * Sets up listeners for voice service events
     */
    private fun setupVoiceServiceListeners() {
        voiceService.setEventListeners(
            onPlaybackStarted = {
                updateUIState { it.copy(isPlaying = true, isLoading = false) }
                updatePlaybackStatus()
            },
            onPlaybackPaused = {
                updateUIState { it.copy(isPlaying = false) }
                updatePlaybackStatus()
            },
            onPlaybackStopped = {
                updateUIState { it.copy(isPlaying = false) }
                updatePlaybackStatus()
            },
            onSegmentStarted = { segment ->
                updateUIState { it.copy(currentSegment = segment) }
                updatePlaybackStatus()
            },
            onSegmentCompleted = { segment ->
                updatePlaybackStatus()
            },
            onPlaybackCompleted = {
                updateUIState { it.copy(isPlaying = false, currentSegment = null) }
                updatePlaybackStatus()
            },
            onError = { error ->
                _errorMessage.value = error
                updateUIState { it.copy(isPlaying = false, isLoading = false) }
            }
        )
    }

    /**
     * Generates a podcast script from the provided URLs
     */
    fun generatePodcastScript(
        urls: List<String>,
        style: PodcastStyle = PodcastStyle.CONVERSATION
    ) {
        viewModelScope.launch {
            try {
                updateUIState { it.copy(isLoading = true, errorMessage = null) }
                
                // Validate URLs
                val validUrls = urls.filter { webContentParser.isValidUrl(it) }
                if (validUrls.isEmpty()) {
                    _errorMessage.value = "Please provide valid URLs"
                    updateUIState { it.copy(isLoading = false) }
                    return@launch
                }
                
                updateUIState { it.copy(loadingMessage = "Extracting content from URLs...") }
                
                // Extract content from URLs
                val webContents = webContentParser.extractContentFromUrls(validUrls)
                if (webContents.isEmpty()) {
                    _errorMessage.value = "Failed to extract content from any of the provided URLs"
                    updateUIState { it.copy(isLoading = false) }
                    return@launch
                }
                
                updateUIState { it.copy(loadingMessage = "Generating podcast script...") }
                
                // Generate podcast script using LLM
                val request = ScriptGenerationRequest(
                    urls = validUrls,
                    style = style,
                    speakers = defaultSpeakers()
                )
                
                llmService.generatePodcastScript(
                    webContents = webContents,
                    style = style,
                    speakers = request.speakers
                ).onSuccess { script ->
                    _currentScript.value = script
                    updateUIState { 
                        it.copy(
                            isLoading = false,
                            hasScript = true,
                            loadingMessage = null
                        ) 
                    }
                }.onFailure { exception ->
                    _errorMessage.value = exception.message
                    updateUIState { it.copy(isLoading = false) }
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Unexpected error: ${e.message}"
                updateUIState { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Plays the current podcast script
     */
    fun playPodcast() {
        val script = _currentScript.value
        if (script != null && voiceService.isReady()) {
            viewModelScope.launch {
                voiceService.playPodcastScript(script)
            }
        } else if (!voiceService.isReady()) {
            _errorMessage.value = "Text-to-speech is not ready"
        } else {
            _errorMessage.value = "No script available to play"
        }
    }

    /**
     * Pauses the current playback
     */
    fun pausePodcast() {
        voiceService.pausePlayback()
    }

    /**
     * Resumes the current playback
     */
    fun resumePodcast() {
        voiceService.resumePlayback()
    }

    /**
     * Stops the current playback
     */
    fun stopPodcast() {
        voiceService.stopPlayback()
    }

    /**
     * Skips to the next segment
     */
    fun skipToNext() {
        voiceService.skipToNextSegment()
    }

    /**
     * Skips to the previous segment
     */
    fun skipToPrevious() {
        voiceService.skipToPreviousSegment()
    }

    /**
     * Plays a specific segment
     */
    fun playSegment(segmentIndex: Int) {
        voiceService.playSegment(segmentIndex)
    }

    /**
     * Clears the current error message
     */
    fun clearError() {
        _errorMessage.value = null
        updateUIState { it.copy(errorMessage = null) }
    }

    /**
     * Resets the entire state (for generating a new script)
     */
    fun reset() {
        voiceService.stopPlayback()
        _currentScript.value = null
        updateUIState { 
            PodcastUIState(
                isLoading = false,
                hasScript = false,
                isPlaying = false
            ) 
        }
        clearError()
    }

    /**
     * Updates the UI state
     */
    private fun updateUIState(update: (PodcastUIState) -> PodcastUIState) {
        _uiState.value = update(_uiState.value ?: PodcastUIState())
    }

    /**
     * Updates the playback status
     */
    private fun updatePlaybackStatus() {
        _playbackStatus.value = voiceService.getPlaybackStatus()
    }

    /**
     * Validates input URLs
     */
    fun validateUrls(urls: List<String>): ValidationResult {
        val validUrls = mutableListOf<String>()
        val invalidUrls = mutableListOf<String>()
        
        urls.forEach { url ->
            if (webContentParser.isValidUrl(url.trim())) {
                validUrls.add(url.trim())
            } else {
                invalidUrls.add(url.trim())
            }
        }
        
        return ValidationResult(
            validUrls = validUrls,
            invalidUrls = invalidUrls,
            isValid = invalidUrls.isEmpty() && validUrls.isNotEmpty()
        )
    }

    /**
     * Gets the current script segments for UI display
     */
    fun getScriptSegments(): List<ScriptSegment> {
        return _currentScript.value?.segments ?: emptyList()
    }

    /**
     * Gets the current script title
     */
    fun getScriptTitle(): String {
        return _currentScript.value?.title ?: "Podcast Script"
    }

    override fun onCleared() {
        super.onCleared()
        voiceService.release()
    }
}

/**
 * UI state for the podcast generation screen
 */
data class PodcastUIState(
    val isLoading: Boolean = false,
    val hasScript: Boolean = false,
    val isPlaying: Boolean = false,
    val loadingMessage: String? = null,
    val errorMessage: String? = null,
    val currentSegment: ScriptSegment? = null
)

/**
 * Result of URL validation
 */
data class ValidationResult(
    val validUrls: List<String>,
    val invalidUrls: List<String>,
    val isValid: Boolean
)