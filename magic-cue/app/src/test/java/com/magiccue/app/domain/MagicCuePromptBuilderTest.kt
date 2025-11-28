package com.magiccue.app.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class MagicCuePromptBuilderTest {

    private val builder = MagicCuePromptBuilder()

    @Test
    fun `prompt builder embeds scenario and persona`() {
        val request = MagicCueRequest(
            conversationNotes = "가격 재협상 필요",
            scenario = "파트너 계약 업데이트",
            persona = "파트너십 매니저",
            latestQuestion = "최종 견적이 어떻게 되나요?",
            tone = ResponseTone.CONFIDENT
        )

        val payload = builder.build(request)

        assertTrue(payload.systemPrompt.contains("Pixel 10 Magic Cue"))
        assertTrue(payload.userPrompt.contains("파트너십 매니저"))
        assertTrue(payload.userPrompt.contains("파트너 계약 업데이트"))
    }
}
