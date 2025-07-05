# SMS Forwarder - Background Persistence Guide

## Overview

The SMS forwarder app has been enhanced with multiple mechanisms to ensure it runs permanently in the background without being killed by Android's aggressive battery optimization and background restrictions.

## Key Improvements Made

### 1. Enhanced Manifest Configuration
- **Special Use Foreground Service**: Changed from `connectedDevice` to `specialUse` type for better Android 14+ compatibility
- **Enhanced Boot Receivers**: Added support for more boot scenarios including QuickBoot, Direct Boot, and Package Replace
- **Battery Optimization Permissions**: Added `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` permission
- **Wake Lock Permissions**: Added proper wake lock permissions to prevent device sleep
- **Service Persistence**: Added `android:stopWithTask="false"` to prevent service termination

### 2. Multi-Layer Service Architecture

#### **SMS Monitoring Service (Primary)**
- Runs as a foreground service with persistent notification
- Implements heartbeat mechanism to stay alive
- Uses wake locks to prevent device sleep
- Automatically restarts if killed

#### **Keep-Alive Service (Guardian)**
- Monitors the SMS Monitoring Service
- Automatically restarts the main service if it dies
- Uses AlarmManager for reliable scheduling
- Implements multiple restart mechanisms

#### **Service Restart Receiver (Recovery)**
- Handles service restart events
- Responds to system broadcasts
- Ensures services start after device reboot
- Implements retry logic with delays

### 3. Advanced Restart Mechanisms

#### **Multiple Restart Triggers**
- `START_STICKY` service flag for automatic restart
- AlarmManager-based periodic checks
- Boot receiver for post-reboot startup
- Task removal handling
- System broadcast receivers

#### **Robust Boot Handling**
- Supports multiple boot scenarios:
  - `ACTION_BOOT_COMPLETED` - Standard boot
  - `ACTION_LOCKED_BOOT_COMPLETED` - Direct boot
  - `ACTION_QUICKBOOT_POWERON` - Quick boot
  - `ACTION_REBOOT` - System reboot
  - `ACTION_PACKAGE_REPLACED` - App updates

### 4. Battery Optimization Handling

#### **Automatic Request**
- App automatically requests battery optimization exclusion
- Shows explanatory dialog to users
- Provides direct link to battery settings
- Handles both automatic and manual exclusion

#### **Wake Lock Management**
- Acquires partial wake locks to prevent sleep
- Properly releases locks to prevent battery drain
- Uses tagged wake locks for debugging
- Handles wake lock exceptions gracefully

### 5. Enhanced Error Handling

#### **Comprehensive Logging**
- Detailed logs for all service operations
- Error tracking and recovery logging
- Performance monitoring
- Debug information for troubleshooting

#### **Exception Handling**
- Try-catch blocks around all critical operations
- Graceful degradation when services fail
- Automatic retry mechanisms
- Fallback options for failed operations

## Installation and Setup

### 1. Install the APK
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 2. Grant Permissions
The app will automatically request these permissions:
- **SMS Permissions**: RECEIVE_SMS, READ_SMS, SEND_SMS
- **Battery Optimization**: Exclusion from battery optimization
- **Background Execution**: Foreground service permissions

### 3. Configure Battery Optimization
1. Open the app
2. If prompted, tap "Open Settings" for battery optimization
3. Find "SMS Forwarder" in the battery optimization list
4. Select "Don't optimize"
5. Confirm the selection

### 4. Additional Manual Configuration (Optional)

#### **Disable Doze Mode** (Android 6.0+)
```bash
adb shell dumpsys deviceidle whitelist +com.smsforwarder
```

#### **Disable App Standby** (Android 6.0+)
```bash
adb shell am set-inactive com.smsforwarder false
```

#### **Enable Auto-Start** (Manufacturer-specific)
- **Xiaomi**: Security → Manage Apps → SMS Forwarder → Auto-start: ON
- **Huawei**: Phone Manager → Protected Apps → SMS Forwarder: ON
- **Samsung**: Device Care → Battery → App Power Management → SMS Forwarder: Disabled
- **OnePlus**: Settings → Battery → Battery Optimization → SMS Forwarder: Don't optimize

## How It Works

### 1. Service Lifecycle
```
User Enables Monitoring
    ↓
SMS Monitoring Service Starts
    ↓
Keep-Alive Service Starts
    ↓
Both Services Run in Foreground
    ↓
If Service Dies → Automatic Restart
    ↓
If Device Reboots → Boot Receiver Restarts Services
```

### 2. Monitoring Mechanism
- **Heartbeat**: Service sends heartbeat every 10 seconds
- **Health Check**: Keep-alive service checks main service every 30 seconds
- **Recovery**: If main service dies, keep-alive restarts it within 5 seconds
- **Persistence**: AlarmManager ensures services restart even if both die

### 3. Notification System
- **Primary Notification**: Shows SMS monitoring status
- **Keep-Alive Notification**: Minimal notification for guardian service
- **Processing Notification**: Temporary notification during SMS forwarding
- **Silent Notifications**: All notifications are silent to avoid user disturbance

