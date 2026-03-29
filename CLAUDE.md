# mindYourself – Claude Instructions

## Version Management

Bei **jeder Änderungsrunde** (neue Features, Bugfixes, Refactoring) die `versionCode` und `versionName`
in **beiden** Gradle-Dateien erhöhen:

- [app/build.gradle.kts](app/build.gradle.kts) — `defaultConfig { versionCode / versionName }`
- [wear/build.gradle.kts](wear/build.gradle.kts) — `defaultConfig { versionCode / versionName }`

Schema: `versionCode` immer +1, `versionName` nach SemVer (`MAJOR.MINOR.PATCH`):
- PATCH (+0.0.1): Bugfix, kleinere Korrekturen
- MINOR (+0.1.0): Neue Features, UI-Erweiterungen
- MAJOR (+1.0.0): Breaking Changes, große Architekturänderungen

Beide APKs immer gemeinsam versionieren (gleiche `versionCode` + `versionName`).

## Deployment

Aktuell verbundene Geräte (Ports können sich ändern):
- **Pixel 7** (Phone): USB — `adb -s 2B131FDH2000G5`
- **Pixel Watch 4** (Wear): WLAN — `adb -s 192.168.178.56:<port>` (Port prüfen mit `adb devices -l`)

Nach Änderungen immer **beide** APKs neu bauen und installieren:
```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
  ./gradlew :app:assembleDebug :wear:assembleDebug

adb -s 2B131FDH2000G5 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s 192.168.178.56:<port> install -r wear/build/outputs/apk/debug/wear-debug.apk
```

Bei `ClassNotFoundException` nach Incremental Install: **vollständig deinstallieren** statt `-r`:
```bash
adb -s <device> uninstall de.stroebele.mindyourself
adb -s <device> install wear/build/outputs/apk/debug/wear-debug.apk
```

Version auf Gerät prüfen:
```bash
adb -s <device> shell dumpsys package de.stroebele.mindyourself | grep -E "versionName|versionCode|lastUpdateTime"
```

## Datenbankänderungen & Migrationen

Bei **jeder Änderung an Datenmodellen** (Erinnerungen, Orte, Health Connect Logs, App-Einstellungen, synced Data etc.) muss geprüft werden:

1. **Migration notwendig?** — Ja, wenn sich Tabellenstruktur, Spalten, Indizes oder Constraints in `AppDatabase` ändern.
2. **Migration implementieren** — Neue `MIGRATION_X_Y` in `AppDatabase.kt` hinzufügen:
   - DDL-Statements für alle Schema-Änderungen (ALTER TABLE, CREATE INDEX, etc.)
   - Migration in `DataModule.kt` unter `.addMigrations(...)` registrieren
   - DB-Versionsnummer (`version = X`) in `AppDatabase.kt` erhöhen
3. **Automatisch beim App-Start** — Room wendet registrierte Migrationen beim ersten DB-Zugriff automatisch an; kein zusätzlicher Code nötig.
4. **Wear DataLayer** — Bei Änderungen an Daten-Strukturen, die über den DataLayer synchronisiert werden (`/reminder_configs`, `/vacation_settings`, `/app_settings`, etc.), auch `WearSyncService` und `WearStartupRestoreUseCase` auf Kompatibilität prüfen.

**Checkliste bei Schemaänderungen:**
- [ ] `@Entity`-Annotierung der betroffenen Room-Entity angepasst
- [ ] `MIGRATION_X_Y` in `AppDatabase.kt` implementiert
- [ ] Migration in `DataModule.kt` registriert
- [ ] DB-Version in `AppDatabase.kt` erhöht
- [ ] Room-Schema-Datei (in `schemas/`) nach Build prüfen (wird automatisch generiert)
- [ ] Beide Apps (Phone + Wear) bauen und testen — Room prüft Identity Hash beim Start

