package com.webagent.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.webagent.app.MainActivity
import com.webagent.app.R
import com.webagent.app.WebAgentApplication
import com.webagent.app.data.EventData
import com.webagent.app.network.LLMClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DataCollectionService : Service() {
    private lateinit var app: WebAgentApplication
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isRunning = false
    
    override fun onCreate() {
        super.onCreate()
        app = applicationContext as WebAgentApplication
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        
        if (!isRunning) {
            isRunning = true
            startDataProcessing()
        }
        
        return START_STICKY
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "WebAgent Data Collection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "WebAgent 데이터 수집 서비스"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WebAgent 실행 중")
            .setContentText("데이터 수집 및 분석 중...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun startDataProcessing() {
        scope.launch {
            while (isRunning) {
                try {
                    // 최근 이벤트 가져오기
                    val events = app.database.eventDao().getEventsSince(
                        System.currentTimeMillis() - 3600000 // 최근 1시간
                    ).first()
                    
                    if (events.isNotEmpty()) {
                        // LLM으로 추천 생성
                        generateRecommendations(events)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                delay(300000) // 5분마다 처리
            }
        }
    }
    
    private suspend fun generateRecommendations(events: List<EventData>) {
        try {
            val prefs = app.preferenceStore
            val provider = prefs.llmProvider
            val geminiKey = prefs.geminiApiKey
            val chatgptKey = prefs.chatgptApiKey
            val chatgptUrl = prefs.chatgptApiUrl
            
            if ((provider == "gemini" && geminiKey == null) ||
                (provider == "chatgpt" && chatgptKey == null)) {
                return // API 키가 없으면 스킵
            }
            
            val llmClient = LLMClient(provider, geminiKey, chatgptKey, chatgptUrl)
            val recommendations = llmClient.generateRecommendations(events)
            
            // 추천 저장
            recommendations.forEach { rec ->
                app.database.recommendationDao().insertRecommendation(rec)
            }
            
            // 오버레이에 새 추천 표시
            if (prefs.enableOverlay && recommendations.isNotEmpty()) {
                showOverlay(recommendations.first())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun showOverlay(recommendation: com.webagent.app.data.Recommendation) {
        // OverlayService에서 처리
        val intent = Intent(this, OverlayService::class.java).apply {
            putExtra("recommendation_id", recommendation.id)
            putExtra("title", recommendation.title)
            putExtra("description", recommendation.description)
            putExtra("type", recommendation.type.name)
        }
        startService(intent)
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }
    
    companion object {
        private const val CHANNEL_ID = "webagent_data_collection"
        private const val NOTIFICATION_ID = 1
    }
}
