# mindYourself – Architektur-Übersicht

## Modul-Struktur

```
mindYourself/
├── app/                        # Phone Companion App
│   └── src/main/kotlin/de/stroebele/mindyourself/
│       ├── ui/                 # Compose Screens (Konfiguration, Visualisierung)
│       ├── sync/               # WearableListenerService
│       └── di/                 # Hilt Module
│
├── wear/                       # Wear OS App
│   └── src/main/kotlin/de/stroebele/mindyourself/
│       ├── ui/                 # Compose for Wear OS Screens
│       ├── service/            # PassiveListenerService
│       ├── receiver/           # BootCompletedReceiver
│       ├── worker/             # ReminderEvaluationWorker (WorkManager)
│       ├── notification/       # NotificationDispatcher
│       └── di/                 # Hilt Module
│
├── shared/
│   ├── domain/                 # Kein Framework, pure Kotlin
│   │   └── src/main/kotlin/de/stroebele/mindyourself/domain/
│   │       ├── model/          # ReminderConfig, HydrationLog, SupplementLog, HeartRateEntry
│   │       ├── usecase/        # EvaluateReminderTrigger, LogHydration, etc.
│   │       └── repository/     # Interfaces (implementiert in :data)
│   │
│   ├── data/                   # Room DB + Repository-Implementierungen
│   │   └── src/main/kotlin/de/stroebele/mindyourself/data/
│   │       ├── db/             # AppDatabase, DAOs
│   │       └── repository/     # Concrete Repository Impl
│   │
│   └── sync/                   # Wear Data Layer API Wrapper
│       └── src/main/kotlin/de/stroebele/mindyourself/sync/
│           ├── WatchDataClient.kt
│           └── model/          # Sync-DTOs (serialisierbar)
│
├── build-logic/                # Gradle Convention Plugins
├── gradle/
│   └── libs.versions.toml      # Version Catalog
└── docs/
    └── architecture.md         # Diese Datei
```

## Datenfluss

```
[Health Services API]
    PassiveListenerService
        │ steps, UserActivityInfo, heart_rate
        ▼
[Room DB :shared:data]
    health_cache (30 Tage)
    reminder_configs (sync'd von Phone)
    hydration_logs
    supplement_logs
    heart_rate_cache
        │
        ▼
[ReminderEvaluationWorker :wear]
    evaluiert Trigger-Regeln
        │
        ▼
[NotificationDispatcher :wear]
    Vibration + Text + Action Buttons
        │ (Nutzer-Aktion: "Getrunken", "Snooze")
        ▼
[Room DB] ← Log-Eintrag wird gespeichert

[Manueller Sync]
Watch DB → DataClient → Phone DB → Visualisierung
Phone Config → DataClient → Watch DB → Reminder Engine
```

## Room DB Tabellen

| Tabelle | Haltezeit | Beschreibung |
|---|---|---|
| `reminder_configs` | persistent | Konfiguration aller Reminder-Templates |
| `hydration_logs` | 30 Tage Watch / 180 Tage Phone | Getränkte Mengen mit Timestamp |
| `supplement_logs` | 30 Tage Watch / 180 Tage Phone | Einnahmen mit Timestamp |
| `health_cache` | 30 Tage | Schritte, Aktivitätsstatus, cached vom PassiveListenerService |
| `heart_rate_cache` | 30 Tage | Background HR von PassiveMonitoringClient |
