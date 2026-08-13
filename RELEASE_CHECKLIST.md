# NEXUS Release Checklist

## Repository
- [ ] Repository is clean (no uncommitted changes)
- [ ] All tests pass: `./gradlew test`
- [ ] Debug APK builds: `./gradlew assembleDebug`
- [ ] Release APK builds: `./gradlew assembleRelease`
- [ ] Release AAB builds: `./gradlew bundleRelease`
- [ ] Room migration chain verified (1→2→3→4→5→6→7→8)
- [ ] No destructive migration in production code
- [ ] No secrets committed to repository
- [ ] .gitignore excludes keystores and sensitive files

## Gradle
- [ ] Gradle wrapper is present and functional
- [ ] JDK 17 configured
- [ ] Android SDK 34 available
- [ ] `./gradlew clean` succeeds

## Build Verification
- [ ] Unit tests pass
- [ ] Debug APK generated and non-zero size
- [ ] Release APK generated and non-zero size
- [ ] Release AAB generated and non-zero size
- [ ] APK installs on device/emulator
- [ ] App launches without crash

## GitHub Actions
- [ ] CI workflow (`android-ci.yml`) exists and is valid
- [ ] Release workflow (`android-release.yml`) exists and is valid
- [ ] CI workflow triggers on push/PR
- [ ] Release workflow triggers on tag push
- [ ] Debug APK uploaded as artifact
- [ ] Release APK uploaded as artifact
- [ ] Release AAB uploaded as artifact

## Signing
- [ ] No keystore files committed
- [ ] No passwords committed
- [ ] GitHub Secrets configured for release signing (if applicable)
- [ ] Release APK is signed (if secrets configured) or unsigned (if not)

## Security
- [ ] No API keys in repository
- [ ] No tokens in repository
- [ ] No credentials in repository
- [ ] Sensitive files in .gitignore

## Final Sign-Off
- [ ] All checklist items above are PASS
- [ ] Version tag created: `git tag v1.0.0`
- [ ] Tag pushed: `git push origin v1.0.0`
- [ ] GitHub Release created with artifacts
- [ ] APK tested on physical device (if available)
