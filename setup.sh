#!/bin/bash
# NEXUS Development Setup Script

set -e

echo "=== NEXUS Development Setup ==="

# Check Java
if ! command -v java &> /dev/null; then
    echo "ERROR: Java not found. Install JDK 17."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "ERROR: JDK 17+ required. Found: $JAVA_VERSION"
    exit 1
fi
echo "✓ JDK $JAVA_VERSION found"

# Check Android SDK
if [ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ]; then
    echo "WARNING: ANDROID_HOME/ANDROID_SDK_ROOT not set."
    echo "  Set it to your Android SDK path."
fi
echo "✓ Android SDK check passed"

# Generate Gradle wrapper if missing
if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then
    echo "Generating Gradle wrapper..."
    if command -v gradle &> /dev/null; then
        gradle wrapper --gradle-version 8.5
        echo "✓ Gradle wrapper generated"
    else
        echo "WARNING: Gradle not found. Install Gradle or run 'gradle wrapper' manually."
    fi
else
    echo "✓ Gradle wrapper exists"
fi

# Make gradlew executable
chmod +x gradlew
echo "✓ gradlew is executable"

# Build
echo ""
echo "=== Building NEXUS ==="
./gradlew clean test assembleDebug --no-daemon
echo ""
echo "✓ Build complete!"
echo ""
echo "Artifacts:"
echo "  Debug APK: app/build/outputs/apk/debug/"
echo ""
echo "To build release: ./gradlew assembleRelease"
echo "To build AAB:     ./gradlew bundleRelease"
