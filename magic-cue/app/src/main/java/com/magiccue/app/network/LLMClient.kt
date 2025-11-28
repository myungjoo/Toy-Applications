package com.magiccue.app.network

import com.magiccue.app.data.LLMConfig
import com.magiccue.app.data.LLMProvider
import com.magiccue.app.domain.LLMPromptPayload
import com.magiccue.app.domain.MagicCueRequest
import com.magiccue.app.domain.MagicCueResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

interface LLMClient {
    suspend fun generate(
        payload: LLMPromptPayload,
        request: MagicCueRequest,
        config: LLMConfig
    ): Result<MagicCueResponse>
}

class NetworkLLMClient(
    private val httpClient: OkHttpClient,
    private val parser: CueJsonParser = CueJsonParser(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : LLMClient {
    override suspend fun generate(
        payload: LLMPromptPayload,
        request: MagicCueRequest,
        config: LLMConfig
    ): Result<MagicCueResponse> = withContext(dispatcher) {
        runCatching {
            val httpRequest = when (config.provider) {
                LLMProvider.GEMINI -> buildGeminiRequest(payload, request, config)
                LLMProvider.OPEN_AI -> buildOpenAiRequest(payload, request, config)
            }

            httpClient.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("LLM 응답 오류: ${response.code} ${response.message}")
                }
                val rawBody = response.body?.string().orEmpty()
                val llmText = when (config.provider) {
                    LLMProvider.GEMINI -> extractGeminiText(rawBody)
                    LLMProvider.OPEN_AI -> extractOpenAiText(rawBody)
                }
                parser.parse(llmText)
            }
        }
    }

    private fun buildGeminiRequest(
        payload: LLMPromptPayload,
        request: MagicCueRequest,
        config: LLMConfig
    ): Request {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/${config.model}:generateContent?key=${config.apiKey}"
        val body = JSONObject().apply {
            put(
                "contents",
                JSONArray().apply {
                    put(
                        JSONObject().apply {
                            put("role", "user")
                            put(
                                "parts",
                                JSONArray().apply {
                                    put(
                                        JSONObject().apply {
                                            put("text", "${payload.systemPrompt}\n${payload.userPrompt}")
                                        }
                                    )
                                }
                            )
                        }
                    )
                }
            )
            put(
                "generationConfig",
                JSONObject().apply {
                    put("temperature", request.temperature)
                    put("maxOutputTokens", 512)
                }
            )
        }.toString()

        return Request.Builder()
            .url(url)
            .post(body.toRequestBody(JSON_MEDIA_TYPE))
            .build()
    }

    private fun buildOpenAiRequest(
        payload: LLMPromptPayload,
        request: MagicCueRequest,
        config: LLMConfig
    ): Request {
        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", payload.systemPrompt)
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", payload.userPrompt)
            })
        }
        val body = JSONObject().apply {
            put("model", config.model)
            put("messages", messages)
            put("temperature", request.temperature)
            put("max_tokens", 512)
        }.toString()

        return Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .post(body.toRequestBody(JSON_MEDIA_TYPE))
            .build()
    }

    private fun extractGeminiText(rawBody: String): String {
        val json = JSONObject(rawBody)
        val candidates = json.optJSONArray("candidates") ?: return rawBody
        val content = candidates.optJSONObject(0)?.optJSONObject("content")
        val parts = content?.optJSONArray("parts") ?: return rawBody
        return parts.optJSONObject(0)?.optString("text").orEmpty()
    }

    private fun extractOpenAiText(rawBody: String): String {
        val json = JSONObject(rawBody)
        val choices = json.optJSONArray("choices") ?: return rawBody
        val message = choices.optJSONObject(0)?.optJSONObject("message")
        return message?.optString("content").orEmpty()
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
