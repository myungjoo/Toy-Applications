package com.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootReceiver";
    private static final String PREFS_NAME = "SmsForwarderPrefs";
    private static final String KEY_MONITORING_ENABLED = "monitoring_enabled";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        Log.d(TAG, "Boot receiver triggered with action: " + action);
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action) ||
            Intent.ACTION_QUICKBOOT_POWERON.equals(action) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) ||
            Intent.ACTION_PACKAGE_REPLACED.equals(action) ||
            Intent.ACTION_REBOOT.equals(action)) {
            
            Log.d(TAG, "Boot completed or package replaced, checking if monitoring should be started");
            
            // Check if monitoring was enabled before reboot
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean monitoringEnabled = prefs.getBoolean(KEY_MONITORING_ENABLED, false);
            
            if (monitoringEnabled) {
                Log.d(TAG, "Starting SMS monitoring and keep-alive services on boot");
                
                // Use a handler to delay the start slightly to ensure system is ready
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    try {
                        // Start SMS monitoring service
                        Intent serviceIntent = new Intent(context, SmsMonitoringService.class);
                        context.startForegroundService(serviceIntent);
                        
                        // Start keep-alive service
                        Intent keepAliveIntent = new Intent(context, KeepAliveService.class);
                        context.startForegroundService(keepAliveIntent);
                        
                        Log.d(TAG, "Services started successfully");
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to start services on boot", e);
                        
                        // Try again after a longer delay
                        handler.postDelayed(() -> {
                            try {
                                Intent serviceIntent = new Intent(context, SmsMonitoringService.class);
                                context.startForegroundService(serviceIntent);
                                
                                Intent keepAliveIntent = new Intent(context, KeepAliveService.class);
                                context.startForegroundService(keepAliveIntent);
                                
                                Log.d(TAG, "Services started successfully on retry");
                            } catch (Exception e2) {
                                Log.e(TAG, "Failed to start services on retry", e2);
                            }
                        }, 10000); // 10 seconds delay
                    }
                }, 5000); // 5 seconds delay
            }
        }
    }
}