# Building mindYourself: policy and API guide for Wear OS health apps

**Health Services API handles real-time sensor data on the watch; Health Connect serves as the phone-side data repository — and you need both.** For Play Store publication, Google requires a Health Apps declaration, per-data-type permission justifications, a public privacy policy, and a Wear OS quality review. The critical architectural insight: Health Connect does **not** run on Wear OS devices, so your watch app must use Health Services for all sensor access and maintain a local Room database for offline-first operation, syncing to Health Connect on the phone when connected.

This report covers every concrete policy requirement and API decision needed to build and eventually publish mindYourself as a Wear OS wellness/reminder app.

---

## Health Services vs Health Connect: two APIs, one architecture

Google's health platform splits into two complementary APIs that serve fundamentally different roles. Understanding this split is the single most important architectural decision for mindYourself.

**Health Services** (`androidx.health:health-services-client:1.1.0-rc01`) is a system-level service built into every Wear OS 3+ device. It provides **real-time, power-efficient access to watch sensors** — accelerometer, heart rate, GPS — and runs entirely on-watch without any phone connection. It is your data *source*.

**Health Connect** (`androidx.health.connect:connect-client:1.2.0-alpha02`) is an on-device data *store* on the phone. It acts as a centralized, encrypted repository where multiple apps can read and write health records. It does **not** read sensors directly and does **not** run on Wear OS. On Android 14+, it's a built-in framework module; on Android 9–13, it's a downloadable app.

| Dimension | Health Services | Health Connect |
|-----------|----------------|----------------|
| **Platform** | Wear OS 3+ (watch only) | Android phone (API 28+) |
| **Role** | Real-time sensor collection | Cross-app data storage/sharing |
| **Works offline?** | Yes — fully standalone on watch | Yes — local encrypted store on phone |
| **Real-time data?** | Yes — streaming and batched | No — read/write stored records |
| **Key dependency** | `androidx.health:health-services-client` | `androidx.health.connect:connect-client` |
| **Min API level** | 30 (Android 11 / Wear OS 3) | 26 (runtime requires 28+) |

For mindYourself V1 with local-only storage, **Health Services is your primary API**. Health Connect becomes relevant when you add a phone companion app or want to share data with other health apps.

---

## Three Health Services clients and when to use each

Health Services exposes functionality through three specialized clients, all obtained from a single entry point:

```kotlin
val healthClient = HealthServices.getClient(context)
val passiveClient = healthClient.passiveMonitoringClient
val measureClient = healthClient.measureClient
val exerciseClient = healthClient.exerciseClient
```

**PassiveMonitoringClient** is the workhorse for mindYourself. It provides always-on background monitoring with the lowest battery impact. Data arrives in batches (not at fixed intervals) — the watch's MCU buffers sensor readings and delivers them when buffers fill or the display activates. This client supports daily step counting via `DataType.STEPS_DAILY`, background heart rate via `DataType.HEART_RATE_BPM`, and critically, **sedentary detection** through `UserActivityInfo` which reports states like `USER_ACTIVITY_PASSIVE`, `USER_ACTIVITY_EXERCISE`, and `USER_ACTIVITY_ASLEEP`. You can also set `PassiveGoal` targets (e.g., 10,000 daily steps) that fire callbacks when reached. One important caveat: **passive registrations do not persist across reboots** — you must re-register via a `BOOT_COMPLETED` BroadcastReceiver delegating to WorkManager.

**MeasureClient** delivers real-time streaming data while the app UI is visible — primarily for on-demand heart rate spot checks. It increases sensor sampling rate significantly, so registration time should be minimized to conserve battery.

**ExerciseClient** manages full workout sessions with start/pause/resume/stop lifecycle, live metrics at ~1Hz, auto-pause detection, and GPS tracking. Use this if mindYourself adds explicit exercise tracking later.

### Data type mapping for mindYourself features

| Feature | Client | DataType | Notes |
|---------|--------|----------|-------|
| Daily step count | `PassiveMonitoringClient` | `STEPS_DAILY` | Resets at midnight automatically |
| Step goal alerts | `PassiveMonitoringClient` | `PassiveGoal` on `STEPS_DAILY` | Triggers callback when goal met |
| Sedentary reminders | `PassiveMonitoringClient` | `UserActivityInfo` + `STEPS` delta | Track duration in `USER_ACTIVITY_PASSIVE` state; trigger notification after threshold |
| Background heart rate | `PassiveMonitoringClient` | `HEART_RATE_BPM` | Sampling interval varies by device (1s to 10min) |
| Spot heart rate check | `MeasureClient` | `HEART_RATE_BPM` | Real-time streaming; unregister quickly |
| Hydration tracking | None (user input) | N/A | Store manually in local Room DB; write `HydrationRecord` to Health Connect on sync |
| Supplement tracking | None (user input) | N/A | Store in local Room DB; map to `NutritionRecord` in Health Connect |
| Location reminders | Fused Location Provider | N/A | Use standard Google Play Services FLP, not Health Services |