> **Achtung:** Fehlende Migrationen führen zu `IllegalStateException` (Migration didn't properly handle) oder Hash-Mismatch-Crashes beim App-Start auf bereits installierten Versionen.

## Health Connect Integration

Official Android Health Connect documentation is stored in `docs/health-connect/`. Key implementation details:

### Metadata & Recording Methods (v1.1.0-alpha12+)

- **Mandatory:** Every `Record` must specify `Metadata` with a recording method via factory method:
  - `Metadata.manualEntry()` — user manually entered the data
  - `Metadata.activelyRecorded(device)` — user initiated recording on a device
  - `Metadata.autoRecorded(device)` — device/sensor recorded automatically
- **Client ID:** `clientRecordId` enables idempotent upserts — HC deduplicates on this key across syncs
- **Device Type:** Required for auto/actively recorded data (e.g., `Device.TYPE_WATCH`, `Device.TYPE_PHONE`)

mindYourself hydration logs use `Metadata.manualEntry(device = Device(type = Device.TYPE_WATCH))` since the watch is the recording device.

### Rate Limiting & Changelog API

- **Changelog API** (preferred): Use `getChangesToken()` + `getChanges()` for incremental syncs to avoid repeated full reads
- **Token expiry:** Changes tokens expire after 30 days of disuse — fallback to full `readRecords()` on `IllegalStateException`
- **Background stricter:** Background API calls have stricter rate limits than foreground — keep HC operations foreground-only when possible
- **Partial unique index** on `healthConnectId` (NULL-safe) prevents duplicate HC-sourced records on watch

### Data Synchronization

- **Upsert pattern:** `insertRecords()` with `clientRecordId` + `clientRecordVersion` for idempotent updates
- **Deletion:** Track HC `id` for external records; use `deleteRecords()` when user removes entries
- **Exception handling:** On write failure, retry from failure point (don't delete all and retry — wastes quota)

## Design & UI

### Wear OS Design Prinzipien

Offizielle Android Developer Design-Dokumentationen für Wear OS befinden sich in `docs/design/wear/`:

- **`material-2.5/`** — Aktuelle Design-Prinzipien für mindYourself (Material 2.5)
  - Phone und Watch Apps folgen diesen Richtlinien
  - Etabliertes Design System mit bewährten Patterns
- **`material-3-expressive/`** — Neueres, expressiveres Design System (Material 3)
  - Für zukünftige Migration verfügbar
  - Kann später als major update integriert werden

mindYourself implementiert **Material 2.5** und ist bei Bedarf später zu Material 3 Expressive migrierbar.

### Design-Tools

**Figma MCP Server** ist in `mcp.json` konfiguriert für direkte Integration:

- Design-Assets und Prototypen in Figma verwalten
- Claude kann über den MCP Server auf Design-Dateien zugreifen
- Koordination zwischen Code und Design-Spezifikationen
- MCP-Konfiguration in [`mcp.json`](mcp.json) definiert

## Projektarchitektur & Module

Multi-Modul Clean Architecture:

| Modul | Inhalt |
|-------|--------|
| `:shared:domain` | Pure Kotlin — Domain Models (`ReminderConfig`, `AppSettings`, `NamedLocation`, …), Repository Interfaces |
| `:shared:data` | Room DB, DAOs, Repository-Implementierungen, DataStore, `BackupRepository`, Room-Migrationen |
| `:shared:sync` | Wear Data Layer Wrapper (`WearDataClient`), Sync-DTOs, manuelles JSON-Serialisieren |
| `:app` | Phone-App (Jetpack Compose) — Konfiguration, History, Einstellungen, Backup/Restore, `PhoneSyncUseCase` |
| `:wear` | Wear OS App — `PassiveListenerService`, `ReminderEvaluationWorker`, `WearSyncService`, `HomeViewModel` |

### Backup & Restore

`BackupRepository` (in `:shared:data`) deckt folgende Bereiche ab:

| JSON-Key | Inhalt |
|----------|--------|
| `reminder_configs` | Alle Erinnerungen (Typ-Config, Zeitplan, Orts-Filter) |
| `locations` | Named Locations (WiFi-SSID, Cell-IDs) |
| `vacation_periods` | Urlaubsbereiche |
| `hydration_portion_sizes` | Schnell-Log-Mengen |
| `step_daily_goal` | Tägliches Schrittziel |

Import: DB-Tabellen werden via replace-all ersetzt; DataStore-Felder über `.copy()` gemergt — nur im Backup enthaltene Felder werden überschrieben, andere Settings bleiben erhalten.

### Hydration Goal Sync

`PhoneSyncUseCase` berechnet `hydrationDailyGoalMl = sum(enabled HYDRATION reminders' reminderGoalMl)` und pusht es als Teil von `AppSettingsDto` bei jedem Sync. Die Watch nutzt diesen Wert; Fallback auf lokale Summe wenn `hydrationDailyGoalMl == 0` (noch kein Sync).

## Wear Data Layer API

Dokumentation in `docs/data-handling/`. Drei Client-Typen — die Wahl beeinflusst Persistenz und Offline-Verhalten:

| Client | Persistenz | Netzwerk nötig? | Offline schreibbar? | Typischer Einsatz |
|--------|-----------|-----------------|---------------------|-------------------|
| **DataClient** | Persistent (bis Delete) | Nein | Ja | Config-Sync, App-Settings — **mindYourself nutzt diesen** |
| **MessageClient** | Keine (best-effort) | Ja | Nein | One-shot-Kommandos, RPCs (z. B. „Sync jetzt starten") |
| **ChannelClient** | Keine (verbindungsorientiert) | Ja | Nein | Große Dateien (>100 KB), Streams |

**Wichtige Hinweise:**
- `DataItem`s werden auf allen verbundenen Geräten synchronisiert und persistent gespeichert → ideal für Konfigurations-Sync
- `MessageClient.sendMessage()` hat **keinen eingebauten Retry**; bei Trennung vor Sendestart → `TARGET_NODE_NOT_CONNECTED`
- Reconnect kann bis zu **4 Minuten** dauern (bei Wear-Inaktivität oder Doze-Modus des Phones)
- Wenn kein Bluetooth verfügbar, fließen Daten über Google Cloud (end-to-end-verschlüsselt)

### Geräte-Erkennung

- **`NodeClient`** — Liste aller verbundenen Knoten, unabhängig von installierten Apps (auch wenn die eigene App fehlt)
- **`CapabilityClient`** — prüft ob ein Gerät eine bestimmte App-Capability unterstützt; verwenden wenn Feature-Abhängigkeiten zwischen Phone- und Watch-App bestehen (z. B. neue API-Version nur auf einer Seite)

### Permissions auf Wear OS

- **Wear-App kann Phone-Berechtigungen NICHT übernehmen** — jede Permission muss separat auf jedem Gerät erteilt werden
- Wenn Wear eine Phone-Permission benötigt: User muss zum Phone weitergeleitet werden (z. B. via `MessageClient` → Activity öffnen)
- Wenn Phone eine Wear-Permission benötigt: Phone schickt User zur Watch
- Für neue Features mit Permission-Bedarf beide Seiten (`:app` + `:wear`) prüfen
