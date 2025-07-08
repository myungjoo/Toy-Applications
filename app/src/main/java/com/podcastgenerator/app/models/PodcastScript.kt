package com.podcastgenerator.app.models

/**
 * Represents a speaker in the podcast
 */
data class Speaker(
    val id: String,
    val name: String,
    val voice: String = "default" // For TTS voice selection
)

/**
 * Represents a segment of the podcast script
 */
data class ScriptSegment(
    val id: String,
    val speaker: Speaker,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: SegmentType = SegmentType.SPEECH
)

/**
 * Types of script segments
 */
enum class SegmentType {
    SPEECH,
    QUESTION,
    ANSWER,
    DEBATE_POINT,
    CONCLUSION,
    INTRODUCTION
}

/**
 * Represents the complete podcast script
 */
data class PodcastScript(
    val id: String,
    val title: String,
    val sourceUrls: List<String>,
    val segments: List<ScriptSegment>,
    val speakers: List<Speaker>,
    val createdAt: Long = System.currentTimeMillis(),
    val duration: Long = 0, // in milliseconds
    val status: ScriptStatus = ScriptStatus.DRAFT
)

/**
 * Status of the podcast script
 */
enum class ScriptStatus {
    DRAFT,
    PROCESSING,
    COMPLETED,
    ERROR
}

/**
 * Request for generating a podcast script
 */
data class ScriptGenerationRequest(
    val urls: List<String>,
    val style: PodcastStyle = PodcastStyle.CONVERSATION,
    val speakers: List<Speaker> = defaultSpeakers()
)

/**
 * Different styles of podcast scripts
 */
enum class PodcastStyle {
    CONVERSATION,
    QNA,
    DEBATE,
    INTERVIEW
}

/**
 * Default speakers for the podcast
 */
fun defaultSpeakers(): List<Speaker> {
    return listOf(
        Speaker("speaker1", "Alex", "male"),
        Speaker("speaker2", "Sam", "female")
    )
}

/**
 * Web content extracted from URLs
 */
data class WebContent(
    val url: String,
    val title: String,
    val content: String,
    val summary: String = "",
    val extractedAt: Long = System.currentTimeMillis()
)