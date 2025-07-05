#!/bin/bash

# Enhanced SMS Forwarder - Build and Install Script
# This script builds the APK and installs it with proper configuration

echo "🔧 Building Enhanced SMS Forwarder..."
echo "=====================================\n"

# Check if Android SDK is available
if ! command -v adb &> /dev/null; then
    echo "❌ Error: ADB not found. Please install Android SDK and add it to PATH."
    exit 1
fi

# Check if device is connected
if ! adb devices | grep -q "device"; then
    echo "❌ Error: No Android device connected. Please connect your device and enable USB debugging."
    exit 1
fi

# Build the APK
echo "📦 Building APK..."
if ./gradlew assembleDebug; then
    echo "✅ APK built successfully!"
else
    echo "❌ Error: Failed to build APK."
    exit 1
fi

# Install the APK
echo "\n📱 Installing APK..."
if adb install -r app/build/outputs/apk/debug/app-debug.apk; then
    echo "✅ APK installed successfully!"
else
    echo "❌ Error: Failed to install APK."
    exit 1
fi

# Launch the app
echo "\n🚀 Launching SMS Forwarder..."
adb shell am start -n com.smsforwarder/.MainActivity

echo "\n🎉 Installation Complete!"
echo "========================="
echo ""
echo "📋 Next Steps:"
echo "1. Grant SMS permissions when prompted"
echo "2. Disable battery optimization when prompted"
echo "3. Add your forwarding rules"
echo "4. Enable SMS monitoring"
echo ""
echo "🔍 Verification Commands:"
echo "• Check if services are running:"
echo "  adb shell dumpsys activity services | grep -i smsforwarder"
echo ""
echo "• View app logs:"
echo "  adb logcat | grep -i 'SmsForwarder\\|SmsReceiver\\|SmsMonitoringService\\|KeepAliveService'"
echo ""
echo "• Check battery optimization status:"
echo "  adb shell dumpsys deviceidle whitelist | grep -i smsforwarder"
echo ""
echo "📖 For detailed configuration, see BACKGROUND_PERSISTENCE_GUIDE.md"
echo ""
echo "⚠️  Important: Make sure to disable battery optimization for the app to ensure"
echo "   it runs permanently in the background without being killed by Android."