# HydrationRecord.Builder

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)

# Builder

*** ** * ** ***

Kotlin\|[Java](https://developer.android.com/reference/android/health/connect/datatypes/HydrationRecord.Builder "View this page in Java")  

```
class Builder
```

|---|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [kotlin.Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)                                                                                       ||
| ↳ | [android.health.connect.datatypes.HydrationRecord.Builder](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/HydrationRecord.Builder#) |

Builder class for[HydrationRecord](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/HydrationRecord)

## Summary

|                                                                                                                                                                                                                                                                                                                                                  Public constructors                                                                                                                                                                                                                                                                                                                                                   ||
|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---|
| [Builder](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/HydrationRecord.Builder#Builder(android.health.connect.datatypes.Metadata,%20java.time.Instant,%20java.time.Instant,%20android.health.connect.datatypes.units.Volume))`(`metadata:` `[Metadata](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata)`, `startTime:` `[Instant](https://developer.android.com/reference/kotlin/java/time/Instant.html#)`, `endTime:` `[Instant](https://developer.android.com/reference/kotlin/java/time/Instant.html#)`, `volume:` `[Volume](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/units/Volume)`)` <br /> |

|                                                                                                                                                                                                                                     Public methods                                                                                                                                                                                                                                     ||
|-------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [HydrationRecord](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/HydrationRecord)                  | [build](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/HydrationRecord.Builder#build())`()` <br />                                                                                                                                                                                                               |
| [HydrationRecord.Builder](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/HydrationRecord.Builder#) | [clearEndZoneOffset](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/HydrationRecord.Builder#clearEndZoneOffset())`()` Sets the start zone offset of this record to system default.                                                                                                                               |
| [HydrationRecord.Builder](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/HydrationRecord.Builder#) | [clearStartZoneOffset](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/HydrationRecord.Builder#clearStartZoneOffset())`()` Sets the start zone offset of this record to system default.                                                                                                                           |
| [HydrationRecord.Builder](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/HydrationRecord.Builder#) | [setEndZoneOffset](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/HydrationRecord.Builder#setEndZoneOffset(java.time.ZoneOffset))`(`endZoneOffset:` `[ZoneOffset](https://developer.android.com/reference/kotlin/java/time/ZoneOffset.html#)`)` Sets the zone offset of the user when the activity ended         |
| [HydrationRecord.Builder](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/HydrationRecord.Builder#) | [setStartZoneOffset](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/HydrationRecord.Builder#setStartZoneOffset(java.time.ZoneOffset))`(`startZoneOffset:` `[ZoneOffset](https://developer.android.com/reference/kotlin/java/time/ZoneOffset.html#)`)` Sets the zone offset of the user when the activity started |

## Public constructors

### Builder

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)  

```
Builder(
    metadata: Metadata, 
    startTime: Instant, 
    endTime: Instant, 
    volume: Volume)
```

|                                                                                                                                                           Parameters                                                                                                                                                           ||
|-------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `metadata`  | [Metadata](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata):Metadata to be associated with the record. See[Metadata](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata). This value cannot be`null`.                              |
| `startTime` | [Instant](https://developer.android.com/reference/kotlin/java/time/Instant.html#):Start time of this activity This value cannot be`null`.                                                                                                                                                                         |
| `endTime`   | [Instant](https://developer.android.com/reference/kotlin/java/time/Instant.html#):End time of this activity This value cannot be`null`.                                                                                                                                                                           |
| `volume`    | [Volume](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/units/Volume):Volume of the liquids in[Volume](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/units/Volume)unit. Required field. Valid range: 0-100 liters. This value cannot be`null`. |

## Public methods

### build

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)  

```
fun build(): HydrationRecord
```

|                                                                                                                                   Return                                                                                                                                   ||
|--------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| [HydrationRecord](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/HydrationRecord) | Object of[HydrationRecord](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/HydrationRecord)This value cannot be`null`. |

### clearEndZoneOffset

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)  

```
fun clearEndZoneOffset(): HydrationRecord.Builder
```

Sets the start zone offset of this record to system default.

|                                                                              Return                                                                              ||
|-------------------------------------------------------------------------------------------------------------------------------------|-----------------------------|
| [HydrationRecord.Builder](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/HydrationRecord.Builder#) | This value cannot be`null`. |

### clearStartZoneOffset

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)  

```
fun clearStartZoneOffset(): HydrationRecord.Builder
```

Sets the start zone offset of this record to system default.

|                                                                              Return                                                                              ||
|-------------------------------------------------------------------------------------------------------------------------------------|-----------------------------|
| [HydrationRecord.Builder](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/HydrationRecord.Builder#) | This value cannot be`null`. |

### setEndZoneOffset

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)  

```
fun setEndZoneOffset(endZoneOffset: ZoneOffset): HydrationRecord.Builder
```

Sets the zone offset of the user when the activity ended

|                                                              Parameters                                                              ||
|-----------------|---------------------------------------------------------------------------------------------------------------------|
| `endZoneOffset` | [ZoneOffset](https://developer.android.com/reference/kotlin/java/time/ZoneOffset.html#):This value cannot be`null`. |

|                                                                              Return                                                                              ||
|-------------------------------------------------------------------------------------------------------------------------------------|-----------------------------|
| [HydrationRecord.Builder](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/HydrationRecord.Builder#) | This value cannot be`null`. |

### setStartZoneOffset

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)  

```
fun setStartZoneOffset(startZoneOffset: ZoneOffset): HydrationRecord.Builder
```

Sets the zone offset of the user when the activity started

|                                                               Parameters                                                               ||
|-------------------|---------------------------------------------------------------------------------------------------------------------|
| `startZoneOffset` | [ZoneOffset](https://developer.android.com/reference/kotlin/java/time/ZoneOffset.html#):This value cannot be`null`. |

|                                                                              Return                                                                              ||
|-------------------------------------------------------------------------------------------------------------------------------------|-----------------------------|
| [HydrationRecord.Builder](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/HydrationRecord.Builder#) | This value cannot be`null`. |