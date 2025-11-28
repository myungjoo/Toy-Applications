package com.magiccue.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val STORE_NAME = "magic_cue_settings"

private val Context.magicCueDataStore by preferencesDataStore(name = STORE_NAME)

class LLMPreferenceStore(private val context: Context) {
    private object Keys {
        val provider = stringPreferencesKey("provider")
        val apiKey = stringPreferencesKey("api_key")
        val model = stringPreferencesKey("model")
    }

    val configFlow: Flow<LLMConfig> = context.magicCueDataStore.data.map { prefs ->
        val providerName = prefs[Keys.provider] ?: LLMProvider.GEMINI.name
        LLMConfig(
            provider = runCatching { LLMProvider.valueOf(providerName) }.getOrDefault(LLMProvider.GEMINI),
            apiKey = prefs[Keys.apiKey] ?: "",
            model = prefs[Keys.model] ?: "gemini-1.5-flash"
        )
    }

    suspend fun updateConfig(newConfig: LLMConfig) {
        context.magicCueDataStore.edit { prefs ->
            prefs[Keys.provider] = newConfig.provider.name
            prefs[Keys.apiKey] = newConfig.apiKey
            prefs[Keys.model] = newConfig.model
        }
    }
}
