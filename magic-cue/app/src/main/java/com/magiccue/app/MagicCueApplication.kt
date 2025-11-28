package com.magiccue.app

import android.app.Application
import com.magiccue.app.data.LLMPreferenceStore
import com.magiccue.app.data.MagicCueRepository
import com.magiccue.app.network.NetworkLLMClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class MagicCueApplication : Application() {
    val preferenceStore: LLMPreferenceStore by lazy { LLMPreferenceStore(this) }

    private val httpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
            redactHeader("Authorization")
            redactHeader("x-goog-api-key")
        }

        OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "MagicCue/1.0 (Pixel10 emulation)")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()
    }

    val repository: MagicCueRepository by lazy {
        MagicCueRepository(llmClient = NetworkLLMClient(httpClient))
    }
}
