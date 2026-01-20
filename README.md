# Task Stash — Recent Cleaner

## Overview
Task Stash includes an AccessibilityService that automatically clears the Android "Recents" / overview screen when it is opened. The service watches accessibility events and attempts to click the "Close all" / "Clear all" control so the user doesn't have to manually clear recent apps.

## Key features
- Automatically detects Recents/Overview on common launchers and system UIs (AOSP/SystemUI, Samsung).
- Attempts button click by matching localized text variants (string array) or by view id.
- Includes helpers for MIUI-specific layouts with a fallback tree-walk search.
- Lightweight: runs as an AccessibilityService and only acts when Recents is opened.

## How it works
The core logic lives in `app/src/main/java/com/task/stash/RecentCleanerService.kt`. When Recents opens the service:
- It checks the package/class for Recents activities.
- Finds possible "close all" controls by text using a localized string array (`R.array.close_all_variants`).
- Falls back to clicking a known view id (`com.android.systemui:id/button_clear_all`).
- Contains MIUI helper functions that attempt additional view-id or content-description matches.

## Permissions & setup
- Enable the app's Accessibility service in Settings → Accessibility → (this app) → turn on.
- No other special permissions are required, but Accessibility access is necessary for the clearing behavior to work.

## Build & install
From the project root on Windows:

```bash
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

Or on UNIX-like environments:

```bash
./gradlew assembleDebug
./gradlew installDebug
```

## Customization
- Edit the localized string array `close_all_variants` in `res/values/strings.xml` (and locale-specific folders) to add alternate phrases for the "Close all" button used on different devices or languages.
- The service uses view IDs as a fallback; adding more device-specific IDs in the code can improve compatibility.

## Limitations & privacy
- The app requires Accessibility access; users should review the implications and grant access knowingly.
- The service only performs simple UI interactions (button clicks). It does not collect or transmit user data.
- Currently, this app is only tested on SamSung device.

## Troubleshooting
- If clearing doesn't work on a device, check for different button text or view IDs for that device/vendor and add them to `close_all_variants` or adjust the code.
- Check log output (Logcat) under tag `MyAccessibilityService` for runtime errors and detection decisions.

## Where to look in the code
- Main logic: `app/src/main/java/com/task/stash/RecentCleanerService.kt`
- Resources: `app/src/main/res/values/strings.xml` (look for `close_all_variants`)

If you want, I can also add a short sample UI or instructions to automatically enable the service for testing devices. Want me to do that next?