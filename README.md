# mindYourself

A privacy-first reminder app for Android and Wear OS that helps build healthier habits through context-aware notifications. All data stays local — no cloud, no third-party sharing.

## Features

- **Supplement reminders** — per-location, time-based reminders for supplements and medications with configurable items (capsules, drops, pills) and snooze support
- **Hydration tracking** — reminds when daily water intake goal is not on track; syncs with Android Health Connect to share data with other health apps; daily goal is computed on the phone from all active hydration reminders and synced to the watch
- **Movement reminders** — fires every N minutes within a defined time window when step count in the last Z minutes falls below a configurable threshold; notification shows current steps vs. threshold and auto-dismisses after 5 seconds
- **Sedentary alerts** — triggers after continuous inactivity exceeding a configurable duration
- **Screen break reminders** — interval-based reminders to rest your eyes
- **Location filtering** — reminders can be restricted to specific named places (WiFi SSID or cell tower fingerprint)
- **Vacation mode** — defined time periods during which only vacation-flagged reminders are shown
- **Full backup & restore** — exports all reminders, locations, vacation periods, hydration portion sizes, and app settings as JSON; import restores all data without touching unrelated settings
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
[Room DB] ← reminder_configs, app_settings (synced from phone)
        │
        ▼
[ReminderEvaluationWorker] — every 15 min (WorkManager)
        │  evaluates all enabled rules
        ▼
[NotificationDispatcher] → Wear OS notification (auto-dismisses after 5 s)
```

```
[Phone App]
    User configures reminders
        │  tap "Sync to Watch"
        ▼
[PhoneSyncUseCase]
    ├── reminder_configs
    ├── vacation_settings
    ├── app_settings (incl. computed hydrationDailyGoalMl)
    └── hydration_hc_logs (from Health Connect)
    WearDataClient (Wear Data Layer API)
        │
        ▼
[WatchSyncListenerService] → saves to Room DB / DataStore on watch
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
| Health Integration | Android Health Connect API (hydration sync) |
| Design System | Material 2.5 (Material 3 Expressive migration path available) |
| Design Tools | Figma (MCP Server for design integration) |

## Modules

### `:shared:domain`
Pure Kotlin, no Android framework dependencies. Contains:
- Domain models: `ReminderConfig`, `VacationSettings`, `NamedLocation`, `HydrationLog`, `SupplementLog`, `AppSettings`, …
- Repository interfaces

### `:shared:data`
Room database with DAOs and repository implementations. Also holds `VacationSettingsRepositoryImpl` and `AppSettingsRepositoryImpl` (DataStore-backed). Includes Room schema migrations and `BackupRepository` for full JSON backup/restore.

### `:shared:sync`
Wear Data Layer API wrapper (`WearDataClient`), sync DTOs, and manual JSON serialization. No third-party serialization library — keeps the module dependency-light.

### `:app` (Phone)
Jetpack Compose screens for reminder management, history, settings (locations, vacation periods, step goal), and backup/restore. Communicates with the watch via `PhoneSyncUseCase`.

### `:wear` (Watch)
Passive sensor monitoring, periodic reminder evaluation (today-scoped hydration), notification dispatch. Offline-first: works without a phone connection once synced. Receives Health Connect hydration logs and app settings from phone via Wear Data Layer.

## Hydration Goal Sync

The hydration daily goal displayed on the watch is **computed on the phone** from all enabled hydration reminder configs (`sum of reminderGoalMl`) and pushed as `hydrationDailyGoalMl` inside `AppSettingsDto` during each sync. The watch uses this synced value; if no sync has occurred yet, it falls back to summing locally. This ensures the goal is always consistent with the user's configuration and cannot silently fall back to a default.

## Privacy

- All data is stored exclusively on-device (watch + phone)
- No analytics, no crash reporting, no network calls
- Location matching uses local WiFi/cell fingerprints — no GPS, no location services in the background

## Building

Requirements: Android Studio Hedgehog or newer, JDK 17.

```bash
# Debug build (phone + watch)
./gradlew :app:assembleDebug :wear:assembleDebug
```

The project uses a Gradle Version Catalog (`gradle/libs.versions.toml`) and a `build-logic` module for shared Gradle convention plugins.

## Health Connect Integration

**V1.5.0+** includes Android Health Connect sync:

- Watch hydration logs are written to Health Connect (shared with other health apps)
- External hydration data from other apps/devices in Health Connect syncs back to the watch
- Reminder evaluation uses `getTodayTotalMl()` against the synced `hydrationDailyGoalMl` goal
- Idempotent upserts via `clientRecordId` prevent duplicate entries on repeated syncs
- Full read fallback when changelog tokens expire

## Backup & Restore

The JSON backup (Settings → Export) covers:

| Key | Content |
|-----|---------|
| `reminder_configs` | All reminders with type config, schedule, location filter |
| `locations` | Named places with WiFi SSID and cell IDs |
| `vacation_periods` | Vacation ranges |
| `hydration_portion_sizes` | Quick-log amounts |
| `step_daily_goal` | Daily step goal |

Import merges via replace-all for DB tables and `copy()` for DataStore — only the fields present in the file are overwritten.

## Design & Documentation

Official Android Developer Wear OS design docs are stored in `docs/design/wear/`:

- **`material-2.5/`** — Current design system for mindYourself
- **`material-3-expressive/`** — Future migration target available

Design assets and prototypes managed in Figma with MCP Server integration (configured in `mcp.json`).

## Status

Active development — v1.5.11. Core reminder engine, watch UI, hydration sync, supplement reminders with location filtering, and full backup/restore are functional. SQLCipher encryption for sensitive tables is planned but not yet implemented.
