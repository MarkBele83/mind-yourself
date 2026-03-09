package de.stroebele.mindyourself.worker

import de.stroebele.mindyourself.domain.model.HydrationConfig
import de.stroebele.mindyourself.domain.model.MovementConfig
import de.stroebele.mindyourself.domain.model.ScreenBreakConfig
import de.stroebele.mindyourself.domain.model.SedentaryConfig
import de.stroebele.mindyourself.domain.model.SupplementConfig
import java.time.Instant
import java.time.LocalTime

/**
 * Pure evaluation logic for each reminder type.
 * Extracted from [ReminderEvaluationWorker] for unit testability.
 * All methods are stateless and do not access repositories or Android APIs.
 */
object ReminderEvaluator {

    /** True if enough time has passed since [lastFired] for a re-fire. */
    fun movementShouldFire(
        config: MovementConfig,
        stepsSinceWindowStart: Long,
        now: Instant,
        lastFired: Instant,
    ): Boolean {
        if (now.epochSecond - lastFired.epochSecond < config.windowMinutes * 60L) return false
        return stepsSinceWindowStart < config.stepThreshold
    }

    fun sedentaryShouldFire(
        config: SedentaryConfig,
        passiveDurationMs: Long,
        now: Instant,
        lastFired: Instant,
    ): Boolean {
        if (now.epochSecond - lastFired.epochSecond < config.repeatIntervalMinutes * 60L) return false
        return passiveDurationMs / 60_000 >= config.inactiveThresholdMinutes
    }

    fun hydrationShouldFire(
        config: HydrationConfig,
        todayMl: Int,
        now: Instant,
        lastFired: Instant,
    ): Boolean {
        if (now.epochSecond - lastFired.epochSecond < config.intervalMinutes * 60L) return false
        return config.dailyGoalMl - todayMl > 0
    }

    fun supplementShouldFire(
        config: SupplementConfig,
        currentTime: LocalTime,
        now: Instant,
        lastFired: Instant,
    ): Boolean {
        if (now.epochSecond - lastFired.epochSecond < 3600L) return false
        return config.scheduledTimes.any { scheduled ->
            Math.abs(currentTime.toSecondOfDay() - scheduled.toSecondOfDay()) <= 600
        }
    }

    fun screenBreakShouldFire(
        config: ScreenBreakConfig,
        now: Instant,
        lastFired: Instant,
    ): Boolean {
        return now.epochSecond - lastFired.epochSecond >= config.intervalMinutes * 60L
    }

    /** True if [current] is within the [from]..[until] window (handles midnight wrap). */
    fun isInActiveWindow(current: LocalTime, from: LocalTime, until: LocalTime): Boolean =
        if (from <= until) current in from..until
        else current >= from || current <= until
}
