package com.magiccue.app.domain

import kotlin.math.min

enum class ResponseTone(val label: String) {
    FRIENDLY("친근함"),
    CONFIDENT("자신감"),
    PROFESSIONAL("전문성"),
    EMPATHETIC("공감"),
    CALM("차분함");

    companion object {
        fun fromLabel(label: String): ResponseTone = values().firstOrNull {
            it.name.equals(label, ignoreCase = true) || it.label == label
        } ?: PROFESSIONAL
    }
}

data class MagicCueRequest(
    val conversationNotes: String,
    val scenario: String,
    val persona: String,
    val latestQuestion: String,
    val tone: ResponseTone,
    val temperature: Double = 0.4
)

data class CueSuggestion(
    val headline: String,
    val script: String
)

data class MagicCueResponse(
    val quickPrompts: List<CueSuggestion>,
    val followUps: List<String>,
    val summary: String,
    val confidenceTips: List<String>
)

data class LLMPromptPayload(
    val systemPrompt: String,
    val userPrompt: String
)

class MagicCuePromptBuilder {
    fun build(request: MagicCueRequest): LLMPromptPayload {
        val systemPrompt = """
            당신은 Pixel 10 Magic Cue 비서입니다. 사용자의 통화/미팅 상황을 분석하여 
            1) quickPrompts (headline, script)
            2) followUps (최대 3개)
            3) summary (문단 하나)
            4) confidenceTips (최대 3개) 
            를 JSON으로만 응답하세요. JSON schema:
            {
              "quickPrompts": [{"headline":"","script":""}],
              "followUps": [""],
              "summary": "",
              "confidenceTips": [""]
            }
            헤드라인은 35자 이내, 스크립트는 2문장 이내 한국어로 작성하고 톤은 ${request.tone.label} 기준으로 유지하세요.
        """.trimIndent()

        val userPrompt = """
            상황: ${request.scenario.ifBlank { "일반 통화" }}
            화자 페르소나: ${request.persona.ifBlank { "모바일 사용자" }}
            최근 상대 질문: ${request.latestQuestion.ifBlank { "없음" }}
            메모: ${request.conversationNotes.ifBlank { "사용자 추가 메모 없음" }}
            응답 온도: ${request.temperature}
            출력 언어: 한국어
        """.trimIndent()

        return LLMPromptPayload(systemPrompt = systemPrompt, userPrompt = userPrompt)
    }
}

object MagicCueFallbackGenerator {
    private val defaultFollowUps = listOf(
        "다음 단계 일정 제안하시겠어요?",
        "상대의 우려를 한 문장으로 확인해주세요.",
        "공유할 핵심 자료가 있다면 언급하세요."
    )

    fun generate(request: MagicCueRequest): MagicCueResponse {
        val normalizedScenario = request.scenario.ifBlank { "일반 대화" }
        val focus = extractFocus(request)
        val prompt = CueSuggestion(
            headline = "${request.tone.label} 톤 핵심 포인트",
            script = "${focus.first}에 대해 ${focus.second} 방식으로 답변하되 ${normalizedScenario} 맥락을 잊지 마세요."
        )

        val summary = buildString {
            append("${normalizedScenario} 상황에서 ")
            if (request.latestQuestion.isNotBlank()) {
                append("상대가 '${request.latestQuestion}' 질문을 던졌고, ")
            }
            append("당신은 ${request.persona.ifBlank { "사용자" }} 페르소나로 자연스럽게 이어가야 합니다.")
        }

        val tips = listOf(
            "문장당 길이를 ${if (request.tone == ResponseTone.PROFESSIONAL) "30" else "20"}자 이내로 유지하세요.",
            "상대 이름을 최소 한 번 호명하면 신뢰가 높아집니다.",
            "핵심 메시지를 재확인하며 마무리하세요."
        )

        return MagicCueResponse(
            quickPrompts = listOf(prompt),
            followUps = defaultFollowUps.take(min(3, defaultFollowUps.size)),
            summary = summary,
            confidenceTips = tips
        )
    }

    private fun extractFocus(request: MagicCueRequest): Pair<String, String> {
        return when {
            request.conversationNotes.contains("가격") -> "가격 업데이트" to "명확한 숫자"
            request.conversationNotes.contains("문제" ) -> "문제 해결" to "원인 파악"
            request.conversationNotes.contains("채용") -> "채용 제안" to "성장 기회"
            else -> "핵심 요청" to "상대 공감"
        }
    }
}
