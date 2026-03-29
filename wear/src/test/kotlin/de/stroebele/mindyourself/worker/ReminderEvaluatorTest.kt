package de.stroebele.mindyourself.worker

import de.stroebele.mindyourself.domain.model.HydrationConfig
import de.stroebele.mindyourself.domain.model.MovementConfig
import de.stroebele.mindyourself.domain.model.ScreenBreakConfig
import de.stroebele.mindyourself.domain.model.SedentaryConfig
import de.stroebele.mindyourself.domain.model.SupplementConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalTime

class ReminderEvaluatorTest {

    // ── Movement ─────────────────────────────────────────────────────────────

    @Test
    fun `movement fires when steps below threshold and repeat interval elapsed`() {
        val config = MovementConfig(stepThreshold = 500, windowMinutes = 30, repeatIntervalMinutes = 30)
        val lastFired = Instant.EPOCH
        val now = Instant.ofEpochSecond(3600L * 3) // 3h after epoch
        assertTrue(ReminderEvaluator.movementShouldFire(config, stepsSinceWindowStart = 100, now, lastFired))
    }

    @Test
    fun `movement does not fire when steps meet threshold`() {
        val config = MovementConfig(stepThreshold = 500, windowMinutes = 30, repeatIntervalMinutes = 30)
        val lastFired = Instant.EPOCH
        val now = Instant.ofEpochSecond(3600L * 3)
        assertFalse(ReminderEvaluator.movementShouldFire(config, stepsSinceWindowStart = 600, now, lastFired))
    }

    @Test
    fun `movement does not fire when within repeat interval`() {
        val config = MovementConfig(stepThreshold = 500, windowMinutes = 30, repeatIntervalMinutes = 30)
        val now = Instant.ofEpochSecond(3600L)
        val lastFired = now.minusSeconds(60) // only 1 min ago — repeat interval is 30 min
        assertFalse(ReminderEvaluator.movementShouldFire(config, stepsSinceWindowStart = 100, now, lastFired))
    }

    // ── Sedentary ─────────────────────────────────────────────────────────────

    @Test
    fun `sedentary fires when passive duration exceeds threshold`() {
        val config = SedentaryConfig(inactiveThresholdMinutes = 45, repeatIntervalMinutes = 60)
        val lastFired = Instant.EPOCH
        val now = Instant.ofEpochSecond(3600L * 3)
        val passiveDurationMs = 50L * 60 * 1000 // 50 minutes
        assertTrue(ReminderEvaluator.sedentaryShouldFire(config, passiveDurationMs, now, lastFired))
    }

    @Test
    fun `sedentary does not fire when passive duration below threshold`() {
        val config = SedentaryConfig(inactiveThresholdMinutes = 45, repeatIntervalMinutes = 60)
        val lastFired = Instant.EPOCH
        val now = Instant.ofEpochSecond(3600L * 3)
        val passiveDurationMs = 30L * 60 * 1000 // 30 minutes
        assertFalse(ReminderEvaluator.sedentaryShouldFire(config, passiveDurationMs, now, lastFired))
    }

    @Test
    fun `sedentary does not fire before repeat interval`() {
        val config = SedentaryConfig(inactiveThresholdMinutes = 45, repeatIntervalMinutes = 60)
        val now = Instant.ofEpochSecond(3600L * 3)
        val lastFired = now.minusSeconds(30 * 60) // only 30 min ago
        val passiveDurationMs = 50L * 60 * 1000
        assertFalse(ReminderEvaluator.sedentaryShouldFire(config, passiveDurationMs, now, lastFired))
    }

    // ── Hydration ─────────────────────────────────────────────────────────────

    private val activeFrom = LocalTime.of(8, 0)
    private val activeUntil = LocalTime.of(22, 0)

    @Test
    fun `hydration fires when no recent log within window`() {
        val config = HydrationConfig(reminderGoalMl = 2000, repeatIntervalMinutes = 60, noLogWindowMinutes = 60)
        val now = Instant.ofEpochSecond(3600L * 12) // noon in epoch terms
        val lastFired = Instant.EPOCH
        // lastLogTime = null → no log at all
        assertTrue(
            ReminderEvaluator.hydrationShouldFire(
                config, todayMl = 500, dailyGoalMl = 2000, lastLogTime = null,
                now, lastFired, LocalTime.of(10, 0), activeFrom, activeUntil,
            )
        )
    }

    @Test
    fun `hydration fires when behind schedule by time fraction`() {
        val config = HydrationConfig(reminderGoalMl = 2000, repeatIntervalMinutes = 60, noLogWindowMinutes = 60)
        val now = Instant.ofEpochSecond(3600L * 12)
        val lastFired = Instant.EPOCH
        val recentLog = now.minusSeconds(10) // logged 10s ago — no "no recent log" trigger
        // Window 8–22 = 14h total. At 15:00 = 7h elapsed = 50% of window. 200/2000 = 10% → behind schedule
        assertTrue(
            ReminderEvaluator.hydrationShouldFire(
                config, todayMl = 200, dailyGoalMl = 2000, lastLogTime = recentLog,
                now, lastFired, LocalTime.of(15, 0), activeFrom, activeUntil,
            )
        )
    }

