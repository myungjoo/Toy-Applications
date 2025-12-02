package com.webagent.app.data

import android.content.Context
import android.content.SharedPreferences

class PreferenceStore(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("webagent_prefs", Context.MODE_PRIVATE)
    
    var geminiApiKey: String?
        get() = prefs.getString("gemini_api_key", null)
        set(value) = prefs.edit().putString("gemini_api_key", value).apply()
    
    var chatgptApiKey: String?
        get() = prefs.getString("chatgpt_api_key", null)
        set(value) = prefs.edit().putString("chatgpt_api_key", value).apply()
    
    var chatgptApiUrl: String
        get() = prefs.getString("chatgpt_api_url", "https://api.openai.com/v1/chat/completions") ?: "https://api.openai.com/v1/chat/completions"
        set(value) = prefs.edit().putString("chatgpt_api_url", value).apply()
    
    var llmProvider: String
        get() = prefs.getString("llm_provider", "gemini") ?: "gemini"
        set(value) = prefs.edit().putString("llm_provider", value).apply()
    
    var enableSmsMonitoring: Boolean
        get() = prefs.getBoolean("enable_sms_monitoring", true)
        set(value) = prefs.edit().putBoolean("enable_sms_monitoring", value).apply()
    
    var enableNotificationMonitoring: Boolean
        get() = prefs.getBoolean("enable_notification_monitoring", true)
        set(value) = prefs.edit().putBoolean("enable_notification_monitoring", value).apply()
    
    var enableKeyboardMonitoring: Boolean
        get() = prefs.getBoolean("enable_keyboard_monitoring", true)
        set(value) = prefs.edit().putBoolean("enable_keyboard_monitoring", value).apply()
    
    var enableEmailMonitoring: Boolean
        get() = prefs.getBoolean("enable_email_monitoring", true)
        set(value) = prefs.edit().putBoolean("enable_email_monitoring", value).apply()
    
    var enableOverlay: Boolean
        get() = prefs.getBoolean("enable_overlay", true)
        set(value) = prefs.edit().putBoolean("enable_overlay", value).apply()
}
