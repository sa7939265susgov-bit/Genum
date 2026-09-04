# Gennum - Phone Number Generator & Active Call Simulator

**Gennum** is an Android application built with Jetpack Compose and Material Design 3. It generates realistic phone numbers for telecom carriers across multiple countries (Bahrain, Saudi Arabia, UAE, Kuwait, Qatar, Oman, UK, US) and features custom audio playback, WhatsApp direct dispatch with audio/video links, physical phone dialer delivery, and connected app integration.

---

## Key Features

- **Country & Carrier Number Generation**:
  - Realistic prefix rules and total digit lengths for Bahrain (Batelco, Zain, stc), GCC, UK, and US.
  - Generates realistic MSISDN formatting with country codes.
- **WhatsApp Generator (Numbers Only)**:
  - Dedicated **"WhatsApp Generator Numbers Only"** mode.
  - Automatically filters out fixed/landline prefixes (e.g. Bahrain `17`) to ensure generated numbers are mobile and WhatsApp-compatible.
- **Audio & Video Call Links Hub**:
  - Custom input fields to write your voice/audio link (with presets for delivery audio notes, verification OTPs, greetings).
  - Video call room link generator (Google Meet, Zoom, Jitsi WebRTC).
  - One-tap **"Take Me to WhatsApp & Start Call"** dispatches the number, message, and audio/video links directly into WhatsApp.
- **Active Call Dialog Simulation**:
  - Full-screen interactive in-call simulator with carrier branding, live duration timer, mute, speaker, dialpad, and video app switcher.
- **Physical Phone Ring Assistant**:
  - Hands-free countdown assistant that dials the generated number on your actual device.
- **Direct APK Installation Pop-Out**:
  - In-app installer banner and dialog to trigger Android's native package installer pop-out directly without typing or manual searching.

---

## Tech Stack & Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Kotlin Coroutines & StateFlow
- **Audio Engine**: Android `MediaPlayer` & `AudioRecord` with 3-second delivery ringtone simulation
- **Navigation & Storage**: Clean local state persistence, `FileProvider` for APK installation, Android Intent protocols for WhatsApp, Meet, Zoom, and system dialer.

---

## Building from Source

To compile and build the debug APK:

```bash
gradle :app:assembleDebug
```

The APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

To run unit tests:

```bash
gradle :app:testDebugUnitTest
```
