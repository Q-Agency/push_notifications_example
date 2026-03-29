import Foundation
import UserNotifications

struct NotificationActions {

    static let actionAccept  = "ACTION_ACCEPT"
    static let actionDecline = "ACTION_DECLINE"
    static let actionReply   = "ACTION_REPLY"

    static let actionsCategoryIdentifier = "ACTIONS_CATEGORY"

    static func notificationCategories() -> Set<UNNotificationCategory> {
        let acceptAction = UNNotificationAction(
            identifier: actionAccept,
            title: "Accept",
            options: [.foreground]
        )
        let declineAction = UNNotificationAction(
            identifier: actionDecline,
            title: "Decline",
            options: [.destructive, .foreground]
        )
        let replyAction = UNTextInputNotificationAction(
            identifier: actionReply,
            title: "Reply",
            options: [.foreground],
            textInputButtonTitle: "Send",
            textInputPlaceholder: "Type your reply…"
        )
        let actionsCategory = UNNotificationCategory(
            identifier: actionsCategoryIdentifier,
            actions: [acceptAction, replyAction, declineAction],
            intentIdentifiers: [], // ie. "INSendMessageIntent"
            options: [] // ie. .allowInCarPlay
        )
        return [actionsCategory]
    }

    static func cancelIfPresent(identifier: String) {
        UNUserNotificationCenter.current().removeDeliveredNotifications(
            withIdentifiers: [identifier]
        )
    }
}