## Troubleshooting

### 1. App Still Getting Killed
**Check these settings:**
- Battery optimization is disabled for the app
- Auto-start is enabled (manufacturer-specific)
- App is not in any power-saving mode
- Recent apps list doesn't show the app (it should run in background)

### 2. Services Not Starting After Reboot
**Check these items:**
- Boot receiver is enabled in manifest
- App has proper permissions
- Device allows background app refresh
- Storage space is sufficient

### 3. High Battery Usage
**Optimization tips:**
- The app uses minimal battery due to efficient wake lock management
- Battery usage is necessary for reliable operation
- Monitor battery stats to ensure normal consumption
- Check for excessive SMS traffic that might increase processing

### 4. Debugging Commands
```bash
# Check if services are running
adb shell dumpsys activity services | grep -i smsforwarder

# Check battery optimization status
adb shell dumpsys deviceidle whitelist | grep -i smsforwarder

# View app logs
adb logcat | grep -i "SmsForwarder\|SmsReceiver\|SmsMonitoringService\|KeepAliveService"

# Check foreground services
adb shell dumpsys activity services | grep -i foreground
```

## Advanced Configuration

### 1. Custom Restart Intervals
Modify these constants in the source code:
```java
// In KeepAliveService.java
private static final long CHECK_INTERVAL_MS = 30000; // Health check interval
private static final long RESTART_DELAY_MS = 5000;   // Restart delay

// In SmsMonitoringService.java
private static final long HEARTBEAT_INTERVAL = 10000; // Heartbeat interval
```

### 2. Custom Wake Lock Behavior
```java
// In KeepAliveService.java
// Change wake lock type for different behavior
wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SmsForwarder::KeepAliveWakeLock");
// Options: PARTIAL_WAKE_LOCK, SCREEN_DIM_WAKE_LOCK, SCREEN_BRIGHT_WAKE_LOCK
```

### 3. Custom Notification Behavior
```java
// In notification creation methods
.setPriority(NotificationCompat.PRIORITY_MIN)  // Change priority
.setSilent(true)                               // Enable/disable sound
.setOngoing(true)                              // Make persistent
```

## Performance Considerations

### 1. Battery Usage
- **Minimal Impact**: Efficient wake lock management
- **Necessary Trade-off**: Background persistence requires some battery usage
- **Optimized**: Services only active when needed

### 2. Memory Usage
- **Low Footprint**: Services use minimal memory
- **Efficient**: Proper cleanup and resource management
- **Monitored**: Automatic memory leak prevention

### 3. CPU Usage
- **Minimal Processing**: Services mostly idle
- **Event-Driven**: Only active during SMS processing
- **Optimized**: Efficient algorithms and data structures

## Security Considerations

### 1. Permissions
- **SMS Permissions**: Required for core functionality
- **Battery Permissions**: Required for background operation
- **Wake Lock**: Required for reliable operation

### 2. Data Privacy
- **Local Processing**: All data processed locally
- **No Network**: No internet connectivity required
- **Secure Storage**: Rules stored in encrypted database

### 3. Service Security
- **Non-Exported Services**: Services not accessible by other apps
- **Intent Validation**: All intents properly validated
- **Error Handling**: Secure error handling prevents information leakage

## Testing the Implementation

### 1. Basic Functionality Test
1. Install and configure the app
2. Send a test SMS matching a rule
3. Verify SMS is forwarded correctly
4. Check that services remain running

### 2. Persistence Test
1. Enable SMS monitoring
2. Force-kill the app using device settings
3. Wait 30 seconds
4. Check if services automatically restart
5. Send test SMS to verify functionality

### 3. Reboot Test
1. Enable SMS monitoring
2. Reboot the device
3. Check if services start automatically
4. Verify monitoring status in app
5. Test SMS forwarding functionality

### 4. Battery Optimization Test
1. Enable battery optimization for the app
2. Monitor if services get killed
3. Disable battery optimization
4. Verify services stay alive longer

## Success Indicators

### 1. Service Persistence
- Services appear in Android's running services list
- Persistent notifications remain visible
- Services restart automatically after being killed
- Services start automatically after device reboot

### 2. SMS Forwarding
- SMS messages are forwarded according to rules
- No missed messages during service restarts
- Forwarding works even when app is not visible
- Forwarding continues after device reboot

### 3. Battery Optimization
- App appears in battery optimization whitelist
- Services don't get killed by battery optimization
- Reasonable battery usage (not excessive)
- App works reliably over extended periods

## Conclusion

The enhanced SMS forwarder now implements multiple layers of protection against Android's background restrictions:

1. **Foreground Services**: Prevent system killing
2. **Wake Locks**: Prevent device sleep interference
3. **Battery Optimization**: Bypass aggressive power management
4. **Multiple Restart Mechanisms**: Ensure automatic recovery
5. **Comprehensive Error Handling**: Graceful failure recovery

This multi-layered approach ensures the SMS forwarder will run permanently in the background on modern Android devices, providing reliable SMS forwarding functionality without user intervention.