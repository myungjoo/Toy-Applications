# SMS Forwarder Android Application

An Android application that monitors incoming SMS messages and forwards them to configured phone numbers based on customizable rules. This app works in the background and continues monitoring even after the device restarts.

## Features

- **Background SMS Monitoring**: Continuously monitors incoming SMS messages whether the app is in foreground, background, or closed
- **Persistent Operation**: Automatically starts monitoring after device reboot
- **Flexible Filtering Rules**: Configure forwarding rules based on:
  - Sender phone number (partial or exact match)
  - Message content (partial string matching)
- **Rule Management**: Easy-to-use interface for adding, editing, and deleting forwarding rules
- **Comprehensive SMS Support**: Monitors all messages in the default SMS app, including those from banking/credit card companies that appear as SMS
- **Permission Management**: Built-in permission request and management system

## System Requirements

- Android 7.0 (API level 24) or higher
- SMS permissions (automatically requested by the app)
- Storage space for the application and database

## Installation

### Building from Source

1. **Prerequisites**:
   - Android Studio Arctic Fox or later
   - Android SDK with API level 34
   - Java 8 or higher

2. **Download Gradle Wrapper** (required for building):
   ```bash
   # Download the gradle wrapper jar manually and place it in gradle/wrapper/
   # You can get it from: https://gradle.org/releases/
   ```

3. **Build the APK**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on device**:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Direct Installation
Install the APK file on your Android device by enabling "Unknown Sources" in security settings.

## Setup and Configuration

### Initial Setup

1. **Launch the app** and grant required permissions:
   - SMS Read permission
   - SMS Receive permission  
   - SMS Send permission

2. **Enable monitoring** by tapping the "Enable SMS Monitoring" button

3. **Add forwarding rules** using the floating action button (+)

### Creating Forwarding Rules

Each forwarding rule consists of:

- **Sender Number** (optional): 
  - Leave empty to match any sender
  - Choose "Partial Match" to match numbers containing your input
  - Choose "Exact Match" for precise number matching
  
- **Message Content** (optional):
  - Leave empty to match any message content
  - Enter keywords that should be present in the message
  - Always uses partial matching (case-insensitive)
  
- **Forward To Number** (required):
  - The phone number that will receive the forwarded SMS
  - Must be a valid phone number

### Example Rules

1. **Forward all messages from your bank**:
   - Sender Number: "BANK" (partial match)
   - Message Content: (empty)
   - Forward To: "+1234567890"

2. **Forward security alerts**:
   - Sender Number: (empty)
   - Message Content: "security alert"
   - Forward To: "+0987654321"

3. **Forward from specific number**:
   - Sender Number: "+1122334455" (exact match)
   - Message Content: (empty)
   - Forward To: "+1111222333"

## Usage

### Managing Rules
- **View Rules**: All configured rules are displayed on the main screen
- **Edit Rule**: Tap the "Edit" button on any rule card
- **Delete Rule**: Tap the "Delete" button and confirm

### Monitoring Control
- **Enable/Disable**: Use the toggle button to start/stop SMS monitoring
- **Status Check**: The app shows current monitoring status on the main screen
- **Persistent Operation**: Once enabled, monitoring continues until manually disabled

### Background Operation
The app runs a foreground service to ensure continuous operation:
- Shows a persistent notification while monitoring
- Automatically restarts after device reboot (if previously enabled)
- Continues working even when the app is closed

## Troubleshooting

### Common Issues

1. **SMS not being forwarded**:
   - Check if all required permissions are granted
   - Verify that monitoring is enabled
   - Ensure forwarding rules are correctly configured
   - Check if the target phone number is valid

2. **App stops working after reboot**:
   - The app should automatically restart - check if monitoring is enabled
   - Manually open the app and verify the monitoring status

3. **Permission errors**:
   - Go to Android Settings > Apps > SMS Forwarder > Permissions
   - Enable all SMS-related permissions
   - Restart the app

4. **Battery optimization issues**:
   - Add the app to battery optimization whitelist
   - Go to Settings > Battery > Battery Optimization > SMS Forwarder > Don't optimize

### Logs and Debugging
The app logs all SMS processing activities. Use logcat to view logs:
```bash
adb logcat | grep "SmsForwarder\|SmsReceiver\|SmsMonitoringService"
```

## Security and Privacy

- **Local Operation**: All processing happens locally on your device
- **No Internet Required**: The app doesn't need internet connectivity
- **Data Storage**: Forwarding rules are stored in a local SQLite database
- **Message Content**: Original message content is forwarded with sender information

## Technical Details

### Architecture
- **Room Database**: For storing forwarding rules
- **BroadcastReceiver**: For intercepting SMS messages
- **Foreground Service**: For persistent background monitoring
- **RecyclerView**: For displaying rules in a modern UI

### Permissions Used
- `RECEIVE_SMS`: To intercept incoming SMS messages
- `READ_SMS`: To read SMS content and metadata
- `SEND_SMS`: To forward messages to configured numbers
- `WAKE_LOCK`: To maintain service operation
- `RECEIVE_BOOT_COMPLETED`: To restart after device reboot
- `FOREGROUND_SERVICE`: For background operation

### File Structure
```
app/
├── src/main/
│   ├── java/com/smsforwarder/
│   │   ├── MainActivity.java              # Main UI activity
│   │   ├── ForwardingRule.java           # Database entity
│   │   ├── ForwardingRuleDao.java        # Database operations
│   │   ├── AppDatabase.java              # Room database
│   │   ├── SmsReceiver.java              # SMS interception
│   │   ├── SmsForwardingService.java     # SMS forwarding logic
│   │   ├── SmsMonitoringService.java     # Background monitoring
│   │   ├── BootReceiver.java             # Auto-start after reboot
│   │   └── ForwardingRuleAdapter.java    # UI adapter
│   ├── res/
│   │   ├── layout/                       # UI layouts
│   │   ├── values/                       # Strings, colors, styles
│   │   └── drawable/                     # App icons
│   └── AndroidManifest.xml               # App configuration
├── build.gradle                          # App dependencies
└── proguard-rules.pro                    # Code obfuscation rules
```

## Contributing

This is a toy application for educational purposes. Feel free to:
- Report bugs or issues
- Suggest new features
- Submit pull requests
- Use as a learning resource

## License

This project is created for educational purposes in the Toy-Applications repository.

## Disclaimer

- Use this application responsibly and in compliance with local laws
- Be aware of SMS forwarding costs from your mobile carrier
- Test thoroughly before using for important messages
- The authors are not responsible for any misuse or issues arising from the use of this application