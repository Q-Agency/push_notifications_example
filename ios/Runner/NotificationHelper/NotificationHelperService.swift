import Foundation
import UserNotifications
import Flutter

class NotificationHelperService: NSObject, UNUserNotificationCenterDelegate {

    static let shared = NotificationHelperService()

    var methodChannel: FlutterMethodChannel?

    var pendingOpenPayload: [String: Any?]?

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound, .badge])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let actionIdentifier = response.actionIdentifier

        let action: String? = (actionIdentifier == UNNotificationDefaultActionIdentifier)
            ? nil
            : actionIdentifier

        var replyText: String? = nil
        if let textResponse = response as? UNTextInputNotificationResponse {
            replyText = textResponse.userText
        }

        let payload: [String: Any?] = [
            "action": action,
            "reply": replyText,
        ]

        NotificationActions.cancelIfPresent(
            identifier: response.notification.request.identifier
        )

        if let channel = methodChannel {
            channel.invokeMethod("onNotification", arguments: payload)
        } else {
            pendingOpenPayload = payload
        }

        completionHandler()
    }

    func flushIfPossible() {
        guard let channel = methodChannel, let payload = pendingOpenPayload else { return }
        channel.invokeMethod("onNotification", arguments: payload)
        pendingOpenPayload = nil
    }
}

