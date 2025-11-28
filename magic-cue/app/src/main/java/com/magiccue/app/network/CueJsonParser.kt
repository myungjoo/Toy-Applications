package com.magiccue.app.network

import com.magiccue.app.domain.CueSuggestion
import com.magiccue.app.domain.MagicCueResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class CueJsonParser(
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = false }
) {
    fun parse(raw: String): MagicCueResponse {
        val sanitized = sanitize(raw)
        val dto = json.decodeFromString(MagicCueResponseDto.serializer(), sanitized)
        return MagicCueResponse(
            quickPrompts = dto.quickPrompts.map { CueSuggestion(it.headline, it.script) },
            followUps = dto.followUps,
            summary = dto.summary,
            confidenceTips = dto.confidenceTips
        )
    }

    private fun sanitize(raw: String): String {
        val trimmed = raw.trim()
        return trimmed
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
            .ifBlank { "{\"quickPrompts\":[],\"followUps\":[],\"summary\":\"\",\"confidenceTips\":[]}" }
    }
}

@Serializable
private data class MagicCueResponseDto(
    @SerialName("quickPrompts") val quickPrompts: List<CueSuggestionDto> = emptyList(),
    @SerialName("followUps") val followUps: List<String> = emptyList(),
    @SerialName("summary") val summary: String = "",
    @SerialName("confidenceTips") val confidenceTips: List<String> = emptyList()
)

@Serializable
private data class CueSuggestionDto(
    @SerialName("headline") val headline: String,
    @SerialName("script") val script: String
)
