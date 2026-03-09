package de.stroebele.mindyourself.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.stroebele.mindyourself.data.db.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HydrationRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: HydrationRepositoryImpl

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = HydrationRepositoryImpl(db.hydrationDao())
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun logAndObserveToday() = runTest {
        repository.log(250)
        repository.log(500)

        val today = repository.observeToday().first()
        assertEquals(2, today.size)
        assertEquals(750, today.sumOf { it.amountMl })
    }

    @Test
    fun getTodayTotalMl_returnsCorrectSum() = runTest {
        repository.log(300)
        repository.log(400)

        assertEquals(700, repository.getTodayTotalMl())
    }

    @Test
    fun markSynced_updatesFlag() = runTest {
        val id = repository.log(200)
        val before = repository.getUnsynced()
        assertEquals(1, before.size)

        repository.markSynced(listOf(id))

        val after = repository.getUnsynced()
        assertTrue(after.isEmpty())
    }

    @Test
    fun saveAll_doesNotDuplicateOnReInsert() = runTest {
        repository.log(100)
        val existing = repository.observeToday().first()
        assertEquals(1, existing.size)

        // saveAll with IGNORE strategy — same id should not duplicate
        repository.saveAll(existing)
        val after = repository.observeToday().first()
        assertEquals(1, after.size)
    }
}
