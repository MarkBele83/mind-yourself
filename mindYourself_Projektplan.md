# mindYourself – Projektplan

## Vision

**mindYourself** ist eine Privacy-First Reminder-App für Android und Wear OS, die Nutzer durch intelligente, kontextsensitive Erinnerungen zu gesünderen Gewohnheiten motiviert. Alle Daten werden lokal verarbeitet – keine Cloud, keine Datenweitergabe an Dritte.

---

## Zielgruppe

Primär für den Eigengebrauch entwickelt, jedoch mit einer Architektur und UX, die eine spätere Öffnung für externe Nutzer ohne grundlegende Umbauten ermöglicht.

---

## App-Struktur

### Zwei Apps, ein Bundle

Beide Apps werden als **ein App Bundle** im Google Play Store veröffentlicht – der empfohlene Google-Standard für Wear OS Companion-Apps.

**Wear OS App (Primär)**

- Ausführung und Auslösung aller Erinnerungen direkt auf der Uhr
- Lokale Datenhaltung und Sensor-Auswertung
- Offline-First: Erinnerungen funktionieren auch ohne Verbindung zum Smartphone, sofern die Trigger-Daten auf der Uhr vorhanden sind

**Android Phone App (Companion)**

- Konfiguration aller Reminder-Regeln und Templates
- Visualisierung der Verlaufsdaten von der Uhr
- Datensicherung und Export-Funktionen
- Manueller Sync mit der Uhr (Button-getriggert, nicht automatisch)

---

## Tech Stack

| Bereich | Technologie |
| --- | --- |
| Sprache | Kotlin (vollständig) |
| UI Watch | Jetpack Compose for Wear OS |
| UI Phone | Jetpack Compose |
| Architektur | Clean Architecture, Multi-Modul |
| Lokale Datenhaltung | Room (SQLite) |
| Watch-Sensoren | Health Services API |
| Gesundheitsdaten Phone | Health Connect API |
| Watch ↔ Phone Sync | Wear Data Layer API |
| Standort | Fused Location Provider |
| IDE | Android Studio + VS Code + Claude Code |
| Entwicklungsgerät | Mac Mini |

---

## Datenarchitektur

### Speicherung

- **Wear OS Watch**: Lokale Room-Datenbank, max. **30 Tage** Datenhaltung
- **Android Phone**: Lokale Room-Datenbank, max. **180 Tage** Datenhaltung
- **Cloud Export**: Verschlüsselter Export in Cloud Storage – **erst in einer späteren Version**

### API-Rollenteilung

**Health Services API** (Watch, Echtzeit)

- Primäre Datenquelle für alle Sensor-Trigger auf der Uhr
- Schritte, Herzfrequenz, Aktivitätsstatus, Bewegungserkennung
- Funktioniert vollständig offline ohne Smartphone-Verbindung

**Health Connect API** (Phone, Historisch)

- Zentrale Datendrehscheibe auf dem Smartphone
- Ermöglicht Datenaustausch mit Drittgeräten (Oura Ring 4, Garmin Vivoactive 5)
- Schnittstelle für Langzeit-Trends und Visualisierungen in der Phone-App

### Sync-Strategie

Manueller Sync zwischen Watch und Phone, ausgelöst durch Nutzerinteraktion. Bewusste Designentscheidung für Akku-Effizienz und Nutzerkontrolle.

---

## Hardware & Geräte

| Gerät | Rolle |
| --- | --- |
| Pixel Watch 4 | Primäres Wear OS Gerät, Haupt-Sensor-Quelle via Health Services API |
| Pixel 7 (Phone) | Companion-App, Health Connect Hub |
| Oura Ring 4 | Zusätzliche Gesundheitsdaten via Health Connect |
| Garmin Vivoactive 5 | Zusätzliche Gesundheitsdaten via Health Connect |
| Weitere Geräte | Architektur offen für zukünftige Erweiterungen |

Oura Ring und Garmin liefern Daten **nicht direkt** als Watch-Trigger. Der Flow ist: Gerät → Health Connect (Phone) → manueller Sync → Watch-Datenbank → Trigger-Logik.

---

## UX & Notifications

Erinnerungen auf der Uhr bestehen aus **Vibration + Text + kontextabhängigen Aktions-Buttons**. Die Konfiguration erfolgt über **Templates in der Phone-App**:

- Inhaltliche Vorlagen pro Reminder-Typ
- Anpassbar in **Zeit**, **Frequenz** und **Zielwert**
- Aktions-Buttons teilweise konfigurierbar (z. B. „Erledigt", „Snooze", „Später")

---

## Features V1

### Bewegungserinnerung

Erinnerung wenn die Schrittzahl in einem definierten Zeitfenster unter einen Schwellwert fällt (z. B. < 500 Schritte in 2 Stunden).

### Sitz-Pause-Alarm

Erkennung längerer Inaktivitätsphasen via `UserActivityInfo` der Health Services API, Auslösung einer Pause-Erinnerung nach konfigurierbarer Dauer.

### Hydration Tracking

Manuelles Einloggen von Wassermengen auf der Uhr. Intelligente Erinnerungen basierend auf Tageszeit, konfiguriertem Tagesziel und verbleibender Menge.

### Supplements Tracking

Erinnerungen für Nahrungsergänzungsmittel mit Bestätigungs-Tracking. Konfigurierbarer Name, Zeitpunkt und Frequenz pro Supplement.

### Screen Break Erinnerungen

Zeitbasierte Erinnerungen für Bildschirmpausen (Augen-/Fokuspausen). Konfigurierbar in Intervall und aktivem Zeitfenster.

### Standort-basierte Aufgaben-Trigger

Erinnerungen werden beim Betreten oder Verlassen definierter Orte ausgelöst (z. B. Einkaufsliste beim Supermarkt, Aufgaben beim Nachhause-Kommen). Implementiert via Fused Location Provider mit Geofencing.

---

## Roadmap

### Version 1 (MVP)

Alle sechs oben genannten Features, vollständig lokal, Watch + Phone App als Bundle.

### Spätere Versionen

- Verschlüsselter Cloud-Export und -Backup
- Stress-Erkennung via HRV (bewusst aus V1 ausgeschlossen)
- Automatische Gerätesynchronisation
- Erweiterte Visualisierungen und Trend-Analysen
- Open-Source Veröffentlichung (Option)
- Öffnung für externe Nutzer (Play Store Release)

---

## Entwicklungsprinzipien

- **Privacy & Sicherheit**: Alle Daten lokal, keine Weitergabe, kein Tracking. Sensible Daten (Logs, Konfiguration) lokal verschlüsselt (SQLCipher); reine Cache-Daten (health_cache) unverschlüsselt für Performance. Klare Zugriffskontrollen.
- **Offline-First**: Kern-Funktionalität ohne Internetverbindung und ohne Smartphone. Sensor-Monitoring und Reminder laufen vollständig auf der Uhr.
- **Akku-Effizienz**: Manueller Sync statt automatisch, passive Sensor-Nutzung bevorzugt. Hintergrundprozesse (PassiveListenerService, WorkManager) sind funktional notwendig und davon ausgenommen — Nutzerkontrolle gilt explizit für Sync und manuelle Aktionen.
- **Nutzerkontrolle**: Explizite Aktionen für Sync und Datenverwaltung. Automatische Hintergrundprozesse nur wo funktional unvermeidbar (Sensor-Monitoring, Reminder-Evaluierung).
- **Nutzer-UX**: Einfache, intuitive Bedienung auf Watch und Phone. Klare, verständliche Benachrichtigungen. Konsistenz auf Design-Token-Ebene (Sprache, Farben, Terminologie) — nicht auf Komponenten-Ebene (Watch und Phone haben fundamental unterschiedliche UI-Paradigmen).
- **Clean Architecture**: Multi-Modul-Struktur für Skalierbarkeit und Testbarkeit. KDoc auf öffentlichen Interfaces und Klassen, nicht auf internen Implementierungsdetails.
- **Iteratives Vorgehen**: Fokus auf robuste, gut getestete Kernfunktionen statt Feature-Overload. Klare Erweiterungspfade für zukünftige Features ohne grundlegende Umbauten — Abstraktion nur wenn konkret benötigt, nicht spekulativ.
- **Performance**: Minimaler Ressourcenverbrauch auf der Uhr. Abwägung gegen Sicherheit: Verschlüsselung nur auf sensiblen Tabellen.
- **Transparenz**: Klare In-App-Kommunikation der Datenverarbeitung gegenüber Nutzern. Permission-Rationale vor jeder Berechtigungsanfrage.
- **Testbarkeit**: Unit-Tests für Trigger-Logik (ReminderEvaluationWorker) und Datenhaltung (Repository-Implementierungen). Integrationstests auf Pixel Watch 4.
- **Dokumentation**: Architekturentscheidungen und öffentliche APIs dokumentiert. Ausreichend für zukünftige Erweiterungen oder Open-Source-Veröffentlichung.
