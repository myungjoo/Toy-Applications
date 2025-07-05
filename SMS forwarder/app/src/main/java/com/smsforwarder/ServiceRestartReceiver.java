package com.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class ServiceRestartReceiver extends BroadcastReceiver {
    
    private static final String TAG = "ServiceRestartReceiver";
    private static final String PREFS_NAME = "SmsForwarderPrefs";
    private static final String KEY_MONITORING_ENABLED = "monitoring_enabled";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Service restart receiver triggered");
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean monitoringEnabled = prefs.getBoolean(KEY_MONITORING_ENABLED, false);
        
        if (monitoringEnabled) {
            Log.d(TAG, "Restarting SMS monitoring service");
            
            // Start the monitoring service
            Intent serviceIntent = new Intent(context, SmsMonitoringService.class);
            try {
                context.startForegroundService(serviceIntent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to start monitoring service", e);
            }
            
            // Start the keep-alive service
            Intent keepAliveIntent = new Intent(context, KeepAliveService.class);
            try {
                context.startForegroundService(keepAliveIntent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to start keep-alive service", e);
            }
        }
    }
}