# RoundForge — Martial Arts Round Timer

A native Android round timer for boxing, kickboxing, MMA, and interval training, built with Kotlin and Jetpack Compose.

## Current features
- Dark, minimal training-focused interface with animated round progress.
- Configurable round count (1–20), round duration, and rest duration.
- Start, pause, resume, and reset controls.
- Audible cues for phase changes, final countdown warning, and workout completion.
- Sound cues can be enabled or disabled.

## Build an APK
Every push to `main` runs the Android GitHub Actions workflow. Successful builds upload an APK artifact to the workflow run. Push a version tag such as `v0.1.0` to create a GitHub Release with the APK attached.

- [Latest workflow runs](https://github.com/Suushian/Test/actions)
- [Releases](https://github.com/Suushian/Test/releases)
- [Project roadmap](PROJECT_PLAN.md)

## Local build
Requires JDK 17, Android SDK platform 35, build-tools 35.0.0, and Gradle 8.9.

```bash
gradle --no-daemon assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Signing
The current CI workflow produces a debug-signed APK suitable for testing. Configure protected signing secrets and a release-signing step before distributing a production build. Never commit keystores or passwords to the repository.
