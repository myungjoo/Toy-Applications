package com.magiccue.app.data

import com.magiccue.app.domain.LLMPromptPayload
import com.magiccue.app.domain.MagicCueFallbackGenerator
import com.magiccue.app.domain.MagicCueRequest
import com.magiccue.app.domain.MagicCueResponse
import com.magiccue.app.domain.MagicCuePromptBuilder
import com.magiccue.app.network.LLMClient

class MagicCueRepository(
    private val llmClient: LLMClient,
    private val promptBuilder: MagicCuePromptBuilder = MagicCuePromptBuilder()
) {
    suspend fun generateCues(
        request: MagicCueRequest,
        config: LLMConfig
    ): Result<MagicCueResponse> {
        if (!config.isValid) {
            return Result.success(MagicCueFallbackGenerator.generate(request))
        }

        val payload: LLMPromptPayload = promptBuilder.build(request)
        return llmClient.generate(payload, request, config)
            .recoverCatching { MagicCueFallbackGenerator.generate(request) }
    }
}
