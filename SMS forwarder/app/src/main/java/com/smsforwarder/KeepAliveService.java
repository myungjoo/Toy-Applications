package com.smsforwarder;

import android.app.ActivityManager;
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

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KeepAliveService extends Service {
    
    private static final String TAG = "KeepAliveService";
    private static final int NOTIFICATION_ID = 1002;
    private static final String CHANNEL_ID = "KEEP_ALIVE_CHANNEL";
    private static final String PREFS_NAME = "SmsForwarderPrefs";
    private static final String KEY_MONITORING_ENABLED = "monitoring_enabled";
    
    private static final long CHECK_INTERVAL_MS = 30000; // Check every 30 seconds
    private static final long RESTART_DELAY_MS = 5000; // Wait 5 seconds before restart
    
    private PowerManager.WakeLock wakeLock;
    private ScheduledExecutorService scheduler;
    private Handler handler;
    private SharedPreferences prefs;
    private boolean isRunning = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "KeepAliveService created");
        
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        handler = new Handler();
        
        // Acquire wake lock to prevent device from sleeping
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SmsForwarder::KeepAliveWakeLock");
        
        createNotificationChannel();
        startPeriodicChecks();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "KeepAliveService started");
        
        if (!isRunning) {
            isRunning = true;
            
            // Acquire wake lock
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
            
            // Start foreground service
            Notification notification = createNotification();
            startForeground(NOTIFICATION_ID, notification);
            
            // Schedule periodic checks
            schedulePeriodicRestart();
        }
        
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "KeepAliveService destroyed - attempting to restart");
        
        // Release wake lock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        
        // Stop scheduler
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        
        isRunning = false;
        
        // Schedule restart
        scheduleRestart();
        
        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Keep Alive Service",
                    NotificationManager.IMPORTANCE_MIN
            );
            channel.setDescription("Keeps SMS forwarding service alive");
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
                .setContentText("Monitoring service active")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();
    }
    
    private void startPeriodicChecks() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            boolean monitoringEnabled = prefs.getBoolean(KEY_MONITORING_ENABLED, false);
            
            if (monitoringEnabled && !isMonitoringServiceRunning()) {
                Log.d(TAG, "SMS Monitoring service not running, restarting...");
                restartMonitoringService();
            }
        }, 0, CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }
    
    private boolean isMonitoringServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) return false;
        
        List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);
        
        for (ActivityManager.RunningServiceInfo service : services) {
            if (SmsMonitoringService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    
    private void restartMonitoringService() {
        handler.postDelayed(() -> {
            try {
                Intent serviceIntent = new Intent(this, SmsMonitoringService.class);
                startForegroundService(serviceIntent);
                Log.d(TAG, "SMS Monitoring service restarted");
            } catch (Exception e) {
                Log.e(TAG, "Failed to restart monitoring service", e);
            }
        }, RESTART_DELAY_MS);
    }
    
    private void schedulePeriodicRestart() {
        // Schedule periodic restart using AlarmManager for better reliability
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(this, ServiceRestartReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                    PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT);
            
            // Set inexact repeating alarm to restart service periodically
            alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + CHECK_INTERVAL_MS,
                CHECK_INTERVAL_MS,
                pendingIntent
            );
        }
    }
    
    private void scheduleRestart() {
        // Schedule immediate restart
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(this, ServiceRestartReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                    PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT);
            
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + RESTART_DELAY_MS,
                pendingIntent
            );
        }
    }
}