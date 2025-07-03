package com.smsforwarder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class SmsForwardingService extends Service {
    
    private static final String TAG = "SmsForwardingService";
    private AppDatabase database;
    
    @Override
    public void onCreate() {
        super.onCreate();
        database = AppDatabase.getInstance(this);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String sender = intent.getStringExtra("sender");
            String message = intent.getStringExtra("message");
            
            if (sender != null && message != null) {
                processSmsForwarding(sender, message);
            }
        }
        
        // Return START_STICKY to restart service if killed
        return START_STICKY;
    }
    
    private void processSmsForwarding(String sender, String message) {
        try {
            // Get all enabled forwarding rules
            List<ForwardingRule> rules = database.forwardingRuleDao().getAllEnabledRules();
            
            Log.d(TAG, "Processing SMS from: " + sender + " with " + rules.size() + " rules");
            
            for (ForwardingRule rule : rules) {
                if (rule.matchesSms(sender, message)) {
                    Log.d(TAG, "Rule matched: " + rule.toString());
                    forwardSms(rule.forwardToNumber, sender, message);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing SMS forwarding", e);
        }
    }
    
    private void forwardSms(String forwardToNumber, String originalSender, String originalMessage) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            
            // Create forwarded message with original sender info
            String forwardedMessage = "Forwarded SMS from " + originalSender + ":\n" + originalMessage;
            
            // Split message if it's too long
            if (forwardedMessage.length() > 160) {
                List<String> messageParts = smsManager.divideMessage(forwardedMessage);
                smsManager.sendMultipartTextMessage(forwardToNumber, null, messageParts, null, null);
            } else {
                smsManager.sendTextMessage(forwardToNumber, null, forwardedMessage, null, null);
            }
            
            Log.d(TAG, "SMS forwarded to: " + forwardToNumber);
            
            // Show toast notification
            Toast.makeText(this, 
                getString(R.string.sms_forwarded, forwardToNumber), 
                Toast.LENGTH_SHORT).show();
                
        } catch (Exception e) {
            Log.e(TAG, "Failed to forward SMS to: " + forwardToNumber, e);
            Toast.makeText(this, 
                getString(R.string.forward_failed), 
                Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}