# Martial Arts Round Timer — Project Roadmap

## Product direction
A native Android app built with Kotlin and Jetpack Compose, with a minimal dark-first interface, fluid timer animation, configurable round/rest intervals, audible phase cues, and reliable timing while the screen is on. GitHub Actions will build a debug APK for every main-branch push and create a versioned GitHub Release with an APK when a version tag is pushed.

## Phases

### Phase 1 — Foundation and build pipeline
- Establish the Android project structure and app identity.
- Configure Gradle, Android SDK targets, and a reproducible GitHub Actions build.
- Verify the APK build and artifact upload before adding complexity.

### Phase 2 — Timer engine
- Implement a monotonic-clock-based round/rest state machine.
- Support start, pause/resume, reset, round count, round duration, rest duration, and final-round completion.
- Handle app lifecycle transitions without timer drift.

### Phase 3 — UI and interaction
- Build a modern, minimal dark-first Compose interface.
- Add a large readable countdown, phase indicator, progress ring, round counter, and responsive controls.
- Add editable presets and custom round/rest settings.

### Phase 4 — Sound and workout reliability
- Add distinct start-round, rest, countdown-warning, and session-complete cues.
- Add haptic feedback where supported, audio preferences, and keep-screen-on behavior during active sessions.
- Test edge cases such as pausing near a phase boundary and finishing the last round.

### Phase 5 — Quality and accessibility
- Add unit tests for timer transitions and settings validation.
- Check small/large screens, contrast, touch targets, and system-bar behavior.
- Run CI builds and resolve build/test failures.

### Phase 6 — Release automation
- Build a release APK with GitHub Actions on version tags.
- Attach the APK to a GitHub Release and retain a CI artifact for ordinary builds.
- Document installation, release steps, and known limitations.

## Delivery criteria
- The app builds reproducibly in GitHub Actions.
- Users can configure rounds and rest periods, start/pause/resume/reset, and hear distinct cues.
- The final APK is downloadable from the repository's Releases page.

## Implementation notes
- Initial implementation targets Android with Kotlin + Jetpack Compose.
- Release signing will use GitHub Actions secrets if a private signing key is configured; otherwise, the workflow can publish an installable APK signed with the standard debug key for testing. A production signing key must never be committed to the repository.
