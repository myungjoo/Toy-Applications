package com.webagent.app.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.webagent.app.R
import com.webagent.app.WebAgentApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OverlayService : Service() {
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private lateinit var app: WebAgentApplication
    
    override fun onCreate() {
        super.onCreate()
        app = applicationContext as WebAgentApplication
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!app.preferenceStore.enableOverlay) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        val title = intent?.getStringExtra("title") ?: return START_NOT_STICKY
        val description = intent?.getStringExtra("description") ?: return START_NOT_STICKY
        val recommendationId = intent?.getLongExtra("recommendation_id", -1) ?: -1
        
        showOverlay(title, description, recommendationId)
        
        return START_NOT_STICKY
    }
    
    private fun showOverlay(title: String, description: String, recommendationId: Long) {
        // 기존 오버레이 제거
        removeOverlay()
        
        // 새 오버레이 생성
        val layoutInflater = LayoutInflater.from(this)
        overlayView = layoutInflater.inflate(R.layout.overlay_recommendation, null)
        
        val titleView = overlayView?.findViewById<TextView>(R.id.overlay_title)
        val descView = overlayView?.findViewById<TextView>(R.id.overlay_description)
        val closeBtn = overlayView?.findViewById<Button>(R.id.overlay_close)
        val actionBtn = overlayView?.findViewById<Button>(R.id.overlay_action)
        
        titleView?.text = title
        descView?.text = description
        
        closeBtn?.setOnClickListener {
            if (recommendationId > 0) {
                val scope = CoroutineScope(Dispatchers.IO)
                scope.launch {
                    app.database.recommendationDao().markAsRead(recommendationId)
                }
            }
            removeOverlay()
            stopSelf()
        }
        
        actionBtn?.setOnClickListener {
            // 추천에 따른 액션 수행
            // 여기서는 간단히 닫기만
            closeBtn?.performClick()
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 0
            y = 100
        }
        
        windowManager?.addView(overlayView, params)
        
        // 10초 후 자동으로 닫기
        overlayView?.postDelayed({
            removeOverlay()
            stopSelf()
        }, 10000)
    }
    
    private fun removeOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }
}
