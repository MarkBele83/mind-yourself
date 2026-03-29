# Metadata

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)

# Metadata

*** ** * ** ***

Kotlin\|[Java](https://developer.android.com/reference/android/health/connect/datatypes/Metadata "View this page in Java")  

```
class Metadata
```

|---|----------------------------------------------------------------------------------------------------------------------------------------|
| [kotlin.Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)                                                         ||
| ↳ | [android.health.connect.datatypes.Metadata](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata#) |

Set of shared metadata fields for[Record](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Record)

## Summary

|                                                    Nested classes                                                     ||
|---|--------------------------------------------------------------------------------------------------------------------|
|   | [Builder](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata.Builder) <br /> |

|                                                                                                                                                    Constants                                                                                                                                                     ||
|----------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| static[Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) | [RECORDING_METHOD_ACTIVELY_RECORDED](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata#RECORDING_METHOD_ACTIVELY_RECORDED:kotlin.Int) For actively recorded data by the user.           |
| static[Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) | [RECORDING_METHOD_AUTOMATICALLY_RECORDED](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata#RECORDING_METHOD_AUTOMATICALLY_RECORDED:kotlin.Int) For passively recorded data by the app. |
| static[Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) | [RECORDING_METHOD_MANUAL_ENTRY](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata#RECORDING_METHOD_MANUAL_ENTRY:kotlin.Int) For manually entered data by the user.                      |
| static[Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) | [RECORDING_METHOD_UNKNOWN](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata#RECORDING_METHOD_UNKNOWN:kotlin.Int) Unknown recording method.                                             |

|                                                                                                                                                                                                      Public methods                                                                                                                                                                                                      ||
|----------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)                       | [equals](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata#equals(kotlin.Any))`(`other:` `[Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?`)` Indicates whether some other object is "equal to" this one.                                    |
| [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?                        | [getClientRecordId](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata#getClientRecordId())`()` <br />                                                                                                                                                                   |
| [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)                             | [getClientRecordVersion](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata#getClientRecordVersion())`()` <br />                                                                                                                                                         |
| [DataOrigin](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/DataOrigin) | [getDataOrigin](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata#getDataOrigin())`()` <br />                                                                                                                                                                           |
| [Device](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Device)         | [getDevice](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata#getDevice())`()` <br />                                                                                                                                                                                   |
| [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)                         | [getId](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata#getId())`()` <br />                                                                                                                                                                                           |
| [Instant](https://developer.android.com/reference/kotlin/java/time/Instant.html#)                        | [getLastModifiedTime](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata#getLastModifiedTime())`()` <br />                                                                                                                                                               |
| [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)                               | [getRecordingMethod](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata#getRecordingMethod())`()` Returns recording method which indicates how data was recorded for the[Record](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Record) |
| [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)                               | [hashCode](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata#hashCode())`()` Returns a hash code value for the object.                                                                                                                                                  |

## Constants

### RECORDING_METHOD_ACTIVELY_RECORDED

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)  

```
static val RECORDING_METHOD_ACTIVELY_RECORDED: Int
```

For actively recorded data by the user.

For e.g. An exercise session actively recorded by the user using a phone or a watch device.  

    Value: 1

### RECORDING_METHOD_AUTOMATICALLY_RECORDED

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)  

```
static val RECORDING_METHOD_AUTOMATICALLY_RECORDED: Int
```

For passively recorded data by the app.

For e.g. Steps data recorded by a watch or phone without the user starting a session.  

    Value: 2

### RECORDING_METHOD_MANUAL_ENTRY

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)  

```
static val RECORDING_METHOD_MANUAL_ENTRY: Int
```

For manually entered data by the user.

For e.g. Nutrition or weight data entered by the user.  

    Value: 3

### RECORDING_METHOD_UNKNOWN

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)  

```
static val RECORDING_METHOD_UNKNOWN: Int
```

Unknown recording method.  

    Value: 0

## Public methods

### equals

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)  

```
fun equals(other: Any?): Boolean
```

Indicates whether some other object is "equal to" this one.

|                                   Parameters                                   ||
|----------|----------------------------------------------------------------------|
| `obj`    | the reference object with which to compare.                          |
| `object` | the reference object with which to compare. This value may be`null`. |

|                                                             Return                                                              ||
|------------------------------------------------------------------------------------|---------------------------------------------|
| [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) | `true`if this object is the same as the obj |

### getClientRecordId

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)  

```
fun getClientRecordId(): String?
```

|                                                           Return                                                           ||
|-----------------------------------------------------------------------------------|-----------------------------------------|
| [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? | Client record ID if set, null otherwise |

### getClientRecordVersion

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)  

```
fun getClientRecordVersion(): Long
```

|                                                         Return                                                          ||
|------------------------------------------------------------------------------|-------------------------------------------|
| [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) | Client record version if set, 0 otherwise |

### getDataOrigin

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)  

```
fun getDataOrigin(): DataOrigin
```

|                                                                                                          Return                                                                                                          ||
|----------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| [DataOrigin](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/DataOrigin) | Corresponds to package name if set. If no data origin is set`getDataOrigin().getPackageName()`will return null |

### getDevice

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)  

```
fun getDevice(): Device
```

|                                                                                      Return                                                                                      ||
|--------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| [Device](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Device) | The device details that contributed to this record This value cannot be`null`. |

### getId

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)  

```
fun getId(): String
```

|                                                                             Return                                                                             ||
|----------------------------------------------------------------------------------|------------------------------------------------------------------------------|
| [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) | Record identifier if set, empty string otherwise This value cannot be`null`. |

### getLastModifiedTime

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)  

```
fun getLastModifiedTime(): Instant
```

|                                                                                   Return                                                                                   ||
|-----------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| [Instant](https://developer.android.com/reference/kotlin/java/time/Instant.html#) | Record's last modified time if set, Instant.EPOCH otherwise This value cannot be`null`. |

### getRecordingMethod

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)  

```
fun getRecordingMethod(): Int
```

Returns recording method which indicates how data was recorded for the[Record](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Record)

|                                                                                                                                                                                                                                                                                                                                                                                                                                                                             Return                                                                                                                                                                                                                                                                                                                                                                                                                                                                             ||
|----------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) | Value is[android.health.connect.datatypes.Metadata#RECORDING_METHOD_UNKNOWN](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata#RECORDING_METHOD_UNKNOWN:kotlin.Int),[android.health.connect.datatypes.Metadata#RECORDING_METHOD_ACTIVELY_RECORDED](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata#RECORDING_METHOD_ACTIVELY_RECORDED:kotlin.Int),[android.health.connect.datatypes.Metadata#RECORDING_METHOD_AUTOMATICALLY_RECORDED](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata#RECORDING_METHOD_AUTOMATICALLY_RECORDED:kotlin.Int), or[android.health.connect.datatypes.Metadata#RECORDING_METHOD_MANUAL_ENTRY](https://developer.android.com/reference/kotlin/android/health/connect/datatypes/Metadata#RECORDING_METHOD_MANUAL_ENTRY:kotlin.Int) |

### hashCode

Added in[API level 34](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels)  

```
fun hashCode(): Int
```

Returns a hash code value for the object.

|                                                     Return                                                     ||
|----------------------------------------------------------------------------|------------------------------------|
| [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) | a hash code value for this object. |