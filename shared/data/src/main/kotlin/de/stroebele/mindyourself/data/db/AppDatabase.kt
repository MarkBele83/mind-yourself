package de.stroebele.mindyourself.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.stroebele.mindyourself.data.db.dao.HealthCacheDao
import de.stroebele.mindyourself.data.db.dao.HydrationDao
import de.stroebele.mindyourself.data.db.dao.NamedLocationDao
import de.stroebele.mindyourself.data.db.dao.ReminderConfigDao
import de.stroebele.mindyourself.data.db.dao.ReminderStateDao
import de.stroebele.mindyourself.data.db.dao.SupplementDao
import de.stroebele.mindyourself.data.db.entity.HealthCacheEntity
import de.stroebele.mindyourself.data.db.entity.HeartRateCacheEntity
import de.stroebele.mindyourself.data.db.entity.HydrationLogEntity
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
    ],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderConfigDao(): ReminderConfigDao
    abstract fun reminderStateDao(): ReminderStateDao
    abstract fun hydrationDao(): HydrationDao
    abstract fun supplementDao(): SupplementDao
    abstract fun healthCacheDao(): HealthCacheDao
    abstract fun namedLocationDao(): NamedLocationDao

    companion object {
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
