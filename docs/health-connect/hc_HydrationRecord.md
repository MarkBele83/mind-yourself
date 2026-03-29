# HydrationRecord

Artifact: [androidx.health.connect:connect-client](https://developer.android.com/jetpack/androidx/releases/health-connect) [View Source](https://cs.android.com/search?q=file:androidx/health/connect/client/records/HydrationRecord.kt+class:androidx.health.connect.client.records.HydrationRecord) Added in [1.1.0](https://developer.android.com/jetpack/androidx/releases/health-connect#1.1.0)

*** ** * ** ***

Kotlin \|[Java](https://developer.android.com/reference/androidx/health/connect/client/records/HydrationRecord "View this page in Java")


```
class HydrationRecord : Record
```

<br />

*** ** * ** ***

Captures how much water a user drank in a single drink.

## Summary

| ### Public companion properties |
|---|---|
| `https://developer.android.com/reference/kotlin/androidx/health/connect/client/aggregate/AggregateMetric<https://developer.android.com/reference/kotlin/androidx/health/connect/client/units/Volume>` | `https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/HydrationRecord#VOLUME_TOTAL()` Metric identifier to retrieve total hydration from `https://developer.android.com/reference/kotlin/androidx/health/connect/client/aggregate/AggregationResult`. |

| ### Public constructors |
|---|
| `https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/HydrationRecord#HydrationRecord(java.time.Instant,java.time.ZoneOffset,java.time.Instant,java.time.ZoneOffset,androidx.health.connect.client.units.Volume,androidx.health.connect.client.records.metadata.Metadata)( startTime: https://developer.android.com/reference/java/time/Instant.html, startZoneOffset: https://developer.android.com/reference/java/time/ZoneOffset.html?, endTime: https://developer.android.com/reference/java/time/Instant.html, endZoneOffset: https://developer.android.com/reference/java/time/ZoneOffset.html?, volume: https://developer.android.com/reference/kotlin/androidx/health/connect/client/units/Volume, metadata: https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/metadata/Metadata )` |

| ### Public functions |
|---|---|
| `open operator https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html` | `https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/HydrationRecord#equals(kotlin.Any)(other: https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-any/index.html?)` |
| `open https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html` | `https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/HydrationRecord#hashCode()()` |
| `open https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html` | `https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/HydrationRecord#toString()()` |

| ### Public properties |
|---|---|
| `open https://developer.android.com/reference/java/time/Instant.html` | `https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/HydrationRecord#endTime()` End time of the record. |
| `open https://developer.android.com/reference/java/time/ZoneOffset.html?` | `https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/HydrationRecord#endZoneOffset()` User experienced zone offset at `https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/IntervalRecord#endTime()`, or null if unknown. |
| `open https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/metadata/Metadata` | `https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/HydrationRecord#metadata()` Set of common metadata associated with the written record. |
| `open https://developer.android.com/reference/java/time/Instant.html` | `https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/HydrationRecord#startTime()` Start time of the record. |
| `open https://developer.android.com/reference/java/time/ZoneOffset.html?` | `https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/HydrationRecord#startZoneOffset()` User experienced zone offset at `https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/IntervalRecord#startTime()`, or null if unknown. |
| `https://developer.android.com/reference/kotlin/androidx/health/connect/client/units/Volume` | `https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/HydrationRecord#volume()` Volume of water in `https://developer.android.com/reference/kotlin/androidx/health/connect/client/units/Volume` unit. |

## Public companion properties

### VOLUME_TOTAL

```
val VOLUME_TOTAL: AggregateMetric<Volume>
```

Metric identifier to retrieve total hydration from `https://developer.android.com/reference/kotlin/androidx/health/connect/client/aggregate/AggregationResult`.

## Public constructors

### HydrationRecord

Added in [1.1.0](https://developer.android.com/jetpack/androidx/releases/health-connect#1.1.0)

```
HydrationRecord(
    startTime: Instant,
    startZoneOffset: ZoneOffset?,
    endTime: Instant,
    endZoneOffset: ZoneOffset?,
    volume: Volume,
    metadata: Metadata
)
```

## Public functions

### equals

```
open operator fun equals(other: Any?): Boolean
```

### hashCode

```
open fun hashCode(): Int
```

### toString

```
open fun toString(): String
```

## Public properties

### endTime

Added in [1.1.0](https://developer.android.com/jetpack/androidx/releases/health-connect#1.1.0)

```
open val endTime: Instant
```

End time of the record.

### endZoneOffset

Added in [1.1.0](https://developer.android.com/jetpack/androidx/releases/health-connect#1.1.0)

```
open val endZoneOffset: ZoneOffset?
```

User experienced zone offset at `https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/IntervalRecord#endTime()`, or null if unknown. Providing these will help history aggregations results stay consistent should user travel. Queries with user experienced time filters will assume system current zone offset if the information is absent.

### metadata

```
open val metadata: Metadata
```

Set of common metadata associated with the written record.

### startTime

Added in [1.1.0](https://developer.android.com/jetpack/androidx/releases/health-connect#1.1.0)

```
open val startTime: Instant
```

Start time of the record.

### startZoneOffset

Added in [1.1.0](https://developer.android.com/jetpack/androidx/releases/health-connect#1.1.0)

```
open val startZoneOffset: ZoneOffset?
```

User experienced zone offset at `https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/IntervalRecord#startTime()`, or null if unknown. Providing these will help history aggregations results stay consistent should user travel. Queries with user experienced time filters will assume system current zone offset if the information is absent.

### volume

Added in [1.1.0](https://developer.android.com/jetpack/androidx/releases/health-connect#1.1.0)

```
val volume: Volume
```

Volume of water in `https://developer.android.com/reference/kotlin/androidx/health/connect/client/units/Volume` unit. Required field. Valid range: 0-100 liters.