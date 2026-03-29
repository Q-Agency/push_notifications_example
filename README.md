# <img src="q_agency.svg" alt="Q" width="28" valign="middle"/>&nbsp; push_notifications_example

Push notification **actions** (the inline Accept / Decline / Reply buttons on a notification) are not readily available out of the box in Flutter or in the `firebase_messaging` plugin. This demo shows one way to add them by going native on each side and routing the user's response back to Dart through a single `MethodChannel`.

Accompanying the blog post: [TODO: blog link].

## Notes

### Flow

Notifications are intercepted on the native side, the user's tap or action is stashed, and the payload is then exposed to Flutter through a `MethodChannel`. The [`flutter_local_notifications`](https://pub.dev/packages/flutter_local_notifications) package could replace parts of this on Android to render the notifications with action buttons, on iOS to register the notification categories. That variation will live in a separate branch.

### Android

- FCM messages must be sent **data only**. `notification` key payloads are intercepted by `firebase_messaging` itself and rendered before our code runs.
- `category` is not officially supported on Android. Workaround: include it as a `data` field and intercept the message to render a local notification with the desired action buttons.
- Notification **actions** are not officially supported by Firebase Messaging.
- Background work for an action (e.g. Accept) can run via the Flutter `onBackgroundMessage` handler.

### iOS

- Upload an APNs Auth Key (`.p8`) in the Firebase Console under Cloud Messaging.
- Notification **categories** are supported by `firebase_messaging`, but must be registered manually. **Actions** are not supported.
- `onBackgroundMessage` is not supported on iOS due to platform limits.
- Background work for an action must be done native (Swift), since Dart isn't available from a background notification response on iOS.

## Sample payload

Send to the FCM HTTP v1 endpoint (`https://fcm.googleapis.com/v1/projects/<PROJECT_ID>/messages:send`). Replace `<FCM_TOKEN>` with the device token printed by the app in debug builds. The same payload works for both platforms, FCM ignores the `apns` block on Android and the `android` block on iOS.

```json
{
  "message": {
    "token": "<FCM_TOKEN>",
    "apns": {
      "payload": {
        "aps": {
          "alert": {
            "title": "Hello from postman"
          },
          "sound": "default",
          "category": "ACTIONS_CATEGORY"
        }
      }
    },
    "android": {
      "notification": {
        "channel_id": "high_importance_channel"
      },
      "data": {
        "category": "ACTIONS_CATEGORY",
        "title": "Hello from postman",
        "body": "Notification body"
      }
    }
  }
}
```

## Authors

- **Author:** Zvonimir Babić
- **Mentor:** Ivan Celija
