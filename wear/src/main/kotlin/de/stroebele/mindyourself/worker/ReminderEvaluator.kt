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
        if (now.epochSecond - lastFired.epochSecond < config.repeatIntervalMinutes * 60L) return false
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
        dailyGoalMl: Int,
        lastLogTime: Instant?,
        now: Instant,
        lastFired: Instant,
        currentTime: LocalTime,
        activeFrom: LocalTime,
        activeUntil: LocalTime,
    ): Boolean {
        if (now.epochSecond - lastFired.epochSecond < config.repeatIntervalMinutes * 60L) return false
        val goal = if (dailyGoalMl > 0) dailyGoalMl else config.reminderGoalMl
        if (todayMl >= goal) return false

        val secondsSinceLastLog = lastLogTime?.let { now.epochSecond - it.epochSecond } ?: Long.MAX_VALUE
        val noRecentLog = secondsSinceLastLog >= config.noLogWindowMinutes * 60L

        val totalWindowSecs = windowDurationSeconds(activeFrom, activeUntil)
        val elapsedSecs = elapsedInWindowSeconds(activeFrom, currentTime)
        val intakeFraction = todayMl.toDouble() / goal
        val timeFraction = if (totalWindowSecs > 0) elapsedSecs.toDouble() / totalWindowSecs else 0.0
        val behindSchedule = intakeFraction < timeFraction

        return noRecentLog || behindSchedule
    }

    private fun windowDurationSeconds(from: LocalTime, until: LocalTime): Long =
        if (from <= until) (until.toSecondOfDay() - from.toSecondOfDay()).toLong()
        else (86400 - from.toSecondOfDay() + until.toSecondOfDay()).toLong()

    private fun elapsedInWindowSeconds(from: LocalTime, current: LocalTime): Long =
        if (current >= from) (current.toSecondOfDay() - from.toSecondOfDay()).toLong()
        else (86400 - from.toSecondOfDay() + current.toSecondOfDay()).toLong()

    fun supplementShouldFire(
        config: SupplementConfig,
        currentTime: LocalTime,
        now: Instant,
        lastFired: Instant,
    ): Boolean {
        if (now.epochSecond - lastFired.epochSecond < 3600L) return false
        return Math.abs(currentTime.toSecondOfDay() - config.scheduledTime.toSecondOfDay()) <= 600
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
