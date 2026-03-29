package agency.q.push_notifications_example.notification_helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {

  const val CHANNEL_HIGH_IMPORTANCE_ID = "high_importance_channel"

  fun createHighImportanceNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val highImportanceChannel = NotificationChannel(
      NotificationChannels.CHANNEL_HIGH_IMPORTANCE_ID,
      "High importance channel",
      NotificationManager.IMPORTANCE_HIGH
    ).apply {
      description = "High importance notifications"
      enableLights(true)
      enableVibration(true)
      setShowBadge(true)
    }

    val nm = context.getSystemService(NotificationManager::class.java)
    nm.createNotificationChannel(highImportanceChannel)
  }
}