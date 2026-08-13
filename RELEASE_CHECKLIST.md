# NEXUS Release Checklist

## Pre-Release

- [ ] All unit tests pass: `./gradlew test`
- [ ] Debug APK builds: `./gradlew assembleDebug`
- [ ] Release APK builds: `./gradlew assembleRelease`
- [ ] Release AAB builds: `./gradlew bundleRelease`
- [ ] Room migration chain verified (1→2→3→4→5→6→7→8)
- [ ] No destructive migration in production code
- [ ] Manifest permissions audited
- [ ] ProGuard rules verified
- [ ] No secrets committed to repository
- [ ] No debug-only code in release build
- [ ] Version code and name updated
- [ ] README.md updated
- [ ] All strings are accessible
- [ ] Dark/light theme works
- [ ] Navigation works correctly

## Build Verification

- [ ] Gradle wrapper is functional
- [ ] JDK 17 is available
- [ ] Android SDK 34 is available
- [ ] `./gradlew clean` succeeds
- [ ] `./gradlew test` passes
- [ ] `./gradlew assembleDebug` produces APK
- [ ] `./gradlew assembleRelease` produces APK
- [ ] `./gradlew bundleRelease` produces AAB
- [ ] APK is installable on device
- [ ] App launches without crash

## GitHub Actions

- [ ] CI workflow triggers on push/PR
- [ ] CI workflow runs tests
- [ ] CI workflow builds debug APK
- [ ] CI workflow uploads artifacts
- [ ] Release workflow triggers on tag push
- [ ] Release workflow builds release APK/AAB
- [ ] Release workflow creates GitHub Release
- [ ] Release workflow attaches artifacts

## Security

- [ ] No API keys in repository
- [ ] No passwords in repository
- [ ] No keystores in repository
- [ ] No debug credentials
- [ ] No hardcoded secrets
- [ ] `.gitignore` excludes sensitive files

## Database

- [ ] Migration chain complete: 1→2→3→4→5→6→7→8
- [ ] All entities registered in database
- [ ] All DAOs registered
- [ ] Foreign keys correct
- [ ] CASCADE behavior correct
- [ ] Indexes exist
- [ ] Unique constraints correct

## Automation Safety

- [ ] Cooldown works
- [ ] Duplicate events deduplicated
- [ ] Rate limiting enforced
- [ ] Loop protection active
- [ ] WorkManager retries bounded
- [ ] App launch actions safe
- [ ] URL actions validated
- [ ] Missing apps handled gracefully

## Final Sign-Off

- [ ] All checklist items above are PASS
- [ ] Version tag created: `git tag v1.0.0`
- [ ] Tag pushed: `git push origin v1.0.0`
- [ ] GitHub Release created with artifacts
- [ ] APK tested on physical device (if available)
