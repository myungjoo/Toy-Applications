package com.webagent.app.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.webagent.app.WebAgentApplication
import com.webagent.app.data.EventData
import com.webagent.app.data.EventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationListenerService : NotificationListenerService() {
    private lateinit var app: WebAgentApplication
    
    override fun onCreate() {
        super.onCreate()
        app = applicationContext as WebAgentApplication
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        if (!app.preferenceStore.enableNotificationMonitoring) return
        if (sbn == null) return
        
        val packageName = sbn.packageName ?: return
        val notification = sbn.notification ?: return
        val title = notification.extras?.getCharSequence("android.title")?.toString() ?: ""
        val text = notification.extras?.getCharSequence("android.text")?.toString() ?: ""
        
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            app.database.eventDao().insertEvent(
                EventData(
                    type = EventType.NOTIFICATION,
                    content = "$packageName: $title - $text",
                    metadata = "{\"package\":\"$packageName\",\"title\":\"${title.replace("\"", "\\\"")}\",\"text\":\"${text.replace("\"", "\\\"")}\"}"
                )
            )
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // 필요시 구현
    }
}
