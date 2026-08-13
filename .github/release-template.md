## NEXUS v{VERSION}

### Highlights
- Contextual control layer for Android
- Persistent Context Engine with Room database
- Android app integration and discovery
- Action Engine with workflow execution
- Capsule Engine for workspace snapshots
- Capsule restoration
- Automation Engine with 28 trigger types
- Environment event sources (Wi-Fi, Bluetooth, Battery, NFC, Calendar, etc.)
- Smart automation with health monitoring and pattern analysis

### Features
- Context management (create, edit, delete, activate)
- App discovery and context-app associations
- Action creation and workflow execution
- Capsule capture and restoration
- Automation rules with cooldown and rate limiting
- Environment event sources
- Smart conditions and composite triggers
- Automation templates
- Event diagnostics and privacy controls

### Performance
- Room database with indexed queries
- Flow-based reactive UI updates
- WorkManager for background scheduling
- Event-driven architecture
- Rate limiting and cooldown protection

### Security
- Local-first architecture
- No cloud backend
- No telemetry
- No credential storage
- Privacy-first design

### Known Limitations
- Build verification requires JDK 17 and Android SDK 34
- Some features require Android 10+ (API 29+)
- NFC requires device hardware support
- Location features require runtime permissions

### Installation
1. Download the APK from the release assets
2. Enable "Install from unknown sources" in Android settings
3. Install the APK
4. Launch NEXUS

### Verification
- SHA-256 checksum of APK is available in the release assets
- All tests pass in CI before release
