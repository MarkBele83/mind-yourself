package de.stroebele.mindyourself.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import de.stroebele.mindyourself.domain.model.ReminderType
import de.stroebele.mindyourself.receiver.NotificationActionReceiver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationDispatcher @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val manager = context.getSystemService(NotificationManager::class.java)

    init {
        createChannel()
    }

    fun showMovementReminder() = show(
        id = NotificationId.MOVEMENT,
        title = "Zeit zum Bewegen!",
        text = "Du warst in den letzten 2 Stunden wenig aktiv.",
        actions = listOf(
            action("Jetzt bewegen", Action.DISMISS, NotificationId.MOVEMENT),
            action("Snooze 10 min", Action.SNOOZE_10, NotificationId.MOVEMENT),
        )
    )

    fun showSedentaryReminder() = show(
        id = NotificationId.SEDENTARY,
        title = "Zu lange gesessen?",
        text = "Steh kurz auf und streck dich.",
        actions = listOf(
            action("Jetzt bewegen", Action.DISMISS, NotificationId.SEDENTARY),
            action("Snooze 10 min", Action.SNOOZE_10, NotificationId.SEDENTARY),
        )
    )

    fun showHydrationReminder(remainingMl: Int) = show(
        id = NotificationId.HYDRATION,
        title = "Trinkerinnerung",
        text = "Noch $remainingMl ml bis zu deinem Tagesziel.",
        actions = listOf(
            action("Getrunken", Action.LOG_HYDRATION, NotificationId.HYDRATION),
            action("Snooze 30 min", Action.SNOOZE_30, NotificationId.HYDRATION),
        )
    )

    fun showSupplementReminder(name: String) = show(
        id = NotificationId.SUPPLEMENT,
        title = name,
        text = "Zeit für dein Supplement.",
        actions = listOf(
            action("Genommen", Action.LOG_SUPPLEMENT.withExtra(EXTRA_SUPPLEMENT_NAME, name), NotificationId.SUPPLEMENT),
            action("Später erinnern", Action.SNOOZE_30, NotificationId.SUPPLEMENT),
        )
    )

    fun showScreenBreakReminder() = show(
        id = NotificationId.SCREEN_BREAK,
        title = "Bildschirmpause",
        text = "Gönn deinen Augen eine kurze Pause.",
        actions = listOf(
            action("Pause starten", Action.DISMISS, NotificationId.SCREEN_BREAK),
            action("Überspringen", Action.DISMISS, NotificationId.SCREEN_BREAK),
        )
    )

    fun cancel(notificationId: Int) = manager.cancel(notificationId)

    // ── Internal helpers ──────────────────────────────────────────────────────

    private fun show(
        id: Int,
        title: String,
        text: String,
        actions: List<NotificationCompat.Action>,
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .apply { actions.forEach(::addAction) }
            .build()

        manager.notify(id, notification)
    }

    private fun action(label: String, actionType: Action, notifId: Int): NotificationCompat.Action {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            putExtra(EXTRA_ACTION, actionType.name)
            putExtra(EXTRA_NOTIFICATION_ID, notifId)
        }
        val pi = PendingIntent.getBroadcast(
            context, actionType.ordinal + notifId * 100,
            intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action(0, label, pi)
    }

    private fun Action.withExtra(key: String, value: String): Action = this // extras set on intent in action()

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "mindYourself Reminder",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "mindyourself_reminders"
        const val EXTRA_ACTION = "action"
        const val EXTRA_NOTIFICATION_ID = "notif_id"
        const val EXTRA_SUPPLEMENT_NAME = "supplement_name"
    }
}

object NotificationId {
    const val MOVEMENT = 1
    const val SEDENTARY = 2
    const val HYDRATION = 3
    const val SUPPLEMENT = 4
    const val SCREEN_BREAK = 5
}

enum class Action {
    DISMISS,
    SNOOZE_10,
    SNOOZE_30,
    LOG_HYDRATION,
    LOG_SUPPLEMENT,
}
