package com.webagent.app.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.webagent.app.data.EventData
import com.webagent.app.data.Recommendation
import com.webagent.app.data.RecommendationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class LLMClient(
    private val provider: String,
    private val geminiApiKey: String?,
    private val chatgptApiKey: String?,
    private val chatgptApiUrl: String
) {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    
    suspend fun generateRecommendations(events: List<EventData>): List<Recommendation> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildPrompt(events)
            val response = when (provider.lowercase()) {
                "gemini" -> callGemini(prompt)
                "chatgpt" -> callChatGPT(prompt)
                else -> throw IllegalArgumentException("Unknown provider: $provider")
            }
            parseRecommendations(response)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    private fun buildPrompt(events: List<EventData>): String {
        val eventsSummary = events.takeLast(50).joinToString("\n") { event ->
            "- [${event.type}] ${event.content} (${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(event.timestamp))})"
        }
        
        return """
당신은 사용자의 스마트폰 활동을 분석하여 유용한 추천을 제공하는 AI 어시스턴트입니다.

사용자의 최근 활동 내역:
$eventsSummary

다음 항목들을 분석하여 추천을 생성해주세요:

1. **약속 리마인더**: SMS나 이메일에서 언급된 약속, 미팅, 일정 등을 추출하여 리마인더 생성
2. **운동 추천**: 사용자의 활동 패턴을 보고 적절한 운동 시간과 종류 추천
3. **메시지/이메일 보내기**: 읽지 않은 메시지나 이메일에 대한 응답이 필요한 경우 알림
4. **설정 변경**: 배터리, 알림, 화면 밝기 등 전화기 설정 변경이 필요한 경우 안내

응답은 다음 JSON 형식으로 제공해주세요:
{
  "recommendations": [
    {
      "type": "REMINDER|EXERCISE_SUGGESTION|MESSAGE_TO_SEND|EMAIL_TO_SEND|SETTINGS_CHANGE",
      "title": "추천 제목",
      "description": "상세 설명",
      "action": "{\"key\": \"value\"}", // 선택적, JSON 문자열
      "priority": 0|1|2 // 0=낮음, 1=보통, 2=높음
    }
  ]
}

중요한 추천만 생성하고, 최대 10개까지만 제공해주세요.
""".trimIndent()
    }
    
    private suspend fun callGemini(prompt: String): String {
        if (geminiApiKey == null) throw IllegalStateException("Gemini API key not set")
        
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$geminiApiKey"
        
        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }.toString().toRequestBody(jsonMediaType)
        
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Gemini API error: ${response.code} ${response.message}")
        }
        
        val responseBody = response.body?.string() ?: throw IOException("Empty response")
        val json = JSONObject(responseBody)
        val candidates = json.getJSONArray("candidates")
        if (candidates.length() == 0) {
            throw IOException("No candidates in response")
        }
        
        val content = candidates.getJSONObject(0).getJSONObject("content")
        val parts = content.getJSONArray("parts")
        return parts.getJSONObject(0).getString("text")
    }
    
    private suspend fun callChatGPT(prompt: String): String {
        if (chatgptApiKey == null) throw IllegalStateException("ChatGPT API key not set")
        
        val requestBody = JSONObject().apply {
            put("model", "gpt-4")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("temperature", 0.7)
        }.toString().toRequestBody(jsonMediaType)
        
        val request = Request.Builder()
            .url(chatgptApiUrl)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $chatgptApiKey")
            .addHeader("Content-Type", "application/json")
            .build()
        
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("ChatGPT API error: ${response.code} ${response.message}")
        }
        
        val responseBody = response.body?.string() ?: throw IOException("Empty response")
        val json = JSONObject(responseBody)
        val choices = json.getJSONArray("choices")
        if (choices.length() == 0) {
            throw IOException("No choices in response")
        }
        
        return choices.getJSONObject(0).getJSONObject("message").getString("content")
    }
    
    private fun parseRecommendations(response: String): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()
        
        try {
            // JSON 응답에서 추출
            val jsonStart = response.indexOf("{")
            val jsonEnd = response.lastIndexOf("}") + 1
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonStr = response.substring(jsonStart, jsonEnd)
                val json = JSONObject(jsonStr)
                val recsArray = json.getJSONArray("recommendations")
                
                for (i in 0 until recsArray.length()) {
                    val rec = recsArray.getJSONObject(i)
                    recommendations.add(
                        Recommendation(
                            type = RecommendationType.valueOf(rec.getString("type")),
                            title = rec.getString("title"),
                            description = rec.getString("description"),
                            action = rec.optString("action", null),
                            priority = rec.optInt("priority", 0)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // JSON 파싱 실패 시 빈 리스트 반환
        }
        
        return recommendations
    }
}