    @Test
    fun `hydration does not fire when daily goal reached`() {
        val config = HydrationConfig(reminderGoalMl = 2000, repeatIntervalMinutes = 60, noLogWindowMinutes = 60)
        val now = Instant.ofEpochSecond(3600L * 12)
        val lastFired = Instant.EPOCH
        assertFalse(
            ReminderEvaluator.hydrationShouldFire(
                config, todayMl = 2000, dailyGoalMl = 2000, lastLogTime = null,
                now, lastFired, LocalTime.of(10, 0), activeFrom, activeUntil,
            )
        )
    }

    @Test
    fun `hydration does not fire before repeat interval elapsed`() {
        val config = HydrationConfig(reminderGoalMl = 2000, repeatIntervalMinutes = 60, noLogWindowMinutes = 60)
        val now = Instant.ofEpochSecond(3600L * 12)
        val lastFired = now.minusSeconds(30 * 60) // 30 min ago, interval is 60 min
        assertFalse(
            ReminderEvaluator.hydrationShouldFire(
                config, todayMl = 200, dailyGoalMl = 2000, lastLogTime = null,
                now, lastFired, LocalTime.of(10, 0), activeFrom, activeUntil,
            )
        )
    }

    // ── Supplement ────────────────────────────────────────────────────────────

    private fun supplementConfig() = SupplementConfig(
        scheduledTime = LocalTime.of(8, 0),
        items = listOf(
            de.stroebele.mindyourself.domain.model.SupplementItem(
                name = "Vitamin D",
                amount = 1,
                form = de.stroebele.mindyourself.domain.model.SupplementForm.CAPSULE,
            )
        ),
    )

    @Test
    fun `supplement fires when within scheduled time window`() {
        val currentTime = LocalTime.of(8, 5) // 5 min after schedule — within ±10 min
        val lastFired = Instant.EPOCH
        val now = Instant.ofEpochSecond(3600L * 24)
        assertTrue(ReminderEvaluator.supplementShouldFire(supplementConfig(), currentTime, now, lastFired))
    }

    @Test
    fun `supplement does not fire outside scheduled time window`() {
        val currentTime = LocalTime.of(12, 0) // 4h after schedule
        val lastFired = Instant.EPOCH
        val now = Instant.ofEpochSecond(3600L * 24)
        assertFalse(ReminderEvaluator.supplementShouldFire(supplementConfig(), currentTime, now, lastFired))
    }

    @Test
    fun `supplement does not re-fire within 1 hour`() {
        val currentTime = LocalTime.of(8, 5)
        val now = Instant.ofEpochSecond(3600L * 24)
        val lastFired = now.minusSeconds(30 * 60) // 30 min ago
        assertFalse(ReminderEvaluator.supplementShouldFire(supplementConfig(), currentTime, now, lastFired))
    }

    // ── Screen Break ──────────────────────────────────────────────────────────

    @Test
    fun `screen break fires when interval elapsed`() {
        val config = ScreenBreakConfig(intervalMinutes = 45)
        val lastFired = Instant.EPOCH
        val now = Instant.ofEpochSecond(60L * 60) // 60 min later
        assertTrue(ReminderEvaluator.screenBreakShouldFire(config, now, lastFired))
    }

    @Test
    fun `screen break does not fire before interval elapsed`() {
        val config = ScreenBreakConfig(intervalMinutes = 45)
        val now = Instant.ofEpochSecond(3600L)
        val lastFired = now.minusSeconds(20 * 60) // 20 min ago, interval is 45 min
        assertFalse(ReminderEvaluator.screenBreakShouldFire(config, now, lastFired))
    }

    // ── Active window ─────────────────────────────────────────────────────────

    @Test
    fun `isInActiveWindow returns true when current is within window`() {
        assertTrue(ReminderEvaluator.isInActiveWindow(LocalTime.of(10, 0), LocalTime.of(8, 0), LocalTime.of(22, 0)))
    }

    @Test
    fun `isInActiveWindow returns false when current is outside window`() {
        assertFalse(ReminderEvaluator.isInActiveWindow(LocalTime.of(7, 0), LocalTime.of(8, 0), LocalTime.of(22, 0)))
    }

    @Test
    fun `isInActiveWindow handles midnight-wrap correctly`() {
        // Window 22:00–06:00 (overnight)
        assertTrue(ReminderEvaluator.isInActiveWindow(LocalTime.of(23, 0), LocalTime.of(22, 0), LocalTime.of(6, 0)))
        assertTrue(ReminderEvaluator.isInActiveWindow(LocalTime.of(2, 0), LocalTime.of(22, 0), LocalTime.of(6, 0)))
        assertFalse(ReminderEvaluator.isInActiveWindow(LocalTime.of(12, 0), LocalTime.of(22, 0), LocalTime.of(6, 0)))
    }
}
