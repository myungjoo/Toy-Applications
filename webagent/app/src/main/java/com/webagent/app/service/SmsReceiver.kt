package com.webagent.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import com.webagent.app.WebAgentApplication
import com.webagent.app.data.EventData
import com.webagent.app.data.EventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val app = context.applicationContext as WebAgentApplication
            if (!app.preferenceStore.enableSmsMonitoring) return
            
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val scope = CoroutineScope(Dispatchers.IO)
            
            for (smsMessage in messages) {
                val sender = smsMessage.originatingAddress ?: "Unknown"
                val messageBody = smsMessage.messageBody ?: ""
                
                scope.launch {
                    app.database.eventDao().insertEvent(
                        EventData(
                            type = EventType.SMS_INCOMING,
                            content = "From: $sender\n$messageBody",
                            metadata = "{\"sender\":\"$sender\",\"body\":\"${messageBody.replace("\"", "\\\"")}\"}"
                        )
                    )
                }
            }
        }
    }
}
