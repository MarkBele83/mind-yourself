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
    fun `movement fires when steps below threshold and window elapsed`() {
        val config = MovementConfig(stepThreshold = 500, windowMinutes = 120)
        val lastFired = Instant.EPOCH
        val now = Instant.ofEpochSecond(3600L * 3) // 3h after epoch
        assertTrue(ReminderEvaluator.movementShouldFire(config, stepsSinceWindowStart = 100, now, lastFired))
    }

    @Test
    fun `movement does not fire when steps meet threshold`() {
        val config = MovementConfig(stepThreshold = 500, windowMinutes = 120)
        val lastFired = Instant.EPOCH
        val now = Instant.ofEpochSecond(3600L * 3)
        assertFalse(ReminderEvaluator.movementShouldFire(config, stepsSinceWindowStart = 600, now, lastFired))
    }

    @Test
    fun `movement does not fire when within re-fire window`() {
        val config = MovementConfig(stepThreshold = 500, windowMinutes = 120)
        val now = Instant.ofEpochSecond(3600L)
        val lastFired = now.minusSeconds(60) // only 1 min ago — window is 120 min
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

    @Test
    fun `hydration fires when goal not reached and interval elapsed`() {
        val config = HydrationConfig(dailyGoalMl = 2000, intervalMinutes = 60)
        val lastFired = Instant.EPOCH
        val now = Instant.ofEpochSecond(3600L * 3)
        assertTrue(ReminderEvaluator.hydrationShouldFire(config, todayMl = 500, now, lastFired))
    }

    @Test
    fun `hydration does not fire when daily goal reached`() {
        val config = HydrationConfig(dailyGoalMl = 2000, intervalMinutes = 60)
        val lastFired = Instant.EPOCH
        val now = Instant.ofEpochSecond(3600L * 3)
        assertFalse(ReminderEvaluator.hydrationShouldFire(config, todayMl = 2000, now, lastFired))
    }

    @Test
    fun `hydration does not fire before interval elapsed`() {
        val config = HydrationConfig(dailyGoalMl = 2000, intervalMinutes = 60)
        val now = Instant.ofEpochSecond(3600L * 3)
        val lastFired = now.minusSeconds(30 * 60) // 30 min ago, interval is 60 min
        assertFalse(ReminderEvaluator.hydrationShouldFire(config, todayMl = 500, now, lastFired))
    }

    // ── Supplement ────────────────────────────────────────────────────────────

    @Test
    fun `supplement fires when within scheduled time window`() {
        val config = SupplementConfig(
            supplementName = "Vitamin D",
            scheduledTimes = listOf(LocalTime.of(8, 0)),
        )
        val currentTime = LocalTime.of(8, 5) // 5 min after schedule — within ±10 min
        val lastFired = Instant.EPOCH
        val now = Instant.ofEpochSecond(3600L * 24)
        assertTrue(ReminderEvaluator.supplementShouldFire(config, currentTime, now, lastFired))
    }

    @Test
    fun `supplement does not fire outside scheduled time window`() {
        val config = SupplementConfig(
            supplementName = "Vitamin D",
            scheduledTimes = listOf(LocalTime.of(8, 0)),
        )
        val currentTime = LocalTime.of(12, 0) // 4h after schedule
        val lastFired = Instant.EPOCH
        val now = Instant.ofEpochSecond(3600L * 24)
        assertFalse(ReminderEvaluator.supplementShouldFire(config, currentTime, now, lastFired))
    }

    @Test
    fun `supplement does not re-fire within 1 hour`() {
        val config = SupplementConfig(
            supplementName = "Vitamin D",
            scheduledTimes = listOf(LocalTime.of(8, 0)),
        )
        val currentTime = LocalTime.of(8, 5)
        val now = Instant.ofEpochSecond(3600L * 24)
        val lastFired = now.minusSeconds(30 * 60) // 30 min ago
        assertFalse(ReminderEvaluator.supplementShouldFire(config, currentTime, now, lastFired))
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
