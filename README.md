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

## Architecture

```
CONTEXTS → APPS → ACTIONS → WORKFLOWS → CAPSULES → RESTORATION → AUTOMATIONS → TRIGGERS
```

## Requirements

- JDK 17
- Android SDK 34
- Gradle 8.5 (via wrapper)

## Build

```bash
# Generate Gradle wrapper (if not present)
gradle wrapper --gradle-version 8.5

# Run tests
./gradlew test

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Build release AAB
./gradlew bundleRelease
```

### Build Artifacts

| Artifact | Path |
|----------|------|
| Debug APK | `app/build/outputs/apk/debug/` |
| Release APK | `app/build/outputs/apk/release/` |
| Release AAB | `app/build/outputs/bundle/release/` |

## GitHub Actions

### CI (android-ci.yml)

Triggers on push/PR:
- Runs unit tests
- Builds debug APK
- Builds release APK
- Builds release AAB
- Uploads artifacts

### Release (android-release.yml)

Triggers on version tag (`v*`):
- Runs tests
- Builds release APK/AAB
- Creates GitHub Release
- Attaches artifacts

### Creating a Release

```bash
git tag v1.0.0
git push origin v1.0.0
```

### Workflow Files

Due to GitHub App permission restrictions, workflow files must be added manually. Create `.github/workflows/android-ci.yml` and `.github/workflows/android-release.yml` with the content from the repository's `.github/release-template.md`.

To add workflow files:
1. Go to your repository on GitHub
2. Navigate to `.github/workflows/`
3. Create `android-ci.yml` with CI configuration
4. Create `android-release.yml` with release configuration
5. Commit and push

## Versioning

- `versionCode`: Integer, incremented for each release
- `versionName`: Semantic version (e.g., `1.0.0`)

## Signing

Release signing uses GitHub Secrets:
- `NEXUS_KEYSTORE_BASE64`: Base64-encoded keystore
- `NEXUS_KEYSTORE_PASSWORD`: Keystore password
- `NEXUS_KEY_ALIAS`: Key alias
- `NEXUS_KEY_PASSWORD`: Key password

For local builds, create `keystore.properties` in the project root:
```
storeFile=/path/to/keystore.jks
storePassword=your-password
keyAlias=your-alias
keyPassword=your-key-password
```

## Database

Room database version 8 with migration chain:
1→2→3→4→5→6→7→8

All migrations are non-destructive.

## License

Private — All rights reserved.
