package com.smsforwarder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.List;

public class SmsForwardingService extends Service {
    
    private static final String TAG = "SmsForwardingService";
    private static final int NOTIFICATION_ID = 1003;
    private static final String CHANNEL_ID = "SMS_FORWARDING_CHANNEL";
    
    private AppDatabase database;
    private boolean isProcessing = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        database = AppDatabase.getInstance(this);
        createNotificationChannel();
        Log.d(TAG, "SMS Forwarding Service created");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String sender = intent.getStringExtra("sender");
            String message = intent.getStringExtra("message");
            
            if (sender != null && message != null) {
                // Start as foreground service when processing SMS
                startForegroundProcessing();
                processSmsForwarding(sender, message);
            }
        }
        
        // Return START_STICKY to restart service if killed
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "SMS Forwarding Service destroyed");
        
        // Schedule restart if we were processing something
        if (isProcessing) {
            scheduleRestart();
        }
        
        super.onDestroy();
    }
    
    private void startForegroundProcessing() {
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SMS Forwarding Service",
                    NotificationManager.IMPORTANCE_MIN
            );
            channel.setDescription("Processes SMS forwarding");
            channel.setShowBadge(false);
            channel.setSound(null, null);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                    PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SMS Forwarder")
                .setContentText("Processing SMS forwarding...")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();
    }
    
    private void processSmsForwarding(String sender, String message) {
        isProcessing = true;
        
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
        } finally {
            isProcessing = false;
            
            // Stop foreground service after processing
            stopForeground(true);
            
            // Stop service after a delay to allow for any pending operations
            new android.os.Handler().postDelayed(() -> {
                stopSelf();
            }, 1000);
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
                smsManager.sendMultipartTextMessage(forwardToNumber, null, new java.util.ArrayList<>(messageParts), null, null);
            } else {
                smsManager.sendTextMessage(forwardToNumber, null, forwardedMessage, null, null);
            }
            
            Log.d(TAG, "SMS forwarded to: " + forwardToNumber);
            
            // Show toast notification
            try {
                Toast.makeText(this, "SMS forwarded to " + forwardToNumber, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Failed to show toast", e);
            }
                
        } catch (Exception e) {
            Log.e(TAG, "Failed to forward SMS to: " + forwardToNumber, e);
            try {
                Toast.makeText(this, "Failed to forward SMS", Toast.LENGTH_SHORT).show();
            } catch (Exception e2) {
                Log.e(TAG, "Failed to show error toast", e2);
            }
        }
    }
    
    private void scheduleRestart() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(this, ServiceRestartReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 2, intent, 
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                    PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT);
            
            // Schedule restart after 5 seconds
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 5000,
                pendingIntent
            );
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}