import Flutter
import UIKit
import UserNotifications

@main
@objc class AppDelegate: FlutterAppDelegate, FlutterImplicitEngineDelegate {

  private let channelName = "agency.q.push_notifications_example"

  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {

    let center = UNUserNotificationCenter.current()

    center.setNotificationCategories(NotificationActions.notificationCategories())
    center.delegate = NotificationHelperService.shared

    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }

  func didInitializeImplicitFlutterEngine(_ engineBridge: FlutterImplicitEngineBridge) {
    GeneratedPluginRegistrant.register(with: engineBridge.pluginRegistry)

    guard let registrar = engineBridge.pluginRegistry.registrar(forPlugin: "push_notification_helper") else { return }
    let binaryMessenger = registrar.messenger()

    let methodChannel = FlutterMethodChannel(
      name: channelName,
      binaryMessenger: binaryMessenger
    )

    methodChannel.setMethodCallHandler { (call, result) in
      switch call.method {
      case "getInitialNotification":
        let payload = NotificationHelperService.shared.pendingOpenPayload
        NotificationHelperService.shared.pendingOpenPayload = nil
        result(payload)
      case "dartReady":
        NotificationHelperService.shared.flushIfPossible()
        result(nil)
      default:
        result(FlutterMethodNotImplemented)
      }
    }

    NotificationHelperService.shared.methodChannel = methodChannel
  }
}

