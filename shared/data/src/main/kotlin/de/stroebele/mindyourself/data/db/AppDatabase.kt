package de.stroebele.mindyourself.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.stroebele.mindyourself.data.db.dao.HealthCacheDao
import de.stroebele.mindyourself.data.db.dao.HydrationDao
import de.stroebele.mindyourself.data.db.dao.HydrationPortionSizeDao
import de.stroebele.mindyourself.data.db.dao.NamedLocationDao
import de.stroebele.mindyourself.data.db.dao.ReminderConfigDao
import de.stroebele.mindyourself.data.db.dao.ReminderStateDao
import de.stroebele.mindyourself.data.db.dao.SupplementDao
import de.stroebele.mindyourself.data.db.entity.HealthCacheEntity
import de.stroebele.mindyourself.data.db.entity.HeartRateCacheEntity
import de.stroebele.mindyourself.data.db.entity.HydrationLogEntity
import de.stroebele.mindyourself.data.db.entity.HydrationPortionSizeEntity
import de.stroebele.mindyourself.data.db.entity.NamedLocationEntity
import de.stroebele.mindyourself.data.db.entity.ReminderConfigEntity
import de.stroebele.mindyourself.data.db.entity.ReminderStateEntity
import de.stroebele.mindyourself.data.db.entity.SupplementLogEntity

@Database(
    entities = [
        ReminderConfigEntity::class,
        ReminderStateEntity::class,
        HydrationLogEntity::class,
        SupplementLogEntity::class,
        HealthCacheEntity::class,
        HeartRateCacheEntity::class,
        NamedLocationEntity::class,
        HydrationPortionSizeEntity::class,
    ],
    version = 6,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderConfigDao(): ReminderConfigDao
    abstract fun reminderStateDao(): ReminderStateDao
    abstract fun hydrationDao(): HydrationDao
    abstract fun supplementDao(): SupplementDao
    abstract fun healthCacheDao(): HealthCacheDao
    abstract fun namedLocationDao(): NamedLocationDao
    abstract fun hydrationPortionSizeDao(): HydrationPortionSizeDao

    companion object {
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Adds the unique index on healthConnectId that was missing from
                // MIGRATION_4_5 on devices that were already at version 5.
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_hydration_logs_healthConnectId ON hydration_logs (healthConnectId)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE hydration_logs ADD COLUMN healthConnectId TEXT")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_hydration_logs_healthConnectId ON hydration_logs (healthConnectId)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS hydration_portion_sizes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        amountMl INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                // Default portion sizes
                listOf(150, 200, 300, 500, 750).forEach { ml ->
                    db.execSQL("INSERT INTO hydration_portion_sizes (amountMl) VALUES ($ml)")
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminder_configs ADD COLUMN activeInVacation INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS named_locations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        wifiSsid TEXT,
                        cellIds TEXT NOT NULL DEFAULT ''
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_named_locations_name ON named_locations(name)"
                )
                db.execSQL(
                    "ALTER TABLE reminder_configs ADD COLUMN locationFilterJson TEXT"
                )
            }
        }
    }
}
