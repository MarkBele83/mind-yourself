package de.stroebele.mindyourself.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import de.stroebele.mindyourself.domain.model.ReminderType
import de.stroebele.mindyourself.domain.model.SupplementForm
import de.stroebele.mindyourself.domain.model.SupplementItem
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

    fun showMovementReminder(steps: Long, stepThreshold: Int, windowMinutes: Int) = show(
        id = NotificationId.MOVEMENT,
        title = "Zeit zum Bewegen!",
        text = "$steps / $stepThreshold Schritte in den letzten $windowMinutes Minuten.",
        timeoutMs = 5_000L,
        actions = listOf(
            action("Schließen", Action.DISMISS, NotificationId.MOVEMENT),
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

    fun showHydrationReminder(todayMl: Int, dailyGoalMl: Int) {
        val percent = if (dailyGoalMl > 0) (todayMl * 100 / dailyGoalMl) else 0
        show(
            id = NotificationId.HYDRATION,
            title = "Trinkerinnerung",
            text = "$todayMl / $dailyGoalMl ml ($percent%)",
            timeoutMs = 5_000L,
            actions = listOf(
                action("Erfassen", Action.OPEN_HYDRATION_LOG, NotificationId.HYDRATION),
                action("Schließen", Action.DISMISS, NotificationId.HYDRATION),
            )
        )
    }

    fun showSupplementReminder(items: List<SupplementItem>) {
        val text = items.joinToString("\n") { "${it.amount} ${it.form.display(it.amount)} ${it.name}" }
        show(
            id = NotificationId.SUPPLEMENT,
            title = "Zeit für deine Supplemente",
            text = text,
            actions = listOf(
                action("Genommen", Action.LOG_SUPPLEMENT, NotificationId.SUPPLEMENT),
                action("Später erinnern", Action.SNOOZE_30, NotificationId.SUPPLEMENT),
            )
        )
    }

    private fun SupplementForm.display(amount: Int): String = when (this) {
        SupplementForm.CAPSULE -> if (amount == 1) "Kapsel" else "Kapseln"
        SupplementForm.PILL -> if (amount == 1) "Tablette" else "Tabletten"
        SupplementForm.DROP -> "Tropfen"
        SupplementForm.GUM -> if (amount == 1) "Gummie" else "Gummies"
    }

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
        timeoutMs: Long? = null,
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .apply { timeoutMs?.let { setTimeoutAfter(it) } }
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
    OPEN_HYDRATION_LOG,
}
