#!/bin/bash
# NEXUS Development Setup Script
# Generates Gradle wrapper if missing, then builds the project.

set -e

echo "=== NEXUS Development Setup ==="

# Check Java
if ! command -v java &> /dev/null; then
    echo "ERROR: Java 17+ required but not found."
    echo "Install JDK 17: https://adoptium.net/"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "ERROR: JDK 17+ required. Found: $JAVA_VERSION"
    exit 1
fi
echo "✓ JDK $JAVA_VERSION found"

# Generate Gradle wrapper if missing
if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then
    echo "Gradle wrapper JAR missing — generating..."
    if command -v gradle &> /dev/null; then
        gradle wrapper --gradle-version 8.5
        echo "✓ Gradle wrapper generated"
    else
        echo "ERROR: Gradle not found and wrapper JAR is missing."
        echo "Install Gradle or clone the repository with the wrapper JAR."
        echo ""
        echo "To install Gradle:"
        echo "  brew install gradle        # macOS"
        echo "  sdk install gradle 8.5     # SDKMAN"
        echo "  apt install gradle         # Debian/Ubuntu"
        exit 1
    fi
else
    echo "✓ Gradle wrapper exists"
fi

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
echo "  Debug APK:   app/build/outputs/apk/debug/"
echo ""
echo "To build release: ./gradlew assembleRelease"
echo "To build AAB:     ./gradlew bundleRelease"