---

## Recommended offline-first architecture for V1

Since mindYourself V1 stores data locally with no cloud sync, the architecture centers on the watch with an optional phone-side sync layer:

```
┌──────────── WEAR OS WATCH ────────────┐
│                                        │
│  Health Services API                   │
│  ├─ PassiveMonitoringClient            │
│  │  (steps, heart rate, activity)      │
│  ├─ MeasureClient                      │
│  │  (on-demand heart rate)             │
│  └─ FusedLocationProvider              │
│     (location-based reminders)         │
│            │                           │
│  Local Room Database ◄────────────────│
│  ├─ Cached sensor data                 │
│  ├─ Hydration entries (user input)     │
│  ├─ Supplement logs (user input)       │
│  ├─ Reminder state & preferences       │
│  └─ Pending sync queue                 │
│            │                           │
│  Notification & Reminder Engine        │
│  (triggered by goals, activity state,  │
│   geofence, or scheduled time)         │
└────────────┼───────────────────────────┘
             │ Data Layer API (when connected)
┌────────────▼───────────────────────────┐
│         ANDROID PHONE (future V2)      │
│  WearableListenerService               │
│       → HealthConnectClient            │
│         (write StepsRecord,            │
│          HydrationRecord, etc.)        │
└────────────────────────────────────────┘
```

**Key architectural decisions**: Store all data in a local Room database on the watch. Use `PassiveListenerService` (not `PassiveListenerCallback`) for background data — it survives app process death. For hydration and supplement tracking, these are pure user-input features with no sensor equivalent; store them locally and map to `HydrationRecord` (volume in liters) and `NutritionRecord` when syncing to Health Connect. For location-based reminders, use the **Fused Location Provider** from Google Play Services rather than Health Services' `DataType.LOCATION`, which is designed for exercise tracking. Sedentary reminders require custom logic: monitor `UserActivityInfo` for prolonged `USER_ACTIVITY_PASSIVE` state and trigger local notifications via standard Android `NotificationManager`.

---

## Play Store policy requirements: the full checklist

Google enforces layered policy requirements for health and fitness Wear OS apps. Here is every requirement that applies to mindYourself, with specific permission strings and deadlines.

### Mandatory permissions for AndroidManifest.xml

```xml
<!-- Wear OS identification -->
<uses-feature android:name="android.hardware.type.watch" />
<meta-data android:name="com.google.android.wearable.standalone" android:value="true" />

<!-- Health Services (watch sensors) -->
<uses-permission android:name="android.permission.BODY_SENSORS" />
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />

<!-- Location (for location-based reminders) -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<!-- Only if geofencing while app is closed: -->
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

<!-- Foreground service -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_HEALTH" />

<!-- Health Connect (phone-side companion, V2) -->
<uses-permission android:name="android.permission.health.READ_STEPS" />
<uses-permission android:name="android.permission.health.WRITE_STEPS" />
<uses-permission android:name="android.permission.health.READ_HEART_RATE" />
<uses-permission android:name="android.permission.health.WRITE_HEART_RATE" />
<uses-permission android:name="android.permission.health.READ_HYDRATION" />
<uses-permission android:name="android.permission.health.WRITE_HYDRATION" />
<uses-permission android:name="android.permission.health.READ_NUTRITION" />
<uses-permission android:name="android.permission.health.WRITE_NUTRITION" />
<uses-permission android:name="android.permission.health.READ_DISTANCE" />
<uses-permission android:name="android.permission.health.WRITE_DISTANCE" />
```

For **Android 16+ / Wear OS 6** (API 36), `BODY_SENSORS` is being replaced by granular permissions like `android.permission.READ_HEART_RATE`. Use `android:maxSdkVersion="35"` on `BODY_SENSORS` and add the new granular permissions for forward compatibility.

### Play Console declarations and reviews

**Health Apps declaration** is mandatory for all published apps. Navigate to Play Console → Policy & Programs → App Content → Health Apps. Select "Activity and fitness" and "Nutrition and weight management." Provide detailed per-data-type justifications — vague statements like "needed for app functionality" will be rejected. Example: *"Our app tracks daily step counts to help users monitor physical activity levels and trigger personalized movement reminders."*

**Health Connect permission review** triggers automatically when you upload an APK that declares Health Connect permissions. Google reviews each data type request. Without approval, published apps display an error dialog: *"This app can't access Health Connect."* During development, Health Connect grants unrestricted access — the restriction only applies to published apps.

