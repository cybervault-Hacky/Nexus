# NEXUS

A futuristic Android contextual control layer — local-first, privacy-first.

## Features

- **Context Engine**: Create and manage workflow contexts with apps and actions
- **App Integration**: Discover installed apps, associate with contexts
- **Action Engine**: Define reusable actions (open app, open URL, delay)
- **Workflow Execution**: Run action sequences with progress tracking
- **Capsule Engine**: Capture and restore context snapshots
- **Automation Engine**: 28 trigger types with cooldowns, rate limiting, and health monitoring
- **Environment Events**: Wi-Fi, Bluetooth, Battery, NFC, Calendar, Notifications
- **Smart Conditions**: Composite triggers with ALL/ANY/NOT operators
- **Privacy First**: All data local, no cloud, no telemetry

## Requirements

- JDK 17
- Android SDK 34
- Gradle 8.5 (via wrapper)

## Quick Start

```bash
# Clone
git clone <repository-url>
cd Nexus

# Generate Gradle wrapper (if missing)
gradle wrapper --gradle-version 8.5

# Build
./gradlew assembleDebug

# Run tests
./gradlew test
```

Or use the setup script:
```bash
chmod +x setup.sh
./setup.sh
```

## Build Commands

| Command | Output |
|---------|--------|
| `./gradlew test` | Run unit tests |
| `./gradlew assembleDebug` | Debug APK |
| `./gradlew assembleRelease` | Release APK |
| `./gradlew bundleRelease` | Release AAB |

### Build Artifacts

| Artifact | Path |
|----------|------|
| Debug APK | `app/build/outputs/apk/debug/app-debug.apk` |
| Release APK | `app/build/outputs/apk/release/app-release.apk` |
| Release AAB | `app/build/outputs/bundle/release/app-release.aab` |

## GitHub Actions

### CI Workflow (`android-ci.yml`)

Triggers on every push and PR:
- Sets up JDK 17
- Generates Gradle wrapper if missing
- Runs unit tests
- Builds debug APK
- Uploads APK as GitHub Actions artifact

### Release Workflow (`android-release.yml`)

Triggers on version tags (`v*`):
- Runs tests
- Builds release APK and AAB
- Creates GitHub Release with artifacts attached
- Optionally signs with GitHub Secrets

### Creating a Release

```bash
git tag v1.0.0
git push origin v1.0.0
```

## Release Signing

Release signing uses GitHub Secrets (never committed to repository):

| Secret | Description |
|--------|-------------|
| `NEXUS_KEYSTORE_BASE64` | Base64-encoded keystore file |
| `NEXUS_KEYSTORE_PASSWORD` | Keystore password |
| `NEXUS_KEY_ALIAS` | Key alias |
| `NEXUS_KEY_PASSWORD` | Key password |

For local builds, create `keystore.properties` in the project root:
```
storeFile=/path/to/keystore.jks
storePassword=your-password
keyAlias=your-alias
keyPassword=your-key-password
```

## Versioning

- `versionCode`: Integer, incremented for each release
- `versionName`: Semantic version (e.g., `1.0.0`)

## Database

Room database version 8 with non-destructive migration chain:
1→2→3→4→5→6→7→8

## License

Private — All rights reserved.
