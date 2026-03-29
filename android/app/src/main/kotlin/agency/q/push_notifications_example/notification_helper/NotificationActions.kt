package agency.q.push_notifications_example.notification_helper

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import agency.q.push_notifications_example.MainActivity

object NotificationActions {
    const val ACTION_ACCEPT = "ACTION_ACCEPT"
    const val ACTION_DECLINE = "ACTION_DECLINE"
    const val ACTION_REPLY = "ACTION_REPLY"

    const val ACTION_ACCEPT_ID = 1
    const val ACTION_DECLINE_ID = 2
    const val ACTION_REPLY_ID = 3

    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    private const val FROM_NOTIFICATION_VALUE = "1"

    const val REMOTE_INPUT_KEY = "REMOTE_INPUT_KEY"

    private fun requestCode(actionId: Int, notificationId: Int): Int {
        return actionId * 100_000 + notificationId
    }

    fun accept(context: Context, id: Int): NotificationCompat.Action {
        val activityPI = PendingIntent.getActivity(
            context,
            requestCode(ACTION_ACCEPT_ID, id),
            Intent(context, MainActivity::class.java)
                .setAction(ACTION_ACCEPT)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(EXTRA_NOTIFICATION_ID, id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(
            android.R.drawable.checkbox_on_background,
            "Accept",
            activityPI
        ).build()
    }

    fun decline(context: Context, id: Int): NotificationCompat.Action {
        val activityPI = PendingIntent.getActivity(
            context,
            requestCode(ACTION_DECLINE_ID, id),
            Intent(context, MainActivity::class.java)
                .setAction(ACTION_DECLINE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(EXTRA_NOTIFICATION_ID, id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(
            android.R.drawable.checkbox_on_background,
            "Decline",
            activityPI
        ).build()
    }

    fun reply(context: Context, id: Int): NotificationCompat.Action {
        val activityPI = PendingIntent.getActivity(
            context,
            requestCode(ACTION_REPLY_ID, id),
            Intent(context, MainActivity::class.java)
                .setAction(ACTION_REPLY)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(EXTRA_NOTIFICATION_ID, id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val remoteInput = RemoteInput.Builder(REMOTE_INPUT_KEY)
            .setLabel("Reply")
            .build()

        return NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            "Reply",
            activityPI
        )
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()
    }

    fun cancelIfPresent(context: Context, intent: Intent?) {
        if (intent == null) return
        val notificationID = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        if (notificationID == -1) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(notificationID)
    }
}