package de.stroebele.mindyourself.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.stroebele.mindyourself.data.db.AppDatabase
import de.stroebele.mindyourself.domain.model.HydrationConfig
import de.stroebele.mindyourself.domain.model.MovementConfig
import de.stroebele.mindyourself.domain.model.ReminderConfig
import de.stroebele.mindyourself.domain.model.ReminderType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.DayOfWeek
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class ReminderConfigRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: ReminderConfigRepositoryImpl

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = ReminderConfigRepositoryImpl(db, db.reminderConfigDao())
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun saveAndGetById() = runTest {
        val id = repository.save(movementConfig())
        val loaded = repository.getById(id)

        assertEquals(ReminderType.MOVEMENT, loaded?.type)
        assertEquals("Bewegung", loaded?.label)
    }

    @Test
    fun observeAll_emitsInsertedConfig() = runTest {
        repository.save(movementConfig())
        repository.save(hydrationConfig())

        val all = repository.observeAll().first()
        assertEquals(2, all.size)
    }

    @Test
    fun delete_removesConfig() = runTest {
        val id = repository.save(movementConfig())
        repository.delete(id)

        assertNull(repository.getById(id))
    }

    @Test
    fun replaceAll_replacesExistingConfigs() = runTest {
        repository.save(movementConfig())
        repository.save(hydrationConfig())

        val replacement = listOf(movementConfig().copy(label = "Neue Bewegung"))
        repository.replaceAll(replacement)

        val all = repository.getAll()
        assertEquals(1, all.size)
        assertEquals("Neue Bewegung", all.first().label)
    }

    @Test
    fun typeConfig_roundTrip_preservesMovementConfig() = runTest {
        val original = MovementConfig(stepThreshold = 300, windowMinutes = 90)
        val id = repository.save(movementConfig().copy(typeConfig = original))
        val loaded = repository.getById(id)

        val loaded_tc = loaded?.typeConfig as? MovementConfig
        assertEquals(300, loaded_tc?.stepThreshold)
        assertEquals(90, loaded_tc?.windowMinutes)
    }

    @Test
    fun toggleEnabled_persists() = runTest {
        val id = repository.save(movementConfig())
        val config = repository.getById(id)!!
        repository.save(config.copy(enabled = false))

        val updated = repository.getById(id)
        assertEquals(false, updated?.enabled)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun movementConfig() = ReminderConfig(
        type = ReminderType.MOVEMENT,
        label = "Bewegung",
        activeDays = DayOfWeek.entries.toSet(),
        activeFrom = LocalTime.of(8, 0),
        activeUntil = LocalTime.of(22, 0),
        typeConfig = MovementConfig(),
    )

    private fun hydrationConfig() = ReminderConfig(
        type = ReminderType.HYDRATION,
        label = "Hydration",
        activeDays = DayOfWeek.entries.toSet(),
        activeFrom = LocalTime.of(8, 0),
        activeUntil = LocalTime.of(22, 0),
        typeConfig = HydrationConfig(),
    )
}
