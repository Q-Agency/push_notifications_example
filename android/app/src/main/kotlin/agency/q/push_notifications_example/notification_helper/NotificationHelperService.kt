package agency.q.push_notifications_example.notification_helper

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import agency.q.push_notifications_example.MainActivity
import agency.q.push_notifications_example.R
import java.util.concurrent.atomic.AtomicInteger


class NotificationHelperService : FirebaseMessagingService() {

    companion object {
        private val notificationID = AtomicInteger(1000)
        private fun newNotificationID(): Int = notificationID.incrementAndGet()
    }

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createHighImportanceNotificationChannel(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        buildNotification(remoteMessage)
    }


    fun buildNotification(remoteMessage: RemoteMessage) {
        val defaultChannel = "high_importance_channel"
        val notificationIdToUse = newNotificationID()
        val category = remoteMessage.data["category"]

        when (category) {
            "ACTIONS_CATEGORY" -> showNotificationActionCategory(remoteMessage,defaultChannel,
                notificationIdToUse)
            else -> showNotificationDefault(remoteMessage,defaultChannel,notificationIdToUse)
        }

    }

    private fun contentPendingIntent(remoteMessage: RemoteMessage,notificationIdToUse: Int): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(NotificationActions.EXTRA_NOTIFICATION_ID, notificationIdToUse)

        return PendingIntent.getActivity(
            this,
            notificationIdToUse,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showNotificationActionCategory(remoteMessage: RemoteMessage,channelId: String = "misc",notificationIdToUse: Int) {
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.q_agency)
            .setContentTitle(remoteMessage.data["title"] ?: "No Title")
            .setContentText(remoteMessage.data["body"] ?: "No Body")
            .setContentIntent(contentPendingIntent(remoteMessage,notificationIdToUse))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(NotificationActions.accept(this,notificationIdToUse))
            .addAction(NotificationActions.reply(this,notificationIdToUse))
            .addAction(NotificationActions.decline(this,notificationIdToUse))
        notify(notificationBuilder,notificationIdToUse)

    }
    fun showNotificationDefault(remoteMessage: RemoteMessage,channelId: String = "misc",notificationIdToUse: Int) {
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.q_agency)
            .setContentTitle(remoteMessage.data.get("title") ?: "No Title")
            .setContentText(remoteMessage.data.get("body") ?: "No Body")
            .setContentIntent(contentPendingIntent(remoteMessage,notificationIdToUse))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notify(notificationBuilder,notificationIdToUse)

    }

    fun notify(notificationBuilder: NotificationCompat.Builder, notificationIdToUse: Int) {
        val nm =
            this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notificationIdToUse, notificationBuilder.build())
    }

}