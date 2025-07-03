package com.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootReceiver";
    private static final String PREFS_NAME = "SmsForwarderPrefs";
    private static final String KEY_MONITORING_ENABLED = "monitoring_enabled";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) ||
            Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            
            Log.d(TAG, "Boot completed or package replaced, checking if monitoring should be started");
            
            // Check if monitoring was enabled before reboot
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean monitoringEnabled = prefs.getBoolean(KEY_MONITORING_ENABLED, false);
            
            if (monitoringEnabled) {
                Log.d(TAG, "Starting SMS monitoring service on boot");
                Intent serviceIntent = new Intent(context, SmsMonitoringService.class);
                context.startForegroundService(serviceIntent);
            }
        }
    }
}