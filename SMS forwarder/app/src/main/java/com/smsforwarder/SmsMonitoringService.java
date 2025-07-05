package com.smsforwarder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class SmsMonitoringService extends Service {
    
    private static final String TAG = "SmsMonitoringService";
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "SMS_MONITORING_CHANNEL";
    private static final String PREFS_NAME = "SmsForwarderPrefs";
    private static final String KEY_MONITORING_ENABLED = "monitoring_enabled";
    
    private PowerManager.WakeLock wakeLock;
    private Handler handler;
    private SharedPreferences prefs;
    private boolean isRunning = false;
    
    // Heartbeat mechanism to keep service alive
    private static final long HEARTBEAT_INTERVAL = 10000; // 10 seconds
    private Runnable heartbeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                Log.d(TAG, "Service heartbeat");
                
                // Check if monitoring is still enabled
                boolean monitoringEnabled = prefs.getBoolean(KEY_MONITORING_ENABLED, false);
                if (!monitoringEnabled) {
                    Log.d(TAG, "Monitoring disabled, stopping service");
                    stopSelf();
                    return;
                }
                
                // Schedule next heartbeat
                handler.postDelayed(this, HEARTBEAT_INTERVAL);
            }
        }
    };
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SMS Monitoring Service created");
        
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        handler = new Handler();
        
        // Acquire wake lock to prevent device from sleeping
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SmsForwarder::SmsMonitoringWakeLock");
        
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SMS Monitoring Service started");
        
        if (!isRunning) {
            isRunning = true;
            
            // Acquire wake lock
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
            
            // Create and show persistent notification
            Notification notification = createNotification();
            startForeground(NOTIFICATION_ID, notification);
            
            // Start heartbeat mechanism
            startHeartbeat();
            
            // Start keep-alive service
            Intent keepAliveIntent = new Intent(this, KeepAliveService.class);
            try {
                startForegroundService(keepAliveIntent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to start keep-alive service", e);
            }
        }
        
        // Return START_STICKY to restart service if killed
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "SMS Monitoring Service destroyed - attempting to restart");
        
        isRunning = false;
        
        // Release wake lock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        
        // Cancel heartbeat
        if (handler != null) {
            handler.removeCallbacks(heartbeatRunnable);
        }
        
        // Schedule restart if monitoring is still enabled
        boolean monitoringEnabled = prefs.getBoolean(KEY_MONITORING_ENABLED, false);
        if (monitoringEnabled) {
            scheduleRestart();
        }
        
        super.onDestroy();
    }
    
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "Task removed - scheduling restart");
        
        // Check if monitoring is enabled
        boolean monitoringEnabled = prefs.getBoolean(KEY_MONITORING_ENABLED, false);
        if (monitoringEnabled) {
            scheduleRestart();
        }
        
        super.onTaskRemoved(rootIntent);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SMS Monitoring Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Monitors incoming SMS messages for forwarding");
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
                .setContentText("Monitoring SMS messages for forwarding")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
    
    private void startHeartbeat() {
        handler.post(heartbeatRunnable);
    }
    
    private void scheduleRestart() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(this, ServiceRestartReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 
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
}