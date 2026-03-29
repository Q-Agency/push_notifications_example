package agency.q.push_notifications_example

import android.content.Intent
import android.os.Bundle
import androidx.core.app.RemoteInput
import agency.q.push_notifications_example.notification_helper.NotificationChannels
import agency.q.push_notifications_example.notification_helper.NotificationActions
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val channelName = "agency.q.push_notifications_example"
    private var methodChannel: MethodChannel? = null
    private var pendingOpenPayload: Map<String, Any?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationChannels.createHighImportanceNotificationChannel(this)
        captureNotificationOpen(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        captureNotificationOpen(intent)
        flushIfPossible()
    }

    override fun onResume() {
        super.onResume()
        val currentIntent = intent ?: return
        val nid = currentIntent.getIntExtra(NotificationActions.EXTRA_NOTIFICATION_ID, -1)
        if (nid != -1) {
            captureNotificationOpen(currentIntent)
            flushIfPossible()
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        methodChannel = MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            channelName
        ).also { channel ->
            channel.setMethodCallHandler { call, result ->
                when (call.method) {
                    "getInitialNotification" -> {
                        val payload = pendingOpenPayload
                        pendingOpenPayload = null
                        result.success(payload)
                    }
                    "dartReady" -> {
                        flushIfPossible()
                        result.success(null)
                    }
                    else -> result.notImplemented()
                }
            }
        }
    }

    override fun onDestroy() {
        methodChannel?.setMethodCallHandler(null)
        methodChannel = null
        super.onDestroy()
    }

    private fun captureNotificationOpen(intent: Intent?) {
        if (intent == null) return

        val notificationID = intent.getIntExtra(NotificationActions.EXTRA_NOTIFICATION_ID, -1)
        if (notificationID == -1) return

        val replyText = RemoteInput.getResultsFromIntent(intent)
            ?.getCharSequence(NotificationActions.REMOTE_INPUT_KEY)
            ?.toString()

        pendingOpenPayload = mapOf(
            "action" to intent.action,
            "reply" to replyText,
        )

        NotificationActions.cancelIfPresent(this, intent)

        intent.removeExtra(NotificationActions.EXTRA_NOTIFICATION_ID)
    }

    private fun flushIfPossible() {
        val channel = methodChannel ?: return
        val payload = pendingOpenPayload ?: return
        channel.invokeMethod("onNotification", payload)
        pendingOpenPayload = null
    }
}