**Background location approval** requires a separate declaration under Play Console → App Content → Sensitive permissions and APIs. You must submit a **≤30-second video** demonstrating: prominent disclosure mentioning "location," "background," and "when the app is closed"; the runtime permission prompt; and the feature working from background. Google may reject background location for reminder use cases that could work with foreground-only location. **Strong recommendation for V1**: use foreground location only, avoiding the background location approval process entirely unless geofencing is truly essential.

**Data Safety section** must declare: health info (steps, activity, hydration), location data (precise), local storage, encryption practices, and a data deletion mechanism.

### Privacy policy and disclosure requirements

Your privacy policy must be hosted at a **publicly accessible, non-geofenced URL** (not a PDF or editable document) and linked identically in three places: Play Console, inside the app, and on your website. It must disclose what health/fitness data is collected, how it's used/stored/shared, retention and deletion policies, and security practices.

Before collecting data via dangerous permissions, the app must show a **prominent in-app disclosure** and obtain affirmative user consent. For example: *"mindYourself collects physical activity data to track your steps and provide movement reminders."*

A **non-medical disclaimer** is required in the first paragraph of the Play Store description as of January 2026: *"This app is not a medical device and does not diagnose, treat, or prevent any condition."*

A **PermissionsRationaleActivity** must be declared in the manifest for Health Connect:

```xml
<activity android:name=".PermissionsRationaleActivity" android:exported="true">
    <intent-filter>
        <action android:name="androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE" />
    </intent-filter>
</activity>
```

---

## Wear OS quality requirements and distribution

Wear OS apps undergo a **separate mandatory review** from phone apps. Status appears in Play Console as Pending → Approved / Not approved. A failed Wear OS review can block updates across all form factors. Key quality requirements enforced since August 2023 include:

- **Black background** on all screens and tiles
- **Time of day** displayed at the top of the home screen and ongoing activity screens
- **Swipe-to-dismiss** supported on almost all screens (fitness activities and maps exempted)
- **Rotating crown/bezel** support for all scrollable content, with visible scrollbar
- **Text scaling** must work correctly when users change system text size
- **App state preservation** across sessions
- Wear OS-specific **screenshots** required in the Play Store listing (black background, no device masking)

**Target API requirements**: as of August 31, 2025, new Wear OS apps must target **API 34** (Android 14), existing apps must target at least **API 33** (Android 13). Set `com.google.android.wearable.standalone` to `true` since mindYourself functions independently without a paired phone.

To distribute: add the Wear OS form factor in Play Console → Test and release → Advanced Settings → Form factors. **Closed testing is required before production release.** Use the same package name as any future mobile companion app to share ratings and reviews.

---

## Concrete recommendation for mindYourself V1

For V1 with offline-first, watch-only operation: **build exclusively on Health Services API with a local Room database.** Health Connect integration should be deferred to V2 when you add a phone companion app. Here is the priority implementation order:

1. **PassiveMonitoringClient** for daily steps (`STEPS_DAILY`), background heart rate (`HEART_RATE_BPM`), and sedentary detection (`UserActivityInfo`). Register via `PassiveListenerService` with re-registration on `BOOT_COMPLETED`.
2. **Room database** for hydration entries, supplement logs, reminder preferences, and cached sensor data. This is your single source of truth on the watch.
3. **Local notification engine** triggered by `PassiveGoal` callbacks (step goals), `UserActivityInfo` state changes (sedentary reminders), scheduled alarms (hydration/supplement reminders), and FusedLocationProvider geofence events (location reminders).
4. **MeasureClient** for on-demand heart rate display in the app UI.
5. **Health Connect sync layer** (V2) using the Wearable Data Layer API (`MessageClient`/`DataClient`) to transfer watch data to the phone and write `StepsRecord`, `HydrationRecord`, `HeartRateRecord`, and `NutritionRecord` to Health Connect.

For future stress detection via heart rate variability: Health Services does not provide HRV as a direct data type. You'll need to collect raw heart rate samples from `PassiveMonitoringClient` and compute RMSSD-based HRV yourself, or use Health Connect's `HeartRateVariabilityRmssdRecord` if the watch's default health app writes HRV data there. This is a V2+ consideration.

### Personal use vs published: what changes

During development and personal sideloaded use, **no Play Store approvals are needed** — Health Connect grants unrestricted access to development builds, and Health Services requires no approval at all. When you publish to the Play Store for other users, you must complete the Health Apps declaration, Health Connect permission justifications, Data Safety section, privacy policy, optional background location approval (with video), and pass Wear OS quality review. Plan **2–4 weeks** for the combined review process, and budget time for potential rejection-and-resubmission cycles on Health Connect permission justifications.