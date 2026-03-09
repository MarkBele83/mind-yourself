package de.stroebele.mindyourself.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.stroebele.mindyourself.domain.model.ActivityState
import de.stroebele.mindyourself.domain.model.HydrationConfig
import de.stroebele.mindyourself.domain.model.MovementConfig
import de.stroebele.mindyourself.domain.model.ReminderConfig
import de.stroebele.mindyourself.domain.model.ReminderType
import de.stroebele.mindyourself.domain.model.ScreenBreakConfig
import de.stroebele.mindyourself.domain.model.SedentaryConfig
import de.stroebele.mindyourself.domain.model.SupplementConfig
import de.stroebele.mindyourself.domain.repository.HealthCacheRepository
import de.stroebele.mindyourself.domain.repository.HydrationRepository
import de.stroebele.mindyourself.domain.repository.NamedLocationRepository
import de.stroebele.mindyourself.domain.repository.ReminderConfigRepository
import de.stroebele.mindyourself.domain.repository.ReminderStateRepository
import de.stroebele.mindyourself.domain.repository.VacationSettingsRepository
import de.stroebele.mindyourself.location.LocationResolver
import de.stroebele.mindyourself.notification.NotificationDispatcher
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

/**
 * Runs periodically (every 15 minutes) and evaluates all enabled reminder rules.
 * Checks snooze state, minimum re-fire interval, and optional location filter
 * before dispatching a notification.
 */
@HiltWorker
class ReminderEvaluationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val reminderConfigRepository: ReminderConfigRepository,
    private val reminderStateRepository: ReminderStateRepository,
    private val healthCacheRepository: HealthCacheRepository,
    private val hydrationRepository: HydrationRepository,
    private val namedLocationRepository: NamedLocationRepository,
    private val locationResolver: LocationResolver,
    private val notificationDispatcher: NotificationDispatcher,
    private val vacationSettingsRepository: VacationSettingsRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val now = Instant.now()
        val localNow = now.atZone(ZoneId.systemDefault())
        val currentTime = localNow.toLocalTime()
        val currentDay = localNow.dayOfWeek

        val configs = reminderConfigRepository.getAll()
        val namedLocations = namedLocationRepository.getAll()

        val vacationSettings = vacationSettingsRepository.observe().first()
        val now2 = LocalDateTime.now()
        val isVacationActive = vacationSettings.periods.any { now2 >= it.from && now2 <= it.until }

        for (config in configs) {
            if (!config.enabled) continue
            if (isVacationActive && !config.activeInVacation) continue
            if (!config.activeDays.contains(currentDay)) continue
            if (!ReminderEvaluator.isInActiveWindow(currentTime, config.activeFrom, config.activeUntil)) continue

            val state = reminderStateRepository.getState(config.type)
            if (state.isSnoozed) {
                Log.d(TAG, "${config.type} is snoozed until ${state.snoozeUntil}")
                continue
            }

            if (!isLocationAllowed(config, namedLocations, locationResolver)) {
                Log.d(TAG, "${config.type} skipped — location filter not satisfied")
                continue
            }

            try {
                val fired = when (config.type) {
                    ReminderType.MOVEMENT -> evaluateMovement(config.typeConfig as MovementConfig, now, state.lastFired)
                    ReminderType.SEDENTARY -> evaluateSedentary(config.typeConfig as SedentaryConfig, now, state.lastFired)
                    ReminderType.HYDRATION -> evaluateHydration(config.typeConfig as HydrationConfig, now, state.lastFired)
                    ReminderType.SUPPLEMENT -> evaluateSupplement(config.typeConfig as SupplementConfig, currentTime, now, state.lastFired)
                    ReminderType.SCREEN_BREAK -> evaluateScreenBreak(config.typeConfig as ScreenBreakConfig, now, state.lastFired)
                }
                if (fired) reminderStateRepository.markFired(config.type, now)
            } catch (e: Exception) {
                Log.e(TAG, "Error evaluating reminder ${config.type}", e)
            }
        }

        return Result.success()
    }

    /** Returns true if the reminder is allowed to fire at the current location. */
    private fun isLocationAllowed(
        config: ReminderConfig,
        namedLocations: List<de.stroebele.mindyourself.domain.model.NamedLocation>,
        resolver: LocationResolver,
    ): Boolean {
        val filter = config.locationFilter ?: return true // no filter = always allowed
        val currentLocation = resolver.resolve(namedLocations)
        if (currentLocation == null) {
            // Unknown location: fire only if strictMode is off
            return !filter.strictMode
        }
        return currentLocation.id in filter.allowedLocationIds
    }

    /** Returns true if a notification was dispatched. */
    private suspend fun evaluateMovement(config: MovementConfig, now: Instant, lastFired: Instant): Boolean {
        val windowStart = now.minusSeconds(config.windowMinutes * 60L)
        val steps = healthCacheRepository.getStepsBetween(windowStart, now)
        if (ReminderEvaluator.movementShouldFire(config, steps, now, lastFired)) {
            Log.d(TAG, "Movement: $steps steps in ${config.windowMinutes}min < threshold ${config.stepThreshold}")
            notificationDispatcher.showMovementReminder()
            return true
        }
        return false
    }

    private suspend fun evaluateSedentary(config: SedentaryConfig, now: Instant, lastFired: Instant): Boolean {
        val passiveDurationMs = healthCacheRepository.continuousDurationInState(ActivityState.PASSIVE, now)
        if (ReminderEvaluator.sedentaryShouldFire(config, passiveDurationMs, now, lastFired)) {
            Log.d(TAG, "Sedentary: ${passiveDurationMs / 60_000}min >= threshold ${config.inactiveThresholdMinutes}min")
            notificationDispatcher.showSedentaryReminder()
            return true
        }
        return false
    }

    private suspend fun evaluateHydration(config: HydrationConfig, now: Instant, lastFired: Instant): Boolean {
        val todayMl = hydrationRepository.getTodayTotalMl()
        if (ReminderEvaluator.hydrationShouldFire(config, todayMl, now, lastFired)) {
            Log.d(TAG, "Hydration: ${todayMl}ml logged, ${config.dailyGoalMl - todayMl}ml remaining")
            notificationDispatcher.showHydrationReminder(config.dailyGoalMl - todayMl)
            return true
        }
        return false
    }

    private fun evaluateSupplement(config: SupplementConfig, currentTime: LocalTime, now: Instant, lastFired: Instant): Boolean {
        if (ReminderEvaluator.supplementShouldFire(config, currentTime, now, lastFired)) {
            Log.d(TAG, "Supplement: ${config.supplementName}")
            notificationDispatcher.showSupplementReminder(config.supplementName)
            return true
        }
        return false
    }

    private fun evaluateScreenBreak(config: ScreenBreakConfig, now: Instant, lastFired: Instant): Boolean {
        if (ReminderEvaluator.screenBreakShouldFire(config, now, lastFired)) {
            Log.d(TAG, "Screen break: ${config.intervalMinutes}min interval elapsed")
            notificationDispatcher.showScreenBreakReminder()
            return true
        }
        return false
    }

    companion object {
        private const val TAG = "ReminderEvaluationWorker"
        private const val WORK_NAME = "reminder_evaluation"

        fun schedule(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<ReminderEvaluationWorker>(
                15, TimeUnit.MINUTES
            ).build()
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
