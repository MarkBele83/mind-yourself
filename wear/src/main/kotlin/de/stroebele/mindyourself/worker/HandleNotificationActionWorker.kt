package de.stroebele.mindyourself.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.stroebele.mindyourself.domain.model.ReminderType
import de.stroebele.mindyourself.domain.repository.HydrationRepository
import de.stroebele.mindyourself.domain.repository.ReminderStateRepository
import de.stroebele.mindyourself.domain.repository.SupplementRepository
import de.stroebele.mindyourself.notification.Action
import de.stroebele.mindyourself.notification.NotificationDispatcher
import de.stroebele.mindyourself.notification.NotificationId
import java.time.Instant

@HiltWorker
class HandleNotificationActionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val hydrationRepository: HydrationRepository,
    private val supplementRepository: SupplementRepository,
    private val reminderStateRepository: ReminderStateRepository,
    private val notificationDispatcher: NotificationDispatcher,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val actionName = inputData.getString(KEY_ACTION) ?: return Result.failure()
        val notifId = inputData.getInt(KEY_NOTIF_ID, -1)
        val supplementName = inputData.getString(KEY_SUPPLEMENT_NAME)

        when (Action.valueOf(actionName)) {
            Action.DISMISS -> notificationDispatcher.cancel(notifId)
            Action.SNOOZE_10 -> {
                notificationDispatcher.cancel(notifId)
                val type = notifId.toReminderType() ?: return Result.failure()
                reminderStateRepository.snooze(type, Instant.now().plusSeconds(600))
                Log.d(TAG, "Snoozed 10min: $type")
            }
            Action.SNOOZE_30 -> {
                notificationDispatcher.cancel(notifId)
                val type = notifId.toReminderType() ?: return Result.failure()
                reminderStateRepository.snooze(type, Instant.now().plusSeconds(1800))
                Log.d(TAG, "Snoozed 30min: $type")
            }
            Action.LOG_HYDRATION -> {
                hydrationRepository.log(DEFAULT_HYDRATION_ML)
                notificationDispatcher.cancel(notifId)
                Log.d(TAG, "Logged ${DEFAULT_HYDRATION_ML}ml hydration")
            }
            Action.LOG_SUPPLEMENT -> {
                supplementName?.let {
                    supplementRepository.log(it)
                    Log.d(TAG, "Logged supplement: $it")
                }
                notificationDispatcher.cancel(notifId)
            }
        }

        return Result.success()
    }

    companion object {
        const val KEY_ACTION = "action"
        const val KEY_NOTIF_ID = "notif_id"
        const val KEY_SUPPLEMENT_NAME = "supplement_name"
        private const val DEFAULT_HYDRATION_ML = 250
        private const val TAG = "HandleNotificationActionWorker"
    }
}

private fun Int.toReminderType(): ReminderType? = when (this) {
    NotificationId.MOVEMENT -> ReminderType.MOVEMENT
    NotificationId.SEDENTARY -> ReminderType.SEDENTARY
    NotificationId.HYDRATION -> ReminderType.HYDRATION
    NotificationId.SUPPLEMENT -> ReminderType.SUPPLEMENT
    NotificationId.SCREEN_BREAK -> ReminderType.SCREEN_BREAK
    else -> null
}
