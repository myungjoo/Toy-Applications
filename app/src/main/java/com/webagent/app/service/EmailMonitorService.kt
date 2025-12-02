package com.webagent.app.service

import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.IBinder
import android.provider.ContactsContract
import com.webagent.app.WebAgentApplication
import com.webagent.app.data.EventData
import com.webagent.app.data.EventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EmailMonitorService : Service() {
    private lateinit var app: WebAgentApplication
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.IO)
    private var lastCheckTime = System.currentTimeMillis()
    
    override fun onCreate() {
        super.onCreate()
        app = applicationContext as WebAgentApplication
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            startMonitoring()
        }
        return START_STICKY
    }
    
    private fun startMonitoring() {
        scope.launch {
            while (isRunning) {
                if (app.preferenceStore.enableEmailMonitoring) {
                    checkEmails()
                }
                delay(60000) // 1분마다 체크
            }
        }
    }
    
    private suspend fun checkEmails() {
        try {
            // Gmail 앱의 이메일 확인 (Content Provider 사용)
            val gmailUri = Uri.parse("content://gmail-ls/unread/")
            val cursor: Cursor? = contentResolver.query(
                gmailUri,
                null,
                null,
                null,
                null
            )
            
            cursor?.use {
                // Gmail 데이터 파싱 및 저장
                // 실제 구현은 Gmail API나 다른 방법이 필요할 수 있음
            }
            
            // 일반 이메일 클라이언트는 Content Provider로 접근이 제한적이므로
            // 사용자가 이메일 앱을 열 때 알림을 받는 방식으로 대체
            // 여기서는 기본 구조만 제공
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // 이메일 앱에서 공유된 데이터를 받는 메서드
    fun handleEmailData(subject: String, from: String, body: String) {
        scope.launch {
            app.database.eventDao().insertEvent(
                EventData(
                    type = EventType.EMAIL_INCOMING,
                    content = "From: $from\nSubject: $subject\n$body",
                    metadata = "{\"from\":\"$from\",\"subject\":\"${subject.replace("\"", "\\\"")}\"}"
                )
            )
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }
}
