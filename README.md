# mindYourself

A privacy-first reminder app for Android and Wear OS that helps build healthier habits through context-aware notifications. All data stays local — no cloud, no third-party sharing.

## Features

- **Movement reminders** — fires when step count drops below a threshold within a time window
- **Sedentary alerts** — triggers after continuous inactivity exceeding a configurable duration
- **Hydration tracking** — reminds when daily water intake goal is not on track
- **Supplement reminders** — time-based reminders for medications or supplements
- **Screen break reminders** — interval-based reminders to rest your eyes
- **Location filtering** — reminders can be restricted to specific places (WiFi SSID or cell tower fingerprint)
- **Vacation mode** — defined time periods during which only vacation-flagged reminders are shown
- **Watch-first** — all reminders fire on the watch; phone app is used purely for configuration and history

## Architecture

Multi-module Clean Architecture:

```
mindYourself/
├── app/            # Phone companion app (Jetpack Compose)
├── wear/           # Wear OS app (Compose for Wear OS)
└── shared/
    ├── domain/     # Pure Kotlin — models, repository interfaces
    ├── data/       # Room DB, repository implementations, DataStore
    └── sync/       # Wear Data Layer API wrapper, sync DTOs
```

### Data flow

```
[Health Services API]
    PassiveListenerService (wear)
        │  steps, activity state, heart rate
        ▼
[Room DB] ← reminder_configs (synced from phone)
        │
        ▼
[ReminderEvaluationWorker] — every 15 min (WorkManager)
        │  evaluates all enabled rules
        ▼
[NotificationDispatcher] → Wear OS notification with snooze action
```

```
[Phone App]
    User configures reminders
        │  tap "Sync to Watch"
        ▼
[PhoneSyncUseCase]
    WearDataClient (Wear Data Layer API)
        │
        ▼
[WatchSyncListenerService] → saves to Room DB on watch
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI (Phone) | Jetpack Compose + Material3 |
| UI (Watch) | Compose for Wear OS |
| Architecture | Clean Architecture, multi-module |
| DI | Hilt |
| Local storage | Room (SQLite) |
| Preferences | DataStore |
| Watch sensors | Health Services API (Passive Monitoring) |
| Watch ↔ Phone | Wear Data Layer API |
| Background work | WorkManager (CoroutineWorker) |

## Modules

### `:shared:domain`
Pure Kotlin, no Android framework dependencies. Contains:
- Domain models: `ReminderConfig`, `VacationSettings`, `NamedLocation`, `HydrationLog`, `SupplementLog`, …
- Repository interfaces

### `:shared:data`
Room database with DAOs and repository implementations. Also holds `VacationSettingsRepositoryImpl` (DataStore-backed). Includes Room schema migrations.

### `:shared:sync`
Wear Data Layer API wrapper (`WearDataClient`), sync DTOs, and manual JSON serialization. No third-party serialization library — keeps the module dependency-light.

### `:app` (Phone)
Jetpack Compose screens for reminder management, history, and settings (locations, vacation periods). Communicates with the watch via `PhoneSyncUseCase`.

### `:wear` (Watch)
Passive sensor monitoring, periodic reminder evaluation, notification dispatch. Offline-first: works without a phone connection once synced.

## Privacy

- All data is stored exclusively on-device (watch + phone)
- No analytics, no crash reporting, no network calls
- Location matching uses local WiFi/cell fingerprints — no GPS, no location services in the background

## Building

Requirements: Android Studio Hedgehog or newer, JDK 17.

```bash
# Debug build (phone)
./gradlew :app:assembleDebug

# Debug build (watch)
./gradlew :wear:assembleDebug
```

The project uses a Gradle Version Catalog (`gradle/libs.versions.toml`) and a `build-logic` module for shared Gradle convention plugins.

## Status

Active development — V1 targeting personal use. Core reminder engine and watch UI are functional. SQLCipher encryption for sensitive tables is planned but not yet implemented.
