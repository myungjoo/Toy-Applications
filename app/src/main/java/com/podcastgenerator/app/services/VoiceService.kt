package com.podcastgenerator.app.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.podcastgenerator.app.models.PodcastScript
import com.podcastgenerator.app.models.ScriptSegment
import com.podcastgenerator.app.models.Speaker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Service for handling text-to-speech functionality
 */
class VoiceService(private val context: Context) : TextToSpeech.OnInitListener {
    
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var isPlaying = false
    private var currentSegmentIndex = 0
    private var currentScript: PodcastScript? = null
    
    // Callbacks for playback events
    private var onPlaybackStarted: (() -> Unit)? = null
    private var onPlaybackPaused: (() -> Unit)? = null
    private var onPlaybackStopped: (() -> Unit)? = null
    private var onSegmentStarted: ((ScriptSegment) -> Unit)? = null
    private var onSegmentCompleted: ((ScriptSegment) -> Unit)? = null
    private var onPlaybackCompleted: (() -> Unit)? = null
    private var onError: ((String) -> Unit)? = null

    init {
        initializeTextToSpeech()
    }

    /**
     * Initialize the TextToSpeech engine
     */
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.let { tts ->
                val result = tts.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    onError?.invoke("Language not supported")
                } else {
                    isInitialized = true
                    setupUtteranceProgressListener()
                }
            }
        } else {
            onError?.invoke("TextToSpeech initialization failed")
        }
    }

    /**
     * Sets up the utterance progress listener for tracking playback
     */
    private fun setupUtteranceProgressListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                utteranceId?.let { id ->
                    val segmentIndex = id.removePrefix("segment_").toIntOrNull() ?: 0
                    currentScript?.segments?.getOrNull(segmentIndex)?.let { segment ->
                        onSegmentStarted?.invoke(segment)
                    }
                }
            }

            override fun onDone(utteranceId: String?) {
                utteranceId?.let { id ->
                    val segmentIndex = id.removePrefix("segment_").toIntOrNull() ?: 0
                    currentScript?.segments?.getOrNull(segmentIndex)?.let { segment ->
                        onSegmentCompleted?.invoke(segment)
                    }
                    
                    // Move to next segment or complete playback
                    playNextSegment()
                }
            }

            override fun onError(utteranceId: String?) {
                onError?.invoke("Error during speech synthesis")
            }
        })
    }

    /**
     * Plays the entire podcast script
     */
    suspend fun playPodcastScript(script: PodcastScript) = withContext(Dispatchers.Main) {
        if (!isInitialized) {
            onError?.invoke("TextToSpeech not initialized")
            return@withContext
        }

        currentScript = script
        currentSegmentIndex = 0
        isPlaying = true
        
        onPlaybackStarted?.invoke()
        playCurrentSegment()
    }

    /**
     * Plays the current segment
     */
    private fun playCurrentSegment() {
        val script = currentScript ?: return
        val segment = script.segments.getOrNull(currentSegmentIndex) ?: return

        textToSpeech?.let { tts ->
            // Configure voice parameters based on speaker
            configureVoiceForSpeaker(segment.speaker)
            
            // Speak the segment content
            val utteranceId = "segment_$currentSegmentIndex"
            tts.speak(segment.content, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    /**
     * Plays the next segment in the script
     */
    private fun playNextSegment() {
        val script = currentScript ?: return
        
        currentSegmentIndex++
        
        if (currentSegmentIndex < script.segments.size) {
            // Add small pause between segments
            textToSpeech?.playSilence(500, TextToSpeech.QUEUE_ADD, null)
            playCurrentSegment()
        } else {
            // Playback completed
            isPlaying = false
            onPlaybackCompleted?.invoke()
        }
    }

    /**
     * Configures voice parameters for different speakers
     */
    private fun configureVoiceForSpeaker(speaker: Speaker) {
        textToSpeech?.let { tts ->
            // Adjust pitch and speech rate based on speaker
            when (speaker.voice.lowercase()) {
                "male" -> {
                    tts.setPitch(0.8f)
                    tts.setSpeechRate(0.9f)
                }
                "female" -> {
                    tts.setPitch(1.2f)
                    tts.setSpeechRate(1.0f)
                }
                else -> {
                    tts.setPitch(1.0f)
                    tts.setSpeechRate(1.0f)
                }
            }
        }
    }

    /**
     * Pauses the current playback
     */
    fun pausePlayback() {
        if (isPlaying) {
            textToSpeech?.stop()
            isPlaying = false
            onPlaybackPaused?.invoke()
        }
    }

    /**
     * Resumes playback from current position
     */
    fun resumePlayback() {
        if (!isPlaying && currentScript != null) {
            isPlaying = true
            onPlaybackStarted?.invoke()
            playCurrentSegment()
        }
    }

    /**
     * Stops the current playback
     */
    fun stopPlayback() {
        textToSpeech?.stop()
        isPlaying = false
        currentSegmentIndex = 0
        currentScript = null
        onPlaybackStopped?.invoke()
    }

    /**
     * Skips to the next segment
     */
    fun skipToNextSegment() {
        if (isPlaying && currentScript != null) {
            textToSpeech?.stop()
            playNextSegment()
        }
    }

    /**
     * Skips to the previous segment
     */
    fun skipToPreviousSegment() {
        if (currentScript != null && currentSegmentIndex > 0) {
            currentSegmentIndex -= 2 // Will be incremented in playNextSegment
            if (isPlaying) {
                textToSpeech?.stop()
                playNextSegment()
            }
        }
    }

    /**
     * Plays a specific segment by index
     */
    fun playSegment(segmentIndex: Int) {
        val script = currentScript ?: return
        
        if (segmentIndex in 0 until script.segments.size) {
            currentSegmentIndex = segmentIndex
            if (isPlaying) {
                textToSpeech?.stop()
            }
            isPlaying = true
            playCurrentSegment()
        }
    }

    /**
     * Gets the current playback status
     */
    fun getPlaybackStatus(): PlaybackStatus {
        return PlaybackStatus(
            isPlaying = isPlaying,
            currentSegmentIndex = currentSegmentIndex,
            totalSegments = currentScript?.segments?.size ?: 0,
            currentSegment = currentScript?.segments?.getOrNull(currentSegmentIndex)
        )
    }

    /**
     * Sets event listeners for playback events
     */
    fun setEventListeners(
        onPlaybackStarted: (() -> Unit)? = null,
        onPlaybackPaused: (() -> Unit)? = null,
        onPlaybackStopped: (() -> Unit)? = null,
        onSegmentStarted: ((ScriptSegment) -> Unit)? = null,
        onSegmentCompleted: ((ScriptSegment) -> Unit)? = null,
        onPlaybackCompleted: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        this.onPlaybackStarted = onPlaybackStarted
        this.onPlaybackPaused = onPlaybackPaused
        this.onPlaybackStopped = onPlaybackStopped
        this.onSegmentStarted = onSegmentStarted
        this.onSegmentCompleted = onSegmentCompleted
        this.onPlaybackCompleted = onPlaybackCompleted
        this.onError = onError
    }

    /**
     * Releases resources
     */
    fun release() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }

    /**
     * Checks if TTS is available and ready
     */
    fun isReady(): Boolean {
        return isInitialized && textToSpeech != null
    }

    /**
     * Data class for playback status
     */
    data class PlaybackStatus(
        val isPlaying: Boolean,
        val currentSegmentIndex: Int,
        val totalSegments: Int,
        val currentSegment: ScriptSegment?
    )
}